<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="network">
        <xsl:param name="rootNode"/>

        <div class="section" id="network">
            <xsl:call-template name="headline-network"/>

            <div class="content">
                <xsl:call-template name="description-network"/>

                <div class="data">
                    <xsl:call-template name="network-table">
                        <xsl:with-param name="rootNode" select="$rootNode"/>
                    </xsl:call-template>
                </div>

                <div class="charts">
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/RequestsPerSecond.png</xsl:attribute>
                            <xsl:attribute name="alt">Requests</xsl:attribute>
                        </img>
                    </div>
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/SentBytesPerSecond.png</xsl:attribute>
                            <xsl:attribute name="alt">Sent Bytes</xsl:attribute>
                        </img>
                    </div>
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/ReceivedBytesPerSecond.png</xsl:attribute>
                            <xsl:attribute name="alt">Received Bytes</xsl:attribute>
                        </img>
                    </div>
                </div>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>