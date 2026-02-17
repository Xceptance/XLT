<#import "../../common/util/format.ftl" as format>

<#macro render rootNode name id>
    <div class="subsection" id="${id}">
        <div class="content">
            <div class="data">
                <table class="">
                    <thead>
                        <tr>
                            <th>Name / Directory</th>
                            <th>Test Start</th>
                            <th>Test End</th>
                            <th>Test Duration</th>
                            <th>Total Hits</th>
                            <th>Average Hits Per Second</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <tr>
                            <td class="centeredtext">
                                ${name}
                            </td>
                            <td class="centeredtext">
                                ${rootNode.startTime}
                            </td>
                            <td class="centeredtext">
                                ${rootNode.endTime}
                            </td>
                            <td class="centeredtext">
                                <#local durationInH = format.formatMsecToH(rootNode.duration?trim?number * 1000)>
                                ${durationInH}
                            </td>
                            <td class="centeredtext">
                                ${rootNode.hits?trim?number?string("#,##0")}
                            </td>
                            <td class="centeredtext">
                                <#if rootNode.duration?trim?number gt 0>
                                    ${(rootNode.hits?trim?number / rootNode.duration?trim?number)?string("#,##0")}
                                <#else>
                                    0
                                </#if>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</#macro>
