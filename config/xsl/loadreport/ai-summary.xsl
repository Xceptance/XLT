<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="text" 
            omit-xml-declaration="yes"
            encoding="UTF-8"
             />

<xsl:strip-space elements="*"/>

<xsl:template name="timer-table">
<xsl:param name="rootNode"/>
| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="$rootNode/*">| <xsl:value-of select="name"/> | <xsl:value-of select="count"/> | <xsl:value-of select="countPerSecond"/> | <xsl:value-of select="errors"/> | <xsl:value-of select="errorPercentage"/> | <xsl:value-of select="min"/> | <xsl:value-of select="max"/> | <xsl:value-of select="mean"/> | <xsl:value-of select="median"/> | <xsl:value-of select="deviation"/> |
</xsl:for-each>
</xsl:template>



<xsl:template match="/testreport">

<xsl:comment>
    AI Summary Template — generates ai-summary.md
    Hybrid YAML+Markdown format optimized for LLM token efficiency.
</xsl:comment>


<xsl:comment>===== Test Metadata =====</xsl:comment>

# Test Metadata

```yaml
startTime: <xsl:value-of select="general/startTime"/>
endTime: <xsl:value-of select="general/startTime"/>
duration: <xsl:value-of select="general/duration"/>
bytesSent: <xsl:value-of select="general/bytesSent"/>
bytesReceived: <xsl:value-of select="general/bytesReceived"/>
hits: <xsl:value-of select="general/hits"/>
```

<xsl:comment>===== Configuration =====</xsl:comment>

# XLT Version

```yaml
product: <xsl:value-of select="configuration/version/productName"/>
version: <xsl:value-of select="configuration/version/version"/>
```

<xsl:comment>===== Project =====</xsl:comment>

<xsl:if test="configuration/projectName">
# Project

```yaml
name: <xsl:value-of select="configuration/projectName"/>
```
</xsl:if>


<xsl:comment>===== Comments =====</xsl:comment>

<xsl:if test="configuration/comments/string">
# Comments

<xsl:for-each select="configuration/comments/string">- <xsl:value-of select="."/>
</xsl:for-each>
</xsl:if>


<xsl:comment>===== Load Profile =====</xsl:comment>

<xsl:if test="configuration/loadProfile/testCase">
# Load Profile

| Test Case | Users | Iterations | Measurement [s] | Ramp-Up [s] | Shutdown [s] |
| --- | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="configuration/loadProfile/testCase">| <xsl:value-of select="userName"/> | <xsl:value-of select="numberOfUsers"/> | <xsl:value-of select="numberOfIterations"/> | <xsl:value-of select="measurementPeriod"/> | <xsl:value-of select="measurementPeriod"/> | <xsl:value-of select="shutdownPeriod"/> |
</xsl:for-each>
</xsl:if>


<xsl:comment>===== Transactions =====</xsl:comment>

<xsl:if test="transactions/*">
# Transactions

<xsl:call-template name="timer-table">
    <xsl:with-param name="rootNode" select="transactions"/>
</xsl:call-template>
</xsl:if>


<xsl:comment>===== Actions =====</xsl:comment>

<xsl:if test="actions/*">
# Actions

<xsl:call-template name="timer-table">
    <xsl:with-param name="rootNode" select="actions"/>
</xsl:call-template>
</xsl:if>

<xsl:comment>===== Requests =====</xsl:comment>

<xsl:if test="requests/*">
# Requests

| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev | DNS | Connect | Send | ServerBusy | Receive | TTFB | BytesSent | BytesRecv |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="requests/request">
<xsl:text>| </xsl:text>
<xsl:value-of select="name"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="count"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="countPerSecond"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="errors"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="errorPercentage"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="min"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="max"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="median"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="deviation"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="dnsTime/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="connectTime/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="sendTime/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="serverBusyTime/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="receiveTime/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="timeToFirstBytes/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="bytesSent/mean"/>
<xsl:text> | </xsl:text>
<xsl:value-of select="bytesReceived/mean"/>
<xsl:text> |&#xa;</xsl:text>
</xsl:for-each>
</xsl:if>


