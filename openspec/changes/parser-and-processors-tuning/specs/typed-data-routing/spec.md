## ADDED Requirements

### Requirement: Hardcoded Typed Array Buckets in PostProcessedDataContainer
`PostProcessedDataContainer` SHALL replace the legacy `List<Data> data` field with 9 explicitly declared `SimpleArrayList` fields corresponding to the core data types: `requests`, `transactions`, `actions`, `events`, `customTimers`, `jvmResourceUsage`, `customValues`, `pageLoadTimings`, and `webVitals`. It SHALL also include one fallback bucket: `customData`.

#### Scenario: Contiguous arrays eliminate wasted method calls
- **WHEN** `DataParserThread` processes a line identifying a `RequestData` object
- **THEN** it SHALL evaluate an `instanceof` cascade, identify it as `RequestData`, and add it directly to `this.requests`, preventing non-request providers from ever seeing it.

### Requirement: Fallback unknown type bucket
To support dynamic custom plugins, `PostProcessedDataContainer` SHALL include a generic `SimpleArrayList<Data> customData` bucket.

#### Scenario: Fallback isolation
- **WHEN** `DataParserThread` parses a `Data` subclass that is not one of the explicitly hardcoded core classes
- **THEN** it SHALL place the record in the `customData` list. A specialized provider matching this data SHALL pull from `customData` and use `instanceof` internally.

### Requirement: Explicit Typed Getters
`PostProcessedDataContainer` SHALL expose explicit getter methods for each typed list: `getRequests()`, `getTransactions()`, `getActions()`, `getEvents()`, `getCustomTimers()`, `getJvmResourceUsage()`, `getCustomValues()`, `getPageLoadTimings()`, `getWebVitals()`, and `getCustomData()`. 

#### Scenario: Providers pull specific data
- **WHEN** `RequestsReportProvider` processes a chunk
- **THEN** it SHALL call `container.getRequests()` and iterate only that list.

### Requirement: Complete Removal of Legacy Sampling
The `sampleFactor` and `droppedLines` fields SHALL be removed from `PostProcessedDataContainer`. The sampling compensation loop in `AbstractReportProvider.processAll()` SHALL be deleted.

#### Scenario: Unimpeded iteration
- **WHEN** a concrete `ReportProvider` iterates its data
- **THEN** it SHALL iterate from beginning to end without checking `sampleFactor` or evaluating fallback logic.

### Requirement: SummaryReportProvider explicit gathering
The `SummaryReportProvider` processes 7 distinct data types. It SHALL be updated to explicitly pull and process from all 7 relevant typed lists, instead of running a single `instanceof` loop over an untyped list.

#### Scenario: Multi-type provider
- **WHEN** `SummaryReportProvider` processes a chunk
- **THEN** it SHALL iterate `getRequests()`, `getTransactions()`, `getActions()`, `getEvents()`, `getPageLoadTimings()`, `getCustomTimers()`, and `getJvmResourceUsage()` separately, routing data to its internal sub-processors.

### Requirement: Per-Container Time Statistics
`PostProcessedDataContainer` SHALL continue to evaluate local `minimumTime` and `maximumTime` metrics upon insertion of any data record, inside its `add(Data d)` method.

#### Scenario: Local min/max maintained
- **WHEN** any record is added to any of the 10 typed buckets
- **THEN** the container's local `minimumTime` and `maximumTime` SHALL be updated.
