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

<#function ensureNumber node default="NaN">
    <#local val = "">
    <#if node?is_sequence>
        <#if node?size gt 0><#local val = node[0]?string?trim></#if>
    <#else>
        <#local val = node?string?trim>
    </#if>
    <#if val?has_content && val != "NaN">
        <#return val?replace(",", "")?number>
    <#else>
        <#return default>
    </#if>
</#function>

<#import "/util/timer-labels.ftl" as tl>

<#-- Colorize a table cell based on runtime colorization config -->
<#macro colorize classNames runtime targetAverage targetFrom targetTo inverted=false>
    <#local positivePrefix = inverted?then("n", "p")>
    <#local negativePrefix = inverted?then("p", "n")>

    <#if targetAverage gte 0 && targetFrom gte 0 && targetTo gte 0>
        <#if (runtime > targetAverage)>
            <#local percent = ((runtime - targetAverage) * (100.0 / (targetTo - targetAverage)))?floor>
            <#if percent gte 100>
                <#local _dummy><@extendClass classNames=classNames + " " + negativePrefix + "100" /></#local>
            <#elseif percent lte 0>
                <#local _dummy><@extendClass classNames=classNames + " " + negativePrefix + "0" /></#local>
            <#else>
                <#local _dummy><@extendClass classNames=classNames + " " + negativePrefix + percent?c /></#local>
            </#if>
        <#else>
            <#local percent = ((runtime - targetAverage) * (100.0 / (targetFrom - targetAverage)))?floor>
            <#if percent gte 100>
                <#local _dummy><@extendClass classNames=classNames + " " + positivePrefix + "100" /></#local>
            <#elseif percent lte 0>
                <#local _dummy><@extendClass classNames=classNames + " " + positivePrefix + "0" /></#local>
            <#else>
                <#local _dummy><@extendClass classNames=classNames + " " + positivePrefix + percent?c /></#local>
            </#if>
        </#if>
    </#if>
</#macro>

<#-- Extend the class attribute with the associated runtime color -->
<#macro extendClass classNames>
    <#-- This sets the class attribute on the parent td element -->
</#macro>

<#-- Get colorization class for a cell value -->
<#function getColorClass classNames runtime targetAverage targetFrom targetTo inverted=false>
    <#local positivePrefix = inverted?then("n", "p")>
    <#local negativePrefix = inverted?then("p", "n")>

    <#if (runtime?is_number) && targetAverage gte 0 && targetFrom gte 0 && targetTo gte 0>
        <#if (runtime > targetAverage)>
            <#local denom = targetTo - targetAverage>
            <#if denom == 0><#local denom = 1></#if>
            <#local percent = ((runtime - targetAverage) * (100.0 / denom))?floor>
            <#if percent gte 100>
                <#return classNames + " " + negativePrefix + "100 colorized">
            <#elseif percent lte 0>
                <#return classNames + " " + negativePrefix + "0 colorized">
            <#else>
                <#return classNames + " " + negativePrefix + percent?c + " colorized">
            </#if>
        <#else>
            <#local denom = targetFrom - targetAverage>
            <#if denom == 0><#local denom = 1></#if>
            <#local percent = ((runtime - targetAverage) * (100.0 / denom))?floor>
            <#if percent gte 100>
                <#return classNames + " " + positivePrefix + "100 colorized">
            <#elseif percent lte 0>
                <#return classNames + " " + positivePrefix + "0 colorized">
            <#else>
                <#return classNames + " " + positivePrefix + percent?c + " colorized">
            </#if>
        </#if>
    </#if>
    <#return classNames>
</#function>

<#-- Apdex color function -->
<#function convertApdexToColor apdex>
    <#if apdex gte 0.94>
        <#return "apdex-excellent">
    <#elseif apdex gte 0.85>
        <#return "apdex-good">
    <#elseif apdex gte 0.70>
        <#return "apdex-fair">
    <#elseif apdex gte 0.50>
        <#return "apdex-poor">
    <#else>
        <#return "apdex-unacceptable">
    </#if>
