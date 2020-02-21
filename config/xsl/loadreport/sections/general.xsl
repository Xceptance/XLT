<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="general">
        <xsl:param name="rootNode"/>

        <div class="section" id="general">
            <xsl:call-template name="headline-general"/>

            <div class="content">
                <xsl:call-template name="description-general"/>

                <div class="data">
                    <table class="table-autostripe table-stripeclass:odd">
                        <thead>
                            <tr>
                                <th>Test Duration [hh:mm:ss]</th>
                                <th>Test Start</th>
                                <th>Test End</th>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <tr>
                                <xsl:variable name="duration-in-h">
                                    <xsl:call-template name="format-msec-to-h">
                                        <xsl:with-param name="n1" select="$rootNode/duration * 1000"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <td class="value">
                                    <xsl:value-of select="$duration-in-h"/>
                                </td>
                                <td class="value">
                                    <xsl:value-of select="$rootNode/startTime"/>
                                </td>
                                <td class="value">
                                    <xsl:value-of select="$rootNode/endTime"/>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="charts">
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/ConcurrentUsers.png</xsl:attribute>
                            <xsl:attribute name="alt">Concurrent Users</xsl:attribute>
                        </img>
                    </div>
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/RequestsPerSecond.png</xsl:attribute>
                            <xsl:attribute name="alt">Requests</xsl:attribute>
                        </img>
                    </div>
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/RequestRuntime.png</xsl:attribute>
                            <xsl:attribute name="alt">Request Runtime</xsl:attribute>
                        </img>
                    </div>
                    <div class="chart">
                        <img>
                            <xsl:attribute name="src">charts/TransactionErrors.png</xsl:attribute>
                            <xsl:attribute name="alt">Transaction Errors</xsl:attribute>
                        </img>
                    </div>
                </div>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
