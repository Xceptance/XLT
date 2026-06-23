<#import "/sections/descriptions.ftl" as desc>
<#import "/util/timer-section.ftl" as ts>

<#macro custom_timers>
    <div class="section" id="custom-timer-summary">
        <@desc.headline_custom_timer_summary />

        <div class="content">
            <@desc.description_custom_timer_summary />

            <@ts.timer_section elements=report.testreport.customTimers.* 
                               summaryElement=report.testreport.summary.customTimers
                               tableRowHeader="Custom Timer Name" 
                               directory="custom" 
                               type="custom" />
        </div>
    </div>
</#macro>
