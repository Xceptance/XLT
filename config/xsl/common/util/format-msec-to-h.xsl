<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="format-msec-to-h">
<xsl:param name="n1"/>

<!-- put it down to seconds -->
<xsl:variable name="seconds" select="normalize-space($n1) div 1000"/>

<xsl:variable name="h" select="floor(floor($seconds) div 3600)"/>
<!--<xsl:message><xsl:value-of select="$h"/></xsl:message>-->

<xsl:variable name="min" select="floor(floor($seconds mod 3600) div 60)"/>
<!--<xsl:message><xsl:value-of select="$min"/></xsl:message>-->

<xsl:variable name="sec" select="floor(floor($seconds mod 3600) mod 60)"/>
<!--<xsl:message><xsl:value-of select="$sec"/></xsl:message>-->

<!-- format mm:ss --> 
<xsl:variable name="result"><xsl:value-of select="format-number($h, '00')"/>:<xsl:value-of select="format-number($min, '00')"/>:<xsl:value-of select="format-number($sec, '00')"/></xsl:variable>

<xsl:value-of select="$result"/>

</xsl:template>

<!--
<xsl:template match="/">
	<xsl:message>00:00:00 (0) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">0</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>00:00:01 (1) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">1000</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>01:01:01 (3661) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">3661000</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>12:12:30 (43950) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">43950000</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>04:32:12 (16332) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">16332000</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>00:00:17 (17000) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">17000</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>23:59:59 (86399) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">86399000</xsl:with-param></xsl:call-template></xsl:message>
	<xsl:message>46:59:59 (169199) = <xsl:call-template name="format-msec-to-h"><xsl:with-param name="n1">169199000</xsl:with-param></xsl:call-template></xsl:message>
</xsl:template>
-->
</xsl:stylesheet>


<!--
01:01:01 = ( 1 * 60 * 60) + ( 1 * 60) + 1  = 3661  

12:12:30 = (12 * 60 * 60) + (12 * 60) + 30 = 43950

04:32:12 = ( 4 * 60 * 60) + (32 * 60) + 12 = 16332

00:00:17 = ( 0 * 60 * 60) + ( 0 * 60) + 17 = 17

23:59:59 = 86399
-->