## Context

XLT's report generator logs diagnostic output via `XltLogger.reportLogger`, an SLF4J logger named `"report"` backed by Log4j2. Currently this output goes only to the console (configured in `config/reportgenerator.properties` as the Log4j2 config). The output directory for reports is dynamic — determined at runtime from user arguments — so a static Log4j2 config cannot target it.

Three entry points generate reports: `ReportGeneratorMain` (load reports), `TrendReportGeneratorMain` (trend reports), and `DiffReportGeneratorMain` (diff reports). All use `ReportGenerator` which sets `config.setReportDirectory()` before generation begins.

## Goals / Non-Goals

**Goals:**
- Additionally copy all `reportLogger` output to a `report.log` file inside the report output directory (existing console logging stays unchanged)
- Timestamped, level-tagged lines for easy scanning and debugging
- Configurable log level for the file appender (independent of console)
- Configurable max file size (default 1 MB) to prevent bloated reports
- "Log" navigation link in the HTML report header

**Non-Goals:**
- Changing any existing console output behavior
- Capturing output from loggers other than `reportLogger`
- Structured/JSON log format (plain text is sufficient)
- Log rotation (single file per report run)

## Decisions

### 1. Tee via programmatic Log4j2 FileAppender

**Decision**: Add a `FileAppender` programmatically to the Log4j2 `"report"` logger at the start of report generation, remove it in a `finally` block.

**Rationale**: All ~40 existing `XltLogger.reportLogger.*()` call sites automatically write to both console and file with zero code changes. The alternative — a separate logger or manual writer — would require touching every call site and is fragile as new log statements are added.

**Implementation**:
```java
// Pseudocode — in ReportGenerator, after outputDir is known
LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
Configuration logConfig = ctx.getConfiguration();

PatternLayout layout = PatternLayout.newBuilder()
    .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %msg%n")
    .build();

FileAppender appender = FileAppender.newBuilder()
    .setName("report-log-file")
    .withFileName(new File(outputDir, "report.log").getAbsolutePath())
    .setLayout(layout)
    .build();
appender.start();

LoggerConfig reportLoggerConfig = logConfig.getLoggerConfig("report");
reportLoggerConfig.addAppender(appender, configuredLevel, null);
ctx.updateLoggers();
```

Remove in `finally`:
```java
reportLoggerConfig.removeAppender("report-log-file");
appender.stop();
ctx.updateLoggers();
```

**Alternatives rejected**:
- *Separate writer*: Requires modifying every log call site. High maintenance.
- *Static Log4j2 config*: Output dir is dynamic, not known at config load time.
- *Console output capture (System.out redirect)*: Fragile, captures unrelated output.

### 2. File size limiting

**Decision**: Use Log4j2's `SizeBasedTriggeringPolicy` with a `DefaultRolloverStrategy` that keeps only the tail (most recent entries). Default 1 MB, configurable via `com.xceptance.xlt.reportgenerator.reportLogging.maxSize`.

**Rationale**: Log4j2 handles this natively. If the report generation produces more than 1 MB of logs, the oldest entries are the least useful — recent errors and timings matter most.

**Alternative considered**: Post-generation truncation — simpler but loses atomicity and requires a second pass over the file.

### 3. Configurable log level

**Decision**: New property `com.xceptance.xlt.reportgenerator.reportLogging.level` (default: `INFO`). Applied as a `LevelRangeFilter` on the file appender only — console level is unaffected.

**Rationale**: Users who want verbose debug output for the file can set `DEBUG` without flooding the console. Users who only care about problems can set `WARN`.

### 4. Navigation link

**Decision**: Add a simple `<li><a href="report.log">Log</a></li>` after the existing "Configuration" entry in both `sections/navigation.ftl` and `common/sections/navigation.xsl`. The link always appears (the file is always generated). The log file opens as plain text in the browser.

**Alternative considered**: Conditional link (only if `report.log` exists). Rejected because the file is always generated and the unconditional link is simpler.

## Risks / Trade-offs

- **SLF4J abstraction layer**: We need Log4j2-specific API (`LogManager`, `LoggerConfig`) to add appenders programmatically. This creates a compile-time dependency on Log4j2-core, which already exists in the project. *Mitigation*: Isolate appender setup in a single helper method.
- **Concurrent report generation**: If two reports run in the same JVM (unlikely but possible in tests), the shared `"report"` logger would mix output. *Mitigation*: Use unique appender names per generation call.
- **File size overshoot**: Log4j2's size-based rollover may slightly exceed the limit before rolling. *Mitigation*: Acceptable for a 1 MB default; the overshoot is typically a few KB.
