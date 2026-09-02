# AI Data Export

Every load report includes `ai-data.md`, a compact version of the report's statistics meant to be
read by a language model rather than by a person. Paste it into an LLM, or hand the path to an
agent, and ask questions about the run.

## Quick start

After generating a load report you will find `ai-data.md` in the output directory next to
`index.html` and `testreport.xml`. The report's navigation bar links to it as **AI Data**.

```
Here is my load test data:

[paste contents of ai-data.md]

Which transactions are slowest, and is the slowness in the server or on the client?
```

The file is self-contained. It states its own units and its own limits, so the model does not have
to guess and you do not have to explain the format first.

## Why a separate file

`testreport.xml` holds the same numbers, but it is built for machines that parse it. It is verbose,
it repeats element names on every value, and it contains a great deal that is irrelevant to
analysis. Feeding it to a model costs several times more tokens and gives worse answers, because
the interesting numbers are diluted among the rest.

`ai-data.md` uses YAML for the metadata and Markdown tables for the statistics. Tables are the
densest reliable format for rows of numbers: the column header stays close to the values, and one
row per entity maps directly onto "compare these".

## What is in the file

Sections appear only when the run produced data for them.

### Header

A single YAML block:

```yaml
schemaVersion: 1
units:
  responseTimes: ms      # min, max, mean, dev, all P* columns, all network timings
  periods: s             # duration, rampUp, measurement, shutdown
  thinkTime: ms
  sizes: bytes
  rates: per second
  webVitals: "CLS unitless score; FCP/LCP/INP/TTFB ms"
product: "Xceptance LoadTest 10.0.0"
project: "ACME"
startTime: "2025-11-19 17:57:27 CET"
endTime: "2025-11-19 19:12:27 CET"
duration: 4500
rampUpPeriod: 900
hits: 3137711
bytesSent: 4231268620
bytesReceived: 26559661891
xtc:
  organization: "ACME"
  project: "lt-2025"
  loadTestRunId: "#18"
  resultId: "#1"
  reportId: "#1"
```

`schemaVersion` lets a consumer tell the layout apart from later ones. The `units` block matters
more than it looks: most values are milliseconds, but periods are seconds and CLS is a unitless
score, so a single blanket statement would be wrong.

`rampUpPeriod` is repeated here from the load profile so that ramp-up contamination of the run-wide
rates is visible right where those rates are.

The `xtc` block identifies the run in XTC. It is omitted entirely for runs that did not come from
XTC, rather than written out as empty strings.

### Reading this file

