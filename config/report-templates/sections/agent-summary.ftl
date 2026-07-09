<#import "/sections/descriptions.ftl" as desc>

<#macro agent_summary rootNode>
    <div class="section" id="agents">
        <@desc.headline_agent_summary />

        <div class="content">
            <@desc.description_agent_summary />

            <div class="charts">
                <div class="chart">
                    <img src="charts/agents/All%20Agents/CpuUsage.webp" alt="&#34;charts/agents/All Agents/CpuUsage.webp&#34;">
                </div>
            </div>
        </div>
    </div>
</#macro>
