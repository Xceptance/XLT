<#import "util.ftl" as util>

<#macro rules results definitions configuration>
    <div class="section" id="scorecard-rules">
        <@headline_rules/>

        <div class="content">
            <@description_rules/>

            <div class="data">
                <table class="no-auto-stripe">
                    <thead>
                        <tr>
                            <th>Group</th>
                            <th>Rule</th>
                            <th>Description</th>
                            <th>Enabled</th>
                            <th>Fails Test</th>
                            <th>Negate Result</th>
                            <th>Check Count</th>
                            <th>Result</th>
                            <th>Message</th>
                            <th>Max Points</th>
                            <th>Points</th>
                        </tr>
                    </thead>
                    <#if results?has_content>
                        <tbody>
                            <#assign rowIndex = 0>
                            <#list results as groupRules>
                                <#assign group = groupRules?parent>
                                <#assign groupRefId = group["@ref-id"]?string>
                                <#-- Find group definition in configuration -->
                                <#assign groupDef = "">
                                <#list configuration.groups.group as g>
                                    <#if g.@id?string == groupRefId>
                                        <#assign groupDef = g>
                                        <#break>
                                    </#if>
                                </#list>

                                <#if groupRules.rule?has_content>
                                    <#assign numRules = groupRules.rule?size>
                                    <#assign groupMode = "">
                                    <#if groupDef?has_content>
                                        <#assign groupMode = (groupDef.@mode[0]!"")?string>
                                    </#if>
                                    
                                    <#-- Identify passed rules for mode logic -->
                                    <#assign passedRulesIds = []>
                                    <#list groupRules.rule as r>
                                        <#if (r.result[0]!"") == "PASSED">
                                            <#assign passedRulesIds = passedRulesIds + [r.@["ref-id"][0]?string]>
                                        </#if>
                                    </#list>
                                    <#assign passedRulesCount = passedRulesIds?size>
                                    
                                    <#assign stripeClass = (rowIndex % 2 == 0)?string("odd", "even")>

                                    <#list groupRules.rule as groupRule>
                                        <#assign ruleId = groupRule["@ref-id"]?string>
                                        <#assign ruleResult = groupRule.result>
                                        <#assign isFirstRuleOfGroup = (groupRule_index == 0)>
                                        
                                        <#-- Find rule definition -->
                                        <#assign ruleDef = "">
                                        <#list definitions as d>
                                            <#if d.@id?string == ruleId>
                                                <#assign ruleDef = d>
                                                <#break>
                                            </#if>
                                        </#list>
                                        
                                        <#assign numRuleChecks = 0>
                                        <#if ruleDef?has_content && ruleDef.checks.check?has_content>
                                            <#assign numRuleChecks = ruleDef.checks.check?size>
                                        </#if>

                                        <#-- Logic for 'inactive' class based on group mode -->
                                        <#assign activeClass = "">
                                        <#if ruleDef?has_content>
                                            <#assign isRuleEnabled = ((ruleDef.@enabled[0]!"") == "true")>
                                            <#assign isInactive = false>
                                            
                                            <#if !isRuleEnabled>
                                                <#assign isInactive = true>
                                            <#else>
                                                <#if groupMode == "firstPassed">
                                                    <#if passedRulesIds?has_content>
                                                        <#if passedRulesIds[0] != ruleId>
                                                            <#assign isInactive = true>
                                                        </#if>
                                                    <#else>
                                                        <#assign isInactive = true>
                                                    </#if>
                                                <#elseif groupMode == "lastPassed">
                                                    <#if passedRulesIds?has_content>
                                                        <#if passedRulesIds[passedRulesIds?size - 1] != ruleId>
                                                            <#assign isInactive = true>
                                                        </#if>
                                                    <#else>
                                                        <#assign isInactive = true>
                                                    </#if>
                                                <#elseif groupMode == "allPassed">
                                                    <#if passedRulesCount != numRules>
                                                        <#assign isInactive = true>
                                                    </#if>
                                                </#if>
                                            </#if>
                                            
                                            <#if isInactive>
                                                <#assign activeClass = "inactive">
                                            </#if>
                                        </#if>
                                        
                                        <#assign cellClass = activeClass>
                                        <#if activeClass?has_content>
                                            <#assign cellClass = " " + activeClass>
                                        </#if>

                                        <tr class="${stripeClass}">
                                            <#if isFirstRuleOfGroup>
                                                <@util.multi_row_cell numRows=numRules class="key">
                                                    <#if groupDef?has_content>
                                                        <@util.name_or_id node=groupDef/>
                                                    <#else>
                                                        ${groupRefId}
                                                    </#if>
                                                </@util.multi_row_cell>
                                            </#if>
                                            <td class="key${cellClass}">
                                                <#if ruleDef?has_content>
                                                    <@util.name_or_id node=ruleDef/>
                                                <#else>
                                                    ${ruleId}
                                                </#if>
                                            </td>
                                            <td class="value text${cellClass}">
                                                <#if ruleDef?has_content>${(ruleDef.description[0]!ruleDef.@description[0]!"")?trim}</#if>
                                            </td>
                                            <td class="value${cellClass}">
                                                <#if ruleDef?has_content>${((ruleDef.@enabled[0]!"") == "true")?string("true", "false")}</#if>
                                            </td>
                                            <td class="value${cellClass}">
                                                <#if ruleDef?has_content>
                                                    <@test_fail_trigger ruleDef=ruleDef/>
                                                </#if>
                                            </td>
                                            <td class="value${cellClass}">
                                                <#if ruleDef?has_content>${((ruleDef.@negateResult[0]!"") == "true")?string("true", "false")}</#if>
                                            </td>
                                            <td class="value number${cellClass}">
                                                ${numRuleChecks}
                                            </td>
                                            <td class="value${cellClass}">
                                                ${ruleResult[0]!""}
                                            </td>
                                            <td class="value text${cellClass}">
                                                ${groupRule.message[0]!""}
                                            </td>
                                            <td class="value number${cellClass}">
                                                <#if ruleDef?has_content>${ruleDef.@points[0]!""}</#if>
                                            </td>
                                            <td class="value number${cellClass}">
                                                ${groupRule.@points[0]!""}
                                            </td>
                                        </tr>
                                    </#list>
                                    <#assign rowIndex = rowIndex + 1>
                                </#if>
                            </#list>
                        </tbody>
                    <#else>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="11">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</#macro>

<#macro test_fail_trigger ruleDef>
    <#assign failsTest = ((ruleDef.@failsTest[0]!"") == "true")>
    <#assign trigger = (ruleDef.@failsOn[0]!"")?string>
    <#if failsTest && trigger?has_content>
        <span title="Fails On: ${trigger}">true</span>
    <#else>
        ${failsTest?string("true", "false")}
    </#if>
</#macro>

<#macro headline_rules>
    <h2>Rules</h2>
</#macro>

<#macro description_rules>
<div class="description">
    <#assign gid = "rules-d0e1">
    <p>Rules consist of data checks on the test result. A rule must be assigned to at least one group to be effective.
        <@util.show_n_hide gid=gid />
    </p>
    <div id="more-${gid}" class="more">
        <p>A rule passes if all its conditions evaluate to <code>true</code>, otherwise it fails.</p>
        <p>If a rule is marked as "Error", the evaluation statement was incorrect and the rule is ignored. A rule can mark the entire test as failed, regardless of the rating result, if the "fails test" attribute is set to <code>true</code>.</p>
    </div>
</div>
</#macro>
