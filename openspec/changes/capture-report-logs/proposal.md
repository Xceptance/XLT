## Why

During report generation, XLT logs warnings, errors, timing information, and diagnostic messages to the console via `XltLogger.reportLogger`. Once the report is generated and shared, this log output is lost — making it difficult to diagnose issues like missing data, configuration problems, scorecard evaluation errors, or unexpected merge rule behavior. Embedding the log output directly in the report provides a self-contained debug artifact that travels with the report.

## What Changes

- Capture all `XltLogger.reportLogger` output (including scorecard evaluation, chart generation, data processing, etc.) to a plain text file `report.log` in the report output directory
- Apply to all report types: load reports, trend reports, and diff reports
- **Timestamped output**: Each log line includes a precise timestamp for performance analysis and debugging
- **Log level visibility**: Each line clearly shows its log level (DEBUG, INFO, WARN, ERROR) so users can quickly spot issues
- **Configurable log level**: A property controls the minimum log level captured to the file (e.g., `com.xceptance.xlt.reportgenerator.log.level = INFO`), allowing users to turn volume up (DEBUG) or down (WARN/ERROR)
- **File size limit**: Cap `report.log` at 1 MB by default, configurable via `com.xceptance.xlt.reportgenerator.log.maxSize` property. Oldest entries are truncated when the limit is exceeded.
- Exclude interactive progress bar output — only structured log messages
- Add a "Log" link in the HTML report's header navigation bar that opens `report.log`

## Capabilities

### New Capabilities
- `report-log-capture`: Capturing report generator log output to a timestamped, level-tagged file with configurable volume and size limits, accessible from the report UI

### Modified Capabilities
_None — this is additive. No existing spec-level behavior changes._

## Impact

- **`ReportGenerator.java`**: Add a Log4j2 file appender at the start of report generation, remove it at the end
- **`TrendReportGeneratorMain.java`** / **`DiffReportGeneratorMain.java`**: Same appender lifecycle for trend/diff reports
- **`ReportGeneratorConfiguration.java`**: New properties for log level and max file size
- **FreeMarker navigation template** (`sections/navigation.ftl`): Add "Log" link to `report.log`
- **XSL navigation template** (`common/sections/navigation.xsl`): Add matching "Log" link
- **Log4j2**: Programmatic file appender (output directory is dynamic) with a pattern layout including timestamp and level
