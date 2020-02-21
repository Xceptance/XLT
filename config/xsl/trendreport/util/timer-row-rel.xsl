<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xalan"
    exclude-result-prefixes="xalan">

    <xsl:template name="timer-row-rel-value-tds">
        <xsl:param name="gid"/>
        <xsl:param name="valueName"/>
        <xsl:param name="showValues"/>
        <xsl:param name="format"/>

        <xsl:for-each select="trendValues/trendValue">
            <xsl:variable name="previousValue">
                <xsl:choose>
                    <xsl:when test="position() = 1">
                        <xsl:value-of select="*[name() = $valueName]"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="preceding-sibling::*[1]/*[name() = $valueName]"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:call-template name="timer-cell">
                <xsl:with-param name="node" select="*[name() = $valueName]"/>
                <xsl:with-param name="baselineNode" select="$previousValue"/>
                <xsl:with-param name="format" select="$format"/>
                <xsl:with-param name="showValue" select="$showValues"/>
                <xsl:with-param name="position" select="position()"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="timer-row-rel">
        <xsl:param name="gid"/>
        <xsl:param name="valueName"/>
        <xsl:param name="showValues"/>
        <xsl:param name="format"/>

        <tr>
            <!-- name -->
            <td class="key">
                <a>
                    <xsl:attribute name="href">#timerchart-<xsl:value-of select="$gid"/></xsl:attribute>
                    <xsl:value-of select="name"/>
                </a>
            </td>

            <!-- values -->
            <xsl:call-template name="timer-row-rel-value-tds">
                <xsl:with-param name="gid" select="$gid"/>
                <xsl:with-param name="valueName" select="$valueName"/>
                <xsl:with-param name="showValues" select="$showValues"/>
                <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
        </tr>
    </xsl:template>

    <xsl:template name="timer-summary-row-rel">
        <xsl:param name="gid"/>
        <xsl:param name="valueName"/>
        <xsl:param name="showValues"/>
        <xsl:param name="format"/>
        <xsl:param name="rows-in-table"/>

        <tr class="totals">
            <xsl:call-template name="create-totals-td">
                <xsl:with-param name="rows-in-table" select="$rows-in-table" />
            </xsl:call-template>
            
            <xsl:call-template name="timer-row-rel-value-tds">
                <xsl:with-param name="gid" select="$gid"/>
                <xsl:with-param name="valueName" select="$valueName"/>
                <xsl:with-param name="showValues" select="$showValues"/>
                <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
        </tr>
    </xsl:template>

</xsl:stylesheet>
