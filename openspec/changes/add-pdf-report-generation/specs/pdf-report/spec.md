## Purpose

Provides optional pure-Java PDF report generation for XLT load test reports in a clean, print-ready landscape layout, downloadable directly from the generated HTML report.

## ADDED Requirements

### Requirement: Optional PDF Report Generation
The system SHALL support optional generation of a standalone PDF summary report during load test report generation.

#### Scenario: PDF generation enabled via CLI flag
- **WHEN** `create_report.sh` is executed with the `--pdf` (or `-pdf`) flag
- **THEN** the system SHALL generate a `load-report.pdf` file in the report output directory alongside `index.html`

#### Scenario: PDF generation enabled via configuration property
- **WHEN** `com.xceptance.xlt.reportgenerator.pdf.enable` is set to `true` in `reportgenerator.properties`
- **THEN** the system SHALL generate a `load-report.pdf` file in the report output directory

#### Scenario: PDF generation disabled by default
- **WHEN** neither the `--pdf` flag is provided nor the property is set to `true`
- **THEN** the system SHALL NOT generate a PDF report file

---

### Requirement: PDF Report Layout and Orientation
The PDF report SHALL be formatted in A4 Landscape orientation with professional print styling, running headers, and page numbers.

#### Scenario: Landscape page layout
- **WHEN** the PDF report is rendered
- **THEN** all pages SHALL use A4 Landscape page dimensions with consistent margins, headers, and footer page counts

---

### Requirement: PDF Report Content — Test Metadata and Summary
The PDF report SHALL include an executive summary section containing test metadata, overall KPIs, and scorecard results (if available).

#### Scenario: Test summary included
- **WHEN** the PDF report is generated
- **THEN** the PDF report SHALL include test start time, end time, duration, total requests, error rates, and scorecard results if scorecard evaluation is present

---

### Requirement: PDF Report Content — Overview Runtime Chart
The PDF report SHALL include the top-level overview runtime chart and SHALL omit granular per-transaction and per-request charts.

#### Scenario: Overview chart embedded
- **WHEN** chart generation is enabled for the report
- **THEN** the PDF report SHALL embed the main overview runtime chart image and omit individual timer and request charts

#### Scenario: Charts generation disabled
- **WHEN** chart generation is disabled via `noCharts`
- **THEN** the PDF report SHALL omit charts without causing errors

---

### Requirement: PDF Report Content — Runtime Statistics Tables
The PDF report SHALL include complete statistical tables for Transactions, Actions, Requests, and Network/HTTP status codes.

#### Scenario: Runtime tables rendered
- **WHEN** performance metrics are present in the load report XML
- **THEN** the PDF report SHALL render tables for Transactions, Actions, Requests, and Network status codes with repeated table headers across page breaks

---

### Requirement: PDF Report Content — Error and Event Statistics
The PDF report SHALL include statistical summary tables for Errors and Events while omitting verbose debugging stack traces.

#### Scenario: Error statistics summary
- **WHEN** errors or events occurred during the test
- **THEN** the PDF report SHALL include error count and event count summaries grouped by type without full stack traces

---

### Requirement: HTML Report Download Link
The HTML report navigation bar SHALL provide a download link to `load-report.pdf` when PDF generation is enabled.

#### Scenario: PDF download link present when PDF exists
- **WHEN** the HTML report is generated and PDF generation is enabled
- **THEN** the navigation bar in the HTML report SHALL display a download link pointing to `load-report.pdf`
