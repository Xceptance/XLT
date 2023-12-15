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

<xsl:include href="../common/util/string-replace-all.xsl" />
<xsl:include href="../common/util/convertIllegalCharactersInFileName.xsl" />
<xsl:include href="../common/util/create-totals-td.xsl" />
<xsl:include href="../common/util/filtered-footer-row.xsl" />

<xsl:include href="util/timer-cell.xsl" />
<xsl:include href="util/timer-row-abs.xsl" />
<xsl:include href="util/timer-row-rel.xsl" />
<xsl:include href="util/timer-chart.xsl" />
<xsl:include href="util/timer-table.xsl" />
<xsl:include href="util/timer-section.xsl" />

<xsl:include href="text/descriptions.xsl" />

<xsl:include href="sections/transactions.xsl" />

<xsl:include href="../common/sections/head.xsl" />
<xsl:include href="../common/sections/header.xsl" />
<xsl:include href="sections/navigation.xsl" />
<xsl:include href="../common/sections/footer.xsl" />

<xsl:include href="../common/sections/javascript.xsl" />

<xsl:param name="productName" />
<xsl:param name="productVersion" />
<xsl:param name="productUrl" />
<xsl:param name="projectName" />

<xsl:template match="trendreport">

<xsl:text disable-output-escaping="yes">&lt;!</xsl:text><xsl:text>DOCTYPE html</xsl:text><xsl:text disable-output-escaping="yes">&gt;&#13;</xsl:text>
<html lang="en">
<head>
    <xsl:call-template name="head">
        <xsl:with-param name="title" select="'XLT Performance Trend Report - Overview'" />
        <xsl:with-param name="projectName" select="$projectName" />
    </xsl:call-template>
</head>
<body id="trendreport">
<div id="container">
    <div id="content">
        <xsl:call-template name="header">
            <xsl:with-param name="title" select="'Performance Trend Report'" />
            <xsl:with-param name="productName" select="$productName" />
            <xsl:with-param name="productVersion" select="$productVersion" />
            <xsl:with-param name="productUrl" select="$productUrl" />
            <xsl:with-param name="projectName" select="$projectName" />
        </xsl:call-template>

        <div id="data-content">

            <!--
                ************************************
                * General section
                ************************************
            -->
            <div class="section" id="general">
                <xsl:call-template name="headline-general" />

                <div class="content">
                    <xsl:call-template name="description-general" />
                </div>
            </div>


        </div> <!-- data-content -->

        <xsl:call-template name="footer">
            <xsl:with-param name="productName" select="$productName" />
            <xsl:with-param name="productVersion" select="$productVersion" />
            <xsl:with-param name="productUrl" select="$productUrl" />
    	</xsl:call-template>
    </div> <!-- data-content -->
</div> <!-- end container -->    

<xsl:call-template name="javascript" />

</body>
</html>

</xsl:template>

</xsl:stylesheet>
