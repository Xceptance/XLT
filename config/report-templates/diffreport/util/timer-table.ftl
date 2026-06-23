<#import "timer-row.ftl" as timerRow>
<#import "../../common/util/filtered-footer-row.ftl" as filteredFooterRow>

<#macro render elements summaryElement tableRowHeader type>
    <#local percentileCount = 0>
    <#if summaryElement?has_content && summaryElement.percentiles?has_content>
        <#local percentileCount = summaryElement.percentiles?children?size>
    </#if>
    
    <table class="table-autosort:0">
        <thead>
            <tr>
                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                    ${tableRowHeader}
                    <br/>
                    <form>
                        <input class="filter" placeholder="Enter filter substrings" title="" data-filter-id="filterByName" data-col-index="0"/>
                        <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                    </form>
                </th>
                <#if type == "request">
                    <th colspan="5">Count</th>
                <#else>
                    <th colspan="4">Count</th>
                </#if>
                <th class="colgroup1">Errors</th>
                <#if type == "transaction">
                    <th class="colgroup1">Events</th>
                </#if>
                <th colspan="4">Runtime [ms]</th>
                <#if (percentileCount > 0)>
                    <th colspan="${percentileCount}" class="colgroup1">Runtime Percentiles [ms]</th>
                </#if>
            </tr>
            <tr>
                <#if type == "request">
                    <th class="table-sortable:numeric" id="sortByCountDistinct">Distinct**</th>
                </#if>
                <th class="table-sortable:numeric" id="sortByCountTotal">Total</th>
                <th class="table-sortable:numeric" id="sortByCountPerSecond">1/s</th>
                <th class="table-sortable:numeric" id="sortByCountPerMinute">1/min</th>
                <th class="table-sortable:numeric" id="sortByCountPerHour">1/h*</th>
                <th class="table-sortable:numeric colgroup1" id="sortByErrorsTotal">Total</th>
                <#if type == "transaction">
                    <th class="table-sortable:numeric colgroup1" id="sortByEventsTotal">Total</th>
                </#if>
                <th class="table-sortable:numeric" title="The arithmetic mean of the data series." id="sortByRuntimeMean">Mean</th>
                <th class="table-sortable:numeric" title="The smallest value of the data series." id="sortByRuntimeMin">Min.</th>
                <th class="table-sortable:numeric" title="The largest value of the data series." id="sortByRuntimeMax">Max.</th>
                <th class="table-sortable:numeric" title="The standard deviation of all data within this data series." id="sortByRuntimeDev">Dev.</th>
                <#if summaryElement?has_content && summaryElement.percentiles?has_content>
                    <#list summaryElement.percentiles?children as p>
                        <th class="table-sortable:numeric colgroup1" title="The nth percentile of the data series." id="sortByPercentile${p?node_name?upper_case}">
                            ${p?node_name?upper_case}
                        </th>
                    </#list>
                </#if>
            </tr>
        </thead>
        <#local count = elements?size>
        <#if (count > 0)>
            <tfoot>
                <#if summaryElement?has_content>
                    <@timerRow.render_summary summaryElement=summaryElement type=type rowsInTable=count />
                </#if>
                <@filteredFooterRow.filtered_footer_row />
            </tfoot>
            <tbody>
                <#list elements as timerElement>
                    <@timerRow.render timerElement=timerElement type=type />
                </#list>
            </tbody>
        <#else>
            <#local columns = 0>
            <#if type == "request">
                <#local columns = 11 + percentileCount>
            <#elseif type == "transaction">
                <#local columns = 11 + percentileCount>
            <#else>
                <#local columns = 10 + percentileCount>
            </#if>
            <tfoot>
                <tr>
                    <td class="colgroup1"></td>
                    <#if type == "request">
                        <td colspan="5"></td>
                    <#else>
                        <td colspan="4"></td>
                    </#if>
                    <#if type == "transaction">
                        <td colspan="2" class="colgroup1"></td>
                    <#else>
                        <td class="colgroup1"></td>
                    </#if>
                    <td colspan="4"></td>
                    <#if (percentileCount > 0)>
                        <td colspan="${percentileCount}" class="colgroup1"></td>
                    </#if>
                </tr>
                <@filteredFooterRow.filtered_footer_row />
            </tfoot>
            <tbody class="table-nosort">
                <tr>
                    <td class="no-data" colspan="${columns}">
                        No data available
                    </td>
                </tr>
            </tbody>
        </#if>
    </table>
</#macro>
