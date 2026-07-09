<#import "/util/timer-section.ftl" as timer>
<#import "/sections/descriptions.ftl" as desc>

<#macro actions rootNode>
    <#if rootNode.actions?has_content>
        <div class="section" id="action-summary">
            <@desc.headline_action_summary />

            <div class="content">
                <@desc.description_action_summary />

                <@timer.timer_section elements=rootNode.actions.action 
                                      summaryElement=(rootNode.summary[0].actions)! 
                                      tableRowHeader="Action Name" 
                                      directory="actions" 
                                      type="action" />
            </div>
        </div>
    </#if>
</#macro>
