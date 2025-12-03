<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="timer-chart">
        <xsl:param name="directory"/>
        <xsl:param name="type"/>

        <xsl:variable name="encodedName">
            <xsl:call-template name="convertIllegalCharactersInFileName">
                <xsl:with-param name="filename" select="name"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="gid" select="generate-id(.)"/>

        <xsl:variable name="dynamicChartsEnabled">
            <xsl:value-of select="/testreport/configuration/reportGeneratorConfiguration/dynamicChartsEnabled"/>
        </xsl:variable>

        <xsl:variable name="timeZoneLabel">
            <xsl:value-of select="/testreport/general/timeZoneLabel"/>
        </xsl:variable>

        <xsl:variable name="timeZoneOffset">
            <xsl:value-of select="/testreport/general/timeZoneOffset"/>
        </xsl:variable>

        <a>
            <xsl:attribute name="id"><xsl:value-of select="name"/></xsl:attribute>
            <xsl:comment>
                This is a placeholder for the anchor.
            </xsl:comment>
        </a>

        <div class="chart-group tabs c-tabs no-print" data-name="{name}">
            <xsl:attribute name="id">chart-<xsl:value-of select="$gid"/></xsl:attribute>
            <ul class="c-tabs-nav">
                <li class="c-tabs-nav-link img-tab c-is-active">
                    <a href="#Overview-{$gid}">Overview</a>
                </li>
                <xsl:if test="$dynamicChartsEnabled = 'true'">
                    <li class="c-tabs-nav-link echart-tab">
                        <a href="#DynamicOverview-{$gid}">Dynamic Overview</a>
                    </li>
                </xsl:if>
                <li class="c-tabs-nav-link img-tab">
                    <a href="#Averages-{$gid}">Averages</a>
                </li>
                <li class="c-tabs-nav-link img-tab">
                    <a href="#Count-{$gid}">Count/s</a>
                </li>

                <xsl:if test="$type = 'transaction'">
                    <li class="c-tabs-nav-link img-tab">
                        <a href="#ArrivalRate-{$gid}">Arrival Rate</a>
                    </li>
                    <li class="c-tabs-nav-link img-tab">
                        <a href="#ConcurrentUsers-{$gid}">Concurrent Users</a>
                    </li>
                </xsl:if>

                <xsl:if test="$type = 'request'">
                    <li class="c-tabs-nav-link img-tab">
                        <a href="#Distribution-{$gid}">Distribution</a>
                    </li>
                    <li class="c-tabs-nav-link img-tab">
                        <a href="#Network-{$gid}">Response Size</a>
                    </li>
                </xsl:if>
            </ul>

            <xsl:if test="count(parent::summary)=0">
                <a href="#tableEntry-{$gid}" class="backlink">Back to Table</a>
            </xsl:if>
            <div id="Overview-{$gid}" class="c-tab c-is-active img-tab overview">
                <div class="c-tab-content chart">
                    <img>
                        <xsl:attribute name="src">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                            select="$encodedName"/>.webp</xsl:attribute>
                        <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                            select="$encodedName"/>.webp</xsl:attribute>
                        <xsl:attribute name="loading">lazy</xsl:attribute>
                    </img>
                </div>
            </div>

            <xsl:if test="$dynamicChartsEnabled = 'true'">
                <div id="DynamicOverview-{$gid}" class="c-tab echart-tab overview">
                    <div class="c-tab-content echart">
                        <xsl:attribute name="src">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                            select="$encodedName"/>.json</xsl:attribute>
                        <xsl:attribute name="name"><xsl:value-of select="name"/></xsl:attribute>
                        <xsl:attribute name="data-timezone-label">
                            <xsl:value-of select="$timeZoneLabel"/>
                        </xsl:attribute>
                        <xsl:attribute name="data-timezone-offset">
                            <xsl:value-of select="$timeZoneOffset"/>
                        </xsl:attribute>
                    </div>
                </div>
            </xsl:if>

            <div id="Averages-{$gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img>
                        <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                        <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                            select="$encodedName"/>_Average.webp</xsl:attribute>
                    </img>
                </div>
            </div>

            <div id="Count-{$gid}" class="c-tab img-tab">
                <div class="c-tab-content chart">
                    <img>
                        <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                        <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                            select="$encodedName"/>_CountPerSecond.webp</xsl:attribute>
                    </img>
                </div>
            </div>

            <xsl:if test="$type = 'transaction'">
                <div id="ArrivalRate-{$gid}" class="c-tab img-tab">
                    <div class="c-tab-content chart">
                        <img>
                            <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                            <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                                select="$encodedName"/>_ArrivalRate.webp</xsl:attribute>
                        </img>
                    </div>
                </div>
                <div id="ConcurrentUsers-{$gid}" class="c-tab img-tab">
                    <div class="c-tab-content chart">
                        <img>
                            <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                            <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                                select="$encodedName"/>_ConcurrentUsers.webp</xsl:attribute>
                        </img>
                    </div>
                </div>
            </xsl:if>

            <xsl:if test="$type = 'request'">
                <div id="Distribution-{$gid}" class="c-tab img-tab">
                    <div class="c-tab-content chart">
                        <img>
                            <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                            <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                                select="$encodedName"/>_Histogram.webp</xsl:attribute>
                        </img>
                    </div>
                </div>

                <div id="Network-{$gid}" class="c-tab img-tab">
                    <div class="c-tab-content chart">
                        <img>
                            <xsl:attribute name="src">charts/placeholder.webp</xsl:attribute>
                            <xsl:attribute name="alt">charts/<xsl:value-of select="$directory"/>/<xsl:value-of
                                select="$encodedName"/>_ResponseSize.webp</xsl:attribute>
                        </img>
                    </div>
                </div>
            </xsl:if>
        </div>

        <div class="chart-group print">
            <h3>
                <xsl:value-of select="name"/>
            </h3>
            <xsl:choose>
                <xsl:when test="$type = 'transaction'">
                    <div class="chart">
                        <h5>Overview</h5>
                        <img alt="charts/{$directory}/{$encodedName}.webp"/>

                        <h5>Averages</h5>
                        <img alt="charts/{$directory}/{$encodedName}_Average.webp"/>
                    </div>

                    <div class="chart">
                        <h5>Count/s</h5>
                        <img alt="charts/{$directory}/{$encodedName}_CountPerSecond.webp"/>

                        <h5>Arrival Rate</h5>
                        <img alt="charts/{$directory}/{$encodedName}_ArrivalRate.webp"/>

                        <h5>Concurrent Users</h5>
                        <img alt="charts/{$directory}/{$encodedName}_ConcurrentUsers.webp"/>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <div class="chart">
                        <h5>Overview</h5>
                        <img alt="charts/{$directory}/{$encodedName}.webp"/>
                    </div>
                    <div class="chart">
                        <h5>Count/s</h5>
                        <img alt="charts/{$directory}/{$encodedName}_CountPerSecond.webp"/>
                    </div>
                    <div class="chart">
                        <h5>Averages</h5>
                        <img alt="charts/{$directory}/{$encodedName}_Average.webp"/>
                    </div>
                    <xsl:if test="$type = 'request'">
                        <div class="chart">
                            <h5>Response Size</h5>
                            <img alt="charts/{$directory}/{$encodedName}_ResponseSize.webp"/>
                        </div>
                        <div class="chart">
                            <h5>Distribution</h5>
                            <img alt="charts/{$directory}/{$encodedName}_Histogram.webp"/>
                        </div>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>

</xsl:stylesheet>
