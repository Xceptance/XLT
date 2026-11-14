# Tasks

## 1. Modern Static Template Directory & Shared Navigation Metadata (`config/testreport-modern/`)

- [x] 1.1 Copy the modern static assets (`*.html`, `css/style.css`, `images/*`, `js/*.js`) from `reports/modernized-report-v2/` into `config/testreport-modern/` (excluding sample `*-data.js` files) and verify all 16 HTML shells and static JS/CSS/image files exist under `config/testreport-modern/`.
- [x] 1.2 Update `config/testreport-modern/js/nav.js` and all 16 HTML shells in `config/testreport-modern/` to load `js/report-meta.js` and dynamically populate the document `<title>`, header project badge, load test badge, start/end/duration subtitle, `Errors` / `Agents` count pills, `Scorecard` status pill (`PASSED`/`FAILED` or hidden when absent), `AI Data` link (`ai-data.md`), and page footer from `window.XLT_META`, verifying with `node -c config/testreport-modern/js/nav.js` and `rg` that no hardcoded `BBW WEB` or `Load Test #15` strings remain in `config/testreport-modern/*.html`.
- [x] 1.3 Convert the remaining 6 static HTML pages in `config/testreport-modern/` (`index.html`, `errors.html`, `network.html`, `external.html`, `page-load-timings.html`, `web-vitals.html`) to pure templates backed by `js/overview-page.js` (`overview-data.js`), `js/errors-page.js` (`errors-data.js`), `js/network-page.js` (`network-data.js`), `js/external-page.js` (`external-data.js`), `page-load-timings-data.js`, and `web-vitals-data.js`, including `resultBrowsers/` and `custom_data_logs/` links on `errors.html` and clean empty-state rendering when a section has 0 items; verify syntax of all page scripts with `node -c` and confirm via `rg` that no hardcoded sample metrics remain in those 6 HTML files.

## 2. Workbench Engine Full XLT Feature Parity (`config/testreport-modern/js/workbench.js`)

- [x] 2.1 Update `config/testreport-modern/js/workbench.js` and `config/testreport-modern/css/style.css` to support XLT `<labels>` (`item.labels` array): omit the `Labels` column and hide the label filter chip bar when no item on the page has labels, render 1–2 labels as `.label-chip` pills, render 3+ labels as 2 `.label-chip` pills plus a `+(N-2)` `.label-chip-more` hover popover, and filter by label chips and boolean query against `name` + `labels`; verify with `node -c` and a headless Chrome test covering 0-label, 1-label, and 3-label items.
- [x] 2.2 Update `config/testreport-modern/js/workbench.js` and the timer HTML shells (`transactions.html`, `actions.html`, `requests.html`, `custom-timers.html`, `page-load-timings.html`) to dynamically construct Percentile columns (`window.XLT_META.testReportConfig.runtimePercentiles`) and Runtime Segmentation columns (`window.XLT_META.testReportConfig.runtimeIntervals`) instead of hardcoded `P50/P95/P99/P99.9` and 4 fixed segmentation columns; verify with `node -c` and a headless Chrome check using custom percentile/interval definitions.
- [x] 2.3 Implement `<requestTableColorization>` cell color interpolation in `config/testreport-modern/js/workbench.js` (matching `mean`, `min`, `max`, `percentile`, and `segmentation` rules by `item.colorizationGroup` or `default`) and `<apdex>` sortable column rendering on `actions.html`; verify with `node -c` and a headless Chrome DOM check confirming computed colorization attributes/styles on `requests.html` and Apdex cells on `actions.html`.

## 3. Java `ModernReportGenerator`, UI Mode Switch & Standalone CLI

- [x] 3.1 Add `MODERN_REPORT_RESOURCES_PATH = "testreport-modern"` and the `com.xceptance.xlt.reportgenerator.ui` property (`modern` default vs `classic`) to `XltConstants.java`, `ReportGeneratorConfiguration.java`, and `config/reportgenerator.properties`; verify compilation with `mvn test-compile`.
- [x] 3.2 Implement `com.xceptance.xlt.report.modern.ModernReportGenerator` to read `<outputDir>/testreport.xml`, optional `<outputDir>/scorecard.xml`, and `<outputDir>/charts/*/*.json` (downsampling chart JSON series into 20-bucket `spark` arrays) and write `js/report-meta.js` plus all per-page `js/*-data.js` bundles; write unit tests in `src/test/java/com/xceptance/xlt/report/modern/ModernReportGeneratorTest.java` covering metadata, `<labels>`, dynamic percentiles/intervals, colorization rules, 20-bucket sparkline downsampling, and missing-chart fallbacks, and verify `mvn test -Dtest=ModernReportGeneratorTest` passes.
- [x] 3.3 Wire `ModernReportGenerator` and `config/testreport-modern/` template copying into `ReportGenerator.transformReport(...)`, `transformScorecard(...)`, and `updateScorecard()` when `ui == modern` (preserving `ai-data.md` generation and keeping `ui == classic` behavior intact), and add `com.xceptance.xlt.report.UpdateReportUiMain` plus `bin/update_report_ui.sh` and `bin/update_report_ui.cmd`; verify with `mvn test-compile` and by running `bin/update_report_ui.sh` on a test report directory.

## 4. End-to-End Integration Verification

- [x] 4.1 Run `bin/update_report_ui.sh` against `reports/modernized-report-v2` (and `reports/xlt-report-bbw-bbwca-lt-2026-ca-366-20260831-154448`) and verify via headless Chrome CDP checks across all 16 HTML pages that `window.XLT_META` and `window.XLT_*` load without console errors, tables/charts/streams render accurately, and `openspec validate dynamic-modern-load-report` passes.

## Workflow follow-upxlt-result-aritzia-lt-2025-317-20260202-220012/

- Archive the change with `openspec archive` after implementation and verification are complete.

