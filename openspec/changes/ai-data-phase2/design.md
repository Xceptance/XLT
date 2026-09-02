## Context

`ai-data.md` is produced by `config/xsl/loadreport/ai-data.xsl` as transformation #18
(`config/reportgenerator.properties:492`), transforming `testreport.xml` to text. It is a static
artifact pasted into an LLM by a human, or read by an agent alongside the report.

All measurements below come from a real run:
`reports/xlt-result-ariat-lt-2025-315-20251119-165727` — 4,500 s, 3,137,711 hits, 99,987
transactions, 7,306 transaction errors, 4 agents.

### Baseline measurement

| Section | Bytes | Share |
|---|---:|---:|
| Errors | 119,235 | 79.6% |
| Requests | 17,924 | 12.0% |
| Actions | 4,287 | 2.9% |
| Transactions | 2,143 | 1.4% |
| Events | 1,979 | 1.3% |
| Custom Values | 1,784 | 1.2% |
| Load Profile | 742 | 0.5% |
| Custom Timers | 740 | 0.5% |
| Agents | 425 | 0.3% |
| Test Metadata / Comments / Version / Project | 447 | 0.3% |
| **Total** | **149,707** | |

100 error entries; 100,300 bytes of that is stack trace, every one at the 1000-character cap.
106 lines of the file are pure whitespace leaked from XSL indentation through `method="text"`.

## Goals / Non-Goals

**Goals**
- Reduce `ai-data.md` to roughly 35 KB while increasing the information it carries.
  (Measured outcome: 47.5 KB, see the verification note at the end.)
- Make every value unambiguous without inference (units, semantics, what is and is not derivable).
- Give the file a time dimension.
- Keep the file a single self-contained artifact requiring no external context.

**Non-Goals**
- Apdex, scorecard, trend/diff reports, per-entity time series (see proposal.md).
- Any LLM integration. This is data packaging.
- Changing the HTML report's content or layout, beyond one factual correction to its help text.

## Decisions

### 1. XSLT 3.0 under Saxon

`XSLTUtils` uses `TransformerFactory.newInstance()` (`src/main/java/com/xceptance/common/xml/XSLTUtils.java:48`)
and never overrides it outside tests. Saxon-HE 12.5 is a direct dependency (`pom.xml:638`) and
ships in `lib/`, so it wins the JAXP `ServiceLoader` lookup. Verified end-to-end against the real
`lib/` directory:

```
FACTORY: net.sf.saxon.TransformerFactoryImpl
factory=Saxonica
```

with `xsl:for-each-group` evaluating correctly. This gives us `for-each-group`, `xsl:function`,
`tokenize`, `replace`, `every ... satisfies` and `format-number` — no Muenchian grouping needed.

**Risk**: `ai-data.xsl` becomes the only stylesheet that cannot run on a JDK-default Xalan. If the
`TransformerFactory` is ever swapped, it fails with a confusing error rather than degrading.
**Mitigation**: state the requirement in a header comment, and assert the processor in the new
unit test so a change fails loudly.

### 2. Rounding and column selection

- Times → `format-number(x, '0')`. `31529.106` ms carries three decimals of noise on a
  millisecond-resolution measurement.
- Rates and percentages → `format-number(x, '0.##')`, so `0.000` becomes `0` and `0.037` becomes
  `0.04`.
- `Median` is dropped **only when a `p50` percentile is configured**, since they are then the same
  number in every row. Where percentiles are configured without p50, `Median` is the only source
  and stays.
- A request network column is suppressed when it is zero for every row, tested with
  `every $r in $rows satisfies number($r/dnsTime/mean) = 0`.
- The request table carries a `Labels` column from `TimerReport.labels`, the whitespace-delimited
  label string the report's labeling rules assign. It is what a run uses to qualify its requests -
  by business area, page type, or anything else - so it lets a model compare areas rather than
  individual request names. Omitted when no request carries a label, like the network columns.
  (`colorizationGroupName` also exists on the request report and is populated in the sample, but it
  is a presentation grouping derived for colouring the HTML table, not a label the run assigned.)
- `Iterations` is dropped when all rows are 0. Zero does not mean "zero iterations completed", it
  means "not configured", and a model reads it literally.

