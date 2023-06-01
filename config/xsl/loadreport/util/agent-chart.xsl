<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="agent-chart">
        <xsl:param name="directory"/>
        <xsl:param name="isSummary"/>

        <!-- unique id -->
        <xsl:variable name="gid" select="generate-id(.)"/>

        <a>
            <xsl:attribute name="id"><xsl:value-of select="name"/></xsl:attribute>
            <xsl:comment>
                This is a placeholder for the anchor.
            </xsl:comment>
        </a>

        <div class="chart-group tabs c-tabs no-print" data-name="{name}">
            <xsl:attribute name="id">chart-<xsl:value-of select="$gid"/></xsl:attribute>
            <ul class="c-tabs-nav">
                <li class="c-tabs-nav-link c-is-active">
                    <a href="#CPU-{$gid}">CPU</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Memory-{$gid}">Memory</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Threads-{$gid}">Threads</a>
                </li>
            </ul>

            <xsl:choose>
                <xsl:when test="$isSummary = 'false'">
                    <a href="#tableEntry-{$gid}" class="backlink">Back to Table</a>
                </xsl:when>
            </xsl:choose>

            <div id="CPU-{$gid}" class="c-tab c-is-active">
                <div class="c-tab-content chart">
                    <img>
                        <xsl:attribute name="src">charts/agents/<xsl:value-of select="$directory"/>/CpuUsage.webp</xsl:attribute>
                        <xsl:attribute name="alt">charts/agents/<xsl:value-of select="$directory"/>/CpuUsage.webp</xsl:attribute>
                        <xsl:attribute name="loading">lazy</xsl:attribute>
                    </img>
                </div>
            </div>

            <div id="Memory-{$gid}" class="c-tab">
                <div class="c-tab-content chart">
                    <img>
                        <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                        <xsl:attribute name="alt">charts/agents/<xsl:value-of select="$directory"/>/MemoryUsage.webp</xsl:attribute>
                    </img>
                </div>
            </div>

            <div id="Threads-{$gid}" class="c-tab">
                <div class="c-tab-content chart">
                    <img>
                        <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                        <xsl:attribute name="alt">charts/agents/<xsl:value-of select="$directory"/>/Threads.webp</xsl:attribute>
                    </img>
                </div>
            </div>
        </div>

        <div class="chart-group print">
            <div class="chart">
                <h5>Memory</h5>
                <img>
                    <xsl:attribute name="alt">charts/agents/<xsl:value-of select="$directory"/>/MemoryUsage.webp</xsl:attribute>
                </img>
            </div>

            <div class="chart">
                <h5>CPU</h5>
                <img>
                    <xsl:attribute name="alt">charts/agents/<xsl:value-of select="$directory"/>/CpuUsage.webp</xsl:attribute>
                </img>

                <h5>Threads</h5>
                <img>
                    <xsl:attribute name="alt">charts/agents/<xsl:value-of select="$directory"/>/Threads.webp</xsl:attribute>
                </img>
            </div>
        </div>
    </xsl:template>

</xsl:stylesheet>
