<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Calculates the maximum value of a sequence of numbers. -->
    <xsl:template name="max">
        <xsl:param name="seq"/>

        <xsl:for-each select="$seq">
            <xsl:sort data-type="number" order="descending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>