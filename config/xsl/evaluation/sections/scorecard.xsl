<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="scorecard">
    <xsl:param name="rootNode" />

    <xsl:variable name="error" select="$rootNode/error" />
    <xsl:variable name="rating" select="normalize-space($rootNode/rating)" />

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
                    <div class="paragraph">

                            The test got <xsl:value-of select="$rootNode/@points" /> out of <xsl:value-of select="$rootNode/@totalPoints" /> points.
                            The rating result is <xsl:value-of select="concat($rootNode/@pointsPercentage, '%')" />.

                            <xsl:choose>
                                <xsl:when test="string-length($rating) &gt; 0">
                                    <xsl:variable name="ratingDefinition" select="$rootNode/preceding-sibling::configuration/ratings/rating[@name=$rating]" />
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
                                                status 'FAILED'.
                                            -->
                                            <xsl:variable name="hardFails" select="filter($rootNode/preceding-sibling::configuration/rules/rule[@failsTest='true']/@id, function($a){ count($rootNode//rule[normalize-space(status) = 'FAILED'][@ref-id=$a]) &gt; 0 })" />
                                        would have succeeded with <xsl:value-of select="$ratingDescription" /> but at least one rule still qualifies this test as failed
                                            <xsl:value-of select="concat('(',string-join($hardFails, ', '),').')" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                        failed with <xsl:value-of select="$ratingDescription" />.
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:if test="$rootNode[@testFailed='true']">
                                The test failed due to a rule qualifying the test as failed.
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>

                    </div><!-- /paragraph -->
                </xsl:otherwise>
            </xsl:choose>

        </div><!-- /content -->
    </div><!-- /section -->
</xsl:template>


</xsl:stylesheet>
