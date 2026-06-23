<#import "timer-table.ftl" as timerTable>
<#import "timer-chart.ftl" as timerChart>

<#macro render elements summaryElement tableRowHeader directory type>
    <div class="data">
        <div id="tabletabbies-${type}" class="chart-group tabs c-tabs">
            <#local gid = "section-" + type>
            <ul class="c-tabs-nav">
                <li class="c-tabs-nav-link c-is-active">
                    <a href="#Median-${gid}">Median</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Mean-${gid}">Mean</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Minimum-${gid}">Minimum</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Maximum-${gid}">Maximum</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Errors-${gid}">Errors</a>
                </li>
            </ul>

            <#-- Report information -->
            <#list elements[0].trendValues.trendValue as trendValue>
                <div class="cluetip-data" id="ReportInfo-median-absolute-${trendValue?index}">
                    <h4>#${trendValue?index + 1}: ${trendValue.reportName?trim} (${trendValue.reportDate?trim})</h4>
                    <div class="description">
                        ${trendValue.reportComments!}
                    </div>
                </div>
                <#-- Duplicate for other tabs if they use same cluetip IDs or ensure IDs are unique -->
                <#list ["median", "mean", "min", "max", "errors"] as vn>
                    <#list ["absolute", "relative"] as m>
                        <#if vn != "median" || m != "absolute">
                            <div class="cluetip-data" id="ReportInfo-${vn}-${m}-${trendValue?index}">
                                <h4>#${trendValue?index + 1}: ${trendValue.reportName?trim} (${trendValue.reportDate?trim})</h4>
                                <div class="description">
                                    ${trendValue.reportComments!}
                                </div>
                            </div>
                        </#if>
                    </#list>
                </#list>
            </#list>

            <h3 class="print">Median</h3>
            <div id="Median-${gid}" class="c-tab c-is-active">
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="median" mode="absolute" />
                <p/>
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="median" mode="relative" />
            </div>

            <h3 class="print">Mean</h3>
            <div id="Mean-${gid}" class="c-tab">
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="mean" mode="absolute" />
                <p/>
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="mean" mode="relative" />
            </div>

            <h3 class="print">Minimum</h3>
            <div id="Minimum-${gid}" class="c-tab">
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="min" mode="absolute" />
                <p/>
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="min" mode="relative" />
            </div>

            <h3 class="print">Maximum</h3>
            <div id="Maximum-${gid}" class="c-tab">
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="max" mode="absolute" />
                <p/>
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="max" mode="relative" />
            </div>

            <h3 class="print">Errors</h3>
            <div id="Errors-${gid}" class="c-tab">
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="errors" mode="absolute" />
                <p/>
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader valueName="errors" mode="relative" />
            </div>
        </div>
    </div>

    <#if (elements?size > 0)>
        <div class="charts">
            <#list elements?sort_by("name") as timerElement>
                <@timerChart.render timerElement=timerElement directory=directory />
            </#list>
        </div>
    </#if>
</#macro>
