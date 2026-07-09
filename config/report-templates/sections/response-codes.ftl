<#import "/common/util/format.ftl" as fmt>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/sections/descriptions.ftl" as desc>

<#macro response_codes rootNode totalHits>
    <div class="section" id="http-response-codes">
        <@desc.headline_http_response_codes />

        <div class="content">
            <@desc.description_http_response_codes />
            
            <div class="data">
                <table class="table-autosort:0">
                    <thead>
                        <tr>
                            <th class="table-sortable:numeric" id="sortByResponseCode">Response Code</th>
                            <th class="table-sortable:numeric" id="sortByResponseCodeCount">Count</th>
                            <th class="table-sortable:numeric" id="sortByResponseCodePercentage">Percentage</th>
                        </tr>
                    </thead>
                    <#assign codeList = rootNode.responseCode![]>
                    <#if codeList?size gt 0>
                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=codeList?size />
                                
                                <td class="value number">
                                    ${fmt.formatNumber(totalHits)}
                                </td>
                                <td class="value number">
                                    100.0%
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list codeList?sort_by("code") as rc>
                                <tr>
                                    <td class="key">
                                        ${rc.code} &ndash; ${rc.statusText}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatNumber(rc.count)}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatPercentage2(rc.count?number / totalHits?number)}
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
            </div>
            <div class="charts">
                <div class="chart">
                    <img src="charts/ResponseCodesPerSecond.webp" alt="Response Codes Per Second"/>
                </div>
            </div>
        </div>
    </div>
</#macro>
