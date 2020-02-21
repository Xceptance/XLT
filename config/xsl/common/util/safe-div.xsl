<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="safe-div">
<xsl:param name="n1"/>
<xsl:param name="n2"/>
	
<!-- <xsl:message> max-3 n1: <xsl:value-of select="$n1" /></xsl:message> -->
<!-- <xsl:message> max-3 n2: <xsl:value-of select="$n2" /></xsl:message> -->
<!-- <xsl:message> max-3 n3: <xsl:value-of select="$n3" /></xsl:message> -->

<xsl:choose>
	<xsl:when test="$n2 &gt; 0">
		<xsl:value-of select="$n1 div $n2"/>
	</xsl:when>
	<xsl:otherwise>
		0
	</xsl:otherwise>
</xsl:choose>

</xsl:template>

</xsl:stylesheet>