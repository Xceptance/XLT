<#import "/common/util/format.ftl" as fmt>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/sections/descriptions.ftl" as desc>

<#macro content_types rootNode totalHits>
    <div class="section" id="content-types">
        <@desc.headline_content_types />

        <div class="content">
            <@desc.description_content_types />

            <div class="data">
                <table class="table-autosort:0">
                    <thead>
                        <tr>
                            <th class="table-sortable:alphanumeric" id="sortByContentType">Content Type</th>
                            <th class="table-sortable:numeric" id="sortByContentTypeCount">Count</th>
                            <th class="table-sortable:numeric" id="sortByContentTypePercentage">Percentage</th>
                        </tr>
                    </thead>
                    <#assign typeList = rootNode.contentType![]>
                    <#if typeList?size gt 0>
                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=typeList?size />

                                <td class="value number">
                                    ${fmt.formatNumber(totalHits)}
                                </td>
                                <td class="value number">
                                    100.0%
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list typeList?sort_by("contentType") as ct>
                                <tr>
                                    <td class="key">
                                        ${ct.contentType}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatNumber(ct.count)}
                                    </td>
                                    <td class="value">
                                        ${fmt.formatPercentage2(ct.count?number / totalHits?number)}
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
