# XLT Report Generator: Performance Tuning & Diagnostic Investigations

## 1. Overview & High-Level Architecture

The XLT Report Generator is designed to process massive load test result datasets (over 100M+ records across 50k+ compressed CSV files) within a constrained JVM heap (`-Xmx4g` by default).

To achieve maximum throughput while preventing memory exhaustion and garbage collection thrashing, the system utilizes a **Two-Pass Architecture**:

1. **Phase 1: CSV Ingestion & ChunkDB Spooling**
   - Multi-threaded readers decompress log files and parse raw CSV lines with minimal/zero allocations.
   - Request data objects are passed through the request merge rule pipeline.
   - Merged records are routed to `ChunkIngestionCollector`, which encodes columns using FastPFOR and dictionary IDs, sealing chunks into disk-backed `ChunkDB` files (`xlt-cache/`).
   - **Crucial Invariant:** `StatisticsProcessor` is **not active** during Phase 1. Heap consumption remains bounded (< 1.5 GB Old Gen on a 4 GB heap), maintaining zero Full GCs.

2. **Phase 2: Multi-Threaded ChunkDB Aggregation**
   - Once all logs are ingested and chunk files are sealed, `DataProcessor` initiates a parallel scan via `ChunkQueryEngine`.
   - Worker threads concurrently decompress chunks, reconstitute records, and stream them into `StatisticsProcessor`.
   - In this phase, 100% of the heap is dedicated to the in-memory statistics model (12M+ time series entries, 4.2M+ bitsets).
   - Throughput regularly reaches **3.4M – 3.75M records/second**.

3. **Subsequent Cached Runs**
   - If the input log files and configuration rules have not changed, Phase 1 is completely bypassed.
   - The report generator streams directly from `ChunkDB`, aggregating 100M+ records in under 35–40 seconds.

---

## 2. Investigation Case Study: Root Cause Analysis on Large Datasets

### Problem Statement
When running `./bin/create_report.sh` on a production dataset containing 108,790,200 records across 49,358 files (`bbw-2025-wah-2-281`), throughput steadily degraded as processing progressed. Phase 1 took **322 seconds** (337k lines/s) compared to the expected baseline (~180s / 600k+ lines/s).

### Diagnostic Investigation Playbook

#### Step 1: Rule Out Filesystem I/O Contention
- Moving data to local NVMe storage (`/tmp/xlt-result-...`) showed identical throughput degradation, proving the issue was CPU and/or memory contention, not disk latency.
- Checking system health via `uptime` and `top` showed high load averages.

#### Step 2: Garbage Collection Monitoring (`jstat`)
Running `jstat -gcutil <pid> 1000 10` revealed severe JVM thrashing:
```text
  S0     S1     E      O      M     CCS    YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT   
     -  99.62   4.65  98.48  98.95  94.95    142    18.861     38   22.410    82     1.104    42.375
```
- **Finding:** 40 Full GCs occurred during Phase 1, stopping the world for over 24.2 seconds!
- **Reason:** Old Generation was filled to 98% capacity, forcing the JVM to invoke continuous STW Full GCs.

#### Step 3: Thread CPU Distribution (`top -H`)
Inspecting worker threads via `top -b -n 1 -H -p <pid>`:
```text
    PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND
1860213 user      20   0   11,8g   3,5g  34484 R  95,0  11,2   4:15.51 DataParserThread
1860216 user      20   0   11,8g   3,5g  34484 R  94,7  11,2   4:14.94 DataParserThread
1860231 user      20   0   11,8g   3,5g  34484 S  14,3  11,2   0:12.57 DataReaderThread
```
- **Finding:** `DataParserThread` instances were pinned at 95% CPU, while `DataReaderThread` instances were idle (14% CPU), indicating that parsing and rule evaluation were the CPU bottleneck.

#### Step 4: Component Isolation with `BytePipelineBenchmark`
Running `BytePipelineBenchmark diag <maxFiles> <numThreads>` isolated individual pipeline stages:
- **Raw CSV Read + Decode:** 3.1M records/s
- **ChunkDB Ingestion Spooling:** 2.1M records/s
- **ChunkDB Scan + Statistics Aggregation:** 3.1M records/s
- **Result:** The ChunkDB engine itself had minimal overhead (~330 ns/record). The bottleneck existed in merge rule evaluation during CSV parsing.

---

## 3. Discovered Bottlenecks and Applied Solutions

