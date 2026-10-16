# Tasks: Vectorized Columnar Query Engine & Embedded Jetty API (Part 2 of 3)

> **Full Reference & Context**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete architectural reference, benchmarks, and design conversation.
> **Prerequisite**: `chunkdb-v3-completeness` (Part 1).

## Phase 1: Vectorized Columnar Query Engine (`com.xceptance.xlt.report.query`)

- [ ] **1.1 Define `QuerySpec` and `ScanPredicate` V3 extensions**
  - Create `TimeSeriesQuerySpec`, `SummaryTableQuerySpec`, `InspectQuerySpec`, and `SessionQuerySpec` records (Jackson-serializable).
  - Extend `ScanPredicate` with Zone Map pruning (`failedOnly`, `minRunTime`, `maxRunTime`) and secondary dictionary filters (`usedIpIds`, `httpMethodIds`, `contentTypeIds`, `responseCodeRange`).
- [ ] **1.2 Implement `ColumnarQueryEngine` with selective column decompression**
  - Implement two-stage column decompression per chunk: decompress filter columns first, build a matching row index array (`int[] matchedRows`), and only decompress projected `groupBy` / metric columns when `matchCount > 0`.
  - Implement lock-free thread-local vector accumulators using `IntTimeSeries` and `RuntimeHistogram` across `ForkJoinPool` workers.
- [ ] **1.3 Implement Forensic Outlier Inspector & Virtual User Session Tracer**
  - Implement `inspectOutliers(InspectQuerySpec)` using a bounded priority queue across matching chunks and resolving exact `(url, requestId, responseId, formData, rawName)` from the Forensic Tail Block.
  - Implement `traceUserSession(SessionQuerySpec)` scanning matching `T`, `A`, `R`, `P`, `E` chunks for a specific `(agentName, testCaseName, userNumber)` and returning a chronological event tree with Result Browser dump paths.

## Phase 2: Embedded Jetty Server (`com.xceptance.xlt.report.server`)

- [ ] **2.1 Implement `ChunkExplorerServer` (Embedded Jetty)**
  - Add `--serve [port]` CLI option to `ReportGeneratorMain`.
  - Configure Jetty servlets for `/api/v1/metadata`, `/api/v1/summary-table`, `/api/v1/timeseries`, `/api/v1/inspect`, and `/api/v1/session`.
  - Configure read-only static file serving for the generated report (`/`) and Result Browser dumps (`/dumps/*` mapped to `<results-dir>`).
- [ ] **2.2 Query Engine & API integration tests**
  - Test query correctness, selective column decompression, HTTP endpoint JSON responses, and sub-100ms response times on multi-chunk datasets.
