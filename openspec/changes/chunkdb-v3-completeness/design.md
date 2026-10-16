# Design: ChunkDB V3 Binary Format & Data Completeness (Part 1 of 3)

> **Full Reference & Conversation History**: See [`docs/LIVEREPORTS.md`](../../../docs/LIVEREPORTS.md) for the complete critical review, space/speed cost tables (Options A/B/C), and ChunkDB V3 binary layout diagrams.

## 1. Virtual User Correlation (`compUserNumbers`)
- **Problem**: `DataParserThread.applyDataAdjustments()` currently sets `userNumber` only on `TransactionData`, preventing cross-type correlation of a single virtual user's session.
- **Design**:
  - Add `userNumber` (or interned `userNumberId` via `GlobalDictionaries.getOrCreateStringId(userNumber)`) to `AbstractData` / `Data` and populate it in `DataParserThread.applyDataAdjustments()` for all records (`R`, `T`, `A`, `C`, `P`, `W`, `V`, `E`).
  - Add `int[] compUserNumbers` (FastPFOR-compressed) to `RequestChunk`, `TimerChunk`, `DoubleValueChunk`, and `EventChunk`.
  - Because `DataReaderThread` reads one `<agent>/<testCase>/<userNumber>/timers.csv` file at a time, consecutive rows in a chunk share identical `userNumber` IDs, compressing via FastPFOR128 to `< 0.3 bytes/row`.

## 2. `RequestChunk` Completeness & Cold Forensic Tail Block
1. **Form Data Encoding**:
   - Intern `req.getFormDataEncoding()` via `GlobalDictionaries.getOrCreateStringId()` and store in a new FastPFOR column `compFormDataEncodings` in `RequestChunk`.
2. **Pre-Merge Raw Request Name**:
   - In `DataParserThread`, before `MergeRuleProcessor.postprocess(req)` mutates `req.getName()`, store `req.setRawName(req.getName())` if not already set.
3. **Cold Forensic Tail Block (`sparse` vs. `full`)**:
   - Controlled by property `com.xceptance.xlt.report.storage.requestDetailsMode = sparse | full` (default: `sparse`) or CLI flag `--preserve-all-details`.
   - Placed at the **very end** of `RequestChunk` binary payload (after all FastPFOR integer columns) and preceded by `int vectorPayloadLength` / `forensicTailOffset` in the chunk header so vectorized scans never read or decompress it.
   - **Mode `sparse` (Option C - Default)**:
     - During `RequestChunkHandler` accumulation, a row index `idx` (`0..count-1`) is selected for forensic preservation if:
       1. `req.hasFailed() || req.getResponseCode() >= 400 || req.getResponseCode() == 0`, OR
       2. `req.getRunTime()` ranks in the top-256 slowest requests in the current chunk.
     - Selected rows store `(rowIdx, url, requestId, responseId, formData, rawName)` into a chunk-local byte buffer compressed with `java.util.zip.Deflater(Deflater.BEST_SPEED)` on `seal()`.
   - **Mode `full` (Option B - Opt-in)**:
     - Every row `0..count-1` appends length-prefixed UTF-8 bytes of `(url, requestId, responseId, formData, rawName)` into a chunk-local byte slab compressed with `Deflater(Deflater.BEST_SPEED)` on `seal()`.

## 3. `TransactionChunk` Non-Failed Result Browser Dumps
- In `TransactionChunkHandler`, record diagnostic entries in `errorMap` whenever `td.hasFailed() || StringUtils.isNotEmpty(td.getDirectoryName())`.

## 4. `DiskChunk` Zone Maps & Column Offset Directory
- Add `int maxRunTime` and `boolean hasFailures` to `Chunk` and `DiskChunk` (serialized in `chunks.bin` tail index table, bumping `ChunkCatalog.VERSION` to `3` and `CacheFingerprint` version).
- Inside `RequestChunk` binary framing, store the byte length of the columnar vector payload (`vectorPayloadLength`) before the forensic tail block so `DiskChunk` can read either just the vectors (`readVectorPayload()`) or only the forensic tail block (`readForensicTail()`).
