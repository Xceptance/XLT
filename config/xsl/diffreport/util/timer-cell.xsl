<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="timer-cell">
		<xsl:param name="node"/>
		<xsl:param name="isInverse"/>
		<xsl:param name="format"/>

		<xsl:variable name="value">
			<xsl:choose>
				<xsl:when test="count($node/newValue) = 0">
					<xsl:value-of select="'(removed)'"/>
				</xsl:when>
				<xsl:when test="count($node/oldValue) = 0">
					<xsl:value-of select="'(added)'"/>
				</xsl:when>
				<xsl:when test="contains($node/relativeDifference, 'Infinity')">
					<![CDATA[&infin;]]>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$node/absoluteDifference > 0">
						<xsl:text>+</xsl:text>
					</xsl:if>
					<xsl:value-of select="format-number($node/relativeDifference, '#,##0.00')"/>
					<xsl:text>%</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="colorClass">
			<xsl:choose>
				<xsl:when test="count($node/newValue) = 0">
					<xsl:value-of select="'removed'"/>
				</xsl:when>
				<xsl:when test="count($node/oldValue) = 0">
					<xsl:value-of select="'added'"/>
				</xsl:when>
				<xsl:when test="contains($node/relativeDifference, 'Infinity')">
					<xsl:value-of select="'infinity'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="classNumber">
						<xsl:choose>
							<xsl:when test="$node/relativeDifference &lt; -99 or $node/relativeDifference &gt; 99">
								<xsl:value-of select="100"/>
							</xsl:when>
							<xsl:when test="$node/relativeDifference &lt; 0">
								<xsl:value-of select="ceiling(-$node/relativeDifference)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="ceiling($node/relativeDifference)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="($node/relativeDifference &lt; 0) = $isInverse">
							<xsl:text>n</xsl:text>
							<xsl:value-of select="$classNumber"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>p</xsl:text>
							<xsl:value-of select="$classNumber"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<td>
			<xsl:attribute name="title">
				<xsl:value-of select="format-number($node/oldValue, $format)"/> 
				<xsl:text> -> </xsl:text>
				<xsl:value-of select="format-number($node/newValue, $format)"/>
				<xsl:text> (</xsl:text>
				<xsl:if test="$node/absoluteDifference > 0">+</xsl:if>
				<xsl:value-of select="format-number($node/absoluteDifference, $format)"/>
				<xsl:text>)</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="class">
				<xsl:text>value number </xsl:text>
				<xsl:value-of select="$colorClass"/> 
			</xsl:attribute>
			<xsl:value-of select="$value" disable-output-escaping="yes"/>
		</td>

	</xsl:template>

</xsl:stylesheet>