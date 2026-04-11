## Why

The XLT reporting pipeline has three performance bottlenecks that compound under load:

1. **Wasted dispatch calls.** `PostProcessedDataContainer` holds a single `List<Data>`. `AbstractReportProvider.processAll()` iterates this list and calls `processDataRecord(d)` on every record for every provider. Most providers only care about one data type, so the majority of these calls hit a failed `instanceof` check and return immediately. With ~20 providers and a 200-record chunk, that is ~4000 virtual method calls where ~3600 do nothing.

2. **Spin-lock contention.** `ReportProvider` implementations are not thread-safe internally. `StatisticsProcessor` serializes access via a `tryLock()` spin-loop with `Thread.yield()`. Parser threads burn CPU waiting instead of parsing.

3. **Char doubling.** `InputStreamReader` decodes every byte to a UTF-16 `char` before `CsvLineDecoder` splits fields. For numeric-heavy timer data (timestamps, runtimes, byte counts), this doubles memory allocation for no reason.

## What Changes

- **Typed data routing:** Replace the single `List<Data>` with per-type `SimpleArrayList` fields for each of the 9 registered data types. The parser routes each record via an `instanceof` cascade at add-time. Providers pull only the list they need. This eliminates ~3600 no-op `processDataRecord()` calls per chunk. The unused sampling feature (`sampleFactor`, `droppedLines`) is removed.

- **Provider Concurrency via Actor Model:** The `tryLock()` spin-loop is removed. `StatisticsProcessor` wraps each stat-collecting provider in a lightweight Actor (a dedicated Virtual Thread looping over a `BlockingQueue`). Parser threads push chunks into these queues and use a latch mechanism to await completion. This completely eliminates explicit `ReentrantLock` handling while natively maintaining memory backpressure and ensuring no provider is ever accessed concurrently.

- **Byte-level CSV parsing:** A new `ByteCsvDecoder` reads `byte[]` buffers directly from disk. Numeric fields are parsed inline via `value = value * 10 + (b - '0')` without creating `String` or `char[]` objects. String fields are converted to `XltCharBuffer` on demand.

## Capabilities

### New Capabilities
- `typed-data-routing`: Per-type `SimpleArrayList` fields in `PostProcessedDataContainer` with typed getters. Fallback `customData` list for unknown extension types. Sampling logic removed.
- `concurrent-provider-dispatch`: Actor Model concurrency (dedicated VT + BlockingQueue per provider) replacing the spin-loop. `CountDownLatch` or `StructuredTaskScope` preserves backpressure. `lock()`/`unlock()` removed from `ReportProvider` interface. Zero explicit locks (`java.util.concurrent.locks.Lock`) in the provider dispatch path.
- `byte-csv-parser`: Byte-level CSV decoder with inline numeric parsing. `DataReaderThread` emits `byte[]` buffers instead of `char[]`.

### Modified Capabilities
_(none)_

## Impact

- **Affected classes:** `PostProcessedDataContainer`, `StatisticsProcessor`, `DataParserThread`, `ReportProvider`, `AbstractReportProvider`, all concrete `*ReportProvider` classes, `CsvLineDecoder`, `DataReaderThread`.
- **Test infrastructure:** Parity tests against existing known data. Benchmarks on Ariat and BBW datasets.
- **Dependencies:** None added. Pure-Java implementation.
- **Delivery:** Incremental phases, each independently benchmarked and reversible.
