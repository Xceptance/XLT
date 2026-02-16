<#import "/common/util/format.ftl" as fmt>

<#macro network_table rootNode>
    <table class="">
        <thead>
            <tr>
                <th></th>
                <th>Total</th>
                <th>1/s</th>
                <th>1/min</th>
                <th>1/h*</th>
                <th>1/d*</th>
            </tr>
        </thead>
        <tfoot>
            <tr>
                <td></td><td></td><td></td><td></td><td></td><td></td>
            </tr>
        </tfoot>
        <tbody>
            <#assign duration = (rootNode.duration[0]!"0")?number>
            <#if duration == 0><#assign duration = 1></#if>
            <tr>
                <#assign hits = (rootNode.hits[0]!"0")?number>
                <#assign hitsPerSecond = hits / duration>
                <td class="key">Requests</td>
                <td class="value">${fmt.formatNumber(hits)}</td>
                <td class="value">${fmt.formatNumber(hitsPerSecond)}</td>
                <td class="value">${fmt.formatNumber(hitsPerSecond * 60)}</td>
                <td class="value">${fmt.formatNumber(hitsPerSecond * 3600)}</td>
                <td class="value">${fmt.formatNumber(hitsPerSecond * 86400)}</td>
            </tr>
            <tr>
                <#assign bytesSent = (rootNode.bytesSent[0]!"0")?number>
                <#assign bytesSentPerSecond = bytesSent / duration>
                <td class="key">Bytes Sent</td>
                <td class="value">${fmt.formatBytes(bytesSent)}</td>
                <td class="value">${fmt.formatBytes(bytesSentPerSecond)}</td>
                <td class="value">${fmt.formatBytes(bytesSentPerSecond * 60)}</td>
                <td class="value">${fmt.formatBytes(bytesSentPerSecond * 3600)}</td>
                <td class="value">${fmt.formatBytes(bytesSentPerSecond * 86400)}</td>
            </tr>
            <tr>
                <#assign bytesReceived = (rootNode.bytesReceived[0]!"0")?number>
                <#assign bytesReceivedPerSecond = bytesReceived / duration>
                <td class="key">Bytes Received</td>
                <td class="value">${fmt.formatBytes(bytesReceived)}</td>
                <td class="value">${fmt.formatBytes(bytesReceivedPerSecond)}</td>
                <td class="value">${fmt.formatBytes(bytesReceivedPerSecond * 60)}</td>
                <td class="value">${fmt.formatBytes(bytesReceivedPerSecond * 3600)}</td>
                <td class="value">${fmt.formatBytes(bytesReceivedPerSecond * 86400)}</td>
            </tr>
        </tbody>
    </table>
</#macro>
