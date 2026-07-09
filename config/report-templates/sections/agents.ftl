<#import "../util/agent-chart.ftl" as agent_chart>
<#import "descriptions.ftl" as descriptions>
<#import "/common/sections/javascript.ftl" as js>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/common/util/filtered-footer-row.ftl" as filtered_footer>
<#import "/common/util/format.ftl" as format>

<#macro agents rootNode summaryNode>
    <div class="section" id="agents">
        <@descriptions.headline_agents />

        <div class="content">
            <@descriptions.description_agents />

            <div class="charts overview">
                <@agent_chart.agent_chart directory="All Agents" isSummary=true gid="agent_summary" />
            </div>

            <div class="data">
                <table class="c-tab-content table-autosort:0">
                    <thead>
                        <tr>
                            <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                                Agent Name
                                <br/>
                                <input class="filter" placeholder="Enter filter substrings" title="" data-filter-id="filterByName" data-col-index="0"/>
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
                    <#if rootNode.agent?has_content>
                        <#assign count = rootNode.agent?size>
                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=count class="key colgroup1" />

                                <td class="value number">
                                    <#assign transTotal = 0>
                                    <#assign hasNaN = false>
                                    <#list rootNode.agent as agent>
                                        <#if !agent.transactions?has_content || agent.transactions == "NaN" || agent.transactions == "nan">
                                            <#assign hasNaN = true>
                                        <#else>
                                            <#assign transTotal = transTotal + agent.transactions?number>
                                        </#if>
                                    </#list>
                                    <#if hasNaN || transTotal?string == "NaN">nan<#else>${transTotal?string(",##0")}</#if>
                                </td>

                                    <#assign errorsTotal = 0>
                                    <#assign hasNaNErrors = false>
                                    <#list rootNode.agent as agent>
                                         <#assign tErrs = (agent.transactionErrors?has_content)?then(agent.transactionErrors, "nan")>
                                         <#if tErrs?string == "NaN" || tErrs?string == "nan">
                                             <#assign hasNaNErrors = true>
                                         <#else>
                                             <#assign errorsTotal = errorsTotal + tErrs?number>
                                         </#if>
                                    </#list>
                                <td class="value number<#if (!hasNaNErrors && errorsTotal > 0)> error</#if>">
                                    <#if hasNaNErrors || errorsTotal?string == "NaN">
                                        nan
                                    <#else>
                                        ${format.formatNumber(errorsTotal)}
                                    </#if>
                                </td>
                                <td class="value number<#if (!hasNaNErrors && errorsTotal > 0)> error</#if>">
                                    <#if hasNaN || hasNaNErrors || transTotal?string == "NaN" || errorsTotal?string == "NaN">
                                        nan%
                                    <#else>
                                        <#if (transTotal > 0)>
                                            <#assign errorPct = (errorsTotal / transTotal) * 100>
                                        <#else>
                                            <#assign errorPct = 0>
                                        </#if>
                                        ${format.formatPercentageValue(errorPct)}
                                    </#if>
                                </td>
                                <td class="value number colgroup1">
                                    <#assign sumMean = 0>
                                    <#assign hasNaN = false>
                                    <#list rootNode.agent as agent>
                                        <#if !agent.totalCpuUsage.mean?has_content>
                                            <#assign val = 0>
                                            <#assign sumMean = sumMean + val>
                                        <#elseif agent.totalCpuUsage.mean == "NaN" || agent.totalCpuUsage.mean == "nan">
                                          <#assign hasNaN = true>
                                        <#else>
                                          <#assign val = agent.totalCpuUsage.mean?number>
                                          <#assign sumMean = sumMean + val>
                                        </#if>
                                    </#list>
                                    <#if hasNaN>nan<#else>${(sumMean / count)?string("0.00")}</#if>
                                </td>
                                <td class="value number colgroup1">
                                    <#assign maxTotalCpu = 0>
                                    <#assign hasNaN = false>
                                    <#list rootNode.agent as agent>
                                        <#if !agent.totalCpuUsage.max?has_content || agent.totalCpuUsage.max == "NaN" || agent.totalCpuUsage.max == "nan">
                                          <#assign hasNaN = true>
                                        <#else>
                                          <#assign val = agent.totalCpuUsage.max?number>
                                          <#if (val > maxTotalCpu)>
                                              <#assign maxTotalCpu = val>
                                          </#if>
                                        </#if>
                                    </#list>
                                    <#if hasNaN>nan<#else>${(maxTotalCpu)?string("0.00")}</#if>
                                </td>
                                <td class="value number">
                                    <#assign sumAgentMean = 0>
                                    <#assign hasNaN = false>
                                    <#list rootNode.agent as agent>
                                        <#if !agent.cpuUsage.mean?has_content>
                                            <#assign val = 0>
                                            <#assign sumAgentMean = sumAgentMean + val>
                                        <#elseif agent.cpuUsage.mean == "NaN" || agent.cpuUsage.mean == "nan">
                                          <#assign hasNaN = true>
                                        <#else>
                                          <#assign val = agent.cpuUsage.mean?number>
                                          <#assign sumAgentMean = sumAgentMean + val>
                                        </#if>
                                    </#list>
                                    <#if hasNaN>nan<#else>${(sumAgentMean / count)?string("0.00")}</#if>
                                </td>
                                <td class="value number">
                                    <#assign maxAgentCpu = 0>
                                    <#assign hasNaN = false>
                                    <#list rootNode.agent as agent>
                                        <#if !agent.cpuUsage.max?has_content || agent.cpuUsage.max == "NaN" || agent.cpuUsage.max == "nan">
                                          <#assign hasNaN = true>
                                        <#else>
                                          <#assign val = agent.cpuUsage.max?number>
                                          <#if (val > maxAgentCpu)>
                                              <#assign maxAgentCpu = val>
                                          </#if>
                                        </#if>
                                    </#list>
                                    <#if hasNaN>nan<#else>${(maxAgentCpu)?string("0.00")}</#if>
                                </td>
                                <td class="value number colgroup1">
                                    <#assign sumMinorGcCount = 0>
                                    <#list rootNode.agent as agent>
                                        <#assign val = (agent.minorGcCount?has_content)?then(agent.minorGcCount?number, 0)>
                                        <#assign sumMinorGcCount = sumMinorGcCount + val>
                                    </#list>
                                    ${format.formatNumber(sumMinorGcCount / count)}
                                </td>
                                <td class="value number colgroup1">
                                    <#assign sumMinorGcTime = 0>
                                    <#list rootNode.agent as agent>
                                        <#assign val = (agent.minorGcTime?has_content)?then(agent.minorGcTime?number, 0)>
                                        <#assign sumMinorGcTime = sumMinorGcTime + val>
                                    </#list>
                                    ${format.formatNumber(sumMinorGcTime / count)}
                                </td>
                                <td class="value number colgroup1">
                                    <#assign sumMinorGcCpu = 0>
                                    <#list rootNode.agent as agent>
                                        <#assign val = (agent.minorGcCpuUsage?has_content)?then(agent.minorGcCpuUsage?number, 0)>
                                        <#assign sumMinorGcCpu = sumMinorGcCpu + val>
                                    </#list>
                                    ${(sumMinorGcCpu / count)?string("0.00")}
                                </td>
                                <td class="value number">
                                    <#assign sumFullGcCount = 0>
                                    <#list rootNode.agent as agent>
                                        <#assign val = (agent.fullGcCount?has_content)?then(agent.fullGcCount?number, 0)>
                                        <#assign sumFullGcCount = sumFullGcCount + val>
                                    </#list>
                                    ${format.formatNumber(sumFullGcCount / count)}
                                </td>
                                <td class="value number">
                                    <#assign sumFullGcTime = 0>
                                    <#list rootNode.agent as agent>
                                        <#assign val = (agent.fullGcTime?has_content)?then(agent.fullGcTime?number, 0)>
                                        <#assign sumFullGcTime = sumFullGcTime + val>
                                    </#list>
                                    ${format.formatNumber(sumFullGcTime / count)}
                                </td>
                                <td class="value number">
                                    <#assign sumFullGcCpu = 0>
                                    <#list rootNode.agent as agent>
                                        <#assign val = (agent.fullGcCpuUsage?has_content)?then(agent.fullGcCpuUsage?number, 0)>
                                        <#assign sumFullGcCpu = sumFullGcCpu + val>
                                    </#list>
                                    ${(sumFullGcCpu / count)?string("0.00")}
                                </td>
                            </tr>
                            <@filtered_footer.filtered_footer_row />
                        </tfoot>
                        <tbody>
                            <#list rootNode.agent?sort_by("name") as agent>
                                <#assign gid = "agent_" + agent_index>
                                
                                <tr>
                                    <td class="value text colgroup1">
                                        <a href="#chart-${gid}" data-id="tableEntry-${gid}">${agent.name}</a>
                                    </td>
                                    <td class="value number">
                                        <#assign txCount = (agent.transactions?has_content)?then(agent.transactions, "nan")>
                                        <#if txCount?string == "NaN" || txCount?string == "nan">nan<#else>${format.formatNumber(txCount)}</#if>
                                    </td>
                                    <#assign txErrors = (agent.transactionErrors?has_content)?then(agent.transactionErrors, "nan")>
                                    <td class="value number<#if (txErrors?string != "NaN" && txErrors?string != "nan" && txErrors?number > 0)> error</#if>">
                                        <!-- DEBUG -->
                                        <#if txErrors?string == "NaN" || txErrors?string == "nan">nan<#else>${format.formatNumber(txErrors?number)}</#if>
                                    </td>
                                    <td class="value number<#if (txErrors?string != "NaN" && txErrors?string != "nan" && txErrors?number > 0)> error</#if>">
                                        <#assign txErrorPct = (agent.transactionErrorPercentage?has_content)?then(agent.transactionErrorPercentage, "nan")>
                                        <#if txErrorPct?string == "NaN" || txErrorPct?string == "nan">nan%<#else>${format.formatPercentageValue(txErrorPct?number)}</#if>
                                    </td>
                                    <td class="value number colgroup1">
                                        <#assign val = (agent.totalCpuUsage.mean?has_content)?then(agent.totalCpuUsage.mean!0, "nan")>
                                        <#if val?string == "NaN" || val?string == "nan">nan<#else>${val?number?string("0.00")}</#if>
                                    </td>
                                    <td class="value number colgroup1">
                                        <#assign val = (agent.totalCpuUsage.max?has_content)?then(agent.totalCpuUsage.max!0, "nan")>
                                        <#if val?string == "NaN" || val?string == "nan">nan<#else>${val?number?string("0.00")}</#if>
                                    </td>
                                    <td class="value number">
                                        <#assign val = (agent.cpuUsage.mean?has_content)?then(agent.cpuUsage.mean!0, "nan")>
                                        <#if val?string == "NaN" || val?string == "nan">nan<#else>${val?number?string("0.00")}</#if>
                                    </td>
                                    <td class="value number">
                                        <#assign val = (agent.cpuUsage.max?has_content)?then(agent.cpuUsage.max!0, "nan")>
                                        <#if val?string == "NaN" || val?string == "nan">nan<#else>${val?number?string("0.00")}</#if>
                                    </td>
                                    <td class="value number colgroup1">
                                        ${format.formatNumber(agent.minorGcCount!0)}
                                    </td>
                                    <td class="value number colgroup1">
                                        ${format.formatNumber(agent.minorGcTime!0)}
                                    </td>
                                    <td class="value number colgroup1">
                                        <#assign val = (agent.minorGcCpuUsage?has_content)?then(agent.minorGcCpuUsage!0, "nan")>
                                        <#if val?string == "NaN" || val?string == "nan">nan<#else>${val?number?string("0.00")}</#if>
                                    </td>
                                    <td class="value number">
                                        ${format.formatNumber(agent.fullGcCount!0)}
                                    </td>
                                    <td class="value number">
                                        ${format.formatNumber(agent.fullGcTime!0)}
                                    </td>
                                    <td class="value number">
                                        <#assign val = (agent.fullGcCpuUsage?has_content)?then(agent.fullGcCpuUsage!0, "nan")>
                                        <#if val?string == "NaN" || val?string == "nan">nan<#else>${val?number?string("0.00")}</#if>
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    <#else>
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
                    </#if>
                </table>
            </div>

            <#if rootNode.agent?has_content>
                <h3 class="no-print">
                    Individual Agents
                </h3>
                <div class="charts">
                    <#list rootNode.agent?sort_by("name") as agent>
                         <@agent_chart.agent_chart directory=agent.name isSummary=false gid="agent_" + agent_index />
                    </#list>
                </div>
            </#if>
        </div>
    </div>
</#macro>
