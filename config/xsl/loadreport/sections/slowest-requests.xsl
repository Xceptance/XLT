<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="slowest-requests">

        <div class="section" id="slowest-requests-summary">
            <xsl:call-template name="headline-slowest-requests-summary"/>

            <div id="progressmeter">
                <p>Loading...</p>
                <div class="loader"></div>
            </div>
            <div class="content hidden">
                <xsl:call-template name="description-slowest-requests-summary"/>

                <xsl:call-template name="slowest-requests-table">
                    <xsl:with-param name="slowestRequests" select="slowestRequests/*"/>
                    <xsl:with-param name="requests" select="requests/*"/>
                </xsl:call-template>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
