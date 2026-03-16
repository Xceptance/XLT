## ADDED Requirements

### Requirement: AI Summary File Generation
The system SHALL generate an `ai-summary.md` file in the load report output directory during report generation. The file SHALL contain a YAML+Markdown hybrid format with all key statistical data from the report.

#### Scenario: AI summary generated during load report creation
- **WHEN** a load report is generated with AI summary enabled (default)
- **THEN** an `ai-summary.md` file SHALL be written to the report output directory alongside `testreport.xml` and `index.html`

#### Scenario: AI summary disabled via configuration
- **WHEN** the property `com.xceptance.xlt.reportgenerator.aiSummary.enabled` is set to `false`
- **THEN** no `ai-summary.md` file SHALL be generated

#### Scenario: AI summary enabled by default
- **WHEN** no explicit configuration property is set for AI summary
- **THEN** the AI summary SHALL be generated (enabled by default)

---

### Requirement: AI Summary Content — Test Metadata
The AI summary SHALL include a YAML section with test metadata extracted from the `GeneralReport` fragment.

#### Scenario: Metadata section present
- **WHEN** the AI summary is generated
- **THEN** the file SHALL contain a YAML section with: test start time, end time, duration, and XLT product version

---

### Requirement: AI Summary Content — Transaction KPIs
The AI summary SHALL include a Markdown table with per-transaction statistics extracted from the `TransactionsReport` fragment.

#### Scenario: Transaction table contains all configured metrics
- **WHEN** the report contains transaction data
- **THEN** the AI summary SHALL include a Markdown table with columns for: name, count, count/s, errors, error%, min, max, mean, median, deviation, and all configured percentiles

#### Scenario: No transactions present
- **WHEN** the report contains no transaction data
- **THEN** the transaction section SHALL be omitted from the AI summary

---

### Requirement: AI Summary Content — Action KPIs
The AI summary SHALL include a Markdown table with per-action statistics extracted from the `ActionsReport` fragment.

#### Scenario: Action table contains all configured metrics
- **WHEN** the report contains action data
- **THEN** the AI summary SHALL include a Markdown table with columns for: name, count, count/s, errors, error%, min, max, mean, median, deviation, and all configured percentiles

---

### Requirement: AI Summary Content — Request KPIs
The AI summary SHALL include a Markdown table with per-request statistics extracted from the `RequestsReport` fragment.

#### Scenario: Request table contains timing breakdown
- **WHEN** the report contains request data
- **THEN** the AI summary SHALL include a Markdown table with columns for: name, count, count/s, errors, error%, min, max, mean, median, deviation, configured percentiles, plus DNS time, connect time, send time, server busy time, receive time, TTFB, and bytes sent/received (all as mean values)

---

### Requirement: AI Summary Content — Error Details
The AI summary SHALL include error details extracted from the `ErrorsReport` fragment, formatted as Markdown KV lists for optimal LLM data extraction accuracy.

#### Scenario: Errors present in report
- **WHEN** the report contains error data
- **THEN** the AI summary SHALL list each unique error with: test case name, action name, error message, count, and stack trace (truncated if excessively long)

#### Scenario: No errors present
- **WHEN** the report contains no error data
- **THEN** an "Errors: None" indicator SHALL be included in the AI summary

---

### Requirement: AI Summary Content — Agent Information
The AI summary SHALL include agent resource data extracted from the `AgentsReport` fragment.

#### Scenario: Agent data present
- **WHEN** the report contains agent data
- **THEN** the AI summary SHALL include a Markdown table with agent name and total transaction count per agent

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

### Requirement: AI Summary Content — Scorecard
The AI summary SHALL include scorecard results when present.

#### Scenario: Scorecard present
- **WHEN** a scorecard was evaluated during report generation
- **THEN** the AI summary SHALL include a section with the overall scorecard grade and rule results

#### Scenario: No scorecard
- **WHEN** no scorecard was evaluated
- **THEN** the scorecard section SHALL be omitted

---

### Requirement: Navigation Link
The report navigation SHALL include an "AI Data" link to `ai-summary.md` when the file is present.

#### Scenario: AI summary file exists in load report
- **WHEN** the `ai-summary.md` file exists in the report output directory
- **THEN** the navigation bar SHALL display an "AI Data" link pointing to `ai-summary.md`

#### Scenario: AI summary file does not exist
- **WHEN** the `ai-summary.md` file does not exist (e.g., in trend/diff reports or when disabled)
- **THEN** the navigation bar SHALL NOT display the "AI Data" link

---

### Requirement: Data Source — In-Memory TestReport
The AI summary writer SHALL read data directly from the in-memory `TestReport` object's fragment list, not from the serialized XML file.

#### Scenario: Data read from TestReport fragments
- **WHEN** the `AiSummaryWriter` generates the summary
- **THEN** it SHALL iterate `TestReport.getReportFragments()` and use `instanceof` checks to identify and format each fragment type

#### Scenario: Unknown fragment types are skipped
- **WHEN** the fragment list contains a type not recognized by `AiSummaryWriter`
- **THEN** that fragment SHALL be silently skipped without error

