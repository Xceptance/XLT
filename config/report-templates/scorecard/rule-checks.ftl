<#import "util.ftl" as util>

<#macro rule_checks definitions results configuration>
    <div class="section" id="scorecard-rulechecks">
        <@headline_rulechecks/>

        <div class="content">
            <@description_rulechecks/>

            <div class="data">
                <table class="no-auto-stripe">
                    <thead>
                        <tr>
                            <th>Rule</th>
                            <th>Check Number</th>
                            <th>Enabled</th>
                            <th>Display Value</th>
                            <th>Selector</th>
                            <th>Condition</th>
                            <th>Value</th>
                            <th>Result</th>
                        </tr>
                    </thead>
                    <#if results?has_content>
                        <tbody>
                            <#list results as groupRules>
                                <#if groupRules.rule?has_content>
                                    <#list groupRules.rule as groupRule>
                                        <#assign ruleId = groupRule["@ref-id"]?string>
                                        
                                        <#-- Find matching definition and its position for stripe class -->
                                        <#assign ruleDef = "">
                                        <#assign defIndex = 0>
                                        <#list definitions as d>
                                            <#if d.@id?string == ruleId>
                                                <#assign ruleDef = d>
                                                <#assign defIndex = d_index>
                                                <#break>
                                            </#if>
                                        </#list>
                                        
                                        <#if ruleDef?has_content && groupRule.checks.check?has_content>
                                            <#assign numChecks = groupRule.checks.check?size>
                                            <#assign stripeClass = (defIndex % 2 == 0)?string("odd", "even")>
                                            
                                            <#list groupRule.checks.check as check>
                                                <#-- Find check definition by index -->
                                                <#assign checkIndex = check.@index?string>
                                                <#assign checkDef = "">
                                                <#list ruleDef.checks.check as c>
                                                    <#if c.@index?string == checkIndex>
                                                        <#assign checkDef = c>
                                                        <#break>
                                                    </#if>
                                                </#list>
                                                
                                                <#if checkDef?has_content>
                                                    <#assign checkSelector = checkDef.selector>
                                                    <#-- Resolve selector ref-id to expression from configuration -->
                                                    <#assign checkSelectorResolved = checkSelector>
                                                    <#if checkSelector["@ref-id"]?? && checkSelector["@ref-id"]?has_content>
                                                        <#assign refId = checkSelector["@ref-id"]?string>
                                                        <#if configuration.selectors.selector?has_content>
                                                            <#list configuration.selectors.selector as s>
                                                                <#if s.@id?string == refId>
                                                                    <#assign checkSelectorResolved = s.expression>
                                                                    <#break>
                                                                </#if>
                                                            </#list>
                                                        </#if>
                                                    </#if>
                                                    
                                                    <tr class="${stripeClass}">
                                                        <#if check_index == 0>
                                                            <@util.multi_row_cell numRows=numChecks class="key">
                                                                <@util.name_or_id node=ruleDef/>
                                                            </@util.multi_row_cell>
                                                        </#if>
                                                        <td class="value number">
                                                            ${check.@index?number + 1}
                                                        </td>
                                                        <td class="value">
                                                            ${((checkDef.@enabled[0]!"") == "true")?string("true", "false")}
                                                        </td>
                                                        <td class="value">
                                                            ${((checkDef.@displayValue[0]!"") == "true")?string("true", "false")}
                                                        </td>
                                                        <td class="value text">
                                                            ${checkSelectorResolved[0]!""}
                                                        </td>
                                                        <td class="value text">
                                                            ${checkDef.condition[0]!""}
                                                        </td>
                                                        <td class="value">
                                                            ${check.value[0]!""}
                                                        </td>
                                                        <td class="value">
                                                            ${check.result[0]!""}
                                                        </td>
                                                    </tr>
                                                </#if>
                                            </#list>
                                        </#if>
                                    </#list>
                                </#if>
                            </#list>
                        </tbody>
                    <#else>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="8">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</#macro>

<#macro headline_rulechecks>
    <h2>Rule Checks</h2>
</#macro>

<#macro description_rulechecks>
<div class="description">
    <#assign gid = "rulechecks-d0e1">
    <p>Rule checks act as queries against the load test result data.
        <@util.show_n_hide gid=gid/>
    </p>
    <div id="more-${gid}" class="more">
        <p>Any data provided by XLT in its XML result format can be used to create rule checks. A check must be associated with a rule to be effective, and it can be used in more than one rule. A check consists of a selector against the XML document and a condition that it must satisfy. This table lists the details of the rule and its results within the group to which it is assigned.</p>
    </div>
</div>
</#macro>
