<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="web-vitals">

        <div class="section" id="action-summary">
            <xsl:call-template name="headline-web-vitals-summary"/>

            <div class="content">
                <xsl:call-template name="description-web-vitals-summary"/>
                
                <xsl:variable name="count" select="count(webVitalsList/*)"/>

                <table class="c-tab-content table-autosort:0">
                    <thead>
                        <tr>
                            <th class="table-sortable:alphanumeric">
                                Action Name
                                <br/>
                                <input class="filter" placeholder="Enter filter substrings" title=""/>
                                <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                            </th>
                            <th class="table-sortable:numeric">First Contentful Paint<br/>(FCP)</th>
                            <th class="table-sortable:numeric">Largest Contentful Paint<br/>(LCP)</th>
                            <th class="table-sortable:numeric">Cumulative Layout Shift<br/>(CLS)</th>
                            <th class="table-sortable:numeric">First Input Delay<br/>(FID)</th>
                            <th class="table-sortable:numeric">Interaction to Next Paint<br/>(INP)</th>
                            <th class="table-sortable:numeric">Time to First Byte<br/>(TTFB)</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr class="totals">
                            <xsl:call-template name="create-totals-td">
                                <xsl:with-param name="rows-in-table" select="$count"/>
                                <xsl:with-param name="class" select="'key'"/>
                            </xsl:call-template>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <xsl:choose>
                        <xsl:when test="$count > 0">
                            <tbody>
                                <xsl:for-each select="webVitalsList/webVitals">
                                    <xsl:sort select="name"/>
                                    <tr>
                                        <td class="key">
                                            <xsl:value-of select="name"/>
                                        </td>
                                        <xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="fcp/score"/>
								            <xsl:with-param name="isInverse" select="false()"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="unit" select="' ms'"/>
								        </xsl:call-template>
                                       	<xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="lcp/score"/>
								            <xsl:with-param name="isInverse" select="false()"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="unit" select="' ms'"/>
								        </xsl:call-template>
                                       	<xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="cls/score"/>
								            <xsl:with-param name="isInverse" select="false()"/>
								            <xsl:with-param name="format" select="'#,##0.000'"/>
								        </xsl:call-template>
                                       	<xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="fid/score"/>
								            <xsl:with-param name="isInverse" select="false()"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="unit" select="' ms'"/>
								        </xsl:call-template>
                                       	<xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="inp/score"/>
								            <xsl:with-param name="isInverse" select="false()"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="unit" select="' ms'"/>
								        </xsl:call-template>
                                       	<xsl:call-template name="timer-cell">
								            <xsl:with-param name="node" select="ttfb/score"/>
								            <xsl:with-param name="isInverse" select="false()"/>
								            <xsl:with-param name="format" select="'#,##0'"/>
								            <xsl:with-param name="unit" select="' ms'"/>
								        </xsl:call-template>
                                    </tr>
                                </xsl:for-each>
                            </tbody>
                        </xsl:when>
                        <xsl:otherwise>
                            <tbody class="table-nosort">
                                <tr>
                                    <td colspan="7" class="no-data">No data available</td>
                                </tr>
                            </tbody>
                        </xsl:otherwise>
                    </xsl:choose>
                </table>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>