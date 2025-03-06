<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="rule-checks">
    <xsl:param name="definitions" />
    <xsl:param name="results" />

    <div class="section" id="scorecard-rulechecks">
        <xsl:call-template name="headline-rulechecks" />

        <div class="content">
            <xsl:call-template name="description-rulechecks" />

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
                                        <xsl:variable name="checkSelector" select="$checkDef/selector" />
                                        <xsl:variable name="checkSelectorResolved">
                                            <xsl:choose>
                                                <xsl:when test="string-length($checkSelector/@ref-id) &gt; 0">
                                                    <xsl:value-of select="ancestor-or-self::outcome/preceding-sibling::configuration/selectors/selector[@id=$checkSelector/@ref-id]/expression" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$checkSelector" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <tr class="{$stripeClass}">
                                            <!-- Rule ID -->
                                            <xsl:if test="position() = 1" >
                                                <xsl:call-template name="multiRowCell">
                                                    <xsl:with-param name="numRows" select="$numChecks" />
                                                    <xsl:with-param name="cellContent">
                                                        <xsl:call-template name="name-or-id">
                                                            <xsl:with-param name="node" select="$ruleDef" />
                                                        </xsl:call-template>
                                                    </xsl:with-param>
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
                                                <xsl:value-of select="$checkSelectorResolved" />
                                            </td>
                                            <!-- Check Condition -->
                                            <td class="value text">
                                                <xsl:value-of select="$checkDef/condition" />
                                            </td>
                                            <!-- Check Value -->
                                            <td class="value">
                                                <xsl:value-of select="./value" />
                                            </td>
                                           <!-- Check Result -->
                                            <td class="value">
                                                <xsl:value-of select="./result" />
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

</xsl:stylesheet>
