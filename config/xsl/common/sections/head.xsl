<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="head">

        <xsl:param name="projectName" />
        <xsl:param name="title" />

        <meta charset="utf-8" />
        <!-- Min width set because we cannot handle our data size on a smartphone display -->
        <meta name="viewport" content="width=768, initial-scale=1" />
        <meta name="generator" content="XLT" />

        <xsl:variable name="normProjName" select="normalize-space($projectName)"/>

        <title>
            <xsl:if test="string-length($normProjName) &gt; 0">
                <xsl:choose>
                    <xsl:when test="string-length($normProjName) &lt; 32">
                        <xsl:value-of select="$normProjName" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat(substring($normProjName, 0, 29),'...')" />
                    </xsl:otherwise>
                </xsl:choose>
                |
            </xsl:if>
            <xsl:value-of select="$title" />
        </title>

        <link href="css/default.css" type="text/css" rel="stylesheet"/>
        <link href="css/print.css" type="text/css" rel="stylesheet" media="print"/>

        <script src="js/jquery-3.6.4.min.js"/>
        <script src="js/jquery.hoverIntent-1.10.2.min.js"/>
        <script src="js/jquery.scrollTo-2.1.3.min.js"/>
        <script src="js/tabs.js"/>
        <script src="js/table.js"/>

        <link rel="icon" href="images/favicon.png" sizes="any" />
        <link rel="icon" href="images/favicon.svg" type="image/svg+xml" />

    </xsl:template>

</xsl:stylesheet>
