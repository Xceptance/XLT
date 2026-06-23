<#function ensureString val>
    <#if val?is_string><#return val?trim></#if>
    <#if val?is_number><#return val?c></#if>
    <#if val?is_sequence>
        <#if val?size == 0><#return ""></#if>
        <#return val[0]?string?trim>
    </#if>
    <#return val?string?trim>
</#function>

<#function formatMsecToH n1>
    <#local s1 = "" + ensureString(n1)>
    <#if s1 == "">
        <#return "&ndash;"?no_esc>
    </#if>
    <#local seconds = (s1?number / 1000)?floor>
    <#local h = (seconds / 3600)?floor>
    <#local min = ((seconds % 3600) / 60)?floor>
    <#local sec = (seconds % 60)?floor>
    <#return h?string("00") + ":" + min?string("00") + ":" + sec?string("00")>
</#function>

<#function formatNumber val format=",##0">
    <#local s = "" + ensureString(val)>
    <#if s == "">
        <#return "&ndash;"?no_esc>
    </#if>
    <#return s?number?string(format)>
</#function>

<#function formatPercentage val>
    <#local s = "" + ensureString(val)>
    <#if s == "">
        <#return "nan%">
    </#if>
    <#return s?number?string("0.0%")>
</#function>

<#function formatPercentage2 val>
    <#local s = "" + ensureString(val)>
    <#if s == "">
        <#return "nan%">
    </#if>
    <#return s?number?string("0.00%")>
</#function>

<#function formatPercentageValue val>
    <#local s = "" + ensureString(val)>
    <#if s == "">
        <#return "nan%">
    </#if>
    <#return s?number?string("0.00") + "%">
</#function>

<#function formatBytes bytes>
    <#local s = "" + ensureString(bytes)>
    <#if s == "">
        <#return "&ndash;"?no_esc>
    </#if>
    <#local b = s?number>
    <#if b gt 1099511627776>
        <#return (b / 1099511627776)?string(",##0.0") + " TB">
    <#elseif b gt 1073741824>
        <#return (b / 1073741824)?string(",##0.0") + " GB">
    <#elseif b gt 1048576>
        <#return (b / 1048576)?string(",##0.0") + " MB">
    <#elseif b gt 1024>
        <#return (b / 1024)?string(",##0.0") + " KB">
    <#else>
        <#return b?string(",##0") + " B">
    </#if>
</#function>

<#function formatRange min max isTime=false>
    <#local sMin = "" + ensureString(min)>
    <#local sMax = "" + ensureString(max)>
    
    <#if sMax == "" || sMax?number <= 0>
        <#return "nan">
    <#elseif sMin == sMax>
        <#if isTime>
            <#return formatMsecToH(sMin?number * 1000)>
        <#else>
            <#return formatNumber(sMax)?no_esc>
        </#if>
    <#else>
        <#local start = (sMin == "" || sMin?number < 0)?then(0, sMin?number)>
        <#if isTime>
            <#return formatMsecToH(start * 1000) + "..." + formatMsecToH(sMax?number * 1000)>
        <#else>
            <#return formatNumber(start) + "..." + formatNumber(sMax)>
        </#if>
    </#if>
</#function>

<#function isNumber val>
    <#if val?is_number><#return true></#if>
    <#if val?is_string>
        <#-- Attempt to parse as number, handle potential error -->
        <#attempt>
            <#local dummy = val?number>
            <#return true>
        <#recover>
            <#return false>
        </#attempt>
    </#if>
    <#return false>
</#function>

<#function convertIllegalCharactersInFileName filename>
    <#local result = filename?string>
    <#local result = result?replace("$", "$24")>
    <#local result = result?replace("?", "$3f")>
    <#local result = result?replace(":", "$3a")>
    <#local result = result?replace("/", "$2f")>
    <#local result = result?replace("#", "$23")>
    <#local result = result?replace("%", "$25")>
    <#local result = result?replace(",", "$2c")>
    <#local result = result?replace(";", "$3b")>
    <#local result = result?replace("*", "$2a")>
    <#local result = result?replace("|", "$7c")>
    <#local result = result?replace("\\", "$5c")>
    <#local result = result?replace("<", "$3c")>
    <#local result = result?replace(">", "$3e")>
    <#local result = result?replace("\"", "$22")>
    <#return result>
</#function>

<#function formatTimerVal node format=",##0">
    <#local val = "">
    <#if node?is_sequence>
        <#if node?size gt 0>
            <#local val = node[0]?string?trim>
        </#if>
    <#else>
        <#local val = node?string?trim>
    </#if>
    
    <#if val?has_content && val != "NaN">
        <#return val?replace(",", "")?number?string[format]>
    <#else>
        <#return "nan">
    </#if>
</#function>

<#function ensureNumber node default=0>
    <#if node?has_content>
        <#return node?string?replace(",", "")?number>
    <#else>
        <#return default>
    </#if>
</#function>
