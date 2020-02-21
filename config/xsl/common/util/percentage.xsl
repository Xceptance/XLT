<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="percentage">
<xsl:param name="n1"/> <!-- 100% value -->
<xsl:param name="n2"/> <!-- value to determine percentage for -->
	
<xsl:choose>
	<xsl:when test="$n1 &gt; 0"><xsl:value-of select="$n2 * 100 div $n1"/></xsl:when>
	<xsl:otherwise>0</xsl:otherwise>
</xsl:choose>

</xsl:template>

</xsl:stylesheet>