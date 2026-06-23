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

<#import "/common/util/create-totals-td.ftl" as tt>
<#import "/util/timer-row.ftl" as tr>

<#macro timer_summary_row element type rowsInTable>
    <tr class="totals">
        <@tt.create_totals_td rows_in_table=rowsInTable class="key colgroup1" />

        <#-- labels -->
        <#if type == "transaction" || type == "action" || type == "request">
            <td class="colgroup1"></td>
        </#if>

        <#-- count -->
        <td class="value number">${formatTimerVal(element.count)}</td>

        <#-- distinct -->
        <#if type == "request">
            <td class="value number">
                <#-- value not available in the summary element -->
            </td>
        </#if>

        <#-- count per sec -->
        <td class="value number">${formatTimerVal(element.countPerSecond, ",##0.0")}</td>
        <#-- count per min -->
        <td class="value number">${formatTimerVal(element.countPerMinute)}</td>
        <#-- count per hour -->
        <td class="value number">${formatTimerVal(element.countPerHour)}</td>

        <#-- errors -->
        <#local errorsVal = ensureNumber(element.errors, 0)>
        <td class="value number colgroup1<#if errorsVal gt 0> error</#if>">${formatTimerVal(errorsVal, ",##0")}</td>
        <td class="value number colgroup1<#if errorsVal gt 0> error</#if>">${formatTimerVal(element.errorPercentage, ",##0.00")}%</td>

        <#-- events -->
        <#if type == "transaction">
            <#local eventsVal = ensureNumber(element.events)>
            <td class="value number colgroup1<#if eventsVal gt 0> event</#if>">${formatTimerVal(eventsVal, ",##0")}</td>
        </#if>

        <#-- mean -->
        <td class="value number">${formatTimerVal(element.mean)}</td>
        <#-- min -->
        <td class="value number">${formatTimerVal(element.min)}</td>
        <#-- max -->
        <td class="value number">${formatTimerVal(element.max)}</td>
        <#-- deviation -->
        <td class="value number">${formatTimerVal(element.deviation)}</td>

        <#-- runtime percentiles -->
        <#if element.percentiles?size gt 0>
            <#list element.percentiles[0]?children as p>
                <#if p?node_type == "element">
                    <td class="value number colgroup1">${formatTimerVal(p)}</td>
                </#if>
            </#list>
        </#if>

        <#-- apdex -->
        <#if type == "action">
            <#local apdexValue = (element.apdex?size gt 0)?then(ensureNumber(element.apdex[0].value, -1), -1)>
            <#local apdexColor = tr.convertApdexToColor(apdexValue)>
            <td class="value number ${apdexColor}">${(element.apdex?size gt 0)?then(element.apdex[0].longValue, "")}</td>
        </#if>

        <#-- runtime segmentation -->
        <#if type == "request">
            <#if element.countPerInterval?size gt 0>
                <#local intervals = element.countPerInterval[0].int![]>
                <#local percentages = (element.percentagePerInterval?has_content)?then(element.percentagePerInterval[0]["big-decimal"]![], [])>
                <#list intervals as intVal>
                    <#local pos = intVal?index>
                    <#local percentage = percentages[pos]!"NaN">
                    <td class="value number"><span title="${formatTimerVal(intVal)} (${formatTimerVal(percentage, ",##0.00")}%)">${formatTimerVal(percentage, ",##0.00")}%</span></td>
                </#list>
            </#if>
        </#if>
    </tr>
</#macro>
