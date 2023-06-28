<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="agents-summary">
        <xsl:param name="rootNode"/>

        <div class="section" id="agents">
            <xsl:call-template name="headline-agents"/>

            <div class="content">
                <xsl:call-template name="description-agents"/>

                <div class="charts">
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/agents/All%20Agents/CpuUsage.webp</xsl:attribute>
                            <xsl:attribute name="alt">"charts/agents/All Agents/CpuUsage.webp"</xsl:attribute>
                        </img>
                    </div>
                </div>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
