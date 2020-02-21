<?xml version="1.0"?>
<!DOCTYPE stylesheet [
    <!ENTITY infin "&#8734;" >
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template name="timer-cell">
		<xsl:param name="node"/>
		<xsl:param name="baselineNode"/>
		<xsl:param name="isInverse"/>
		<xsl:param name="format"/>
		<xsl:param name="showValue"/>
		<xsl:param name="position"/>

		<xsl:variable name="value">
			<xsl:choose>
				<xsl:when test="contains($node, 'null') and contains($baselineNode, 'null')">
					<xsl:value-of select="'(n/a)'"/>
				</xsl:when>
				<xsl:when test="contains($node, 'null')">
					<xsl:value-of select="'(removed)'"/>
				</xsl:when>
				<xsl:when test="contains($baselineNode, 'null')">
					<xsl:value-of select="'(added)'"/>
				</xsl:when>
				<xsl:when test="$baselineNode = 0 and $node &gt; 0">
					<xsl:text>&infin;</xsl:text>
				</xsl:when>
				<xsl:when test="$baselineNode = $node">
					<xsl:value-of select="0"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$node * 100 div $baselineNode - 100"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="colorClass">
			<xsl:choose>
				<xsl:when test="$value = '(n/a)'">
					<xsl:value-of select="'removed'"/>
				</xsl:when>
				<xsl:when test="$value = '(removed)'">
					<xsl:value-of select="'removed'"/>
				</xsl:when>
				<xsl:when test="$value = '(added)'">
					<xsl:value-of select="'added'"/>
				</xsl:when>
				<xsl:when test="$value = '&infin;'">
					<xsl:value-of select="'infinity'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="classNumber">
						<xsl:choose>
							<xsl:when test="$value &lt; -99 or $value &gt; 99">
								<xsl:value-of select="100"/>
							</xsl:when>
							<xsl:when test="$value &lt; 0">
								<xsl:value-of select="ceiling(-$value)"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="ceiling($value)"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="($value &lt; 0) = $isInverse">
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

		<xsl:variable name="percentageValue">
			<xsl:choose>
				<xsl:when test="$value = '(n/a)'">
					<xsl:value-of select="$value"/>
				</xsl:when>
				<xsl:when test="$value = '(removed)'">
					<xsl:value-of select="$value"/>
				</xsl:when>
				<xsl:when test="$value = '(added)'">
					<xsl:value-of select="$value"/>
				</xsl:when>
				<xsl:when test="$value = '&infin;'">
					<xsl:text>&infin;</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$value &gt; 0">
						<xsl:text>+</xsl:text>
					</xsl:if>
					<xsl:value-of select="format-number($value, '#,##0.0')"/>
					<xsl:text>%</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<td>
			<!-- base value, no diff display -->
			<xsl:if test="$position = 1">
				<xsl:attribute name="title">
					<xsl:value-of select="format-number($node, $format)"/>
				</xsl:attribute>
			</xsl:if>

			<!-- a trend value, diff display -->
			<xsl:if test="$position &gt; 1">
				<xsl:attribute name="title">
					<xsl:value-of select="format-number($baselineNode, $format)"/> 
					<xsl:text> -> </xsl:text>
					<xsl:value-of select="format-number($node, $format)"/>
					<xsl:text> (</xsl:text>
					<xsl:if test="$node &gt; $baselineNode">+</xsl:if>
					<xsl:value-of select="format-number($node - $baselineNode, $format)"/>
					<xsl:text> / </xsl:text>
					<xsl:value-of select="$percentageValue"/>
					<xsl:text>)</xsl:text>
				</xsl:attribute>
			</xsl:if>
		
			<xsl:attribute name="class">
				<xsl:text>value number </xsl:text>
				<xsl:value-of select="$colorClass"/> 
			</xsl:attribute>

			<xsl:if test="$showValue">
				<!-- we are not at the base value and will display percentages -->
				<xsl:if test="$position &gt; 1">
					<xsl:value-of select="$percentageValue"/>
				</xsl:if>	 
				
				<!-- this is the base value, display the absolute data instead -->
				<xsl:if test="$position = 1">
					<xsl:value-of select="format-number($node, $format)"/>
				</xsl:if>	 
			</xsl:if>
		</td>

	</xsl:template>

</xsl:stylesheet>