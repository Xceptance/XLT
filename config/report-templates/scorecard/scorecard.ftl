<#import "util.ftl" as util>

<#macro scorecard rootNode configuration>
    <div class="section" id="scorecard-result">
        <@headline_scorecard/>

        <div class="content">
            <@description_scorecard/>
            
            <#assign error = rootNode.error!>
            <#if error?has_content>
                <div class="error">
                   The test could not be evaluated for the following reason:
                   <pre>${error}</pre>
                </div>
            <#else>
                <div class="paragraph verdict">
                    <#-- Safe attribute extraction with defaults -->
                    <#assign points = (rootNode.@points[0])!"0">
                    <#assign totalPoints = (rootNode.@totalPoints[0])!"0">
                    <#assign pointsPercentage = (rootNode.@pointsPercentage[0])!"0">
                    <#assign ratingId = (rootNode.rating[0])!"">
                    <#assign testFailed = (rootNode.@testFailed[0]!"false") == "true">
                    
                    <#-- Find matching rating definition -->
                    <#assign ratingDefinition = "">
                    <#list configuration.ratings.rating as r>
                        <#if r.@id == ratingId>
                            <#assign ratingDefinition = r>
                            <#break>
                        </#if>
                    </#list>
                    
                    The test got ${points} out of ${totalPoints} points.
                    The rating result is ${pointsPercentage}%.

                    <#if ratingDefinition?is_hash>
                        <#-- Extract rating description safely -->
                        <#assign ratingDescription = "\"" + ((ratingDefinition.description[0]!(ratingDefinition.@description[0]!""))?trim) + "\"">
                        <#assign ratingFailsTest = (ratingDefinition.@failsTest[0]!"false") == "true">
                        
                        Based on the result, the test
                        <#if !testFailed>
                            succeeded with ${ratingDescription}.
                        <#elseif !ratingFailsTest>
                            <#-- Rating doesn't fail test, check for specific rules/groups -->
                            <#assign failedRules = []>
                            <#list configuration.rules.rule as r>
                                <#if (r.@failsTest[0]!"false") == "true" && (r.@enabled[0]!"true") == "true">
                                    <#-- Check against results -->
                                    <#assign ruleFailed = false>
                                    <#list rootNode.groups.group as g>
                                        <#list g.rules.rule as gr>
                                             <#if gr.@["ref-id"] == r.@id && (gr.@testFailed[0]!"false") == "true">
                                                 <#assign ruleFailed = true>
                                                 <#break>
                                             </#if>
                                        </#list>
                                        <#if ruleFailed><#break></#if>
                                    </#list>
                                    <#if ruleFailed>
                                        <#assign failedRules = failedRules + [r]>
                                    </#if>
                                </#if>
                            </#list>

                            <#assign failedGroups = []>
                            <#list configuration.groups.group as g>
                                <#if (g.@failsTest[0]!"false") == "true" && (g.@enabled[0]!"true") == "true">
                                    <#assign groupFailed = false>
                                    <#list rootNode.groups.group as resG>
                                        <#if resG.@["ref-id"] == g.@id && (resG.@testFailed[0]!"false") == "true">
                                            <#assign groupFailed = true>
                                            <#break>
                                        </#if>
                                    </#list>
                                    <#if groupFailed>
                                        <#assign failedGroups = failedGroups + [g]>
                                    </#if>
                                </#if>
                            </#list>
                            
                            <#if failedRules?has_content>
                                would have succeeded with ${ratingDescription} but at least one rule still qualifies this test as failed:
                                <@util.item_list items=failedRules/>.
                            <#elseif failedGroups?has_content>
                                would have succeeded with ${ratingDescription} but at least one group still qualifies this test as failed:
                                <@util.item_list items=failedGroups/>.
                            </#if>
                        <#else>
                            failed with ${ratingDescription}.
                        </#if>
                    <#else>
                        <#if testFailed>
                            The test failed due to a rule or group qualifying the test as failed.
                        </#if>
                    </#if>
                </div>
            </#if>
        </div>
    </div>
</#macro>

<#macro headline_scorecard>
    <h2>Scorecard Result</h2>
</#macro>

<#macro description_scorecard>
    <div class="description">
        <#assign gid = "scorecard-d0e1">
        <p>A performance test scorecard is a quick and easy way to evaluate a test result.
            <@util.show_n_hide gid=gid/>
        </p>
        <div id="more-${gid}" class="more">
            <p>Based on the total score and the maximum possible score, a percentage score is calculated that serves as the final verdict for the test.</p>
            <p>This summary page gives you the insight you need into the assessment and helps you to derive recommendations for next steps. It also helps you identifying areas for improvement.</p>
        </div>
    </div>
</#macro>
