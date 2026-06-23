<#import "/sections/descriptions.ftl" as descriptions>
<#import "/util/slowest-requests-table.ftl" as srt>

<#macro slowest_requests rootNode>
    <div class="section" id="slowest-requests-summary">
        <@descriptions.headline_slowest_requests_summary />

        <div id="progressmeter">
            <p>Loading...</p>
            <div class="loader"></div>
        </div>
        <div class="content hidden">
            <@descriptions.description_slowest_requests_summary rootNode=rootNode />

            <#local slowestRequestsNode = rootNode.slowestRequests>
            <#local requestsNode = rootNode.requests>
            <@srt.slowest_requests_table 
                slowestRequests=(slowestRequestsNode?size gt 0)?then(slowestRequestsNode?children?filter(c -> c?node_type == "element"), [])
                requests=(requestsNode?size gt 0)?then(requestsNode?children?filter(c -> c?node_type == "element"), []) />
        </div>
    </div>
</#macro>
