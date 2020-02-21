<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="general">
    <xsl:param name="rootNode" />
    <xsl:param name="name" />
    <xsl:param name="id" />

    <div class="subsection" id="{$id}">
        <div class="content">
            <div class="data">
                <table class="table-autostripe table-stripeclass:odd">
                    <thead>
                        <tr>
                            <th>Name / Directory</th>
                            <th>Test Start</th>
                            <th>Test End</th>
                            <th>Test Duration</th>
                            <th>Total Hits</th>
                            <th>Average Hits Per Second</th>
                        </tr>
                    </thead>
                    <tfoot>
                        <tr>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <tr>
                            <td class="centeredtext">
                                <xsl:value-of select="$name" />
                            </td>
                            <td class="centeredtext">
                                <xsl:value-of select="$rootNode/startTime" />
                            </td>
                            <td class="centeredtext">
                                <xsl:value-of select="$rootNode/endTime" />
                            </td>
                            <td class="centeredtext">
    	                        <xsl:variable name="duration-in-h">
    	                            <xsl:call-template name="format-msec-to-h">
    	                                <xsl:with-param name="n1" select="$rootNode/duration * 1000" />
    	                            </xsl:call-template>
    	                        </xsl:variable>
                                <xsl:value-of select="$duration-in-h" />
                            </td>
                            <td class="centeredtext">
                                <xsl:value-of select="format-number($rootNode/hits, '#,##0')" />
                            </td>
                            <td class="centeredtext">
                                <xsl:value-of select="format-number($rootNode/hits div $rootNode/duration, '#,##0')" />
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</xsl:template>

</xsl:stylesheet>