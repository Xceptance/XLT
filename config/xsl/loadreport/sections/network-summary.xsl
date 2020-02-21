<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="network-summary">
        <xsl:param name="rootNode"/>

        <div class="section" id="network">
            <xsl:call-template name="headline-network-summary"/>

            <div class="content">
                <xsl:call-template name="description-network-summary"/>

                <div class="data">
                    <xsl:call-template name="network-table">
                        <xsl:with-param name="rootNode" select="$rootNode"/>
                    </xsl:call-template>
                </div>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>