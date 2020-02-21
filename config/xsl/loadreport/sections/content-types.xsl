<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="content-types">
        <xsl:param name="rootNode"/>
        <xsl:param name="totalHits"/>

        <div class="section" id="content-types">
            <xsl:call-template name="headline-content-types"/>

            <div class="content">
                <xsl:call-template name="description-content-types"/>

                <div class="data">
                    <table class="table-autosort:0 table-autostripe table-stripeclass:odd">
                        <thead>
                            <tr>
                                <th class="table-sortable:alphanumeric">Content Type</th>
                                <th class="table-sortable:numeric">Count</th>
                                <th class="table-sortable:numeric">Percentage</th>
                            </tr>
                        </thead>
                        <xsl:variable name="count" select="count($rootNode/contentType)"/>
                        <xsl:choose>
                            <xsl:when test="$count > 0">
                                <tfoot>
                                    <tr class="totals">
                                        <xsl:call-template name="create-totals-td">
                                            <xsl:with-param name="rows-in-table" select="$count" />
                                        </xsl:call-template>
                                        
                                        <td class="value number">
                                            <xsl:value-of select="format-number($totalHits, '#,##0')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(1, '#0.0%')"/>
                                        </td>
                                    </tr>
                                </tfoot>
                                <tbody>
                                    <xsl:for-each select="$rootNode/contentType">
                                        <xsl:sort select="contentType"/>
                                        <tr>
                                            <td class="key">
                                                <xsl:value-of select="contentType"/>
                                            </td>
                                            <td class="value">
                                                <xsl:value-of select="format-number(count, '#,##0')"/>
                                            </td>
                                            <td class="value">
                                                <xsl:value-of select="format-number(count div $totalHits, '#,##0.00%')"/>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </tbody>
                            </xsl:when>
                            <xsl:otherwise>
                                <tfoot>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </tfoot>
                                <tbody>
                                    <tr>
                                        <td class="value text" colspan="3">There are no values to show in this table.</td>
                                    </tr>
                                </tbody>
                            </xsl:otherwise>
                        </xsl:choose>
                    </table>
                </div>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>