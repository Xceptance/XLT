<?xml version="1.0"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ai="urn:xlt:ai-data"
                exclude-result-prefixes="xs ai">
    <xsl:output method="text" encoding="UTF-8" />
    <xsl:strip-space elements="*" />

    <xsl:include href="text/ai-descriptions.xsl" />

    <!--
    Generates ai-data.md - a YAML+Markdown hybrid meant to be read by a language model
    rather than a person.

    REQUIRES XSLT 3.0. Unlike every other stylesheet here this one uses xsl:function,
    for-each-group, tokenize, replace and "every ... satisfies". XLT resolves Saxon-HE as
    its TransformerFactory, which supports all of it. On a JDK-default Xalan (XSLT 1.0)
    this file does not run. AiDataStylesheetTest asserts the processor so a swap fails
    loudly instead of quietly emitting nonsense.

    All literal output goes through xsl:text. Bare literals in the template body would
    carry their own indentation into the output, which is where the 106 stray whitespace
    lines in the previous version came from.
    -->

    <!-- how many error groups get a stack trace -->
    <xsl:param name="tracesIncludedFor" select="10" />

    <!-- how many leading frames of a stack trace to keep -->
    <xsl:param name="traceFrames" select="8" />

    <!-- ============================================================ helpers -->

    <!--
    A response time, rounded to whole milliseconds. The source carries three decimals on
    a measurement whose resolution is one millisecond, so they are noise.
    -->
    <xsl:function name="ai:ms" as="xs:string">
        <xsl:param name="value" />
        <xsl:sequence select="if (string($value) = '') then '' else format-number(number($value), '0')" />
    </xsl:function>

    <!--
    A rate or a percentage. Two decimals at most, no trailing zeros, so 0.000 prints as 0
    and 0.037 as 0.04.
    -->
    <xsl:function name="ai:num" as="xs:string">
        <xsl:param name="value" />
        <xsl:sequence select="if (string($value) = '') then '' else format-number(number($value), '0.##')" />
    </xsl:function>

    <!--
    A count. Plain integer, never with grouping separators - a comma is a decimal
    separator in half of Europe and ambiguous inside a pipe table.
    -->
    <xsl:function name="ai:int" as="xs:string">
        <xsl:param name="value" />
        <xsl:sequence select="if (string($value) = '') then '' else format-number(number(replace(string($value), ',', '')), '0')" />
    </xsl:function>

    <!-- A share of a total, as a percentage with two decimals. -->
    <xsl:function name="ai:share" as="xs:string">
        <xsl:param name="value" />
        <xsl:param name="total" />
        <xsl:sequence select="if (number($total) = 0) then '' else format-number(number($value) div number($total) * 100, '0.##')" />
    </xsl:function>

    <!-- One table row. -->
    <xsl:function name="ai:row" as="xs:string">
        <xsl:param name="cells" as="xs:string*" />
        <xsl:sequence select="concat('| ', string-join($cells, ' | '), ' |&#10;')" />
    </xsl:function>

    <!--
    The separator row. The first column holds a name and stays left aligned, everything
    after it is a number and gets right aligned.
    -->
    <xsl:function name="ai:separator" as="xs:string">
        <xsl:param name="cells" as="xs:string*" />
        <xsl:sequence select="concat('| --- | ', string-join(for $i in 2 to count($cells) return '---:', ' | '), ' |&#10;')" />
    </xsl:function>

    <!-- A table header plus its separator row. -->
    <xsl:function name="ai:header" as="xs:string">
        <xsl:param name="cells" as="xs:string*" />
        <xsl:sequence select="concat(ai:row($cells), ai:separator($cells))" />
    </xsl:function>

    <!--
    Cell text that came from the test run. A pipe would end the cell early and shift every
    column after it, and a newline would end the row.
    -->
    <xsl:function name="ai:cell" as="xs:string">
        <xsl:param name="value" />
        <xsl:sequence select="normalize-space(replace(string($value), '\|', '\\|'))" />
    </xsl:function>

    <!-- True when the given element is zero, absent or empty for every row. -->
    <xsl:function name="ai:all-zero" as="xs:boolean">
        <xsl:param name="values" as="node()*" />
        <xsl:sequence select="every $v in $values satisfies (string($v) = '' or number($v) = 0)" />
    </xsl:function>

    <!-- ============================================================ document -->

    <xsl:template match="/testreport">
        <xsl:text># XLT Load Test Report - AI Data&#10;</xsl:text>

        <xsl:call-template name="header" />
        <xsl:call-template name="ai-reading-rules" />
        <xsl:call-template name="comments" />
        <xsl:call-template name="summary" />
        <xsl:call-template name="time-series" />
        <xsl:call-template name="load-profile" />
        <xsl:call-template name="timer-section">
            <xsl:with-param name="title" select="'Transactions'" />
            <xsl:with-param name="elements" select="transactions/*" />
            <xsl:with-param name="description" select="'transactions'" />
        </xsl:call-template>
        <xsl:call-template name="timer-section">
            <xsl:with-param name="title" select="'Actions'" />
            <xsl:with-param name="elements" select="actions/*" />
            <xsl:with-param name="description" select="'actions'" />
        </xsl:call-template>
        <xsl:call-template name="requests" />
        <xsl:call-template name="timer-section">
            <xsl:with-param name="title" select="'Page Load Timings'" />
            <xsl:with-param name="elements" select="pageLoadTimings/*" />
        </xsl:call-template>
        <xsl:call-template name="timer-section">
            <xsl:with-param name="title" select="'Custom Timers'" />
            <xsl:with-param name="elements" select="customTimers/*" />
        </xsl:call-template>
        <xsl:call-template name="custom-values" />
        <xsl:call-template name="response-codes" />
        <xsl:call-template name="content-types" />
        <xsl:call-template name="hosts" />
        <xsl:call-template name="request-methods" />
        <xsl:call-template name="errors" />
        <xsl:call-template name="events" />
        <xsl:call-template name="agents" />
        <xsl:call-template name="web-vitals" />
    </xsl:template>

    <!-- ============================================================ header -->

    <!--
    One YAML block instead of the four separate fragments this file used to open with.
    The units mapping is the important part: most values are milliseconds but not all,
    and without it a model has to infer the unit of every column on every read.
    -->
    <xsl:template name="header">
        <xsl:variable name="props" select="configuration/properties" />

        <xsl:text>&#10;```yaml&#10;</xsl:text>
        <xsl:text>schemaVersion: 1&#10;</xsl:text>

        <xsl:text>units:&#10;</xsl:text>
        <xsl:text>  responseTimes: ms      # min, max, mean, dev, all P* columns, all network timings&#10;</xsl:text>
        <xsl:text>  periods: s             # duration, rampUp, measurement, shutdown&#10;</xsl:text>
        <xsl:text>  thinkTime: ms&#10;</xsl:text>
        <xsl:text>  sizes: bytes&#10;</xsl:text>
        <xsl:text>  rates: per second&#10;</xsl:text>
        <xsl:text>  webVitals: "CLS unitless score; FCP/LCP/INP/TTFB ms"&#10;</xsl:text>

        <xsl:if test="configuration/version">
            <xsl:text>product: "</xsl:text>
            <xsl:value-of select="configuration/version/productName" />
            <xsl:text> </xsl:text>
            <xsl:value-of select="configuration/version/version" />
            <xsl:text>"&#10;</xsl:text>
        </xsl:if>

        <xsl:if test="configuration/projectName != ''">
            <xsl:text>project: "</xsl:text>
            <xsl:value-of select="configuration/projectName" />
            <xsl:text>"&#10;</xsl:text>
        </xsl:if>

        <xsl:if test="general">
            <xsl:text>startTime: "</xsl:text>
            <xsl:value-of select="general/startTime" />
            <xsl:text>"&#10;endTime: "</xsl:text>
            <xsl:value-of select="general/endTime" />
            <xsl:text>"&#10;duration: </xsl:text>
            <xsl:value-of select="ai:int(general/duration)" />
            <xsl:text>&#10;</xsl:text>

            <!--
            Carried up from the load profile so that ramp-up contamination of the
            run-wide rates is visible without cross-referencing that table.
            -->
            <xsl:if test="configuration/loadProfile/testCase">
                <xsl:text>rampUpPeriod: </xsl:text>
                <xsl:value-of select="ai:int(max(configuration/loadProfile/testCase/rampUpPeriod/number(.)))" />
                <xsl:text>&#10;</xsl:text>
            </xsl:if>

            <xsl:text>hits: </xsl:text>
            <xsl:value-of select="ai:int(general/hits)" />
            <xsl:text>&#10;bytesSent: </xsl:text>
            <xsl:value-of select="ai:int(general/bytesSent)" />
            <xsl:text>&#10;bytesReceived: </xsl:text>
            <xsl:value-of select="ai:int(general/bytesReceived)" />
            <xsl:text>&#10;</xsl:text>
        </xsl:if>

        <!--
        Identifies the run in XTC. Omitted whole for runs that did not come from XTC,
        rather than written out as empty strings.
        -->
        <xsl:if test="$props/property[@name = 'com.xceptance.xtc.organization']/@value != ''">
            <xsl:text>xtc:&#10;  organization: "</xsl:text>
            <xsl:value-of select="$props/property[@name = 'com.xceptance.xtc.organization']/@value" />
            <xsl:text>"&#10;  project: "</xsl:text>
            <xsl:value-of select="$props/property[@name = 'com.xceptance.xtc.project']/@value" />
            <xsl:text>"&#10;  loadTestRunId: "</xsl:text>
            <xsl:value-of select="$props/property[@name = 'com.xceptance.xtc.loadtest.run.id']/@value" />
            <xsl:text>"&#10;  resultId: "</xsl:text>
            <xsl:value-of select="$props/property[@name = 'com.xceptance.xtc.loadtest.result.id']/@value" />
            <xsl:text>"&#10;  reportId: "</xsl:text>
            <xsl:value-of select="$props/property[@name = 'com.xceptance.xtc.loadtest.report.id']/@value" />
            <xsl:text>"&#10;</xsl:text>
        </xsl:if>

        <xsl:text>```&#10;</xsl:text>
    </xsl:template>

    <!-- ============================================================ comments -->

    <!--
    Comments are free text written by whoever configured the run, and they land in an LLM
    prompt. A comment containing "# Errors" would forge a section heading and split the
    document, so every line is blockquoted.

    rawComments holds the text as authored; comments holds the rendered HTML. Prefer the
    raw form, fall back to the rendered one for reports generated before rawComments
    existed. Tags are stripped either way, because a comment without the ::markdown::
    marker may legitimately be raw HTML.
    -->
    <xsl:template name="comments">
        <xsl:variable name="source"
                      select="if (configuration/rawComments/string) then configuration/rawComments/string
                              else configuration/comments/string" />

        <xsl:if test="$source">
            <xsl:text>&#10;## Comments&#10;&#10;</xsl:text>
            <xsl:for-each select="$source">
                <xsl:variable name="plain" select="normalize-space(replace(string(.), '&lt;[^&gt;]*&gt;', ' '))" />
                <xsl:if test="$plain != ''">
                    <xsl:text>&gt; </xsl:text>
                    <xsl:value-of select="$plain" />
                    <xsl:text>&#10;</xsl:text>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ summary -->

    <!--
    The top level picture, five rows. Nothing else in the file states the overall counts
    and error rates without the reader adding up a table first.
    -->
    <xsl:template name="summary">
        <xsl:if test="summary/*[count]">
            <xsl:variable name="headers" as="xs:string*"
                          select="'Scope', 'Count', 'Count/s', 'Errors', 'Error%'" />

            <xsl:text>&#10;## Summary&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="summary/*[count]">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(name)" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:num(countPerSecond)" />
                    <xsl:sequence select="ai:int(errors)" />
                    <xsl:sequence select="ai:num(errorPercentage)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ time series -->

    <!--
    One table for all three scopes rather than one table each. The elapsed and wall clock
    columns would otherwise be repeated three times, and side by side on a row the three
    means say which layer slowed down.
    -->
    <xsl:template name="time-series">
        <xsl:if test="summary/timeSeries/rows/row">
            <xsl:variable name="series" select="summary/timeSeries" />

            <xsl:text>&#10;## Time Series&#10;</xsl:text>
            <xsl:call-template name="ai-desc-time-series" />

            <xsl:text>&#10;```yaml&#10;interval: </xsl:text>
            <xsl:value-of select="ai:int($series/interval)" />
            <xsl:text>&#10;buckets: </xsl:text>
            <xsl:value-of select="ai:int($series/buckets)" />
            <xsl:text>&#10;sourceResolution: </xsl:text>
            <xsl:value-of select="ai:int($series/sourceResolution)" />
            <xsl:text>&#10;```&#10;&#10;</xsl:text>

            <xsl:value-of select="ai:header(('Elapsed', 'Time', 'Txn Mean', 'Txn /s', 'Txn Err/s',
                                             'Act Mean', 'Req Mean', 'Req /s'))" />

            <xsl:for-each select="$series/rows/row">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="ai:int(elapsed)" />
                    <!--
                    The rendered timestamp carries the date and zone too, which are already in the
                    header. Seconds stay because a short test gets sub-minute buckets, and without
                    them consecutive rows would show the same clock time.
                    -->
                    <xsl:sequence select="substring(string(time), 12, 8)" />
                    <xsl:sequence select="ai:ms(transactionMean)" />
                    <xsl:sequence select="ai:num(transactionCountPerSecond)" />
                    <xsl:sequence select="ai:num(transactionErrorsPerSecond)" />
                    <xsl:sequence select="ai:ms(actionMean)" />
                    <xsl:sequence select="ai:ms(requestMean)" />
                    <xsl:sequence select="ai:num(requestCountPerSecond)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ network distributions -->

    <!--
    None of these reached the AI file before, which meant a run could serve ten thousand
    404s and thousands of redirects with no trace of it anywhere: nothing asserted on
    them, so they never became errors.
    -->
    <xsl:template name="response-codes">
        <xsl:if test="responseCodes/responseCode">
            <xsl:variable name="hits" select="general/hits" />
            <xsl:variable name="headers" as="xs:string*" select="'Code', 'Status', 'Count', 'Share%'" />

            <xsl:text>&#10;## Response Codes&#10;</xsl:text>
            <xsl:call-template name="ai-desc-network" />
            <xsl:text>&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="responseCodes/responseCode">
                <xsl:sort select="number(count)" order="descending" />
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="ai:int(code)" />
                    <xsl:sequence select="string(statusText)" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:share(count, $hits)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="content-types">
        <xsl:if test="contentTypes/contentType">
            <xsl:variable name="hits" select="general/hits" />
            <xsl:variable name="headers" as="xs:string*" select="'Content Type', 'Count', 'Share%'" />

            <xsl:text>&#10;## Content Types&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="contentTypes/contentType">
                <xsl:sort select="number(count)" order="descending" />
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="if (contentType != '') then string(contentType) else '(none)'" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:share(count, $hits)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="hosts">
        <xsl:if test="hosts/host">
            <xsl:variable name="hits" select="general/hits" />
            <xsl:variable name="headers" as="xs:string*" select="'Host', 'Count', 'Share%'" />

            <xsl:text>&#10;## Hosts&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="hosts/host">
                <xsl:sort select="number(count)" order="descending" />
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(name)" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:share(count, $hits)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="request-methods">
        <xsl:if test="requestMethods/requestMethod">
            <xsl:variable name="hits" select="general/hits" />
            <xsl:variable name="headers" as="xs:string*" select="'Method', 'Count', 'Share%'" />

            <xsl:text>&#10;## Request Methods&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="requestMethods/requestMethod">
                <xsl:sort select="number(count)" order="descending" />
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(method)" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:share(count, $hits)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ load profile -->

    <xsl:template name="load-profile">
        <xsl:if test="configuration/loadProfile/testCase">
            <xsl:variable name="cases" select="configuration/loadProfile/testCase" />

            <!--
            Iterations is zero for every row unless iteration mode is in use, and zero
            there does not mean "none completed", it means "not configured". A model
            reads it literally, so the column only appears when it carries something.
            -->
            <xsl:variable name="showIterations" select="not(ai:all-zero($cases/numberOfIterations))" />

            <xsl:variable name="headers" as="xs:string*">
                <xsl:sequence select="'Test Case', 'Users', 'Arrival Rate Min', 'Arrival Rate Max'" />
                <xsl:if test="$showIterations">
                    <xsl:sequence select="'Iterations'" />
                </xsl:if>
                <xsl:sequence select="'Measurement', 'Ramp-Up', 'Shutdown'" />
            </xsl:variable>

            <xsl:text>&#10;## Load Profile&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="$cases">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="if (userName != '') then string(userName) else string(testCaseClassName)" />
                    <xsl:sequence select="ai:int(numberOfUsersMax)" />
                    <xsl:sequence select="ai:int(arrivalRateMin)" />
                    <xsl:sequence select="ai:int(arrivalRateMax)" />
                    <xsl:if test="$showIterations">
                        <xsl:sequence select="ai:int(numberOfIterations)" />
                    </xsl:if>
                    <xsl:sequence select="ai:int(measurementPeriod)" />
                    <xsl:sequence select="ai:int(rampUpPeriod)" />
                    <xsl:sequence select="ai:int(shutdownPeriod)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ timer tables -->

    <xsl:template name="timer-section">
        <xsl:param name="title" />
        <xsl:param name="elements" />
        <xsl:param name="description" select="''" />

        <xsl:if test="$elements">
            <xsl:text>&#10;## </xsl:text>
            <xsl:value-of select="$title" />
            <xsl:text>&#10;</xsl:text>
            <xsl:if test="$description = 'transactions'">
                <xsl:call-template name="ai-desc-transactions" />
            </xsl:if>
            <xsl:if test="$description = 'actions'">
                <xsl:call-template name="ai-desc-actions" />
            </xsl:if>
            <xsl:text>&#10;</xsl:text>
            <xsl:call-template name="timer-table">
                <xsl:with-param name="elements" select="$elements" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="timer-table">
        <xsl:param name="elements" />

        <xsl:variable name="percentiles" select="$elements[1]/percentiles/*" />

        <!-- Median duplicates P50 in every row, so it only appears when P50 does not. -->
        <xsl:variable name="showMedian" select="empty($elements[1]/percentiles/p50)" />

        <xsl:variable name="headers" as="xs:string*">
            <xsl:sequence select="'Name', 'Count', 'Count/s', 'Errors', 'Error%', 'Min', 'Max', 'Mean'" />
            <xsl:if test="$showMedian">
                <xsl:sequence select="'Median'" />
            </xsl:if>
            <xsl:sequence select="'Dev'" />
            <xsl:for-each select="$percentiles">
                <xsl:sequence select="concat('P', substring-after(name(), 'p'))" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:value-of select="ai:header($headers)" />

        <xsl:for-each select="$elements">
            <xsl:variable name="cells" as="xs:string*">
                <xsl:sequence select="string(name)" />
                <xsl:sequence select="ai:int(count)" />
                <xsl:sequence select="ai:num(countPerSecond)" />
                <xsl:sequence select="ai:int(errors)" />
                <xsl:sequence select="ai:num(errorPercentage)" />
                <xsl:sequence select="ai:ms(min)" />
                <xsl:sequence select="ai:ms(max)" />
                <xsl:sequence select="ai:ms(mean)" />
                <xsl:if test="$showMedian">
                    <xsl:sequence select="ai:ms(median)" />
                </xsl:if>
                <xsl:sequence select="ai:ms(deviation)" />
                <xsl:for-each select="percentiles/*">
                    <xsl:sequence select="ai:ms(.)" />
                </xsl:for-each>
            </xsl:variable>
            <xsl:value-of select="ai:row($cells)" />
        </xsl:for-each>
    </xsl:template>

    <!-- ============================================================ requests -->

    <xsl:template name="requests">
        <xsl:if test="requests/*">
            <xsl:variable name="rows" select="requests/*" />
            <xsl:variable name="percentiles" select="$rows[1]/percentiles/*" />
            <xsl:variable name="showMedian" select="empty($rows[1]/percentiles/p50)" />

            <!--
            A timing column that is zero for every request carries nothing. DNS and
            connect are routinely zero under keep-alive, and printing "0" 80 times costs
            tokens and invites the model to reason about it.
            -->
            <xsl:variable name="showDns" select="not(ai:all-zero($rows/dnsTime/mean))" />
            <xsl:variable name="showConnect" select="not(ai:all-zero($rows/connectTime/mean))" />
            <xsl:variable name="showSend" select="not(ai:all-zero($rows/sendTime/mean))" />
            <xsl:variable name="showBusy" select="not(ai:all-zero($rows/serverBusyTime/mean))" />
            <xsl:variable name="showReceive" select="not(ai:all-zero($rows/receiveTime/mean))" />
            <xsl:variable name="showTtfb" select="not(ai:all-zero($rows/timeToFirstBytes/mean))" />

            <xsl:variable name="headers" as="xs:string*">
                <xsl:sequence select="'Name', 'Count', 'Count/s', 'Errors', 'Error%', 'Min', 'Max', 'Mean'" />
                <xsl:if test="$showMedian">
                    <xsl:sequence select="'Median'" />
                </xsl:if>
                <xsl:sequence select="'Dev'" />
                <xsl:for-each select="$percentiles">
                    <xsl:sequence select="concat('P', substring-after(name(), 'p'))" />
                </xsl:for-each>
                <xsl:if test="$showDns">
                    <xsl:sequence select="'DNS'" />
                </xsl:if>
                <xsl:if test="$showConnect">
                    <xsl:sequence select="'Connect'" />
                </xsl:if>
                <xsl:if test="$showSend">
                    <xsl:sequence select="'Send'" />
                </xsl:if>
                <xsl:if test="$showBusy">
                    <xsl:sequence select="'ServerBusy'" />
                </xsl:if>
                <xsl:if test="$showReceive">
                    <xsl:sequence select="'Receive'" />
                </xsl:if>
                <xsl:if test="$showTtfb">
                    <xsl:sequence select="'TTFB'" />
                </xsl:if>
                <xsl:sequence select="'BytesSent', 'BytesRecv'" />
            </xsl:variable>

            <xsl:text>&#10;## Requests&#10;</xsl:text>
            <xsl:call-template name="ai-desc-requests" />
            <xsl:text>&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="$rows">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(name)" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:num(countPerSecond)" />
                    <xsl:sequence select="ai:int(errors)" />
                    <xsl:sequence select="ai:num(errorPercentage)" />
                    <xsl:sequence select="ai:ms(min)" />
                    <xsl:sequence select="ai:ms(max)" />
                    <xsl:sequence select="ai:ms(mean)" />
                    <xsl:if test="$showMedian">
                        <xsl:sequence select="ai:ms(median)" />
                    </xsl:if>
                    <xsl:sequence select="ai:ms(deviation)" />
                    <xsl:for-each select="percentiles/*">
                        <xsl:sequence select="ai:ms(.)" />
                    </xsl:for-each>
                    <xsl:if test="$showDns">
                        <xsl:sequence select="ai:ms(dnsTime/mean)" />
                    </xsl:if>
                    <xsl:if test="$showConnect">
                        <xsl:sequence select="ai:ms(connectTime/mean)" />
                    </xsl:if>
                    <xsl:if test="$showSend">
                        <xsl:sequence select="ai:ms(sendTime/mean)" />
                    </xsl:if>
                    <xsl:if test="$showBusy">
                        <xsl:sequence select="ai:ms(serverBusyTime/mean)" />
                    </xsl:if>
                    <xsl:if test="$showReceive">
                        <xsl:sequence select="ai:ms(receiveTime/mean)" />
                    </xsl:if>
                    <xsl:if test="$showTtfb">
                        <xsl:sequence select="ai:ms(timeToFirstBytes/mean)" />
                    </xsl:if>
                    <xsl:sequence select="ai:ms(bytesSent/mean)" />
                    <xsl:sequence select="ai:ms(bytesReceived/mean)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ custom values -->

    <xsl:template name="custom-values">
        <xsl:if test="customValues/*">
            <xsl:variable name="headers" as="xs:string*"
                          select="'Name', 'Count', 'Count/s', 'Min', 'Max', 'Mean', 'StdDev'" />

            <xsl:text>&#10;## Custom Values&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="customValues/*">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(name)" />
                    <xsl:sequence select="ai:int(count)" />
                    <xsl:sequence select="ai:num(countPerSecond)" />
                    <xsl:sequence select="ai:num(min)" />
                    <xsl:sequence select="ai:num(max)" />
                    <xsl:sequence select="ai:num(mean)" />
                    <xsl:sequence select="ai:num(standardDeviation)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ errors -->

    <xsl:template name="errors">
        <xsl:text>&#10;## Errors&#10;</xsl:text>
        <xsl:call-template name="ai-desc-errors" />

        <xsl:choose>
            <xsl:when test="errors/error">
                <xsl:variable name="entries" select="errors/error" />
                <xsl:variable name="messages" select="distinct-values($entries/message)" />
                <xsl:variable name="groupCount" select="count($messages)" />

                <xsl:text>&#10;```yaml&#10;</xsl:text>
                <xsl:text>totalErrors: </xsl:text>
                <xsl:value-of select="ai:int(sum($entries/count/number(.)))" />
                <xsl:text>&#10;distinctEntries: </xsl:text>
                <xsl:value-of select="ai:int(count($entries))" />
                <xsl:text>&#10;distinctMessages: </xsl:text>
                <xsl:value-of select="ai:int($groupCount)" />
                <xsl:text>&#10;tracesIncludedFor: </xsl:text>
                <xsl:value-of select="ai:int(min(($tracesIncludedFor, $groupCount)))" />
                <xsl:text>&#10;```&#10;&#10;</xsl:text>

                <!-- overview first, so the shape is clear before any trace -->
                <xsl:value-of select="ai:header(('#', 'Message', 'Count', 'Test Cases', 'Actions'))" />

                <xsl:for-each-group select="$entries" group-by="string(message)">
                    <xsl:sort select="sum(current-group()/count/number(.))" order="descending" data-type="number" />
                    <xsl:variable name="cells" as="xs:string*">
                        <xsl:sequence select="ai:int(position())" />
                        <xsl:sequence select="ai:cell(current-grouping-key())" />
                        <xsl:sequence select="ai:int(sum(current-group()/count/number(.)))" />
                        <xsl:sequence select="ai:int(count(distinct-values(current-group()/testCaseName)))" />
                        <xsl:sequence select="ai:int(count(distinct-values(current-group()/actionName)))" />
                    </xsl:variable>
                    <xsl:value-of select="ai:row($cells)" />
                </xsl:for-each-group>

                <!-- then each group in the same order, with a trace for the largest few -->
                <xsl:for-each-group select="$entries" group-by="string(message)">
                    <xsl:sort select="sum(current-group()/count/number(.))" order="descending" data-type="number" />

                    <xsl:text>&#10;### </xsl:text>
                    <xsl:value-of select="position()" />
                    <xsl:text>. </xsl:text>
                    <xsl:value-of select="current-grouping-key()" />
                    <xsl:text> - </xsl:text>
                    <xsl:value-of select="ai:int(sum(current-group()/count/number(.)))" />
                    <xsl:text>&#10;&#10;</xsl:text>

                    <xsl:value-of select="ai:header(('Test Case', 'Action', 'Count'))" />
                    <xsl:for-each select="current-group()">
                        <xsl:sort select="number(count)" order="descending" />
                        <xsl:variable name="cells" as="xs:string*">
                            <xsl:sequence select="if (testCaseName != '') then ai:cell(testCaseName) else 'n/a'" />
                            <xsl:sequence select="if (actionName != '') then ai:cell(actionName) else 'n/a'" />
                            <xsl:sequence select="ai:int(count)" />
                        </xsl:variable>
                        <xsl:value-of select="ai:row($cells)" />
                    </xsl:for-each>

                    <!--
                    Only the largest groups carry a trace, and only its leading frames.
                    The old file spent 100 KB on 100 traces padded to a 1000 character cap,
                    almost all of it framework frames that diagnose nothing.
                    -->
                    <xsl:if test="position() &lt;= $tracesIncludedFor">
                        <xsl:variable name="trace" select="(current-group()[count = max(current-group()/count/number(.))])[1]/trace" />
                        <xsl:if test="$trace != ''">
                            <xsl:variable name="lines" select="tokenize(string($trace), '&#10;')" />
                            <xsl:text>&#10;```&#10;</xsl:text>
                            <xsl:value-of select="string-join($lines[position() &lt;= $traceFrames], '&#10;')" />
                            <xsl:if test="count($lines) &gt; $traceFrames">
                                <xsl:text>&#10;&#9;... </xsl:text>
                                <xsl:value-of select="count($lines) - $traceFrames" />
                                <xsl:text> more frames</xsl:text>
                            </xsl:if>
                            <xsl:text>&#10;```&#10;</xsl:text>
                        </xsl:if>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>&#10;No errors recorded.&#10;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ============================================================ events -->

    <xsl:template name="events">
        <xsl:if test="events/event">
            <xsl:variable name="headers" as="xs:string*" select="'Test Case', 'Event Name', 'Count'" />

            <xsl:text>&#10;## Events&#10;&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <!-- loudest first, same reasoning as the error section -->
            <xsl:for-each select="events/event">
                <xsl:sort select="number(totalCount)" order="descending" />
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="if (testCaseName != '') then string(testCaseName) else 'n/a'" />
                    <xsl:sequence select="string(name)" />
                    <xsl:sequence select="ai:int(totalCount)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ agents -->

    <xsl:template name="agents">
        <xsl:if test="agents/agent">
            <xsl:variable name="headers" as="xs:string*"
                          select="'Agent', 'Transactions', 'Errors', 'Error%', 'CPU% Mean', 'CPU% Max',
                                  'FullGC', 'FullGC Time', 'MinorGC', 'MinorGC Time'" />

            <xsl:text>&#10;## Agents&#10;</xsl:text>
            <xsl:call-template name="ai-desc-agents" />
            <xsl:text>&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="agents/agent">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(name)" />
                    <xsl:sequence select="ai:int(transactions)" />
                    <xsl:sequence select="ai:int(transactionErrors)" />
                    <xsl:sequence select="ai:num(transactionErrorPercentage)" />
                    <!-- a mean of 33% hides an agent that was pegged during peak, so max comes too -->
                    <xsl:sequence select="ai:num(cpuUsage/mean)" />
                    <xsl:sequence select="ai:num(cpuUsage/max)" />
                    <xsl:sequence select="ai:int(fullGcCount)" />
                    <xsl:sequence select="ai:ms(fullGcTime)" />
                    <xsl:sequence select="ai:int(minorGcCount)" />
                    <xsl:sequence select="ai:ms(minorGcTime)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================ web vitals -->

    <xsl:template name="web-vitals">
        <xsl:if test="webVitalsList/webVitals">
            <xsl:variable name="headers" as="xs:string*">
                <xsl:sequence select="'Action'" />
                <xsl:sequence select="'CLS', 'CLS Rating'" />
                <xsl:sequence select="'FCP', 'FCP Rating'" />
                <xsl:sequence select="'LCP', 'LCP Rating'" />
                <xsl:sequence select="'INP', 'INP Rating'" />
                <xsl:sequence select="'TTFB', 'TTFB Rating'" />
            </xsl:variable>

            <xsl:text>&#10;## Web Vitals&#10;</xsl:text>
            <xsl:call-template name="ai-desc-web-vitals" />
            <xsl:text>&#10;</xsl:text>
            <xsl:value-of select="ai:header($headers)" />

            <xsl:for-each select="webVitalsList/webVitals">
                <xsl:variable name="cells" as="xs:string*">
                    <xsl:sequence select="string(name)" />
                    <!-- CLS is a unitless layout shift score, the rest are milliseconds -->
                    <xsl:sequence select="ai:num(cls/score)" />
                    <xsl:sequence select="string(cls/rating)" />
                    <xsl:sequence select="ai:ms(fcp/score)" />
                    <xsl:sequence select="string(fcp/rating)" />
                    <xsl:sequence select="ai:ms(lcp/score)" />
                    <xsl:sequence select="string(lcp/rating)" />
                    <xsl:sequence select="ai:ms(inp/score)" />
                    <xsl:sequence select="string(inp/rating)" />
                    <xsl:sequence select="ai:ms(ttfb/score)" />
                    <xsl:sequence select="string(ttfb/rating)" />
                </xsl:variable>
                <xsl:value-of select="ai:row($cells)" />
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
