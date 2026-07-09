<#import "/sections/descriptions.ftl" as desc>
<#import "/util/network-table.ftl" as nt>

<#macro network_summary rootNode>
    <div class="section" id="network">
        <@desc.headline_network_summary />

        <div class="content">
            <@desc.description_network_summary />

            <div class="data">
                <@nt.network_table rootNode=rootNode />
            </div>
        </div>
    </div>
</#macro>
