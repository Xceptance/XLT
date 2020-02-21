<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="timer-table">
        <xsl:param name="elements"/>
        <xsl:param name="summaryElement"/>
        <xsl:param name="tableRowHeader"/>
        <xsl:param name="valueName"/>
        <xsl:param name="mode"/>

        <xsl:variable name="valuesCount" select="count($elements[1]/trendValues/trendValue)"/>
        <xsl:variable name="showValues" select="$valuesCount &lt; 16"/>

        <xsl:variable name="format">
            <xsl:if test="$valueName = 'errors'">
                <xsl:text>#</xsl:text>
            </xsl:if>
            <xsl:if test="$valueName != 'errors'">
                <xsl:text>#,##0 ms</xsl:text>
            </xsl:if>
        </xsl:variable>

        <table class="c-tab-content table-autosort:0 table-autostripe table-stripeclass:odd">
            <thead>
                <tr>
                    <th class="table-sortable:alphanumeric" rowspan="2">
                        <xsl:value-of select="$tableRowHeader"/>
                        <br/>
                        <input class="filter" placeholder="Enter filter substrings"/>
                    </th>

                    <!-- write the data for the first report as base column -->
                    <xsl:for-each select="$elements[1]/trendValues/trendValue">
                        <xsl:if test="position() = 1">
                            <th rowspan="2" class="cluetip">
                                <xsl:attribute name="data-rel">#ReportInfo-<xsl:value-of select="generate-id(.)"/></xsl:attribute>
                                <xsl:if test="$showValues">
                                    <xsl:attribute name="class">
										<xsl:text>table-sortable:numeric cluetip</xsl:text>
									</xsl:attribute>
                                    <xsl:choose>
                                        <xsl:when test="$mode = 'absolute'">
                                            <xsl:call-template name="table-headline-absolute-base"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:call-template name="table-headline-relative-base"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                            </th>
                        </xsl:if>
                    </xsl:for-each>

                    <th colspan="{$valuesCount - 1}">
                        <xsl:choose>
                            <xsl:when test="$mode = 'absolute'">
                                <xsl:call-template name="table-headline-absolute"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="table-headline-relative"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </th>
                </tr>

                <xsl:if test="count($elements) > 0">
                    <tr>
                        <xsl:for-each select="$elements[1]/trendValues/trendValue">
                            <!-- the first one has its own column as base -->
                            <xsl:if test="position() > 1">
                                <th class="cluetip">
                                    <xsl:attribute name="data-rel">#ReportInfo-<xsl:value-of select="generate-id(.)"/></xsl:attribute>
                                    <xsl:if test="$showValues">
                                        <xsl:attribute name="class">
											<xsl:text>table-sortable:numeric cluetip</xsl:text>
										</xsl:attribute>
                                        <xsl:value-of select="position()"/>
                                    </xsl:if>
                                </th>
                            </xsl:if>
                        </xsl:for-each>
                    </tr>
                </xsl:if>
            </thead>
            <xsl:variable name="count" select="count($elements)"/>
            <xsl:choose>
                <xsl:when test="$count > 0">
                    <tfoot>
                        <xsl:for-each select="$summaryElement">
                            <xsl:choose>
                                <xsl:when test="$mode = 'absolute'">
                                    <xsl:call-template name="timer-summary-row-abs">
                                        <xsl:with-param name="gid" select="generate-id(.)"/>
                                        <xsl:with-param name="valueName" select="$valueName"/>
                                        <xsl:with-param name="showValues" select="$showValues"/>
                                        <xsl:with-param name="format" select="$format"/>
                                        <xsl:with-param name="rows-in-table" select="$count"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="timer-summary-row-rel">
                                        <xsl:with-param name="gid" select="generate-id(.)"/>
                                        <xsl:with-param name="valueName" select="$valueName"/>
                                        <xsl:with-param name="showValues" select="$showValues"/>
                                        <xsl:with-param name="format" select="$format"/>
                                        <xsl:with-param name="rows-in-table" select="$count"/>
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <tbody>
                        <xsl:for-each select="$elements">
                            <xsl:sort select="name" data-type="number"/>
                            <xsl:choose>
                                <xsl:when test="$mode = 'absolute'">
                                    <xsl:call-template name="timer-row-abs">
                                        <xsl:with-param name="gid" select="generate-id(.)"/>
                                        <xsl:with-param name="valueName" select="$valueName"/>
                                        <xsl:with-param name="showValues" select="$showValues"/>
                                        <xsl:with-param name="format" select="$format"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:call-template name="timer-row-rel">
                                        <xsl:with-param name="gid" select="generate-id(.)"/>
                                        <xsl:with-param name="valueName" select="$valueName"/>
                                        <xsl:with-param name="showValues" select="$showValues"/>
                                        <xsl:with-param name="format" select="$format"/>
                                    </xsl:call-template>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </tbody>
                </xsl:when>
                <xsl:otherwise>
                    <tfoot>
                        <tr><td/><td/></tr>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <tbody class="table-nosort">
                        <tr>
                            <td colspan="2">
                                There are no values to show in this table.
                            </td>
                        </tr>
                    </tbody>
                </xsl:otherwise>
            </xsl:choose>
        </table>

    </xsl:template>

</xsl:stylesheet>
