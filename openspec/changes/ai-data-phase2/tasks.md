## 0. Raw values in the report model (Java)

- [x] 0.1 Add a `rawComments` list to `ConfigurationReport`, index-aligned with `comments`, holding
      the comment with the `::markdown::` prefix removed when present and the value as-is otherwise;
      populate it in `ConfigurationReportProvider` alongside `processComment()`
- [x] 0.2 Add numeric min/max fields to `LoadProfileConfigurationReport` for both load functions —
      `arrivalRateMin` / `arrivalRateMax` and `numberOfUsersMin` / `numberOfUsersMax` — since
      `LoadFunctionXStreamConverter` renders both with grouping separators and an ellipsis range
- [x] 0.3 Verify the rendered forms are unchanged so the HTML report and existing stylesheets are
      unaffected

## 1. Foundation — ai-data.xsl

- [x] 1.1 Raise `config/xsl/loadreport/ai-data.xsl` to `version="3.0"`; add a header comment
      recording the Saxon/XSLT 3.0 requirement and why
- [x] 1.2 Add `xsl:function` helpers: `fn:ms()` (`format-number(x,'0')`), `fn:rate()` and
      `fn:pct()` (`format-number(x,'0.##')`), `fn:num()` (strips thousands separators)
- [x] 1.3 Eliminate whitespace leakage — 106 pure-whitespace lines today; wrap literals in
      `xsl:text` so indentation stays inside tags
- [x] 1.4 Single `# XLT Load Test Report — AI Data` h1; demote all sections to `##`

## 2. Header, comments, glossary

- [x] 2.1 Merge the three YAML fragments (Test Metadata / XLT Version / Project) into one header
      block; add `schemaVersion: 1`, the per-field `units` block, and `rampUpPeriod`. Comments stay
      out of the YAML and get their own section in 2.2
- [x] 2.1a Add the XTC identifiers to the header from
      `configuration/properties/property[@name='com.xceptance.xtc.*']/@value` — organization,
      project, `loadtest.run.id`, `loadtest.result.id`, `loadtest.report.id`; omit when absent
- [x] 2.2 Emit `## Comments` as blockquoted lines, reading `rawComments` when present and falling
      back to `comments`; strip HTML tags via `replace()` + `normalize-space()` in both cases
- [x] 2.3 Create `config/xsl/loadreport/text/ai-descriptions.xsl` with the AI-facing text
      (see design.md Decision 11); named templates mirroring `descriptions.xsl` naming
- [x] 2.3a `xsl:include` `ai-descriptions.xsl` from `ai-data.xsl`
- [x] 2.4 Emit the `## Reading this file` block from `ai-descriptions.xsl`
- [x] 2.5 Call the per-section description templates directly under each section heading

## 3. New sections from existing XML

- [x] 3.1 `## Summary` from `summary/*` (transactions, actions, requests, page load timings,
      custom timers)
- [x] 3.2 `## Response Codes` from `responseCodes/responseCode` with share against `general/hits`
- [x] 3.3 `## Content Types` from `contentTypes` with share
- [x] 3.4 `## Hosts` from `hosts` and `## Request Methods` from `requestMethods` with share
- [x] 3.5 Extend `## Agents` with CPU max, `fullGcCount`, `fullGcTime`, `minorGcCount`,
      `minorGcTime`

## 4. Table cleanup

- [x] 4.1 Rewrite the `timer-table` template using the rounding helpers
- [x] 4.2 Drop `Median` when a `p50` percentile is present; keep it otherwise
- [x] 4.3 Requests table: suppress network columns that are uniformly zero
- [x] 4.4 Load profile: drop `Iterations` when all zero; render `arrivalRateMin` /
      `arrivalRateMax` from the raw fields added in 0.2 (do not parse the rendered `1...3,535` form)
- [x] 4.5 Custom Values: apply the rounding helpers (own table, not covered by 4.1)
- [x] 4.6 Web Vitals: apply the rounding helpers and add the description line — CLS unitless, the
      rest milliseconds, all scores 75th percentile per action
- [x] 4.7 Events: sort by count descending (same reasoning as the error section)
- [x] 4.8 Verify no projection fields (`countPerMinute/Hour/Day`) are emitted anywhere

## 5. Error section redesign

- [x] 5.1 `xsl:for-each-group group-by="message"` with
      `xsl:sort select="sum(current-group()/count)" order="descending" data-type="number"`
- [x] 5.2 Emit the group overview table (rank, message, count, distinct test cases, distinct actions)
- [x] 5.3 Emit the per-group `(test case, action, count)` table, sorted descending
- [x] 5.4 Emit stack traces for the top N groups only (`xsl:param`, default 10), trimmed to the top
      8 frames via `tokenize(trace,'\n')` with a `... N more frames` marker
- [x] 5.5 Emit the counters block: `transactionErrors`, `distinctEntries`, `distinctMessages`,
      `tracesIncludedFor`

## 6. Time series (Java)

