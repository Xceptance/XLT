## 1. Parity Test Harness

- [x] 1.1 Create `ReportParityTest`: Run known timer files through the unmodified (Phase 0) pipeline and capture the output XML fragments as reference texts.
- [x] 1.2 Add verification toggle: Run the same files through the new pipeline, assert that output XML exactly matches the reference texts.
- [x] 1.3 Validate detection: Intentionally modify a known parity XML file and certify the test fails.

## 2. Phase 1 — Typed Arrays & Iteration Cleanup

- [ ] 2.1 In `PostProcessedDataContainer`, remove `List<Data> data`.
- [ ] 2.2 Add 10 `SimpleArrayList` explicitly typed fields: `requests`, `transactions`, `actions`, `events`, `customTimers`, `jvmResourceUsage`, `customValues`, `pageLoadTimings`, `webVitals`, and `customData`.
- [ ] 2.3 Expose clear getter methods for each new field (e.g., `getRequests()`, `getCustomData()`).
- [ ] 2.4 Update `PostProcessedDataContainer.add(Data)` to use an `instanceof` sequence to route each record into its designated `SimpleArrayList`.
- [ ] 2.5 Ensure the local per-container `minimumTime` and `maximumTime` are updated during `add(Data)`.
- [ ] 2.6 Remove `sampleFactor` and `droppedLines` from `PostProcessedDataContainer` and from parser instantiations.
- [ ] 2.7 In `AbstractReportProvider`, remove `processDataRecord()` calling logic and the entire sampling compensation loop.
- [ ] 2.8 Update single-type concrete providers (e.g., `RequestsReportProvider`) to directly call `container.getRequests()`, iterate the list, and process without `instanceof` checks.
- [ ] 2.9 Update `SummaryReportProvider` to sequentially pull and iterate the 7 specific typed lists it requires (requests, actions, transactions, events, page loads, custom timers, and jvm data).
- [ ] 2.10 Run parity suite to confirm exact XML match. Benchmark parsing output to confirm method/iteration reduction benefits.

## 3. Phase 2 — Concurrent Actor Dispatch

- [ ] 3.1 Do not add internal synchronization (locks/synchronized blocks) to `ReportProvider` implementations.
- [ ] 3.2 In `StatisticsProcessor`, create an internal Actor wrapper class containing a `ReportProvider`, a `BlockingQueue`, and a `Thread.ofVirtual().start(...)` loop.
- [ ] 3.3 Spin up one Actor instance for ALL active providers uniformly.
- [ ] 3.4 Inside the Actor's VT loop: `take()` a task from the queue, call `provider.processAll()`, and signal the parser thread it has finished.
- [ ] 3.5 Rewrite `StatisticsProcessor.process(chunk)`: Create a `CountDownLatch` (or similar synchronization barrier) equal to the number of actors, and `put()` the chunk into every actor's queue.
- [ ] 3.6 In `StatisticsProcessor.process()`, call `latch.await()` to synchronize chunk completion before returning to the parser thread.
- [ ] 3.7 Delete the legacy `tryLock()` spin-loop and `Thread.yield()` logic.
- [ ] 3.8 Delete `lock()`, `unlock()`, and `wantsDataRecords()` from the `ReportProvider` interface and all implementations. Set `ConfigurationReportProvider` and `CustomLogsReportProvider` to execute an empty `processAll` block.
- [ ] 3.9 Run parity suite. Benchmark with `Dispatcher.DEFAULT_QUEUE_CHUNK_SIZE` to confirm queueing overhead scales appropriately.

## 4. Phase 3 — Byte-Level CSV Loading

- [ ] 4.1 Create `ByteCsvDecoder` directly processing `byte[]` arrays. Implement splitting on commas and respecting `""` quoting at the byte level.
- [ ] 4.2 Implement inline primitive numeric parsing directly from ASCII bytes (e.g., `value = value * 10 + (b - '0')`) without `String` allocation.
- [ ] 4.3 Wrap string fields (names, URLs) dynamically into `XltCharBuffer` lazily only when requested via `Data` API getters.
- [ ] 4.4 Port `CsvLineDecoderTest` cases into `ByteCsvDecoderTest` verifying identical behavior given byte inputs.
- [ ] 4.5 Update `DataReaderThread` to stream raw `byte[]` line blocks via `FileInputStream` instead of `InputStreamReader` and `char[]`.
- [ ] 4.6 Feed `byte[]` line slices into the decoder and map outputs identically against `Data` class setters.
- [ ] 4.7 Execute final parity assertions.
- [ ] 4.8 Benchmark overall parse throughput establishing the final pipeline JVM gains.
