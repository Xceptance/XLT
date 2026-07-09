<#import "/sections/descriptions.ftl" as descriptions>
<#import "/util/timer-section.ftl" as ts>

<#macro page_load_timings rootNode>
    <div class="section" id="custom-timer-summary">
        <@descriptions.headline_page_load_timings_summary />

        <div class="content">
            <@descriptions.description_page_load_timings_summary />

            <#local pltNode = rootNode.pageLoadTimings>
            <@ts.timer_section 
                elements=(pltNode?size gt 0)?then(pltNode?children?filter(c -> c?node_type == "element"), [])
                summaryElement=rootNode.summary.pageLoadTimings
                tableRowHeader="Page Load Timing Name"
                directory="pageLoadTimings"
                type="pageLoadTiming" />
        </div>
    </div>
</#macro>
