## 1. Configuration

- [x] 1.1 Add `reportLogging.level` and `reportLogging.maxSize` properties to `ReportGeneratorConfiguration`
- [x] 1.2 Add commented-out defaults to `config/reportgenerator.properties`

## 2. Log Capture Infrastructure

- [x] 2.1 Create a helper class or method to add a programmatic Log4j2 `FileAppender` to the `"report"` logger
- [x] 2.2 Wire appender setup into `ReportGenerator.generateReport()` (add in try, remove in finally)
- [x] 2.3 Wire appender setup into `updateScorecard()` flow
- [x] 2.4 Trend/diff reports use their own logger (not `reportLogger`) — no wiring needed

## 3. Navigation Link

- [x] 3.1 Add "Log" link to FreeMarker navigation template (`sections/navigation.ftl`)
- [x] 3.2 Add "Log" link to XSLT navigation templates (load/diff/trend)

## 4. Verification

- [x] 4.1 Compilation successful
- [x] 4.2 Integration test `testReportFromExistingResult` passed
- [ ] 4.3 Parity test may need nav link update (new "Log" entry)
