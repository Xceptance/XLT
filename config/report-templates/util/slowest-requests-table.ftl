<#import "/common/util/format.ftl" as format>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/common/util/filtered-footer-row.ftl" as ffr>

<#macro slowest_requests_table slowestRequests requests>
    <table class="c-tab-content table-autosort:1 table-autosort-order:desc">
        <thead>
            <tr>
                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                    Request Name
                    <br/>
                    <form>
                        <input class="filter" placeholder="Enter filter substrings" title="" data-filter-id="filterByName" data-col-index="0"/>
                        <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                    </form>
                </th>
                <th rowspan="2" class="table-sortable:numeric" id="sortByRuntime" title="Request Runtime">Runtime [ms]</th>
                <th colspan="2" class="colgroup1">Baseline [ms]</th>
                <th colspan="2">Bandwidth [Bytes]</th>
                <th colspan="7" class="colgroup1">Network Timing [ms]</th>
                <th colspan="2">Request Details</th>
                <th colspan="2" class="colgroup1">IP Addresses</th>
            </tr>
            <tr>
                <th class="table-sortable:numeric colgroup1" id="sortByMean" title="Base Mean Runtime">Mean</th>
                <th class="table-sortable:numeric colgroup1" id="sortByP95" title="Base P95 Runtime">P95</th>
                <th class="table-sortable:numeric" id="sortByBytesSent" title="Bytes Sent">Sent</th>
                <th class="table-sortable:numeric" id="sortByBytesReceived" title="Bytes Received">Received</th>
                <th class="table-sortable:numeric colgroup1" id="sortByDnsTime" title="DNS Time">DNS</th>
                <th class="table-sortable:numeric colgroup1" id="sortByConnectTime" title="Connect Time">Connect</th>
                <th class="table-sortable:numeric colgroup1" id="sortBySendTime" title="Send Time">Send</th>
                <th class="table-sortable:numeric colgroup1" id="sortByServerBusyTime" title="Server Busy Time">Server</th>
                <th class="table-sortable:numeric colgroup1" id="sortByReceiveTime" title="Receive Time">Receive</th>
                <th class="table-sortable:numeric colgroup1" id="sortByTimeToFirstBytes" title="Time To First Bytes">TTFB</th>
                <th class="table-sortable:numeric colgroup1" id="sortByTimeToLastBytes" title="Time To Last Bytes">TTLB</th>
                <th class="table-sortable:numeric" id="sortByResponseCode" title="Response Code">Response</th>
                <th class="table-sortable:alphanumeric" id="sortByTime" title="Request Start Time">Time</th>
                <th class="table-sortable:alphanumeric colgroup1" id="sortByUsedIpAddress" title="Used IP Address">Used</th>
                <th class="table-sortable:alphanumeric colgroup1" id="sortByReportedIpAddresses" title="IP Addresses Reported By DNS">Reported</th>
            </tr>
        </thead>

        <#if slowestRequests?size gt 0>
            <tfoot>
                <tr class="totals">
                    <@totals.create_totals_td rows_in_table=slowestRequests?size class="key colgroup1" />
                    <td></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td></td>
                    <td></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                    <td></td>
                    <td></td>
                    <td class="colgroup1"></td>
                    <td class="colgroup1"></td>
                </tr>
                <@ffr.filtered_footer_row />
            </tfoot>
            <tbody>
                <#list slowestRequests as sr>
                    <#assign name = sr.name>
                    <#-- Baseline lookup -->
                    <#assign r = requests?filter(i -> i.name == name)?first!{}>
                    <#assign mean = r.mean!"">
                    <#assign p95 = (r.percentiles.p95)!"">
                    <#assign gid = "id" + sr?counter>

                    <tr>
                        <td class="key">
                            <@slow_request_name_with_cluetip 
                                name=sr.name 
                                requestId=sr.requestId!""
                                httpMethod=sr.httpMethod!""
                                url=sr.url!""
                                formDataEncoding=sr.formDataEncoding!""
                                formData=sr.formData!""
                                responseId=sr.responseId!""
                                responseCode=sr.responseCode!""
                                contentType=sr.contentType!""
                                gid=gid />
                        </td>
                        <td class="value number">${format.formatNumber(sr.runtime, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(mean, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(p95, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.bytesSent, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.bytesReceived, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.dnsTime, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.connectTime, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.sendTime, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.serverBusyTime, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.receiveTime, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.timeToFirstBytes, "#,##0")?no_esc}</td>
                        <td class="value number">${format.formatNumber(sr.timeToLastBytes, "#,##0")?no_esc}</td>
                        <td class="value number">${sr.responseCode!""}</td>
                        <td class="value">${sr.time!""}</td>
                        <td class="value">${sr.usedIpAddress!""}</td>
                        <td class="value">
                            <@ip_addresses ipAddresses=sr.ipAddresses gid=gid />
                        </td>
                    </tr>
                </#list>
            </tbody>
        <#else>
            <tfoot>
                <td></td>
                <td></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td></td>
                <td></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
                <td></td>
                <td></td>
                <td class="colgroup1"></td>
                <td class="colgroup1"></td>
            </tfoot>
            <tbody>
                <tr>
                    <td class="no-data" colspan="17">No data available</td>
                </tr>
            </tbody>
        </#if>
    </table>
</#macro>

<#macro slow_request_name_with_cluetip name requestId httpMethod url formDataEncoding formData responseId responseCode contentType gid>
    <#assign showRequestInfo = (requestId != "") || (httpMethod != "") || (url != "") || (formDataEncoding != "") || (formData != "")>
    <#assign showResponseInfo = (responseId != "") || (responseCode != "") || (contentType != "")>

    <#if showRequestInfo || showResponseInfo>
        <a href="" onclick="return false;" data-rel="#request-details-${gid}" class="cluetip">${name}</a>
        <div id="request-details-${gid}" class="cluetip-data cluetip-request-info">
            <table class="cluetip-table">
                <tbody>
                    <#if showRequestInfo>
                        <tr><th colspan="2">Request</th></tr>
                        <#if requestId != "">
                            <tr>
                                <td>ID</td>
                                <td>${requestId}</td>
                            </tr>
                        </#if>
                        <#if httpMethod != "">
                            <tr>
                                <td>Method</td>
                                <td>${httpMethod}</td>
                            </tr>
                        </#if>
                        <#if url != "">
                            <tr>
                                <td>URL</td>
                                <td>
                                    <a target="_blank" href="${url}">${url}</a>
                                </td>
                            </tr>
                        </#if>
                        <#if formDataEncoding != "">
                            <tr>
                                <td>Form Data Encoding</td>
                                <td>${formDataEncoding}</td>
                            </tr>
                        </#if>
                        <#if formData != "">
                            <tr>
                                <td>Form Data</td>
                                <td>${formData}</td>
                            </tr>
                        </#if>
                    </#if>
                    <#if showResponseInfo>
                        <tr><th colspan="2">Response</th></tr>
                        <#if responseId != "">
                            <tr>
                                <td>ID</td>
                                <td>${responseId}</td>
                            </tr>
                        </#if>
                        <#if responseCode != "">
                            <tr>
                                <td>Code</td>
                                <td>${responseCode}</td>
                            </tr>
                        </#if>
                        <#if contentType != "">
                            <tr>
                                <td>Content Type</td>
                                <td>${contentType}</td>
                            </tr>
                        </#if>
                    </#if>
                </tbody>
            </table>
        </div>
    <#else>
        ${name}
    </#if>
</#macro>

<#macro ip_addresses ipAddresses gid>
    <#local ipList = ipAddresses.string![]>
    <#local ipAddressCount = ipList?size>

    <#if ipAddressCount gt 0>
        <#if ipAddressCount gt 1>
            <a href="" onclick="return false;" data-rel="#reported-ip-list-${gid}" class="cluetip">${ipAddressCount} IP Addresses</a>
            <div id="reported-ip-list-${gid}" class="cluetip-data cluetip-data-openleft">
                <h4>Reported IP Addresses:</h4>
                <ul>
                    <#list ipList as ip>
                        <li>${ip}</li>
                    </#list>
                </ul>
            </div>
        <#else>
            ${ipList[0]}
        </#if>
    </#if>
</#macro>
