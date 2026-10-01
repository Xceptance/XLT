# Chunked Storage & Columnar Pseudo-DB Architecture (Stage 2)

## 1. Executive Summary & Motivation

In **Stage 1**, we addressed the primary ingestion bottleneck by replacing global locks with **thread-local, mergeable accumulators** (`IntTimeSeries`, `RuntimeHistogram`, `IntTimeSeriesEntry`). This eliminated lock contention, enabled 100% core utilization during CSV parsing, and yielded an immediate **>2.4x speedup** (~300,000+ lines/s).

However, pure streaming accumulation has an inherent architectural limitation:
**It eagerly flattens and condenses the time and metric dimensions into summary statistics.**

### The Slicing Bottleneck
When a user or automated reporting pipeline requests:
1. **Time Slicing into Fixed Blocks**: Slicing the test into consecutive 15-minute, 5-minute, or 1-minute intervals (e.g., progressive reporting, phase-based comparisons, trend curves).
2. **Warm-up / Ramp-down Exclusion**: Discarding the first 15 minutes of ramp-up or trailing cooldown to evaluate pure steady-state metrics.
3. **Ad-Hoc Zooming**: Investigating a transient degradation window (e.g., "minutes 25 through 38").
4. **Dimension Filtering**: Excluding noisy agents (e.g., `--exclude-agents ac0001`) or filtering specific transactions/errors.

In the streaming-only model, none of these queries can be answered without **re-reading, re-decompressing, and re-parsing all raw log files from scratch** (e.g., 49,358 `.gz` files across 45 agent directories on a 100M request test, taking minutes).

**Stage 2 Objective**: Decouple raw parsing from statistical aggregation by introducing an in-memory / cache-backed **Columnar Pseudo-DB ("Chunk DB")** that stores compact raw facts in immutable chunks, enabling **instant (sub-second) time slicing and filtering** without re-parsing CSV logs.

---

## 2. Core Architecture: Two-Phase Decoupling

Instead of streaming directly into final report accumulators, the reporting pipeline splits into two distinct, high-performance phases:

```
PHASE 1: INGESTION & CHUNK CREATION (Lock-Free, Streaming)
┌────────────────────────────────────────────────────────────────────────┐
│  49,358 raw .gz log files on disk                                      │
└────────────────────────────────────────────────────────────────────────┘
        │
   (Parallel Reader / Parser Threads)
        │
        ▼
   Thread-Local Columnar Chunk Writers (append rows into primitive arrays)
        │
        ▼ (when chunk reaches 64k rows or time threshold)
   Seal Chunk & Push to ConcurrentChunkStore (or optional disk cache)
        │
        ▼
┌────────────────────────────────────────────────────────────────────────┐
│  Immutable In-Memory Pseudo-DB (ConcurrentChunkStore)                 │
│  - Sharded columnar chunks of primitive arrays                         │
│  - Header metadata per chunk: [minTime, maxTime, rowCount]             │
│  - Zero GC overhead, compact footprint (~2 GB for 100M rows)           │
└────────────────────────────────────────────────────────────────────────┘

PHASE 2: INSTANT QUERY & AGGREGATION (Parallel Scan & Reduce)
┌────────────────────────────────────────────────────────────────────────┐
│  Query Request: e.g. [from: 15m, to: 45m, excludeAgent: ac0001]        │
└────────────────────────────────────────────────────────────────────────┘
        │
   Chunk Pruning (skip chunks where maxTime < from || minTime > to)
        │
        ▼
   Parallel Worker Threads scan surviving chunks:
     - Vectorized / branch-friendly scan over primitive arrays
     - Populate thread-local Stage 1 accumulators (IntTimeSeries, RuntimeHistogram)
        │
        ▼
   Merge thread-local accumulators (~10-50 ms)
        │
        ▼
   Generate Report XML & Charts (< 1 second total)
```

---

## 3. Storage Layout & Memory Budget

### 3.1 Row Structure for Requests (`RequestData`)
Each raw request row is stripped of object wrappers and decomposed into compact primitive columns:

| Field | Primitive Type | Size | Description |
| :--- | :--- | :--- | :--- |
| `timeOffset` | `int` | 4 bytes | Milliseconds relative to test start (supports runs up to 24.8 days) |
| `runTime` | `int` | 4 bytes | Response time in milliseconds |
| `timerNameId` | `short` / `int` | 2–4 bytes | Dictionary-encoded ID of the timer/request name |
| `agentId` | `short` | 2 bytes | Dictionary-encoded load agent ID |
| `transactionId` | `short` | 2 bytes | Dictionary-encoded parent transaction ID |
| `responseCode` | `short` | 2 bytes | HTTP status code (200, 404, 500, etc.) |
| `bytesReceived` | `int` | 4 bytes | Wire payload size |
| `flags` | `byte` | 1 byte | Bitmask: failed (bit 0), cached (bit 1), etc. |
| **Total per Row** | | **~21–23 bytes** | **Primitive array backing** |

