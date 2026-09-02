<?xml version="1.0"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!--
    Explanatory text for ai-data.md.

    This is a deliberate fork of descriptions.xsl, not a copy. That file describes an HTML
    page - tabs, hover popups, chart sections, colour coded ratings, the Runtime
    Segmentation column. None of it exists in a Markdown file, so reusing it would spend
    tokens on things the reader cannot see and point at controls that are not there.

    Written as inference rules rather than definitions. A model reading this needs to know
    what it may NOT conclude at least as much as what a column means, so most paragraphs
    end with the mistake they are there to prevent.

    Everything sits inside xsl:text, and the lines start at column 0, because the output
    method is text and any indentation here would end up in the file.
    -->

    <xsl:template name="ai-reading-rules">
<xsl:text>
## Reading this file

- The statistical tables are aggregates over the whole run. They contain no time series,
  so trends, spikes and degradation over time cannot be derived from them. Use the Time
  Series section for anything about change over time.
- Rates such as Count/s cover the full duration including ramp-up, so they understate
  steady-state throughput whenever ramp-up is a large share of the run. `rampUpPeriod` is
  in the header.
- The network timing columns are independent means over their own distributions. They do
  not sum exactly, and none of them should be reconstructed from the others.
- Percentile columns are response times, not counts.
- A section is absent when the run produced no data for it.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-transactions">
<xsl:text>
A transaction is one completed execution of a test case, containing one or more actions.
Its runtime includes all action runtimes, the think times between actions, and the test
code's own processing time. Think time is deliberate and often dominates, so a slow
transaction does not imply a slow application. Heavily randomized test paths make
transaction runtimes vary by design; a large standard deviation here is frequently
expected rather than a finding.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-actions">
<xsl:text>
An action is one step within a transaction, typically a page view or a single user
interaction, made up of one or more requests. Its runtime covers preparing, sending,
waiting for and receiving the request data, JavaScript execution when enabled, and any
explicit waits or pauses coded into the test. Action errors count both failures during
page loading and failures of the test's own validation of the loaded page.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-requests">
<xsl:text>
A request is a single HTTP operation. This section reflects server and network behaviour
most directly. Request names derive from the action name and may have been merged by
filter and transformation rules, so one row can aggregate many distinct URLs.

Request runtime is the network time plus the client-side time to process headers and
protocol data and hand the payload to the application.

Request errors count only failures during loading: 5xx responses, timeouts, connection
resets. They do NOT include validation failures on received content, which appear as
action or transaction errors instead. A request row with zero errors can still have
served wrong content.

Network timings are socket-level means:

- Connect: establishing the connection. Near zero under keep-alive, so a low value means
  connections were reused, not that connecting is fast.
- Send: sending the request. Usually near zero.
- ServerBusy: last byte sent to first byte received. This is server processing time and
  is the primary server-side indicator.
- Receive: first to last byte received. Reflects payload size and bandwidth.
- TTFB: approximately Connect + Send + ServerBusy. Approximately, because each column is
  an independent mean.

BytesSent and BytesRecv are measured on the wire, so compressed payloads are counted
compressed, not as expanded by the parser.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-errors">
<xsl:text>
Errors are grouped by message and ordered by descending count. Each group lists the test
cases and actions that produced it. Stack traces are included for the largest groups only
and are trimmed to the leading frames; deep framework frames carry no diagnostic value.

Response codes outside 2xx that did not trip a test assertion never became errors and do
not appear here. Check the Response Codes section for those.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-time-series">
<xsl:text>
How the run behaved over time. This is the only section with a time dimension; everything
else is a single aggregate over the whole test.

Elapsed is seconds since the start of the run, so comparing it against `rampUpPeriod` in
the header shows where ramp-up ended and steady state began. Time is the wall clock, for
lining a spike up against server logs.

The three scopes sit on the same row on purpose. Requests rising points at the server.
Actions rising while requests stay flat points at client-side work - JavaScript, waits, or
the test code. Transactions rising while actions stay flat points at think time or the
test's own processing.

The bucket interval is derived from the test duration and stated above, so it varies
between reports. It is never finer than the resolution of the collected data.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-network">
<xsl:text>
Counts cover every request, including those that passed validation. A response code
outside 2xx that did not trip a test assertion appears here but not in the Errors section,
so this is the only place a run's 404s or redirect overhead become visible.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-agents">
<xsl:text>
Load generator health. If agent CPU approaches saturation, or full GC time is a
significant share of the run, client-side queuing inflates every measured time in this
file and the response times should be read as upper bounds rather than as application
behaviour.
</xsl:text>
    </xsl:template>

    <xsl:template name="ai-desc-web-vitals">
<xsl:text>
Each score is the 75th percentile across all measurements for that action. CLS is a
unitless layout shift score; FCP, LCP, INP and TTFB are milliseconds. Ratings follow the
Google Web Vitals thresholds.
</xsl:text>
    </xsl:template>

</xsl:stylesheet>
