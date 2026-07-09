<#import "descriptions.ftl" as desc>

<#macro configuration rootNode>
    <div class="section" id="configuration">
        <@desc.headline_configuration />

        <div class="content">
            <@desc.description_configuration />

            <div class="data">
                <table class="properties">
                    <thead>
                        <tr>
                            <th>Property Name</th>
                            <th>Property Value</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                            <td></td>
                        </tr>
                    </tfoot>
                            <tbody>
                                <#assign keys = []>
                                <#list rootNode.properties.property as p>
                                    <#assign keys = keys + [p["@name"]]>
                                </#list>
                                <#list keys?sort as key>
                                    <#list rootNode.properties.property as p>
                                        <#if p["@name"] == key>
                                            <tr>
                                                <td class="key">
                                                    ${p["@name"]}
                                                </td>
                                                <td class="value text wrap">
                                                    ${p["@value"]}
                                                </td>
                                            </tr>
                                            <#break>
                                        </#if>
                                    </#list>
                                </#list>
                            </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="section" id="jvm-configuration">
        <@desc.headline_jvm_configuration />

        <div class="content">
            <@desc.description_jvm_configuration />

            <div class="data">
                <table class="">
                    <thead>
                        <tr>
                            <th>Settings</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <#if rootNode.customJvmArgs.string?has_content>
                            <#list rootNode.customJvmArgs.string as arg>
                                <tr>
                                    <td class="value text">
                                        ${arg}
                                    </td>
                                </tr>
                            </#list>
                        <#else>
                             <tr>
                                <td class="no-data">No data available</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</#macro>
