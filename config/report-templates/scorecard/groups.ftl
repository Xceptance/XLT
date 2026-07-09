<#import "util.ftl" as util>

<#macro groups results definitions>
    <div class="section" id="scorecard-groups">
        <@headline_groups/>

        <div class="content">
            <@description_groups/>

            <div class="data">
                <table>
                    <thead>
                        <tr>
                            <th>Group</th>
                            <th>Description</th>
                            <th>Enabled</th>
                            <th>Fails Test</th>
                            <th>Mode</th>
                            <th>Rule Count</th>
                            <th>Result</th>
                            <th>Message</th>
                            <th>Max Points</th>
                            <th>Points</th>
                        </tr>
                    </thead>
                    <#if results?has_content>
                        <tfoot>
                            <tr class="totals">
                                <td class="key">Totals</td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td class="value number">
                                    <#assign totalRuleCount = 0>
                                    <#list definitions as d>
                                        <#if d.rules.rule?has_content>
                                            <#assign totalRuleCount = totalRuleCount + d.rules.rule?size>
                                        </#if>
                                    </#list>
                                    ${totalRuleCount}
                                </td>
                                <td></td>
                                <td></td>
                                <td class="value number">
                                    <#assign maxPoints = 0>
                                    <#list results as group>
                                        <#assign maxPoints = maxPoints + (group.@totalPoints[0]!0)?number>
                                    </#list>
                                    ${maxPoints}
                                </td>
                                <td class="value number">
                                    <#assign points = 0>
                                    <#list results as group>
                                        <#assign points = points + (group.@points[0]!0)?number>
                                    </#list>
                                    ${points}
                                </td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <#list results as group>
                                <#assign groupId = group["@ref-id"]?string>
                                <#-- Find definition for this group -->
                                <#assign groupDef = "">
                                <#list definitions as d>
                                    <#if d.@id?string == groupId>
                                        <#assign groupDef = d>
                                        <#break>
                                    </#if>
                                </#list>

                                <tr>
                                    <td class="key">
                                        <#if groupDef?has_content>
                                            <@util.name_or_id node=groupDef/>
                                        <#else>
                                            ${groupId}
                                        </#if>
                                    </td>
                                    <td class="value text">
                                        <#if groupDef?has_content>${(groupDef.description[0]!groupDef.@description[0]!"")?trim}</#if>
                                    </td>
                                    <td class="value">
                                        <#if groupDef?has_content>${((groupDef.@enabled[0]!"") == "true")?string("true", "false")}</#if>
                                    </td>
                                    <td class="value">
                                        <#if groupDef?has_content>
                                            <#assign failsTest = ((groupDef.@failsTest[0]!"") == "true")>
                                            <#assign failsOn = (groupDef.@failsOn[0]!"")?string>
                                            <span<#if failsOn?has_content> title="Fails On: ${failsOn}"</#if>>${failsTest?string("true", "false")}</span>
                                        </#if>
                                    </td>
                                    <td class="value">
                                        <#if groupDef?has_content>${groupDef.@mode[0]!""}</#if>
                                    </td>
                                    <td class="value number">
                                        <#if groupDef?has_content && groupDef.rules.rule?has_content>
                                            ${groupDef.rules.rule?size}
                                        <#else>
                                            0
                                        </#if>
                                    </td>
                                    <td class="value">
                                        ${group.result[0]!""}
                                    </td>
                                    <td class="value text">
                                        <#list group.message as m>${m}<#if m_has_next>;</#if></#list>
                                    </td>
                                    <td class="value number">
                                        ${group.@totalPoints[0]!""}
                                    </td>
                                    <td class="value number">
                                        ${group.@points[0]!""}
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    <#else>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="10">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</#macro>

<#macro headline_groups>
    <h2>Groups</h2>
</#macro>

<#macro description_groups>
<div class="description">
    <p>Groups define which rules are evaluated and how the overall result of multiple related rules is determined. A rule may be used in more than one group. Each group requires at least one rule to be considered.</p>
</div>
</#macro>
