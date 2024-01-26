<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="web-vital-cell">
        <xsl:param name="value"/><!-- the class attribute of the td, defaults to "key" -->

        <xsl:if test="$value">
            <xsl:variable name="count" select="$value/goodCount + $value/imprCount + $value/poorCount"/>
            <xsl:variable name="goodPercentage" select="$value/goodCount div $count"/>
            <xsl:variable name="imprPercentage" select="$value/imprCount div $count"/>
            <xsl:variable name="poorPercentage" select="$value/poorCount div $count"/>

			<!--
			<div class="score {$value/rating}">
	            <xsl:value-of select="format-number($value/score, '#,##0.0##')"/>
			</div>
			-->
			<div style="width: 75%; border-right: 1px">
	            <xsl:value-of select="format-number($value/score, '#,##0.0##')"/>
			</div>
	
			<div class="marker">
		        <span class="arrow {$value/rating}"></span>
			</div>
	
			<div class="bar">
                <xsl:call-template name="web-vital-bar-segment">
                    <xsl:with-param name="count" select="$value/goodCount"/>
                    <xsl:with-param name="percentage" select="$goodPercentage"/>
                    <xsl:with-param name="rating" select="'good'"/>
                </xsl:call-template>
                <xsl:call-template name="web-vital-bar-segment">
                    <xsl:with-param name="count" select="$value/imprCount"/>
                    <xsl:with-param name="percentage" select="$imprPercentage"/>
                    <xsl:with-param name="rating" select="'impr'"/>
                </xsl:call-template>
                <xsl:call-template name="web-vital-bar-segment">
                    <xsl:with-param name="count" select="$value/poorCount"/>
                    <xsl:with-param name="percentage" select="$poorPercentage"/>
                    <xsl:with-param name="rating" select="'poor'"/>
                </xsl:call-template>
			</div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="web-vital-bar-segment">
        <xsl:param name="count"/><!-- the class attribute of the td, defaults to "key" -->
        <xsl:param name="percentage"/><!-- the class attribute of the td, defaults to "key" -->
        <xsl:param name="rating"/><!-- the class attribute of the td, defaults to "key" -->

        <div class="segment {$rating}" style="flex-grow: {$percentage}">
			<xsl:attribute name="title">
	            <xsl:value-of select="$count"/>
				<xsl:text> (</xsl:text>
	            <xsl:value-of select="format-number($percentage * 100, '#,##0.0')"/>
				<xsl:text>%)</xsl:text>
			</xsl:attribute>
		</div>
    </xsl:template>

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
                                Value Name
                                <br/>
                                <input class="filter" placeholder="Enter filter substrings" title=""/>
                                <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                            </th>
                            <th class="table-sortable:numeric">First Contentful Paint<br/>(FCP)</th>
                            <th class="table-sortable:numeric">Largest Contentful Paint<br/>(LCP)</th>
                            <th class="table-sortable:numeric">Cumulative Layout Shift<br/>(CLS)</th>
                            <th class="table-sortable:numeric">First Input Delay<br/>(FID)</th>
                            <th class="table-sortable:numeric">Interaction to Next Paint<br/>(INP)</th>
                            <th class="table-sortable:numeric">Time to First Byte<br/>(TTBF)</th>
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

</xsl:stylesheet>
