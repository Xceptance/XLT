<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html" 
            indent="yes" 
            omit-xml-declaration="yes"
            encoding="UTF-8"
             />

<!-- 
    Generate the page frame 
-->

<xsl:include href="../common/util/percentage.xsl" />
<xsl:include href="../common/util/format-msec-to-h.xsl" />
<xsl:include href="../common/util/create-totals-td.xsl" />
<xsl:include href="../common/util/filtered-footer-row.xsl" />
<xsl:include href="../common/util/load-profile-table.xsl" />

<xsl:include href="util/timer-row.xsl" />
<xsl:include href="util/timer-section.xsl" />
<xsl:include href="util/timer-cell.xsl" />

<xsl:include href="text/descriptions.xsl" />

<xsl:include href="sections/custom-timers.xsl" />

<xsl:include href="../common/sections/head.xsl" />
<xsl:include href="../common/sections/header.xsl" />
<xsl:include href="sections/navigation.xsl" />
<xsl:include href="../common/sections/footer.xsl" />

<xsl:include href="../common/sections/javascript.xsl" />

<xsl:param name="productName" />
<xsl:param name="productVersion" />
<xsl:param name="productUrl" />
<xsl:param name="projectName" />

<xsl:template match="testreport">

<xsl:text disable-output-escaping="yes">&lt;!</xsl:text><xsl:text>DOCTYPE html</xsl:text><xsl:text disable-output-escaping="yes">&gt;&#13;</xsl:text>
<html lang="en">
<head>
    <xsl:call-template name="head">
        <xsl:with-param name="title" select="'XLT Performance Comparison Report - Custom Timers'"/>
        <xsl:with-param name="projectName" select="$projectName" />
    </xsl:call-template>
</head>
<body id="diffreport">
<div id="container">
    <div id="content">
        <xsl:call-template name="header">
            <xsl:with-param name="title" select="'Performance Comparison Report'" />
            <xsl:with-param name="productName" select="$productName" />
            <xsl:with-param name="productVersion" select="$productVersion" />
            <xsl:with-param name="productUrl" select="$productUrl" />
            <xsl:with-param name="projectName" select="$projectName" />
        </xsl:call-template>

        <div id="data-content">

            <!--
                ************************************
                * Custom Timers
                ************************************
            -->
            <xsl:call-template name="custom-timers"/>

        </div> <!-- data-content -->

        <xsl:call-template name="footer" />
    </div> <!-- data-content -->
</div> <!-- end container -->

<xsl:call-template name="javascript" />

</body>
</html>

</xsl:template>

</xsl:stylesheet>
