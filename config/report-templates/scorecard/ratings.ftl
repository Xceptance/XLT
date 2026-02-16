<#import "util.ftl" as util>

<#macro ratings elements active>
    <div class="section" id="scorecard-ratings">
        <@headline_ratings/>

        <div class="content">
            <@description_ratings/>

            <div class="data">
                <table>
                    <thead>
                        <tr>
                            <th>Rating</th>
                            <th>Description</th>
                            <th>Enabled</th>
                            <th>Fails Test</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                    <#if elements?has_content>
                        <tbody>
                            <#list elements as rating>
                                <#assign ratingId = rating["@id"]?string>
                                <#assign isEnabled = (rating.@enabled[0]!"") == "true">
                                <#assign isActive = (ratingId == active?string && isEnabled)>
                                <tr<#if !isActive> class="inactive"</#if>>
                                    <td class="key">
                                        <@util.name_or_id node=rating/>
                                    </td>
                                    <td class="value text">
                                        ${(rating.description[0]!rating.@description[0]!"")?trim}
                                    </td>
                                    <td class="value">
                                        ${rating.@enabled[0]!""}
                                    </td>
                                    <td class="value">
                                        ${rating.@failsTest[0]!""}
                                    </td>
                                    <td class="value number">
                                        ${rating.@value[0]!""}
                                    </td>
                                </tr>
                            </#list>
                        </tbody>
                    <#else>
                        <tbody>
                            <tr>
                                <td class="no-data" colspan="5">No data available</td>
                            </tr>
                        </tbody>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</#macro>

<#macro headline_ratings>
    <h2>Rating</h2>
</#macro>

<#macro description_ratings>
<div class="description">
    <#assign gid = "ratings-d0e1">
    <p>A rating defines the result of the scoring. It is basically a quick verdict on the created scorecard.
        <@util.show_n_hide gid=gid/>
    </p>
    <div id="more-${gid}" class="more">
        <p>A score defines a verbal test result and can also mark the test as failed if necessary. The score uses the percentage value of the points achieved versus the maximum points achievable to calculate the score. The rating value defines the upper limit for the application of this score.</p>
    </div>
</div>
</#macro>
