<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="header">
        <xsl:param name="title" select="'Performance Test Report'" />
        <xsl:param name="productName" select="/testreport/configuration/version/productName" />
        <xsl:param name="productVersion" select="/testreport/configuration/version/version" />
        <xsl:param name="productUrl" select="/testreport/configuration/version/productURL" />
        <xsl:param name="projectName" select="/testreport/configuration/projectName" />

        <xsl:variable name="normProjName" select="normalize-space($projectName)" />

        <xsl:call-template name="navigation">
            <xsl:with-param name="current" select="general" />
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
                <h2>Created with
                    <a href="{$productUrl}?source=TestReport">
                        <span class="productname">
                            <xsl:value-of select="$productName" />
                        </span>
                        <span class="productversion">
                            <xsl:value-of select="$productVersion" />
                        </span>
                    </a></h2>
            </div>
        </header>

    </xsl:template>

</xsl:stylesheet>
