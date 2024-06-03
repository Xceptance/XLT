<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="custom-values">

		<div class="section" id="custom-values-summary">
			<xsl:call-template name="headline-custom-values-summary" />

            <div class="content">
				<xsl:call-template name="description-custom-values-summary" />
				
				<xsl:variable name="percentileCount" select="count(/testreport/summary/requests/percentiles/*)"/>

				<table class="c-tab-content table-autosort:0">
                    <thead>
                        <tr>
                            <th rowspan="2" class="table-sortable:alphanumeric colgroup1">
                                <xsl:attribute name="id">sortByName</xsl:attribute>
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
                            <xsl:for-each select="/testreport/summary/requests/percentiles/*">
	                        <th class="table-sortable:numeric" title="The nth percentile of the data series.">
                                <xsl:attribute name="id">
                                    <xsl:value-of select="concat('sortByPercentile', translate(name(), 'p', 'P'))" />
                                </xsl:attribute>
                                <xsl:value-of select="translate(name(), 'p', 'P')"/>
	                        </th>
	                    	</xsl:for-each>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr class="totals">
                            <xsl:call-template name="create-totals-td">
                                <xsl:with-param name="rows-in-table" select="count(customValues/*)"/>
                                <xsl:with-param name="class" select="'key'"/>
                            </xsl:call-template>
                            <td class=""></td>
                            <td class=""></td>
                            <td class=""></td>
                            <td class=""></td>
                            <td class="colgroup1"></td>
                            <td class="colgroup1"></td>
                            <td class="colgroup1"></td>
                            <td class="colgroup1"></td>
                            <xsl:for-each select="/testreport/summary/requests/percentiles/*">
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

                                    <xsl:variable name="gid" select="generate-id(.)"/>

                                    <tr>
                                        <td class="key colgroup1">
                                            <a>
                                                <xsl:attribute name="title"><xsl:value-of select="description"/></xsl:attribute>
                                                <xsl:attribute name="data-id">tableEntry-<xsl:value-of select="$gid"/></xsl:attribute>
                                                <xsl:value-of select="name"/>
                                            </a>
                                        </td>
                                        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="count"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>
								        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="countPerSecond"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>
                                        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="countPerHour"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>
										<xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="countPerDay"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>
								        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="mean"/>
								            <xsl:with-param name="format" select="'#,##0.###'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>
								        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="min"/>
								            <xsl:with-param name="format" select="'#,##0.###'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>	
								        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="max"/>
								            <xsl:with-param name="format" select="'#,##0.###'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>
								        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="standardDeviation"/>
								            <xsl:with-param name="format" select="'#,##0.###'"/>
								            <xsl:with-param name="neutral" select="true()"/>
								        </xsl:call-template>							        
                                        <xsl:for-each select="percentiles/*">
                                        	<xsl:call-template name="timer-cell">
									            <xsl:with-param name="node" select="current()"/>
									            <xsl:with-param name="format" select="'#,##0.###'"/>
									            <xsl:with-param name="neutral" select="true()"/>
									        </xsl:call-template>
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
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>