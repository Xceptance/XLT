# AI Data Export

## Purpose

Provides AI-friendly data alongside the standard HTML and XML load test reports. The file (`ai-data.md`) is a YAML and Markdown hybrid, written to be read by a language model rather than a person: it states its own units, carries the rules that constrain what may be concluded from it, and covers the run's statistics, its network distributions, its errors grouped by message, and a coarse time series.

## Requirements

### Requirement: AI Summary File Generation
The system SHALL generate an `ai-data.md` file in the load report output directory during report
generation. The file SHALL contain a YAML+Markdown hybrid format with all key statistical data
from the report. The file SHALL begin with a single top-level heading and SHALL declare a
`schemaVersion`.

#### Scenario: AI summary generated during load report creation
- **WHEN** a load report is generated with the AI data transformation enabled (default)
- **THEN** an `ai-data.md` file SHALL be written to the report output directory alongside
  `testreport.xml` and `index.html`

#### Scenario: AI summary disabled via configuration
- **WHEN** the `transformations.18` entries referencing `ai-data.xsl` are removed or commented out
  in `reportgenerator.properties`
- **THEN** no `ai-data.md` file SHALL be generated

#### Scenario: AI summary enabled by default
- **WHEN** no changes are made to the default `reportgenerator.properties` transformations
- **THEN** the AI data file SHALL be generated (enabled by default as transformation #18)

#### Scenario: Schema version declared
- **WHEN** the AI data file is generated
- **THEN** it SHALL declare `schemaVersion: 2` in its header block, version 1 being the layout that
  shipped before this change, which this one extends

#### Scenario: Output contains no stray whitespace lines
- **WHEN** the AI data file is generated
- **THEN** it SHALL NOT contain lines consisting solely of whitespace produced by stylesheet
  indentation

---

### Requirement: AI Summary Content — Test Metadata
The AI data file SHALL include a single YAML header block containing test metadata, product
version, project name, and an explicit statement of the units used throughout the file.

#### Scenario: Metadata section present
- **WHEN** the AI data file is generated
- **THEN** the header SHALL contain start time, end time, duration, ramp-up period, hits, bytes
  sent, bytes received, XLT product version, project name, and a `units` mapping, emitted as one
  YAML block rather than several

#### Scenario: Units are stated per field group
- **WHEN** the `units` mapping is emitted
- **THEN** it SHALL distinguish response times (milliseconds), periods (seconds), sizes (bytes),
  rates (per second), and Web Vitals (CLS unitless, others milliseconds)

#### Scenario: Ramp-up period is visible in the header
- **WHEN** the load profile declares a ramp-up period
- **THEN** the header SHALL carry it, so steady-state contamination of run-wide rates is
  detectable without cross-referencing the load profile table

#### Scenario: XTC run identity included
- **WHEN** the report properties contain the XTC identifiers
- **THEN** the header SHALL carry organization, project, load test run id, result id and report id,
  so the run can be identified and two reports can be told apart

#### Scenario: XTC identity omitted when absent
- **WHEN** the report properties contain no XTC identifiers
- **THEN** the corresponding header entries SHALL be omitted rather than emitted as empty values

---

### Requirement: AI Summary Content — Transaction KPIs
The AI data file SHALL include a Markdown table with per-transaction statistics. Redundant and
projected columns SHALL be suppressed.

#### Scenario: Transaction table contains all configured metrics
- **WHEN** the report contains transaction data
- **THEN** the table SHALL include name, count, count/s, errors, error%, min, max, mean,
  deviation, and all configured percentiles

#### Scenario: No transactions present
- **WHEN** the report contains no transaction data
- **THEN** the transaction section SHALL be omitted

#### Scenario: Median suppressed when P50 present
- **WHEN** the report configures a `p50` percentile
- **THEN** the `Median` column SHALL be omitted, since it duplicates `P50` in every row

#### Scenario: Median retained when P50 absent
- **WHEN** the report configures percentiles that do not include `p50`
- **THEN** the `Median` column SHALL be emitted

#### Scenario: No projected count columns
- **WHEN** any timer table is emitted
- **THEN** it SHALL NOT contain per-minute, per-hour or per-day count columns, which are linear
  extrapolations indistinguishable from measurements in a Markdown table

#### Scenario: Millisecond values rounded
- **WHEN** a response time value is emitted
- **THEN** it SHALL be rounded to a whole millisecond

#### Scenario: Rates and percentages rounded
- **WHEN** a rate or percentage is emitted
- **THEN** it SHALL be formatted with at most two decimals and without trailing zeros

---

### Requirement: AI Summary Content — Action KPIs
The AI data file SHALL include a Markdown table with per-action statistics using the same column
rules, formatting and suppressions as the transaction table.

#### Scenario: Action table contains all configured metrics
- **WHEN** the report contains action data
- **THEN** the table SHALL include name, count, count/s, errors, error%, min, max, mean,
  deviation, and all configured percentiles, applying the same median suppression, projection
  exclusion and rounding rules as the transaction table

#### Scenario: Apdex excluded
- **WHEN** the report contains Apdex values for actions
- **THEN** they SHALL NOT be emitted, as the configured percentiles describe the same distribution
  without a hidden threshold

---

### Requirement: AI Summary Content — Request KPIs
The AI data file SHALL include a Markdown table with per-request statistics including network
timing means, omitting timing columns that carry no information.

#### Scenario: Request table contains timing breakdown
- **WHEN** the report contains request data
- **THEN** the table SHALL include the standard timer columns plus DNS, connect, send, server
  busy, receive and TTFB means, and bytes sent/received means

#### Scenario: Labels included
- **WHEN** at least one entry in a timer table carries a label
- **THEN** that table SHALL include a labels column, so entries can be grouped and compared by
  whatever the labeling rules assign rather than only by name

#### Scenario: Labels on every timer table
- **WHEN** labeling rules assign labels to timers other than requests
- **THEN** those tables SHALL carry the labels column too

#### Scenario: Labels column omitted when unused
- **WHEN** no entry in a table carries a label
- **THEN** that table SHALL omit the labels column

#### Scenario: Uniformly zero network columns suppressed
- **WHEN** a network timing column is zero for every request in the report
- **THEN** that column SHALL be omitted

---

### Requirement: AI Summary Content — Error Details
The AI data file SHALL aggregate errors by message, order them by descending occurrence count, and
include stack traces only for the largest groups.

#### Scenario: Errors present in report
- **WHEN** the report contains error data
- **THEN** the file SHALL list each distinct error message with its total count and a nested table
  of test case, action and count

#### Scenario: No errors present
- **WHEN** the report contains no error data
- **THEN** an explicit "no errors" indicator SHALL be included

#### Scenario: Errors grouped by message
- **WHEN** multiple error entries share an identical message
- **THEN** they SHALL be emitted as one group rather than as repeated entries

#### Scenario: Errors ordered by impact
- **WHEN** the error section is emitted
- **THEN** groups SHALL appear in descending order of total occurrence count

#### Scenario: Stack traces limited
- **WHEN** the error section is emitted
- **THEN** stack traces SHALL be included only for the top N groups (default 10, configurable) and
  SHALL be trimmed to the leading frames with an explicit marker for the omitted remainder

#### Scenario: Error section counters present
- **WHEN** the error section is emitted
- **THEN** it SHALL state total error count, distinct entry count, distinct message count, and how
  many groups carry traces

---

### Requirement: AI Summary Content — Agent Information
The AI data file SHALL include agent data sufficient to judge whether the load generators
themselves affected the measurements.

#### Scenario: Agent data present
- **WHEN** the report contains agent data
- **THEN** the file SHALL include a Markdown table with one row per agent

#### Scenario: Agent table includes saturation indicators
- **WHEN** the report contains agent data
- **THEN** the table SHALL include transactions, errors, error%, CPU mean, CPU maximum, full GC
  count and time, and minor GC count and time

#### Scenario: Consequence stated
- **WHEN** the agent section is emitted
- **THEN** it SHALL state that agent saturation inflates all measured times and that response
  times should then be read as upper bounds

---

### Requirement: AI Summary Content — Events
The AI summary SHALL include event data extracted from the `EventsReport` fragment.

#### Scenario: Events present
- **WHEN** the report contains event data
- **THEN** the AI summary SHALL include a summary of events with name and count

#### Scenario: No events
- **WHEN** no events are present
- **THEN** the events section SHALL be omitted

---

### Requirement: AI Summary Content — Page Load Timings
The AI summary SHALL include page load timing data extracted from the `PageLoadTimingsReport` fragment.

#### Scenario: Page load timing data present
- **WHEN** the report contains page load timing data
- **THEN** the AI summary SHALL include a Markdown table with name, count, min, max, mean, median, and configured percentiles

---

### Requirement: AI Summary Content — Web Vitals
The AI summary SHALL include web vitals data extracted from the `WebVitalsReports` fragment.

#### Scenario: Web vitals data present
- **WHEN** the report contains web vitals data
- **THEN** the AI summary SHALL include a Markdown table with the web vitals metrics

---

### Requirement: AI Summary Content — Custom Timers and Values
The AI summary SHALL include custom timer and custom value data from `CustomTimersReport` and `CustomValuesReportProvider` fragments.

#### Scenario: Custom timers present
- **WHEN** the report contains custom timer data
- **THEN** the AI summary SHALL include a Markdown table with name, count, min, max, mean, median, and configured percentiles

#### Scenario: Custom values present
- **WHEN** the report contains custom value data
- **THEN** the AI summary SHALL include a Markdown table with the custom value statistics

---

### Requirement: AI Summary Content — Load Profile
The AI summary SHALL include load profile configuration data extracted from the `ConfigurationReport` fragment.

#### Scenario: Load profile present
- **WHEN** the report contains load profile configuration
- **THEN** the AI summary SHALL include a YAML or table section listing each test case with its configured user count, iterations, and duration

---

### Requirement: Navigation Link
The report navigation SHALL include an "AI Data" link to `ai-data.md` in load reports.

#### Scenario: AI summary file exists in load report
- **WHEN** a load report is generated with the default configuration
- **THEN** the navigation bar SHALL display an "AI Data" link pointing to `ai-data.md`

#### Scenario: Trend and diff reports
- **WHEN** a trend or diff report is generated
- **THEN** the navigation bar SHALL NOT display the "AI Data" link (separate navigation templates)

---

### Requirement: Data Source — Standard Rendering Pipeline
The AI data file SHALL be generated as a stylesheet transformation within the standard report
rendering pipeline, reading data from `testreport.xml`.

#### Scenario: Template-based generation
- **WHEN** the report generator processes transformations
- **THEN** `ai-data.xsl` SHALL be processed like any other report stylesheet, reading from the XML
  data model

#### Scenario: Output format
- **WHEN** the stylesheet renders
- **THEN** it SHALL use a text output method, so that no escaping is applied to the Markdown and
  YAML it produces

#### Scenario: Processor capability
- **WHEN** the stylesheet is compiled
- **THEN** the resolved XSLT processor SHALL support XSLT 3.0, which the grouping, functions and
  regular expressions in this stylesheet require

---

### Requirement: AI Data Content — Reading Rules
The AI data file SHALL include a section stating the rules required to interpret it correctly,
phrased as constraints on inference rather than as descriptions of a user interface.

#### Scenario: Aggregate limitation stated
- **WHEN** the reading rules are emitted
- **THEN** they SHALL state that the statistical tables are aggregates over the whole run and that
  trends over time SHALL NOT be inferred from them

#### Scenario: Ramp-up contamination stated
- **WHEN** the reading rules are emitted
- **THEN** they SHALL state that run-wide rates include the ramp-up period and therefore
  understate steady-state throughput

#### Scenario: Timing columns are independent means
- **WHEN** the reading rules are emitted
- **THEN** they SHALL state that network timing columns are independent means that do not sum
  exactly and SHALL NOT be reconstructed from one another

#### Scenario: Text is specific to this file
- **WHEN** any explanatory text is emitted
- **THEN** it SHALL NOT reference charts, tabs, hover interactions, colour coding, or any other
  element absent from the Markdown file

---

### Requirement: AI Data Content — Overall Summary
The AI data file SHALL include an aggregate summary section covering all transactions, actions and
requests.

#### Scenario: Summary section present
- **WHEN** the report contains summary data
- **THEN** the file SHALL include a table of count, count/s, errors and error% per scope

---

### Requirement: AI Data Content — Network Distributions
The AI data file SHALL include the response code, content type, host and request method
distributions, each with a share of total hits.

#### Scenario: Response codes emitted
- **WHEN** the report contains response code data
- **THEN** the file SHALL include a table of code, status text, count and share of total hits

#### Scenario: Non-error status codes visible
- **WHEN** the run produced responses outside 2xx that did not trip a test assertion
- **THEN** they SHALL be visible in the response code table even though they do not appear in the
  error section

#### Scenario: Content types, hosts and request methods emitted
- **WHEN** the report contains the corresponding data
- **THEN** the file SHALL include a table for each, with counts and shares

#### Scenario: Network projections excluded
- **WHEN** the network data is emitted
- **THEN** per-hour and per-day projections SHALL NOT be included; raw totals and duration suffice

---

### Requirement: AI Data Content — Time Series
The AI data file SHALL include a downsampled time series for the aggregate transaction and request
scopes, so that ramp-up boundaries, degradation over time and error bursts are derivable.

#### Scenario: Single combined table
- **WHEN** time series data is available in the report
- **THEN** the file SHALL include one table covering all exported scopes, sharing the elapsed and
  absolute time columns, rather than a separate table per scope

#### Scenario: All three scopes present
- **WHEN** the time series table is emitted
- **THEN** it SHALL carry transaction, action and request columns, so that a rise confined to one
  layer can be told apart from a rise across all of them

#### Scenario: Errors over time
- **WHEN** the time series table is emitted
- **THEN** it SHALL include a transaction errors-per-second column

#### Scenario: Interval scales with test duration
- **WHEN** the time series is generated
- **THEN** the bucket interval SHALL be the smallest human-readable ladder value of at least
  duration divided by the target row count, so that row count stays bounded regardless of test
  length

#### Scenario: Two-hour test resolves to one-minute buckets
- **WHEN** the test duration is two hours
- **THEN** the bucket interval SHALL be one minute

#### Scenario: Interval is not configurable
- **WHEN** the interval is selected
- **THEN** it SHALL be derived in code with no configuration property, so a value that the source
  cannot deliver or that would outweigh the rest of the file cannot be requested

#### Scenario: Interval never finer than the source
- **WHEN** the computed interval is finer than the resolution of the underlying data
- **THEN** the source resolution SHALL be used instead

#### Scenario: Interval stated, not inferred
- **WHEN** the time series is emitted
- **THEN** the file SHALL declare the interval, the bucket count and the source resolution

#### Scenario: Independent of chart generation
- **WHEN** chart generation or dynamic chart output is disabled
- **THEN** the time series SHALL still be emitted, since its resolution is a property of the data
  and not of chart rendering

---

### Requirement: AI Data Content — Comments
The AI data file SHALL emit test comments as plain text that cannot alter the document structure.

#### Scenario: Raw comment source preferred
- **WHEN** the report model carries the raw comment text alongside the rendered form
- **THEN** the AI data file SHALL use the raw text, so that a Markdown-authored comment reaches the
  file as the author's Markdown rather than as de-tagged HTML

#### Scenario: Markup stripped
- **WHEN** a comment contains HTML markup, whether because no raw form is available or because the
  comment was authored as raw HTML
- **THEN** the markup SHALL be removed before the comment is written

#### Scenario: Structure protected
- **WHEN** a comment is emitted
- **THEN** every line SHALL be prefixed so that heading syntax within the comment cannot create a
  section in the document

---

### Requirement: AI Data Content — Numeric Formatting
The AI data file SHALL emit numbers in an unambiguous machine-readable form.

#### Scenario: No grouping separators
- **WHEN** any numeric value is emitted
- **THEN** it SHALL NOT contain thousands separators, which are ambiguous with decimal separators

#### Scenario: Raw values carried in the report model
- **WHEN** `testreport.xml` carries only a rendered display form of a value the AI data file needs,
  such as a range rendered with an ellipsis and a thousands separator
- **THEN** the report model SHALL additionally carry the raw value, and the AI data file SHALL use
  the raw value rather than parsing the rendered one

#### Scenario: Rendered forms preserved for existing consumers
- **WHEN** a raw value is added to the report model alongside a rendered one
- **THEN** the rendered form SHALL remain unchanged, so that existing stylesheets and report output
  are unaffected

#### Scenario: Every table rounded
- **WHEN** a table builds its own markup rather than using the shared timer template, such as
  custom values or web vitals
- **THEN** the same rounding rules SHALL apply to it

#### Scenario: Listings ordered by significance
- **WHEN** a section lists entries that carry a count, such as events or errors
- **THEN** the entries SHALL be ordered by descending count rather than by document order

#### Scenario: Unconfigured columns omitted
- **WHEN** a column carries a placeholder value for every row because the feature is not
  configured
- **THEN** the column SHALL be omitted rather than emitted as zero

#### Scenario: Constant columns stated once
- **WHEN** a load profile setting holds the same value for every test case
- **THEN** it SHALL be stated once above the table rather than repeated as a column, since a column
  of identical values invites a comparison that cannot be made

#### Scenario: Varying settings stay columns
- **WHEN** a load profile setting differs between test cases
- **THEN** it SHALL be emitted as a column

#### Scenario: Load function shape preserved
- **WHEN** a load function changes over the course of the run in a way the peak value and ramp-up
  period do not already describe
- **THEN** the file SHALL carry its points as time and value pairs, so that a stepped or spiky
  profile can be told apart from a smooth ramp

#### Scenario: A plain ramp-up needs no shape
- **WHEN** a load function climbs from its lowest value at the start of the run to its highest at
  the end of the ramp-up period, and does nothing else
- **THEN** its points SHALL be omitted, since the peak value and the ramp-up period already state
  the same thing

#### Scenario: Unmeasured scopes omitted from the summary
- **WHEN** a summary scope has a count of zero because the run never measured it
- **THEN** its row SHALL be omitted rather than emitted as zeros

#### Scenario: Explanations follow their columns
- **WHEN** a column is omitted because it carries nothing for this run
- **THEN** any text explaining that column SHALL be omitted with it

---

### Requirement: AI Data — User Documentation
The feature SHALL be documented for users under `doc/`, describing everything a user must know to
obtain, read, trust and customise the AI data file, without requiring them to read the stylesheet
or the report XML.

#### Scenario: Feature documentation exists and is accurate
- **WHEN** the change is complete
- **THEN** `doc/feature-doc/ai-data.md` SHALL describe the shipped artifact using its real file
  name, its real generating stylesheet, and its real configuration keys

#### Scenario: Every section documented
- **WHEN** a section is emitted into the AI data file
- **THEN** the documentation SHALL describe what that section contains, what the columns mean, and
  the units used

#### Scenario: Units and semantics documented
- **WHEN** the documentation describes the statistical tables
- **THEN** it SHALL state which values are milliseconds, which are seconds, which are bytes, and
  which are unitless

#### Scenario: Deliberate omissions documented
- **WHEN** the documentation describes the file's contents
- **THEN** it SHALL state what is deliberately excluded and why, covering at least Apdex,
  per-hour and per-day projections, the scorecard, per-entity time series, full stack traces for
  low-count errors, external data, and custom logs

#### Scenario: Limits of the data documented
- **WHEN** the documentation describes how to use the file
- **THEN** it SHALL state what cannot be concluded from it, including that the statistical tables
  are whole-run aggregates and that run-wide rates include the ramp-up period

#### Scenario: Time series documented
- **WHEN** the documentation describes the time series section
- **THEN** it SHALL explain that the bucket interval is derived from test duration, that it is
  stated in the file rather than fixed, and which scopes are exported

#### Scenario: Error aggregation documented
- **WHEN** the documentation describes the error section
- **THEN** it SHALL explain grouping by message, ordering by count, and the trace limit, including
  how to raise the trace limit

#### Scenario: Customisation documented
- **WHEN** a user wants to change what the file contains
- **THEN** the documentation SHALL explain how to disable the export, how to change the
  configurable parameters, and that editing the stylesheet requires no build step

#### Scenario: Usage guidance included
- **WHEN** a user wants to use the file with an LLM
- **THEN** the documentation SHALL include example prompts appropriate to the sections the file
  actually contains

#### Scenario: No stale references
- **WHEN** the documentation is reviewed
- **THEN** it SHALL contain no reference to `ai-summary.md`, to a FreeMarker template, or to
  configuration keys that do not exist

