## Context

Report generation in XLT is driven by four shell scripts, each launching a different Java `*Main` class:

| Script | Main Class | Output |
|--------|-----------|--------|
| `create_report.sh` | `ReportGeneratorMain` → `ReportGenerator` | `testreport.xml` + full HTML report |
| `create_diff_report.sh` | `DiffReportGeneratorMain` → `DiffReportGenerator` | `diffreport.xml` + HTML |
| `create_trend_report.sh` | `TrendReportGeneratorMain` → `TrendReportGenerator` | `trendreport.xml` + HTML |
| `update_scorecard.sh` | `UpdateMain` → scorecard logic | `scorecard.xml` + HTML |

An existing `ReportGeneratorRegressionTest` shows the approach: directly construct `ReportGenerator`, wire up directories, and pass a `Properties` map. However, this test is messy: it manually copies config trees, has DEBUG println noise, and directly exposes `ReportGenerator` constructor arguments. Each new test needs to repeat all this boilerplate.

We also have real result data in `src/test/resources/results/` (5 complete agent result directories), which can drive all four generator types.

## Goals / Non-Goals

**Goals:**
- Provide a single fluent builder class (`ReportTestHarness`) that a test author can call without knowing the internal constructor signatures of `ReportGenerator` et al.
- Support all four report types behind a uniform interface.
- The **complete** production pipeline MUST run end-to-end: data reading, statistics computation, chart generation, and HTML/XML rendering. No internal steps may be bypassed or mocked. The only convenience provided is eliminating shell-script and JVM-flag boilerplate.
- Charts run by default; can be disabled via a simple builder method (which sets the appropriate property internally — the same switch the shell script uses).
- Property overrides are set via a fluent API on the builder, not via CLI arg parsing.
- Support virtual overlay of input data: replace or transform individual input files without modifying the originals.
- Support convenient manipulation of input file content (filtering lines, replacing text, applying a transform function) to create test variations easily.
- `ReportResult` provides rich query and assertion methods over the generated output (XML XPath, HTML content, chart file existence).

**Non-Goals:**
- Exposing or testing the CLI argument parsing logic (`init(String[] args)`) — that is covered by existing `ReportGeneratorMain_TimeParsingTest`.
- Cross-process invocation (spawning the shell scripts in subprocess during JUnit).
- Providing a mock or stub for `ReportGenerator` internals — all real production code runs.

## Decisions

### Decision 1: Builder pattern over static factory

**Chosen**: `ReportTestHarness` is a fluent builder. Tests construct an instance, configure it (input dir, properties, boolean flags), then call `.generateReport()`, `.generateDiffReport()`, `.generateTrendReport()`, or `.generateScorecard()`.

**Alternative considered**: Static helper methods like `TestReportRunner.runReport(inputDir, outputDir, props)`. Rejected because it becomes unwieldy once you need to add optional flags (no-charts, time boundaries, etc.).

**Rationale**: Builder lets options accumulate naturally and keeps test code readable:
```java
new ReportTestHarness(tempDir)
    .withInput("src/test/resources/results/xlt-result-xc-advanced-posters-20260216-152202")
    .withProperty("com.xceptance.xlt.reportgenerator.linkToResultBrowsers", "false")
    .withNoCharts()        // optional, only when explicitly needed
    .generateReport();
```

### Decision 2: Shared config directory sourced from project `config/`

**Chosen**: The harness copies the project's `config/` directory into each test's temp output area at construction time (or lazily on first generate call). This mirrors exactly what the shell scripts do (`XLT_HOME/config`).

**Alternative considered**: Point the harness directly at the project `config/` directory without copying. Rejected because some tests may need to override properties files, and mutations must not affect parallel tests.

**Rationale**: Isolation — each test gets its own config copy.

### Decision 3: `ReportResult` return object

**Chosen**: Each `generate*()` method returns a `ReportResult` record-like object that gives access to:
- `outputDir()` — the directory that was written
- `xmlFile()` — the primary XML file (testreport.xml, diffreport.xml, etc.)
- `htmlFile(String name)` — convenience method to get a named HTML file
- `allHtmlFiles()` — all generated `.html` files
- `assertHtmlFileExists(String name)` — assertion shortcut

**Rationale**: Avoids tests having to know the exact output file name conventions; keeps assertions concise.

### Decision 4: Charts on by default

The harness does **not** set `noCharts = true` unless the caller explicitly calls `.withNoCharts()`. This matches the production behavior and ensures chart generation code is tested.

### Decision 5: Harness manages its own temporary directory

**Chosen**: The harness creates its own temp directory internally using `Files.createTempDirectory` and registers a JVM shutdown hook (or JUnit extension) to clean it up. Test authors do NOT pass a temp directory — there is no constructor argument for it.