### 3. Never emit projections

`countPerMinute`, `countPerHour`, `countPerDay` exist on every timer. For this run `countPerDay`
is `1,919,750.400` — a linear extrapolation from 75 minutes. A Markdown table cannot mark that as
a projection, and a model will cite it as capacity. `Count`, `Count/s` and `duration` in the
header let it compute any rate it needs, with the extrapolation base visible.

The same applies to the Network summary table in `network.html`, whose `1/h*` and `1/d*` columns
are already asterisked as projections in the HTML. Only the raw totals are carried over.

### 4. Raw values in the XML, never reverse-engineered display strings

`testreport.xml` carries display-formatted strings where the model holds richer data.
`<arrivalRate>1...3,535</arrivalRate>` is an ellipsis range with a thousands separator, produced by
`@XStreamConverter(LoadFunctionXStreamConverter.class)` on
`LoadProfileConfigurationReport.arrivalRate`, which is an `int[][]` load function
(`src/main/java/com/xceptance/xlt/report/providers/LoadProfileConfigurationReport.java:33`).

Parsing that string back apart in XSLT was the original plan. It is the wrong approach: it makes
the AI export depend on a rendering format chosen for humans, and a comma is a decimal separator
in half of Europe and ambiguous inside a pipe table.

**Principle**: where `testreport.xml` carries only a rendered form of a value the AI export needs,
add the raw value to the report model rather than reverse-engineering the rendered one. The raw
and rendered forms coexist; existing consumers are untouched.

Applied here: emit min/max numeric fields alongside the rendered form. `numberOfUsers` needs it as
much as `arrivalRate` — it carries the same `@XStreamConverter` and renders through the same
`String.format("%,d")`, which is where a flat 1021-user profile turns into `1,021` in the table.
So both get min/max fields.

The `fn:num()` helper remains as a defensive guard for any field not yet covered, but nothing
should depend on it.

### 5. Error aggregation

Group by `message`, sort groups by `sum(current-group()/count)` descending. Within a group emit a
`(test case, action, count)` table, also sorted descending. Emit a stack trace only for the top N
groups (N = 10, exposed as an `xsl:param`), trimmed to the top 8 frames with a
`... N more frames` marker.

Grouping by message alone will merge errors sharing a message but arising from different code
paths. Accepted for this iteration: the per-group test-case/action table keeps the distinction
visible. If it proves too coarse, the key becomes `message || first-non-framework-frame`.

### 6. Comments

Emitted as a `## Comments` section of blockquoted lines, not inside the YAML block.

Two reasons. Multi-line Markdown inside a YAML scalar needs `|` block-scalar handling and is
fragile. And comments are user-authored free text flowing into an LLM prompt: a comment
containing `# Errors` would forge a section heading and split the document. Prefixing every line
with `> ` neutralises that.

**Comments reach the XML as HTML regardless of the Markdown feature.** The `::markdown::` prefix
(`doc/feature-doc/markdown-comments.md`) converts Markdown to HTML and wraps it in a
`<div class="markdown">`, and `ConfigurationReportProvider` stores only the processed result
(`report.comments.add(processComment(entry.getValue()))`,
`src/main/java/com/xceptance/xlt/report/providers/ConfigurationReportProvider.java:140`). The raw
Markdown source is discarded, so tag-stripping would be permanent, not a stopgap.

Applying the Decision 4 principle: **retain the raw comment alongside the rendered one.** A
parallel `rawComments` list on `ConfigurationReport`, index-aligned with `comments`, holding the
property value with the `::markdown::` prefix removed when present and the value as-is otherwise.
Additive, so the HTML report and every existing stylesheet are untouched.

The AI export then reads `rawComments`, which for a `::markdown::` comment is the author's actual
Markdown — strictly better input than de-tagged HTML. Tag-stripping stays as a fallback, because a
comment without the prefix may legitimately be raw HTML, and blockquoting stays regardless, since a
raw Markdown comment containing `# Errors` is exactly the structure-forging case it guards against.

The stripping fallback uses `replace($c, '&lt;[^&gt;]*&gt;', ' ')` and `normalize-space()`, which
handles today's shape (`<div class='xtc_comment'><strong>...</strong><p>...</p></div>`) and any
raw HTML a user writes by hand. Until task 0.1 lands, that fallback is the only path and the
export still works — just with de-tagged HTML instead of the author's Markdown.

