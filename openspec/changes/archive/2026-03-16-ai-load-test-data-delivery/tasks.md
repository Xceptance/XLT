## 1. Setup

- [ ] 1.1 Create git branch `features/ai-load-test-data-delivery` from current integration point
- [ ] 1.2 Add configuration property `com.xceptance.xlt.reportgenerator.aiSummary.enabled` to `ReportGeneratorConfiguration` (default: `true`)

## 2. Core Implementation — AiSummaryWriter

- [ ] 2.1 Create `AiSummaryWriter` class in `com.xceptance.xlt.report` package with `write(TestReport, File outputDir, ReportGeneratorConfiguration config)` method
- [ ] 2.2 Implement test metadata section from `GeneralReport` fragment (YAML: start/end time, duration, XLT version)
- [ ] 2.3 Implement transaction KPI table from `TransactionsReport` fragment (Markdown table with name, count, count/s, errors, error%, min, max, mean, median, deviation, percentiles)
- [ ] 2.4 Implement action KPI table from `ActionsReport` fragment (same column structure)
- [ ] 2.5 Implement request KPI table from `RequestsReport` fragment (extended with DNS/connect/send/server-busy/receive/TTFB/bytes)
- [ ] 2.6 Implement error details from `ErrorsReport` fragment (Markdown KV list per error)
- [ ] 2.7 Implement agent info table from `AgentsReport` fragment
- [ ] 2.8 Implement events section from `EventsReport` fragment
- [ ] 2.9 Implement page load timings table from `PageLoadTimingsReport` fragment
- [ ] 2.10 Implement web vitals table from `WebVitalsReports` fragment
- [ ] 2.11 Implement custom timers table from `CustomTimersReport` fragment
- [ ] 2.12 Implement custom values table from `CustomValuesReportProvider` output
- [ ] 2.13 Implement load profile section from `ConfigurationReport` fragment
- [ ] 2.14 Implement scorecard section (conditional, from scorecard data if present)
- [ ] 2.15 Implement performance summary section from `SummaryReport` fragment

## 3. Pipeline Integration

- [ ] 3.1 Modify `XmlReportGenerator.createReport()` to return or expose the `TestReport` object alongside the XML `File`
- [ ] 3.2 Call `AiSummaryWriter.write()` in `ReportGenerator.generateReport()` after `createReport()`, gated by the configuration property
- [ ] 3.3 Pass `aiSummaryPresent` boolean to `transformReport()` (similar to `scorecardPresent`)

## 4. Navigation Templates

- [ ] 4.1 Update FreeMarker navigation macro (`config/report-templates/sections/navigation.ftl`) to include conditional "AI Data" link
- [ ] 4.2 Update XSLT navigation template (`config/xsl/loadreport/sections/navigation.xsl`) to include conditional "AI Data" link
- [ ] 4.3 Update trend report navigation templates (FreeMarker + XSLT) — link only if file exists
- [ ] 4.4 Update diff report navigation templates (FreeMarker + XSLT) — link only if file exists

## 5. Testing

- [ ] 5.1 Write unit tests for `AiSummaryWriter` — verify all sections are generated from a mock `TestReport`
- [ ] 5.2 Write unit test for disabled configuration — verify no file is written when property is `false`
- [ ] 5.3 Write unit test for unknown fragment types — verify they are silently skipped
- [ ] 5.4 Write unit test for empty fragment types — verify sections are omitted gracefully
- [ ] 5.5 Integration test: generate a load report and verify `ai-summary.md` exists and contains expected sections

## 6. Verification

- [ ] 6.1 Run full test suite to verify no regressions
- [ ] 6.2 Generate a sample load report and inspect `ai-summary.md` content
- [ ] 6.3 Verify "AI Data" navigation link appears in the generated report
- [ ] 6.4 Verify "AI Data" link does NOT appear in trend/diff reports (no `ai-summary.md` present)
