<#import "../../common/util/format.ftl" as format>

<#macro render node baselineNode formatStr showValue position isInverse=false>
    <#local valStr = format.ensureString(node)>
    <#local baselineValStr = format.ensureString(baselineNode)>
    
    <#local value = "">
    <#if valStr?contains("null") && baselineValStr?contains("null")>
        <#local value = "(n/a)">
    <#elseif valStr?contains("null")>
        <#local value = "(removed)">
    <#elseif baselineValStr?contains("null")>
        <#local value = "(added)">
    <#else>
        <#local n = format.ensureNumber(valStr)>
        <#local b = format.ensureNumber(baselineValStr)>
        <#if b == 0 && n > 0>
            <#local value = "\u221E">
        <#elseif b == n>
            <#local value = 0>
        <#else>
            <#local value = (n * 100 / b) - 100>
        </#if>
    </#if>

    <#local colorClass = "">
    <#if value == "(n/a)" || value == "(removed)">
        <#local colorClass = "removed">
    <#elseif value == "(added)">
        <#local colorClass = "added">
    <#elseif value == "\u221E">
        <#local colorClass = "infinity">
    <#else>
        <#local classNumber = 0>
        <#if value < -99 || value > 99>
            <#local classNumber = 100>
        <#elseif value < 0>
            <#local classNumber = (-value)?ceiling>
        <#else>
            <#local classNumber = value?ceiling>
        </#if>
        
        <#if (value < 0) == isInverse>
            <#local colorClass = "n" + classNumber>
        <#else>
            <#local colorClass = "p" + classNumber>
        </#if>
    </#if>

    <#local percentageValue = "">
    <#if value == "(n/a)" || value == "(removed)" || value == "(added)">
        <#local percentageValue = value>
    <#elseif value == "\u221E">
        <#local percentageValue = "\u221E">
    <#else>
        <#if value > 0>
            <#local percentageValue = "+">
        </#if>
        <#local percentageValue = percentageValue + value?string("#,##0.0") + "%">
    </#if>

    <td class="value number ${colorClass} colorized"<#if position == 1> title="${format.formatTimerVal(node, formatStr)}"<#else> title="${format.formatTimerVal(baselineNode, formatStr)} -> ${format.formatTimerVal(node, formatStr)} (<#if (format.ensureNumber(node) > format.ensureNumber(baselineNode))>+</#if>${(format.ensureNumber(node) - format.ensureNumber(baselineNode))?string(formatStr)} / ${percentageValue})"</#if>>
        <#if showValue>
            <#if position gt 1>
                ${percentageValue}
            <#else>
                ${format.formatTimerVal(node, formatStr)}
            </#if>
        </#if>
    </td>
</#macro>
