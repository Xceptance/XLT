<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="requests">

		<div class="section" id="request-summary">
			<xsl:call-template name="headline-request-summary" />

			<div class="content">
				<xsl:call-template name="description-request-summary" />

				<xsl:call-template name="timer-section">
					<xsl:with-param name="elements" select="requests/*" />
                    <xsl:with-param name="summaryElement" select="summary/requests" />
					<xsl:with-param name="tableRowHeader">Request Name</xsl:with-param>
					<xsl:with-param name="type">request</xsl:with-param>
				</xsl:call-template>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>