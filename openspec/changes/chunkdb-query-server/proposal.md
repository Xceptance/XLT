# Proposal: Vectorized Columnar Query Engine & Embedded Jetty API (Part 2 of 3)

> **Full Reference & Conversation History**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete architectural analysis, quantitative space/speed benchmarks (100M requests), and the chronological design conversation.
> **Sequence**: Part 2 of 3 (`chunkdb-v3-completeness` $\rightarrow$ `chunkdb-query-server` $\rightarrow$ `chunkdb-explorer-ui`).

## Why

Once `ChunkDB` V3 (`chunkdb-v3-completeness`) stores complete forensic data, `userNumber` correlation, and chunk-level Zone Maps, we need a high-speed analytical query engine and HTTP endpoint layer. Currently, `ChunkQueryEngine` reconstitutes `RequestData` POJOs across all 18+ columns to feed `StatisticsProcessor`, which takes seconds on 100M-row datasets and cannot dynamically group by secondary dimensions (`usedIpAddress`, `agentName`, `responseCode`, `contentType`) or reconstruct virtual user sessions in real time.

## What Changes

1. **Vectorized Columnar Query Engine (`com.xceptance.xlt.report.query`)**:
   - Implement `ColumnarQueryEngine` that reads and decompresses **only** the columns required by a query's filter, group-by, and metric projections—bypassing `RequestData` instantiation, `StatisticsProcessor`, and the cold Forensic Tail Block during chart/table aggregations.
   - Support dynamic time-window filtering, dynamic time-bucket resolution, multi-dimensional grouping (`timerName`, `agentName`, `testCaseName`, `usedIpAddress`, `responseCode`, `httpMethod`, `contentType`), lock-free thread-local percentile/histogram aggregation (`IntTimeSeries` + `RuntimeHistogram`), outlier record inspection (`inspectOutliers`), and Virtual User session tracing (`traceUserSession`).
2. **Embedded Jetty Server & REST API (`com.xceptance.xlt.report.server`)**:
   - Add `--serve [port]` support to `ReportGeneratorMain` to mount `xlt-cache/`, the static report directory, and read-only `/dumps/*` (for 1-click Result Browser access) on an embedded Jetty server.
   - Expose stateless JSON endpoints (`/api/v1/metadata`, `/api/v1/summary-table`, `/api/v1/timeseries`, `/api/v1/inspect`, `/api/v1/session`).

## Non-Goals

- No SQL parser or heavy third-party query dependencies (e.g., Apache Calcite, DuckDB).
- Does not build the frontend `/explorer` UI or `chunkdb-bridge.js` DOM adapter (covered in Part 3: `chunkdb-explorer-ui`).
