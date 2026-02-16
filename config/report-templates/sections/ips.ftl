<#import "/common/util/format.ftl" as fmt>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/sections/descriptions.ftl" as desc>

<#macro ips rootNode totalHits>
    <div class="section" id="ip-addresses">
        <@desc.headline_ips />

        <div class="content">
            <@desc.description_ips />

            <div class="data">
                <table class="table-autosort:0 table-autostripe table-stripeclass:odd">
                    <thead>
                        <tr>
                            <th class="table-sortable:alphanumeric">IP</th>
                            <th class="table-sortable:alphanumeric">Host</th>
                            <th class="table-sortable:numeric">Count</th>
                            <th class="table-sortable:numeric">Percentage</th>
                        </tr>
                    </thead>
                    <#assign ipList = rootNode.ip![]>
                    <#if ipList?size gt 0>
                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=ipList?size />
                                
                                <td></td>
                                
                                <td class="value number">
                                    ${fmt.formatNumber(totalHits)}
                                </td>
                                <td class="value number">
                                    100.0%
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list ipList?sort_by("ip") as ip>
                                <tr>
                                    <td class="key">
                                        ${ip.ip}
                                    </td>
                                    <td class="key">
                                        ${ip.host}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatNumber(ip.count)}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatPercentage2(ip.count?number / totalHits?number)}
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
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="4">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</#macro>
