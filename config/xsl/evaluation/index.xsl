<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!--  Generate the page frame -->


<xsl:include href="../common/sections/head.xsl" />
<xsl:include href="../common/sections/header.xsl" />
<xsl:include href="../common/sections/footer.xsl" />
<xsl:include href="../common/sections/javascript.xsl" />

<xsl:include href="../loadreport/sections/navigation.xsl" />

<xsl:include href="text/descriptions.xsl" />


<xsl:include href="sections/util.xsl" />
<xsl:include href="sections/scorecard.xsl" />
<xsl:include href="sections/ratings.xsl" />
<xsl:include href="sections/rules.xsl" />
<xsl:include href="sections/groups.xsl" />

<xsl:param name="productName" />
<xsl:param name="productVersion" />
<xsl:param name="productUrl" />
<xsl:param name="projectName" />
<xsl:param name="evaluationPresent" />

<!-- XTC specific parameters -->
<xsl:param name="xtcOrganization" />
<xsl:param name="xtcProject" />
<xsl:param name="xtcLoadTestId" />
<xsl:param name="xtcResultId" />
<xsl:param name="xtcReportId" />

<xsl:template match="/evaluation">

<html lang="en">
<head>
    <xsl:call-template name="head">
        <xsl:with-param name="title" select="'XLT Report - Scorecard'"/>
        <xsl:with-param name="projectName" select="$projectName" />
    </xsl:call-template>

    <!-- Custom CSS for this page only -->
    <style type="text/css">
    /* No auto-stripe for table rows, use CSS class to derive color for row stripes. */
    table.no-auto-stripe tr.odd td {
        background-color: #f9f9fc;
    }
    table.no-auto-stripe tr.even td {
        background-color: #e8e8e8;
    }

    #scorecard-result .error > pre {
        border: 1px solid var(--main-color);
        padding: 16px;
    }
    #scorecard-ratings .inactive, #scorecard-rules .inactive {
        color: #5f5f5f;
    }
    </style>
</head>
<body id="loadtestreport">
<div id="container">
    <div id="content">

        <xsl:call-template name="header">
            <xsl:with-param name="productName" select="$productName" />
            <xsl:with-param name="productVersion" select="$productVersion" />
            <xsl:with-param name="productUrl" select="$productUrl" />
            <xsl:with-param name="projectName" select="$projectName" />
            <xsl:with-param name="evaluationPresent" select="$evaluationPresent" />
            <!-- XTC specific parameters -->
            <xsl:with-param name="xtcOrganization" select="$xtcOrganization" />
            <xsl:with-param name="xtcProject" select="$xtcProject" />
            <xsl:with-param name="xtcLoadTestId" select="$xtcLoadTestId" />
            <xsl:with-param name="xtcResultId" select="$xtcResultId" />
            <xsl:with-param name="xtcReportId" select="$xtcReportId" />
        </xsl:call-template>

        <div id="data-content">

            <xsl:call-template name="scorecard">
                <xsl:with-param name="rootNode" select="./outcome" />
            </xsl:call-template>

            <xsl:call-template name="ratings">
                <xsl:with-param name="elements" select="./configuration/ratings/rating" />
                <xsl:with-param name="active" select="./outcome/rating" />
            </xsl:call-template>

            <xsl:call-template name="groups">
                <xsl:with-param name="definitions" select="./configuration/groups/group" />
                <xsl:with-param name="results" select="./outcome/groups/group" />
            </xsl:call-template>

            <xsl:call-template name="rules">
                <xsl:with-param name="definitions" select="./configuration/rules/rule" />
                <xsl:with-param name="results" select="./outcome/groups/group/rules" />
            </xsl:call-template>

        </div> <!-- /data-content -->

        <xsl:call-template name="footer">
            <xsl:with-param name="productName" select="$productName" />
            <xsl:with-param name="productVersion" select="$productVersion" />
            <xsl:with-param name="productUrl" select="$productUrl" />
        </xsl:call-template>
    </div> <!-- /content -->
</div> <!-- /container -->

<xsl:call-template name="javascript" />

</body>
</html>

</xsl:template>

</xsl:stylesheet>
