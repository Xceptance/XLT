<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="head">

        <xsl:param name="projectName" />
        <xsl:param name="title" />

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
        <link href="css/superfish.custom.css" type="text/css" rel="stylesheet"/>

        <script src="js/jquery-3.6.1.min.js" type="text/javascript">/* Placeholder */</script>
        <script src="js/jquery.hoverIntent-1.8.0.min.js" type="text/javascript">/* Placeholder */</script>
        <script src="js/jquery.scrollTo-2.1.3.min.js" type="text/javascript">/* Placeholder */</script>
        <script src="js/jquery.superfish-1.7.10.min.js" type="text/javascript">/* Placeholder */</script>
        <script src="js/tabs.js" type="text/javascript">/* Placeholder */</script>
        <script type="text/javascript" src="js/table.js">/* Placeholder */</script>

    </xsl:template>

</xsl:stylesheet>
