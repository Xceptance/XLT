<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="page-load-timings">

		<div class="section" id="page-load-timing-summary">
			<xsl:call-template name="headline-page-load-timing-summary" />

            <div class="content">
				<xsl:call-template name="description-page-load-timing-summary" />

				<xsl:call-template name="timer-section">
					<xsl:with-param name="elements" select="pageLoadTimings/*" />
                    <xsl:with-param name="summaryElement" select="summary/pageLoadTimings" />
					<xsl:with-param name="tableRowHeader">Page Load Timing Name</xsl:with-param>
					<xsl:with-param name="type">pageLoadTiming</xsl:with-param>
				</xsl:call-template>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>