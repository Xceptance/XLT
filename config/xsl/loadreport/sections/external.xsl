<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="external">

        <xsl:choose>
            <xsl:when test="(count(genericReport) &gt; 0)">
                <xsl:for-each select="genericReport">
                    <xsl:call-template name="external-subsection"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <div class="section" id="external-summary">
                    <xsl:call-template name="headline-external"/>
                    <xsl:call-template name="description-external"/>
                </div>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template name="external-subsection">

        <div class="section">
            <h2>
                <xsl:value-of select="headline"/>
            </h2>
            <div class="content">
                <xsl:call-template name="external-data-section-description">
                    <xsl:with-param name="description" select="description"/>
                </xsl:call-template>

                <xsl:for-each select="tables/table">
                    <h3>
                        <xsl:value-of select="title"/>
                    </h3>

                    <div class="data">
                        <table class="table-autostripe table-stripeclass:odd">
                            <xsl:if test="count(headRow) &gt; 0">
                                <thead>
                                    <tr>
                                        <xsl:for-each select="headRow/cells/*">
                                            <th>
                                                <xsl:value-of select="."/>
                                            </th>
                                        </xsl:for-each>
                                    </tr>
                                </thead>
                            </xsl:if>

                            <tbody>
                                <xsl:for-each select="bodyRows/row">
                                    <tr>
                                        <xsl:for-each select="cells/*">

                                            <td>
                                                <!-- int, double, long, float, big-decimal will be right-aligned and formatted, others 
                                                    left-aligned -->
                                                <xsl:choose>
                                                    <xsl:when
                                                        test="local-name()='int' or local-name()='double' or local-name()='long' or local-name()='float' or local-name()='big-decimal'">
                                                        <xsl:attribute name="class">value number count</xsl:attribute>
                                                        <xsl:value-of select="format-number(., '#,##0.###')"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:attribute name="class">key</xsl:attribute>
                                                        <xsl:value-of select="."/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </td>
                                        </xsl:for-each>
                                    </tr>
                                </xsl:for-each>
                            </tbody>
                        </table>
                    </div><!-- end data -->
                </xsl:for-each>

                <xsl:if test="count(chartFileNames) &gt; 0">
                    <div class="charts">
                        <xsl:for-each select="chartFileNames/string">

                            <xsl:variable name="encodedChartFilename">
                                <xsl:call-template name="convertIllegalCharactersInFileName">
                                    <xsl:with-param name="filename" select="."/>
                                </xsl:call-template>
                            </xsl:variable>

                            <div class="chart">
                                <img>
                                    <xsl:attribute name="src">charts/external/<xsl:value-of select="$encodedChartFilename"/>.png</xsl:attribute>
                                    <xsl:attribute name="alt">Hits</xsl:attribute>
                                </img>
                            </div><!-- end chart -->
                        </xsl:for-each>
                    </div><!-- end charts -->
                </xsl:if>
            </div><!-- end content -->
        </div><!-- end section -->

    </xsl:template>

</xsl:stylesheet>