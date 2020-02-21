<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
>

    <!-- Document Settings -->
    <xsl:output method="xml" indent="yes" encoding="US-ASCII" />
    <xsl:decimal-format decimal-separator="."
        grouping-separator="," />
    <xsl:strip-space elements="*" />

    <xsl:template match="log">
        <xsl:element name="bugs">
            <xsl:for-each select="logentry">
                <xsl:if test="contains(msg, '#')">
                    <xsl:call-template name="get-info">
                        <xsl:with-param name="info-string">
                            <xsl:value-of select="msg" />
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template name="get-info">
        <xsl:param name="info-string" />
        <xsl:variable name="head">
            <xsl:choose>
                <xsl:when test="contains(substring-after($info-string, '#'),'#')" >
                    <xsl:value-of select="concat('#', substring-before(substring-after($info-string, '#'),'#'))" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$info-string" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="bug-no"
            select="normalize-space(substring-before(substring-after($head, '#'),':'))" />
        <xsl:variable name="bug-msg" select="normalize-space(substring-after($head, ':'))" />

        <xsl:if
            test="string-length($bug-no) > 0 and string-length($bug-msg) > 0"
        >
            <xsl:element name="bug">
                <xsl:attribute name="no">
                <xsl:value-of select="$bug-no" />
            </xsl:attribute>
                <xsl:value-of select="$bug-msg" />
            </xsl:element>
            <xsl:call-template name="get-info">
                <xsl:with-param name="info-string">
                    <xsl:value-of
                        select="substring-after($info-string, $head)" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>