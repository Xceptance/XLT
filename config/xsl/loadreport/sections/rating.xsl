<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template name="rating-section">
        <xsl:param name="rootNode" />

        <xsl:if test="$rootNode">
            <div id="rating" class="section">
                <xsl:call-template name="headline-rating" />

                <div class="content">
                    <xsl:call-template name="description-rating"/>

                    <div class="rating-container">
                        <xsl:if test="$rootNode/score">
                            <div class="rating-bar">
                                <span class="rating-item rating-item-aplus">
                                    <xsl:if test="$rootNode/score = 'A+'">
                                        <xsl:attribute name="class">rating-item rating-item-aplus rating-active</xsl:attribute>
                                    </xsl:if>
                                    <xsl:text>A+</xsl:text>
                                </span>
                                <span class="rating-item rating-item-a">
                                    <xsl:if test="$rootNode/score = 'A'">
                                        <xsl:attribute name="class">rating-item rating-item-a rating-active</xsl:attribute>
                                    </xsl:if>
                                    <xsl:text>A</xsl:text>
                                </span>
                                <span class="rating-item rating-item-b">
                                    <xsl:if test="$rootNode/score = 'B'">
                                        <xsl:attribute name="class">rating-item rating-item-b rating-active</xsl:attribute>
                                    </xsl:if>
                                    <xsl:text>B</xsl:text>
                                </span>
                                <span class="rating-item rating-item-c">
                                    <xsl:if test="$rootNode/score = 'C'">
                                        <xsl:attribute name="class">rating-item rating-item-c rating-active</xsl:attribute>
                                    </xsl:if>
                                    <xsl:text>C</xsl:text>
                                </span>
                                <span class="rating-item rating-item-d">
                                    <xsl:if test="$rootNode/score = 'D'">
                                        <xsl:attribute name="class">rating-item rating-item-d rating-active</xsl:attribute>
                                    </xsl:if>
                                    <xsl:text>D</xsl:text>
                                </span>
                                <span class="rating-item rating-item-f">
                                    <xsl:if test="$rootNode/score = 'F'">
                                        <xsl:attribute name="class">rating-item rating-item-f rating-active</xsl:attribute>
                                    </xsl:if>
                                    <xsl:text>F</xsl:text>
                                </span>
                            </div>
                        </xsl:if>
                        <xsl:if test="$rootNode/summary">
                            <div class="rating-summary">
                                <xsl:value-of select="$rootNode/summary" />
                            </div>
                        </xsl:if>
                    </div>
                    <xsl:if test="$rootNode/evaluation">
                        <div class="rating-evaluation">
                            <xsl:value-of select="$rootNode/evaluation" disable-output-escaping="yes" />
                        </div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
