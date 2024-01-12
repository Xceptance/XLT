<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="timer-table">
        <xsl:param name="elements"/>
        <xsl:param name="summaryElement"/>
        <xsl:param name="tableRowHeader"/>
        <xsl:param name="type"/>
        
        <xsl:variable name="percentileCount" select="count($summaryElement/percentiles/*)"/>
        
            <table class="table-autosort:0">
                <thead>
                    <tr>
                        <th rowspan="2" class="table-sortable:alphanumeric colgroup1">
                            <xsl:value-of select="$tableRowHeader"/>
                            <br/>
                            <input class="filter" placeholder="Enter filter substrings" title=""/>
                            <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                        </th>
                        <xsl:choose>
	                        <xsl:when test="$type = 'request'">
	                            <th colspan="5">Count</th>
	                        </xsl:when>
	                        <xsl:otherwise>
	                            <th colspan="4">Count</th>
	                        </xsl:otherwise>
	                    </xsl:choose>
                        <th class="colgroup1">Errors</th>
                        <xsl:if test="$type = 'transaction'">
                            <th class="colgroup1">Events</th>
                        </xsl:if>
                        <th colspan="5">Runtime [ms]</th>
                        <xsl:if test="$percentileCount &gt; 0">
	                        <th colspan="{$percentileCount}" class="colgroup1">Runtime Percentiles [ms]</th>
	                    </xsl:if>
                    </tr>
                    <tr>
                        <xsl:if test="$type = 'request'">
                            <th class="table-sortable:numeric">Distinct**</th>
                        </xsl:if>
                        <th class="table-sortable:numeric">Total</th>
                        <th class="table-sortable:numeric">1/s</th>
                        <th class="table-sortable:numeric">1/min</th>
                        <th class="table-sortable:numeric">1/h*</th>
                        <th class="table-sortable:numeric colgroup1">Total</th>
                        <xsl:if test="$type = 'transaction'">
                            <th class="table-sortable:numeric colgroup1">Total</th>
                        </xsl:if>
                        <th class="table-sortable:numeric" title="The median of the data series.">Med.</th>
                        <th class="table-sortable:numeric" title="The arithmetic mean of the data series.">Mean</th>
                        <th class="table-sortable:numeric" title="The smallest value of the data series.">Min.</th>
                        <th class="table-sortable:numeric" title="The largest value of the data series.">Max.</th>
                        <th class="table-sortable:numeric" title="The standard deviation of all data within this data series.">Dev.</th>
                        <xsl:for-each select="$summaryElement/percentiles/*">
	                        <th class="table-sortable:numeric colgroup1" title="The nth percentile of the data series.">
	                            <xsl:value-of select="translate(name(), 'p', 'P')"/>
	                        </th>
	                    </xsl:for-each>
                    </tr>
                </thead>
                <xsl:variable name="count" select="count($elements)" />
                <xsl:choose>
                    <xsl:when test="$count > 0">
                        <tfoot>
                            <xsl:for-each select="$summaryElement">
                                <xsl:call-template name="timer-summary-row">
                                    <xsl:with-param name="type" select="$type"/>
                                    <xsl:with-param name="rows-in-table" select="$count"/>
                                </xsl:call-template>
                            </xsl:for-each>
                            <xsl:call-template name="filtered-footer-row" />
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
                        <xsl:variable name="columns">
                            <xsl:choose>
                                <xsl:when test="$type = 'transaction'">
                                    13
                                </xsl:when>
                                <xsl:otherwise>
                                    12
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <tfoot>
                            <tr>
                                <td colspan="{$columns}"></td>
                            </tr>
                            <xsl:call-template name="filtered-footer-row" />
                        </tfoot>
                        <tbody class="table-nosort">
                            <tr>
                                <td class="no-data" colspan="{$columns}">
                                    No data available
                                </td>
                            </tr>
                        </tbody>
                    </xsl:otherwise>
                </xsl:choose>
            </table>
    </xsl:template>

</xsl:stylesheet>
