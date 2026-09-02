<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="rating-section">
		<xsl:param name="rootNode" />

		<xsl:if test="string-length(normalize-space($rootNode/rating)) > 0 or string-length(normalize-space($rootNode/ratingSummary)) > 0 or string-length(normalize-space($rootNode/ratingEvaluation)) > 0">
			<div id="rating" class="section">
				<xsl:call-template name="headline-rating" />

				<div class="content">
					<div class="rating-container">
						<xsl:if test="string-length(normalize-space($rootNode/rating)) > 0">
							<xsl:variable name="score" select="normalize-space($rootNode/rating)" />
							<xsl:variable name="scoreLower" select="translate($score, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
							<div class="rating-badge rating-badge-{$scoreLower}">
								<span class="rating-letter"><xsl:value-of select="$score" /></span>
							</div>
						</xsl:if>
						<xsl:if test="string-length(normalize-space($rootNode/ratingSummary)) > 0">
							<div class="rating-summary">
								<xsl:value-of select="$rootNode/ratingSummary" />
							</div>
						</xsl:if>
					</div>
					<xsl:if test="string-length(normalize-space($rootNode/ratingEvaluation)) > 0">
						<div class="rating-evaluation">
							<xsl:value-of select="$rootNode/ratingEvaluation" disable-output-escaping="yes" />
						</div>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
