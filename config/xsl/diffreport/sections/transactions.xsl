<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="transactions">

		<div class="section" id="transaction-summary">
			<xsl:call-template name="headline-transaction-summary" />

			<div class="content">
				<xsl:call-template name="description-transaction-summary" />

				<xsl:call-template name="timer-section">
					<xsl:with-param name="elements" select="transactions/*" />
                    <xsl:with-param name="summaryElement" select="summary/transactions" />
					<xsl:with-param name="tableRowHeader">Transaction Name</xsl:with-param>
					<xsl:with-param name="type">transaction</xsl:with-param>
				</xsl:call-template>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>