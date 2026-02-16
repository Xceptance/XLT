<#import "/util/timer-section.ftl" as ts>
<#import "/sections/descriptions.ftl" as desc>

<#macro transactions rootNode>
    <#if rootNode.transactions?has_content>
        <div class="section" id="transaction-summary">
            <@desc.headline_transaction_summary />

            <div class="content">
                <@desc.description_transaction_summary />

                <@ts.timer_section
                    elements=rootNode.transactions[0]?children?filter(c -> c?node_type == "element")
                    summaryElement=(rootNode.summary[0].transactions)!
                    tableRowHeader="Transaction Name"
                    directory="transactions"
                    type="transaction" />
            </div>
        </div>
    </#if>
</#macro>
