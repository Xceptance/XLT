# Delta Specification: ChunkDB V3 Data Completeness & Zone Maps

> **Background & Detailed Reference**: See [`docs/LIVEREPORTS.md`](../../../../docs/LIVEREPORTS.md) for the complete data-completeness audit and space/speed benchmarks.

## ADDED Requirements

### Requirement: Virtual User Correlation Across All Chunk Types
The storage engine SHALL preserve the virtual user instance identifier (`userNumber`) across all chunk types (`R`, `T`, `A`, `C`, `P`, `W`, `V`, `E`) as a FastPFOR-compressed integer column referencing `GlobalDictionaries`.

#### Scenario: Round-tripping virtual user identifiers across all record types
- **WHEN** `RequestData`, `TransactionData`, `ActionData`, `CustomData`, `PageLoadTimingData`, `WebVitalData`, `CustomValue`, and `EventData` records are written to and read from `ChunkDB` V3
- **THEN** each record preserves its exact `userNumber` dictionary ID in `compUserNumbers`.

### Requirement: Configurable Request Forensic Tail Block (Sparse Outlier Default + Full Opt-In)
`RequestChunk` SHALL preserve `formDataEncoding` as an interned column and store high-cardinality forensic attributes (`url`, `requestId`, `responseId`, `formData`, `rawName`) in a compressed tail block at the end of the chunk payload.
- By default (`requestDetailsMode = sparse`), exact forensic attributes SHALL be stored for all failed requests (`failed == true` or `responseCode >= 400` or `responseCode == 0`) and the top-256 slowest requests per chunk.
- When configured with `requestDetailsMode = full` (or CLI `--preserve-all-details`), exact forensic attributes SHALL be stored for every request row in the chunk.

#### Scenario: Reading forensic details for failed and slowest requests in sparse mode
- **WHEN** a `RequestChunk` is sealed in `sparse` mode and its forensic tail block is queried for a failed or top-256 slowest row index
- **THEN** the chunk returns the exact `url`, `requestId`, `responseId`, `formData`, and pre-merge `rawName` for that row.

#### Scenario: Vector payload read skips forensic tail block
- **WHEN** a caller reads only the vector payload of a `DiskChunk`
- **THEN** only the FastPFOR integer columns are read and decompressed, without reading or inflating the forensic tail block bytes.

### Requirement: Non-Failed Transaction Dump Path Preservation
`TransactionChunk` SHALL preserve `TransactionData.directoryName` even when `hasFailed() == false`.

#### Scenario: Slow non-failed transaction with Result Browser dump directory
- **WHEN** a non-failed `TransactionData` record has a non-empty `directoryName`
- **THEN** `TransactionChunk` persists and restores `directoryName` on disk round-trip.

### Requirement: Chunk-Level Zone Maps for Latency and Failure Pruning
`DiskChunk` descriptors in `ChunkCatalog` (V3) SHALL store `maxRunTime` (int) and `hasFailures` (boolean) in the tail metadata index of `chunks.bin`.

#### Scenario: Persisting and loading V3 Zone Maps in chunks.bin
- **WHEN** chunks are spooled to `chunks.bin` and reloaded via `ChunkCatalog`
- **THEN** each `DiskChunk` exposes accurate `getMaxRunTime()` and `hasFailures()` values before reading any chunk payload from disk.