### 3.2 Memory Math for Large-Scale Tests
For the real-world **BBW 100-Million Request Dataset**:

$$\begin{aligned}
100{,}000{,}000 \text{ requests} \times 22 \text{ bytes} &\approx \mathbf{2.20 \text{ GB}} \\
15{,}000{,}000 \text{ actions} \times 16 \text{ bytes} &\approx \mathbf{0.24 \text{ GB}} \\
570{,}000 \text{ transactions} \times 20 \text{ bytes} &\approx \mathbf{0.01 \text{ GB}} \\
1{,}100{,}000 \text{ custom timers} \times 16 \text{ bytes} &\approx \mathbf{0.02 \text{ GB}} \\
\hline
\mathbf{Total\ In\text{-}Memory\ Footprint} &\approx \mathbf{2.47 \text{ GB}}
\end{aligned}$$

Because the data is stored in large primitive array blocks (e.g., $65{,}536$ elements per chunk) rather than Java objects:
- **Zero object header overhead** (eliminates 16–24 bytes of Java object headers per record).
- **Zero garbage collection pressure** (arrays are allocated once, kept alive during analysis, or allocated off-heap).
- **Exceptional CPU L1/L2 cache locality** during sequential scans.

---

## 4. Chunk Design Options: Row-Count vs. Time-Windowed

Two primary chunking paradigms were evaluated:

### Option A: Fixed Row-Count Chunks (e.g. 64,536 rows / chunk)
- **Mechanism**: A parser thread appends incoming rows to an array until full, then seals it.
- **Header**: Stores `minTime`, `maxTime`, `count`.
- **Pros**: Perfectly uniform chunk size; trivial array allocation; natural load-balancing during parse.
- **Cons**: Time spans of chunks vary depending on traffic density (high traffic = short time span; low traffic = long time span). Chunks can overlap temporally across threads.

### Option B: Fixed Time-Window Chunks (e.g. 1-minute or 15-minute blocks)
- **Mechanism**: Ingestion sorts or assigns records into discrete time buckets (e.g. bucket 0 = [0..15m), bucket 1 = [15m..30m)).
- **Header**: Exact fixed time bounds.
- **Pros**: Ideal for 15-minute slice reporting. To extract a 15-minute report, simply pick the exact chunk(s) corresponding to that slice without filtering interior rows.
- **Cons**: Incoming logs from 45 distributed agents are not globally ordered in time. Parser threads would need to route records to multiple open bucket buffers or re-merge time buckets at the end.

### Option C: The Hybrid Chunk Model (Recommended)
1. **Parser-Level Chunks**: Threads write fixed-size row blocks (64k rows) with recorded `[minTime, maxTime]`.
2. **Chunk Index / Catalog**: An immutable catalog orders chunks by time envelope.
3. **Block-Level Pruning**: When querying an arbitrary time range $[T_{\text{start}}, T_{\text{end}}]$:
   - Chunks completely outside the range are discarded in $O(1)$.
   - Chunks completely inside the range are processed with unconditional vectorized scans (no `if (time >= start && time <= end)` branch checks per row).
   - Only boundary chunks (partially overlapping) evaluate the time condition per row.

---

## 5. Instant Time Slicing & Window Aggregation

### 5.1 Slicing into 15-Minute Blocks
For automated trend graphs, degradation tracking, or test-phase reports:
- The user specifies an interval (e.g. 15 minutes = 900 seconds).
- The query executor runs a parallel reduction:
  ```java
  for (Chunk chunk : chunksForInterval(intervalIndex)) {
      chunk.aggregateInto(intervalAccumulator);
  }
  ```
- Because chunks reside in memory (or fast NVMe memory-mapped files), aggregating an entire 15-minute slice (tens of millions of rows) takes **100–200 milliseconds**.

### 5.2 Ad-Hoc Zoom & Ramp-Up Stripping
- Stripping the first 15 minutes is simply a query with `timeOffset >= 900_000`.
- Generating a report for any arbitrary slice takes **~0.2 to 0.5 seconds**, compared to **10–15 minutes** in the legacy architecture.

