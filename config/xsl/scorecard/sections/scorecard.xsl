<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="scorecard">
    <xsl:param name="rootNode" />

    <xsl:variable name="error" select="$rootNode/error" />
    <xsl:variable name="ratingId" select="$rootNode/rating" />
    <xsl:variable name="config" select="$rootNode/preceding-sibling::configuration" />
    <xsl:variable name="ratingDefinition" select="$config/ratings/rating[@id=$ratingId]" />

    <div class="section" id="scorecard-result">
        <xsl:call-template name="headline-scorecard" />

        <div class="content">
            <xsl:call-template name="description-scorecard" />

            <xsl:choose>
                <xsl:when test="string-length($error) &gt; 0">
                    <div class="error">
                       The test could not be evaluated for the following reason:
                       <pre><xsl:value-of select="$error" /></pre>
                    </div><!-- /error -->
                </xsl:when>
                <xsl:otherwise>
                    <div class="paragraph verdict">

                            The test got <xsl:value-of select="$rootNode/@points" /> out of <xsl:value-of select="$rootNode/@totalPoints" /> points.
                            The rating result is <xsl:value-of select="concat($rootNode/@pointsPercentage, '%')" />.

                            <xsl:choose>
                                <xsl:when test="count($ratingDefinition) &gt; 0">
                                    <xsl:variable name="ratingDescription" select="concat('&quot;', normalize-space($ratingDefinition/description), '&quot;')" />

                            Based on the result, the test
                                    <xsl:choose>
                                        <xsl:when test="$rootNode[not(@testFailed='true')]">
                                        succeeded with <xsl:value-of select="$ratingDescription" />.
                                        </xsl:when>
                                        <xsl:when test="$ratingDefinition[not(@failsTest='true')]">
                                            <!--
                                                Lookup all 'rule' definitions in configuration that have 'failsTest' attribute set to 'true',
                                                collect the value of their 'id' attribute and filter out those that are not used as value for
                                                the 'ref-id' attribute of at least one 'rule' element node reachable via '$rootNode' having
                                                attribute 'testFailed' set to 'true'.
                                            -->
                                            <xsl:variable name="failedRules" select="filter($config//rule[@failsTest='true' and @enabled='true'], function($a){ count($rootNode//rule[@testFailed='true'][@ref-id=$a/@id]) &gt; 0 })" />
                                           <!--
                                                Lookup all 'group' definitions in configuration that have 'failsTest' attribute set to 'true',
                                                collect the value of their 'id' attribute and filter out those that are not used as value for
                                                the 'ref-id' attribute of at least one 'group' element node reachable via '$rootNode' having
                                                attribute 'testFailed' set to 'true'.
                                            -->
                                            <xsl:variable name="failedGroups" select="filter($config//group[@failsTest='true' and @enabled='true'], function($a){ count($rootNode//group[@testFailed='true'][@ref-id=$a/@id]) &gt; 0 })" />
                                            <xsl:choose>
                                                <xsl:when test="count($failedRules) &gt; 0">
                                        would have succeeded with <xsl:value-of select="$ratingDescription" /> but at least one rule still qualifies this test as failed:
                                                    <xsl:call-template name="items-quoted">
                                                        <xsl:with-param name="separator" select="', '" />
                                                        <xsl:with-param name="items" select="$failedRules" />
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:otherwise>
                                        would have succeeded with <xsl:value-of select="$ratingDescription" /> but at least one group still qualifies this test as failed:
                                                    <xsl:call-template name="items-quoted">
                                                        <xsl:with-param name="separator" select="', '" />
                                                        <xsl:with-param name="items" select="$failedGroups" />
                                                    </xsl:call-template>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        .
                                        </xsl:when>
                                        <xsl:otherwise>
                                        failed with <xsl:value-of select="$ratingDescription" />.
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:if test="$rootNode[@testFailed='true']">
                                The test failed due to a rule or group qualifying the test as failed.
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>

                    </div><!-- /paragraph -->
                </xsl:otherwise>
            </xsl:choose>

        </div><!-- /content -->
    </div><!-- /section -->
</xsl:template>

<xsl:template name="items-quoted">
    <xsl:param name="separator" select="''" />
    <xsl:param name="items" select="/.." />

    <xsl:for-each select="$items">
        <xsl:variable name="itemId" select="normalize-space(current()/@id)" />
        <xsl:variable name="itemName" select="normalize-space(current()/@name)" />

        <xsl:choose>
            <xsl:when test="string-length($itemName) &gt; 0">
                <span title="{$itemId}"><xsl:value-of select="concat('&quot;', $itemName, '&quot;')" /></span>
            </xsl:when>
            <xsl:otherwise>
                <span><xsl:value-of select="concat('&quot;', $itemId, '&quot;')" /></span>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="position() &lt; last()">
            <xsl:value-of select="$separator" />
        </xsl:if>

    </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
