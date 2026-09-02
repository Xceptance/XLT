<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Template for showing up to two labels directly and hiding all other labels behind a hoverable "+X" element. -->
    <!-- Input parameter is a string containing one or more labels separated by whitespaces. -->
    <xsl:template name="timer-labels">
        <xsl:param name="labelString"/>

        <xsl:variable name="normalizedText" select="normalize-space($labelString)"/>

        <xsl:if test="$normalizedText != ''">
            <!-- get number of labels by determining the number of delimiting spaces in the normalized text -->
            <xsl:variable name="count" select="string-length($normalizedText) - string-length(translate($normalizedText, ' ', '')) + 1"/>

            <xsl:choose>
                <!-- 1 label -->
                <xsl:when test="$count = 1">
                    <xsl:call-template name="label-tag">
                        <xsl:with-param name="text" select="$normalizedText"/>
                    </xsl:call-template>
                </xsl:when>
                <!-- 2 labels -->
                <xsl:when test="$count = 2">
                    <xsl:call-template name="label-tag">
                        <xsl:with-param name="text" select="substring-before($normalizedText, ' ')"/>
                    </xsl:call-template>
                    <xsl:call-template name="label-tag">
                        <xsl:with-param name="text" select="substring-after($normalizedText, ' ')"/>
                    </xsl:call-template>
                </xsl:when>
                <!-- 3 or more labels -->
                <xsl:when test="$count &gt;= 3">
                    <!-- show the first two labels -->
                    <xsl:call-template name="label-tag">
                        <xsl:with-param name="text" select="substring-before($normalizedText, ' ')"/>
                    </xsl:call-template>
                    <xsl:variable name="normalizedTextAfterFirstLabel" select="substring-after($normalizedText, ' ')"/>
                    <xsl:call-template name="label-tag">
                        <xsl:with-param name="text" select="substring-before($normalizedTextAfterFirstLabel, ' ')"/>
                    </xsl:call-template>

                    <!-- add a "+X" label that shows a cluetip with the remaining labels on hover -->
                    <xsl:variable name="gid" select="generate-id(.)"/>
                    <span class="label-chip label-chip-more">
                        <a>
                            <xsl:attribute name="href"/>
                            <xsl:attribute name="onclick">return false;</xsl:attribute>
                            <xsl:attribute name="data-rel">#more-labels-<xsl:value-of select="$gid"/></xsl:attribute>
                            <xsl:attribute name="class">cluetip</xsl:attribute>
                            <xsl:text>+</xsl:text><xsl:value-of select="$count - 2"/>
                        </a>
                        <xsl:text></xsl:text>
                        <div id="more-labels-{$gid}" class="cluetip-data">
                            <div class="label-container">
                                <xsl:call-template name="label-list">
                                    <xsl:with-param name="labelString" select="substring-after($normalizedTextAfterFirstLabel, ' ')"/>
                                </xsl:call-template>
                            </div>
                        </div>
                    </span>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- Template for showing a series of labels. -->
    <!-- Input parameter is a string containing one or more labels separated by whitespaces. -->
    <xsl:template name="label-list">
        <xsl:param name="labelString"/>

        <xsl:variable name="normalizedText" select="normalize-space($labelString)"/>

        <xsl:choose>
            <xsl:when test="contains($normalizedText, ' ')">
                <xsl:call-template name="label-tag">
                    <xsl:with-param name="text" select="substring-before($normalizedText, ' ')"/>
                </xsl:call-template>
                <xsl:call-template name="label-list">
                    <xsl:with-param name="labelString" select="substring-after($normalizedText, ' ')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$normalizedText != ''">
                <xsl:call-template name="label-tag">
                    <xsl:with-param name="text" select="$normalizedText"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- Template for an individual label tag with the given text. -->
    <xsl:template name="label-tag">
        <xsl:param name="text"/>

        <span class="label-chip">
            <xsl:attribute name="title"><xsl:value-of select="$text"/></xsl:attribute>
            <xsl:value-of select="$text"/>
        </span>
    </xsl:template>

</xsl:stylesheet>