<#import "/sections/descriptions.ftl" as desc>

<#macro testcomment rootNode>
    <div id="comment" class="section">
        <@desc.headline_testcomment />

        <div class="content">
            <#if rootNode?has_content && rootNode.string?has_content>
                <#list rootNode.string as s>
                    <div class="paragraph">${s?no_esc}</div>
                </#list>
            <#else>
                <div class="paragraph">No comment was given.</div>
            </#if>
        </div>
    </div>
</#macro>
