<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="name-or-id">
    <xsl:param name="node" select="/.." />

    <xsl:variable name="nodeName" select="$node/@name" />
    <xsl:variable name="nodeId" select="$node/@id" />

    <xsl:choose>
        <xsl:when test="string-length($nodeName) &gt; 0">
            <xsl:element name="span">
                <xsl:attribute name="title">
                    <xsl:value-of select="$nodeId" />
                </xsl:attribute>
                <xsl:value-of select="$nodeName" />
            </xsl:element>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$nodeId" />
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="multiRowCell">
    <xsl:param name="numRows" select="1" />
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
        <xsl:copy-of select="$cellContent" />
    </xsl:element>
</xsl:template>

</xsl:stylesheet>
