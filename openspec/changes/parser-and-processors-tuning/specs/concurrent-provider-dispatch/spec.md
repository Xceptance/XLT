## ADDED Requirements

### Requirement: Provider Actor Model (Message Passing)
`StatisticsProcessor` SHALL eliminate all explicit locking (`ReentrantLock`, `synchronized`). Instead, it SHALL use the Actor Model for concurrency. `StatisticsProcessor` SHALL wrap each valid `ReportProvider` in a lightweight Actor consisting of a dedicated Virtual Thread and a `BlockingQueue<PostProcessedDataContainer>` (e.g. `ArrayBlockingQueue` to provide backpressure). 

#### Scenario: Lock-Free Sequential Provider Execution
- **WHEN** a provider's dedicated VT takes a chunk from its queue
- **THEN** it SHALL call `processAll()` sequentially. Because exactly ONE Virtual Thread interacts with a given provider, the provider is inherently protected from concurrent access without using locks.

#### Scenario: Chunks are dispatched via queues
- **WHEN** `StatisticsProcessor.process(chunk)` is invoked by a parser thread
- **THEN** it SHALL iterate over its list of Provider Actors and call `put(chunk)` on their respective queues. It SHALL NOT spawn new threads or use an ExecutorService dynamically.

### Requirement: StructuredTaskScope or CountDownLatch for Backpressure
To prevent `Dispatcher` from thinking work is complete before the actors finish, `StatisticsProcessor` SHALL synchronize chunk completion. When enqueuing a chunk to the actors, it SHALL attach a continuation or use a `CountDownLatch` (or `Phaser` / `StructuredTaskScope`) so the parser thread blocks inside `process()` until all actors have verified they processed that specific chunk.

#### Scenario: Memory backpressure maintained
- **WHEN** a parser thread dispatches a chunk to 18 providers
- **THEN** it blocks awaiting a signal that all 18 providers have completed the chunk, halting parsing safely if a provider falls behind.

### Requirement: Complete deletion of `wantsDataRecords()`
The `wantsDataRecords()` method SHALL be completely removed from the `ReportProvider` interface and all implementing classes. 

#### Scenario: Metadata providers have empty loops
- **WHEN** `ConfigurationReportProvider` (which formerly returned `false`) or `CustomLogsReportProvider` processes a chunk via its Actor
- **THEN** it SHALL execute an empty `processAll()` block. The complexity reduction of removing `wantsDataRecords()` from the codebase outweighs the negligible overhead of an empty VT actor loop firing every 200 records.

### Requirement: Providers pull from typed lists
Each `ReportProvider` SHALL pull only its relevant arrays from the `PostProcessedDataContainer` using explicit getters (e.g., `container.getRequests()`). The container SHALL be treated as read-only.

#### Scenario: Direct typed pulls
- **WHEN** `RequestsReportProvider.processAll()` executes
- **THEN** it SHALL call `container.getRequests()` and iterate only that `SimpleArrayList<RequestData>`.

### Requirement: Providers remain non-thread-safe internally
Because each provider is protected by its unique `ReentrantLock` in `StatisticsProcessor`, `processAll()` is guaranteed single-threaded access. `ReportProvider` implementations SHALL NOT require internal synchronization. 

### Requirement: lock() and unlock() removed from ReportProvider API
The `ReportProvider` interface and `AbstractReportProvider` base class SHALL NOT define or implement `lock()` and `unlock()` methods. Locking is entirely managed by `StatisticsProcessor`.

### Requirement: Global Min/max time aggregation remains thread-safe
`StatisticsProcessor` SHALL maintain thread-safe `minimumTime` and `maximumTime` trackers across all processed containers. The global aggregation SHALL use its existing dedicated `ReentrantLock`.

#### Scenario: Concurrent time updates
- **WHEN** two VTs finish chunk processing concurrently
- **THEN** the global minimum and maximum times SHALL safely aggregate the per-container local min/max values.
