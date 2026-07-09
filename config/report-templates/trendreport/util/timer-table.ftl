<#import "timer-row-abs.ftl" as timerRowAbs>
<#import "timer-row-rel.ftl" as timerRowRel>
<#import "descriptions.ftl" as descriptions>
<#import "../../common/util/filtered-footer-row.ftl" as filteredFooterRow>

<#macro render elements summaryElement tableRowHeader valueName mode>
    <#local valuesCount = elements[0].trendValues.trendValue?size>
    <#local showValues = (valuesCount < 16)>
    <#local formatStr = (valueName == "errors")?then("#", "#,##0 ms")>

    <table class="c-tab-content table-autosort:0">
        <thead>
            <tr>
                <th class="table-sortable:alphanumeric" rowspan="2" id="sortBy${valueName}${mode}Name">
                    ${tableRowHeader}
                    <br/>
                    <form>
                        <input class="filter" placeholder="Enter filter substrings" title="" data-filter-id="filterByName" data-col-index="0"/>
                        <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                    </form>
                </th>

                <#-- write the data for the first report as base column -->
                <#assign firstTrendValue = elements[0].trendValues.trendValue[0]>
                <th rowspan="2" class="cluetip" data-rel="#ReportInfo-${valueName}-${mode}-${firstTrendValue?index}">
                    <#if showValues>
                        <#-- Use a unique ID for sorting if needed, but table-autosort usually handles it -->
                        <#if mode == "absolute">
                            <@descriptions.table_headline_absolute_base />
                        <#else>
                            <@descriptions.table_headline_relative_base />
                        </#if>
                    </#if>
                </th>

                <th colspan="${valuesCount - 1}">
                    <#if mode == "absolute">
                        <@descriptions.table_headline_absolute />
                    <#else>
                        <@descriptions.table_headline_relative />
                    </#if>
                </th>
            </tr>

            <#if (elements?size > 0)>
                <tr>
                    <#list elements[0].trendValues.trendValue as trendValue>
                        <#if trendValue?index gt 0>
                            <th class="cluetip" data-rel="#ReportInfo-${valueName}-${mode}-${trendValue?index}" id="sortBy${valueName}${mode}Run${trendValue?index + 1}">
                                <#if showValues>
                                    ${trendValue?index + 1}
                                </#if>
                            </th>
                        </#if>
                    </#list>
                </tr>
            </#if>
        </thead>
        <#local count = elements?size>
        <#if (count > 0)>
            <tfoot>
                <#if summaryElement?has_content>
                    <#if mode == "absolute">
                        <@timerRowAbs.render_summary gid="summary-" + valueName + "-" + mode valueName=valueName showValues=showValues formatStr=formatStr rowsInTable=count summaryElement=summaryElement />
                    <#else>
                        <@timerRowRel.render_summary gid="summary-" + valueName + "-" + mode valueName=valueName showValues=showValues formatStr=formatStr rowsInTable=count summaryElement=summaryElement />
                    </#if>
                </#if>
                <@filteredFooterRow.render />
            </tfoot>
            <tbody>
                <#list elements?sort_by("name") as timerElement>
                    <#if mode == "absolute">
                        <@timerRowAbs.render gid=timerElement?index + "-" + valueName + "-" + mode valueName=valueName showValues=showValues formatStr=formatStr timerElement=timerElement />
                    <#else>
                        <@timerRowRel.render gid=timerElement?index + "-" + valueName + "-" + mode valueName=valueName showValues=showValues formatStr=formatStr timerElement=timerElement />
                    </#if>
                </#list>
            </tbody>
        <#else>
            <tfoot>
                <tr><td/><td/></tr>
                <@filteredFooterRow.render />
            </tfoot>
            <tbody class="table-nosort">
                <tr>
                    <td class="no-data" colspan="2">
                        No data available
                    </td>
                </tr>
            </tbody>
        </#if>
    </table>
</#macro>
