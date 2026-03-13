## Tasks

### 1. Implement `ReportTestHarness` builder class

**File:** `src/test/java/com/xceptance/xlt/report/ReportTestHarness.java`

- [ ] Internal auto-managed temp directory (`Files.createTempDirectory`), cleaned up after use
- [ ] Static factory methods:
  - `ReportTestHarness.forReport(File inputDir)` / `forReport(String path)`
  - `ReportTestHarness.forDiffReport(File inputDir1, File inputDir2)`
  - `ReportTestHarness.forTrendReport(File... inputDirs)` / `forTrendReport(List<File>)`
  - `ReportTestHarness.forScorecard(File reportDir)`
- [ ] Fluent builder methods:
  - `.withProperty(String key, String value)` — property override
  - `.withNoCharts()` — disables chart generation
  - `.withOverlay(String relativePath, File replacementFile)` — replace file in working copy
  - `.withOverlayContent(String relativePath, String content)` — inline string overlay
  - `.withFilteredLines(String relativePath, Predicate<String>)` — filter lines
  - `.withReplacedText(String relativePath, String search, String replacement)` — literal replace
  - `.withTransform(String relativePath, UnaryOperator<String>)` — arbitrary transform
  - `.withClearedAgentData()` — remove all agent CSV/log files from working copy
  - `.withDataFile(String relativePath, List<? extends Data>)` — write synthetic data via `Data.toList()` + CsvUtils
- [ ] Internal: copy `config/` directory to temp before each run
- [ ] Internal: copy input directory to working copy, apply overlays and transformations in order
- [ ] Generator invocations:
  - `generateReport()` → `ReportGenerator` full pipeline → `ReportResult`
  - `generateDiffReport()` → `DiffReportGenerator` full pipeline → `ReportResult`
  - `generateTrendReport()` → `TrendReportGenerator` full pipeline → `ReportResult`
  - `generateScorecard()` → scorecard `UpdateMain`/`UpdateScorecardRunner` full pipeline → `ReportResult`

### 2. Implement `ReportResult` query class

**File:** `src/test/java/com/xceptance/xlt/report/ReportResult.java`

- [ ] `outputDir()` — root output directory
- [ ] `xmlFile()` — primary XML report file
- [ ] `htmlFile(String name)` — named HTML `File`
- [ ] `allHtmlFiles()` — all `.html` files in output
- [ ] `assertHtmlFileExists(String name)` — `AssertionError` with clear message if missing
- [ ] `assertXmlNode(String xpath)` — XPath match assert on primary XML
- [ ] `queryXml(String xpath)` — `List<String>` of XPath text/attr matches
- [ ] `htmlContains(String fileName, String text)` — `boolean`
- [ ] `assertHtmlContains(String fileName, String text)` — assert form
- [ ] `assertChartExists(String name)` — assert `.png` file exists in output tree
- [ ] `allChartFiles()` — all `.png` files under output

### 3. Write integration tests

**File:** `src/test/java/com/xceptance/xlt/report/ReportGenerationIntegrationTest.java`

- [ ] Full load report from real sample data — assert standard HTML files, `testreport.xml`, charts, XPath data point
- [ ] Load report with property override — assert output changes (e.g., `linkToResultBrowsers`)
- [ ] Load report with `.withNoCharts()` — assert no `.png` files, all HTML present
- [ ] Load report with overlay — replace a properties file, assert report reflects the override
- [ ] Load report with `.withFilteredLines()` — assert report still produces output
- [ ] Load report with `.withReplacedText()` on config — assert reflected in output
- [ ] Fully synthetic report: `.withClearedAgentData()` + `.withDataFile(...)` with known `RequestData` records — assert `testreport.xml` contains expected transaction names
- [ ] Diff report from two sample directories — assert `diffreport.xml` and `index.html` present
- [ ] Trend report from 3+ sample directories — assert `trendreport.xml` and `index.html` present
- [ ] Scorecard on an existing report directory — assert `scorecard.xml` and `scorecard.html` present

### 4. Write demo tests (XSLT and FreeMarker)

**Files:**
- `src/test/java/com/xceptance/xlt/report/HarnessDemo_XsltReportTest.java`
- `src/test/java/com/xceptance/xlt/report/HarnessDemo_FreeMarkerReportTest.java`

These are explicitly labelled demo tests — minimal, well-commented, copy-paste starting points.

#### XSLT demo test
- [ ] Generate a full report from real sample data with `renderingEngine=xslt`
- [ ] Assert `testreport.xml` is non-empty
- [ ] Assert `transactions.html` is present
- [ ] Assert at least one chart file is present
- [ ] Assert an XPath data point (e.g., at least one `<transaction>` node in the XML)

#### FreeMarker demo test
- [ ] Generate a full report from the same sample data with `renderingEngine=freemarker`
- [ ] Assert the same structural properties as the XSLT test above
- [ ] Assert `transactions.html` from both renders contains the same transaction name (basic parity check)

### 5. Verification

- [ ] `mvn test -Dtest=HarnessDemo_XsltReportTest,HarnessDemo_FreeMarkerReportTest` — demo tests pass
- [ ] `mvn test -Dtest=ReportGenerationIntegrationTest` — all integration tests pass
- [ ] `mvn test` — no regressions in `ReportGeneratorRegressionTest`, `FreeMarkerParityTest`, or any other existing test
