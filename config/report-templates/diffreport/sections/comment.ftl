<#macro render rootNode>
    <#if rootNode?has_content && rootNode.string?has_content>
        <div class="description">
            <#list rootNode.string as s>
                <div class="paragraph">
                    ${s?no_esc}
                </div>
            </#list>
        </div>
    </#if>
</#macro>
