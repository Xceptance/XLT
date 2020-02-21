<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xalan"
    exclude-result-prefixes="xalan">

    <xsl:template name="timer-row-abs-value-tds">
        <xsl:param name="gid"/>
        <xsl:param name="valueName"/>
        <xsl:param name="showValues"/>
        <xsl:param name="format"/>

        <xsl:variable name="baseValue" select="trendValues/trendValue[1]"/>
        <xsl:for-each select="trendValues/trendValue">
            <xsl:call-template name="timer-cell">
                <xsl:with-param name="node" select="*[name() = $valueName]"/>
                <xsl:with-param name="baselineNode" select="$baseValue/*[name() = $valueName]"/>
                <xsl:with-param name="format" select="$format"/>
                <xsl:with-param name="showValue" select="$showValues"/>
                <xsl:with-param name="position" select="position()"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="timer-row-abs">
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
            <xsl:call-template name="timer-row-abs-value-tds">
                <xsl:with-param name="gid" select="$gid"/>
                <xsl:with-param name="valueName" select="$valueName"/>
                <xsl:with-param name="showValues" select="$showValues"/>
                <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
        </tr>
    </xsl:template>

    <xsl:template name="timer-summary-row-abs">
        <xsl:param name="gid"/>
        <xsl:param name="valueName"/>
        <xsl:param name="showValues"/>
        <xsl:param name="format"/>
        <xsl:param name="rows-in-table"/>

        <tr class="totals">
            <xsl:call-template name="create-totals-td">
                <xsl:with-param name="rows-in-table" select="$rows-in-table" />
            </xsl:call-template>
            
            <xsl:call-template name="timer-row-abs-value-tds">
                <xsl:with-param name="gid" select="$gid"/>
                <xsl:with-param name="valueName" select="$valueName"/>
                <xsl:with-param name="showValues" select="$showValues"/>
                <xsl:with-param name="format" select="$format"/>
            </xsl:call-template>
        </tr>
    </xsl:template>

</xsl:stylesheet>
