## ADDED Requirements

### Requirement: Fluent builder for report generation in tests

The system SHALL provide a `ReportTestHarness` class that allows test code to invoke any of the four report generators via a fluent Java API, without being exposed to constructor arguments, CLI parsing, config wiring, or temporary directory management.

The harness MUST invoke the **complete** report generation pipeline — data reading, statistics computation, chart generation, and HTML/XML rendering — with no internal steps bypassed or mocked.

The harness MUST manage its own temporary directory internally (using `Files.createTempDirectory`). The test author MUST NOT need to create or pass a temp directory.

#### Scenario: Basic report generation — no temp dir needed

- **WHEN** a test calls `ReportTestHarness.forReport(inputDir).generateReport()`
- **THEN** the full pipeline runs, a `ReportResult` is returned, and all temp storage is managed by the harness

#### Scenario: Report with property override

- **WHEN** a test calls `.withProperty(key, value)` before `generateReport()`
- **THEN** the specified property overrides the default config, with the full pipeline still executing

#### Scenario: Diff report generation runs full pipeline

- **WHEN** a test calls `ReportTestHarness.forDiffReport(inputDir1, inputDir2).generateDiffReport()`
- **THEN** the complete `DiffReportGenerator` pipeline runs and a `ReportResult` is returned

#### Scenario: Trend report generation runs full pipeline

- **WHEN** a test calls `ReportTestHarness.forTrendReport(inputDir1, inputDir2, ...).generateTrendReport()`
- **THEN** the complete `TrendReportGenerator` pipeline runs and a `ReportResult` is returned

#### Scenario: Scorecard generation runs full pipeline

- **WHEN** a test calls `ReportTestHarness.forScorecard(reportDir).generateScorecard()`
- **THEN** the complete scorecard pipeline runs and a `ReportResult` is returned

### Requirement: Charts enabled by default, disableable per-test

The harness SHALL run with chart generation enabled by default. A test MAY call `.withNoCharts()` to disable chart generation. All other pipeline steps still run.

#### Scenario: Default chart generation

- **WHEN** a test calls `generateReport()` without `.withNoCharts()`
- **THEN** chart `.png` files are present in the output directory

#### Scenario: Explicit no-charts mode

- **WHEN** a test calls `.withNoCharts()` before `generateReport()`
- **THEN** no chart image files are generated; all other pipeline steps still execute

### Requirement: Config isolation per test

The harness SHALL copy the project's `config/` directory into an internal temp subdirectory so that property mutations inside one test do not affect other tests.

#### Scenario: Isolated config copy

- **WHEN** two tests use separate `ReportTestHarness` instances
- **THEN** property changes in one test do not affect the other

### Requirement: Typed property manipulation

The harness SHALL provide typed property-setting methods in addition to raw `withProperty(key, value)`.

#### Scenario: Typed property update

- **WHEN** a test calls `.withProperty("com.xceptance.xlt.reportgenerator.linkToResultBrowsers", "true")`
- **THEN** that property is applied to the generator config before the pipeline runs

### Requirement: Virtual file overlay on input data

The harness SHALL support replacing specific files in (a copy of) the input directory before generation. The original source directory MUST NOT be modified.

#### Scenario: Overlay a file by replacement file

- **WHEN** a test calls `.withOverlay("config/reportgenerator.properties", replacementFile)`
- **THEN** the working copy uses the replacement and the original is unchanged

#### Scenario: Overlay with inline string content

- **WHEN** a test calls `.withOverlayContent("config/scorecard.yaml", yamlString)`
- **THEN** the harness writes that string to a temp file at that path in the working copy

### Requirement: Input file content manipulation

The harness SHALL support transforming the content of specific input files to create test variations, without modifying the originals.

#### Scenario: Filter lines by predicate

- **WHEN** a test calls `.withFilteredLines("ac0001/timers.csv", line -> !line.startsWith("#"))`
- **THEN** the working copy of that file contains only lines that match the predicate

#### Scenario: Replace text in a file

- **WHEN** a test calls `.withReplacedText("config/reportgenerator.properties", "enableCharts=true", "enableCharts=false")`
- **THEN** all occurrences of the search string are replaced in the working copy

#### Scenario: Apply a transform function to a file

- **WHEN** a test calls `.withTransform("config/reportgenerator.properties", content -> content + "\nextra.property=value")`
- **THEN** the transform's return value is written to the working copy

### Requirement: Clearing agent data input files

The harness SHALL support removing all agent data input files from the working input directory so that the test can provide only synthetic data.

#### Scenario: Clear all agent data files

- **WHEN** a test calls `.withClearedAgentData()`
- **THEN** all agent data files (CSV timers, log files) in subdirectories under `ac*/` are removed from the working copy before the generator runs

#### Scenario: Clear agent data then add synthetic data

- **WHEN** a test calls `.withClearedAgentData()` followed by `.withDataFile(path, records)`
- **THEN** the generator sees only the synthetic data provided, running the full pipeline over it

### Requirement: Synthetic data file creation from XLT data objects

The harness SHALL support creating agent data files programmatically from lists of XLT `Data` subclass instances (e.g., `RequestData`, `TransactionData`, `ActionData`). Each data object serializes itself to a CSV row via its existing `toList()` method.

#### Scenario: Create a data file from a list of data records

- **WHEN** a test calls `.withDataFile("ac0001/TOrder/0/timers.csv", List.of(requestData1, requestData2))`
- **THEN** the harness creates that file in the working copy by serializing each record to a CSV row using `Data.toList()` and joining fields with the XLT CSV separator

#### Scenario: Mixed data types in one file

- **WHEN** a test passes a `List<Data>` containing both `TransactionData` and `ActionData` records to `.withDataFile(...)`
- **THEN** all records are written in order, each serialized as its own CSV row

#### Scenario: Synthetic data runs through full pipeline

- **WHEN** a test uses `.withClearedAgentData().withDataFile(...)` to create entirely synthetic input
- **THEN** the complete report generator pipeline runs over that data and produces a valid report with content derived from the synthetic records

### Requirement: ReportResult provides rich output querying

`ReportResult` SHALL provide query and assertion methods covering file existence, XML content, HTML text content, and chart images.

#### Scenario: Assert expected HTML files are present

- **WHEN** a test calls `result.assertHtmlFileExists("transactions.html")`
- **THEN** the assertion passes silently if the file exists, or throws an `AssertionError` with a clear message otherwise

#### Scenario: XPath assertion on primary XML

- **WHEN** a test calls `result.assertXmlNode("//summary/transactions/transaction[@name='TOrder']")`
- **THEN** the assertion passes if at least one node matches, and fails with a descriptive message otherwise

#### Scenario: XPath value extraction

- **WHEN** a test calls `result.queryXml("//summary/transactions/transaction/@name")`
- **THEN** a `List<String>` of all matching text/attribute values is returned

#### Scenario: HTML content assertion

- **WHEN** a test calls `result.assertHtmlContains("transactions.html", "TOrder")`
- **THEN** the assertion passes if the string is found in the file, and fails with message otherwise

#### Scenario: HTML content boolean check

- **WHEN** a test calls `result.htmlContains("transactions.html", "TOrder")`
- **THEN** `true` is returned if present, `false` otherwise

#### Scenario: Chart existence assertion

- **WHEN** a test calls `result.assertChartExists("transactions-TOrder-runtime.png")`
- **THEN** the assertion passes if a `.png` file with that name exists anywhere in the output

#### Scenario: List all chart files

- **WHEN** a test calls `result.allChartFiles()`
- **THEN** a `List<File>` of all `.png` files under the output directory is returned
