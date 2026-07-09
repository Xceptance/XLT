<#import "/sections/descriptions.ftl" as desc>
<#import "/util/summary-timer-row.ftl" as str>

<#macro summary>
    <div class="section" id="summary">
        <@desc.headline_summary />

        <div class="content">
            <@desc.description_summary />

            <div class="data">
                <#assign percentiles = report.testreport.testReportConfig.runtimePercentiles.string![]>
                <#assign percentileCount = percentiles?size>

                <table class="">
                    <thead>
                        <tr>
                            <th rowspan="2">Summary</th>
                            <th colspan="4">Count</th>
                            <th colspan="2">Errors</th>
                            <th>Events</th>
                            <th colspan="4">Runtime [ms]</th>
                            <#if percentileCount gt 0>
                                <th colspan="${percentileCount}">Runtime Percentiles [ms]</th>
                            </#if>
                        </tr>
                        <tr>
                            <th>Total</th>
                            <th>1/s</th>
                            <th>1/h*</th>
                            <th>1/d*</th>
                            <th>Total</th>
                            <th>%</th>
                            <th>Total</th>
                            <th title="The arithmetic mean of the data series.">Mean</th>
                            <th title="The smallest value of the data series.">Min.</th>
                            <th title="The largest value of the data series.">Max.</th>
                            <th title="The standard deviation of all values in the data series.">Dev.</th>
                            <#list percentiles as p>
                                <th class="table-sortable:numeric" title="The nth percentile of the data series.">P${p}</th>
                            </#list>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>
                            <#list percentiles as p><td></td></#list>
                        </tr>
                    </tfoot>
                    <tbody>
                        <@str.summary_timer_row name="Transactions" link="transactions" element=report.testreport.summary.transactions />
                        <@str.summary_timer_row name="Actions" link="actions" element=report.testreport.summary.actions />
                        <@str.summary_timer_row name="Requests" link="requests" element=report.testreport.summary.requests />
                        <@str.summary_timer_row name="Custom Timers" link="custom-timers" element=report.testreport.summary.customTimers />
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</#macro>
