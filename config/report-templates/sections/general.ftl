<#import "/sections/descriptions.ftl" as desc>
<#import "/common/util/format.ftl" as fmt>

<#macro general rootNode>
    <div class="section" id="general">
        <@desc.headline_general />

        <div class="content">
            <@desc.description_general />

            <div class="data">
                <table class="">
                    <thead>
                        <tr>
                            <th>Test Duration [hh:mm:ss]</th>
                            <th>Test Start</th>
                            <th>Test End</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <tr>
                            <td class="value">${fmt.formatMsecToH((rootNode.duration[0]!"0")?number * 1000)}</td>
                            <td class="value">${rootNode.startTime[0]!""}</td>
                            <td class="value">${rootNode.endTime[0]!""}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="charts">
                <div class="chart">
                    <img src="charts/ConcurrentUsers.webp" alt="Concurrent Users">
                </div>
                <div class="chart">
                    <img src="charts/RequestsPerSecond.webp" alt="Requests">
                </div>
                <div class="chart">
                    <img src="charts/RequestRuntime.webp" alt="Request Runtime">
                </div>
                <div class="chart">
                    <img src="charts/TransactionErrors.webp" alt="Transaction Errors">
                </div>
            </div>
        </div>
    </div>
</#macro>