### Bottleneck 1: Memory Overlap Between Ingestion and Statistics
- **The Issue:** Attempting to build report statistics in memory while simultaneously reading and spooling chunks caused heap exhaustion. On a 4 GB heap, `StatisticsProcessor` can hold 2.6–3.4 GB of bitsets and time series data. Sharing this space with file readers, buffers, and dictionary builders triggered 40+ Full GCs.
- **The Fix:** Enforce strict Two-Pass separation in `Dispatcher.java` and `DataProcessor.java`. In Phase 1, records are routed solely to `ChunkIngestionCollector`. In Phase 2, `ChunkQueryEngine.executeScan` aggregates directly into `StatisticsProcessor`.
- **Result:** Phase 1 heap usage dropped from 3.5 GB (100%) to ~1.2 GB, reducing Full GCs from 40 down to **0**.

### Bottleneck 2: Byte-Level Caching Overhead in `Condition.java`
- **The Issue:** Commit `c377a0370` added raw byte caching (`hasByteData`) to `Condition.java`. For regex URL rules (where `stopOnMatch = false`), each request was evaluated against 10+ URL conditions. On cache misses:
  1. `ByteSlice.copyOf(buf, off, len)` allocated a `new byte[len]` array and wrapper object.
  2. `new String(buf, off, len, UTF_8)` was created for Java `Matcher` (since Java regex engines cannot operate directly on byte buffers).
  3. On cache lookups, `ByteSlice.set(...)` reset the internal hash code, forcing a full polynomial hash recomputation byte-by-byte for every rule check.
  - Across 108.8M records and 10 rules, this generated **hundreds of millions of short-lived byte arrays** and burned 95% of thread CPU in byte hashing and array copying.
- **The Fix:** Reverted `Condition.java` cache to `LRUClockMap<CharSequence, MatchResult>`:
  - Delegating URL access to `requestData.getUrl()` materializes and memoizes the UTF-8 `String` once on `RequestData`.
  - Java automatically caches `String.hashCode()`.
  - All subsequent merge rules execute map lookups with zero allocation and instant `O(1)` hash reuse.
- **Result:** Phase 1 duration dropped from **322 seconds to 193 seconds** (a **40% reduction / 129 seconds shaved off**), and throughput increased from 337k to 563k lines/second.

### Bottleneck 3: ChunkBuilder Lifecycle & Object Pooling
- **The Issue:** Creating new `ChunkBuilder` objects on every chunk boundary triggered allocation spikes and GC pressure under high thread concurrency.
- **The Fix:** Implemented `acquireBuilder(typeCode)` object recycling pool in `ChunkIngestionCollector.java`, reusing arrays and buffers across chunk seals.

---

## 4. Performance Verification & Results

### Cold Run Performance (Full 108.8M Records)

| Stage | Before Fixes | After Two-Pass & Condition Fix | Improvement |
| :--- | :--- | :--- | :--- |
| **Phase 1 Ingestion** | 322,467 ms (337k lines/s) | **193,325 ms (563k lines/s)** | **-40.1% (-129.1s)** |
| **Full GCs (Phase 1)** | 40 pauses (24.2s freeze) | **0 Full GCs (0.00s)** | **100% eliminated** |
| **Old Gen Memory** | 3.48 GB (thrashing) | **1.2 GB (stable)** | **-65% peak memory** |
| **Phase 2 ChunkDB Scan** | N/A | **35,403 ms (3.42M recs/s)** | Fast multi-threaded scan |
| **Total Cold Report Time** | ~6.5 minutes | **4.5 minutes** | **~30% faster total** |

### Cached Run Performance (Direct ChunkDB Streaming)
```text
Reading Log Files...
Accelerating report generation using ChunkDB cache from: /tmp/xlt-result-.../xlt-cache
ChunkDB timing breakdown: handler.scan CPU time = 162,838 ms, statisticsProcessor.process CPU time = 398,591 ms across 16 threads
121,078,008 records read from cache - 40,931 ms (load: 1,510 ms, scan: 39,421 ms) - 2,958,100 records/s
```
- Re-running the report skips CSV decoding and merge rules entirely.
- All 121M records are read, decoded, and aggregated into statistics in **40.9 seconds**.

---

## 5. Diagnostic Cheat Sheet for Developers

When diagnosing performance or memory issues in the report generator:

```bash
# 1. Run report generation on test data
./bin/create_report.sh /path/to/results

# 2. Monitor GC in real-time
jps -l | grep ReportGeneratorMain
jstat -gcutil <PID> 1000

# 3. Check thread CPU distribution
top -b -n 1 -H -p <PID>

# 4. Check live heap object counts (if memory spikes)
jmap -histo:live <PID> | head -n 30

# 5. Run component-level microbenchmarks
JAVA_HOME=~/.sdkman/candidates/java/21.0.6-tem mvn test-compile
java -cp target/classes:target/test-classes:lib/* \
  com.xceptance.xlt.report.benchmark.BytePipelineBenchmark diag <path_to_results> 1000 16
```