</#function>

<#macro timer_row element type gid="unknown">
    <#local name = element.name>
    <#local hasUrls = (element.urls?size > 0) && (element.urls[0].list?? || element.urls[0].total??)>

    <tr>
        <#-- name -->
        <td class="key colgroup1 forcewordbreak">
            <a href="#chart-${gid}" data-id="tableEntry-${gid}"<#if hasUrls> data-rel="#url-listing-${gid}" class="cluetip"</#if>>${name}</a> <#if hasUrls><div id="url-listing-${gid}" class="cluetip-data"><h4>${formatTimerVal(element.urls[0].total)} distinct URL(s)**</h4><ul class="urls"><#list element.urls[0].list[0].string![] as s><li><a href="${s}" target="_blank">${s}</a></li></#list></ul></div></#if>
        </td>

        <#-- labels -->
        <#if type == "transaction" || type == "action" || type == "request">
            <#local labelText = "">
            <#if element.labels?size gt 0>
                <#local labelText = element.labels[0]?trim>
            </#if>
            <td class="text colgroup1" data-cell-value="${labelText}">
                <#if labelText != "">
                    <@tl.timer_labels labelString=labelText gid=gid />
                </#if>
            </td>
        </#if>

        <#-- count -->
        <td class="value number">${formatTimerVal(element.count)}</td>

        <#if type == "request">
            <#-- distinct -->
            <td class="value number"><#if hasUrls>${formatTimerVal(element.urls[0].total)}</#if></td>
        </#if>

        <#-- count per sec -->
        <td class="value number">${formatTimerVal(element.countPerSecond, ",##0.0")}</td>
        <#-- count per min -->
        <td class="value number">${formatTimerVal(element.countPerMinute, ",##0.0")}</td>
        <#-- count per hour -->
        <td class="value number">${formatTimerVal(element.countPerHour)}</td>

        <#-- errors -->
        <#local errorsVal = ensureNumber(element.errors)>
        <td class="value number colgroup1<#if errorsVal gt 0> error</#if>">${formatTimerVal(errorsVal, ",##0")}</td>
        <td class="value number colgroup1<#if errorsVal gt 0> error</#if>">${formatTimerVal(element.errorPercentage, ",##0.00")}%</td>

        <#-- events -->
        <#if type == "transaction">
            <#local eventsVal = ensureNumber(element.events)>
            <td class="value number colgroup1<#if eventsVal gt 0> event</#if>">${formatTimerVal(eventsVal, ",##0")}</td>
        </#if>

        <#-- colorization config -->
        <#local colorGroup = "">
        <#if element.colorizationGroupName?size gt 0>
            <#local colorGroup = element.colorizationGroupName>
        </#if>
        <#local colorizationConfigs = report.testreport.testReportConfig.requestTableColorization.colorization![]>
        <#local colorizationConfig = {}>
        <#list colorizationConfigs as cc>
            <#if cc.@groupName == colorGroup>
                <#local colorizationConfig = cc>
                <#break>
            </#if>
        </#list>
        <#local hasColorization = colorizationConfig?has_content && colorizationConfig?is_hash>

        <#-- mean -->
        <#local classNames = "value number">
        <#if hasColorization>
            <#local meanRule = {}>
            <#list colorizationConfig.rules[0].rule![] as r><#if r.@id == "mean"><#local meanRule = r><#break></#if></#list>
            <#if meanRule?has_content>
                <#local classNames = getColorClass(classNames, ensureNumber(element.mean), ensureNumber(meanRule.@target), ensureNumber(meanRule.@from), ensureNumber(meanRule.@to))>
            </#if>
        </#if>
        <td class="${classNames}">${formatTimerVal(element.mean)}</td>

        <#-- min -->
        <#local classNames = "value number">
        <#if hasColorization>
            <#local minRule = {}>
            <#list colorizationConfig.rules[0].rule![] as r><#if r.@id == "min"><#local minRule = r><#break></#if></#list>
            <#if minRule?has_content>
                <#local classNames = getColorClass(classNames, ensureNumber(element.min), ensureNumber(minRule.@target), ensureNumber(minRule.@from), ensureNumber(minRule.@to))>
            </#if>
        </#if>
        <td class="${classNames}">${formatTimerVal(element.min)}</td>

        <#-- max -->
        <#local classNames = "value number">
        <#if hasColorization>
            <#local maxRule = {}>
            <#list colorizationConfig.rules[0].rule![] as r><#if r.@id == "max"><#local maxRule = r><#break></#if></#list>
            <#if maxRule?has_content>
                <#local classNames = getColorClass(classNames, ensureNumber(element.max), ensureNumber(maxRule.@target), ensureNumber(maxRule.@from), ensureNumber(maxRule.@to))>
            </#if>
        </#if>
        <td class="${classNames}">${formatTimerVal(element.max)}</td>

        <#-- deviation -->
        <td class="value number">${formatTimerVal(element.deviation)}</td>

        <#-- runtime percentiles -->
        <#if element.percentiles?size gt 0>
            <#list element.percentiles[0]?children as p>
                <#if p?node_type == "element">
                    <#local pId = p?node_name>
                    <#local classNames = "value number colgroup1">
                    <#if hasColorization>
                        <#local pRule = {}>
                        <#list colorizationConfig.rules[0].rule![] as r><#if r.@type! == "percentile" && r.@id == pId><#local pRule = r><#break></#if></#list>
                        <#if pRule?has_content>
                            <#local classNames = getColorClass(classNames, ensureNumber(p), ensureNumber(pRule.@target), ensureNumber(pRule.@from), ensureNumber(pRule.@to))>
                        </#if>
                    </#if>
                    <td class="${classNames}">${formatTimerVal(p)}</td>
                </#if>
            </#list>
        </#if>

        <#-- apdex -->
        <#if type == "action">
            <#local apdexValue = (element.apdex?size gt 0)?then(ensureNumber(element.apdex[0].value, -1), -1)>
            <#local apdexColor = convertApdexToColor(apdexValue)>
            <td class="value number apdex ${apdexColor}">${(element.apdex?size gt 0)?then(element.apdex[0].longValue, "")}</td>
        </#if>

        <#-- runtime segmentation -->
        <#if type == "request">
            <#if element.countPerInterval?size gt 0>
                <#local intervals = element.countPerInterval[0].int![]>
                <#local percentages = (element.percentagePerInterval?has_content)?then(element.percentagePerInterval[0]["big-decimal"]![], [])>
                <#local runtimeIntervalsConfig = report.testreport.testReportConfig.runtimeIntervals.interval![]>
                <#list intervals as intVal>
                    <#local pos = intVal?index>
                    <#local isLast = !intVal?has_next>
                    <#local percentage = percentages[pos]!"NaN">
                    <#local intervalId = (runtimeIntervalsConfig[pos].@to)!"">
                    <#local classNames = "value number">
                    <#if hasColorization>
                        <#local segRule = {}>
                        <#list colorizationConfig.rules[0].rule![] as r><#if r.@type! == "segmentation" && r.@id == intervalId><#local segRule = r><#break></#if></#list>
                        <#if segRule?has_content>
                            <#local classNames = getColorClass(classNames, ensureNumber(percentage), ensureNumber(segRule.@target), ensureNumber(segRule.@from), ensureNumber(segRule.@to), !isLast)>
                        </#if>
                    </#if>
                    <td class="${classNames}"><span title="${formatTimerVal(intVal)} (${formatTimerVal(percentage, ",##0.00")}%)">${formatTimerVal(percentage, ",##0.00")}%</span></td>
                </#list>
            </#if>
        </#if>
    </tr>
</#macro>
