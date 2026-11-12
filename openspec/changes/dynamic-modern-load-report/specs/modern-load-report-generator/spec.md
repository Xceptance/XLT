# Modern Load Report Generator

## Purpose

Generates dynamic JavaScript data bundles (`js/report-meta.js` and per-page `js/*-data.js`) and copies static modern HTML/CSS/JS templates (`config/testreport-modern/`) from XLT load report artifacts (`testreport.xml`, `scorecard.xml`, and `charts/*/*.json`), supporting both standard report creation (`create_report`) with a configurable `modern` / `classic` UI switch and standalone fast regeneration (`update_report_ui`).

## ADDED Requirements

### Requirement: Report UI Mode Configuration Switch
The report generator SHALL support a `com.xceptance.xlt.reportgenerator.ui` property in `config/reportgenerator.properties` accepting `modern` or `classic`, defaulting to `modern`, while keeping `bin/create_report.sh` (`.cmd`) and the existing `classic` XSLT rendering pipeline unchanged.

#### Scenario: Default modern UI mode
- **WHEN** `create_report` runs without overriding `com.xceptance.xlt.reportgenerator.ui`
- **THEN** the report generator SHALL copy static templates and assets from `config/testreport-modern/` into the report output directory and generate `js/report-meta.js` and the per-page `js/*-data.js` bundles

#### Scenario: Classic XSLT UI mode preserved unchanged
- **WHEN** `create_report` runs with `com.xceptance.xlt.reportgenerator.ui=classic` (in `reportgenerator.properties` or via `-Dcom.xceptance.xlt.reportgenerator.ui=classic`)
- **THEN** the report generator SHALL copy static resources from `config/testreport/` and execute the existing XSLT 1.0 HTML transformations from `config/xsl/loadreport/` without modification

#### Scenario: AI summary markdown preserved in modern mode
- **WHEN** `create_report` runs in `modern` UI mode
- **THEN** `ai-data.md` SHALL still be generated in the report output directory

---

### Requirement: Shared Report Metadata Bundle Generation
In `modern` mode, the system SHALL generate `js/report-meta.js` defining `window.XLT_META` from `<general>`, `<summary>`, `<testReportConfig>`, section item counts in `testreport.xml`, and `scorecard.xml` (when present).

#### Scenario: Metadata bundle populated from testreport.xml and scorecard.xml
- **WHEN** `js/report-meta.js` is generated for a report directory containing `testreport.xml` and `scorecard.xml`
- **THEN** `window.XLT_META` SHALL include product information, project name, load test name/comment, start time, end time, duration in milliseconds and formatted `HH:mm:ss`, section counts (`transactions`, `actions`, `requests`, `slowestRequests`, `customTimers`, `customValues`, `pageLoadTimings`, `webVitals`, `agents`, `errors`, `events`, `customLogs`, `externalCharts`), scorecard summary (`present`, `result`, `points`, `maxPoints`, `pointsPercentage`), and `testReportConfig` (`runtimePercentiles`, `runtimeIntervals`, `requestTableColorization`)

#### Scenario: Metadata bundle without scorecard
- **WHEN** `js/report-meta.js` is generated for a report directory without `scorecard.xml`
- **THEN** `window.XLT_META.scorecard.present` SHALL be `false` and `scorecard.html` SHALL NOT be copied into the report output directory

---

### Requirement: Per-Page Data Bundle Serialization
In `modern` mode, the system SHALL generate separate per-page JavaScript data bundles under `<outputDir>/js/` assigning their respective `window.XLT_*` globals so pages load synchronously over `file:///` without `fetch()` CORS errors and without loading unrelated page payloads.

#### Scenario: Timer data bundles include dynamic percentiles, segmentation, labels, and Apdex
- **WHEN** `transactions-data.js`, `actions-data.js`, `requests-data.js`, `custom-timers-data.js`, or `page-load-timings-data.js` is generated from `testreport.xml`
- **THEN** each item SHALL include its summary statistics (`count`, `rate`, `cpm`, `cph`, `cpd`, `errors`, `errPct`, `events`, `mean`, `min`, `max`, `dev`, `median`), all `<percentiles>` entries configured in `<testReportConfig>`, whitespace-split `<labels>` array (`labels: [...]`), `<apdex>` data on actions, and `<colorizationGroupName>`, `<countPerInterval>`, `<percentagePerInterval>`, network timing breakdown, and byte statistics on requests

#### Scenario: Duration-agnostic 20-bucket sparkline computation from chart JSON
- **WHEN** `<outputDir>/charts/<section>/<chartBase>.json` exists for a timer item
- **THEN** the generator SHALL downsample the time-series runtime mean values across the full test window into a 20-bucket `spark` array on that item

#### Scenario: Chart JSON absent or charts disabled
- **WHEN** `create_report` runs with `-noCharts` or a chart `.json` file does not exist for an item
- **THEN** the item's `spark` array SHALL be empty (`[]`) without failing report generation

---

### Requirement: Standalone UI Regeneration CLI
The system SHALL provide `bin/update_report_ui.sh` and `bin/update_report_ui.cmd` (invoking `com.xceptance.xlt.report.UpdateReportUiMain`) to regenerate the modern HTML/CSS/JS templates and `js/*-data.js` bundles directly from an existing report directory containing `testreport.xml`.

#### Scenario: Standalone regeneration on an existing report directory
- **WHEN** `bin/update_report_ui.sh <reportDir>` is executed on a directory containing `testreport.xml`
- **THEN** the tool SHALL copy the latest templates and assets from `config/testreport-modern/` into `<reportDir>` and regenerate `js/report-meta.js` and all `js/*-data.js` bundles from `<reportDir>/testreport.xml`, optional `<reportDir>/scorecard.xml`, and `<reportDir>/charts/*/*.json` without reading raw timer logs

#### Scenario: Missing testreport.xml error handling
- **WHEN** `bin/update_report_ui.sh <reportDir>` is executed on a directory that does not contain `testreport.xml`
- **THEN** the tool SHALL exit with a non-zero status code and log a clear error message

---

### Requirement: Scorecard Re-evaluation Updates Modern Bundles
When `bin/update_scorecard.sh` (`ReportGenerator.updateScorecard()`) runs on a modern report directory, it SHALL update the modern scorecard data and metadata bundles.

#### Scenario: Updating scorecard in modern mode
- **WHEN** `bin/update_scorecard.sh` is executed on a report directory in `modern` mode
- **THEN** the system SHALL evaluate `testreport.xml` into `scorecard.xml`, write `js/scorecard-data.js`, update `window.XLT_META.scorecard` in `js/report-meta.js`, and ensure `scorecard.html` is present in the report directory
