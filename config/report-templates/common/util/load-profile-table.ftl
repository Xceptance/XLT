<#import "/common/util/format.ftl" as fmt>
<#import "/common/util/create-totals-td.ftl" as cttd>

<#macro load_profile_table rootNode>
    <#assign testCases = rootNode.testCase![]>
    <#assign count = testCases?size>

    <table class="">
        <thead>
            <tr>
                <th rowspan="2">Transaction Name</th>
                <th colspan="2">Users</th>
                <th rowspan="2">Iterations</th>
                <th colspan="2">Arrival Rate</th>
                <th rowspan="2">Initial Delay</th>
                <th rowspan="2">Ramp-Up</th>
                <th rowspan="2">Warm-Up</th>
                <th rowspan="2">Measurement</th>
                <th rowspan="2">Shutdown</th>
                <th rowspan="2" title="Thinking time between actions">Think Time [ms]</th>
            </tr>
            <tr>
                <th>Total</th>
                <th>Share</th>
                <th>1/h</th>
                <th>Share</th>
            </tr>
        </thead>
        <#if count gt 0>
            <tfoot>
                <tr class="totals">
                    <#-- Transaction Name column -->
                    <@cttd.create_totals_td rows_in_table=count />

                    <#-- Users Total -->
                    <#assign minUsers = 0, maxUsers = 0>
                    <#list testCases as tc>
                        <#assign range = parseRange(tc.numberOfUsers)>
                        <#assign minUsers += range.min, maxUsers += range.max>
                    </#list>
                    <td class="value number">${(maxUsers gt 0)?then(fmt.formatRange(minUsers, maxUsers), "&ndash;"?no_esc)}</td>
                    <td class="value number">100.00%</td>

                    <#-- Iterations -->
                    <#assign minIterations = 0, maxIterations = 0>
                    <#list testCases as tc>
                        <#assign range = parseRange(tc.numberOfIterations)>
                        <#assign minIterations += range.min, maxIterations += range.max>
                    </#list>
                    <td class="value number">${(maxIterations gt 0)?then(fmt.formatRange(minIterations, maxIterations), "&ndash;"?no_esc)}</td>

                    <#-- Arrival Rate -->
                    <#assign minArrival = 0, maxArrival = 0>
                    <#list testCases as tc>
                        <#assign range = parseRange(tc.arrivalRate!"0")>
                        <#assign minArrival += range.min, maxArrival += range.max>
                    </#list>
                    <td class="value number">${(maxArrival gt 0)?then(fmt.formatRange(minArrival, maxArrival), "&ndash;"?no_esc)}</td>
                    <td class="value number">100.00%</td>

                    <#-- Initial Delay -->
                    <#assign minID = -1, maxID = -1>
                    <#list testCases as tc>
                        <#assign val = toNumber(tc.initialDelay)>
                        <#if minID == -1 || val lt minID><#assign minID = val></#if>
                        <#if val gt maxID><#assign maxID = val></#if>
                    </#list>
                    <td class="value number">${(minID lt 0 || maxID lte 0)?then("&ndash;"?no_esc, fmt.formatRange(minID, maxID, true))}</td>

                    <#-- Ramp-Up -->
                    <#assign minRU = -1, maxRU = -1>
                    <#list testCases as tc>
                        <#assign val = toNumber(tc.rampUpPeriod)>
                        <#if minRU == -1 || val lt minRU><#assign minRU = val></#if>
                        <#if val gt maxRU><#assign maxRU = val></#if>
                    </#list>
                    <td class="value number">${(minRU lt 0 || maxRU lte 0)?then("&ndash;"?no_esc, fmt.formatRange(minRU, maxRU, true))}</td>

                    <#-- Warm-Up -->
                    <#assign minWU = -1, maxWU = -1>
                    <#list testCases as tc>
                        <#assign val = toNumber(tc.warmUpPeriod)>
                        <#if minWU == -1 || val lt minWU><#assign minWU = val></#if>
                        <#if val gt maxWU><#assign maxWU = val></#if>
                    </#list>
                    <td class="value number">${(minWU lt 0 || maxWU lte 0)?then("&ndash;"?no_esc, fmt.formatRange(minWU, maxWU, true))}</td>

                    <#-- Measurement -->
                    <#assign minM = -1, maxM = -1>
                    <#list testCases as tc>
                        <#assign val = toNumber(tc.measurementPeriod)>
                        <#if minM == -1 || val lt minM><#assign minM = val></#if>
                        <#if val gt maxM><#assign maxM = val></#if>
                    </#list>
                    <td class="value number">${(minM lt 0 || maxM lte 0)?then("&ndash;"?no_esc, fmt.formatRange(minM, maxM, true))}</td>

                    <#-- Shutdown -->
                    <#assign minS = -1, maxS = -1>
                    <#list testCases as tc>
                        <#assign val = toNumber(tc.shutdownPeriod)>
                        <#if minS == -1 || val lt minS><#assign minS = val></#if>
                        <#if val gt maxS><#assign maxS = val></#if>
                    </#list>
                    <td class="value number">${(minS lt 0 || maxS lte 0)?then("&ndash;"?no_esc, fmt.formatRange(minS, maxS, true))}</td>

                    <#-- Think Time -->
                    <#assign minTT = -1, maxTT = -1>
                    <#list testCases as tc>
                        <#assign tt = toNumber(tc.actionThinkTime), dev = toNumber(tc.actionThinkTimeDeviation)>
                        <#assign min = tt - dev, max = tt + dev>
                        <#if min lt 0><#assign min = 0></#if>
                        <#if minTT == -1 || min lt minTT><#assign minTT = min></#if>
                        <#if max gt maxTT><#assign maxTT = max></#if>
                    </#list>
                    <td class="value number">${fmt.formatRange(minTT, maxTT)}</td>
                </tr>
            </tfoot>
            <tbody>
                <#list testCases?sort_by("userName") as tc>
                    <#assign numIterations = toNumber(tc.numberOfIterations)>
                    <#assign mode = (numIterations == 0)?then("duration", "iteration")>
                    <tr>
                        <td class="key text" title="${ensureString(tc.testCaseClassName)}">${tc.userName}</td>
                        <#assign complexLF = ensureString(tc.complexLoadFunction)>
                        <td class="value number"<#if (tc.arrivalRate?size == 0) && complexLF?has_content> title="${complexLF}"</#if>>${tc.numberOfUsers}</td>
                        <#local loadP = ensureString(tc.numberOfUsersPercentage)?trim>
                        <td class="value number load-meter" style="--loadp:${loadP}">${fmt.formatPercentageValue(tc.numberOfUsersPercentage)}</td>
                        <td class="value number">
                            <#if mode == "iteration">
                                ${fmt.formatNumber(tc.numberOfIterations)}
                            <#else>
                                &ndash;
                            </#if>
                        </td>
                        <td class="value number"<#if mode == "duration" && tc.arrivalRate?has_content && tc.complexLoadFunction?has_content> title="${ensureString(tc.complexLoadFunction)}"</#if>>
                            <#if mode == "duration" && tc.arrivalRate?has_content>
                                ${tc.arrivalRate}
                            <#else>
                                &ndash;
                            </#if>
                        </td>
                        <#local arrivalP = (tc.arrivalRatePercentage?size gt 0)?then(tc.arrivalRatePercentage[0]?string?trim, "")>
                        <#if tc.arrivalRate?has_content>
                            <td class="value number load-meter" style="--loadp:${arrivalP}">${fmt.formatPercentageValue(arrivalP)}</td>
                        <#else>
                            <td class="value number">&ndash;</td>
                        </#if>
                        <td class="value number">
                            <#if toNumber(tc.initialDelay) gt 0>
                                ${fmt.formatMsecToH(toNumber(tc.initialDelay) * 1000)}
                            <#else>
                                ${fmt.formatNumber("")}
                            </#if>
                        </td>
                        <td class="value number">
                            <#if toNumber(tc.rampUpPeriod) gt 0>
                                ${fmt.formatMsecToH(toNumber(tc.rampUpPeriod) * 1000)}
                            <#else>
                                ${fmt.formatNumber("")}
                            </#if>
                        </td>
                        <td class="value number">
                            <#if toNumber(tc.warmUpPeriod) gt 0>
                                ${fmt.formatMsecToH(toNumber(tc.warmUpPeriod) * 1000)}
                            <#else>
                                ${fmt.formatNumber("")}
                            </#if>
                        </td>
                        <td class="value number">
                            <#if (tc.measurementPeriod?size > 0)>
                                ${fmt.formatMsecToH(toNumber(tc.measurementPeriod) * 1000)}
                            <#else>
                                ${fmt.formatNumber("")}
                            </#if>
                        </td>
                        <td class="value number">
                            <#if toNumber(tc.shutdownPeriod) gt 0>
                                ${fmt.formatMsecToH(toNumber(tc.shutdownPeriod) * 1000)}
                            <#else>
                                ${fmt.formatNumber("")}
                            </#if>
                        </td>
                        <td class="value number">
                            <#assign tt = toNumber(tc.actionThinkTime)>
                            <#assign dev = toNumber(tc.actionThinkTimeDeviation)>
                            ${fmt.formatRange(tt - dev, tt + dev)}
                        </td>
                    </tr>
                </#list>
            </tbody>
        <#else>
            <tfoot><tr><td colspan="12"></td></tr></tfoot>
            <tbody><tr><td class="no-data" colspan="12">No data available</td></tr></tbody>
        </#if>
    </table>
</#macro>

<#function parseRange val>
    <#local sVal = ensureString(val)?trim>
    <#if sVal?contains("...")>
        <#return {"min": sVal?keep_before("...")?replace(",", "")?number, "max": sVal?keep_after("...")?replace(",", "")?number}>
    <#else>
        <#local n = (sVal?replace(",", ""))!0>
        <#if n == ""><#local n = 0></#if>
        <#return {"min": n?number, "max": n?number}>
    </#if>
</#function>

<#function ensureString val>
    <#if val?is_string><#return val></#if>
    <#if val?is_number><#return val?c></#if>
    <#if val?is_sequence>
        <#if val?size == 0><#return ""></#if>
        <#return val[0]?string>
    </#if>
    <#return val?string>
</#function>

<#function toNumber node>
    <#local s = ensureString(node)?trim>
    <#if s?has_content>
        <#return s?number>
    <#else>
        <#return 0>
    </#if>
</#function>
