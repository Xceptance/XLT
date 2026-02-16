<#import "/util/timer-row.ftl" as tr>
<#import "/util/timer-summary-row.ftl" as tsr>
<#import "/common/util/filtered-footer-row.ftl" as ffr>

<#macro timer_table elements summaryElement tableRowHeader type>
    <#local percentiles = report.testreport.testReportConfig.runtimePercentiles.string![]>
    <#local percentileCount = percentiles?size>
    <#local runtimeIntervals = report.testreport.testReportConfig.runtimeIntervals.interval![]>
    <#local intervalCount = runtimeIntervals?size>

        <table class="c-tab-content table-autosort:0">
            <thead>
                <tr>
                    <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">${tableRowHeader}<br/><form><input class="filter" placeholder="Enter filter substrings" title=""/><button class="clear-input" type="clear" title="Click to clear">&#x2715;</button></form></th>
                    <#if type == "transaction" || type == "action" || type == "request">
                    <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByLabels">Labels</th>
                    </#if>
                    <#if type == "request">
                    <th colspan="5">Count</th>
                    <#else>
                    <th colspan="4">Count</th>
                    </#if>
                    <th colspan="2" class="colgroup1">Errors</th>
                    <#if type == "transaction">
                    <th class="colgroup1">Events</th>
                    </#if>
                    <th colspan="4">Runtime [ms]</th>
                    <#if percentileCount gt 0>
                    <th colspan="${percentileCount}" class="colgroup1">Runtime Percentiles [ms]</th>
                    </#if>
                    <#if type == "action">
                    <th rowspan="2" class="table-sortable:alphanumeric" id="sortByApdex">Apdex</th>
                    </#if>
                    <#if type == "request" && intervalCount gt 0>
                    <th colspan="${intervalCount}">Runtime Segmentation [ms]</th>
                    </#if>
                </tr>
                <tr>
                    <th class="table-sortable:numeric" id="sortByCountTotal">Total</th>
                    <#if type == "request">
                    <th class="table-sortable:numeric" id="sortByCountDistinct">Distinct**</th>
                    <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                    <th class="table-sortable:numeric" id="sortByCountPerMinute">1/min</th>
                    <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                    <#else>
                    <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                    <th class="table-sortable:numeric" id="sortByCountPerMinute">1/min</th>
                    <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                    </#if>
                    <th class="table-sortable:numeric colgroup1" id="sortByErrorsTotal">Total</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByErrorsPercentage">%</th>
                    <#if type == "transaction">
                    <th class="table-sortable:numeric colgroup1" id="sortByEventsTotal">Total</th>
                    </#if>
                    <th class="table-sortable:numeric" title="The arithmetic mean of the data series." id="sortByRuntimeMean">Mean</th>
                    <th class="table-sortable:numeric" title="The smallest value of the data series." id="sortByRuntimeMin">Min.</th>
                    <th class="table-sortable:numeric" title="The largest value of the data series." id="sortByRuntimeMax">Max.</th>
                    <th class="table-sortable:numeric" title="The standard deviation of all data within this data series." id="sortByRuntimeDev">Dev.</th>
                    <#list percentiles as p>
                    <th class="table-sortable:numeric colgroup1" title="The nth percentile of the data series." id="sortByPercentile${p}">P${p}</th>
                    </#list>
                    <#if type == "request" && intervalCount gt 0>
                        <#if runtimeIntervals?size gt 0>
                            <#list runtimeIntervals as interval>
                                <th class="table-sortable:numeric" title="A data segment and the percentage of data from the time series that is located within." id="sortBySegmentFrom${interval.@from}To${interval.@to}"><#if interval?has_next>${"&le;"?no_esc}${interval.@to?number?string[",##0"]}<#else>${"&gt;"?no_esc}${interval.@from?number?string[",##0"]}</#if></th>
                            </#list>
                        <#else>
                            <th></th>
                        </#if>
                    </#if>
                </tr>
            </thead>
            <#local count = elements?size>
            <#if count gt 0>
                <tfoot>
                    <#list summaryElement as se>
                        <@tsr.timer_summary_row element=se type=type rowsInTable=count />
                    </#list>
                    <@ffr.filtered_footer_row />
                </tfoot>
                <tbody>
                    <#list elements as el>
                        <@tr.timer_row element=el type=type gid="id${el?counter}" />
                    </#list>
                </tbody>
            <#else>
                <#local columnCount = 0>
                <#if type == "request">
                    <#local columnCount = 13 + percentileCount + intervalCount>
                <#elseif type == "transaction">
                    <#local columnCount = 13 + percentileCount>
                <#elseif type == "action">
                    <#local columnCount = 13 + percentileCount>
                <#else>
                    <#local columnCount = 11 + percentileCount>
                </#if>
                <tfoot>
                    <tr>
                        <#list 1..columnCount as i><td></td></#list>
                    </tr>
                    <@ffr.filtered_footer_row />
                </tfoot>
                <tbody class="table-nosort">
                    <tr>
                        <td class="no-data" colspan="${columnCount}">
                                No data available
                        </td>
                    </tr>
                </tbody>
            </#if>
        </table>
</#macro>
