<#import "/sections/descriptions.ftl" as descriptions>
<#import "/common/util/format.ftl" as format>

<#macro external rootNode>
    <#if rootNode.genericReport?size gt 0>
        <#list rootNode.genericReport as genericReport>
            <div class="section">
                <h2>${genericReport.headline}</h2>
                <div class="content">
                    <@descriptions.external_data_section_description description=genericReport.description />

                    <#list genericReport.tables.table as table>
                        <h3>${table.title}</h3>

                        <div class="data">
                            <table>
                                <#if table.headRow?? && table.headRow.cells.cell??>
                                    <thead>
                                        <tr>
                                            <#list table.headRow.cells.cell as cell>
                                                <th>${cell}</th>
                                            </#list>
                                        </tr>
                                    </thead>
                                </#if>

                                <tbody>
                                    <#if table.bodyRows.row??>
                                        <#list table.bodyRows.row as row>
                                            <tr>
                                                <#list row.cells.cell as cell>
                                                    <#-- Determine if value is numeric. XStream XML uses tag names like 'int', 'double' but FreeMarker NodeModel might just give text if wrap(doc) is used without specific configuration -->
                                                    <#-- However, we can check if it's a number using our format function -->
                                                    <#if format.isNumber(cell)>
                                                        <td class="value number count">${format.formatNumber(cell, "#,##0.###")?no_esc}</td>
                                                    <#else>
                                                        <td class="key">${cell}</td>
                                                    </#if>
                                                </#list>
                                            </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div><#-- end data -->
                    </#list>

                    <#if genericReport.chartFileNames.string??>
                        <div class="charts">
                            <#list genericReport.chartFileNames.string as chartFileName>
                                <#assign encodedChartFilename = format.convertIllegalCharactersInFileName(chartFileName)>
                                <div class="chart">
                                    <img src="charts/external/${encodedChartFilename}.webp" alt="Hits" />
                                </div><#-- end chart -->
                            </#list>
                        </div><#-- end charts -->
                    </#if>
                </div><#-- end content -->
            </div><#-- end section -->
        </#list>
    <#else>
        <div class="section" id="external-summary">
            <@descriptions.headline_external />
            <@descriptions.description_external />
        </div>
    </#if>
</#macro>
