# Modern Load Report UI

## Purpose

Renders the 16 single-run XLT load report pages completely from `window.XLT_META` and per-page `window.XLT_*` JavaScript bundles over `file:///`, providing the three-zone Top Workbench / Directory Table / Continuous Chart Stream layout and full parity with XLT labeling rules, dynamic percentile and runtime segmentation columns, request table colorization, Apdex, Result Browser links, custom data logs, and external data.

## ADDED Requirements

### Requirement: Pure Template HTML Pages and Dynamic Navigation Bar
All 16 HTML files in `config/testreport-modern/` SHALL contain zero hardcoded test-run values and SHALL populate their navigation header, footer, badges, and page body dynamically from `window.XLT_META` and their respective `window.XLT_*` data bundle.

#### Scenario: Navigation bar and header populated from XLT_META
- **WHEN** any report HTML page is opened in a browser
- **THEN** `nav.js` SHALL render the project name, load test name, start/end time, test duration, error count badge, agent count badge, and scorecard status badge (`PASSED` / `FAILED` / hidden when absent) from `window.XLT_META`

#### Scenario: Empty optional report sections
- **WHEN** an optional section (such as `pageLoadTimings`, `webVitals`, `external`, `customTimers`, `customValues`, `events`, or `errors`) has zero items in `window.XLT_META.counts`
- **THEN** its page SHALL render a clear empty-state message instead of hardcoded sample data

---

### Requirement: Three-Zone Workbench, Directory Table, and Full-Width Continuous Chart Stream
Timer workbench pages (`transactions.html`, `actions.html`, `requests.html`, `agents.html`, `custom-timers.html`, `custom-values.html`, `page-load-timings.html`) SHALL render a Top Sticky Chart Workbench and Directory Table inside `.workbench-scope`, followed immediately below the table by a full-width single-column Continuous Chart Stream (`#stream-section`).

#### Scenario: Scrolling from table into continuous chart stream
- **WHEN** the user scrolls down through the directory table and past its bottom edge into `#stream-section`
- **THEN** the Top Workbench SHALL stay sticky while the table is in view, unstick naturally at the end of `.workbench-scope`, and reveal full-width single-column chart cards (`grid-template-columns: 1fr`) ordered and filtered identically to the directory table

#### Scenario: Progressive batch rendering for large endpoint sets
- **WHEN** `#stream-section` renders more than 24 items (such as 1,000+ requests)
- **THEN** `workbench.js` SHALL insert stream cards in batches of 24 via `IntersectionObserver` with manual `Load Next 24` and `Load All Remaining` fallback controls, `loading="lazy" decoding="async"` on chart images, and `content-visibility: auto` on `.stream-card`

---

### Requirement: XLT Labeling Rules Display and Filtering
The directory table, Top Workbench, and Continuous Chart Stream SHALL display and filter XLT `<labels>` assigned to transactions, actions, and requests without inventing synthetic categories.

#### Scenario: No labels defined on any item on the page
- **WHEN** all items on a workbench page have an empty `labels` array (`labels.length === 0`)
- **THEN** no label badges SHALL be rendered, the label filter chip bar SHALL be hidden, and the `Labels` column SHALL be omitted from the directory table

#### Scenario: One or two labels defined on a row
- **WHEN** an item has 1 or 2 labels in `item.labels`
- **THEN** the `Labels` column SHALL render each label as an inline `.label-chip` pill

#### Scenario: Three or more labels defined on a row
- **WHEN** an item has 3 or more labels in `item.labels` (count = $N$)
- **THEN** the `Labels` column SHALL render the first 2 labels as `.label-chip` pills followed by a `+(N-2)` `.label-chip-more` badge that reveals all remaining labels on hover

#### Scenario: Filtering by label chip and search box
- **WHEN** labels exist on a page and the user clicks a label chip or enters a filter query (`foo bar -baz | qux`)
- **THEN** the directory table, Top Workbench, and Continuous Chart Stream SHALL filter items matching the selected label chip and matching the query against both item `name` and all entries in `item.labels`

---

### Requirement: Dynamic Percentile and Runtime Segmentation Columns
The directory table and diagnostic drawer SHALL dynamically construct Percentile columns and Runtime Segmentation columns from `window.XLT_META.testReportConfig`.

#### Scenario: Custom percentiles configured in reportgenerator.properties
- **WHEN** `window.XLT_META.testReportConfig.runtimePercentiles` specifies a custom list of percentiles (e.g., `["50", "90", "95", "99"]`)
- **THEN** the `Percentiles [ms]` column group and row cells SHALL render and sort those exact configured percentiles

#### Scenario: Custom runtime segmentation intervals on requests.html
- **WHEN** `window.XLT_META.testReportConfig.runtimeIntervals` specifies custom intervals
- **THEN** `requests.html` SHALL dynamically render a column for each configured interval showing the request count (or percentage) and full interval details on hover

---

### Requirement: Request Table Colorization and Action Apdex Parity
The UI SHALL apply XLT request table colorization rules on `requests.html` and render Apdex scores on `actions.html`.

#### Scenario: Request cell colorization rule matches
- **WHEN** a request has a `colorizationGroupName` (or matches the `default` colorization group) with rules in `window.XLT_META.testReportConfig.requestTableColorization`
- **THEN** matching `mean`, `min`, `max`, `percentile`, and `segmentation` cells SHALL interpolate their background/text color between the rule's `from` (green), `target` (neutral), and `to` (red) thresholds

#### Scenario: Action Apdex column rendering
- **WHEN** `actions.html` renders action rows containing `<apdex>` data
- **THEN** the table SHALL display a sortable `Apdex` column showing the Apdex value, threshold tooltip, and semantic rating color

---

### Requirement: Duration-Agnostic Trend Sparkline Column
The sparkline column in workbench tables SHALL be labeled `Trend` with a time-agnostic range tooltip (`Trend range: <min> – <max> ms`).

#### Scenario: Sparkline rendered across arbitrary test duration
- **WHEN** an item has a non-empty `spark` array from a 15-minute, 60-minute, or multi-hour test run
- **THEN** the `Trend` column SHALL render an inline SVG polyline scaled across those buckets with tooltip `"Trend range: <min> – <max> ms"`

---

### Requirement: Result Browser Links, Custom Data Logs, and External Data
The UI SHALL render clickable Result Browser links (`resultBrowsers/`), custom data log links (`custom_data_logs/`), and external data tables/charts (`external.html`).

#### Scenario: Error entry with Result Browser directory
- **WHEN** an error in `window.XLT_ERRORS` includes one or more Result Browser `<directory>` entries and `window.XLT_META.linkToResultBrowser` is `true`
- **THEN** `errors.html` SHALL render direct links to `resultBrowsers/<dir>/index.html` alongside the error stack trace and error chart links
