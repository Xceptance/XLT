## ADDED Requirements

### Requirement: Direct HTML Generation

The system SHALL generate HTML reports directly from the XML data using a Java-based template engine, bypassing the XSLT transformation step.

#### Scenario: Generate Load Report

- **WHEN** the report generator is invoked for a load report
- **THEN** the system produces HTML output files
- **AND** the output is generated without invoking the XSLT processor

### Requirement: Support All Report Types

The direct HTML generation capability SHALL support all standard XLT report types including Load Reports, Scorecards, Trend Reports, and Diff Reports.

#### Scenario: Generate Scorecard

- **WHEN** the report generator is invoked for a scorecard
- **THEN** the system produces a scorecard HTML file

#### Scenario: Generate Trend Report

- **WHEN** the report generator is invoked for a trend report
- **THEN** the system produces a trend report HTML file

#### Scenario: Generate Diff Report

- **WHEN** the report generator is invoked for a diff report
- **THEN** the system produces a diff report HTML file

### Requirement: Output Parity

The direct HTML generation capability SHALL produce HTML output that is structurally and visually identical to the output produced by the XSLT pipeline for the same input data.

#### Scenario: Verify Output Matches XSLT

- **WHEN** a report is generated using both XSLT and Direct HTML modes for the same test data
- **THEN** resulting HTML files are effectively identical in structure and content, whitespaces and paths can differ
- **AND** any differences are limited to non-functional timestamps or generation metadata
