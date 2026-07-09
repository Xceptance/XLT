<#import "/sections/descriptions.ftl" as desc>
<#import "/util/network-table.ftl" as networkTable>

<#macro network rootNode>
    <div class="section" id="network">
        <@desc.headline_network_summary />

        <div class="content">
            <@desc.description_network />

            <div class="data">
                <@networkTable.network_table rootNode=rootNode />
            </div>

            <div class="charts">
                <div class="chart">
                    <img src="charts/RequestsPerSecond.webp" alt="Requests" />
                </div>
                <div class="chart">
                    <img src="charts/SentBytesPerSecond.webp" alt="Sent Bytes" />
                </div>
                <div class="chart">
                    <img src="charts/ReceivedBytesPerSecond.webp" alt="Received Bytes" />
                </div>
            </div>
        </div>
    </div>
</#macro>
