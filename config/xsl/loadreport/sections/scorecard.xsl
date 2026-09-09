<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="scorecard-summary">
        <xsl:param name="scorecardXmlUrl" select="''" />
        <xsl:variable name="docUrl">
            <xsl:choose>
                <xsl:when test="string-length($scorecardXmlUrl) &gt; 0">
                    <xsl:value-of select="$scorecardXmlUrl" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>scorecard.xml</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="scorecardDoc" select="document($docUrl, /)" />
        <xsl:if test="$scorecardDoc/scorecard/outcome">
            <xsl:variable name="scorecard" select="$scorecardDoc/scorecard" />
            <xsl:variable name="outcome" select="$scorecard/outcome" />
            <xsl:variable name="config" select="$scorecard/configuration" />
            <xsl:variable name="ratingId" select="$outcome/rating" />
            <xsl:variable name="ratingDef" select="$config/ratings/rating[@id = $ratingId]" />

            <div class="page-break"></div>
            <div class="section" id="scorecard-section">
                <h2>Scorecard Summary</h2>
                <div class="description">
                    <p>A performance test scorecard evaluates the test run against predefined quality rules and performance thresholds.</p>
                </div>

                <div class="scorecard-verdict-banner">
                    <xsl:choose>
                        <xsl:when test="$outcome/@testFailed = 'true'">
                            <span class="scorecard-verdict-badge failed">FAILED</span>
                        </xsl:when>
                        <xsl:otherwise>
                            <span class="scorecard-verdict-badge passed">PASSED</span>
                        </xsl:otherwise>
                    </xsl:choose>
                    <span class="scorecard-verdict-text">
                        The test achieved <b><xsl:value-of select="$outcome/@points" /></b> of <b><xsl:value-of select="$outcome/@totalPoints" /></b> points
                        (<b><xsl:value-of select="$outcome/@pointsPercentage" />%</b>).
                        <xsl:if test="$ratingDef">
                            Rating: <b><xsl:value-of select="$ratingDef/@name" /></b>
                            <xsl:if test="string-length($ratingDef/description) &gt; 0">
                                - <i><xsl:value-of select="$ratingDef/description" /></i>
                            </xsl:if>
                        </xsl:if>
                    </span>
                </div>

                <!-- Groups Overview Table -->
                <h3>Rule Groups</h3>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 25%;">Group</th>
                            <th style="width: 10%;">Status</th>
                            <th style="width: 15%;">Points</th>
                            <th>Message</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="$outcome/groups/group">
                            <xsl:variable name="groupId" select="@ref-id" />
                            <xsl:variable name="groupConfig" select="$config/groups/group[@id = $groupId]" />
                            <xsl:variable name="groupResult" select="result" />
                            <tr>
                                <td class="key">
                                    <xsl:choose>
                                        <xsl:when test="$groupConfig/@name">
                                            <xsl:value-of select="$groupConfig/@name" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$groupId" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value" style="text-align: center;">
                                    <xsl:variable name="resLower" select="translate($groupResult, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
                                    <span class="scorecard-badge {$resLower}">
                                        <xsl:value-of select="$groupResult" />
                                    </span>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="@points" /> / <xsl:value-of select="@totalPoints" />
                                </td>
                                <td class="value text">
                                    <xsl:value-of select="message" />
                                </td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>

                <!-- Failed Rules Section -->
                <xsl:variable name="failedRules" select="$outcome/groups/group/rules/rule[result = 'FAILED' or @testFailed = 'true']" />
                <xsl:choose>
                    <xsl:when test="count($failedRules) &gt; 0">
                        <h3>Failed Rules (<xsl:value-of select="count($failedRules)" />)</h3>
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 20%;">Group</th>
                                    <th style="width: 25%;">Rule</th>
                                    <th style="width: 10%;">Status</th>
                                    <th>Failure Details</th>
                                </tr>
                            </thead>
                            <tbody>
                                <xsl:for-each select="$failedRules">
                                    <xsl:variable name="ruleId" select="@ref-id" />
                                    <xsl:variable name="groupId" select="../../@ref-id" />
                                    <xsl:variable name="groupConfig" select="$config/groups/group[@id = $groupId]" />
                                    <xsl:variable name="ruleConfig" select="$config/rules/rule[@id = $ruleId]" />
                                    <tr>
                                        <td class="key">
                                            <xsl:choose>
                                                <xsl:when test="$groupConfig/@name">
                                                    <xsl:value-of select="$groupConfig/@name" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$groupId" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                        <td class="key">
                                            <xsl:choose>
                                                <xsl:when test="$ruleConfig/@name">
                                                    <xsl:value-of select="$ruleConfig/@name" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$ruleId" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                        <td class="value" style="text-align: center;">
                                            <span class="scorecard-badge failed">FAILED</span>
                                        </td>
                                        <td class="value text">
                                            <xsl:choose>
                                                <xsl:when test="string-length(message) &gt; 0">
                                                    <xsl:value-of select="message" />
                                                </xsl:when>
                                                <xsl:when test="string-length($ruleConfig/failMessage) &gt; 0">
                                                    <xsl:value-of select="$ruleConfig/failMessage" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$ruleConfig/description" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </tbody>
                        </table>
                    </xsl:when>
                    <xsl:otherwise>
                        <p style="color: #28a745; font-weight: bold; margin-top: 6px;">All scorecard rules passed successfully.</p>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:if test="count($outcome/issues/issue) &gt; 0">
                    <p class="scorecard-issues-note">
                        Note: <xsl:value-of select="count($outcome/issues/issue)" /> evaluation issue(s) occurred during scorecard evaluation. See scorecard report for details.
                    </p>
                </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
