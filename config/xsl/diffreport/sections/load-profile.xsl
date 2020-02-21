<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="load-profile">
		<xsl:param name="rootNode"/>
		<xsl:param name="id"/>

		<div class="subsection" id="{$id}">
			<div class="content">
				<div class="data">
					<xsl:call-template name="load-profile-table">
						<xsl:with-param name="rootNode" select="$rootNode"/>
					</xsl:call-template>
				</div>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>
