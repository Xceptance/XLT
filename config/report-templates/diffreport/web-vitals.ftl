<#import "../common/sections/head.ftl" as head>
<#import "../common/sections/header.ftl" as header>
<#import "../common/sections/footer.ftl" as footer>
<#import "../common/sections/javascript.ftl" as javascript>
<#import "navigation.ftl" as navigation>
<#import "descriptions.ftl" as descriptions>
<#import "util/timer-cell.ftl" as timerCell>
<#import "../../common/util/create-totals-td.ftl" as createTotalsTd>
<#import "../../common/util/filtered-footer-row.ftl" as filteredFooterRow>

<!DOCTYPE html>
<html lang="en">
<head>
    <@head.head title="XLT Performance Comparison Report - Web Vitals" projectName=projectName configuration=report.testreport.configuration />
</head>
<body id="diffreport">
<div id="container">
    <div id="content">
        <@header.header navNamespace=navigation title="Performance Comparison Report" productName=productName productVersion=productVersion productUrl=productUrl projectName=projectName />

        <div id="data-content">

            <!--
                ************************************
                * Web Vitals
                ************************************
            -->
            <div class="section" id="action-summary">
                <@descriptions.headline_web_vitals_summary />

                <div class="content">
                    <@descriptions.description_web_vitals_summary />
                    
                    <#local elements = report.testreport.webVitalsList?children>
                    <#local count = elements?size>

                    <table class="c-tab-content table-autosort:0">
                        <thead>
                            <tr>
                                <th class="table-sortable:alphanumeric" id="sortByName">
                                    Action Name
                                    <br/>
                                    <input class="filter" placeholder="Enter filter substrings" title=""/>
                                    <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                </th>
                                <th class="table-sortable:numeric" id="sortByFCP">First Contentful Paint<br/>(FCP)</th>
                                <th class="table-sortable:numeric" id="sortByLCP">Largest Contentful Paint<br/>(LCP)</th>
                                <th class="table-sortable:numeric" id="sortByCLS">Cumulative Layout Shift<br/>(CLS)</th>
                                <th class="table-sortable:numeric" id="sortByFID">First Input Delay<br/>(FID)</th>
                                <th class="table-sortable:numeric" id="sortByINP">Interaction to Next Paint<br/>(INP)</th>
                                <th class="table-sortable:numeric" id="sortByTTFB">Time to First Byte<br/>(TTFB)</th>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr class="totals">
                                <@createTotalsTd.render rowsInTable=count class="key" />
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                            <@filteredFooterRow.render />
                        </tfoot>
                        <#if (count > 0)>
                            <tbody>
                                <#list elements?sort_by("name") as wv>
                                    <tr>
                                        <td class="key">
                                            ${wv.name}
                                        </td>
                                        <@timerCell.render node=wv.fcp.score isInverse=false format="#,##0" unit=" ms" />
                                        <@timerCell.render node=wv.lcp.score isInverse=false format="#,##0" unit=" ms" />
                                        <@timerCell.render node=wv.cls.score isInverse=false format="#,##0.000" />
                                        <@timerCell.render node=wv.fid.score isInverse=false format="#,##0" unit=" ms" />
                                        <@timerCell.render node=wv.inp.score isInverse=false format="#,##0" unit=" ms" />
                                        <@timerCell.render node=wv.ttfb.score isInverse=false format="#,##0" unit=" ms" />
                                    </tr>
                                </#list>
                            </tbody>
                        <#else>
                            <tbody class="table-nosort">
                                <tr>
                                    <td colspan="7" class="no-data">No data available</td>
                                </tr>
                            </tbody>
                        </#if>
                    </table>
                </div>
            </div>

        </div> <!-- data-content -->

        <@footer.footer productName=productName productVersion=productVersion productUrl=productUrl />
    </div> <!-- content -->
</div> <!-- container -->    

<@javascript.render />

</body>
</html>
