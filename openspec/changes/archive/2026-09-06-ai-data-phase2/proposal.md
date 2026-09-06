## Why

`ai-data.md` ships with every load report, but measurement against a real 75-minute
production-scale run (`reports/xlt-result-ariat-lt-2025-315-20251119-165727`, 149,707 bytes)
shows the file spends its budget on the wrong things and omits the data an LLM needs most.

**80% of the file is the error section.** It is 119,235 of 149,707 bytes, of which 100,300 bytes
are stack trace — 100 entries, every one padded to the 1000-character cap. The entries are
emitted in source order, so error counts run `1 1 1 2 1 1 1 2 3 1 1 2 ... 1798 ... 1793 ...` —
the four errors that account for ~7,200 of the ~7,300 occurrences sit at positions 25, 30, 43
and 65. Thirty-five entries share one identical message and repeat the same framework frames.
All the statistical tables together are under 30 KB.

**Nothing states units.** Timings are milliseconds, periods are seconds, Web Vitals CLS is a
unitless score — the file says none of this, leaving the model to infer it on every read.

**Whole sections of the report are missing.** The entire Network page (hosts, IPs, request
methods, response codes, content types) is absent. In the sample run that hides 10,762 HTTP 404s
and 57,805 redirects: they tripped no assertion, so they never became errors and appear nowhere.

**There is no time dimension at all.** Every value is an aggregate over the whole run, ramp-up
included, so `Count/s` understates steady-state throughput and questions like "when did it
degrade" are unanswerable. The per-bucket data already exists — the dynamic chart JSON blobs
under `charts/` — but is not used.

**The explanatory text was written for a different medium.** The report's help text
(`config/xsl/loadreport/text/descriptions.xsl`) describes tabs, hover popups, charts and colour
coding that do not exist in a Markdown file, and its TTFB definition is factually wrong
(see design.md).

## What Changes

- **Cut the noise.** Group errors by message, sort by count descending, emit stack traces for the
  top N groups only and trim them to the top frames. Drop `Median` where `P50` exists, round away
  three-decimal noise on millisecond values, suppress all-zero network columns, drop the
  `Iterations` column when unused.
- **Never emit projections.** `countPerMinute`, `countPerHour` and `countPerDay` are linear
  extrapolations that read as measurements. `Count`, `Count/s` and `duration` are sufficient.
- **Identify the run.** The XTC organization, project, load test run id, result id and report id
  go into the header, so a reader can tell which run the file describes and can tell two reports
  apart.
- **State the contract.** A `schemaVersion` (2, the shipped layout being 1) and a per-field
  `units` block, plus a "Reading this
  file" section carrying the inference rules an LLM needs (no time series in the aggregates,
  rates include ramp-up, timing means do not sum).
- **Add the missing sections.** Summary, Response Codes, Content Types, Hosts, Request Methods,
  agent CPU maximum and GC figures.
- **Add a time dimension.** Downsample the existing per-bucket series into one combined
  time-series table — 60 to 120 rows whatever the test length, transactions, actions and requests
  side by side on each row, plus transaction errors per second — so ramp-up boundaries,
  degradation, error bursts, and which layer slowed down all become visible.
- **Rewrite the explanatory text for AI.** A dedicated `ai-descriptions.xsl`, deliberately forked
  from the human help text, phrased as inference rules rather than UI description.
- **Document it properly.** `doc/feature-doc/ai-data.md` becomes a full user-facing reference:
  what each section holds, what the units are, what is deliberately left out and why, what the
  file does not support concluding, and how to customise it.
- **Correct the record.** The stale `ai-summary.md` / FreeMarker naming in the capability spec and
  in `doc/feature-doc/ai-data.md`, and the wrong TTFB definition in `descriptions.xsl`.

Target was ~149,700 bytes down to ~35,000. Measured on the sample report: **47,537 bytes**, a 3.1x
reduction, while adding seven sections — Reading this file, Summary, Response Codes, Content Types,
Hosts, Request Methods and Time Series. The gap to the estimate is the parts that were added rather
than removed: the time series is 5.2 KB and the explanatory text 1.5 KB, neither of which the
original 35 KB guess accounted for.

## Capabilities

### New Capabilities
_(none)_

### Modified Capabilities
- `ai-data-export`: Content selection, formatting, error aggregation, explanatory text, and a new
  downsampled time-series section. File naming corrected to the shipped `ai-data.md`.

## Non-Goals

- **Apdex.** Deliberately excluded. It compresses a distribution into one number governed by a
  threshold nobody remembers configuring, and the file already carries P50/P95/P99 for the same
  entities.
- **Scorecard.** Requires manual setup today and produces meaningless output when unconfigured;
  in the sample report `scorecard.xml` contains only a Groovy `MissingPropertyException`. The
  insertion point is reserved (see design.md) but nothing is built now.
- **Trend and diff reports.** Unchanged, as in the original change.
- **Per-entity time series.** Only the three aggregate scopes are exported; 81 per-request series
  would be unaffordable.
- **External data and custom logs.** Neither is in the file today and neither is added here. The
  omission is recorded in the documentation so it reads as a choice rather than an oversight.
- **Slowest requests.** `slowestRequests` is not in `ai-data.md` today and is not added here.
  Adding it needs an epoch-millis field on `SlowRequestReport` and careful handling of its four
  sampling properties; design.md Decision 8 keeps the analysis for a later change.

## Impact

- **Templates**: `config/xsl/loadreport/ai-data.xsl` substantially rewritten and raised to
  XSLT 3.0. New `config/xsl/loadreport/text/ai-descriptions.xsl`. One correction to
  `config/xsl/loadreport/text/descriptions.xsl`.
- **Code**: A downsampled time series is added to `testreport.xml` by the report providers so the
  AI export does not depend on chart rendering or on `chartWidth`. Raw values are added alongside
  existing rendered ones where the AI export needs them — `rawComments` on `ConfigurationReport`
  and numeric `arrivalRateMin` / `arrivalRateMax` on `LoadProfileConfigurationReport`. Rendered
  forms are untouched, so existing stylesheets and report output are unaffected.
- **Processor**: `ai-data.xsl` becomes the only stylesheet requiring XSLT 3.0. Saxon-HE 12.5 is a
  direct dependency, ships in `lib/`, and is the resolved `TransformerFactory` — verified, see
  design.md.
- **Docs**: `doc/feature-doc/ai-data.md` rewritten as a complete user-facing reference — every
  section explained, units stated, deliberate omissions and their reasons recorded, and the limits
  of what the file supports concluding made explicit.
- **Breaking changes**: The layout and column set of `ai-data.md` change. The file is an AI input
  artifact, not a parsed interface; `schemaVersion: 2` is introduced so consumers can tell this
  layout from the one that shipped before it, which is treated as version 1.
