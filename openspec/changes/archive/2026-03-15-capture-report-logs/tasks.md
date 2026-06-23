## 1. Configuration

- [x] 1.1 Add `reportLogging.level` and `reportLogging.maxSize` properties to `ReportGeneratorConfiguration`
- [x] 1.2 Add commented-out defaults to `config/reportgenerator.properties`

## 2. Log Capture Infrastructure

- [x] 2.1 Create a helper class or method to add a programmatic Log4j2 `FileAppender` to the `"report"` logger
- [x] 2.2 Wire appender setup into `ReportGenerator.generateReport()` (add in try, remove in finally)
- [x] 2.3 Wire appender setup into `updateScorecard()` flow
- [x] 2.4 Wire log capture into `DiffReportGeneratorMain` and `TrendReportGeneratorMain`; convert their `System.out` calls to `XltLogger.reportLogger`

## 3. Navigation Link

- [x] 3.1 Add "Log" link to FreeMarker navigation template (`sections/navigation.ftl`)
- [x] 3.2 Add "Log" link to XSLT navigation templates (load/diff/trend)

## 4. Verification

- [x] 4.1 Compilation successful
- [x] 4.2 Integration test `testReportFromExistingResult` passed
- [x] 4.3 Verified: Log nav link present in all 4 navigation templates (FTL + 3 XSLT variants). Parity test failure pre-exists and is unrelated (meta charset attribute difference).
