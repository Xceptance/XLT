<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="rules">
    <xsl:param name="definitions" />
    <xsl:param name="results" />

    <div class="section" id="scorecard-rules">
        <xsl:call-template name="headline-rules"/>

        <div class="content">
            <xsl:call-template name="description-rules" />

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

                    <xsl:choose>
                        <xsl:when test="count($results) &gt; 0">
                            <tbody>
                                <xsl:for-each select="$results">
                                    <xsl:variable name="numRules" select="count(rule)" />
                                    <xsl:variable name="stripeClass">
                                        <xsl:choose>
                                            <xsl:when test="position() mod 2 = 0">
                                                <xsl:value-of select="'even'" />
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="'odd'" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:variable>
                                    <xsl:variable name="groupDef" select="ancestor-or-self::outcome/preceding-sibling::configuration/groups/group[@id=current()/ancestor-or-self::group/@ref-id]" />
                                    <xsl:variable name="groupMode" select="normalize-space($groupDef/@mode)" />
                                    <xsl:variable name="passedRules" select="rule[normalize-space(result)='PASSED']" />
                                    <xsl:for-each select="rule">
                                        <xsl:variable name="groupRule" select="current()" />
                                        <xsl:variable name="ruleId" select="$groupRule/@ref-id" />
                                        <xsl:variable name="ruleResult" select="normalize-space($groupRule/result)" />
                                        <xsl:variable name="isFirstRuleOfGroup" select="position() = 1" />
                                        <xsl:variable name="ruleDef" select="$definitions[@id=$ruleId]" />
                                        <xsl:variable name="numRuleChecks" select="count(checks/check)" />

                                        <xsl:variable name="activeClass">
                                            <xsl:if test="not($ruleDef/@enabled='true') or ($groupMode='firstPassed' and not($passedRules[1]/@ref-id=$ruleId)) or ($groupMode='lastPassed' and not($passedRules[last()]/@ref-id=$ruleId)) or ($groupMode='allPassed' and count($passedRules)!=$numRules)">
                                                <xsl:value-of select="'inactive'" />
                                            </xsl:if>
                                        </xsl:variable>

                                        <tr class="{$stripeClass}">
                                            <!-- Group Name/ID -->
                                            <xsl:if test="$isFirstRuleOfGroup">
                                                <xsl:call-template name="multiRowCell">
                                                    <xsl:with-param name="numRows" select="$numRules" />
                                                    <xsl:with-param name="cellContent">
                                                        <xsl:call-template name="name-or-id">
                                                            <xsl:with-param name="node" select="$groupDef" />
                                                        </xsl:call-template>
                                                    </xsl:with-param>
                                                    <xsl:with-param name="class" select="'key'" />
                                                </xsl:call-template>
                                            </xsl:if>
                                            <!-- Rule Name/ID -->
                                            <td class="{string-join(('key', $activeClass), ' ')}">
                                                <xsl:call-template name="name-or-id">
                                                    <xsl:with-param name="node" select="$ruleDef" />
                                                </xsl:call-template>
                                            </td>
                                            <!-- Rule Description -->
                                            <td class="{string-join(('value','text', $activeClass), ' ')}">
                                                <xsl:value-of select="$ruleDef/description" />
                                            </td>
                                            <!-- Enabled -->
                                            <td class="{string-join(('value', $activeClass), ' ')}">
                                                <xsl:value-of select="$ruleDef/@enabled = 'true'" />
                                            </td>
                                            <!-- Fails Test -->
                                            <td class="{string-join(('value', $activeClass), ' ')}">
                                                <xsl:call-template name="testFailTrigger">
                                                    <xsl:with-param name="rule-def" select="$ruleDef" />
                                                </xsl:call-template>
                                            </td>
                                            <!-- Negate Result -->
                                            <td class="{string-join(('value', $activeClass), ' ')}">
                                                <xsl:value-of select="$ruleDef/@negateResult = 'true'" />
                                            </td>
                                            <!-- Rule Check Count -->
                                            <td class="{string-join(('value', 'number', $activeClass), ' ')}">
                                                <xsl:value-of select="$numRuleChecks" />
                                            </td>
                                            <!-- Rule Result -->
                                            <td class="{string-join(('value', $activeClass), ' ')}">
                                                <xsl:value-of select="$ruleResult" />
                                            </td>
                                            <!-- Rule Message -->
                                            <td class="{string-join(('value', 'text', $activeClass), ' ')}">
                                                <xsl:value-of select="$groupRule/message" />
                                            </td>
                                            <!-- Max Points -->
                                            <td class="{string-join(('value', 'number', $activeClass), ' ')}">
                                                <xsl:value-of select="$ruleDef/@points" />
                                            </td>
                                            <!-- Points -->
                                            <td class="{string-join(('value', 'number', $activeClass), ' ')}">
                                                <xsl:value-of select="$groupRule/@points" />
                                            </td>
                                        </tr>
                                    </xsl:for-each><!-- /$results/rule -->
                                </xsl:for-each><!-- /$results -->
                            </tbody>
                        </xsl:when>
                        <xsl:otherwise>
                            <tbody>
                                <tr>
                                    <td class="no-data" colspan="11">No data available</td>
                                </tr>
                            </tbody>
                        </xsl:otherwise>
                    </xsl:choose>
                </table>
            </div><!-- /data -->
        </div><!-- /content -->
    </div><!-- /section -->

</xsl:template>

<xsl:template name="testFailTrigger">
    <xsl:param name="rule-def" select="/.." />
    
    <xsl:variable name="failsTest" select="$rule-def/@failsTest = 'true'" />
    <xsl:variable name="trigger" select="normalize-space($rule-def/@failsOn)" />
    
    <xsl:choose>
        <xsl:when test="$failsTest and string-length($trigger) &gt; 0">
            <span title="{concat('Fails On: ', $trigger)}"><xsl:value-of select="$failsTest" /></span>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$failsTest" />
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
