## ADDED Requirements

### Requirement: Integration tests validate standard load report output
The system SHALL have integration tests that use `ReportTestHarness` to generate a full load report from the sample data in `src/test/resources/results/` and assert key structural properties of the output.

#### Scenario: All expected HTML files are present
- **WHEN** a full report is generated from real sample data
- **THEN** the output directory contains at minimum: `index.html`, `transactions.html`, `actions.html`, `requests.html`, `errors.html`, `events.html`, `configuration.html`, `agents.html`

#### Scenario: Primary XML file is present and non-empty
- **WHEN** a full report is generated from real sample data
- **THEN** `testreport.xml` exists in the output directory and has a non-zero file size

#### Scenario: Report generation with property override changes output
- **WHEN** a report is generated with `com.xceptance.xlt.reportgenerator.linkToResultBrowsers=true` and another with `false`
- **THEN** the `errors.html` output differs between the two (one contains result browser links, the other does not)

### Requirement: Integration tests validate diff report output
The system SHALL have integration tests that generate a diff report from two sample result directories and assert key structural properties.

#### Scenario: Diff report HTML and XML are produced
- **WHEN** `generateDiffReport()` is called with two distinct sample result directories
- **THEN** `diffreport.xml` and `index.html` are present in the output directory

### Requirement: Integration tests validate trend report output
The system SHALL have integration tests that generate a trend report from multiple sample result directories.

#### Scenario: Trend report HTML and XML are produced
- **WHEN** `generateTrendReport()` is called with three or more sample result directories
- **THEN** `trendreport.xml` and `index.html` are present in the output directory

### Requirement: Integration tests validate scorecard output
The system SHALL have integration tests that run the scorecard updater against an existing report directory.

#### Scenario: Scorecard files are produced
- **WHEN** `generateScorecard()` is called on a report directory that contains a `testreport.xml`
- **THEN** `scorecard.xml` and `scorecard.html` are present in the report directory
