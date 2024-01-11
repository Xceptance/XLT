<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="timer-section">
        <xsl:param name="elements"/>
        <xsl:param name="summaryElement"/>
        <xsl:param name="tableRowHeader"/>
        <xsl:param name="type"/>
        
        <xsl:variable name="percentileCount" select="count(/testreport/testReport1/runtimePercentiles/string)"/>
        
        <xsl:choose>
            <xsl:when test="$type = 'request'">
            <div id="tabletabbies" class="c-tabs">
                    <ul class="c-tabs-nav">
                        <li class="c-tabs-nav-link c-is-active">
                            <a href="#Overview">Overview</a>
                        </li>
                        <li class="c-tabs-nav-link">
                            <a href="#Bandwidth">Bandwidth</a>
                        </li>
                    </ul>

                    <div id="Overview" class="c-tab c-is-active">
                        <h4 class="print">Overview</h4>
                        <xsl:call-template name="timer-table">
                            <xsl:with-param name="elements" select="$elements"/>
                            <xsl:with-param name="summaryElement" select="$summaryElement"/>
                            <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                            <xsl:with-param name="type" select="$type"/>
                        </xsl:call-template>
                    </div>
                        
                    <div id="Bandwidth" class="c-tab">
                        <h4 class="print">Overview</h4>
                        <table class="c-tab-content table-autosort:0">
                            <thead>
                                <tr>
                                    <th rowspan="2" class="table-sortable:alphanumeric colgroup1">
                                        <xsl:value-of select="$tableRowHeader"/>
                                        <br/>
                                        <input class="filter" placeholder="Enter filter substrings" title=""/>
                                        <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                    </th>
                                    <th colspan="8">Bytes Sent</th>
                                    <th colspan="8" class="colgroup1">Bytes Received</th>
                                </tr>
                                <tr>
                                    <th class="table-sortable:numeric">Total</th>
                                    <th class="table-sortable:numeric">1/s</th>
                                    <th class="table-sortable:numeric">1/min</th>
                                    <th class="table-sortable:numeric">1/h</th>
                                    <th class="table-sortable:numeric">1/d</th>
                                    <th class="table-sortable:numeric" title="The arithmetic mean.">Mean</th>
                                    <th class="table-sortable:numeric">Min.</th>
                                    <th class="table-sortable:numeric">Max.</th>

                                    <th class="table-sortable:numeric colgroup1">Total</th>
                                    <th class="table-sortable:numeric colgroup1">1/s</th>
                                    <th class="table-sortable:numeric colgroup1">1/min</th>
                                    <th class="table-sortable:numeric colgroup1">1/h</th>
                                    <th class="table-sortable:numeric colgroup1">1/d</th>
                                    <th class="table-sortable:numeric colgroup1" title="The arithmetic mean.">Mean</th>
                                    <th class="table-sortable:numeric colgroup1">Min.</th>
                                    <th class="table-sortable:numeric colgroup1">Max.</th>
                                </tr>
                            </thead>
                            <xsl:variable name="count" select="count($elements)"/>
                            <xsl:choose>
                                <xsl:when test="$count > 0">
                                    <tfoot>
                                        <xsl:for-each select="$summaryElement">
                                            <tr class="totals">
                                                <xsl:call-template name="create-totals-td">
                                                    <xsl:with-param name="rows-in-table" select="$count"/>
                                                    <xsl:with-param name="class" select="'key colgroup1'"/>
                                                </xsl:call-template>

                                               	<xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/totalCount"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerSecond"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>                                                
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerMinute"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerHour"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerDay"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/mean"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/min"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/max"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>

                                                <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/totalCount"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerSecond"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerMinute"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerHour"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerDay"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/mean"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/min"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/max"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                            </tr>
                                        </xsl:for-each>
                                        <xsl:call-template name="filtered-footer-row"/>
                                    </tfoot>
                                    <tbody>
                                        <xsl:for-each select="$elements">
                                            <xsl:sort select="name" data-type="number"/>

                                            <xsl:variable name="gid" select="generate-id(.)"/>

                                            <tr>
                                                <td class="key colgroup1 forcewordbreak">
  		                                            <xsl:value-of select="name"/>
                                                </td>

                                                <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/totalCount"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerSecond"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerMinute"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerHour"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/countPerDay"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/mean"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/min"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesSent/max"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>

                                                <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/totalCount"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerSecond"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerMinute"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerHour"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/countPerDay"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/mean"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/min"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                                   <xsl:call-template name="timer-cell">
										            <xsl:with-param name="node" select="bytesReceived/max"/>
										            <xsl:with-param name="format" select="'#,##0'"/>
										        </xsl:call-template>
                                            </tr>
                                        </xsl:for-each>
                                    </tbody>
                                </xsl:when>
                                <xsl:otherwise>
                                    <tfoot>
                                        <tr>
                                            <td class="colgroup1"></td>

                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>

                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                            <td class="colgroup1"></td>
                                        </tr>
                                        <xsl:call-template name="filtered-footer-row"/>
                                    </tfoot>
                                    <tbody class="table-nosort">
                                        <tr>
                                            <td colspan="17" class="no-data">No data available</td>
                                        </tr>
                                    </tbody>
                                </xsl:otherwise>
                            </xsl:choose>
                        </table>
                    </div>  
             </div> 
            </xsl:when>
            <xsl:otherwise>
        <div class="data">
                    <xsl:call-template name="timer-table">
                        <xsl:with-param name="elements" select="$elements"/>
                        <xsl:with-param name="summaryElement" select="$summaryElement"/>
                        <xsl:with-param name="tableRowHeader" select="$tableRowHeader"/>
                        <xsl:with-param name="type" select="$type"/>
                    </xsl:call-template>
        </div>
        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
