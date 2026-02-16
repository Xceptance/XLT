<#import "/util/timer-chart.ftl" as tc>
<#import "/util/timer-table.ftl" as tt>
<#import "/common/util/create-totals-td.ftl" as totals>
<#import "/common/util/filtered-footer-row.ftl" as ffr>
<#import "/util/timer-labels.ftl" as tl>

<#function formatTimerVal node>
    <#local val = "">
    <#if node?is_sequence>
        <#if node?size gt 0>
            <#local val = node[0]?string?trim>
        </#if>
    <#else>
        <#local val = node?string?trim>
    </#if>
    
    <#if val?has_content && val != "NaN">
        <#return val?number?string[",##0"]>
    <#else>
        <#return "nan">
    </#if>
</#function>

<#macro timer_section elements summaryElement tableRowHeader directory type>
        <div class="charts">
            <#list summaryElement as se>
                <@tc.timer_chart element=se directory=directory type=type gid="summary" />
            </#list>
        </div>

        <#if type == "request">
            <#-- Request type has tabbed view: Overview, Bandwidth, Network Timing -->
            <div id="tabletabbies" class="c-tabs">
                <ul class="c-tabs-nav">
                    <li class="c-tabs-nav-link c-is-active">
                        <a href="#Overview">Overview</a>
                    </li>
                    <li class="c-tabs-nav-link">
                        <a href="#Bandwidth">Bandwidth</a>
                    </li>
                    <li class="c-tabs-nav-link">
                        <a href="#NetworkTiming">Network Timing</a>
                    </li>
                </ul>

                <div id="Overview" class="c-tab c-is-active">
                    <h4 class="print">Overview</h4>
                    <@tt.timer_table elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader type=type />
                </div>

                <div id="Bandwidth" class="c-tab">
                    <h4 class="print">Bandwidth</h4>
                    <table class="c-tab-content table-autosort:0">
                        <thead>
                            <tr>
                                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByBandwidthName">
                                    ${tableRowHeader}
                                    <br/>
                                    <input class="filter" placeholder="Enter filter substrings" title=""/>
                                    <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                </th>
                                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByLabels">Labels</th>
                                <th colspan="8">Bytes Sent</th>
                                <th colspan="8" class="colgroup1">Bytes Received</th>
                            </tr>
                            <tr>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentTotal">Total</th>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentPerSecond">1/s</th>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentPerMinute">1/min</th>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentPerHour">1/h*</th>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentPerDay">1/d*</th>
                                <th class="table-sortable:numeric" title="The arithmetic mean." id="sortByBandwidthSentMean">Mean</th>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentMin">Min.</th>
                                <th class="table-sortable:numeric" id="sortByBandwidthSentMax">Max.</th>

                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedTotal">Total</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedPerSecond">1/s</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedPerMinute">1/min</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedPerHour">1/h*</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedPerDay">1/d*</th>
                                <th class="table-sortable:numeric colgroup1" title="The arithmetic mean." id="sortByBandwidthReceivedMean">Mean</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedMin">Min.</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByBandwidthReceivedMax">Max.</th>
                            </tr>
                        </thead>
                        <#assign count = elements?size>
                        <#if count gt 0>
                            <tfoot>
                                <#list summaryElement as se>
                                    <tr class="totals">
                                        <@totals.create_totals_td rows_in_table=count class="key colgroup1" />

                                        <td class="colgroup1"></td>

                                        <td class="value number">${formatTimerVal(se.bytesSent.totalCount)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.countPerSecond)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.countPerMinute)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.countPerHour)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.countPerDay)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.mean)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.min)}</td>
                                        <td class="value number">${formatTimerVal(se.bytesSent.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.totalCount)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.countPerSecond)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.countPerMinute)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.countPerHour)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.countPerDay)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.bytesReceived.max)}</td>
                                    </tr>
                                </#list>
                                <@ffr.filtered_footer_row />
                            </tfoot>
                            <tbody>
                                <#list elements as el>
                                    <#assign gid = "id" + el?counter>
                                    <tr>
                                        <td class="key colgroup1 forcewordbreak">
                                            <a href="#chart-${gid}" data-id="tableEntry-${gid}" <#if el.urls?children?size gt 0>data-rel="#url-listing-${gid}" class="cluetip"</#if>>${el.name}</a>
                                        </td>

                                        <td class="colgroup1" data-cell-value="${(el.labels[0]!"")?trim}">
                                            <@tl.timer_labels labelString=(el.labels[0]!"") />
                                        </td>

                                        <td class="value number">${formatTimerVal(el.bytesSent.totalCount)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.countPerSecond)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.countPerMinute)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.countPerHour)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.countPerDay)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.mean)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.min)}</td>
                                        <td class="value number">${formatTimerVal(el.bytesSent.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.totalCount)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.countPerSecond)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.countPerMinute)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.countPerHour)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.countPerDay)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.bytesReceived.max)}</td>
                                    </tr>
                                </#list>
                            </tbody>
                        <#else>
                             <tfoot>
                                <tr>
                                    <td class="colgroup1"></td>
                                    <td class="colgroup1"></td>

                                    <td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>

                                    <td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td>
                                </tr>
                                <@ffr.filtered_footer_row />
                            </tfoot>
                            <tbody class="table-nosort">
                                <tr>
                                    <td colspan="18" class="no-data">No data available</td>
                                </tr>
                            </tbody>
                        </#if>
                    </table>
                </div>

                <div id="NetworkTiming" class="c-tab">
                    <h4 class="print">Network Timing</h4>
                    <table class="c-tab-content table-autosort:0">
                        <thead>
                            <tr>
                                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByNetworkName">
                                    ${tableRowHeader}
                                    <br/>
                                    <input class="filter" placeholder="Enter filter substrings" title=""/>
                                    <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                </th>
                                <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByLabels">Labels</th>
                                <th colspan="3">DNS Time [ms]</th>
                                <th colspan="3" class="colgroup1">Connect Time [ms]</th>
                                <th colspan="3">Send Time [ms]</th>
                                <th colspan="3" class="colgroup1">Server Busy Time [ms]</th>
                                <th colspan="3">Receive Time [ms]</th>
                                <th colspan="3" class="colgroup1" title="Time To First Bytes">Time to First [ms]</th>
                                <th colspan="3" title="Time To Last Bytes">Time to Last [ms]</th>
                            </tr>
                            <tr>
                                <th class="table-sortable:numeric" title="The arithmetic mean." id="sortByDNSMean">Mean</th>
                                <th class="table-sortable:numeric" id="sortByDNSMin">Min.</th>
                                <th class="table-sortable:numeric" id="sortByDNSMax">Max.</th>

                                <th class="table-sortable:numeric colgroup1" title="The arithmetic mean." id="sortByConnectTimeMean">Mean</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByConnectTimeMin">Min.</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByConnectTimeMax">Max.</th>

                                <th class="table-sortable:numeric" title="The arithmetic mean." id="sortBySendTimeMean">Mean</th>
                                <th class="table-sortable:numeric" id="sortBySendTimeMin">Min.</th>
                                <th class="table-sortable:numeric" id="sortBySendTimeMax">Max.</th>

                                <th class="table-sortable:numeric colgroup1" title="The arithmetic mean." id="sortByServerBusyTimeMean">Mean</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByServerBusyTimeMin">Min.</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByServerBusyTimeMax">Max.</th>

                                <th class="table-sortable:numeric" title="The arithmetic mean." id="sortByReceiveTimeMean">Mean</th>
                                <th class="table-sortable:numeric" id="sortByReceiveTimeMin">Min.</th>
                                <th class="table-sortable:numeric" id="sortByReceiveTimeMax">Max.</th>

                                <th class="table-sortable:numeric colgroup1" title="The arithmetic mean." id="sortByTTFMean">Mean</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByTTFMin">Min.</th>
                                <th class="table-sortable:numeric colgroup1" id="sortByTTFMax">Max.</th>

                                <th class="table-sortable:numeric" title="The arithmetic mean." id="sortByTTLMean">Mean</th>
                                <th class="table-sortable:numeric" id="sortByTTLMin">Min.</th>
                                <th class="table-sortable:numeric" id="sortByTTLMax">Max.</th>
                            </tr>
                        </thead>
                        <#if count gt 0>
                            <tfoot>
                                <#list summaryElement as se>
                                    <tr class="totals">
                                        <@totals.create_totals_td rows_in_table=count class="key colgroup1" />

                                        <td class="colgroup1"></td>

                                        <td class="value number">${formatTimerVal(se.dnsTime.mean)}</td>
                                        <td class="value number">${formatTimerVal(se.dnsTime.min)}</td>
                                        <td class="value number">${formatTimerVal(se.dnsTime.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(se.connectTime.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.connectTime.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.connectTime.max)}</td>

                                        <td class="value number">${formatTimerVal(se.sendTime.mean)}</td>
                                        <td class="value number">${formatTimerVal(se.sendTime.min)}</td>
                                        <td class="value number">${formatTimerVal(se.sendTime.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(se.serverBusyTime.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.serverBusyTime.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.serverBusyTime.max)}</td>

                                        <td class="value number">${formatTimerVal(se.receiveTime.mean)}</td>
                                        <td class="value number">${formatTimerVal(se.receiveTime.min)}</td>
                                        <td class="value number">${formatTimerVal(se.receiveTime.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(se.timeToFirstBytes.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.timeToFirstBytes.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(se.timeToFirstBytes.max)}</td>

                                        <td class="value number">${formatTimerVal(se.timeToLastBytes.mean)}</td>
                                        <td class="value number">${formatTimerVal(se.timeToLastBytes.min)}</td>
                                        <td class="value number">${formatTimerVal(se.timeToLastBytes.max)}</td>
                                    </tr>
                                </#list>
                                <@ffr.filtered_footer_row />
                            </tfoot>
                            <tbody>
                                <#list elements as el>
                                    <#assign gid = "id" + el?counter>
                                    <tr>
                                        <td class="key colgroup1 forcewordbreak">
                                            <a href="#chart-${gid}" data-id="tableEntry-${gid}" <#if el.urls?children?size gt 0>data-rel="#url-listing-${gid}" class="cluetip"</#if>>${el.name}</a>
                                        </td>

                                        <td class="colgroup1" data-cell-value="${(el.labels[0]!"")?trim}">
                                            <@tl.timer_labels labelString=(el.labels[0]!"") />
                                        </td>

                                        <td class="value number">${formatTimerVal(el.dnsTime.mean)}</td>
                                        <td class="value number">${formatTimerVal(el.dnsTime.min)}</td>
                                        <td class="value number">${formatTimerVal(el.dnsTime.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(el.connectTime.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.connectTime.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.connectTime.max)}</td>

                                        <td class="value number">${formatTimerVal(el.sendTime.mean)}</td>
                                        <td class="value number">${formatTimerVal(el.sendTime.min)}</td>
                                        <td class="value number">${formatTimerVal(el.sendTime.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(el.serverBusyTime.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.serverBusyTime.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.serverBusyTime.max)}</td>

                                        <td class="value number">${formatTimerVal(el.receiveTime.mean)}</td>
                                        <td class="value number">${formatTimerVal(el.receiveTime.min)}</td>
                                        <td class="value number">${formatTimerVal(el.receiveTime.max)}</td>

                                        <td class="value number colgroup1">${formatTimerVal(el.timeToFirstBytes.mean)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.timeToFirstBytes.min)}</td>
                                        <td class="value number colgroup1">${formatTimerVal(el.timeToFirstBytes.max)}</td>

                                        <td class="value number">${formatTimerVal(el.timeToLastBytes.mean)}</td>
                                        <td class="value number">${formatTimerVal(el.timeToLastBytes.min)}</td>
                                        <td class="value number">${formatTimerVal(el.timeToLastBytes.max)}</td>
                                    </tr>
                                </#list>
                            </tbody>
                        <#else>
                            <tfoot>
                                <tr>
                                    <td class="colgroup1"></td>
                                    <td class="colgroup1"></td>
                                    <td></td><td></td><td></td>
                                    <td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td>
                                    <td></td><td></td><td></td>
                                    <td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td>
                                    <td></td><td></td><td></td>
                                    <td class="colgroup1"></td><td class="colgroup1"></td><td class="colgroup1"></td>
                                    <td></td><td></td><td></td>
                                </tr>
                                <@ffr.filtered_footer_row />
                            </tfoot>
                            <tbody class="table-nosort">
                                <tr>
                                    <td colspan="23" class="no-data">No data available</td>
                                </tr>
                            </tbody>
                        </#if>
                    </table>
                </div>
            </div>

        <#else>
            <#-- For transaction, action, custom-timer: just the table -->
            <div class="data">
                <@tt.timer_table elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader type=type />
            </div>
        </#if>

        <#if elements?size gt 0>
            <div>
                <h3 class="no-print">
                    <#if type == "transaction">Individual Transactions</#if>
                    <#if type == "action">Individual Actions</#if>
                    <#if type == "request">Individual Requests</#if>
                    <#if type == "pageLoadTiming">Individual Page Load Timings</#if>
                    <#if type == "custom">Individual Custom Timers</#if>
                </h3>
                <div class="charts">
                    <#list elements as el>
                        <@tc.timer_chart element=el directory=directory type=type gid="id${el?counter}" />
                    </#list>
                </div>
            </div>
        </#if>
</#macro>
