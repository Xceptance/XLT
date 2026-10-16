# Tasks: ChunkDB V3 Data Completeness & Zone Maps (Part 1 of 3)

> **Full Reference & Context**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete architectural reference, benchmarks, and design conversation.

## Phase 1: ChunkDB V3 Data Completeness & Zone Maps

- [ ] **1.1 Propagate `userNumber` across all record types during ingestion**
  - Update `DataParserThread.applyDataAdjustments()` (and `AbstractData` / `Data` or chunk builders) so `userNumber` is available on `RequestData`, `ActionData`, `CustomData`, `PageLoadTimingData`, `WebVitalData`, `CustomValue`, and `EventData` (not just `TransactionData`).
  - Add `compUserNumbers` (FastPFOR-compressed interned string IDs) to `RequestChunk`, `TimerChunk`, `DoubleValueChunk`, and `EventChunk`.
- [ ] **1.2 Preserve non-failed Transaction Result Browser dumps**
  - Update `TransactionChunkHandler` to store `TransactionError` / diagnostic entries whenever `td.hasFailed() || StringUtils.isNotEmpty(td.getDirectoryName())`.
- [ ] **1.3 Add `formDataEncoding` and Cold Forensic Tail Block to `RequestChunk`**
  - Add `compFormDataEncodings` (interned via `GlobalDictionaries`) to `RequestChunk` and `RequestChunkHandler`.
  - Capture pre-merge `rawName` in `DataParserThread` prior to `MergeRuleProcessor.postprocess()`.
  - Implement the compressed Forensic Tail Block in `RequestChunkHandler` supporting `sparse` mode (default: all failed/4xx/5xx rows + top-256 slowest rows per chunk) and `full` mode (opt-in via `com.xceptance.xlt.report.storage.requestDetailsMode=full` or `--preserve-all-details` CLI flag) storing exact `(url, requestId, responseId, formData, rawName)`.
  - Store `vectorPayloadLength` / `forensicTailOffset` in the chunk header so vector scans only read the integer column bytes from `FileChannel`.
- [ ] **1.4 Add Zone Maps (`maxRunTime`, `hasFailures`) to `Chunk` & `DiskChunk` (Catalog V3)**
  - Extend `Chunk`, `DiskChunk`, `ChunkSpooler`, and `ChunkCatalog` (version `3`) to persist and load `maxRunTime` and `hasFailures` in the tail index table of `chunks.bin`.
  - Update `CacheFingerprint` format version so stale V2 caches regenerate cleanly.
- [ ] **1.5 Unit & round-trip tests for ChunkDB V3**
  - Verify `sparse` and `full` forensic preservation, `userNumber` round-tripping across all chunk types, non-failed transaction `directoryName` preservation, `DiskChunk` Zone Map persistence, and backward compatibility of existing report generation.
