<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="network-table">
        <xsl:param name="rootNode"/>

        <table class="table-autostripe table-stripeclass:odd">
            <thead>
                <tr>
                    <th></th>
                    <th>Total</th>
                    <th>1/s</th>
                    <th>1/min</th>
                    <th>1/h*</th>
                    <th>1/d*</th>
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
                    <xsl:variable name="requestsTotal" select="$rootNode/hits"/>
                    <xsl:variable name="requestsPerSecond" select="$rootNode/hits div $rootNode/duration"/>
                    <td class="key">
                        Requests
                    </td>
                    <td class="value">
                        <xsl:value-of select="format-number($requestsTotal, '#,##0')"/>
                    </td>
                    <td class="value">
                        <xsl:value-of select="format-number($requestsPerSecond, '#,##0')"/>
                    </td>
                    <td class="value">
                        <xsl:value-of select="format-number($requestsPerSecond * 60, '#,##0')"/>
                    </td>
                    <td class="value">
                        <xsl:value-of select="format-number($requestsPerSecond * 3600, '#,##0')"/>
                    </td>
                    <td class="value">
                        <xsl:value-of select="format-number($requestsPerSecond * 86400, '#,##0')"/>
                    </td>
                </tr>
                <tr>
                    <xsl:variable name="bytesSentTotal" select="$rootNode/bytesSent"/>
                    <xsl:variable name="bytesSentPerSecond" select="$rootNode/bytesSent div $rootNode/duration"/>
                    <td class="key">
                        Bytes Sent
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesSentTotal"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesSentPerSecond"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesSentPerSecond * 60"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesSentPerSecond * 3600"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesSentPerSecond * 86400"/>
                        </xsl:call-template>
                    </td>
                </tr>
                <tr>
                    <xsl:variable name="bytesReceivedTotal" select="$rootNode/bytesReceived"/>
                    <xsl:variable name="bytesReceivedPerSecond" select="$rootNode/bytesReceived div $rootNode/duration"/>
                    <td class="key">
                        Bytes Received
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesReceivedTotal"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesReceivedPerSecond"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesReceivedPerSecond * 60"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesReceivedPerSecond * 3600"/>
                        </xsl:call-template>
                    </td>
                    <td class="value">
                        <xsl:call-template name="format-bytes">
                            <xsl:with-param name="bytes" select="$bytesReceivedPerSecond * 86400"/>
                        </xsl:call-template>
                    </td>
                </tr>
            </tbody>
        </table>

    </xsl:template>

</xsl:stylesheet>