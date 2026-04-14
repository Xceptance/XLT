<#import "timer-table.ftl" as timerTable>
<#import "timer-cell.ftl" as timerCell>
<#import "../../common/util/create-totals-td.ftl" as createTotalsTd>
<#import "../../common/util/filtered-footer-row.ftl" as filteredFooterRow>

<#macro render elements summaryElement tableRowHeader type>
    <#if type == "request">
        <div id="tabletabbies" class="c-tabs">
            <ul class="c-tabs-nav">
                <li class="c-tabs-nav-link c-is-active">
                    <a href="#Overview">Overview</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Bandwidth">Bandwidth</a>
                </li>
            </ul>

            <div id="Overview" class="c-tab c-is-active">
                <h4 class="print">Overview</h4>
                <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader type=type />
            </div>
                
            <div id="Bandwidth" class="c-tab">
                <h4 class="print">Bandwidth</h4>
                <table class="c-tab-content table-autosort:0">
                    <thead>
                        <tr>
                            <th rowspan="2" class="table-sortable:alphanumeric colgroup1">
                                ${tableRowHeader}
                                <br/>
                                <form>
                                    <input class="filter" placeholder="Enter filter substrings" title="" data-filter-id="filterByName" data-col-index="0"/>
                                    <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                                </form>
                            </th>
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
                    <#local count = elements?size>
                    <#if (count > 0)>
                        <tfoot>
                            <#if summaryElement?has_content>
                                <tr class="totals">
                                    <@createTotalsTd.create_totals_td rows_in_table=count class="key colgroup1" />

                                    <@timerCell.render node=summaryElement.bytesSent.totalCount format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesSent.countPerSecond format="#,##0" />                                                
                                    <@timerCell.render node=summaryElement.bytesSent.countPerMinute format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesSent.countPerHour format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesSent.countPerDay format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesSent.mean format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesSent.min format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesSent.max format="#,##0" />

                                    <@timerCell.render node=summaryElement.bytesReceived.totalCount format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.countPerSecond format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.countPerMinute format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.countPerHour format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.countPerDay format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.mean format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.min format="#,##0" />
                                    <@timerCell.render node=summaryElement.bytesReceived.max format="#,##0" />
                                </tr>
                            </#if>
                            <@filteredFooterRow.filtered_footer_row />
                        </tfoot>
                        <tbody>
                            <#list elements as timerElement>
                                <tr>
                                    <td class="key colgroup1 forcewordbreak">
                                        ${timerElement.name}
                                    </td>

                                    <@timerCell.render node=timerElement.bytesSent.totalCount format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.countPerSecond format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.countPerMinute format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.countPerHour format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.countPerDay format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.mean format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.min format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesSent.max format="#,##0" />

                                    <@timerCell.render node=timerElement.bytesReceived.totalCount format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.countPerSecond format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.countPerMinute format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.countPerHour format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.countPerDay format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.mean format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.min format="#,##0" />
                                    <@timerCell.render node=timerElement.bytesReceived.max format="#,##0" />
                                </tr>
                            </#list>
                        </tbody>
                    <#else>
                        <tfoot>
                            <tr>
                                <td class="colgroup1"></td>
                                <td colspan="8"></td>
                                <td colspan="8" class="colgroup1"></td>
                            </tr>
                            <@filteredFooterRow.filtered_footer_row />
                        </tfoot>
                        <tbody class="table-nosort">
                            <tr>
                                <td colspan="17" class="no-data">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>
            </div>  
        </div> 
    <#else>
        <div class="data">
            <@timerTable.render elements=elements summaryElement=summaryElement tableRowHeader=tableRowHeader type=type />
        </div>
    </#if>
</#macro>