<xsl:comment>===== Page Load Timings =====</xsl:comment>

<xsl:if test="pageLoadTimings/*">
# Page Load Timings 

<xsl:call-template name="timer-table">
    <xsl:with-param name="rootNode" select="pageLoadTimings"/>
</xsl:call-template>
</xsl:if>

<xsl:comment>===== Custom Timers =====</xsl:comment>

<xsl:if test="customTimers/*">
# Custom Timers 

<xsl:call-template name="timer-table">
    <xsl:with-param name="rootNode" select="customTimers"/>
</xsl:call-template>
</xsl:if>


<xsl:comment>===== Custom Values =====</xsl:comment>

<xsl:if test="customValues/*">
# Custom Values 

| Name | Count | Count/s | Min | Max | Mean | StdDev |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
<xsl:for-each select="customValues/customValue">| <xsl:value-of select="name"/> | <xsl:value-of select="count"/> | <xsl:value-of select="countPerSecond"/> | <xsl:value-of select="min"/> | <xsl:value-of select="max"/> | <xsl:value-of select="mean"/> | <xsl:value-of select="standardDeviation"/> |
</xsl:for-each>
</xsl:if>


<xsl:comment>===== Errors =====</xsl:comment>

# Errors

<xsl:choose>
<xsl:when test="errors/error">
<xsl:for-each select="errors/error">
## Error: <xsl:value-of select="message"/>

- **Test Case**: <xsl:value-of select="testCaseName"/>
- **Action**: <xsl:value-of select="actionName"/>
- **Count**: <xsl:value-of select="count"/>
<xsl:if test="trace">
- **Stack Trace**:
```
<xsl:value-of select="trace"/>
```
</xsl:if>
</xsl:for-each>
</xsl:when>
<xsl:otherwise>
No errors recorded.
</xsl:otherwise>
</xsl:choose>


<xsl:comment>===== Events =====</xsl:comment>

<xsl:if test="events/event">
# Events

| Test Case | Event Name | Count |
| --- | --- | ---: |
<xsl:for-each select="events/event">| <xsl:value-of select="testCaseName"/> | <xsl:value-of select="name"/> | <xsl:value-of select="totalCount"/> |
</xsl:for-each>
</xsl:if>


<xsl:comment>===== Agents =====</xsl:comment>

<xsl:if test="agents/*">
# Agents

| Agent | Transactions | Errors | Error% | CPU% (Mean) |
| --- | ---: | ---: | ---: | ---: |
<xsl:for-each select="agents/agent">| <xsl:value-of select="name"/> | <xsl:value-of select="transactions"/> | <xsl:value-of select="transactionErrors"/> | <xsl:value-of select="transactionErrorPercentage"/> | <xsl:value-of select="cpuUsage/mean"/> |
</xsl:for-each>
</xsl:if>


<xsl:comment>===== Web Vitals =====</xsl:comment>

<xsl:if test="webVitalsList/*">
# Web Vitals

| Action | CLS Score | CLS Rating | FCP Score | FCP Rating | LCP Score | LCP Rating | INP Score | INP Rating | TTFB Score | TTFB Rating |
| --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- |
<xsl:for-each select="webVitalsList/webVitals">| <xsl:value-of select="name"/> | <xsl:value-of select="cls/score"/> | <xsl:value-of select="cls/rating"/> | <xsl:value-of select="fcp/score"/> | <xsl:value-of select="fcp/rating"/> | | <xsl:value-of select="lcp/score"/> | <xsl:value-of select="lcp/rating"/> | <xsl:value-of select="inp/score"/> | <xsl:value-of select="inp/rating"/> | <xsl:value-of select="ttfb/score"/> | <xsl:value-of select="ttfb/rating"/> |
</xsl:for-each>
</xsl:if>

</xsl:template>

</xsl:stylesheet>
