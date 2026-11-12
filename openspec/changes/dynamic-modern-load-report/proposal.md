# Proposal

## Why

The modernized XLT single-run load report (`reports/modernized-report-v2/`) currently exists only as a static prototype tied to a single 1-hour sample run (`Load Test #15`), with 6 pages and navigation metadata still hardcoded in HTML and no Java pipeline emitting the `js/*-data.js` bundles from real `testreport.xml` runs. Rather than replacing the existing XSLT 1.0 report rendering (`config/xsl/loadreport/*.xsl` and `config/testreport/`), we approach the new report differently: a parallel Java-to-JS data-bundle generator activated via `config/reportgenerator.properties` (`com.xceptance.xlt.reportgenerator.ui = modern` by default, while `bin/create_report.sh` and the `classic` XSLT rendering stay untouched), plus a dedicated standalone script (`bin/update_report_ui.sh` / `.cmd`) that runs just this modernization step on any existing output report directory.

## What Changes

- **Add report UI mode property (`com.xceptance.xlt.reportgenerator.ui`) in `reportgenerator.properties`**:
  - Introduce `com.xceptance.xlt.reportgenerator.ui = modern | classic` (default `modern`) in `config/reportgenerator.properties` and `ReportGeneratorConfiguration`, leaving `bin/create_report.sh` and `bin/create_report.cmd` untouched.
  - Keep the existing XSLT 1.0 templates (`config/xsl/loadreport/*.xsl`), classic static assets (`config/testreport/`), and classic rendering pipeline completely intact when `classic` is used, while placing the modern data-driven HTML shells, CSS, and client JS engines in `config/testreport-modern/`.
  - Continue generating `ai-data.md` in both `modern` and `classic` modes.
- **Add Java `ModernReportGenerator` & extra standalone script (`bin/update_report_ui.sh` / `.cmd`) to modernize existing reports**:
  - Generate `js/report-meta.js` (`window.XLT_META`) and 16 per-page `js/*-data.js` bundles directly from `<outputDir>/testreport.xml`, optional `<outputDir>/scorecard.xml`, and `<outputDir>/charts/*/*.json` (including 20-bucket `spark` array downsampling for the duration-agnostic `Trend` column).
  - Integrate into `ReportGenerator.transformReport(...)`, `transformScorecard(...)`, and `updateScorecard()` when `ui == modern`.
  - Provide `com.xceptance.xlt.report.UpdateReportUiMain` and `bin/update_report_ui.sh` (`.cmd`) to modernize any existing output report directory in ~1 second by running only the modern UI template copy and `js/*-data.js` generation step without re-reading raw `timers.csv` logs.
- **Complete 100% data-driven HTML templates & full XLT feature parity in JS**:
  - Convert the remaining 6 static HTML pages (`index.html`, `errors.html`, `network.html`, `external.html`, `page-load-timings.html`, `web-vitals.html`) and shared top navigation (`nav.js` reading `window.XLT_META`) into pure templates with zero hardcoded test values.
  - Replace synthetic category guessing in `workbench.js` with true XLT `<labels>` support (`0` labels hides badges, column, and filter chips; `1–2` labels render inline `.label-chip` pills; `3+` labels render 2 pills + hoverable `+N` badge; multi-label filtering and search).
  - Drive Percentile (`<runtimePercentiles>`) and Runtime Segmentation (`<runtimeIntervals>`) columns dynamically from `window.XLT_META.testReportConfig`.
  - Support `<requestTableColorization>` cell color interpolation on `requests.html`, `<apdex>` column rendering on `actions.html`, `resultBrowsers/` snapshot links and `custom_data_logs/` links on `errors.html` / `slowest-requests.html`, and generic tables/charts on `external.html`.

## Capabilities

### New Capabilities
- `modern-load-report-generator`: Java report generation mode (`modern` vs `classic`), `testreport.xml` / `scorecard.xml` / `charts/*/*.json` to `js/report-meta.js` and `js/*-data.js` serialization, template asset copying from `config/testreport-modern/`, scorecard update integration, and standalone `bin/update_report_ui` CLI.
- `modern-load-report-ui`: Client-side single-run load report UI across all 16 HTML pages (`config/testreport-modern/`), including the three-zone workbench/directory/continuous-stream layout, `window.XLT_META` navigation/header binding, XLT `<labels>` rendering and filtering, dynamic percentile and runtime segmentation columns, request table colorization, Apdex display, Result Browser / custom log links, and offline `file:///` execution.

### Modified Capabilities
<!-- None -->

## Impact

- **Java Report Pipeline (`src/main/java/com/xceptance/xlt/report/`)**:
  - `ReportGenerator.java`, `ReportGeneratorConfiguration.java`, `XltConstants.java`
  - New `com.xceptance.xlt.report.modern.ModernReportGenerator` and `com.xceptance.xlt.report.UpdateReportUiMain`
- **Configuration & Scripts**:
  - `config/reportgenerator.properties` (`com.xceptance.xlt.reportgenerator.ui = modern`)
  - New `config/testreport-modern/` template & asset directory
  - New `bin/update_report_ui.sh` and `bin/update_report_ui.cmd`
- **Documentation & Prototype**:
  - `doc/feature-doc/new-reporting.md`

