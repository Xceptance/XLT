<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
>

    <!-- Document Settings -->
    <xsl:output method="html" indent="yes" encoding="US-ASCII" />
    <xsl:decimal-format decimal-separator="."
        grouping-separator="," />
    <xsl:strip-space elements="*" />

    <!-- Parameter for path stripping. -->
    <xsl:param name="dir.prefix" select="''"/>
    <xsl:param name="file.separator" select="'/'" />

    <!-- Use global variable to adjust parameter (must end with an slash). -->
    <xsl:variable name="prefix">
        <xsl:choose>
            <xsl:when
                test="substring($dir.prefix,string-length($dir.prefix)-1,1) = $file.separator"
            >
                <xsl:value-of select="$dir.prefix" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($dir.prefix, $file.separator)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- Transformation entry point. Generates the head and the footer. -->
    <xsl:template match="jslint">
        <html>
            <head>
                <title>JSLint Report</title>
            </head>
            <body>
                <center>
                    <h3>JSLint Report</h3>
                </center>
                <center>
                    <h3>Issues found</h3>
                </center>
                <table align="center" cellspacing="0" cellpadding="3">
                    <tr bgcolor="#00ea00">
                        <th>File</th>
                        <th>Line</th>
                        <th>Evidence</th>
                        <th>Reason</th>
                    </tr>
                    <xsl:apply-templates />
                </table>
            </body>
        </html>
    </xsl:template>

    <!-- Generates the issue entries. -->
    <xsl:template match="issue">
        <tr>
            <xsl:call-template name="alternate-row">
                <xsl:with-param name="position">
                    <xsl:number level="any" from="jslint" />
                </xsl:with-param>
            </xsl:call-template>
            <td width="*%">
                <xsl:call-template name="strip">
                    <xsl:with-param name="raw">
                        <xsl:value-of select="../@name" />
                    </xsl:with-param>
                </xsl:call-template>
            </td>
            <td align="center" width="5%">
                <xsl:value-of select="@line" />
            </td>
            <td width="*%">
                <code style="color:red">
                    <xsl:value-of select="@evidence" />
                </code>
            </td>
            <td width="*%">
                <xsl:value-of select="@reason" />
            </td>
        </tr>
    </xsl:template>

    <!-- Alternating background colors for each issue entry. -->
    <xsl:template name="alternate-row">
        <xsl:param name="position" />
        <xsl:attribute name="bgcolor">
    		<xsl:choose>
	           	<xsl:when test="$position mod 2 = 0">lightgrey</xsl:when>
		        <xsl:otherwise>white</xsl:otherwise>
		      </xsl:choose>
        </xsl:attribute>
    </xsl:template>

    <!-- Strip given parameter. -->
    <xsl:template name="strip">
        <xsl:param name="raw" />
        <xsl:value-of select="substring-after($raw, $prefix)" />
    </xsl:template>

</xsl:stylesheet>