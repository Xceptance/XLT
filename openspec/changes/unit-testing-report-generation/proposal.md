## Why

The four report generation entry points (`ReportGeneratorMain`, `DiffReportGeneratorMain`, `TrendReportGeneratorMain`, `UpdateMain`/scorecard) are currently only exercisable via shell scripts, which tightly couple the test harness to process invocation, filesystem paths, and JVM flags. There is no way to set up fixture input data, inject property overrides, trigger a full report run, and assert against the resulting HTML/XML files from within a JUnit test. This gap means regressions in report output can only be caught manually or through fragile script-based pipelines.

## What Changes

- Introduce programmatic Java test harness classes (one per report type) that mirror what the shell scripts do — setting up `XltExecutionContext`, resolving input/output directories, applying property overrides, and invoking the generator — but from within a normal JUnit test.
- Add a `ReportTestFixture` builder/helper that handles: pointing at a test data directory, creating a temporary output directory, accepting a `Properties` map for overrides, and invoking the correct generator.
- The existing `*Main` classes expose public `init(String[] args)` and `run()` methods — tests can call these directly without shelling out.
- Add a set of curated test data directories under `src/test/resources/report-fixtures/` (one per report type), each containing the minimum input files needed to produce a valid report.
- Write integration tests in `src/test/java/com/xceptance/xlt/report/` that use the harness to run each generator and assert key structural properties of the output (presence of expected HTML files, XML structure, specific elements).
- **Include at least two concrete demo tests** using the existing real sample data, exercising both the XSLT and FreeMarker rendering engines side-by-side, to serve as a working proof-of-concept and usage reference for the new harness.

## Capabilities

### New Capabilities
- `report-test-harness`: Programmatic Java API to invoke all four report generators (report, diff-report, trend-report, scorecard) from within JUnit tests, with configurable input directories, output directories, and property overrides.
- `report-test-fixtures`: Curated minimal input data sets under `src/test/resources/report-fixtures/` covering each report type, enabling repeatable and self-contained integration tests.
- `report-output-assertions`: Integration test suite that asserts on generated HTML/XML output files (file presence, basic structure, key content elements).
- `render-engine-demo-tests`: Concrete demonstration tests in `HarnessDemo_XsltReportTest.java` and `HarnessDemo_FreeMarkerReportTest.java` that generate full reports using each rendering engine from the existing real sample data and assert key output properties. These serve as living proof-of-concept and copy-paste starting points for future tests.

### Modified Capabilities
- None — no existing spec-level behavior changes.

## Impact

- **Test code**: New classes under `src/test/java/com/xceptance/xlt/report/` (harness + integration tests).
- **Test resources**: New directories under `src/test/resources/report-fixtures/`.
- **Production code**: `ReportGeneratorMain`, `DiffReportGeneratorMain`, `TrendReportGeneratorMain`, `UpdateMain` — minor visibility adjustments if needed (e.g., package-private → protected), but currently their `init()`/`run()` are already `public`.
- **Build**: Tests run as standard Maven Surefire/Failsafe tests; no new dependencies expected (JUnit 5 + existing classpath already covers the need).
- **No breaking changes**.
