<#import "/common/util/format.ftl" as fmt>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/sections/descriptions.ftl" as desc>

<#macro hosts rootNode totalHits>
    <div class="section" id="hosts">
        <@desc.headline_hosts />

        <div class="content">
            <@desc.description_hosts />

            <div class="data">
                <table class="table-autosort:0">
                    <thead>
                        <tr>
                            <th class="table-sortable:alphanumeric" id="sortByHost">Host</th>
                            <th class="table-sortable:numeric" id="sortByHostCount">Count</th>
                            <th class="table-sortable:numeric" id="sortByHostPercentage">Percentage</th>
                        </tr>
                    </thead>
                    <#assign hostList = rootNode.host![]>
                    <#if hostList?size gt 0>
                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=hostList?size />

                                <td class="value number">
                                    ${fmt.formatNumber(totalHits)}
                                </td>
                                <td class="value number">
                                    100.0%
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list hostList?sort_by("name") as h>
                                <tr>
                                    <td class="key">
                                        ${h.name}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatNumber(h.count)}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatPercentage2(h.count?number / totalHits?number)}
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
