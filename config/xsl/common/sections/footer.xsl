<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="footer">
	<xsl:param name="productUrl" select="/testreport/configuration/version/productURL" />
    <xsl:param name="productName" select="/testreport/configuration/version/productName" />
    <xsl:param name="productVersion" select="/testreport/configuration/version/version" />
    <div id="footer">
        Copyright (c) 2005-2023 <a href="http://www.xceptance.com/" title="Visit the Xceptance website">Xceptance Software Technologies GmbH</a><br />
        Created with
                <a href="{$productUrl}?piwik_campaign=TestReport">
                    <span class="productname">
                        <xsl:value-of select="$productName" />
                    </span>
                    <span class="productversion">
                        <xsl:value-of select="$productVersion" />
                    </span>
                </a>
    </div>
</xsl:template>

</xsl:stylesheet>
