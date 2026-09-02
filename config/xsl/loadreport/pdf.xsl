<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="xml"
            indent="yes"
            omit-xml-declaration="yes"
            encoding="UTF-8"/>

<xsl:include href="../common/util/convert-apdex-to-color.xsl" />
<xsl:include href="../common/util/convertIllegalCharactersInFileName.xsl" />
<xsl:include href="../common/util/string-replace-all.xsl" />
<xsl:include href="../common/util/percentage.xsl" />
<xsl:include href="../common/util/format-bytes.xsl" />
<xsl:include href="../common/util/format-msec-to-h.xsl" />
<xsl:include href="../common/util/create-totals-td.xsl" />
<xsl:include href="../common/util/filtered-footer-row.xsl" />
<xsl:include href="../common/util/load-profile-table.xsl" />

<xsl:include href="util/timer-labels.xsl" />
<xsl:include href="util/timer-row.xsl" />
<xsl:include href="util/timer-summary-row.xsl" />
<xsl:include href="util/timer-table.xsl" />
<xsl:include href="util/network-table.xsl" />
<xsl:include href="util/summary-timer-row.xsl" />

<xsl:include href="text/descriptions.xsl" />

<xsl:include href="sections/load-profile.xsl" />
<xsl:include href="sections/rating.xsl" />
<xsl:include href="sections/comment.xsl" />
<xsl:include href="sections/general.xsl" />
<xsl:include href="sections/summary.xsl" />
<xsl:include href="sections/network-summary.xsl" />
<xsl:include href="sections/agent-summary.xsl" />

<xsl:key name="errorsByMessage" match="errors/error" use="message" />
<xsl:key name="eventsByName" match="events/event" use="name" />

<xsl:param name="productName" select="'XLT'" />
<xsl:param name="productVersion" select="''" />
<xsl:param name="productUrl" select="'https://www.xceptance.com'" />
<xsl:param name="projectName" select="''" />

<xsl:template match="/testreport">
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>
        <xsl:choose>
            <xsl:when test="string-length(configuration/projectName) &gt; 0">
                <xsl:value-of select="configuration/projectName" /> - <xsl:value-of select="$productName" /> Performance Report
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$productName" /> Performance Report
            </xsl:otherwise>
        </xsl:choose>
    </title>
    <link rel="stylesheet" type="text/css" href="css/pdf.css" />
