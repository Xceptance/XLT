<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="timer-chart">
        <xsl:param name="directory"/>

        <xsl:variable name="encodedName">
            <xsl:call-template name="convertIllegalCharactersInFileName">
                <xsl:with-param name="filename" select="normalize-space(name)"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="gid" select="generate-id(.)"/>

        <a>
            <xsl:attribute name="id">timerchart-<xsl:value-of select="$gid"/></xsl:attribute>
            <xsl:comment>
                This is a placeholder for the anchor.
            </xsl:comment>
        </a>

        <div id="{$gid}" class="chart-group tabs c-tabs no-print" data-name="{name}">
            <ul class="c-tabs-nav">
                <li class="c-tabs-nav-link c-is-active">
                    <a href="#RunTime-{$gid}">Run Times</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Errors-{$gid}">Errors</a>
                </li>
                <li class="c-tabs-nav-link">
                    <a href="#Throughput-{$gid}">Throughput</a>
                </li>
            </ul>

            <div id="RunTime-{$gid}" class="c-tab c-is-active">
                <div class="c-tab-content chart">
                    <xsl:call-template name="create_chart_img_tag">
                        <xsl:with-param name="directory" select="$directory"/>
                        <xsl:with-param name="name" select="$encodedName"/>
                        <xsl:with-param name="suffix" select="'_RunTime.png'"/>
                    </xsl:call-template>
                </div>
            </div>

            <div id="Errors-{$gid}" class="c-tab">
                <div class="c-tab-content chart">
                    <xsl:call-template name="create_chart_img_tag">
                        <xsl:with-param name="directory" select="$directory"/>
                        <xsl:with-param name="name" select="$encodedName"/>
                        <xsl:with-param name="suffix" select="'_Errors.png'"/>
                    </xsl:call-template>
                </div>
            </div>

            <div id="Throughput-{$gid}" class="c-tab">
                <div class="c-tab-content chart">
                    <xsl:call-template name="create_chart_img_tag">
                        <xsl:with-param name="directory" select="$directory"/>
                        <xsl:with-param name="name" select="$encodedName"/>
                        <xsl:with-param name="suffix" select="'_Throughput.png'"/>
                    </xsl:call-template>
                </div>
            </div>

        </div>

        <div class="chart-group print">
            <h3>
                <xsl:value-of select="name"/>
            </h3>
            <div class="chart">
                <h5>Runtimes</h5>
                <img alt="charts/{$directory}/{$encodedName}_RunTime.png"/>
            </div>

            <div class="chart">
                <h5>Errors</h5>
                <img alt="charts/{$directory}/{$encodedName}_Errors.png"/>
            </div>

            <div class="chart">
                <h5>Throughput</h5>
                <img alt="charts/{$directory}/{$encodedName}_Throughput.png"/>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="create_chart_img_tag">
        <xsl:param name="directory"/>
        <xsl:param name="name"/>
        <xsl:param name="suffix"/>

        <img alt="{concat('charts/',$directory,'/',$name,$suffix)}" src="charts/placeholder.png"/>

    </xsl:template>

</xsl:stylesheet>