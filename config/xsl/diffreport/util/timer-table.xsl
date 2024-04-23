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
                            <xsl:attribute name="id">sortByName</xsl:attribute>
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
                        <th colspan="4">Runtime [ms]</th>
                        <xsl:if test="$percentileCount &gt; 0">
	                        <th colspan="{$percentileCount}" class="colgroup1">Runtime Percentiles [ms]</th>
	                    </xsl:if>
                    </tr>
                    <tr>
                        <xsl:if test="$type = 'request'">
                            <th class="table-sortable:numeric" id="sortByCountDistinct">Distinct**</th>
                        </xsl:if>
                        <th class="table-sortable:numeric" id="sortByCountTotal">Total</th>
                        <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                        <th class="table-sortable:numeric" id="sortByCountPerMinute">1/min</th>
                        <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                        <th class="table-sortable:numeric colgroup1" id="sortByErrorsTotal">Total</th>
                        <xsl:if test="$type = 'transaction'">
                            <th class="table-sortable:numeric colgroup1"  id="sortByEventsTotal">Total</th>
                        </xsl:if>
                        <th class="table-sortable:numeric" title="The arithmetic mean of the data series." id="sortByRuntimeMean">Mean</th>
                        <th class="table-sortable:numeric" title="The smallest value of the data series." id="sortByRuntimeMin">Min.</th>
                        <th class="table-sortable:numeric" title="The largest value of the data series." id="sortByRuntimeMax">Max.</th>
                        <th class="table-sortable:numeric" title="The standard deviation of all data within this data series." id="sortByRuntimeDev">Dev.</th>
                        <xsl:for-each select="$summaryElement/percentiles/*">
	                        <th class="table-sortable:numeric colgroup1" title="The nth percentile of the data series.">
	                            <!-- <xsl:value-of select="translate(name(), 'p', 'P')"/> -->
                                <xsl:attribute name="id">
                                    <xsl:value-of select="concat('sortByPercentile', translate(name(), 'p', 'P'))" />
                                </xsl:attribute>
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
	                            <xsl:when test="$type = 'request'">
	                                <xsl:value-of select="11 + $percentileCount"/>
	                            </xsl:when>
	                            <xsl:when test="$type = 'transaction'">
	                                <xsl:value-of select="11 + $percentileCount"/>
	                            </xsl:when>
	                            <xsl:when test="$type = 'action'">
	                                <xsl:value-of select="10 + $percentileCount"/>
	                            </xsl:when>
	                            <xsl:otherwise>
	                                <xsl:value-of select="10 + $percentileCount"/>
	                            </xsl:otherwise>
	                        </xsl:choose>
                        </xsl:variable>
                        <tfoot>
                            <tr>
                                <td class="colgroup1"></td>
                                <xsl:choose>
			                        <xsl:when test="$type = 'request'">
			                            <td colspan="5"></td>
			                        </xsl:when>
			                        <xsl:otherwise>
			                            <td colspan="4"></td>
			                        </xsl:otherwise>
			                    </xsl:choose>
			                    <xsl:choose>
			                        <xsl:when test="$type = 'transaction'">
			                            <td colspan="2" class="colgroup1"></td>
			                        </xsl:when>
			                        <xsl:otherwise>
			                            <td class="colgroup1"></td>
			                        </xsl:otherwise>
			                    </xsl:choose>
                        		<td colspan="4"></td>
                        		<xsl:if test="$percentileCount &gt; 0">
	                        		<td colspan="{$percentileCount}" class="colgroup1"></td>
	                        	</xsl:if>
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
