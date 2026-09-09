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
							<xsl:variable name="rawScore" select="normalize-space($rootNode/rating)" />
							<xsl:variable name="rawScoreUpper" select="translate($rawScore, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
							<xsl:variable name="scoreNorm">
								<xsl:choose>
									<xsl:when test="$rawScoreUpper = 'APLUS' or $rawScoreUpper = 'A_PLUS' or $rawScoreUpper = 'A PLUS' or $rawScore = 'A+' or $rawScore = 'a+'">A+</xsl:when>
									<xsl:otherwise><xsl:value-of select="$rawScoreUpper" /></xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<div class="rating-bar"><span class="rating-item rating-item-aplus"><xsl:if test="$scoreNorm = 'A+'"><xsl:attribute name="class">rating-item rating-item-aplus rating-active</xsl:attribute></xsl:if><xsl:text>A+</xsl:text></span><span class="rating-item rating-item-a"><xsl:if test="$scoreNorm = 'A'"><xsl:attribute name="class">rating-item rating-item-a rating-active</xsl:attribute></xsl:if><xsl:text>A</xsl:text></span><span class="rating-item rating-item-b"><xsl:if test="$scoreNorm = 'B'"><xsl:attribute name="class">rating-item rating-item-b rating-active</xsl:attribute></xsl:if><xsl:text>B</xsl:text></span><span class="rating-item rating-item-c"><xsl:if test="$scoreNorm = 'C'"><xsl:attribute name="class">rating-item rating-item-c rating-active</xsl:attribute></xsl:if><xsl:text>C</xsl:text></span><span class="rating-item rating-item-d"><xsl:if test="$scoreNorm = 'D'"><xsl:attribute name="class">rating-item rating-item-d rating-active</xsl:attribute></xsl:if><xsl:text>D</xsl:text></span><span class="rating-item rating-item-f"><xsl:if test="$scoreNorm = 'F'"><xsl:attribute name="class">rating-item rating-item-f rating-active</xsl:attribute></xsl:if><xsl:text>F</xsl:text></span></div>
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
