<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>
<#import "util/timer-cell.ftl" as timerCell>
<#import "../common/util/create-totals-td.ftl" as createTotalsTd>
<#import "../common/util/filtered-footer-row.ftl" as filteredFooterRow>

<#compress>
<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Comparison Report - Custom Values" projectName=projectName configuration=report.testreport.configuration />
</head>
<body id="diffreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Comparison Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Custom Values
                ************************************
            -->
            <div class="section" id="custom-values-summary">
                <@descriptions.headline_custom_values_summary />

                <div class="content">
                    <@descriptions.description_custom_values_summary />
                    
                    <#assign percentileCount = 0>
                    <#if report.testreport.summary.requests.percentiles?has_content>
                        <#assign percentileCount = report.testreport.summary.requests.percentiles?children?size>
                    </#if>

                    <table class="c-tab-content table-autosort:0">
                        <thead>
                            <tr>
                                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                                    Value Name
                                    <br/>
                                    <input class="filter" placeholder="Enter filter substrings" title=""/>
                                    <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                </th>
                                <th colspan="4">Count</th>
                                <th colspan="4" class="colgroup1">Stats</th>
                                <#if (percentileCount > 0)>
                                    <th colspan="${percentileCount}">Percentiles</th>
                                </#if>
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
                                <#if report.testreport.summary.requests.percentiles?has_content>
                                    <#list report.testreport.summary.requests.percentiles?children as p>
                                        <th class="table-sortable:numeric" title="The nth percentile of the data series." id="sortByPercentile${p?node_name?upper_case}">
                                            ${p?node_name?upper_case}
                                        </th>
                                    </#list>
                                </#if>
                            </tr>
                        </thead>
                        <#if (report.testreport.customValues?size > 0)>
                        <#assign elements = report.testreport.customValues?children>
                        <#assign count = elements?size>
                        <tfoot>
                            <tr class="totals">
                                <@createTotalsTd.create_totals_td rows_in_table=count class="key" />
                                <td class=""></td>
                                <td class=""></td>
                                <td class=""></td>
                                <td class=""></td>
                                <td class="colgroup1"></td>
                                <td class="colgroup1"></td>
                                <td class="colgroup1"></td>
                                <td class="colgroup1"></td>
                                <#if report.testreport.summary.requests.percentiles?has_content>
                                    <#list report.testreport.summary.requests.percentiles?children as p>
                                        <td class=""></td>
                                    </#list>
                                </#if>
                            </tr>
                            <@filteredFooterRow.filtered_footer_row />
                        </tfoot>
                        
                        <#if (count > 0)>
                            <tbody>
                                <#list elements as cv>
                                    <tr>
                                        <td class="key colgroup1">
                                            <a <#if cv.description?has_content>title="${cv.description}"</#if>>
                                                ${cv.name}
                                            </a>
                                        </td>
                                        <@timerCell.render node=cv.count format="#,##0" neutral=true />
                                        <@timerCell.render node=cv.countPerSecond format="#,##0" neutral=true />
                                        <@timerCell.render node=cv.countPerHour format="#,##0" neutral=true />
                                        <@timerCell.render node=cv.countPerDay format="#,##0" neutral=true />
                                        <@timerCell.render node=cv.mean format="#,##0.###" neutral=true />
                                        <@timerCell.render node=cv.min format="#,##0.###" neutral=true />
                                        <@timerCell.render node=cv.max format="#,##0.###" neutral=true />
                                        <@timerCell.render node=cv.standardDeviation format="#,##0.###" neutral=true />
                                        <#if cv.percentiles?has_content>
                                            <#list cv.percentiles?children as p>
                                                <@timerCell.render node=p format="#,##0.###" neutral=true />
                                            </#list>
                                        </#if>
                                    </tr>
                                </#list>
                            </tbody>
                        <#else>
                            <#assign columnCount = 9 + percentileCount>
                            <tbody class="table-nosort">
                                <tr>
                                    <td colspan="${columnCount}" class="no-data">No data available</td>
                                </tr>
                            </tbody>
                        </#if>
                        
                    </table>
                    <#else>
                    <p>No data available.</p>
                    </#if>
                </div>
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.javascript />

</body>
</html>

</#compress>
