<#macro create_totals_td rows_in_table class="key" description="Totals">
    <td class="${class}">${description}<#t>
        <#if rows_in_table == 0> (no entries)<#elseif rows_in_table == 1> (1 entry)<#else> (${rows_in_table} entries)</#if><#t>
    </td>
</#macro>
