<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- replace forbidden chars by $<HEX> while <HEX> is the ASCII hex representation of that character -->
	<xsl:template name="convertIllegalCharactersInFileName">
		<xsl:param name="filename" />
		
		
		<!-- Replacing the dollar sign must be first replacement! -->
		<!-- $ -->
		<xsl:variable name="dollar">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$filename" />
				<xsl:with-param name="replace" select="string('$')" />
				<xsl:with-param name="by" select="string('$24')" />
			</xsl:call-template>
		</xsl:variable>

		<!-- ? -->
		<xsl:variable name="questionMark">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$dollar" />
				<xsl:with-param name="replace" select="string('?')" />
				<xsl:with-param name="by" select="string('$3f')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- : -->
		<xsl:variable name="colon">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$questionMark" />
				<xsl:with-param name="replace" select="string(':')" />
				<xsl:with-param name="by" select="string('$3a')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- / -->
		<xsl:variable name="slash">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$colon" />
				<xsl:with-param name="replace" select="string('/')" />
				<xsl:with-param name="by" select="string('$2f')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- # -->
		<xsl:variable name="sharp">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$slash" />
				<xsl:with-param name="replace" select="string('#')" />
				<xsl:with-param name="by" select="string('$23')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- % -->
		<xsl:variable name="percent">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$sharp" />
				<xsl:with-param name="replace" select="string('%')" />
				<xsl:with-param name="by" select="string('$25')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- , -->
		<xsl:variable name="comma">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$percent" />
				<xsl:with-param name="replace" select="string(',')" />
				<xsl:with-param name="by" select="string('$2c')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- ; -->
		<xsl:variable name="semicolon">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$comma" />
				<xsl:with-param name="replace" select="string(';')" />
				<xsl:with-param name="by" select="string('$3b')" />
			</xsl:call-template>
		</xsl:variable>

		<!-- * -->
		<xsl:variable name="asterisk">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$semicolon" />
				<xsl:with-param name="replace" select="string('*')" />
				<xsl:with-param name="by" select="string('$2a')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- | -->
		<xsl:variable name="pipe">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$asterisk" />
				<xsl:with-param name="replace" select="string('|')" />
				<xsl:with-param name="by" select="string('$7c')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- \ -->
		<xsl:variable name="backslash">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$pipe" />
				<xsl:with-param name="replace" select="string('\')" />
				<xsl:with-param name="by" select="string('$5c')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- lt; -->
		<xsl:variable name="lt">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$backslash" />
				<xsl:with-param name="replace" select="string('&lt;')" />
				<xsl:with-param name="by" select="string('$3c')" />
			</xsl:call-template>
		</xsl:variable>
		
		<!-- gt; -->
		<xsl:variable name="gt">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$lt" />
				<xsl:with-param name="replace" select="string('&gt;')" />
				<xsl:with-param name="by" select="string('$3e')" />
			</xsl:call-template>
		</xsl:variable>

		<!-- " -->
		<xsl:variable name="quote">
			<xsl:call-template name="string-replace-all">
				<xsl:with-param name="text" select="$gt" />
				<xsl:with-param name="replace" select="string('&quot;')" />
				<xsl:with-param name="by" select="string('$22')" />
			</xsl:call-template>
		</xsl:variable>
	 
		<xsl:value-of select="$quote" />
		
	</xsl:template>

</xsl:stylesheet>