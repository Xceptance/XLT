## Context

The XLT report generator reads per-agent CSV timer files, parses lines into `Data` objects, groups them into `PostProcessedDataContainer` chunks (default 200 lines), and delivers each chunk to `StatisticsProcessor`. The processor dispatches the chunk to ~20 `ReportProvider` instances that accumulate statistics and produce XML fragments for the final report.

The data types are configured in `reportgenerator.properties` and mapped by single-character type codes:

| Code | Class | Provider(s) |
|------|-------|-------------|
| T | `TransactionData` | `TransactionsReportProvider`, `SummaryReportProvider`, `GeneralReportProvider`, `ErrorsReportProvider` |
| A | `ActionData` | `ActionsReportProvider`, `SummaryReportProvider` |
| R | `RequestData` | `RequestsReportProvider`, `SummaryReportProvider`, `ResponseCodesReportProvider`, `RequestMethodsReportProvider`, `IpReportProvider`, `HostsReportProvider`, `ContentTypesReportProvider`, `SlowestRequestsReportProvider` |
| C | `CustomData` | `CustomTimersReportProvider`, `SummaryReportProvider` |
| E | `EventData` | `EventsReportProvider`, `SummaryReportProvider` |
| J | `JvmResourceUsageData` | `AgentsReportProvider`, `SummaryReportProvider` |
| V | `CustomValue` | `CustomValuesReportProvider` |
| P | `PageLoadTimingData` | `PageLoadTimingsReportProvider`, `SummaryReportProvider` |
| W | `WebVitalData` | `WebVitalsReportProvider` |

Two providers (`ConfigurationReportProvider`, `CustomLogsReportProvider`) return `wantsDataRecords() == false` and never receive data chunks.

**Current bottlenecks:**

1. **Wasted dispatch.** `AbstractReportProvider.processAll()` iterates the entire untyped `List<Data>` and calls the virtual method `processDataRecord(d)` for every record. Each provider checks `instanceof` inside `processDataRecord()` and returns immediately if the type doesn't match. With 200 records and ~18 active providers, that's ~3600 calls per chunk, most of which are wasted virtual dispatch overhead.

2. **Spin-lock contention.** Provider implementations mutate internal state without synchronization. `StatisticsProcessor.process()` uses `tryLock()` in a tight loop over the provider list, calling `Thread.yield()` between iterations. If all providers are busy, the thread spins doing zero work.

3. **Char doubling.** `InputStreamReader` decodes every byte to a UTF-16 `char`. `CsvLineDecoder` then walks those chars to split fields. For numeric fields (>80% of all fields), this creates garbage for no benefit.

## Goals / Non-Goals

**Goals:**
- Eliminate wasted virtual method dispatch by routing data into per-type lists at parse time
- Replace the spin-loop with the Actor Model (VT + Message Passing) for zero-waste, explicitly lock-free provider concurrency
- Eliminate char allocation overhead for numeric CSV fields via byte-level parsing
- Remove the unused `sampleFactor` feature and simplify `AbstractReportProvider`
- Maintain byte-identical report output at every phase (verified via parity tests)
- Deliver incrementally — each phase benchmarked and reversible independently

**Non-Goals:**
- GC tuning — profiling shows GC is not the bottleneck
- Thread-count tuning — VTs handle scheduling; we don't manually size pools
- Changing report output format (XML/HTML) or the `Data` class hierarchy
- Merge-rule regex optimization — deferred to a separate change
- Fine-grained parallelism within a single provider (spawning child VTs to process 200 items in parallel wastes more CPU on scheduling than it saves on arithmetic)

## Decisions

### Decision 1: Per-type `SimpleArrayList` fields in `PostProcessedDataContainer` (Phase 1)

**Choice:** Add a `SimpleArrayList` field for each of the 9 registered data types (`requests`, `transactions`, `actions`, `events`, `customTimers`, `customValues`, `pageLoadTimings`, `jvmResourceUsage`, `webVitals`). Add a `SimpleArrayList<Data> customData` field as a fallback for any unknown extension types. `PostProcessedDataContainer.add(Data d)` uses an `instanceof` cascade to route each record. The old `List<Data> data` field is removed. The `sampleFactor` and `droppedLines` fields are removed.

Providers get typed getters: `getRequests()` returns `SimpleArrayList<RequestData>`, etc. The `customData` getter returns the untyped fallback list.

**Why not a `Map<Class<?>, List<Data>>`?** A HashMap lookup per record (hash computation, bucket traversal, pointer chasing) is more expensive than a predictable `instanceof` cascade for 9 known types. The `instanceof` cascade compiles to simple vtable pointer comparisons that the branch predictor handles well against a fixed set of types.

**Rationale:** The primary win is eliminating ~3600 wasted `processDataRecord()` virtual calls per chunk. Each provider iterates only its type's list. `SummaryReportProvider` pulls from multiple typed lists explicitly instead of running a single `instanceof` cascade over the entire untyped list.

### Decision 2: Provider Actor Model (Phase 2)

**Choice:** `StatisticsProcessor` wraps each valid provider in a lightweight Actor. Each Actor is a single dedicated Virtual Thread looping over an `ArrayBlockingQueue`. `StatisticsProcessor.process(chunk)` pushes the chunk into every Actor's queue, and uses a coordination lock (like a `CountDownLatch` or `StructuredTaskScope`) to await completion of that specific chunk. 

The `wantsDataRecords()`, `lock()`, and `unlock()` methods are entirely removed from the `ReportProvider` interface and all implementations. The codebase is entirely stripped of explicit locking mechanisms protecting provider state. Providers like `ConfigurationReportProvider` that previously returned `false` for `wantsDataRecords()` will now just have an empty `processAll()` block.

**Why this works:** The Actor model natively converts concurrency into sequential execution. Since only exactly one Virtual Thread reads from the Actor's queue and calls `processAll()`, the provider is inherently thread-safe without deploying `ReentrantLock` or `synchronized`. Backpressure is retained since the parent parse thread blocks in `process()` until all Actor queues have digested and finished the chunk.

**Chunk size consideration:** With the default chunk size of 200 records, task routing must be extremely fast. A single VT looping a queue is the lowest possible overhead pattern in standard Java concurrency. If profiling shows queue contention dominates, increasing the chunk size is the correct response.

### Decision 3: Byte-level CSV parsing (Phase 3)

**Choice:** A new `ByteCsvDecoder` reads `byte[]` buffers directly from a `FileInputStream`. Numeric fields are parsed inline: `value = value * 10 + (b - '0')`. String fields (names, URLs) are converted to `XltCharBuffer` lazily when accessed. `DataReaderThread` emits `byte[]` line buffers instead of `XltCharBuffer` lines.

**Rationale:** Eliminates all `char[]` and `String` allocation for numeric fields. The existing `setBaseValues()` / `setRemainingValues()` API on `Data` subclasses continues to work — the byte decoder produces the same `XltCharBuffer` values for string fields.

## Risks / Trade-offs

- **Parity test flakiness.** Changing iteration order or data grouping can cause floating-point deviations in aggregated statistics. Mitigation: Parity tests compare byte-identical XML output. Run before and after each phase.

- **VT overhead at small chunk sizes.** With 200-record chunks and ~18 providers, each `process()` call allocates ~18 VTs, Futures, and Continuations. If the per-provider work is microseconds, VT scheduling overhead may dominate. Mitigation: Benchmark on Ariat and BBW datasets. Increase chunk size if needed.

- **Unknown extension types.** Custom `Data` subclasses configured by users land in the `customData` fallback list. A provider for such types must iterate that list with `instanceof`. This is acceptable: extension types are rare and the fallback list will be small.
