<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="timer-section">
        <xsl:param name="elements"/>
        <xsl:param name="summaryElement"/>
        <xsl:param name="tableRowHeader"/>
        <xsl:param name="directory"/>

        <div class="data">
            <div id="tabletabbies" class="chart-group tabs c-tabs">
                <xsl:variable name="gid" select="generate-id($elements)"/>
                <ul class="c-tabs-nav">
                    <li class="c-tabs-nav-link c-is-active">
                        <a href="#Median-{$gid}">Median</a>
                    </li>
                    <li class="c-tabs-nav-link">
                        <a href="#Mean-{$gid}">Mean</a>
                    </li>
                    <li class="c-tabs-nav-link">
                        <a href="#Minimum-{$gid}">Minimum</a>
                    </li>
                    <li class="c-tabs-nav-link">
                        <a href="#Maximum-{$gid}">Maximum</a>
                    </li>
                    <li class="c-tabs-nav-link">
                        <a href="#Errors-{$gid}">Errors</a>
                    </li>
                </ul>

                <!-- Report information -->
                <xsl:for-each select="$elements[1]/trendValues/trendValue">
                    <div class="cluetip-data">
                        <xsl:attribute name="id">ReportInfo-<xsl:value-of select="generate-id(.)"/></xsl:attribute>
                        <h4>
                            <xsl:text>#</xsl:text>
                            <xsl:value-of select="position()"/>
                            <xsl:text>: </xsl:text>
                            <xsl:value-of select="normalize-space(reportName)"/>
                            <xsl:text> (</xsl:text>
                            <xsl:value-of select="normalize-space(reportDate)"/>
                            <xsl:text>)</xsl:text>
                        </h4>
                        <div class="description">
                            <xsl:value-of select="reportComments" disable-output-escaping="yes"/>
                        </div>
                    </div>
                </xsl:for-each>

                <h3 class="print">Median</h3>
                <div id="Median-{$gid}" class="c-tab c-is-active">
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'median'"/>
                        <xsl:with-param name="mode" select="'absolute'"/>
                    </xsl:call-template>
                    <p/>
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'median'"/>
                        <xsl:with-param name="mode" select="'relative'"/>
                    </xsl:call-template>
                </div>

                <h3 class="print">Mean</h3>
                <div id="Mean-{$gid}" class="c-tab">
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'mean'"/>
                        <xsl:with-param name="mode" select="'absolute'"/>
                    </xsl:call-template>
                    <p/>
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'mean'"/>
                        <xsl:with-param name="mode" select="'relative'"/>
                    </xsl:call-template>
                </div>

                <h3 class="print">Minimum</h3>
                <div id="Minimum-{$gid}" class="c-tab">
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'min'"/>
                        <xsl:with-param name="mode" select="'absolute'"/>
                    </xsl:call-template>
                    <p/>
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'min'"/>
                        <xsl:with-param name="mode" select="'relative'"/>
                    </xsl:call-template>
                </div>

                <h3 class="print">Maximum</h3>
                <div id="Maximum-{$gid}" class="c-tab">
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'max'"/>
                        <xsl:with-param name="mode" select="'absolute'"/>
                    </xsl:call-template>
                    <p/>
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'max'"/>
                        <xsl:with-param name="mode" select="'relative'"/>
                    </xsl:call-template>
                </div>

                <h3 class="print">Errors</h3>
                <div id="Errors-{$gid}" class="c-tab">
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'errors'"/>
                        <xsl:with-param name="mode" select="'absolute'"/>
                    </xsl:call-template>
                    <p/>
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="valueName" select="'errors'"/>
                        <xsl:with-param name="mode" select="'relative'"/>
                    </xsl:call-template>
                </div>
            </div>
        </div>

        <xsl:if test="count($elements) &gt; 0">
            <div class="charts">
                <xsl:for-each select="$elements">
                    <xsl:sort select="name" data-type="number"/>
                    <xsl:call-template name="timer-chart">
                        <xsl:with-param name="directory" select="$directory"/>
                    </xsl:call-template>
                </xsl:for-each>
            </div>
        </xsl:if>

    </xsl:template>

</xsl:stylesheet>