</head>
<body id="loadtestreport">

    <!-- Document Header -->
    <div id="header">
        <div class="brand"><xsl:value-of select="$productName"/> Performance Report</div>
        <div class="title">
            <xsl:choose>
                <xsl:when test="string-length(configuration/projectName) &gt; 0">
                    <xsl:value-of select="configuration/projectName" />
                </xsl:when>
                <xsl:otherwise>Load &amp; Performance Test</xsl:otherwise>
            </xsl:choose>
        </div>
        <div class="report-meta">
            <span><b>Start:</b>&#160;<xsl:value-of select="general/startTime" /></span> |
            <span><b>End:</b>&#160;<xsl:value-of select="general/endTime" /></span> |
            <span><b>Duration:</b>&#160;<xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1" select="general/duration * 1000" /></xsl:call-template></span>
        </div>
    </div>

    <div id="data-content">
        <!-- Load Profile -->
        <xsl:if test="configuration/loadProfile">
            <xsl:call-template name="load-profile">
                <xsl:with-param name="rootNode" select="configuration" />
                <xsl:with-param name="loadMeter" select="'false'" />
            </xsl:call-template>
        </xsl:if>

        <!-- Rating -->
        <xsl:call-template name="rating-section">
            <xsl:with-param name="rootNode" select="configuration" />
        </xsl:call-template>

        <!-- Test Comment -->
        <xsl:if test="configuration/comments">
            <xsl:call-template name="testcomment">
                <xsl:with-param name="rootNode" select="configuration/comments" />
            </xsl:call-template>
        </xsl:if>

        <!-- General Info -->
        <xsl:call-template name="general">
            <xsl:with-param name="rootNode" select="general" />
        </xsl:call-template>

        <!-- Agent Summary -->
        <xsl:if test="count(agents/*) &gt; 0">
            <xsl:call-template name="agent-summary">
                <xsl:with-param name="rootNode" select="agents" />
            </xsl:call-template>
        </xsl:if>

        <!-- Timer Summary -->
        <xsl:call-template name="summary"/>

        <!-- Network Summary -->
        <xsl:call-template name="network-summary">
            <xsl:with-param name="rootNode" select="general" />
        </xsl:call-template>

        <!-- Transactions Table -->
        <xsl:if test="count(transactions/*) &gt; 0">
            <div class="page-break"></div>
            <div class="section" id="transactions-section">
                <h2>Transactions</h2>
                <div class="chart-container">
                    <img src="charts/transactions/All%20Transactions.webp" alt="Transactions Overview Chart" />
                </div>
                <xsl:call-template name="timer-table">
                    <xsl:with-param name="elements" select="transactions/*"/>
                    <xsl:with-param name="summaryElement" select="summary/transactions"/>
                    <xsl:with-param name="tableRowHeader" select="'Transaction Name'"/>
                    <xsl:with-param name="type" select="'transaction'"/>
                    <xsl:with-param name="hasLinks" select="'false'"/>
                </xsl:call-template>
            </div>
        </xsl:if>

        <!-- Actions Table -->
        <xsl:if test="count(actions/*) &gt; 0">
            <div class="page-break"></div>
            <div class="section" id="actions-section">
                <h2>Actions</h2>
                <div class="chart-container">
                    <img src="charts/actions/All%20Actions.webp" alt="Actions Overview Chart" />
                </div>
                <xsl:call-template name="timer-table">
                    <xsl:with-param name="elements" select="actions/*"/>
                    <xsl:with-param name="summaryElement" select="summary/actions"/>
                    <xsl:with-param name="tableRowHeader" select="'Action Name'"/>
                    <xsl:with-param name="type" select="'action'"/>
                    <xsl:with-param name="hasLinks" select="'false'"/>
                </xsl:call-template>
            </div>
        </xsl:if>

        <!-- Requests Table -->
        <xsl:if test="count(requests/*) &gt; 0">
            <div class="page-break"></div>
            <div class="section" id="requests-section">
                <h2>Requests</h2>
                <div class="chart-container">
                    <img src="charts/requests/All%20Requests.webp" alt="Requests Overview Chart" />
                </div>
                <xsl:call-template name="timer-table">
                    <xsl:with-param name="elements" select="requests/*"/>
                    <xsl:with-param name="summaryElement" select="summary/requests"/>
                    <xsl:with-param name="tableRowHeader" select="'Request Name'"/>
                    <xsl:with-param name="runtimeIntervalsNode" select="testReportConfig/runtimeIntervals"/>
                    <xsl:with-param name="type" select="'request'"/>
                    <xsl:with-param name="hasLinks" select="'false'"/>
                </xsl:call-template>
            </div>
        </xsl:if>

        <!-- Custom Timers Table -->
        <xsl:if test="count(customTimers/*) &gt; 0">
            <div class="page-break"></div>
            <div class="section" id="custom-timers-section">
                <h2>Custom Timers</h2>
                <div class="chart-container">
                    <img src="charts/custom/All%20Custom%20Timers.webp" alt="Custom Timers Overview Chart" />
                </div>
                <xsl:call-template name="timer-table">
                    <xsl:with-param name="elements" select="customTimers/*"/>
                    <xsl:with-param name="summaryElement" select="summary/customTimers"/>
                    <xsl:with-param name="tableRowHeader" select="'Timer Name'"/>
                    <xsl:with-param name="type" select="'custom'"/>
                    <xsl:with-param name="hasLinks" select="'false'"/>
                </xsl:call-template>
            </div>
        </xsl:if>

        <!-- Error Summary (Stats only, no stacktraces) -->
        <xsl:if test="count(errors/error) &gt; 0">
            <div class="section" id="error-overview-section">
                <h2>Error Overview</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Error Message</th>
                            <th>Count</th>
                            <th>Percentage</th>
                        </tr>
                    </thead>
                    <xsl:variable name="totalErrorCount" select="sum(errors/error/count)" />
                    <xsl:variable name="errorRepresentativesWithDistinctMessages" select="errors/error[generate-id() = generate-id(key('errorsByMessage', message)[1])]" />
                    <xsl:variable name="countDistinctErrorMessages" select="count($errorRepresentativesWithDistinctMessages)" />
                    <tfoot>
                        <tr class="totals">
                            <xsl:call-template name="create-totals-td">
                                <xsl:with-param name="rows-in-table" select="$countDistinctErrorMessages" />
                            </xsl:call-template>
                            <td class="value number">
                                <xsl:value-of select="format-number($totalErrorCount, '#,##0')" />
                            </td>
                            <td class="value number">
                                <xsl:value-of select="format-number(1, '#0.0%')" />
                            </td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <xsl:for-each select="$errorRepresentativesWithDistinctMessages">
                            <xsl:sort select="sum(key('errorsByMessage', message)/count)" order="descending" data-type="number" />
                            <xsl:variable name="errorCountByMessage" select="sum(key('errorsByMessage', message)/count)" />
                            <tr>
                                <td class="value text"><xsl:value-of select="message" /></td>
                                <td class="value number"><xsl:value-of select="format-number($errorCountByMessage, '#,##0')" /></td>
                                <td class="value number"><xsl:value-of select="format-number($errorCountByMessage div $totalErrorCount, '#0.0%')" /></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div>
        </xsl:if>

        <!-- Event Summary (Stats only) -->
        <xsl:if test="count(events/event) &gt; 0">
            <div class="section" id="event-overview-section">
                <h2>Event Overview</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Event</th>
                            <th>Count</th>
                            <th>Dropped</th>
                            <th>Percentage</th>
                        </tr>
                    </thead>
                    <xsl:variable name="totalEventCount" select="sum(events/event/totalCount)" />
                    <xsl:variable name="totalDroppedEventCount" select="sum(events/event/droppedCount)" />
                    <xsl:variable name="eventRepresentativesWithDistinctNames" select="events/event[generate-id() = generate-id(key('eventsByName', name)[1])]" />
                    <xsl:variable name="countDistinctEventNames" select="count($eventRepresentativesWithDistinctNames)" />
                    <tfoot>
                        <tr class="totals">
                            <xsl:call-template name="create-totals-td">
                                <xsl:with-param name="rows-in-table" select="$countDistinctEventNames" />
                            </xsl:call-template>
                            <td class="value number">
                                <xsl:value-of select="format-number($totalEventCount, '#,##0')"/>
                            </td>
                            <td class="value number">
                                <xsl:value-of select="format-number($totalDroppedEventCount, '#,##0')"/>
                            </td>
                            <td class="value number">
                                <xsl:value-of select="format-number(1, '#0.0%')"/>
                            </td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <xsl:for-each select="$eventRepresentativesWithDistinctNames">
                            <xsl:sort select="name" />
                            <xsl:variable name="eventCountByName" select="sum(key('eventsByName', name)/totalCount)" />
                            <xsl:variable name="eventDroppedCountByName" select="sum(key('eventsByName', name)/droppedCount)" />
                            <tr>
                                <td class="value text"><xsl:value-of select="name"/></td>
                                <td class="value number"><xsl:value-of select="format-number($eventCountByName, '#,##0')"/></td>
                                <td class="value number"><xsl:value-of select="format-number($eventDroppedCountByName, '#,##0')"/></td>
                                <td class="value number"><xsl:value-of select="format-number($eventCountByName div $totalEventCount, '#0.0%')"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div>
        </xsl:if>

    </div>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
