<#macro render node isInverse=false format="#,##0" unit="" neutral=false>
    <#-- Safely check for relativeDifference existence first -->
    <#local hasRelDiff = (node.relativeDifference?size > 0)>
    <#local isInfinity = false>
    <#local relDiff = 0>
    <#if hasRelDiff>
        <#local relDiffStr = node.relativeDifference?string?trim>
        <#local isInfinity = relDiffStr?contains("Infinity")>
        <#if !isInfinity>
            <#local relDiff = relDiffStr?number>
        </#if>
    </#if>
    
    <#local value = "">
    <#if !node?has_content>
        <#local value = "(no data)">
    <#elseif (node.newValue?size == 0)>
        <#local value = "(removed)">
    <#elseif (node.oldValue?size == 0)>
        <#local value = "(added)">
    <#elseif isInfinity>
        <#local value = "&infin;">
    <#else>
        <#if (relDiff > 0)>
            <#local value = "+" + relDiff?string("#,##0.00") + "%">
        <#else>
            <#local value = relDiff?string("#,##0.00") + "%">
        </#if>
    </#if>

    <#local colorClass = "">
    <#if !node?has_content>
        <#local colorClass = "">
    <#elseif (node.newValue?size == 0)>
        <#local colorClass = "removed">
    <#elseif (node.oldValue?size == 0)>
        <#local colorClass = "added">
    <#else>
        <#local classNumber = 0>
        <#if (relDiff < -99 || relDiff > 99 || isInfinity)>
            <#local classNumber = 100>
        <#elseif (relDiff < 0)>
            <#local classNumber = (-relDiff)?ceiling>
        <#else>
            <#local classNumber = relDiff?ceiling>
        </#if>
        
        <#if neutral>
            <#local colorClass = "a" + classNumber>
        <#elseif (relDiff < 0) == isInverse>
            <#local colorClass = "n" + classNumber>
        <#else>
            <#local colorClass = "p" + classNumber>
        </#if>
    </#if>

    <#local hasAbsDiff = (node?has_content && node.absoluteDifference?size > 0)>
    <#local absDiff = 0>
    <#if hasAbsDiff>
        <#local absDiff = node.absoluteDifference?string?trim?number>
    </#if>
    <#local hasOldNew = (node?has_content && node.oldValue?size > 0 && node.newValue?size > 0)>

    <td <#if hasOldNew>title="${node.oldValue?string?trim?number?string(format)} ${unit} -> ${node.newValue?string?trim?number?string(format)} ${unit} (<#if (absDiff > 0)>+</#if>${absDiff?string(format)} ${unit})"</#if> 
        class="value number ${colorClass} colorized<#if isInfinity> infinity</#if>">
        ${value}
    </td>
</#macro>
