<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Calculates the minimum value of a sequence of numbers. -->
    <xsl:template name="min">
        <xsl:param name="seq"/>

        <xsl:for-each select="$seq">
            <xsl:sort data-type="number" order="ascending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>