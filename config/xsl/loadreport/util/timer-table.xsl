<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Emits the requested number of "<td></td>" pairs. -->
    <xsl:template name="make-tds">
        <xsl:param name="i" select="'0'"/>
        <xsl:param name="count"/>
        <xsl:if test="$i &lt; $count">
            <td></td>
            <xsl:call-template name="make-tds">
                <xsl:with-param name="i" select="$i + 1"/>
                <xsl:with-param name="count" select="$count"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="timer-table">
        <xsl:param name="elements"/>
        <xsl:param name="summaryElement"/>
        <xsl:param name="tableRowHeader"/>
        <xsl:param name="runtimeIntervalsNode"/>
        <xsl:param name="type"/>

        <xsl:variable name="percentileCount" select="count(/testreport/testReportConfig/runtimePercentiles/string)"/>
        <xsl:variable name="intervalCount" select="count(/testreport/testReportConfig/runtimeIntervals/interval)"/>

        <table class="c-tab-content table-autosort:0">
            <thead>
                <tr>
                    <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                        <xsl:value-of select="$tableRowHeader"/>
                        <br/>
                        <form>
                            <input class="filter" placeholder="Enter filter substrings" title=""/>
                            <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                        </form>
                    </th>
                    <xsl:if test="$type = 'transaction' or $type = 'action' or $type = 'request'">
                        <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByLabel">Label</th>
                    </xsl:if>
                    <xsl:choose>
                        <xsl:when test="$type = 'request'">
                            <th colspan="5">Count</th>
                        </xsl:when>
                        <xsl:otherwise>
                            <th colspan="4">Count</th>
                        </xsl:otherwise>
                    </xsl:choose>
                    <th colspan="2" class="colgroup1">Errors</th>
                    <xsl:if test="$type = 'transaction'">
                        <th class="colgroup1">Events</th>
                    </xsl:if>
                    <th colspan="4">Runtime [ms]</th>
                    <xsl:if test="$percentileCount &gt; 0">
                        <th colspan="{$percentileCount}" class="colgroup1">Runtime Percentiles [ms]</th>
                    </xsl:if>
                    <xsl:if test="$type = 'action'">
                        <th rowspan="2" class="table-sortable:alphanumeric" id="sortByApdex">Apdex</th>
                    </xsl:if>
                    <xsl:if test="$type = 'request' and $intervalCount &gt; 0">
                        <th colspan="{count($runtimeIntervalsNode/interval)}">Runtime Segmentation [ms]</th>
                    </xsl:if>
                </tr>
                <tr>
                    <th class="table-sortable:numeric" id="sortByCountTotal">Total</th>

                    <xsl:choose>
                        <xsl:when test="$type = 'request'">
                            <th class="table-sortable:numeric" id="sortByCountDistinct">Distinct**</th>
                            <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                            <th class="table-sortable:numeric" id="sortByCountPerMinute">1/min</th>
                            <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                        </xsl:when>
                        <xsl:otherwise>
                            <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                            <th class="table-sortable:numeric" id="sortByCountPerMinute">1/min</th>
                            <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                        </xsl:otherwise>
                    </xsl:choose>

                    <th class="table-sortable:numeric colgroup1" id="sortByErrorsTotal">Total</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByErrorsPercentage">%</th>
                    <xsl:if test="$type = 'transaction'">
                        <th class="table-sortable:numeric colgroup1" id="sortByEventsTotal">Total</th>
                    </xsl:if>
                    <th class="table-sortable:numeric" title="The arithmetic mean of the data series." id="sortByRuntimeMean">Mean</th>
                    <th class="table-sortable:numeric" title="The smallest value of the data series." id="sortByRuntimeMin">Min.</th>
                    <th class="table-sortable:numeric" title="The largest value of the data series." id="sortByRuntimeMax">Max.</th>
                    <th class="table-sortable:numeric" title="The standard deviation of all data within this data series." id="sortByRuntimeDev">Dev.</th>
                    <xsl:for-each select="/testreport/testReportConfig/runtimePercentiles/string">
                        <th class="table-sortable:numeric colgroup1" title="The nth percentile of the data series.">
                            <xsl:attribute name="id">
                              <xsl:value-of select="concat('sortByPercentile', current())" />
                            </xsl:attribute>
                            <xsl:text>P</xsl:text>
                            <xsl:value-of select="current()"/>
                        </th>
                    </xsl:for-each>
                    <xsl:if test="$type = 'request' and $intervalCount &gt; 0">
                        <xsl:choose>
                            <xsl:when test="count($runtimeIntervalsNode/interval) &gt; 0">
                                <xsl:for-each select="$runtimeIntervalsNode/interval">
                                    <th class="table-sortable:numeric"
                                        title="A data segment and the percentage of data from the time series that is located within.">
                                        <xsl:attribute name="id">
                                            <xsl:value-of select="concat(concat(concat('sortBySegmentFrom', @from), 'To'), @to)" />
                                        </xsl:attribute>
                                        <xsl:choose>
                                            <xsl:when test="position() &lt; count($runtimeIntervalsNode/interval)">
                                                <xsl:text disable-output-escaping="yes">&amp;le;</xsl:text>
                                                <xsl:value-of select="format-number(@to, '#,##0')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:text disable-output-escaping="yes">&amp;gt;</xsl:text>
                                                <xsl:value-of select="format-number(@from, '#,##0')"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </th>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <th></th>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </tr>
            </thead>
            <xsl:variable name="count" select="count($elements)"/>
            <xsl:choose>
                <xsl:when test="$count > 0">
                    <tfoot>
                        <xsl:for-each select="$summaryElement">
                            <!-- There is only one matching node. -->
                            <xsl:call-template name="timer-summary-row">
                                <xsl:with-param name="type" select="$type"/>
                                <xsl:with-param name="rows-in-table" select="$count"/>
                            </xsl:call-template>
                        </xsl:for-each>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <tbody>
                        <xsl:for-each select="$elements">
                            <xsl:sort select="name" data-type="number"/>
                            <xsl:call-template name="timer-row">
                                <xsl:with-param name="type" select="$type"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </tbody>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="columnCount">
                        <xsl:choose>
                            <xsl:when test="$type = 'request'">
                                <xsl:value-of select="13 + $percentileCount + count($runtimeIntervalsNode/interval)"/>
                            </xsl:when>
                            <xsl:when test="$type = 'transaction'">
                                <xsl:value-of select="13 + $percentileCount"/>
                            </xsl:when>
                            <xsl:when test="$type = 'action'">
                                <xsl:value-of select="13 + $percentileCount"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="11 + $percentileCount"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <tfoot>
                        <tr>
                            <xsl:call-template name="make-tds">
                                <xsl:with-param name="count" select="$columnCount"/>
                            </xsl:call-template>
                        </tr>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <tbody class="table-nosort">
                        <tr>
                            <td class="no-data" colspan="{$columnCount}">
                                No data available
                            </td>
                        </tr>
                    </tbody>
                </xsl:otherwise>
            </xsl:choose>
        </table>

    </xsl:template>

</xsl:stylesheet>
