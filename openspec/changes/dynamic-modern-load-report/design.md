# Design

## Context

XLT's single-run load report generator (`ReportGenerator.java`) currently processes raw agent logs (`timers.csv`, etc.) into `<outputDir>/testreport.xml`, optional `<outputDir>/scorecard.xml`, WebP chart images (`charts/**/*.webp`), and time-series JSON files (`charts/**/*.json`), and then runs 16 single-threaded XSLT 1.0 transformations (`config/xsl/loadreport/*.xsl`) plus static asset copying from `config/testreport/` to produce the HTML report.

Meanwhile, the modernized single-run report prototype in `reports/modernized-report-v2/` uses static HTML shells + client-side JavaScript (`js/workbench.js`, `js/echarts-6.0.0.min.js`, etc.) reading `window.XLT_*` globals from `js/*-data.js`. However, 6 of its 16 pages (`index.html`, `errors.html`, `network.html`, `external.html`, `page-load-timings.html`, `web-vitals.html`) and the global navigation bar still contain hardcoded values from a single sample run, `workbench.js` assumes hardcoded single-category badges and fixed percentile/segmentation columns, and Java has no code path to emit `js/*-data.js`.

See `proposal.md` for motivation and scope, and `doc/feature-doc/new-reporting.md` (Part III) for full background.

## Goals / Non-Goals

**Goals:**
- Approach the modern report via a Java-to-JS data-bundle pipeline alongside the existing XSLT rendering (without replacing or modifying the classic report rendering or `bin/create_report.sh`), controlled via `com.xceptance.xlt.reportgenerator.ui` (`modern` by default, `classic` supported) in `config/reportgenerator.properties`.
- Generate `js/report-meta.js` and 16 per-page `js/*-data.js` bundles deterministically from `<outputDir>/testreport.xml`, optional `<outputDir>/scorecard.xml`, and `<outputDir>/charts/*/*.json`.
- Provide an extra standalone CLI script (`bin/update_report_ui.sh` / `.cmd` -> `UpdateReportUiMain`) that modernizes an existing output report directory in ~1 second by running only the modern UI template copy + `js/*-data.js` generation step.
- Achieve 100% XLT load-report feature parity in the modern UI: `<labels>` multi-label display/filtering, dynamic `<testReportConfig>` percentile & runtime segmentation columns, `<requestTableColorization>` cell color interpolation, `<apdex>`, `resultBrowsers/` and `custom_data_logs/` links, `external.html`, and `ai-data.md`.
- Maintain 100% offline `file:///` compatibility (no `fetch()` required for page rendering; only optional on-demand ECharts zoom uses `charts/*/*.json`).

**Non-Goals:**
- Replacing or modifying the existing classic XSLT 1.0 report rendering (`config/xsl/loadreport/*.xsl` and `config/testreport/` remain 100% intact).
- Modifying `bin/create_report.sh` or `bin/create_report.cmd` (standard `reportgenerator.properties` configuration handling is used as with any other property).
- Replacing or modifying raw timer log parsing (`DataReaderThread`, `DataParserThread`, `DataProcessor`s) or the `testreport.xml` / `scorecard.xml` schema.
- Converting `create_diff_report` or `create_trend_report` in this change (scheduled for a subsequent phase).

## Decisions

### 1. Source `js/*-data.js` from `testreport.xml` + `scorecard.xml` + `charts/*/*.json`
- **Decision**: Implement `com.xceptance.xlt.report.modern.ModernReportGenerator` to parse `<outputDir>/testreport.xml` (using standard StAX/DOM XML parsing or XStream/existing JAXB/DOM utilities), `<outputDir>/scorecard.xml`, and `<outputDir>/charts/*/*.json`, rather than hooking directly into in-memory `ReportProvider` state during `createReport()`.
- **Rationale**:
  1. Works identically during normal `create_report`, during `update_scorecard`, and when run standalone via `bin/update_report_ui.sh <reportDir>` on archived reports where in-memory `DataProcessor` state no longer exists.
  2. Avoids duplicating calculation logic already serialized into `testreport.xml` by the 21 `ReportProvider`s.
- **Alternatives considered**:
  - *Serializing directly inside each `ReportProvider`*: Would require re-parsing raw `timers.csv` whenever tweaking UI data structures and could not run standalone on existing `testreport.xml` folders.

