# Delta Specification: Vectorized Columnar Query Engine & Embedded Jetty API

> **Background & Detailed Reference**: See [`docs/LIVEREPORTS.md`](../../../../docs/LIVEREPORTS.md) for the complete query engine architecture and API contract.

## ADDED Requirements

### Requirement: Selective Columnar Scan and Zone Map Pruning
`ColumnarQueryEngine` SHALL prune `DiskChunk`s using time range, `RoaringBitmap` dictionary indexes, and V3 Zone Maps (`maxRunTime`, `hasFailures`), and SHALL decompress only the columns required by the query's filter, group-by, and metric projections.

#### Scenario: Pruning clean or fast chunks during outlier queries
- **WHEN** a query filters by `failedOnly = true` or `minRunTime > X`
- **THEN** `ScanPredicate` skips any `DiskChunk` where `hasFailures == false` or `maxRunTime < X` prior to performing any disk read or column decompression.

#### Scenario: Analytical chart scan skips forensic tail block
- **WHEN** a time-series or summary aggregation query executes over `RequestChunk`s
- **THEN** `ColumnarQueryEngine` reads only the vector payload and does not read or decompress the forensic tail block.

### Requirement: Embedded Jetty Query Endpoints and Dump Mount
XLT SHALL provide an embedded Jetty server (`--serve [port]`) exposing `/api/v1/metadata`, `/api/v1/summary-table`, `/api/v1/timeseries`, `/api/v1/inspect`, `/api/v1/session`, and read-only `/dumps/*` static file serving.

#### Scenario: Slicing the summary table by time window and backend IP
- **WHEN** a client sends a `POST /api/v1/summary-table` request with `[fromTime, toTime]` and `usedIpAddress` filter
- **THEN** the server returns recalculated endpoint statistics (`count`, `cph`, `rate`, `mean`, `p50`, `p95`, `p99`, `p999`, `ttfb`, `conn`, `rx`, `errors`) matching the modernized report's data schema.

#### Scenario: Inspecting a failed or slow request via `/api/v1/inspect`
- **WHEN** a client queries `POST /api/v1/inspect` for failed or slowest outlier requests in a time window
- **THEN** each returned record includes the exact `url`, `requestId`, `responseId`, `formData`, `formDataEncoding`, pre-merge `rawName`, network timing breakdown, and `userNumber`.

#### Scenario: Reconstructing a virtual user's session via `/api/v1/session`
- **WHEN** a client queries `GET /api/v1/session` for `(agentName, testCaseName, userNumber)` within `[t1, t2]`
- **THEN** the server returns the chronological sequence of `TransactionData`, `ActionData`, `RequestData`, `PageLoadTimingData`, and `EventData` records executed by that virtual user instance, including Result Browser dump links.
