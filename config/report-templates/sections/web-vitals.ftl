<#import "/sections/descriptions.ftl" as descriptions>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/common/util/filtered-footer-row.ftl" as ffr>

<#macro web_vitals rootNode>
    <div class="section" id="web-vitals-summary">
        <@descriptions.headline_web_vitals_summary />

        <div class="content">
            <@descriptions.description_web_vitals_summary />

            <#local vitalsListNode = rootNode.webVitalsList>
            <#local vitalsList = (vitalsListNode?size gt 0)?then(vitalsListNode?children?filter(c -> c?node_type == "element"), [])>
            <#local count = vitalsList?size>

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
                        <@totals.create_totals_td rows_in_table=count class="key" />
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                    <@ffr.filtered_footer_row />
                </tfoot>
                <#if count gt 0>
                    <tbody>
                        <#list vitalsList?sort_by("name") as vital>
                            <tr>
                                <td class="key">
                                    ${vital.name}
                                </td>
                                <td class="value number web-vital">
                                    <@web_vital_cell value=vital.fcp />
                                </td>
                                <td class="value number web-vital">
                                    <@web_vital_cell value=vital.lcp />
                                </td>
                                <td class="value number web-vital">
                                    <@web_vital_cell value=vital.cls unit="" format="#,##0.000" />
                                </td>
                                <td class="value number web-vital">
                                    <@web_vital_cell value=vital.fid />
                                </td>
                                <td class="value number web-vital">
                                    <@web_vital_cell value=vital.inp />
                                </td>
                                <td class="value number web-vital">
                                    <@web_vital_cell value=vital.ttfb />
                                </td>
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
</#macro>

<#macro web_vital_cell value unit="ms" format="#,##0">
    <#if value?has_content>
        <#local goodCount = value.goodCount?number>
        <#local improveCount = value.improveCount?number>
        <#local poorCount = value.poorCount?number>
        <#local totalCount = goodCount + improveCount + poorCount>

        <#local goodRatio = goodCount / totalCount>
        <#local improveRatio = improveCount / totalCount>
        <#local poorRatio = poorCount / totalCount>

        <#local rating = value.rating>

        <div class="web-vital-score web-vital-score-${rating}">
            ${value.score?number?string(format)}
            <#if unit != ""> ${unit}</#if>
        </div>

        <div class="web-vital-gauge">
            <span class="web-vital-gauge-arrow web-vital-gauge-arrow-${rating}"></span>
        </div>

        <div class="web-vital-bar">
            <@web_vital_bar_segment count=goodCount ratio=goodRatio rating="good" />
            <@web_vital_bar_segment count=improveCount ratio=improveRatio rating="improve" />
            <@web_vital_bar_segment count=poorCount ratio=poorRatio rating="poor" />
        </div>
    </#if>
</#macro>

<#macro web_vital_bar_segment count ratio rating>
    <div class="web-vital-bar-segment web-vital-bar-segment-${rating}" style="flex-grow: ${ratio}" title="${count} (${(ratio * 100)?string("#,##0.0")}%)">
    </div>
</#macro>
