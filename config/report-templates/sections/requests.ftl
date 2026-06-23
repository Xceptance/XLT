<#import "/util/timer-section.ftl" as timer>
<#import "/sections/descriptions.ftl" as desc>

<#macro requests rootNode>
    <#if rootNode.requests?has_content>
        <div class="section" id="request-summary">
            <@desc.headline_request_summary />
            <div id="progressmeter">
                <p>Loading...</p>
                <div class="loader"></div>
            </div>

            <div class="content hidden">
                <@desc.description_request_summary />

                <@timer.timer_section elements=rootNode.requests.request 
                                      summaryElement=(rootNode.summary[0].requests)! 
                                      tableRowHeader="Request Name" 
                                      directory="requests" 
                                      type="request" />
            </div>
        </div>
    </#if>
</#macro>
