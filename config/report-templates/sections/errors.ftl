<#import "../common/util/format.ftl" as format>
<#import "descriptions.ftl" as descriptions>
<#import "../common/util/create-totals-td.ftl" as totals>

<#macro errors>
    <div class="section" id="error-summary">
        <@descriptions.headline_error_summary />

        <div class="content">
            <@descriptions.description_error_summary />

            <div class="data">

                <div class="charts">
                    <div class="chart">
                        <img src="charts/Errors.webp" alt="Errors" />
                    </div>
                </div>

                <#-- Request Error Charts -->
                <h3 id="request-errors">Request Errors</h3>
                <div class="description">
                    <#local gid = "requestErrorCharts" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
                    <p>
                        This section displays the request errors grouped by response
                        code.
                        <@descriptions.show_n_hide gid=gid />
                    </p>
                    <div id="more-${gid}" class="more">
                        <p>
                            The chart creation can be configured by setting the
                            corresponding report generator properties. (See
                            <b>reportgenerator.properties</b>
                            file for more details)
                        </p>
                    </div>
                </div>

                <#-- Check if request error overview charts exist -->
                <#assign chartsList = report.testreport.errors.requestErrorOverviewCharts.chart>
                <#if chartsList?has_content>
                    <div class="charts">
                        <#list chartsList?sort_by("id") as chart>
                            <div class="chart">
                                <img src="charts/errors/r${chart.id}.webp" alt="Response Errors" />
                            </div>
                        </#list>
                    </div>
                <#else>
                    <p class="no-data">
                        No such data has been collected.
                    </p>
                </#if>

                <#-- Transaction Error Overview -->
                <h3 id="transaction-error-overview">Transaction Error Overview</h3>
                <table class="table-autosort:1 table-autosort-order:desc">
                    <thead>
                        <tr>
                            <th class="table-sortable:alphanumeric" id="sortByErrorOverviewMsg">Error Message</th>
                            <th class="table-sortable:numeric" id="sortByErrorOverviewCount">Count</th>
                            <th class="table-sortable:numeric" id="sortByErrorOverviewPercentage">Percentage</th>
                        </tr>
                    </thead>
                    <#assign errorList = report.testreport.errors.error>
                    <#if errorList?has_content>
                        <#-- Calculate total error count -->
                        <#local totalErrorCount = 0>
                        <#list errorList as error>
                             <#local totalErrorCount = totalErrorCount + error.count?number>
                        </#list>

                        <#-- Group errors by message -->
                        <#local distinctErrors = {}>
                        <#list errorList as error>
                            <#local msg = error.message>
                            <#if distinctErrors[msg]??>
                                <#local currentCount = distinctErrors[msg]>
                                <#local distinctErrors = distinctErrors + {msg: currentCount + error.count?number}>
                            <#else>
                                <#local distinctErrors = distinctErrors + {msg: error.count?number}>
                            </#if>
                        </#list>
                        <#local countDistinctErrorMessages = distinctErrors?keys?size>

                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=countDistinctErrorMessages />

                                <td class="value number">
                                    ${format.formatTimerVal(totalErrorCount, "#,##0")}
                                </td>
                                <td class="value number">
                                    ${format.formatPercentage(1)}
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#-- Iterate over distinct messages (sorted) -->
                            <#list distinctErrors?keys?sort as message>
                                <#local errorCountByMessage = distinctErrors[message]>
                                
                                <#-- Find overview chart ID for this message -->
                                <#local overviewChartID = ""> 
                                <#local transactionCharts = report.testreport.errors.transactionErrorOverviewCharts.chart>
                                <#if transactionCharts?has_content>
                                    <#list transactionCharts as chart>
                                        <#if chart.title == message>
                                            <#local overviewChartID = chart.id>
                                            <#break>
                                        </#if>
                                    </#list>
                                </#if>

                                <tr>
                                    <td class="value text forcewordbreak">
                                        <#if overviewChartID?has_content>
                                            <a id="tableEntry-${overviewChartID}" href="#${overviewChartID}">
                                                ${message}
                                            </a>
                                        <#else>
                                            ${message}
                                        </#if>
                                    </td>
                                    <td class="value number count">
                                        ${format.formatTimerVal(errorCountByMessage, "#,##0")}
                                    </td>
                                    <td class="value number count">
                                        ${format.formatPercentage(errorCountByMessage / totalErrorCount)}
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    <#else>
                        <tfoot>
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="3">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>

                <#-- Transaction Error Overview Charts -->
                <div class="description">
                    <#local gid = "transactionOverview" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
                    <#local errorViewCount = 0>
                    <#if report.testreport.errors.transactionErrorOverviewCharts.chart?has_content>
                         <#local errorViewCount = report.testreport.errors.transactionErrorOverviewCharts.chart?size>
                    </#if>
                    
                    <#-- Need to recalculate distinct count because distinctErrors is local to previous block -->
                    <#local errorsByMessageCount = 0>
                    <#if report.testreport.errors.error?has_content>
                         <#local distinctMsgs = []>
                         <#list report.testreport.errors.error as e>
                             <#if !distinctMsgs?seq_contains(e.message)>
                                 <#local distinctMsgs = distinctMsgs + [e.message]>
                             </#if>
                         </#list>
                         <#local errorsByMessageCount = distinctMsgs?size>
                    </#if>

                    <p>
                        <#if (errorViewCount < errorsByMessageCount)>
                            Displaying the first
                            <b>
                                ${errorViewCount}
                            </b>
                            overview charts for the error types that occurred most often.
                        </#if>
                        <@descriptions.show_n_hide gid=gid />
                    </p>
                    <div id="more-${gid}" class="more">
                        <p>
                            The chart limit can be set or disabled by setting the
                            corresponding report generator properties. (See
                            <b>reportgenerator.properties</b>
                            file for more details)
                        </p>
                    </div>
                </div>

                <#assign transactionChartsList = report.testreport.errors.transactionErrorOverviewCharts.chart>
                <#if transactionChartsList?has_content>
                    <div class="charts">
                        <#list transactionChartsList?sort_by("title") as chart>
                            <div class="chart">
                                <div class="error">
                                    <img id="${chart.id}" src="charts/errors/t${chart.id}.webp" alt="Errors by Type" />
                                    <a class="backlink" href="#tableEntry-${chart.id}">Back to Table</a>
                                </div>
                            </div>
                        </#list>
                    </div>
                <#else>
                    <p class="no-data">
                        No such data has been collected.
                    </p>
                </#if>

                <#-- Transaction Error Details -->
                <h3 id="transaction-error-details">Transaction Error Details</h3>
                <div class="description">
                    <#local gid = "transactionDetails" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
                    
                    <#local errorDetailsCount = 0>
                     <#if report.testreport.errors.error?has_content>
                        <#list report.testreport.errors.error as e>
                             <#if e.detailChartID?has_content && e.detailChartID != "0">
                                 <#local errorDetailsCount = errorDetailsCount + 1>
                             </#if>
                        </#list>
                     </#if>

                     <#local errorsCount = 0>
                     <#if report.testreport.errors.error?has_content>
                        <#local errorsCount = report.testreport.errors.error?size>
                     </#if>

                    <p>
                        <#if (errorDetailsCount < errorsCount)>
                            Displaying the first
                            <b>
                                ${errorDetailsCount}
                            </b>
                            detail charts for the error types that occurred most often,
                            grouped by test case/action.
                        </#if>
                        <@descriptions.show_n_hide gid=gid />
                    </p>
                    <div id="more-${gid}" class="more">
                        <p>
                            The chart limit can be set or disabled by setting the
                            corresponding report generator properties. (See
                            <b>reportgenerator.properties</b>
                            file for more details)
                        </p>
                    </div>
                </div>

                <table class="table-autosort:0 table-autosort-order:desc error-table">
                    <thead>
                        <tr>
                            <th class="table-sortable:numeric" id="sortByErrorDetailCount">Count</th>
                            <th class="table-sortable:alphanumeric" id="sortByErrorDetailTestCase">Test Case</th>
                            <th class="table-sortable:alphanumeric" id="sortByErrorDetailAction">Action</th>
                            <th>Directory</th>
                            <th class="table-sortable:alphanumeric" id="sortByErrorDetailInformation">Error Information</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                    </tfoot>
                    <#if errorList?has_content>
                        <#local sortedErrors = []>
                        <#list errorList as error>
                            <#-- 
                                Calculate sort value for stable descending sort.
                                Primary: count (desc)
                                Secondary: hasDetailChart (desc)
                                We use negation to sort ascending (which is stable in FM) and match descending order.
                                Multiplier 10 ensures hasDetailChart (0 or 1) acts as a lower-significant digit.
                            -->
                            <#local hasChart = ((error.detailChartID!"0") != "0")>
                            <#local sortVal = (error.count?number * 10 + hasChart?string("1", "0")?number) * -1>
                            <#local sortedErrors = sortedErrors + [{"node": error, "sortVal": sortVal}]>
                        </#list>
                        <#list sortedErrors?sort_by("sortVal") as entry>
                            <#local error = entry.node>
                            <tr>
                                <td class="value number count">
                                    ${format.formatTimerVal(error.count?number, "#,##0")}
                                </td>
                                <td class="value text testcasename">
                                    ${error.testCaseName}
                                </td>
                                <td class="value text">
                                    ${error.actionName}
                                </td>
                                <td class="value text directory">
                                    <#if error.directoryHints.string?has_content>
                                         <#local dirList = error.directoryHints.string>
                                         <#list dirList as dir>
                                             <#local dText = dir?string>
                                             <#if report.testreport.errors.resultsPathPrefix?has_content && dText?trim != "..." >
                                                 <a href="${report.testreport.errors.resultsPathPrefix}${dText}/index.html" target="_blank">${dText}</a>
                                             <#else>
                                                 ${dText}
                                             </#if>
                                             <br />
                                         </#list>
                                    </#if>
                                </td>
                                <#-- Error Details -->
                                <td class="value text trace collapsible forcewordbreak">
                                    <div class="collapse">
                                        ${error.message}
                                    </div>
                                    <pre>
                                        ${error.trace}
                                    </pre>
                                    <#-- Error Details Charts -->
                                    <#if error.detailChartID?has_content && error.detailChartID != "0">
                                        <div class="charts">
                                            <div class="chart">
                                                <img src="charts/errors/d${error.detailChartID}.webp" alt="Details Chart" />
                                            </div>
                                        </div>
                                    </#if>

                                    <#-- Link back to overview chart -->
                                    <#local errorMessage = error.message>
                                    <#if report.testreport.errors.transactionErrorOverviewCharts.chart?has_content>
                                            <#local overviewChartID = "">
                                            <#list report.testreport.errors.transactionErrorOverviewCharts.chart as chart>
                                                <#if chart.title == errorMessage>
                                                    <#local overviewChartID = chart.id>
                                                    <#break>
                                                </#if>
                                            </#list>

                                            <#if overviewChartID?has_content>
                                                <a class="backlink" href="#${overviewChartID}">
                                                    Error overview chart
                                                </a>
                                            <#else>
                                                <div>
                                                    No error overview chart available.
                                                </div>
                                            </#if>
                                        </#if>
                                </td>
                            </tr>
                        </#list>
                    <#else>
                        <tr>
                            <td class="no-data" colspan="5">No data available</td>
                        </tr>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</#macro>
