<#import "/common/util/format.ftl" as fmt>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/sections/descriptions.ftl" as desc>

<#macro request_methods rootNode totalHits>
    <div class="section" id="http-request-methods">
        <@desc.headline_http_request_methods />

        <div class="content">
            <@desc.description_http_request_methods />

            <div class="data">
                <table class="table-autosort:0">
                    <thead>
                        <tr>
                            <th class="table-sortable:numeric">Request Method</th>
                            <th class="table-sortable:numeric">Count</th>
                            <th class="table-sortable:numeric">Percentage</th>
                        </tr>
                    </thead>
                    <#assign methodList = rootNode.requestMethod![]>
                    <#if methodList?size gt 0>
                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=methodList?size />

                                <td class="value number">
                                    ${fmt.formatNumber(totalHits)}
                                </td>
                                <td class="value number">
                                    100.0%
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list methodList?sort_by("method") as rm>
                                <tr>
                                    <td class="key">
                                        ${rm.method}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatNumber(rm.count)}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatPercentage2(rm.count?number / totalHits?number)}
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
        </div>
    </div>
</#macro>
