<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="rules">
    <xsl:param name="definitions" />
    <xsl:param name="results" />

    <xsl:variable name="count" select="count($results)"/>

    <div class="section" id="scorecard-rules">
        <xsl:call-template name="headline-rules"/>

        <div class="content">
            <xsl:call-template name="description-rules" />

            <div class="data">
                <table class="no-auto-stripe">
                    <thead>
                        <tr>
                            <th>Group ID</th>
                            <th>Rule ID</th>
                            <th>Description</th>
                            <th>Enabled</th>
                            <th>Fails Test</th>
                            <th>Check Count</th>
                            <th>Status</th>
                            <th>Message</th>
                            <th>Max Points</th>
                            <th>Points</th>
                        </tr>
                    </thead>

                    <xsl:variable name="count" select="count($results)"/>
                    <xsl:choose>
                        <xsl:when test="$count &gt; 0">
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

                                    <xsl:for-each select="./rule">
                                        <xsl:variable name="groupRule" select="current()" />
                                        <xsl:variable name="ruleId" select="$groupRule/@ref-id" />
                                        <xsl:variable name="isFirstRuleOfGroup" select="position() = 1" />
                                        <xsl:variable name="ruleDef" select="$definitions[@id=$ruleId]" />normalize-space(
                                        <xsl:variable name="numRuleChecks" select="count(checks/check)" />

                                        <tr class="{$stripeClass}">
                                            <!-- Group ID -->
                                            <xsl:if test="$isFirstRuleOfGroup">
                                                <xsl:call-template name="multiRowCell">
                                                    <xsl:with-param name="numRows" select="$numRules" />
                                                    <xsl:with-param name="cellContent" select="$groupRule/ancestor::group/@ref-id" />
                                                    <xsl:with-param name="class" select="'key'" />
                                                </xsl:call-template>
                                            </xsl:if>
                                            <!-- Rule ID -->
                                            <td class="key">
                                                <xsl:value-of select="$ruleId" />
                                            </td>
                                            <!-- Rule Description -->
                                            <td class="value text">
                                                <xsl:value-of select="$ruleDef/description" />
                                            </td>
                                            <!-- Enabled -->
                                            <td class="value">
                                                <xsl:value-of select="$ruleDef/@enabled = 'true'" />
                                            </td>
                                            <!-- Fails Test -->
                                            <td class="value">
                                                <xsl:value-of select="$ruleDef/@failsTest = 'true'" />
                                            </td>
                                            <!-- Rule Check Count -->
                                            <td class="value number">
                                                <xsl:value-of select="$numRuleChecks" />
                                            </td>
                                            <!-- Rule Status -->
                                            <td class="value text">
                                                <xsl:value-of select="$groupRule/status" />
                                            </td>
                                            <!-- Rule Message -->
                                            <td class="value text">
                                                <xsl:value-of select="$groupRule/message" />
                                            </td>
                                            <!-- Max Points + Points-->
                                            <td class="value number">
                                                <xsl:value-of select="$ruleDef/@points" />
                                            </td>
                                            <td class="value number">
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
                                    <td class="no-data" colspan="9">No data available</td>
                                </tr>
                            </tbody>
                        </xsl:otherwise>
                    </xsl:choose>
                </table>
            </div><!-- /data -->
        </div><!-- /content -->
    </div><!-- /section -->

    <div class="section" id="scorecard-rulechecks">
        <xsl:call-template name="headline-rulechecks" />

        <div class="content">
            <xsl:call-template name="description-rulechecks" />

            <div class="data">
                <table class="no-auto-stripe">
                    <thead>
                        <tr>
                            <th>Rule ID</th>
                            <th>Check Number</th>
                            <th>Enabled</th>
                            <th>Display Value</th>
                            <th>Selector</th>
                            <th>Condition</th>
                            <th>Value</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <xsl:choose>
                    <xsl:when test="count($results/rule) &gt; 0">
                        <tbody>
                        <xsl:for-each select="$definitions">
                            <xsl:variable name="ruleDef" select="current()" />
                            <xsl:variable name="numChecks" select="count($ruleDef/checks/check)" />
                            <xsl:variable name="ruleId" select="$ruleDef/@id" />

                            <xsl:variable name="ruleResult" select="$results/rule[@ref-id=$ruleId]" />
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

                            <xsl:if test="count($ruleResult) &gt; 0">

                                <xsl:for-each select="$ruleResult[1]/checks/check">
                                    <xsl:variable name="checkDef" select="$ruleDef/checks/check[@index=current()/@index]" />

                                    <tr class="{$stripeClass}">
                                        <!-- Rule ID -->
                                        <xsl:if test="position() = 1" >
                                            <xsl:call-template name="multiRowCell">
                                                <xsl:with-param name="numRows" select="$numChecks" />
                                                <xsl:with-param name="cellContent" select="$ruleId" />
                                                <xsl:with-param name="class" select="'key'" />
                                            </xsl:call-template>
                                        </xsl:if>
                                        <!-- Check Number -->
                                        <td class="value number">
                                            <xsl:value-of select="number(@index) + 1" />
                                        </td>
                                        <!-- Enabled -->
                                        <td class="value">
                                            <xsl:value-of select="$checkDef/@enabled = 'true'" />
                                        </td>
                                        <!-- Display Value -->
                                        <td class="value">
                                            <xsl:value-of select="$checkDef/@displayValue = 'true'" />
                                        </td>
                                        <!-- Check Selector -->
                                        <td class="value text">
                                            <xsl:value-of select="$checkDef/selector" />
                                        </td>
                                        <!-- Check Condition -->
                                        <td class="value text">
                                            <xsl:value-of select="$checkDef/condition" />
                                        </td>
                                        <!-- Check Value -->
                                        <td class="value text">
                                            <xsl:value-of select="./value" />
                                        </td>
                                       <!-- Check Status -->
                                        <td class="value">
                                            <xsl:value-of select="./status" />
                                        </td>
                                    </tr>

                                </xsl:for-each>
                            </xsl:if>
                        </xsl:for-each>
                        </tbody>
                    </xsl:when>
                    <xsl:otherwise>
                    <tbody>
                        <tr>
                            <td class="no-data" colspan="7">No data available</td>
                        </tr>
                    </tbody>
                    </xsl:otherwise>
                    </xsl:choose>

                </table>
            </div><!-- /data -->
        </div><!-- /content -->
    </div><!-- /section -->
</xsl:template>

<xsl:template name="multiRowCell">
    <xsl:param name="numRows" select="1"/>
    <xsl:param name="cellContent" />
    <xsl:param name="class" />

    <xsl:element name="td">
        <xsl:if test="$numRows &gt; 1">
            <xsl:attribute name="rowspan">
                <xsl:value-of select="$numRows" />
            </xsl:attribute>
        </xsl:if>
        <xsl:if test="string-length($class) &gt; 0">
            <xsl:attribute name="class">
                <xsl:value-of select="$class" />
            </xsl:attribute>
        </xsl:if>
        <xsl:value-of select="$cellContent" />
    </xsl:element> 
</xsl:template>

</xsl:stylesheet>
