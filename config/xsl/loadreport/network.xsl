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

<xsl:include href="text/descriptions.xsl" />

<xsl:include href="sections/network.xsl" />
<xsl:include href="sections/hosts.xsl" />
<xsl:include href="sections/ips.xsl" />
<xsl:include href="sections/response-codes.xsl" />
<xsl:include href="sections/request-methods.xsl" />
<xsl:include href="sections/content-types.xsl" />

<xsl:include href="util/network-table.xsl" />

<xsl:include href="../common/sections/head.xsl" />
<xsl:include href="../common/sections/header.xsl" />
<xsl:include href="sections/navigation.xsl" />
<xsl:include href="../common/sections/footer.xsl" />

<xsl:include href="../common/sections/javascript.xsl" />

<xsl:template match="/testreport">

<xsl:text disable-output-escaping="yes">&lt;!</xsl:text><xsl:text>DOCTYPE html</xsl:text><xsl:text disable-output-escaping="yes">&gt;&#13;</xsl:text>
<html lang="en">    
<head>
    <xsl:call-template name="head">
        <xsl:with-param name="title" select="'XLT Report - Network'" />
        <xsl:with-param name="projectName" select="configuration/projectName" />
    </xsl:call-template>
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">
        <xsl:call-template name="header" />

        <div id="data-content">
        	<!--
        		************************************
        		* Network
        		************************************
        	-->
			<xsl:call-template name="network">
				<xsl:with-param name="rootNode" select="general" />
			</xsl:call-template>

            <!--
                ************************************
                * Hosts
                ************************************
            -->
            <xsl:call-template name="hosts">
                <xsl:with-param name="rootNode" select="hosts" />
                <xsl:with-param name="totalHits" select="general/hits" />
            </xsl:call-template>
            
            <!--
                ************************************
                * IPs
                ************************************
            -->
            <xsl:call-template name="ips">
                <xsl:with-param name="rootNode" select="ips" />
                <xsl:with-param name="totalHits" select="general/hits" />
            </xsl:call-template>

            <!--
                ************************************
                * HTTP Request Methods
                ************************************
            -->
            <xsl:call-template name="request-methods">
                <xsl:with-param name="rootNode" select="requestMethods" />
                <xsl:with-param name="totalHits" select="general/hits" />
            </xsl:call-template>
            
            <!--
                ************************************
                * Response Codes
                ************************************
            -->
            <xsl:call-template name="response-codes">
                <xsl:with-param name="rootNode" select="responseCodes" />
                <xsl:with-param name="totalHits" select="general/hits" />
            </xsl:call-template>

            <!--
                ************************************
                * Content Types
                ************************************
            -->
            <xsl:call-template name="content-types">
                <xsl:with-param name="rootNode" select="contentTypes" />
                <xsl:with-param name="totalHits" select="general/hits" />
            </xsl:call-template>

        </div> <!-- data-content -->

        <xsl:call-template name="footer" />
    </div> <!-- data-content -->
</div> <!-- end container -->    

<xsl:call-template name="javascript" />

</body>
</html>

</xsl:template>

</xsl:stylesheet>