- [x] 6.1 Add an interval selector, fixed in code, no property: smallest ladder value
      (`1s,5s,10s,15s,30s,1m,2m,3m,5m,10m,15m,30m,1h`) at least `duration / 120`, floored at source
      resolution. Anchor check: a 2 h test must come out at 1 m
- [x] 6.2 Downsample `All Transactions`, `All Actions` and `All Requests` run-time and count/s
      series from the in-memory `TimeSeries`, independent of `shouldChartsGenerated()`
- [x] 6.3 Add transaction errors per second from `GeneralReportProvider.failedTransactionsValueSet`
- [x] 6.4 Emit one `<timeSeries>` element into `testreport.xml` carrying `interval`, `buckets`,
      `sourceResolution` and one row per bucket holding all three scopes; include elapsed seconds
      per row so the stylesheet never has to parse a formatted date
- [x] 6.5 Render a single `## Time Series` table in `ai-data.xsl` — `Elapsed`, `Time`, `Txn Mean`,
      `Txn /s`, `Txn Err/s`, `Act Mean`, `Req Mean`, `Req /s`. No `Max` columns
- [x] 6.6 Emit the `timeSeries:` metadata block so the interval is stated, not inferred

## 7. Corrections

- [x] 7.1 Fix the TTFB definition in `config/xsl/loadreport/text/descriptions.xsl` —
      TTFB = Connect + Send + ServerBusy, not including Receive Time
- [ ] 7.2 On archive, confirm `openspec/specs/ai-data-export/spec.md` picked up the `ai-summary.md`
      to `ai-data.md` rename from this change's delta
- [ ] 7.3 Remove the stale content from `doc/feature-doc/ai-data.md` — it documents
      `ai-summary.md`, a FreeMarker `ai-summary.ftl`, and a `transformations.18.templateFileName`
      property that does not exist

## 7a. User documentation (doc/feature-doc/ai-data.md)

- [ ] 7a.1 Quick start: where the file appears, what it is for, how to hand it to an LLM
- [ ] 7a.2 Section-by-section reference: every section the file can contain, what its columns mean
- [ ] 7a.3 Units table: which values are milliseconds, seconds, bytes, unitless
- [ ] 7a.4 Time series: how the bucket interval is derived from duration, why it is not fixed at
      one minute, which scopes are exported, how to read `Elapsed` vs `Time`
- [ ] 7a.5 Error section: grouping by message, ordering by count, the trace limit and how to
      raise it
- [ ] 7a.6 "What is deliberately not in here, and why" — Apdex, per-hour/per-day projections,
      scorecard, per-entity series, full traces for low-count errors, external data, custom logs
- [ ] 7a.7 "What you cannot conclude from this file" — whole-run aggregates, ramp-up-inclusive
      rates, timing means that do not sum
- [ ] 7a.8 Customisation: disabling the export, the configurable parameters, and that editing the
      stylesheet needs no build step
- [ ] 7a.9 Example prompts matched to the sections the file actually contains
- [ ] 7a.10 Review for stale references — no `ai-summary.md`, no FreeMarker, no non-existent
      configuration keys

## 8. Testing

- [x] 8.1 Unit test: transform a small fixture and assert the `units` block and `schemaVersion`
      are present
- [x] 8.2 Unit test: assert error groups are ordered by descending total count
- [x] 8.3 Unit test: assert no `Median` column when `p50` is configured, and that it is present
      when p50 is absent
- [x] 8.4 Unit test: assert comments contain no `<` and every line is blockquoted
- [x] 8.5 Unit test: assert the resolved `TransformerFactory` is Saxon, so a processor swap fails
      loudly rather than silently emitting garbage
- [x] 8.6 Unit test: interval ladder selection across durations of 5 min, 30 min, 1 h, 2 h, 4 h,
      8 h and 24 h; assert the 2 h case resolves to 1 m and that no case exceeds 120 rows
- [x] 8.6a Unit test: `rawComments` holds the Markdown source for a `::markdown::` comment and the
      original value otherwise, and `comments` is unchanged
- [x] 8.6b Unit test: `arrivalRateMin` / `arrivalRateMax` match the `int[][]` load function, and the
      rendered `arrivalRate` is unchanged
- [x] 8.6c Unit test: XTC block present when the properties exist, omitted entirely when they do not
- [x] 8.6d Unit test: events are ordered by descending count
- [x] 8.7 Unit test: assert no projection fields appear in the output

## 9. Verification

- [ ] 9.1 Regenerate against
      `reports/xlt-result-ariat-lt-2025-315-20251119-165727/testreport.xml` and diff section sizes
      against the 149,707-byte baseline; confirm ~35 KB
- [ ] 9.2 Confirm the ramp-up plateau is visible in the time series and matches `rampUpPeriod`
- [ ] 9.3 Confirm the 404s and redirects are now visible via the Response Codes section
- [ ] 9.4 Run the full test suite for regressions
- [ ] 9.5 Verify the report renders and the "AI Data" navigation link still resolves