A short list of rules that constrain what may be concluded from the data. See
[What you cannot conclude](#what-you-cannot-conclude-from-this-file) below.

### Comments

Test comments, one blockquoted line each. Markup is stripped, and every line is prefixed with `>`
so that a comment containing something like `# Errors` cannot forge a section heading and split the
document.

### Summary

Totals across all transactions, actions and requests: count, count per second, errors, error
percentage.

### Time Series

The only section with a time dimension. See [Time series](#time-series-1) below.

### Load Profile

Per test case: users, arrival rate range, measurement period, ramp-up, shutdown. The `Iterations`
column appears only when iteration mode is in use — a zero there means "not configured", not "none
completed", and a model reads it literally.

### Transactions, Actions, Requests, Page Load Timings, Custom Timers

One row per entity, with count, count per second, errors, error percentage, min, max, mean,
deviation, and every percentile the report is configured for.

`Median` appears only when no P50 percentile is configured. When P50 is present the two are the
same number in every row, so one of them is dropped.

Requests additionally carry socket-level network timing means — DNS, connect, send, server busy,
receive, time to first bytes — plus mean bytes sent and received. A timing column that is zero for
every request is left out.

### Custom Values

Per custom value: count, count per second, min, max, mean, standard deviation.

### Response Codes, Content Types, Hosts, Request Methods

Counts and share of total hits. These cover **every** request, including those that passed
validation, which makes them the only place where a run's 404s or redirect overhead show up. A
response outside 2xx that tripped no test assertion never became an error and appears nowhere else.

### Errors

See [Errors](#errors-1) below.

### Events

Test case, event name, count. Ordered by count, loudest first.

### Agents

Per agent: transactions, errors, error percentage, CPU mean and max, full and minor GC counts and
times.

CPU **max** is there alongside the mean because a mean of 33% hides an agent that was pegged during
peak. If the load generators were saturated, client-side queuing inflated every measured time in
the file, and the response times should be read as upper bounds rather than as application
behaviour.

### Web Vitals

Per action, the 75th percentile score and rating for CLS, FCP, LCP, INP and TTFB.

## Time series

Everything else in the file is a single aggregate over the whole run. This section is what makes
"when did it get slow" and "did the errors arrive in a burst" answerable.

```
| Elapsed | Time | Txn Mean | Txn /s | Txn Err/s | Act Mean | Req Mean | Req /s |
| ---: | --- | ---: | ---: | ---: | ---: | ---: | ---: |
|  900 | 17:12:00 | 41683 | 22.5 | 0.02 | 512 | 98 | 201.4 |
```

`Elapsed` is seconds since the run started, so comparing it against `rampUpPeriod` needs no
arithmetic. `Time` is the wall clock, for lining a spike up against server logs or APM data.

All three scopes share one table on purpose. Read across a row and the three means say *which
layer* slowed down:

| What rises | Where the problem is |
|---|---|
| Req Mean | The server |
| Act Mean, while Req Mean stays flat | Client-side work — JavaScript, waits, test code |
| Txn Mean, while Act Mean stays flat | Think time or the test's own processing |

### The bucket interval is derived, not fixed

The interval varies with test duration and is stated in the file:

```yaml
interval: 60
buckets: 75
sourceResolution: 5
```

A fixed one-minute interval fails at both ends. A five minute test would produce five rows and hide
its ramp-up completely. A twenty-four hour test would produce 1,440 rows — larger than everything
else in the file put together — and could not be produced anyway, because the underlying data is
collected at roughly 96 second resolution over that span.

So the row count is the target and the interval follows from it, rounded up to a value a reader
recognises. The divisor is set so a two hour test lands on one minute buckets:

| Test duration | Interval | Rows |
|---|---|---:|
| 5 min | 5 s | 60 |
| 15 min | 10 s | 90 |
| 30 min | 15 s | 120 |
| 1 h | 30 s | 120 |
| 2 h | 1 min | 120 |
| 4 h | 2 min | 120 |
| 8 h | 5 min | 96 |
| 24 h | 15 min | 96 |

The interval is never finer than `sourceResolution`, since that would claim precision the collected
data does not have.

This is fixed in code and has no configuration property. A property would let you ask for a
resolution the data cannot deliver, or for a series that outweighs everything else in the file, and
neither would report an error — it would just quietly get expensive.

## Errors

Errors are grouped by message, and the groups are ordered by total count. An overview table comes
first:

```
| # | Message | Count | Test Cases | Actions |
| 1 | No element found for: {CSS=#dwfrm_shipping_...} | 3594 | 2 | 1 |
| 2 | Unexpected HTTP status code. expected:<200> but was:<403> | 46 | 8 | 12 |
```

Then each group in the same order, with a table of the test cases and actions that produced it.

A stack trace is included **only for the largest groups**, and only its leading frames, with a
marker for the rest:

```
java.lang.AssertionError: No element found for: ...
	at org.junit.Assert.fail(Assert.java:89)
	at com.xceptance.loadtest.api.hpu.LookUpResult.single(LookUpResult.java:129)
	... 13 more frames
```

This is deliberate. Untrimmed, the error section was 80% of the file — one thousand-character trace
per entry, almost all framework frames that diagnose nothing, with the errors that mattered buried
among singletons.

The section opens with counters so the shape is clear before any detail:

```yaml
totalErrors: 7306
distinctEntries: 100
distinctMessages: 19
tracesIncludedFor: 10
```

## What you cannot conclude from this file

The file states these itself, in the *Reading this file* section, but they are worth knowing before
you trust an answer:

- **The statistical tables are aggregates over the whole run.** Trends, spikes and degradation over
  time cannot be derived from them. Only the Time Series section carries time.
- **Rates include ramp-up.** `Count/s` covers the full duration, ramp-up included, so it
  understates steady-state throughput whenever ramp-up is a large share of the run. Use the time
  series to find where steady state began.
- **The network timing columns do not sum.** Each is an independent mean over its own distribution,
  not a decomposition of the same samples. TTFB is approximately connect + send + server busy, and
  no column should be reconstructed from the others.
- **Percentile columns are response times**, not counts.
- **Request errors exclude content validation.** They count failures during loading — 5xx,
  timeouts, connection resets. A request row with zero errors can still have served wrong content;
  that shows up as an action or transaction error instead.

## What is deliberately left out

| Not included | Why |
|---|---|
| Apdex | It compresses a distribution into one number governed by a threshold nobody remembers configuring. The file already carries P50/P95/P99 for the same entities. |
| Per-minute, per-hour, per-day counts | Linear extrapolations that read as measurements in a table. `Count`, `Count/s` and `duration` let a model compute any rate with the extrapolation base in view. |
| Scorecard | Needs manual setup today and produces meaningless output when unconfigured. |
| Slowest requests | The list is filtered by a minimum runtime and capped per request name, so it is neither a ranking nor a sample. Presenting it without that context would mislead. |
| Per-entity time series | Only the three aggregate scopes are exported. One series per request would be enormous. |
| Full stack traces for small errors | See [Errors](#errors-1). |
| External data, custom logs | Not currently exported. |
| Chart data, result browser data, per-request logs | Not statistics. |

## Configuration

### Turning it off

`ai-data.md` is generated as report transformation #18. To stop generating it, comment out both
lines in `config/reportgenerator.properties`:

```properties
# com.xceptance.xlt.reportgenerator.transformations.18.styleSheetFileName = ai-data.xsl
# com.xceptance.xlt.reportgenerator.transformations.18.outputFileName = ai-data.md
```

The **AI Data** navigation link will still appear but lead nowhere. To remove it too, edit
`config/xsl/loadreport/sections/navigation.xsl`.

### Changing what it contains

The file is produced by `config/xsl/loadreport/ai-data.xsl`, with the explanatory prose in
`config/xsl/loadreport/text/ai-descriptions.xsl`. Both are plain stylesheets — edit them and
regenerate the report. **No compilation or build step is needed.**

Two values at the top of `ai-data.xsl` are worth knowing about:

```xml
<xsl:param name="tracesIncludedFor" select="10" />
<xsl:param name="traceFrames" select="8" />
```

`tracesIncludedFor` is how many of the largest error groups get a stack trace; `traceFrames` is how
many leading frames each of those keeps. Raise them when you are chasing a specific failure and
want more of the stack, and remember that traces dominate the file size.

Beyond that you can add sections, remove ones you do not need, or change which columns appear.

### Requirements

`ai-data.xsl` is **XSLT 3.0**, unlike the other report stylesheets. It uses grouping, functions and
regular expressions that XSLT 1.0 does not have. XLT ships Saxon-HE and resolves it as the XSLT
processor, so this works out of the box. If you replace the `TransformerFactory` with an XSLT 1.0
processor, this stylesheet will not run.

## Scope

Generated for **load reports** only. Trend reports and diff reports use a different data model and
are not covered.

The file contains aggregated statistics and the coarse time series described above. It does not
contain per-request logs, result browser data, or chart images.

## Size

Typically tens of kilobytes. A run with many distinct request types or many distinct error messages
produces a larger file; the error trace limits are the main lever if you need it smaller.

## Example prompts

**Where is the bottleneck**

> Using the Requests table and the Time Series, identify the top 3 bottlenecks. For each, say
> whether the evidence points at the server, the network, or client-side work, and explain which
> columns you used.

**Steady state only**

> Use the Time Series to find where ramp-up ended, then recompute the effective throughput and mean
> transaction time for the steady-state period only. Compare that against the run-wide figures.

**SLA check**

> Given an SLA of P95 under 2 seconds per request and an error rate below 0.1%, list every
> non-compliant entry with its actual values.

**Error triage**

> Summarise the error groups by likely root cause. Note which test cases and actions each affects,
> and check the Response Codes section for failures that never became errors.

**Was the test valid**

> Check the Agents section and tell me whether the load generators were healthy enough for these
> measurements to reflect the application rather than the test infrastructure.

**Comparing two runs**

> Here is the baseline: [paste ai-data.md]
>
> And here is the run after our change: [paste ai-data.md]
>
> Compare them. Use the xtc block in each header to keep them straight, and call out anything where
> the two runs are not comparable.
