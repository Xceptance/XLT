<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="custom-values">

        <div class="section" id="custom-values-summary">
            <xsl:call-template name="headline-custom-values-summary"/>

            <div class="content">
                <xsl:call-template name="description-custom-values-summary"/>

                <xsl:variable name="percentileCount" select="count(/testreport/testReportConfig/runtimePercentiles/string)"/>

                <table class="c-tab-content table-autosort:0">
                    <thead>
                        <tr>
                            <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                                Value Name
                                <br/>
                                <input class="filter" placeholder="Enter filter substrings" title=""/>
                                <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                            </th>
                            <th colspan="4">Count</th>
                            <th colspan="4" class="colgroup1">Stats</th>
                            <xsl:if test="$percentileCount &gt; 0">
                                <th colspan="{$percentileCount}">Percentiles</th>
                            </xsl:if>
                        </tr>
                        <tr>
                            <th class="table-sortable:numeric" id="sortByCountTotal">Total</th>
                            <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                            <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                            <th class="table-sortable:numeric" id="sortByCountPerDay">1/d*</th>
                            <th class="table-sortable:numeric colgroup1" title="The arithmetic mean." id="sortByStatsMean">Mean</th>
                            <th class="table-sortable:numeric colgroup1" id="sortByStatsMin">Min.</th>
                            <th class="table-sortable:numeric colgroup1" id="sortByStatsMax">Max.</th>
                            <th class="table-sortable:numeric colgroup1" title="The standard deviation." id="sortByStatsDev">Dev.</th>
                            <xsl:for-each select="/testreport/testReportConfig/runtimePercentiles/string">
                                <th class="table-sortable:numeric" title="The nth percentile of the data series.">
                                    <xsl:attribute name="id">
                                        <xsl:value-of select="concat('sortByPercentile', current())" />
                                    </xsl:attribute>
                                    <xsl:text>P</xsl:text>
                                    <xsl:value-of select="current()"/>
                                </th>
                            </xsl:for-each>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td class="colgroup1"></td>
                            <td class=""></td>
                            <td class=""></td>
                            <td class=""></td>
                            <td class=""></td>
                            <td class="colgroup1"></td>
                            <td class="colgroup1"></td>
                            <td class="colgroup1"></td>
                            <td class="colgroup1"></td>
                            <xsl:for-each select="/testreport/testReportConfig/runtimePercentiles/string">
                                <td class=""></td>
                            </xsl:for-each>
                        </tr>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <xsl:choose>
                        <xsl:when test="count(customValues/*) > 0">
                            <tbody>
                                <xsl:for-each select="customValues/*">

                                    <xsl:sort select="name"/>

                                    <xsl:variable name="encodedChartFilename">
                                        <xsl:call-template name="convertIllegalCharactersInFileName">
                                            <xsl:with-param name="filename" select="chartFilename"/>
                                        </xsl:call-template>
                                    </xsl:variable>

                                    <xsl:variable name="gid" select="generate-id(.)"/>

                                    <tr>
                                        <td class="key colgroup1">
                                            <a>
                                                <xsl:attribute name="href">#chart-<xsl:value-of select="$gid"/></xsl:attribute>
                                                <xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
                                                <xsl:attribute name="data-id">tableEntry-<xsl:value-of select="$gid"/></xsl:attribute>
                                                <xsl:value-of select="name"/>
                                            </a>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(count, '#,##0')"></xsl:value-of>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(countPerSecond, '#,##0')"></xsl:value-of>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(countPerHour, '#,##0')"></xsl:value-of>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(countPerDay, '#,##0')"></xsl:value-of>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of select="format-number(mean, '#,##0.###')"></xsl:value-of>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of select="format-number(min, '#,##0.###')"></xsl:value-of>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of select="format-number(max, '#,##0.###')"></xsl:value-of>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of select="format-number(standardDeviation, '#,##0.###')"></xsl:value-of>
                                        </td>
                                        <xsl:for-each select="percentiles/*">
                                            <td class="value number">
                                                <xsl:value-of select="format-number(current(), '#,##0.###')"></xsl:value-of>
                                            </td>
                                        </xsl:for-each>
                                    </tr>
                                </xsl:for-each>
                            </tbody>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="columnCount" select="9 + $percentileCount"/>
                            <tbody class="table-nosort">
                                <tr>
                                    <td colspan="{$columnCount}" class="no-data">No data available</td>
                                </tr>
                            </tbody>
                        </xsl:otherwise>
                    </xsl:choose>
                </table>

                <xsl:if test="count(customValues/*) &gt; 0">
                    <div class="charts">
                        <xsl:for-each select="customValues/*">

                            <xsl:sort select="name"/>

                            <xsl:if test="count(chartFilename) > 0">

                                <xsl:variable name="encodedChartFilename">
                                    <xsl:call-template name="convertIllegalCharactersInFileName">
                                        <xsl:with-param name="filename" select="chartFilename"/>
                                    </xsl:call-template>
                                </xsl:variable>

                                <xsl:variable name="gid" select="generate-id(.)"/>

                                <div class="chart-group tabs c-tabs no-print" data-name="{name}">
                                    <xsl:attribute name="id">chart-<xsl:value-of select="$gid"/></xsl:attribute>

                                    <ul class="c-tabs-nav">
                                        <li class="c-tabs-nav-link c-is-active">
                                            <a href="#Overview-{$gid}">Overview</a>
                                        </li>
                                        <li class="c-tabs-nav-link">
                                            <a href="#Averages-{$gid}">Averages</a>
                                        </li>
                                    </ul>

                                    <a href="#tableEntry-{$gid}" class="backlink">Back to Table</a>

                                    <div id="Overview-{$gid}" class="c-tab c-is-active">
                                        <div class="c-tab-content chart">
                                            <img>
                                                <xsl:attribute name="src">charts/customvalues/<xsl:value-of
                                                    select="$encodedChartFilename"/>.webp</xsl:attribute>
                                                <xsl:attribute name="alt">charts/customvalues/<xsl:value-of
                                                    select="$encodedChartFilename"/>.webp</xsl:attribute>
                                                <xsl:attribute name="loading">lazy</xsl:attribute>
                                            </img>
                                        </div>
                                    </div>

                                    <div id="Averages-{$gid}" class="c-tab">
                                        <div class="c-tab-content chart">
                                            <img>
                                                <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                                                <xsl:attribute name="alt">charts/customvalues/<xsl:value-of
                                                    select="$encodedChartFilename"/>_Average.webp</xsl:attribute>
                                            </img>
                                        </div>
                                    </div>
                                </div>

                                <div class="chart-group print">
                                    <h3>
                                        <xsl:value-of select="name"/>
                                    </h3>
                                    <div class="chart">
                                        <h5>Overview</h5>
                                        <img alt="charts/customvalues/{$encodedChartFilename}.webp"/>
                                    </div>
                                    <div class="chart">
                                        <h5>Averages</h5>
                                        <img alt="charts/customvalues/{$encodedChartFilename}_Average.webp"/>
                                    </div>
                                </div>
                            </xsl:if>
                        </xsl:for-each>
                    </div>
                </xsl:if>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
