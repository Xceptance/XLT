<#import "../common/util/format.ftl" as format>
<#import "descriptions.ftl" as descriptions>
<#import "../common/util/create-totals-td.ftl" as totals>

<#macro events>
    <div class="section" id="event-summary">
        <@descriptions.headline_event_summary />

        <div class="content">
            <@descriptions.description_event_summary />

            <div class="data">

                <div class="charts">
                    <div class="chart">
                        <img src="charts/Events.webp" alt="Events" />
                    </div>
                </div>

                <h3 id="event-overview">Overview</h3>
                <table class="table-autosort:1 table-autosort-order:desc">
                    <thead>
                        <tr>
                            <th class="table-sortable:alphanumeric" id="sortByEventOverview">Event</th>
                            <th class="table-sortable:numeric" id="sortByEventOverviewCount">Count</th>
                            <th class="table-sortable:numeric" id="sortByEventOverviewDropped"><span title="This is the count of dropped event messages, e.g., too many different messages per event.">Dropped</span></th>
                            <th class="table-sortable:numeric" id="sortByEventOverviewPercentage">Percentage</th>
                        </tr>
                    </thead>
                    <#assign eventList = report.testreport.events.event>
                    <#if eventList?has_content>
                        <#-- Calculate totals -->
                        <#local totalEventCount = 0>
                        <#local totalDroppedEventCount = 0>
                        <#list eventList as event>
                            <#local totalEventCount = totalEventCount + event.totalCount?number>
                            <#local totalDroppedEventCount = totalDroppedEventCount + event.droppedCount?number>
                        </#list>

                        <#-- Group by name -->
                        <#local distinctEvents = {}>
                        <#list eventList as event>
                            <#local name = event.name>
                            <#if distinctEvents[name]??>
                                <#local current = distinctEvents[name]>
                                <#local distinctEvents = distinctEvents + {name: {
                                    "totalCount": current.totalCount + event.totalCount?number,
                                    "droppedCount": current.droppedCount + event.droppedCount?number
                                }}>
                            <#else>
                                <#local distinctEvents = distinctEvents + {name: {
                                    "totalCount": event.totalCount?number,
                                    "droppedCount": event.droppedCount?number
                                }}>
                            </#if>
                        </#list>
                        <#local countDistinctEventNames = distinctEvents?keys?size>

                        <tfoot>
                            <tr class="totals">
                                <@totals.create_totals_td rows_in_table=countDistinctEventNames />

                                <td class="value number">
                                    ${format.formatTimerVal(totalEventCount, "#,##0")}
                                </td>
                                <td class="value number">
                                    ${format.formatTimerVal(totalDroppedEventCount, "#,##0")}
                                </td>
                                <td class="value number">
                                    ${format.formatPercentage(1)}
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list distinctEvents?keys?sort as name>
                                <#local data = distinctEvents[name]>
                                <tr>
                                    <td class="value text forcewordbreak">
                                        ${name}
                                    </td>
                                    <td class="value number count">
                                        ${format.formatTimerVal(data.totalCount, "#,##0")}
                                    </td>
                                    <td class="value number count">
                                        ${format.formatTimerVal(data.droppedCount, "#,##0")}
                                    </td>
                                    <td class="value number count">
                                        ${format.formatPercentage(data.totalCount / totalEventCount)}
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    <#else>
                        <tfoot>
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="4">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>

                <h3 id="event-details">Details</h3>
                <table>
                    <thead>
                        <tr>
                            <th rowspan="2">Test Case</th>
                            <th rowspan="2">Event</th>
                            <th rowspan="2">Total</th>
                            <th rowspan="2">Dropped</th>
                            <th colspan="2">Event Information</th>
                        </tr>
                        <tr>
                            <th>Count</th>
                            <th>Message</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <#if eventList?has_content>
                            <#-- Group and sort events by total count desc -->
                            <#local sortedEvents = []>
                            <#list eventList as e>
                                <#local sortedEvents = sortedEvents + [{
                                    "testCaseName": e.testCaseName,
                                    "name": e.name,
                                    "totalCount": e.totalCount?number,
                                    "droppedCount": e.droppedCount?number,
                                    "messages": e.messages.message,
                                    "sortKey": (1000000000 - e.totalCount?number)?string?left_pad(10, '0') + "_" + e?index?string?left_pad(10, '0')
                                }]>
                            </#list>

                            <#list sortedEvents?sort_by("sortKey") as event>
                                <#assign messages = event.messages>
                                <#assign messageCount = messages?size>
                                
                                <#-- Sort messages by count desc, then by original order (stable sort) -->
                                <#local sortedMessages = []>
                                <#list messages as m>
                                    <#local sortedMessages = sortedMessages + [{
                                        "count": m.count?number,
                                        "info": m.info,
                                        "sortKey": (1000000000 - m.count?number)?string?left_pad(10, '0') + "_" + m?index?string?left_pad(10, '0')
                                    }]>
                                </#list>

                                <#list sortedMessages?sort_by("sortKey") as message>
                                    <tr>
                                        <#if message?index == 0>
                                            <td class="value text" rowspan="${messageCount}">
                                                ${event.testCaseName}
                                            </td>
                                            <td class="value text" rowspan="${messageCount}">
                                                ${event.name}
                                            </td>
                                            <td class="value number" rowspan="${messageCount}">
                                                ${format.formatTimerVal(event.totalCount, "#,##0")}
                                            </td>
                                            <td class="value number" rowspan="${messageCount}">
                                                ${format.formatTimerVal(event.droppedCount, "#,##0")}
                                            </td>
                                        </#if>
                                        <td class="value number">
                                            ${format.formatTimerVal(message.count, "#,##0")}
                                        </td>
                                        <td class="value text forcewordbreak">
                                            ${message.info}
                                        </td>
                                    </tr>
                                </#list>
                            </#list>
                        <#else>
                            <tr>
                                <td class="no-data" colspan="6">No data available</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
</#macro>