### 7. Time series

**Source.** The dynamic chart JSON blobs under `charts/` are written by
`BasicTimerDataProcessor.saveResponseTimeSeriesAsJson`
(`src/main/java/com/xceptance/xlt/report/providers/BasicTimerDataProcessor.java:485`) with schema:

```
[periodStartMillis, avgRuntime, minRuntime, maxRuntime, avgCountPerSecond]
```

**Resolution is a rendering artifact.** `minMaxValueSetSize = getConfiguration().getChartWidth()`,
so every series has exactly `chartWidth` buckets — 900 here, giving 4-second buckets over 4,780 s.
Widening the charts would silently change the AI file's time resolution. The blobs are also gated
on `shouldChartsGenerated() && dynamicChartsEnabled()`
(`BasicTimerDataProcessor.java:144`, `:203`; both default true).

**Decision**: do not read the JSON from XSLT. Downsample from the in-memory `TimeSeries` in Java
and emit a `<timeSeries>` element into `testreport.xml`, so resolution is a property of the data
rather than of an image, and the export does not depend on chart generation.

**Interval: target a row count, not a fixed period.** Fixed per-minute fails at both ends — a
5-minute test yields 5 rows and hides ramp-up entirely, a 24-hour test yields 1,440 rows
(more than the rest of the file) and is not even achievable, since source granularity at 24 hours
is 96 s. Instead, take the smallest value from the ladder
`1s, 5s, 10s, 15s, 30s, 1m, 2m, 3m, 5m, 10m, 15m, 30m, 1h` that is at least `duration / 120`,
floored at the source resolution.

The divisor is 120 rather than 90 because a 2-hour test must land on 1-minute buckets — that is the
anchor point. Everything shorter or longer follows from it:

| Duration | duration/120 | Interval | Rows |
|---|---:|---|---:|
| 5 min | 2.5 s | 5 s | 60 |
| 15 min | 7.5 s | 10 s | 90 |
| 30 min | 15 s | 15 s | 120 |
| 1 h | 30 s | 30 s | 120 |
| 75 min | 37.5 s | 1 m | 75 |
| **2 h** | **60 s** | **1 m** | **120** |
| 4 h | 120 s | 2 m | 120 |
| 8 h | 240 s | 5 m | 96 |
| 24 h | 720 s | 15 m | 96 |

Rows stay between 60 and 120 across a 300x range of durations. `interval`, `buckets` and
`sourceResolution` are stated in the file so the model never infers them from timestamps.

**Fixed, not configurable.** The ladder and the divisor live in code. A property would let someone
ask for a row count the source cannot deliver, or one that silently makes the series larger than
everything else in the file — and nothing would error, it would just get expensive. If a real need
turns up, one property is easy to add later; removing one is not.

**Scope: all three, in one table.** `All Transactions`, `All Actions` and `All Requests`.

Actions are not redundant. Their *count* is roughly requests per second divided by requests per
action, but their *runtime* is not derivable from anything else. When action time rises while
request time stays flat, the slowdown is in JavaScript, waits, or the test code rather than the
server — a diagnosis the other two curves cannot support.

Three separate tables would repeat `Elapsed` and `Time` three times, about 9 KB. One combined table
shares them and costs roughly 7 KB at the 120-row maximum, less than two separate tables would
have, and it puts the three numbers on the same row where they can actually be compared:

```
| Elapsed | Time | Txn Mean | Txn /s | Txn Err/s | Act Mean | Req Mean | Req /s |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: |
|  900 | 17:12 | 41683 | 22.5 | 0.02 | 512 | 98 | 201.4 |
```

**Columns**: `Elapsed` and `Time` both. Elapsed makes the ramp-up check arithmetic-free (compare
against `rampUpPeriod`); absolute time is what correlates a spike with server logs or APM during
incident analysis. `Max` is dropped from the combined table — with three scopes it would add three
more columns for a signal the per-entity `Max` and `P99.9` already carry.

