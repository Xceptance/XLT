<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="custom-timers">

		<div class="section" id="custom-timer-summary">
			<xsl:call-template name="headline-custom-timer-summary" />

            <div class="content">
				<xsl:call-template name="description-custom-timer-summary" />

				<xsl:call-template name="timer-section">
					<xsl:with-param name="elements" select="customTimers/*" />
                    <xsl:with-param name="summaryElement" select="summary/customTimers" />
					<xsl:with-param name="tableRowHeader">Custom Timer Name</xsl:with-param>
					<xsl:with-param name="type">custom</xsl:with-param>
				</xsl:call-template>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>