<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" encoding="UTF-8" />
    <xsl:strip-space elements="*" />

    <!--
    AI Summary Template — generates ai-summary.md
    Hybrid YAML+Markdown format optimized for LLM token efficiency.
    -->
    <xsl:template match="/testreport">
        <xsl:if test="general">
# Test Metadata

```yaml
startTime: "<xsl:value-of select="general/startTime" />"
endTime: "<xsl:value-of select="general/endTime" />"
duration: <xsl:value-of select="general/duration" />
bytesSent: <xsl:value-of select="general/bytesSent" />
bytesReceived: <xsl:value-of select="general/bytesReceived" />
hits: <xsl:value-of select="general/hits" />
```

        </xsl:if>

        <!-- Configuration -->
        <xsl:if test="configuration/version">
# XLT Version

```yaml
product: "<xsl:value-of select="configuration/version/productName" />"
version: "<xsl:value-of select="configuration/version/version" />"
```

        </xsl:if>

        <xsl:if test="configuration/projectName != ''">
# Project

```yaml
name: "<xsl:value-of select="configuration/projectName" />"
```

        </xsl:if>

        <xsl:if test="configuration/comments/string">
# Comments

            <xsl:for-each select="configuration/comments/string">
- <xsl:value-of select="." />
            </xsl:for-each>

        </xsl:if>

        <xsl:if test="configuration/testCases/testCase">
# Load Profile

| Test Case | Users | Iterations | Measurement [s] | Ramp-Up [s] | Shutdown [s] |
| --- | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="configuration/testCases/testCase">| <xsl:choose><xsl:when test="userName != ''"><xsl:value-of select="userName" /></xsl:when><xsl:otherwise><xsl:value-of select="testCaseClassName" /></xsl:otherwise></xsl:choose> | <xsl:value-of select="numberOfUsers" /> | <xsl:value-of select="numberOfIterations" /> | <xsl:value-of select="measurementPeriod" /> | <xsl:value-of select="rampUpPeriod" /> | <xsl:value-of select="shutdownPeriod" /> |<xsl:text>&#10;</xsl:text></xsl:for-each>
        </xsl:if>

        <!-- Transactions -->
        <xsl:if test="transactions/*">
# Transactions

<xsl:call-template name="timer-table">
    <xsl:with-param name="elements" select="transactions/*" />
</xsl:call-template>
        </xsl:if>

        <!-- Actions -->
        <xsl:if test="actions/*">
# Actions

<xsl:call-template name="timer-table">
    <xsl:with-param name="elements" select="actions/*" />
</xsl:call-template>
        </xsl:if>

        <!-- Requests -->
        <xsl:if test="requests/*">
# Requests

| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev | <xsl:for-each select="requests/*[1]/percentiles/*">P<xsl:value-of select="substring-after(name(), 'p')" /> | </xsl:for-each>DNS | Connect | Send | ServerBusy | Receive | TTFB | BytesSent | BytesRecv |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | <xsl:for-each select="requests/*[1]/percentiles/*">---: | </xsl:for-each>---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="requests/*">| <xsl:value-of select="name" /> | <xsl:value-of select="count" /> | <xsl:value-of select="countPerSecond" /> | <xsl:value-of select="errors" /> | <xsl:value-of select="errorPercentage" /> | <xsl:value-of select="min" /> | <xsl:value-of select="max" /> | <xsl:value-of select="mean" /> | <xsl:value-of select="median" /> | <xsl:value-of select="deviation" /> | <xsl:for-each select="percentiles/*"><xsl:value-of select="." /> | </xsl:for-each><xsl:value-of select="dnsTime/mean" /> | <xsl:value-of select="connectTime/mean" /> | <xsl:value-of select="sendTime/mean" /> | <xsl:value-of select="serverBusyTime/mean" /> | <xsl:value-of select="receiveTime/mean" /> | <xsl:value-of select="timeToFirstBytes/mean" /> | <xsl:value-of select="bytesSent/mean" /> | <xsl:value-of select="bytesReceived/mean" /> |<xsl:text>&#10;</xsl:text></xsl:for-each>
        </xsl:if>

        <!-- Page Load Timings -->
        <xsl:if test="pageLoadTimings/*">
# Page Load Timings

<xsl:call-template name="timer-table">
    <xsl:with-param name="elements" select="pageLoadTimings/*" />
</xsl:call-template>
        </xsl:if>

        <!-- Custom Timers -->
        <xsl:if test="customTimers/*">
# Custom Timers

