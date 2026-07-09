<#import "timer-cell.ftl" as timerCell>
<#import "../../common/util/create-totals-td.ftl" as createTotalsTd>

<#macro render_value_tds node type>
    <#if type == "request">
        <#-- distinct -->
        <@timerCell.render node=node.urls.total isInverse=true format="#,##0" />
    </#if>

    <#-- count -->
    <@timerCell.render node=node.count isInverse=true format="#,##0" />

    <#-- count per sec -->
    <@timerCell.render node=node.countPerSecond isInverse=true format="#,##0.0" />

    <#-- count per minute -->
    <@timerCell.render node=node.countPerMinute isInverse=true format="#,##0.0" />

    <#-- count per hour -->
    <@timerCell.render node=node.countPerHour isInverse=true format="#,##0" />

    <#-- errors -->
    <@timerCell.render node=node.errors format="#,##0" />

    <#-- events -->
    <#if type == "transaction">
        <@timerCell.render node=node.events format="#,##0" />
    </#if>

    <#-- mean -->
    <@timerCell.render node=node.mean format="#,##0" />

    <#-- min -->
    <@timerCell.render node=node.min format="#,##0" />

    <#-- max -->
    <@timerCell.render node=node.max format="#,##0" />

    <#-- deviation -->
    <@timerCell.render node=node.deviation format="#,##0" />
    
    <#-- Pxx values -->
    <#if node.percentiles?has_content>
        <#list node.percentiles?children as p>
            <@timerCell.render node=p format="#,##0" />
        </#list>
    </#if>
</#macro>

<#macro render timerElement type>
    <tr>
        <#-- name -->
        <td class="key">${timerElement.name}</td>
        
        <@render_value_tds node=timerElement type=type />
    </tr>
</#macro>

<#macro render_summary summaryElement type rowsInTable>
    <tr class="totals">
        <@createTotalsTd.create_totals_td rows_in_table=rowsInTable />
        
        <@render_value_tds node=summaryElement type=type />
    </tr>
</#macro>
