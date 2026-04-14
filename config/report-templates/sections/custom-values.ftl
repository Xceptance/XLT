<#import "/sections/descriptions.ftl" as desc>
<#import "/common/util/format.ftl" as fmt>

<#macro custom_values>
    <div class="section" id="custom-values-summary">
        <@desc.headline_custom_values_summary />

        <div class="content">
            <@desc.description_custom_values_summary />

            <#local percentiles = report.testreport.testReportConfig.runtimePercentiles.string![]>
            <#local percentileCount = percentiles?size>

            <table class="c-tab-content table-autosort:0">
                <thead>
                    <tr>
                        <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                            Value Name
                            <br/>
                            <form>
                                <input class="filter" placeholder="Enter filter substrings" title="" data-filter-id="filterByName" data-col-index="0"/>
                                <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                            </form>
                        </th>
                        <th colspan="4">Count</th>
                        <th colspan="4" class="colgroup1">Stats</th>
                        <#if percentileCount gt 0>
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
                        <#list percentiles as p>
                            <th class="table-sortable:numeric" title="The nth percentile of the data series." id="sortByPercentile${p}">P${p}</th>
                        </#list>
                    </tr>
                </thead>
                <tfoot>
                    <tr>
                        <td class="colgroup1"></td>
                        <td class=""></td>
                        <td class=""></td>
                        <td class=""></td>
                        <td class=""></td>
                        <td class="colgroup1"></td>
                        <td class="colgroup1"></td>
                        <td class="colgroup1"></td>
                        <td class="colgroup1"></td>
                        <#list percentiles as p>
                            <td class=""></td>
                        </#list>
                    </tr>
                    <#-- filtered totals (will be made visible when a filter is applied)  -->
                    <tr class="totals filtered" style="display: none;">
                        <#-- the contents will be created dynamically (xlt.js, function recalculateFilteredFooter) -->
                    </tr>
                </tfoot>
                <#local cvs = report.testreport.customValues.*>
                <#if cvs?size gt 0>
                    <tbody>
                        <#list cvs?sort_by("name") as cv>
                            <#local gid = cv?node_name + cv?index> <#-- Fallback if generate-id is not available, but let's see -->
                            <#local gid = "cv" + cv?index>
                            <tr>
                                <td class="key colgroup1">
                                    <a href="#chart-${gid}" title="<#if cv.description?has_content>${cv.description}</#if>" data-id="tableEntry-${gid}">${cv.name}</a>
                                </td>
                                <td class="value number">${fmt.formatNumber(cv.count)}</td>
                                <td class="value number">${fmt.formatNumber(cv.countPerSecond)}</td>
                                <td class="value number">${fmt.formatNumber(cv.countPerHour)}</td>
                                <td class="value number">${fmt.formatNumber(cv.countPerDay)}</td>
                                <td class="value number colgroup1">${cv.mean?number?string(",##0.###")}</td>
                                <td class="value number colgroup1">${cv.min?number?string(",##0.###")}</td>
                                <td class="value number colgroup1">${cv.max?number?string(",##0.###")}</td>
                                <td class="value number colgroup1">${cv.standardDeviation?number?string(",##0.###")}</td>
                                <#list cv.percentiles.* as p>
                                    <td class="value number">${p?number?string(",##0.###")}</td>
                                </#list>
                            </tr>
                        </#list>
                    </tbody>
                <#else>
                    <tbody class="table-nosort">
                        <tr>
                            <td colspan="${9 + percentileCount}" class="no-data">No data available</td>
                        </tr>
                    </tbody>
                </#if>
            </table>

            <#if cvs?size gt 0>
                <div class="charts">
                    <#list cvs?sort_by("name") as cv>
                        <#if cv.chartFilename?has_content>
                            <#local gid = "cv" + cv?index>
                            <#local encodedChartFilename = cv.chartFilename?string?trim> <#-- Add illegal char conversion if needed -->
                            
                            <div class="chart-group tabs c-tabs no-print" data-name="${cv.name}" id="chart-${gid}">
                                <ul class="c-tabs-nav">
                                    <li class="c-tabs-nav-link img-tab c-is-active">
                                        <a href="#Overview-${gid}">Overview</a>
                                    </li>
                                    <li class="c-tabs-nav-link img-tab">
                                        <a href="#Averages-${gid}">Averages</a>
                                    </li>
                                </ul>

                                <a href="#tableEntry-${gid}" class="backlink">Back to Table</a>

                                <div id="Overview-${gid}" class="c-tab img-tab c-is-active">
                                    <div class="c-tab-content chart">
                                        <img src="charts/customvalues/${encodedChartFilename}.webp" alt="charts/customvalues/${encodedChartFilename}.webp" loading="lazy" />
                                    </div>
                                </div>

                                <div id="Averages-${gid}" class="c-tab img-tab">
                                    <div class="c-tab-content chart">
                                        <img src="charts/placeholder.webp" alt="charts/customvalues/${encodedChartFilename}_Average.webp" />
                                    </div>
                                </div>
                            </div>

                            <div class="chart-group print">
                                <h3>${cv.name}</h3>
                                <div class="chart">
                                    <h5>Overview</h5>
                                    <img alt="charts/customvalues/${encodedChartFilename}.webp">
                                </div>
                                <div class="chart">
                                    <h5>Averages</h5>
                                    <img alt="charts/customvalues/${encodedChartFilename}_Average.webp">
                                </div>
                            </div>
                        </#if>
                    </#list>
                </div>
            </#if>
        </div>
    </div>

    <#local logs = report.testreport.customLogs.customLog![]>
    <div class="section" id="custom-data-logs-summary">
        <@desc.headline_custom_data_logs_summary />

        <div class="content">
            <@desc.description_custom_data_logs_summary />
            
            <table class="c-tab-content table-autosort:0">
                <thead>
                    <tr>
                        <th>Scope</th>
                        <th>Size</th>
                    </tr>
                </thead>
                <tbody>
                <#if logs?size gt 0>
                    <#list logs?sort_by("scope") as log>
                        <tr>
                            <td class="key">
                                <a href="${log.path}">${log.scope}</a>
                            </td>
                            <td class="value">
                                ${fmt.formatBytes(log.size)}
                            </td>
                        </tr>
                    </#list>
                <#else>
                    <tr>
                        <td colspan="2" class="no-data">No data available</td>
                    </tr>
                </#if>
                </tbody>
            </table>
        </div>
    </div>
</#macro>