---

## 6. Handling High Cardinality & Complex Attributes

### 6.1 URLs and Regex Merge Rules
- **Problem**: E-commerce tests often generate millions of pseudo-unique URLs due to dynamic path tokens (`/customers/{id}/baskets`, `/orders/{uuid}`). Holding an in-memory dictionary of millions of URL strings would consume gigabytes of heap.
- **Zero Heuristics Principle**: XLT must never make heuristic assumptions about URL formats or structure.
- **Resolution in Chunk DB**:
  1. **Standard Run (95%+ of cases)**: Regex merge rules are applied *once* during initial parse. The resolved `timerNameId` is stored in the chunk. The raw URL string is discarded from heap. Slicing and re-running never load or inspect URLs.
  2. **Distinct URL Count (HLL)**: Each chunk maintains a compact HyperLogLog sketch (`HllSketch`, ~2–8 KB) for cardinality reporting. Unioning sketches across chunks is instantaneous ($O(1)$).
  3. **Rule Realignment (Edge Case)**: If a user modifies regex merge rules and wants to realign timers without re-reading 49,358 archive files, raw URLs can be streamed sequentially during initial ingestion to a single local file (`.xlt-cache/urls.bin`). Re-evaluating rules scans this single local NVMe file at 1–2 GB/s instead of walking the archive.

### 6.2 Auxiliary Data (IPs, Content Types, Error Messages, Web Vitals)
- Auxiliary metrics have **tiny cardinality** (< 50 distinct IPs, < 20 MIME types, 3 HTTP methods).
- They do not need complex columnar indexing. They can be collected using thread-local mergeable collectors and aggregated per slice or test run.

---

## 7. Disk Persistence & Optional Local Cache (`.xlt-cache`)

To support instantaneous CLI re-runs across separate JVM invocations:
- When enabled, sealed chunks can be written sequentially to a cache directory in the test results folder (or temporary scratch location):
  - `.xlt-cache/requests.bin` (contiguous primitive columnar chunks)
  - `.xlt-cache/metadata.json` (timer names dictionary, agent mapping, chunk index)
- **Memory-Mapping (`mmap`)**:
  - Subsequent invocations of `reportgenerator` with `--from / --to` can memory-map `requests.bin` via `FileChannel.map` / Java Foreign Memory API.
  - Slicing and report generation run in **under 2 seconds from a cold CLI start**.
- **Resilience**: Caching must be strictly optional; if the test results folder is read-only (e.g. mounted archive or CI runner), the engine falls back to pure in-memory execution or a configured scratch directory.

---

## 8. Summary of Benefits vs. Streaming Baseline

| Feature | Legacy Pipeline (develop) | Stage 1 (Mergeable Accumulators) | Stage 2 (Columnar Chunk DB) |
| :--- | :--- | :--- | :--- |
| **Ingestion Speed** | ~112k–148k lines/s (lock contention) | ~286k–337k lines/s (lock-free) | ~300k–400k lines/s (lock-free append) |
| **Initial Report Time** | ~18–25 seconds | ~7.7–9.1 seconds | ~8–10 seconds |
| **15-Min Slicing / Zoom** | **Full Re-parse (18–25s)** | **Full Re-parse (7.7–9.1s)** | **Instant (~0.2 seconds)** |
| **Ramp-Up Exclusion** | Full Re-parse | Full Re-parse | **Instant (~0.2 seconds)** |
| **Memory Footprint** | Low (streaming summary) | Low (streaming summary) | **Controlled (~2.5 GB for 100M)** |
| **CLI Re-run with Cache** | Full Re-parse from disk | Full Re-parse from disk | **Instant via mmap (~1–2s)** |

---

## 9. Key Architectural Decisions for Stage 2 Implementation

1. **Storage Engine Choice**:
   - Pure on-heap primitive arrays (`int[]`, `short[]`, `byte[]`).
   - Off-heap / direct memory (`ByteBuffer` / Java Foreign Memory `Arena`).
   - Memory-mapped file backing for transparent caching and instant CLI re-runs.
2. **Chunk Size & Boundary Strategy**:
   - Fixed size (e.g., $65{,}536$ records per chunk block) with `[minTime, maxTime]` envelopes.
3. **Query Engine Integration**:
   - A `ChunkQueryEngine` that takes a query specification (`TimeRange`, `TimerFilter`, `AgentFilter`) and uses parallel worker threads to scan chunks into the existing Stage 1 `IntTimeSeries` / `RuntimeHistogram` structures.
