<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="agents">
        <xsl:param name="rootNode"/>

        <div class="section" id="agents">
            <xsl:call-template name="headline-agents"/>

            <div class="content">
                <xsl:call-template name="description-agents"/>

                <div class="charts overview">
                    <xsl:call-template name="agent-chart">
                        <xsl:with-param name="directory" select="'All Agents'" />
                        <xsl:with-param name="isSummary" select="'true'" />
                    </xsl:call-template>
		</div>

                <div class="data">
                    <table class="c-tab-content table-autosort:0">
                        <thead>
                            <tr>
                                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                                    Agent Name
                                    <br/>
                                    <input class="filter" placeholder="Enter filter substrings" title=""/>
                                    <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                </th>
                                <th colspan="3">Transactions</th>
                                <th colspan="2" class="colgroup1">Total CPU [%]</th>
                                <th colspan="2">Agent CPU [%]</th>
                                <th colspan="3" class="colgroup1">Minor GC</th>
                                <th colspan="3">Full GC</th>
                            </tr>
                            <tr>
                                <th class="table-sortable:numeric" id="sortByTransactionsTotal">Total</th>
                                <th class="table-sortable:numeric" id="sortByTransactionsErrors">Errors</th>
                                <th class="table-sortable:numeric" id="sortByTransactionsPercentage">%</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByTotalCPUMean">Mean</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByTotalCPUMax">Max.</th>
                                <th class="table-sortable:numeric" id="sortByAgentCPUMean">Mean</th>
                                <th class="table-sortable:numeric" id="sortByAgentCPUMax">Max.</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByMinorGCCount">Count</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByMinorGCTime">Time [ms]</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByMinorGCCPU">CPU [%]</th>
                                <th class="table-sortable:numeric" id="sortByFullGCCount">Count</th>
                                <th class="table-sortable:numeric" id="sortByFullGCTime">Time [ms]</th>
                                <th class="table-sortable:numeric" id="sortByFullGCCPU">CPU [%]</th>
                            </tr>
                        </thead>
                        <xsl:variable name="count" select="count($rootNode/agent)"/>
                        <xsl:choose>
                            <xsl:when test="$count > 0">
                                <tfoot>
                                    <xsl:variable name="totalTransactions">
                                        <xsl:value-of select="sum($rootNode/agent/transactions)" />
                                    </xsl:variable>
                                    <xsl:variable name="totalTransactionErrors">
                                        <xsl:value-of select="sum($rootNode/agent/transactionErrors)" />
                                    </xsl:variable>
                                    <tr class="totals">
                                        <xsl:call-template name="create-totals-td">
                                            <xsl:with-param name="rows-in-table" select="$count"/>
                                            <xsl:with-param name="class" select="'key colgroup1'"/>
                                        </xsl:call-template>

                                        <td class="value number">
                                            <xsl:value-of select="format-number($totalTransactions, '#,##0')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:if test="$totalTransactionErrors &gt; 0">
                                                <xsl:attribute name="class">value number error</xsl:attribute>
                                            </xsl:if>
                                            <xsl:value-of select="format-number($totalTransactionErrors, '#,##0')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:if test="$totalTransactionErrors &gt; 0">
                                                <xsl:attribute name="class">value number error</xsl:attribute>
                                            </xsl:if>
                                            <xsl:variable name="totalTransactionErrorPercentage">
                                                <xsl:call-template name="percentage">
                                                    <xsl:with-param name="n1" select="$totalTransactions" />
                                                    <xsl:with-param name="n2" select="$totalTransactionErrors" />
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:value-of select="format-number($totalTransactionErrorPercentage, '#,##0.00')" />
                                            <xsl:text>%</xsl:text>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of
                                                select="format-number(sum($rootNode/agent/totalCpuUsage/mean) div $count, '#,##0.00')"/>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:variable name="maxTotalCpu">
                                                <xsl:call-template name="max">
                                                    <xsl:with-param name="seq" select="$rootNode/agent/totalCpuUsage/max"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:value-of select="format-number($maxTotalCpu, '#,##0.00')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of
                                                select="format-number(sum($rootNode/agent/cpuUsage/mean) div $count, '#,##0.00')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:variable name="maxCpu">
                                                <xsl:call-template name="max">
                                                    <xsl:with-param name="seq" select="$rootNode/agent/cpuUsage/max"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:value-of select="format-number($maxCpu, '#,##0.00')"/>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of select="format-number(sum($rootNode/agent/minorGcCount) div $count, '#,##0')"/>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of select="format-number(sum($rootNode/agent/minorGcTime) div $count, '#,##0')"/>
                                        </td>
                                        <td class="value number colgroup1">
                                            <xsl:value-of
                                                select="format-number(sum($rootNode/agent/minorGcCpuUsage) div $count, '#,##0.00')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(sum($rootNode/agent/fullGcCount) div $count, '#,##0')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(sum($rootNode/agent/fullGcTime) div $count, '#,##0')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of
                                                select="format-number(sum($rootNode/agent/fullGcCpuUsage) div $count, '#,##0.00')"/>
                                        </td>
                                    </tr>

                                    <xsl:call-template name="filtered-footer-row"/>
                                </tfoot>
                                <tbody>
                                    <xsl:for-each select="$rootNode/agent">
                                        <xsl:sort select="name"/>

                                        <xsl:variable name="gid" select="generate-id(.)"/>

                                        <tr>
                                            <td class="value text colgroup1">
                                                <a>
                                                    <xsl:attribute name="href">#chart-<xsl:value-of select="$gid"/></xsl:attribute>
                                                    <xsl:attribute name="data-id">tableEntry-<xsl:value-of
                                                        select="$gid"/></xsl:attribute>
                                                    <xsl:value-of select="name"/>
                                                </a>
                                            </td>
                                            <td class="value number">
                                                <xsl:value-of select="format-number(transactions, '#,##0')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:if test="transactionErrors &gt; 0">
                                                    <xsl:attribute name="class">value number error</xsl:attribute>
                                                </xsl:if>
                                                <xsl:value-of select="format-number(transactionErrors, '#,##0')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:if test="transactionErrors &gt; 0">
                                                    <xsl:attribute name="class">value number error</xsl:attribute>
                                                </xsl:if>
                                                <xsl:variable name="transactionErrorPercentage">
                                                    <xsl:call-template name="percentage">
                                                        <xsl:with-param name="n1" select="transactions" />
                                                        <xsl:with-param name="n2" select="transactionErrors" />
                                                    </xsl:call-template>
                                                </xsl:variable>
                                                <xsl:value-of select="format-number($transactionErrorPercentage, '#,##0.00')" />
                                                <xsl:text>%</xsl:text>
                                            </td>
                                            <td class="value number colgroup1">
                                                <xsl:value-of select="format-number(totalCpuUsage/mean, '#,##0.00')"/>
                                            </td>
                                            <td class="value number colgroup1">
                                                <xsl:value-of select="format-number(totalCpuUsage/max, '#,##0.00')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:value-of select="format-number(cpuUsage/mean, '#,##0.00')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:value-of select="format-number(cpuUsage/max, '#,##0.00')"/>
                                            </td>
                                            <td class="value number colgroup1">
                                                <xsl:value-of select="format-number(minorGcCount, '#,##0')"/>
                                            </td>
                                            <td class="value number colgroup1">
                                                <xsl:value-of select="format-number(minorGcTime, '#,##0')"/>
                                            </td>
                                            <td class="value number colgroup1">
                                                <xsl:value-of select="format-number(minorGcCpuUsage, '#,##0.00')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:value-of select="format-number(fullGcCount, '#,##0')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:value-of select="format-number(fullGcTime, '#,##0')"/>
                                            </td>
                                            <td class="value number">
                                                <xsl:value-of select="format-number(fullGcCpuUsage, '#,##0.00')"/>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </tbody>
                            </xsl:when>
                            <xsl:otherwise>
                                <tfoot>
                                    <tr>
                                        <td class="colgroup1"></td>
                                        <td colspan="3"></td>
                                        <td colspan="2" class="colgroup1"></td>
                                        <td colspan="2"></td>
                                        <td colspan="3" class="colgroup1"></td>
                                        <td colspan="3"></td>
                                    </tr>
                                </tfoot>
                                <tbody>
                                    <tr>
                                        <td class="no-data" colspan="14">No data available</td>
                                    </tr>
                                </tbody>
                            </xsl:otherwise>
                        </xsl:choose>
                    </table>
                </div>

                <xsl:if test="count($rootNode/agent) &gt; 0">
                    <h3 class="no-print">
                        Individual Agents
                    </h3>
                    <div class="charts">
                        <xsl:for-each select="$rootNode/agent/name[.!='']/..">
                            <xsl:sort select="name"/>

                            <xsl:call-template name="agent-chart">
                                <xsl:with-param name="directory" select="name" />
                                <xsl:with-param name="isSummary" select="'false'" />
                            </xsl:call-template>
                        </xsl:for-each>
                    </div>
                </xsl:if>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
