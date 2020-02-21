<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="actions">

        <div class="section" id="action-summary">
            <xsl:call-template name="headline-action-summary"/>

            <div class="content">
                <xsl:call-template name="description-action-summary"/>

                <xsl:call-template name="timer-section">
                    <xsl:with-param name="elements" select="actions/*"/>
                    <xsl:with-param name="summaryElement" select="summary/actions"/>
                    <xsl:with-param name="tableRowHeader">Action Name</xsl:with-param>
                    <xsl:with-param name="type">action</xsl:with-param>
                </xsl:call-template>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>