**Alternative considered**: Requiring tests to pass a `@TempDir` / `TemporaryFolder`. Rejected because it leaks temp-dir lifecycle management into every test class, forcing boilerplate that serves the harness's concerns, not the test's.

**Rationale**: Tests should read as domain assertions, not infrastructure setup. Hiding the temp directory keeps each test to its essentials: input, configuration, assertions.

### Decision 6: Virtual file overlay — copy-on-write over source data

**Chosen**: The harness exposes `.withOverlay(String relativePath, File replacementFile)` and `.withOverlayContent(String relativePath, String content)`. When a generate method is called, it copies the input directory to a temp subdirectory, then applies all registered overlays (replacing those specific files). The generator runs against this overlay copy.

**Alternative considered**: A custom `VirtualFileSystem` wrapping Apache Commons VFS. Rejected as overly complex — the generators already read from disk, and a file copy (even for multi-MB agent data) is fast enough for integration tests.

**Alternative considered**: Passing `File` objects per-overlay to each generator call. Rejected because it scales poorly when tests need several overrides.

**Rationale**: The copy-on-write model is simple, transparent, and compatible with all four generators without any changes to production code.

### Decision 7: Rich output querying via `ReportResult`

**Chosen**: `ReportResult` exposes a layered query API:

| Method | What it does |
|--------|-------------|
| `assertHtmlFileExists(name)` | Asserts an HTML file is present |
| `assertXmlNode(xpath)` | Asserts an XPath expression matches at least one node in the primary XML |
| `queryXml(xpath)` | Returns a `List<String>` of text values matching the XPath in the primary XML |
| `htmlContains(fileName, text)` | Returns true if the named HTML file contains the given string |
| `assertHtmlContains(fileName, text)` | Assertion form of the above |
| `assertChartExists(name)` | Asserts a chart image file (`.png`) with the given name exists in the output |
| `allChartFiles()` | Returns all chart image files in the output |

**Alternative considered**: Returning raw `File` objects and letting tests do their own I/O. Rejected because it repeats boilerplate in every test and produces less informative failure messages.

**Rationale**: Tests should read as specifications, not file-handling code. A thin query API keeps test intent clear.

### Decision 8: Input file content manipulation — transform-on-copy API

**Chosen**: The harness exposes three manipulation primitives, all operating on the working copy (never the original):

- `.withFilteredLines(relativePath, Predicate<String>)` — retains only lines matching the predicate
- `.withReplacedText(relativePath, searchString, replacement)` — replaces all occurrences of a literal string
- `.withTransform(relativePath, UnaryOperator<String>)` — applies an arbitrary function to the full file content

Manipulations are applied after the directory is copied but before the generator runs. Multiple manipulations on the same file are applied in registration order.

**Alternative considered**: A domain-specific mini-DSL (e.g., XML path editing, CSV column manipulation). Rejected because the three primitives above cover the vast majority of cases without introducing specialised parsing dependencies, and the transform function handles the rest.

**Rationale**: Composing these three operations covers: removing header lines, adjusting property values, truncating data files, injecting synthetic data rows — all common in integration testing without touching production code.

### Decision 9: Synthetic data creation from `Data.toList()` + clearing agent data

**Chosen**: The harness provides:
- `.withClearedAgentData()` — deletes all CSV/log files under `ac*/` in the working copy, enabling a clean slate for purely synthetic tests
- `.withDataFile(String relativePath, List<? extends Data> records)` — creates a file at the given path by calling `record.toList()` on each item and encoding it as a CSV row using the same `CsvUtils` that XLT itself uses for writing

`Data.toList()` already exists on every XLT data class (`RequestData`, `TransactionData`, `ActionData`, `CustomData`, `EventData`, `WebVitalData`, etc.) and is the canonical serialization method. The harness just drives the CSV writer with it.

**Alternative considered**: A custom `DataFileBuilder` DSL. Rejected — `Data.toList()` is already the correct serialization method, so wrapping it is all that is needed.

**Rationale**: A test can now express: "a dataset containing exactly 100 `RequestData` records for name `TOrder` with runtime 500ms" and assert that the generated report reflects exactly that. Zero intermediate formatting code in the test.

## Risks / Trade-offs



- **Test speed**: Running a full report (with charts) from real data takes several seconds per test. Mitigation: declare these as integration tests (or annotate them with a JUnit category) so CI can separate fast unit tests from slow integration tests. By default they run unless tagged to skip.
- **Config drift**: If the `config/` directory structure changes, the harness's copy logic may break. Mitigation: the harness copies the whole `config/` tree, so it tracks changes automatically.
- **Four generators, four different constructor signatures**: The harness encapsulates these differences. If a constructor changes, only the harness needs updating, not every test.
- **XltExecutionContext is static/thread-local**: Concurrent tests mutating it could interfere. Mitigation: document that tests must not run in parallel (fine for integration tests tagged accordingly).