**Errors column.** The chart JSON has no error field, but `GeneralReportProvider` already
maintains `failedTransactionsValueSet`. Since we are downsampling in Java anyway, the table carries
`Txn Err/s`. The sample run's 7,306 errors are currently undistinguishable between a single bad
minute and a steady trickle — the difference between an incident and a systemic defect.

**Validation.** Downsampling the sample to 1 minute yields 81 rows / 2,808 bytes, and `Count/s`
climbs 0 → 22.5 then plateaus at 17:12–17:13. The run starts 16:57 and declares
`rampUpPeriod 900`; 900 s lands exactly on 17:12. The data confirms the declared ramp-up, which
is precisely the inference the current file cannot support.

### 7a. Reserved: scorecard

When the scorecard becomes reliable it enters as an `## Acceptance` section, gated on a clean
`outcome` element, and is omitted entirely otherwise. Recorded here so it is not a redesign later.
Nothing is built in this change.

### 8. Deferred: slowest requests

`slowestRequests` is not in `ai-data.md` today, and this change does not add it. The analysis below
is kept so the work does not have to be redone.

**The sample is filtered and stratified, not a ranking.** Four properties shape it
(`config/reportgenerator.properties:414-423`):

| Property | Default | Effect |
|---|---:|---|
| `slowestRequests.requestsPerBucket` | 20 | Max kept per request name |
| `slowestRequests.totalRequests` | 500 | Max shown in the report |
| `slowestRequests.minRuntime` | 3000 | Only requests at least this slow are kept |
| `slowestRequests.maxRuntime` | 600000 | Requests slower than this are dropped |

This explains the sample: the fastest of the 500 is 3,374 ms, just above the 3 s floor, and eight
request names sit at exactly 20 entries. Any future section must state all four, or a model will
assume a global ranking and compute a distribution that means nothing.

What the 500 entries showed:

- Median runtime 5,554 ms; 453 of 500 under 10 s; only 6 reached the 30 s timeout. Within the 3 s
  floor the spread is unremarkable, and `Max` and `P99.9` in the Requests table already describe it.
- 491 of 500 returned HTTP 200 — slowness is not error-correlated.
- The timestamps cluster: 57 in the 18:10 minute, 54 at 19:00, 46 at 19:05, against a 6.7/min
  baseline. That is a different signal from the Decision 7 time series, which shows overall
  slowness rather than where the extreme outliers landed.

Two obstacles a later change has to clear:

- **Timestamps need an epoch value.** `SlowRequestReport.time` is a `java.util.Date`
  (`src/main/java/com/xceptance/xlt/report/providers/SlowRequestReport.java:45`) rendered by the
  registered `DateConverter("yyyy-MM-dd HH:mm:ss z", ...)`
  (`src/main/java/com/xceptance/xlt/report/XmlReportGenerator.java:94`), so the XML carries
  `2025-11-19 18:01:19 CET` — not ISO 8601, with a timezone abbreviation that cannot be mapped back
  reliably. Bucketing it in XSLT would need string surgery plus a hand-written CET/CEST table. The
  Decision 4 fix applies: add an epoch-millis field beside the `Date`.
- **Per-sample network timings must stay out.** The slowest row has
  `runtime 30372, TTFB 30041, connect 2, send 7, serverBusy 0, receive 330` — TTFB is 30,041
  against components summing to 9. `TTFB = Connect + Send + ServerBusy` holds in aggregate but not
  per sample, so exporting both the rule and these rows hands the model a contradiction.

### 9. Network sections

`network.html` renders `general`, `hosts`, `ips`, `requestMethods`, `responseCodes`,
`contentTypes` (`config/xsl/loadreport/network.xsl:67-119`). Only the three raw `general` totals
reach `ai-data.md` today.

The five distribution tables are 17 rows total in this report and change what is knowable. Against
3,137,711 hits: 3,069,046 × 200, 46,752 × 301, 11,053 × 302, 10,762 × 404, 81 × 403, 11 × 502,
6 × code 0. The 403s and 502s appear in Errors because they tripped assertions; **the 10,762 404s
and 57,805 redirects appear nowhere in the current file** — nothing asserted on them, so they
never became errors.

Content types corroborate: the empty content type counts 46,758 against 46,752 redirects, and
`application/speculationrules+json` is 404,485 hits — 12.9% of all traffic.

