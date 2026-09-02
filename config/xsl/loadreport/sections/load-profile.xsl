<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="load-profile">
		<xsl:param name="rootNode"/>
		<xsl:param name="loadMeter" select="'true'"/>

		<div class="section" id="load-profile">
			<xsl:call-template name="headline-load-profile"/>

			<div class="content">
				<xsl:call-template name="description-load-profile"/>

				<div class="data">
					<xsl:call-template name="load-profile-table">
						<xsl:with-param name="rootNode" select="$rootNode/loadProfile"/>
						<xsl:with-param name="loadMeter" select="$loadMeter"/>
					</xsl:call-template>
				</div>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>
