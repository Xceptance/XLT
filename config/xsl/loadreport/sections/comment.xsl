<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="testcomment">
		<xsl:param name="rootNode" />

		<div id="comment" class="section">
			<xsl:call-template name="headline-testcomment" />

			<div class="content">
				<xsl:choose>
					<xsl:when test="string-length(normalize-space($rootNode)) > 0">
						<xsl:for-each select="$rootNode/string">
							<div class="paragraph">
								<xsl:value-of select="." disable-output-escaping="yes" />
							</div>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<div class="paragraph">
							No comment was given.
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</div>
		</div>
	</xsl:template>
</xsl:stylesheet>
