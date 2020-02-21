<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="format-bytes">
		<xsl:param name="bytes" /><!-- the number of bytes -->

		<xsl:choose>
			<xsl:when test="$bytes &gt; 1099511627776">
				<xsl:value-of select="format-number($bytes div 1099511627776, '#,##0.0')" />
				TB
			</xsl:when>
			<xsl:when test="$bytes &gt; 1073741824">
				<xsl:value-of select="format-number($bytes div 1073741824, '#,##0.0')" />
				GB
			</xsl:when>
			<xsl:when test="$bytes &gt; 1048576">
				<xsl:value-of select="format-number($bytes div 1048576, '#,##0.0')" />
				MB
			</xsl:when>
			<xsl:when test="$bytes &gt; 1024">
				<xsl:value-of select="format-number($bytes div 1024, '#,##0.0')" />
				KB
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="format-number($bytes, '#,##0')" />
				B
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

</xsl:stylesheet>