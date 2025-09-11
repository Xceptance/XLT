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
<xsl:include href="../common/util/convertIllegalCharactersInFileName.xsl" />
<xsl:include href="../common/util/string-replace-all.xsl" />
<xsl:include href="../common/util/percentage.xsl" />
<xsl:include href="../common/util/format-bytes.xsl" />
<xsl:include href="../common/util/format-msec-to-h.xsl" />
<xsl:include href="../common/util/create-totals-td.xsl" />
<xsl:include href="../common/util/filtered-footer-row.xsl" />
<xsl:include href="../common/util/load-profile-table.xsl" />

<xsl:include href="text/descriptions.xsl" />

<xsl:include href="sections/load-profile.xsl" />
<xsl:include href="sections/configuration.xsl" />

<xsl:include href="../common/sections/head.xsl" />
<xsl:include href="../common/sections/header.xsl" />
<xsl:include href="sections/navigation.xsl" />
<xsl:include href="../common/sections/footer.xsl" />

<xsl:include href="../common/sections/javascript.xsl" />

<xsl:param name="productName" />
<xsl:param name="productVersion" />
<xsl:param name="productUrl" />
<xsl:param name="projectName" />
<xsl:param name="scorecardPresent" />

<xsl:template match="/testreport">

<html lang="en">
<head>
    <xsl:call-template name="head">
        <xsl:with-param name="title" select="'XLT Report - Configuration'" />
        <xsl:with-param name="projectName" select="configuration/projectName" />
    </xsl:call-template>
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <xsl:call-template name="header">
            <xsl:with-param name="scorecardPresent" select="$scorecardPresent" />
        </xsl:call-template>

        <div id="data-content">

        	<!--
        		************************************
        		* Load Profile
        		************************************
        	-->
			<xsl:call-template name="load-profile">
				<xsl:with-param name="rootNode" select="configuration" />
			</xsl:call-template>

        	<!--
        		************************************
        		* Configuration
        		************************************
        	-->
			<xsl:call-template name="configuration">
				<xsl:with-param name="rootNode" select="configuration" />
			</xsl:call-template>

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
