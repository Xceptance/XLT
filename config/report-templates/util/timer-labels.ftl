<#-- Template for showing up to two labels directly and hiding all other labels behind a hoverable "+X" element. -->
<#macro timer_labels labelString gid="">
    <#local normalizedText = labelString?trim>
    <#if normalizedText != "">
        <#local labels = normalizedText?split("\\s+", "r")>
        <#local count = labels?size>
        <#if count == 1>
            <@label_tag text=labels[0] />
        <#elseif count == 2>
            <@label_tag text=labels[0] />
            <@label_tag text=labels[1] />
        <#elseif count gte 3>
            <@label_tag text=labels[0] />
            <@label_tag text=labels[1] />
            <span class="label-chip label-chip-more"><a href="" onclick="return false;" data-rel="#more-labels-${gid}" class="cluetip">+${count - 2}</a> <div id="more-labels-${gid}" class="cluetip-data"><div class="label-container"><#list labels as l><#if l?index gte 2><@label_tag text=l /></#if></#list></div></div></span>
        </#if>
    </#if>
</#macro>

<#macro label_tag text>
    <span class="label-chip" title="${text}">${text}</span>
</#macro>
