## ADDED Requirements

### Requirement: Log file generation
The report generator SHALL write a copy of all `reportLogger` output to a file named `report.log` in the report output directory. This applies to load reports, trend reports, and diff reports. The existing console logging SHALL remain unchanged.

#### Scenario: Load report generates log file
- **WHEN** a load report is generated
- **THEN** a `report.log` file SHALL exist in the report output directory containing all report logger messages

#### Scenario: Trend report generates log file
- **WHEN** a trend report is generated
- **THEN** a `report.log` file SHALL exist in the report output directory

#### Scenario: Diff report generates log file
- **WHEN** a diff report is generated
- **THEN** a `report.log` file SHALL exist in the report output directory

#### Scenario: Console output unchanged
- **WHEN** a report is generated with log capture enabled
- **THEN** the console output SHALL be identical to what it was before this feature

### Requirement: Timestamped and level-tagged output
Each line in `report.log` SHALL include a precise timestamp and the log level. The format SHALL be: `yyyy-MM-dd HH:mm:ss.SSS [LEVEL] message`.

#### Scenario: Log line format
- **WHEN** a report logger message is written
- **THEN** the corresponding line in `report.log` SHALL match the pattern `yyyy-MM-dd HH:mm:ss.SSS [LEVEL] message` where LEVEL is one of DEBUG, INFO, WARN, ERROR

### Requirement: Configurable log level
The minimum log level captured to the file SHALL be configurable via the property `com.xceptance.xlt.reportgenerator.reportLogging.level`. The default level SHALL be `INFO`. Valid values are DEBUG, INFO, WARN, ERROR.

#### Scenario: Default level captures INFO and above
- **WHEN** no `reportLogging.level` property is set
- **THEN** `report.log` SHALL contain INFO, WARN, and ERROR messages but not DEBUG

#### Scenario: DEBUG level captures everything
- **WHEN** `com.xceptance.xlt.reportgenerator.reportLogging.level` is set to `DEBUG`
- **THEN** `report.log` SHALL contain DEBUG, INFO, WARN, and ERROR messages

#### Scenario: WARN level reduces volume
- **WHEN** `com.xceptance.xlt.reportgenerator.reportLogging.level` is set to `WARN`
- **THEN** `report.log` SHALL contain only WARN and ERROR messages

### Requirement: File size limit
The `report.log` file SHALL be limited to a maximum size, defaulting to 1 MB. The limit SHALL be configurable via the property `com.xceptance.xlt.reportgenerator.reportLogging.maxSize`. When the limit is exceeded, the oldest entries SHALL be truncated.

#### Scenario: Default size limit
- **WHEN** no `reportLogging.maxSize` property is set
- **THEN** the `report.log` file SHALL not exceed approximately 1 MB

#### Scenario: Custom size limit
- **WHEN** `com.xceptance.xlt.reportgenerator.reportLogging.maxSize` is set to `5m`
- **THEN** the `report.log` file SHALL not exceed approximately 5 MB

### Requirement: Navigation link
The HTML report SHALL include a "Log" link in the header navigation bar that opens `report.log`. The link SHALL appear in both FreeMarker and XSLT rendered reports.

#### Scenario: FreeMarker report has log link
- **WHEN** a FreeMarker-rendered report is viewed
- **THEN** the navigation bar SHALL contain a "Log" link pointing to `report.log`

#### Scenario: XSLT report has log link
- **WHEN** an XSLT-rendered report is viewed
- **THEN** the navigation bar SHALL contain a "Log" link pointing to `report.log`
