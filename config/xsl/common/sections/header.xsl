<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="header">
        <xsl:param name="title" select="'Performance Test Report'" />
        <xsl:param name="productName" select="/testreport/configuration/version/productName" />
        <xsl:param name="productVersion" select="/testreport/configuration/version/version" />
        <xsl:param name="productUrl" select="/testreport/configuration/version/productURL" />
        <xsl:param name="projectName" select="/testreport/configuration/projectName" />
        <xsl:param name="properties" select="/testreport/configuration/properties" />
        <xsl:param name="evaluationPresent" />
        <!-- XTC specific parameters -->
        <xsl:param name="xtcOrganization" select="$properties/property[@name='com.xceptance.xtc.organization']/@value" />
        <xsl:param name="xtcProject" select="$properties/property[@name='com.xceptance.xtc.project']/@value" />
        <xsl:param name="xtcLoadTestId" select="$properties/property[@name='com.xceptance.xtc.loadtest.run.id']/@value" />
        <xsl:param name="xtcResultId" select="$properties/property[@name='com.xceptance.xtc.loadtest.result.id']/@value" />
        <xsl:param name="xtcReportId" select="$properties/property[@name='com.xceptance.xtc.loadtest.report.id']/@value" />

        <xsl:variable name="normProjName" select="normalize-space($projectName)" />

        <xsl:call-template name="navigation">
            <xsl:with-param name="current" select="general" />
            <xsl:with-param name="evaluationPresent" select="$evaluationPresent" />
        </xsl:call-template>

        <header id="header">
            <div class="brand">
                <img src="images/xlt-logo.svg" class="logo" alt="The XLT tool logo" />
            </div>
            <div class="title">
                <h1>
                    <xsl:if test="string-length($normProjName) &gt; 0">
                        <span class="projectname"><xsl:value-of select="$normProjName" /></span>
                         &#8212;
                    </xsl:if>
                    <xsl:value-of select="$title" />
                </h1>
                <xsl:choose>
                <xsl:when test="$xtcOrganization">
                <div>
                    <div class="ltinfo">
                        <span class="key">Organization:</span>
                        <span class="value"><xsl:value-of select="$xtcOrganization" /></span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Project:</span>
                        <span class="value"><xsl:value-of select="$xtcProject" /></span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Load Test:</span>
                        <span class="value"><xsl:value-of select="$xtcLoadTestId" /></span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Result:</span>
                        <span class="value"><xsl:value-of select="$xtcResultId" /></span>
                    </div>
                    <div class="ltinfo">
                        <span class="key">Report:</span>
                        <span class="value"><xsl:value-of select="$xtcReportId" /></span>
                    </div>
                </div>
                </xsl:when>
                <xsl:otherwise>
                <h2 class="ltinfo hide-on-scroll">
                Created with
                    <a href="{$productUrl}?source=TestReport">
                        <span class="productname">
                            <xsl:value-of select="$productName" />
                        </span>
                        <span class="productversion">
                            <xsl:value-of select="$productVersion" />
                        </span>
                    </a>
                </h2>
                <div class="ltinfo show-on-scroll">
                    <xsl:if test="string-length($normProjName) &gt; 0">
                        <span class="value"><xsl:value-of select="$normProjName" /></span>
                        &#8212;
                    </xsl:if>
                    <xsl:value-of select="$title" />
                </div>
                </xsl:otherwise>
                </xsl:choose>
            </div>
        </header>

    </xsl:template>

</xsl:stylesheet>