**Decision**: add Response Codes, Content Types, Hosts and Request Methods, each with a share
column computed against `hits`. Skip IPs (`(unknown)` here, infrastructure trivia).

### 10. Agent health

The Agents table currently exports CPU mean only. The sample: CPU mean 33.5%, max 44.8%,
`fullGcCount` 0, `minorGcCount` 3894, `minorGcTime` 356,020 ms.

This run is fine — and nothing in `ai-data.md` lets a model establish that. If the load generators
had been saturated, every response time in the file would be inflated by client-side queuing and
the model would attribute it to the application. It is the one fact that decides whether the rest
of the data means anything.

**Decision**: add CPU max alongside mean, plus full/minor GC count and time, and a glossary line
stating the consequence.

### 11. Explanatory text: fork, do not reuse

`config/xsl/loadreport/text/descriptions.xsl` describes an HTML page — the Overview tab, hovering
for a URL popup, colour-coded ratings, five chart sections, the Runtime Segmentation column,
"change the setup in the report generator properties". None of that exists in `ai-data.md`.
Verbatim it would cost ~1,200 tokens and actively mislead.

**Decision**: a separate `config/xsl/loadreport/text/ai-descriptions.xsl`, deliberately forked.
Same review surface, different audience; single-sourcing would make both worse. Text is written as
inference rules, not definitions — "think time is deliberate and often dominates, so a slow
transaction does not imply a slow application"; "a request row with zero errors can still have
served wrong content". Budget ~550 tokens total.

Global rules go in a `## Reading this file` block; per-concept text goes directly under each
section heading, adjacent to the table it governs.

### 12. Units are per-field, not global

Most measurements are milliseconds, but not all. The block enumerates:

```yaml
units:
  responseTimes: ms      # min, max, mean, dev, all P* columns, all network timings
  periods: s             # duration, rampUp, measurement, shutdown
  thinkTime: ms
  sizes: bytes
  webVitals: "CLS unitless score; FCP/LCP/INP/TTFB ms"
  rates: per second
```

### 13. Correction to the human help text

`descriptions.xsl` states that Time to First Bytes "Includes Connect, Send, Server Busy, and
Receive Time." This is self-contradictory — Receive Time is defined two lines earlier as
first-byte-to-last-byte — and the data disproves it:

| name | conn | send | busy | recv | TTFB | c+s+b |
|---|---:|---:|---:|---:|---:|---:|
| Crawler | 0.43 | 0.92 | 144.94 | 44.55 | 146.35 | 146.29 |
| Homepage_US | 3.82 | 6.89 | 0.00 | 290.04 | 11.96 | 10.71 |

`Homepage_US` settles it: receive is 290 ms, TTFB is 12 ms. **TTFB = Connect + Send + ServerBusy.**
Fixed in `descriptions.xsl` as well as stated correctly in the AI text.

Note the small residuals (146.35 vs 146.29): each column is an independent mean over its own
distribution, not a decomposition of the same samples. The AI text says "approximately" and warns
against reconstructing one column from the others.

### 13a. XTC run identity in the header

The file says nothing about which run it came from. When someone compares two reports, or asks an
agent to look at a specific run, there is no identifier to go on.

XTC writes five properties, already in the XML and already used by the HTML header
(`config/xsl/common/sections/header.xsl:14-18`): `com.xceptance.xtc.organization`,
`com.xceptance.xtc.project`, `com.xceptance.xtc.loadtest.run.id`,
`com.xceptance.xtc.loadtest.result.id`, `com.xceptance.xtc.loadtest.report.id`. In the sample
report these are `ACME`, `lt-2025`, `#18`, `#1`, `#1`.

All five go into the header block. Result and report id come along with the other three because
the HTML header already treats them as one group, and a run can have several results and reports.
Runs outside XTC have none of these properties, so the block is left out entirely rather than
written as empty strings.

```yaml
xtc:
  organization: "ACME"
  project: "lt-2025"
  loadTestRunId: "#18"
  resultId: "#1"
  reportId: "#1"
```

### 13b. Sections the rewrite must not forget

Rewriting the shared `timer-table` template covers transactions, actions, requests, page load
timings and custom timers. Three tables build their own markup and would silently keep the old
formatting:

