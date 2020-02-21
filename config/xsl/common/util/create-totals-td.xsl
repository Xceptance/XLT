<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <!-- Create the first <td> element for the totals row for a table,
         which contains 'Totals' plus the number of rows in the table (in parentheses) -->
    <xsl:template name="create-totals-td">
        <xsl:param name="rows-in-table" /><!-- the number of rows in the table -->
        <xsl:param name="class" select="'key'" /><!-- the class attribute of the td, defaults to "key" -->
        <xsl:param name="description" select="'Totals'" /><!-- the description text, defaults to "Totals" -->

        <td>
            <xsl:attribute name="class"><xsl:value-of select="$class" /></xsl:attribute>
            <xsl:copy-of select="$description" />
            <xsl:choose>
                <xsl:when test="$rows-in-table = 0"> (no entries)</xsl:when>
                <xsl:when test="$rows-in-table = 1"> (1 entry)</xsl:when>
                <xsl:otherwise> (<xsl:value-of select="$rows-in-table"/> entries)</xsl:otherwise>
            </xsl:choose>
        </td>
    </xsl:template>
    
</xsl:stylesheet>