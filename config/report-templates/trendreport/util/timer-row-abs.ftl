<#import "timer-cell.ftl" as timerCell>
<#import "../../common/util/create-totals-td.ftl" as createTotalsTd>

<#macro render_value_tds gid valueName showValues formatStr trendValues>
    <#local baseValue = trendValues[0]>
    <#list trendValues as trendValue>
        <#local node = trendValue[valueName]>
        <#local baselineNode = baseValue[valueName]>
        <@timerCell.render node=node baselineNode=baselineNode formatStr=formatStr showValue=showValues position=trendValue?index+1 isInverse=(valueName == "errors") />
    </#list>
</#macro>

<#macro render gid valueName showValues formatStr timerElement>
    <tr>
        <td class="key">
            <a href="#timerchart-${gid}">${timerElement.name}</a>
        </td>
        <@render_value_tds gid=gid valueName=valueName showValues=showValues formatStr=formatStr trendValues=timerElement.trendValues.trendValue />
    </tr>
</#macro>

<#macro render_summary gid valueName showValues formatStr rowsInTable summaryElement>
    <tr class="totals">
        <@createTotalsTd.render rowsInTable=rowsInTable />
        <@render_value_tds gid=gid valueName=valueName showValues=showValues formatStr=formatStr trendValues=summaryElement.trendValues.trendValue />
    </tr>
</#macro>
