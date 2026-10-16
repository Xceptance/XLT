# Proposal: ChunkDB V3 Data Completeness & Zone Maps (Part 1 of 3)

> **Full Reference & Conversation History**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete architectural analysis, quantitative space/speed benchmarks (100M requests), data-completeness audit, and the chronological design conversation.
> **Sequence**: Part 1 of 3 (`chunkdb-v3-completeness` $\rightarrow$ `chunkdb-query-server` $\rightarrow$ `chunkdb-explorer-ui`).

## Why

With Stage 2 (`ChunkDB`), XLT parses raw load test logs into a compressed columnar store (`xlt-cache/`) with bounded heap memory (`< 80 MB`). However, before we can build a real-time forensic query engine or interactive explorer on top of `ChunkDB`, several critical diagnostic fields are dropped during Pass 1 ingestion:
1. Virtual user correlation (`userNumber`) is only preserved on `TransactionData`, preventing cross-type reconstruction of a single virtual user's session across requests, actions, page load timings, and events.
2. Five forensic fields on `RequestData` (`url`, `requestId`, `responseId`, `formData`, `formDataEncoding`) and the pre-merge request name (`rawName`) are either dropped or downsampled to 128 round-robin URL strings per timer name.
3. Non-failed transaction dump paths (`TransactionData.directoryName`) are ignored when `td.hasFailed() == false`, preventing Result Browser links on slow (non-failed) transactions.
4. `DiskChunk` descriptors in `chunks.bin` lack latency and failure Zone Maps (`maxRunTime`, `hasFailures`), which are needed to prune clean/fast chunks without disk I/O.

## What Changes

1. **Virtual User Correlation (`compUserNumbers`)**:
   - Propagate `userNumber` across all record types (`R`, `T`, `A`, `C`, `P`, `W`, `V`, `E`) during `DataParserThread` ingestion.
   - Store `compUserNumbers` as a FastPFOR-compressed integer column referencing `GlobalDictionaries` across `RequestChunk`, `TimerChunk`, `DoubleValueChunk`, and `EventChunk`.
2. **Non-Failed Transaction Result Browser Dumps**:
   - Preserve `TransactionData.directoryName` in `TransactionChunk` whenever `td.hasFailed() || StringUtils.isNotEmpty(td.getDirectoryName())`.
3. **Request Completeness & Cold Forensic Tail Block**:
   - Intern `formDataEncoding` in `GlobalDictionaries` and add `compFormDataEncodings` to `RequestChunk`.
   - Capture pre-merge `rawName` in `DataParserThread` prior to `MergeRuleProcessor.postprocess()`.
   - Add a cold **Forensic Tail Block** at the end of `RequestChunk` supporting two configurable modes (`com.xceptance.xlt.report.storage.requestDetailsMode`):
     - `sparse` (Default — Option C): Preserves exact `(url, requestId, responseId, formData, rawName)` for all failed/4xx/5xx requests plus the top-256 slowest requests per chunk (`< 2%` disk overhead, `0 MB` global heap overhead).
     - `full` (Opt-in — Option B): Preserves exact forensic fields for all rows in a chunk-local compressed byte slab (via property or `--preserve-all-details`).
   - Record `vectorPayloadLength` / `forensicTailOffset` in the chunk header so vectorized scans can read only the integer columns without reading the forensic tail block from disk.
4. **Chunk-Level Zone Maps (Catalog V3)**:
   - Add `int maxRunTime` and `boolean hasFailures` to `Chunk` and `DiskChunk` in the `chunks.bin` tail index table, bumping `ChunkCatalog.VERSION` to `3` and updating `CacheFingerprint`.

## Non-Goals

- Does not implement the `ColumnarQueryEngine` or Jetty HTTP endpoints (covered in Part 2: `chunkdb-query-server`).
- Does not modify the static HTML report or add UI components (covered in Part 3: `chunkdb-explorer-ui`).