- **Custom Values** — its own table (name, count, count/s, min, max, mean, stddev). Needs the same
  rounding.
- **Web Vitals** — its own table. Scores are the 75th percentile per action; CLS is unitless and
  the rest are milliseconds, so it needs the rounding rule and a description line.
- **Events** — emitted in document order like the errors were. Sorting by count descending costs
  nothing and puts what matters first, for the same reason as Decision 5.

Two report areas stay out of the AI file entirely: external data (`external.xsl`) and custom logs
(`customLogs`). Neither is in the file today and neither is added here. That is a choice, so it
belongs in the documented omissions rather than being left to look like an oversight.

### 14. Documentation is part of the deliverable

The file is handed to an LLM by a person who has to decide whether to trust the answer that comes
back. That decision needs the units, the deliberate omissions, and above all the limits — that the
statistical tables are whole-run aggregates, that run-wide rates include ramp-up, that timing means
do not sum. None of that is discoverable from the file alone.

`doc/feature-doc/ai-data.md` is therefore rewritten as a complete user-facing reference rather than
patched. It must document every section and its columns, state the units, record what is
deliberately excluded and why (Apdex, projections, scorecard, per-entity series, traces for
low-count errors), state what cannot be concluded from the file, and explain customisation. The
existing document fails all of this: it describes `ai-summary.md`, a FreeMarker `ai-summary.ftl`,
and a `transformations.18.templateFileName` key, none of which exist.

## Risks / Trade-offs

- **XSLT 3.0 coupling** — see Decision 1.
- **Message-only error grouping may be too coarse** — see Decision 5.
- **Layout change breaks anyone parsing `ai-data.md`** — it is an AI input artifact, not an
  interface, and `schemaVersion: 1` is introduced for detection.
- **The time series adds a Java change and grows `testreport.xml`** — accepted; it removes the
  largest gap in the format and decouples resolution from `chartWidth`.
- **Phase 0 adds fields to the report model** (`rawComments`, `arrivalRateMin/Max`) — additive
  only, existing fields untouched, so the HTML report and every current stylesheet are unaffected.
- **`tracesIncludedFor` = 10 is a judgment call** — exposed as an `xsl:param`.

## Migration Plan

Additive within one report generation cycle; no persisted state.

Phase 0 touches the report model (`rawComments`, `arrivalRateMin/Max`) and is additive, so it can
land on its own without changing any output. Phases 1–5 are XSL-only and each is independently
shippable; the export keeps working at every step, just with fewer of the improvements. Phase 6
(time series) is the second Java change and can ship separately from the rest.

Two ordering constraints: phase 2.2 (comments) wants phase 0.1 first or it falls back to
de-tagged HTML, and phase 4.4 (arrival rate) wants phase 0.2 first.

Phase 7a (documentation) is written last, once the layout is final.

## Verified Outcome

Measured on `reports/xlt-result-ariat-lt-2025-315-20251119-165727`, with a time series built from
that report's own chart data:

| Section | Bytes | Share |
|---|---:|---:|
| Errors | 14,615 | 30.7% |
| Requests | 13,600 | 28.6% |
| Time Series | 5,151 | 10.8% |
| Actions | 3,532 | 7.4% |
| Transactions | 2,061 | 4.3% |
| Events | 1,981 | 4.2% |
| Custom Values | 1,421 | 3.0% |
| Everything else | 5,176 | 10.9% |
| **Total** | **47,537** | |

149,707 to 47,537 bytes, a 3.1x reduction with seven sections added. Above the 35 KB estimate
because that figure only counted removals; the time series (5.2 KB) and the explanatory text
(1.5 KB) are additions the estimate did not include.

The error section fell from 119,235 to 14,615 bytes and is no longer the bulk of the file.
Requests is now the largest section, which is the right shape for a file about request performance.

The ramp-up inference the whole time series exists for works end to end. The header states
`rampUpPeriod: 900`, and the rendered series shows transaction throughput climbing 21, 22, 24 and
then holding at 24 from `elapsed 900` onward.

## Open Questions

_(none outstanding)_

Resolved during design: the interval ladder and divisor are fixed in code, anchored so a 2-hour
test lands on 1-minute buckets; all three scopes ship in one combined table rather than as separate
per-scope tables.
