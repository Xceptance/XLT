<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="testcomment">

		<xsl:param name="rootNode" />

		<xsl:choose>
			<xsl:when test="string-length(normalize-space($rootNode)) > 0">
				<div class="description">
					<xsl:for-each select="$rootNode/string">
						<div class="paragraph">
							<xsl:value-of select="." disable-output-escaping="yes" />
						</div>
					</xsl:for-each>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<!-- Empty on purpose for now -->
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>