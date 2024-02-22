<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="web-vitals">
        <div class="section" id="web-vitals-summary">
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
                                <xsl:for-each select="webVitalsList/*">
                                    <xsl:sort select="name"/>
                                    <tr>
                                        <td class="key">
                                            <xsl:value-of select="name"/>
                                        </td>
                                        <td class="value number web-vital">
                                            <xsl:call-template name="web-vital-cell">
                                                <xsl:with-param name="value" select="fcp"/>
                                            </xsl:call-template>
                                        </td>
                                        <td class="value number web-vital">
                                            <xsl:call-template name="web-vital-cell">
                                                <xsl:with-param name="value" select="lcp"/>
                                            </xsl:call-template>
                                        </td>
                                        <td class="value number web-vital">
                                            <xsl:call-template name="web-vital-cell">
                                                <xsl:with-param name="value" select="cls"/>
                                                <xsl:with-param name="unit" select="''"/>
                                                <xsl:with-param name="format" select="'#,##0.000'"/>
                                            </xsl:call-template>
                                        </td>
                                        <td class="value number web-vital">
                                            <xsl:call-template name="web-vital-cell">
                                                <xsl:with-param name="value" select="fid"/>
                                            </xsl:call-template>
                                        </td>
                                        <td class="value number web-vital">
                                            <xsl:call-template name="web-vital-cell">
                                                <xsl:with-param name="value" select="inp"/>
                                            </xsl:call-template>
                                        </td>
                                        <td class="value number web-vital">
                                            <xsl:call-template name="web-vital-cell">
                                                <xsl:with-param name="value" select="ttfb"/>
                                            </xsl:call-template>
                                        </td>
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

    <xsl:template name="web-vital-cell">
        <xsl:param name="value"/><!-- the web-vital value node, such as "fcp" -->
        <xsl:param name="unit" select="'ms'"/><!-- the unit (can be empty) -->
        <xsl:param name="format" select="'#,##0'"/><!-- the format string to use when formatting scores -->

        <xsl:if test="$value">
            <xsl:variable name="goodCount" select="$value/goodCount"/>
            <xsl:variable name="improveCount" select="$value/improveCount"/>
            <xsl:variable name="poorCount" select="$value/poorCount"/>
            <xsl:variable name="totalCount" select="$goodCount + $improveCount + $poorCount"/>

            <xsl:variable name="goodRatio" select="$goodCount div $totalCount"/>
            <xsl:variable name="improveRatio" select="$improveCount div $totalCount"/>
            <xsl:variable name="poorRatio" select="$poorCount div $totalCount"/>

            <xsl:variable name="rating" select="$value/rating"/>

            <div class="web-vital-score web-vital-score-{$rating}">
                <xsl:value-of select="format-number($value/score, $format)"/>
                <xsl:if test="$unit">
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="$unit"/>
                </xsl:if>
            </div>
    
            <div class="web-vital-gauge">
                <span class="web-vital-gauge-arrow web-vital-gauge-arrow-{$rating}"></span>
            </div>
    
            <div class="web-vital-bar">
                <xsl:call-template name="web-vital-bar-segment">
                    <xsl:with-param name="count" select="$goodCount"/>
                    <xsl:with-param name="ratio" select="$goodRatio"/>
                    <xsl:with-param name="rating" select="'good'"/>
                </xsl:call-template>
                <xsl:call-template name="web-vital-bar-segment">
                    <xsl:with-param name="count" select="$improveCount"/>
                    <xsl:with-param name="ratio" select="$improveRatio"/>
                    <xsl:with-param name="rating" select="'improve'"/>
                </xsl:call-template>
                <xsl:call-template name="web-vital-bar-segment">
                    <xsl:with-param name="count" select="$poorCount"/>
                    <xsl:with-param name="ratio" select="$poorRatio"/>
                    <xsl:with-param name="rating" select="'poor'"/>
                </xsl:call-template>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="web-vital-bar-segment">
        <xsl:param name="count"/>
        <xsl:param name="ratio"/>
        <xsl:param name="rating"/>

        <div class="web-vital-bar-segment web-vital-bar-segment-{$rating}" style="flex-grow: {$ratio}">
            <xsl:attribute name="title">
                <xsl:value-of select="$count"/>
                <xsl:text> (</xsl:text>
                <xsl:value-of select="format-number($ratio * 100, '#,##0.0')"/>
                <xsl:text>%)</xsl:text>
            </xsl:attribute>
        </div>
    </xsl:template>

</xsl:stylesheet>
