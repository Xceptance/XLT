<#macro name_or_id node>
    <#if node.@name[0]?? && node.@name[0]?has_content>
        <#assign nodeName = node.@name[0]!"">
        <#assign nodeId = node.@id[0]!"">
        <span title="${nodeId}">${nodeName}</span>
    <#else>
        ${node.@id[0]!""}
    </#if>
</#macro>

<#macro multi_row_cell numRows=1 class="">
    <td<#if numRows gt 1> rowspan="${numRows}"</#if><#if class?has_content> class="${class}"</#if>>
        <#nested>
    </td>
</#macro>

<#macro item_list items separator=", ">
    <#list items as item>
        <#assign itemId = (item.@id[0]!"")?trim>
        <#assign itemName = (item.@name[0]!"")?trim>
        <#if itemName?has_content>
            <span title="${itemId}">"${itemName}"</span>
        <#else>
            <span>"${itemId}"</span>
        </#if><#if item_has_next>${separator}</#if>
    </#list>
</#macro>

<#-- The show and hide part -->
<#macro show_n_hide gid>
    <span id="more-${gid}-show" onclick="$('#more-${gid}').show();$('#more-${gid}-hide').show(); $(this).hide();"
        class="link more-show">More...</span>
    <span id="more-${gid}-hide" onclick="$('#more-${gid}').hide();$('#more-${gid}-show').show(); $(this).hide();"
        class="link more-hide">Hide...</span>
</#macro>
