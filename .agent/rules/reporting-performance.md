---
description: Performance tuning, memory constraints, and diagnostic rules for XLT reporting
globs: src/main/java/com/xceptance/xlt/report/**/*.java
---

# Reporting Performance & Memory Invariants

## 1. Two-Pass Ingestion Invariant
- **Never accumulate report statistics during Phase 1 (CSV ingestion).**
- `StatisticsProcessor` holds 12M+ `IntTimeSeriesEntry` and 4.2M+ bitsets, using 2.6–3.4 GB of heap on a 4 GB max heap (`-Xmx4g`). Running it concurrently with CSV reading and ChunkDB ingestion causes heap exhaustion and catastrophic Full GC thrashing (40+ pauses).
- **Phase 1 Ingestion**: In `Dispatcher.java`, stream parsed/merged data strictly to `ChunkIngestionCollector`. Keep Old Gen usage bounded (< 1.5 GB).
- **Phase 2 Scan**: In `DataProcessor.java`, after chunks are sealed, use `ChunkQueryEngine.executeScan(...)` to scan and aggregate directly into `StatisticsProcessor` in parallel across worker threads at 3.4M+ records/s.

## 2. Parsing & Merge Rule Optimization
- Java `Matcher` cannot evaluate on raw byte buffers without creating `String` instances.
- **Do not create byte-slice caching layers that allocate copies on cache misses.**
- Key the `Condition` cache on `CharSequence` (`String`).
- Rely on `RequestData.getUrl()` memoization so the UTF-8 `String` and `hashCode()` are computed once and reused across all subsequent merge rules with zero memory allocations and instant `O(1)` map lookups.
- For pure substring or plain-text matches (e.g. `UrlConditionText`), use `ByteSlice` with direct byte scanning (`com.xceptance.common.util.ByteSlice`) to avoid string materialization when regex evaluation is not needed.

## 3. Profiling & Diagnostics
- Monitor GC in real-time: `jstat -gcutil <pid> 1000` (FGC must remain 0 during Phase 1).
- Profile worker thread CPU: `top -b -n 1 -H -p <pid>` (check `DataParserThread` vs. `DataReaderThread`).
- Profile with JFR: `JAVA_OPTIONS="-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:StartFlightRecording=filename=profile.jfr,dumponexit=true,settings=profile -XX:FlightRecorderOptions=stackdepth=1024" ./bin/create_report.sh ...`
- Inspect allocation/CPU hotspots: `jfr summary profile.jfr` or `jfr print --events jdk.ExecutionSample,jdk.ObjectAllocationSample profile.jfr`.
- Component isolation: Run `BytePipelineBenchmark diag <resultsDir> <maxFiles> <threads>` to isolate CSV reading vs. ChunkDB ingestion vs. scan aggregation.
