<#macro render node isInverse=false format="#,##0" unit="" neutral=false>
    <#local isInfinity = (node.relativeDifference?string)?contains("Infinity")>
    
    <#local value = "">
    <#if !node?has_content>
        <#local value = "(no data)">
    <#elseif !node.newValue?has_content>
        <#local value = "(removed)">
    <#elseif !node.oldValue?has_content>
        <#local value = "(added)">
    <#elseif isInfinity>
        <#local value = "&infin;">
    <#otherwise>
        <#if (node.relativeDifference > 0)>
            <#local value = "+" + node.relativeDifference?string("#,##0.00") + "%">
        <#else>
            <#local value = node.relativeDifference?string("#,##0.00") + "%">
        </#if>
    </#if>

    <#local colorClass = "">
    <#if !node?has_content>
        <#local colorClass = "">
    <#elseif !node.newValue?has_content>
        <#local colorClass = "removed">
    <#elseif !node.oldValue?has_content>
        <#local colorClass = "added">
    <#otherwise>
        <#local classNumber = 0>
        <#if (node.relativeDifference < -99 || node.relativeDifference > 99 || isInfinity)>
            <#local classNumber = 100>
        <#elseif (node.relativeDifference < 0)>
            <#local classNumber = (-node.relativeDifference)?ceil>
        <#otherwise>
            <#local classNumber = (node.relativeDifference)?ceil>
        </#if>
        
        <#if neutral>
            <#local colorClass = "a" + classNumber>
        <#elseif (node.relativeDifference < 0) == isInverse>
            <#local colorClass = "n" + classNumber>
        <#otherwise>
            <#local colorClass = "p" + classNumber>
        </#if>
    </#if>

    <td <#if node?has_content>title="${node.oldValue?string(format)} ${unit} -> ${node.newValue?string(format)} ${unit} (<#if (node.absoluteDifference > 0)>+</#if>${node.absoluteDifference?string(format)} ${unit})"</#if> 
        class="value number ${colorClass} colorized<#if isInfinity> infinity</#if>">
        ${value}
    </td>
</#macro>