<xsl:call-template name="timer-table">
    <xsl:with-param name="elements" select="customTimers/*" />
</xsl:call-template>
        </xsl:if>
        
        <!-- Custom Values -->
        <xsl:if test="customValues/*">
# Custom Values

| Name | Count | Count/s | Min | Max | Mean | StdDev |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="customValues/*">| <xsl:value-of select="name" /> | <xsl:value-of select="count" /> | <xsl:value-of select="countPerSecond" /> | <xsl:value-of select="min" /> | <xsl:value-of select="max" /> | <xsl:value-of select="mean" /> | <xsl:value-of select="standardDeviation" /> |<xsl:text>&#10;</xsl:text></xsl:for-each>
        </xsl:if>
        <!-- Errors -->
# Errors
        <xsl:choose>
            <xsl:when test="errors/error">
                <xsl:for-each select="errors/error">
## Error: <xsl:value-of select="message" />

- **Test Case**: <xsl:value-of select="testCaseName" />
- **Action**: <xsl:choose><xsl:when test="actionName != ''"><xsl:value-of select="actionName" /></xsl:when><xsl:otherwise>n/a</xsl:otherwise></xsl:choose>
- **Count**: <xsl:value-of select="count" />
                    <xsl:if test="trace != ''">

### Stack Trace
```
<xsl:value-of select="substring(trace, 1, 1000)" /><xsl:if test="string-length(trace) > 1000">...</xsl:if>
```
                    </xsl:if>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
No errors recorded.
            </xsl:otherwise>
        </xsl:choose>

        <!-- Events -->
        <xsl:if test="events/event">
# Events

| Test Case | Event Name | Count |
| --- | --- | ---: |
<xsl:for-each select="events/event">| <xsl:choose><xsl:when test="testCaseName != ''"><xsl:value-of select="testCaseName" /></xsl:when><xsl:otherwise>n/a</xsl:otherwise></xsl:choose> | <xsl:value-of select="name" /> | <xsl:value-of select="totalCount" /> |<xsl:text>&#10;</xsl:text></xsl:for-each>
        </xsl:if>

        <!-- Agents -->
        <xsl:if test="agents/agent">
# Agents

| Agent | Transactions | Errors | Error% | CPU% (Mean) |
| --- | ---: | ---: | ---: | ---: |
<xsl:for-each select="agents/agent">| <xsl:value-of select="name" /> | <xsl:value-of select="transactions" /> | <xsl:value-of select="transactionErrors" /> | <xsl:value-of select="transactionErrorPercentage" /> | <xsl:value-of select="cpuUsage/mean" /> |<xsl:text>&#10;</xsl:text></xsl:for-each>
        </xsl:if>

        <!-- Web Vitals -->
        <xsl:if test="webVitalsList/webVitals">
# Web Vitals

| Action | CLS Score | CLS Rating | FCP Score | FCP Rating | LCP Score | LCP Rating | INP Score | INP Rating | TTFB Score | TTFB Rating |
| --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- |
<xsl:for-each select="webVitalsList/webVitals">| <xsl:value-of select="name" /> | <xsl:value-of select="cls/score" /> | <xsl:value-of select="cls/rating" /> | <xsl:value-of select="fcp/score" /> | <xsl:value-of select="fcp/rating" /> | <xsl:value-of select="lcp/score" /> | <xsl:value-of select="lcp/rating" /> | <xsl:value-of select="inp/score" /> | <xsl:value-of select="inp/rating" /> | <xsl:value-of select="ttfb/score" /> | <xsl:value-of select="ttfb/rating" /> |<xsl:text>&#10;</xsl:text></xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="timer-table">
        <xsl:param name="elements" />
| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev | <xsl:for-each select="$elements[1]/percentiles/*">P<xsl:value-of select="substring-after(name(), 'p')" /> | </xsl:for-each>
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | <xsl:for-each select="$elements[1]/percentiles/*">---: | </xsl:for-each><xsl:text>&#10;</xsl:text>
<xsl:for-each select="$elements">| <xsl:value-of select="name" /> | <xsl:value-of select="count" /> | <xsl:value-of select="countPerSecond" /> | <xsl:value-of select="errors" /> | <xsl:value-of select="errorPercentage" /> | <xsl:value-of select="min" /> | <xsl:value-of select="max" /> | <xsl:value-of select="mean" /> | <xsl:value-of select="median" /> | <xsl:value-of select="deviation" /> | <xsl:for-each select="percentiles/*"><xsl:value-of select="." /> | </xsl:for-each><xsl:text>&#10;</xsl:text></xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>