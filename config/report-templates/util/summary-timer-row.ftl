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

<#macro summary_timer_row name element link>
    <tr>
        <#-- name -->
        <td class="key">
            <a href="${link}.html">${name}</a>
        </td>

        <#-- count -->
        <td class="value number">${formatTimerVal(element.count)}</td>

        <#-- count per sec -->
        <td class="value number">${formatTimerVal(element.countPerSecond, ",##0.0")}</td>

        <#-- count per hour -->
        <td class="value number">${formatTimerVal(element.countPerHour)}</td>

        <#-- count per day -->
        <td class="value number">${formatTimerVal(element.countPerDay)}</td>

        <#-- errors -->
        <#local errorsVal = (element.errors?has_content)?then(element.errors?number, 0)>
        <td class="value number ${(errorsVal gt 0)?then("error", "")}">${formatTimerVal(element.errors)}</td>

        <#-- % errors -->
        <td class="value number ${(errorsVal gt 0)?then("error", "")}">${formatTimerVal(element.errorPercentage, ",##0.00")}%</td>

        <#-- events -->
        <#local eventsVal = (element.events?has_content)?then(element.events?number, 0)>
        <td class="value number ${(element.events?has_content && eventsVal gt 0)?then("colgroup1 event", "")}">
            <#if element.events?has_content>
                ${formatTimerVal(element.events)}
            <#else>
                &ndash;
            </#if>
        </td>

        <#-- mean -->
        <td class="value number">${formatTimerVal(element.mean)}</td>

        <#-- min -->
        <td class="value number">${formatTimerVal(element.min)}</td>

        <#-- max -->
        <td class="value number">${formatTimerVal(element.max)}</td>

        <#-- deviation -->
        <td class="value number">${formatTimerVal(element.deviation)}</td>

        <#-- runtime percentiles -->
        <#if element.percentiles?has_content>
            <#list element.percentiles.* as p>
                <td class="value number">${formatTimerVal(p)}</td>
            </#list>
        </#if>
    </tr>
</#macro>
