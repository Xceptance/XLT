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
        <xsl:text>&#10;</xsl:text>

        <xsl:choose>
            <xsl:when test="errors/error">
                <xsl:for-each select="errors/error">
                    <xsl:text>### Error: </xsl:text>
                    <xsl:value-of select="message" />
                    <xsl:text>&#10;&#10;- **Test Case**: </xsl:text>
                    <xsl:value-of select="testCaseName" />
                    <xsl:text>&#10;- **Action**: </xsl:text>
                    <xsl:value-of select="if (actionName != '') then actionName else 'n/a'" />
                    <xsl:text>&#10;- **Count**: </xsl:text>
                    <xsl:value-of select="ai:int(count)" />
                    <xsl:text>&#10;</xsl:text>

                    <xsl:if test="trace != ''">
                        <xsl:text>&#10;```&#10;</xsl:text>
                        <xsl:value-of select="substring(trace, 1, 1000)" />
                        <xsl:if test="string-length(trace) &gt; 1000">
                            <xsl:text>...</xsl:text>
                        </xsl:if>
                        <xsl:text>&#10;```&#10;</xsl:text>
                    </xsl:if>
                    <xsl:text>&#10;</xsl:text>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>No errors recorded.&#10;</xsl:text>
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
                          select="'Agent', 'Transactions', 'Errors', 'Error%', 'CPU% Mean'" />

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
                    <xsl:sequence select="ai:num(cpuUsage/mean)" />
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