### 2. Separate `config/testreport-modern/` Directory + `com.xceptance.xlt.reportgenerator.ui` Switch
- **Decision**: Store all modern static HTML shells, `css/style.css`, images, and client JS files in `config/testreport-modern/` (`XltConstants.MODERN_REPORT_RESOURCES_PATH = "testreport-modern"`), and switch inside `ReportGenerator.transformReport(...)` / `transformScorecard(...)` / `updateScorecard()` based on `config.getUiMode()` (`modern` default vs `classic`).
- **Rationale**: Zero file collisions with classic `config/testreport/` assets (`default.css`, jQuery scripts) and trivial fallback via `-Dcom.xceptance.xlt.reportgenerator.ui=classic`.
- **AI Summary (`ai-data.md`)**: In `modern` mode, `ReportGenerator.transformReport(...)` still runs the `ai-data.xsl` transformation (if configured in `reportgenerator.properties`) so `ai-data.md` is produced in both modes.

### 3. Per-Page `js/*-data.js` Bundles + Shared `js/report-meta.js`
- **Decision**: Emit a compact `js/report-meta.js` (`window.XLT_META`, <5 KB) loaded by all 16 pages alongside a single page-specific `js/<page>-data.js` bundle per HTML page, using standard `<script src="...">` tags and Gson/Jackson/streaming JSON writer wrapped in `window.XLT_<NAME> = ...;`.
- **Rationale**:
  1. `<script src="...">` loads synchronously under `file:///` without triggering browser CORS blocks.
  2. Keeps `requests-data.js` (~2.7 MB on 1,000+ endpoints) isolated to `requests.html` so `index.html`, `transactions.html`, `errors.html`, etc. open instantaneously.

### 4. Downsample `charts/*/*.json` into 20-Bucket `spark` Arrays in Java
- **Decision**: When building timer items (`transactions`, `actions`, `requests`, `custom-timers`, `page-load-timings`), `ModernReportGenerator` locates `<outputDir>/charts/<dir>/<chartBase>.json`, partitions the time-series points `[timestamp, mean, min, max, count]` into up to 20 equal slices across the test duration, and writes rounded bucket averages into `item.spark`.
- **Rationale**: Avoids 1,000+ XHR/fetch requests on `requests.html` and works offline over `file:///`. Because the 20 buckets span whatever time window the test ran (15m, 60m, 3h), the `Trend` column scales automatically to any test duration.

### 5. Native XLT `<labels>` & Dynamic `<testReportConfig>` Columns in `workbench.js`
- **Decision**:
  - Replace hardcoded `category`/`badge` heuristics with `item.labels` (array split on whitespace from `<labels>`). If no item on the current page has `labels.length > 0`, `workbench.js` omits the `Labels` column and hides the label filter chip bar. Otherwise, it renders up to 2 `.label-chip` pills inline plus a `+(N-2)` `.label-chip-more` hover badge for 3+ labels, and populates the filter chip bar with distinct labels and counts.
  - Replace static percentile/interval column definitions in `transactions.html`, `actions.html`, `requests.html`, `custom-timers.html`, and `page-load-timings.html` with dynamic column expansion in `workbench.js` driven by `window.XLT_META.testReportConfig.runtimePercentiles` and `window.XLT_META.testReportConfig.runtimeIntervals`.
  - Implement `<requestTableColorization>` cell background/text interpolation in `workbench.js` matching `config/xsl/loadreport/util/colorize.xsl`.

## Risks / Trade-offs

- **[Large `requests-data.js` payload on 1,000+ endpoint tests]** → Mitigated by isolating `requests-data.js` to `requests.html`, stripping redundant fields, and using progressive `BATCH = 24` `IntersectionObserver` rendering + `content-visibility: auto` on `#stream-section`.
- **[Custom percentiles or intervals with decimal IDs (e.g., `p99.9`)]** → Mitigated by storing percentiles in a keyed map (`item.percentiles["p99.9"]`) rather than flat top-level property names, and driving column definitions from `window.XLT_META.testReportConfig.runtimePercentiles`.
- **[Missing optional sections or `-noCharts` runs]** → Mitigated by null-safe XML parsing in `ModernReportGenerator`, empty `spark: []` fallback when chart JSONs are absent, and explicit empty-state rendering in every page script.

## Migration Plan

1. Populate `config/testreport-modern/` from `reports/modernized-report-v2/` and refactor all 16 HTML pages + JS scripts to be 100% data-driven from `window.XLT_META` and `window.XLT_*`.
2. Implement `ModernReportGenerator` and `UpdateReportUiMain` (`bin/update_report_ui.sh` / `.cmd`), and wire `com.xceptance.xlt.reportgenerator.ui` into `ReportGenerator` and `config/reportgenerator.properties`.
3. Validate by running `bin/update_report_ui.sh` against existing report directories in `reports/` and running unit/headless browser verification across all 16 pages. Rollback at any time is a single property change (`com.xceptance.xlt.reportgenerator.ui = classic`).

