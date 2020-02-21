<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Create the first <td> element for the totals row for a table, which contains 'Totals' plus the number of rows in the table 
        (in parentheses) -->
    <xsl:template name="convert-apdex-to-color">
        <xsl:param name="apdex"/><!-- the number of rows in the table -->

        <xsl:choose>
            <xsl:when test="$apdex >= 0.94">
                <xsl:text>apdex-excellent</xsl:text>
            </xsl:when>
            <xsl:when test="$apdex >= 0.85">
                <xsl:text>apdex-good</xsl:text>
            </xsl:when>
            <xsl:when test="$apdex >= 0.70">
                <xsl:text>apdex-fair</xsl:text>
            </xsl:when>
            <xsl:when test="$apdex >= 0.50">
                <xsl:text>apdex-poor</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>apdex-unacceptable</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>