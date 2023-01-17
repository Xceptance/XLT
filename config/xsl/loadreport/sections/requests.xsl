<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="requests">

        <div class="section" id="request-summary">
            <xsl:call-template name="headline-request-summary"/>

            <div id="progressmeter">
                <p>Loading...</p>
                <div class="loader"></div>
            </div>
            <div class="content hidden">
                <xsl:call-template name="description-request-summary"/>

                <xsl:call-template name="timer-section">
                    <xsl:with-param name="elements" select="requests/*"/>
                    <xsl:with-param name="summaryElement" select="summary/requests"/>
                    <xsl:with-param name="runtimeIntervalsNode" select="testReportConfig/runtimeIntervals"/>
                    <xsl:with-param name="tableRowHeader" select="'Request Name'"/>
                    <xsl:with-param name="directory" select="'requests'"/>
                    <xsl:with-param name="type" select="'request'"/>
                </xsl:call-template>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
