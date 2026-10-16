# Design: Vectorized Columnar Query Engine & Embedded Jetty API (Part 2 of 3)

> **Full Reference & Conversation History**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete query execution design, latency budgets, and API contract.

## 1. Vectorized Columnar Query Engine (`ColumnarQueryEngine`)

Unlike `ChunkQueryEngine` (which reconstitutes `RequestData` POJOs for all columns to feed `StatisticsProcessor`), `ColumnarQueryEngine` executes queries directly on primitive arrays:

1. **Chunk Pruning (Tier 1)**:
   Prunes `DiskChunk` descriptors in microseconds using:
   - `[minTime, maxTime]` overlap
   - `RoaringBitmap` intersections on `timerNameIds` and `agentTestCaseIds`
   - V3 Zone Map checks: `if (query.failedOnly && !chunk.hasFailures()) skip;` and `if (query.minRunTime > chunk.getMaxRunTime()) skip;`
2. **Selective Column Decompression (Tier 2)**:
   - Reads only the vector payload (`readVectorPayload()`) of matching `DiskChunk`s, leaving the Forensic Tail Block on disk.
   - Decompresses filter columns first (`compTimeOffsets`, `compTimerNameIds`, `compAgentTestCaseIds`, plus any active predicate columns like `compRunTimes` or `compResponseCodes`).
   - Builds a chunk-local matching row mask (`int[] matchedRows`, `int matchCount`). If `matchCount == 0`, returns immediately without decompressing group-by or metric columns.
   - Decompresses only the requested `groupBy` column (e.g. `compUsedIps`) and requested metric columns (e.g. `compServerBusyTimes`).
3. **Lock-Free Thread-Local Aggregation (Tier 3)**:
   - Each `ForkJoinPool` worker thread aggregates matching rows into a thread-local accumulator map using `IntTimeSeries` for time-bucketed series and `RuntimeHistogram` for percentiles (`p50`, `p90`, `p95`, `p99`, `p99.9`).
4. **Forensic Outlier Inspector & Session Tracer**:
   - `inspectOutliers(InspectQuerySpec)` scans `compRunTimes` / `compResponseCodes` + Zone Maps using a bounded priority queue, and reads the Forensic Tail Block **only** for the winning top-N chunks/rows.
   - `traceUserSession(SessionQuerySpec)` scans `T`, `A`, `R`, `P`, `E` chunks matching `(agentName, testCaseName)` and filters `compUserNumbers == targetUserNumberId` to assemble a chronological session trace with Result Browser dump paths.

---

## 2. Embedded Jetty API Contract (`/api/v1/*`)

| Endpoint | Method | Purpose | Target Latency |
| :--- | :--- | :--- | :--- |
| `/api/v1/metadata` | `GET` | Global `minTime`, `maxTime`, row counts, `requestDetailsMode`, and dictionaries (`timerNames`, `agents`, `scenarios`, `usedIps`, `httpMethods`, `contentTypes`). | `< 5 ms` |
| `/api/v1/summary-table` | `POST` | Recomputes the full `window.ALL_REQUESTS` / `ALL_TRANSACTIONS` / `ALL_ACTIONS` summary table for an arbitrary `[fromTime, toTime]` and facet filter (Agent, IP, Scenario, Status). | `15 – 80 ms` |
| `/api/v1/timeseries` | `POST` | Returns columnar arrays (`[timestamps[], series1[], series2[]...]`) + histogram buckets for a filtered & grouped time-series query (native `uPlot` input format). | `10 – 60 ms` |
| `/api/v1/inspect` | `POST` | Reads the Forensic Tail Block for matching chunks and returns the top-N slowest or failed records with exact `url`, `requestId`, `responseId`, `formData`, network waterfall timings, and `userNumber`. | `5 – 30 ms` |
| `/api/v1/session` | `GET` | Given `(agentName, testCaseName, userNumber, aroundTime, windowMs)`, scans `R`, `T`, `A`, `P`, `E` chunks for that exact virtual user and returns their chronological session trace + Result Browser dump path. | `10 – 40 ms` |
| `/dumps/*` | `GET` | Read-only static file mount of `<results-dir>` so Result Browser links (`/dumps/<agent>/<tx>/<user>/output/<dir>/index.html`) open directly in the browser. | `< 5 ms` |
