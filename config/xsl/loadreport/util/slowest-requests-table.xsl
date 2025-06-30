<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="slowest-requests-table">
        <xsl:param name="slowestRequests"/>
        <xsl:param name="requests"/>

        <table class="c-tab-content table-autosort:1 table-autosort-order:desc">
            <thead>
                <tr>
                    <th rowspan="2" class="table-sortable:alphanumeric colgroup1" id="sortByName">
                        <xsl:text>Request Name</xsl:text>
                        <br/>
                        <form>
                            <input class="filter" placeholder="Enter filter substrings" title=""/>
                            <button class="clear-input" type="clear" title="Click to clear">&#x2715;</button>
                        </form>
                    </th>
                    <th rowspan="2" class="table-sortable:numeric" id="sortByRuntime" title="Request Runtime">Runtime [ms]</th>
                    <th colspan="2" class="colgroup1">Baseline [ms]</th>
                    <th colspan="2">Bandwidth [Bytes]</th>
                    <th colspan="7" class="colgroup1">Network Timing [ms]</th>
                    <th colspan="2">Request Details</th>
                    <th colspan="2" class="colgroup1">IP Addresses</th>
                </tr>
                <tr>
                    <th class="table-sortable:numeric colgroup1" id="sortByMean" title="Base Mean Runtime">Mean</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByP95" title="Base P95 Runtime">P95</th>
                    <th class="table-sortable:numeric" id="sortByBytesSent" title="Bytes Sent">Sent</th>
                    <th class="table-sortable:numeric" id="sortByBytesReceived" title="Bytes Received">Received</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByDnsTime" title="DNS Time">DNS</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByConnectTime" title="Connect Time">Connect</th>
                    <th class="table-sortable:numeric colgroup1" id="sortBySendTime" title="Send Time">Send</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByServerBusyTime" title="Server Busy Time">Server</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByReceiveTime" title="Receive Time">Receive</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByTimeToFirstBytes" title="Time To First Bytes">TTFB</th>
                    <th class="table-sortable:numeric colgroup1" id="sortByTimeToLastBytes" title="Time To Last Bytes">TTLB</th>
                    <th class="table-sortable:numeric" id="sortByResponseCode" title="Response Code">Response</th>
                    <th class="table-sortable:alphanumeric" id="sortByTime" title="Request Start Time">Time</th>
                    <th class="table-sortable:alphanumeric colgroup1" id="sortByUsedIpAddress" title="Used IP Address">Used</th>
                    <th class="table-sortable:alphanumeric colgroup1" id="sortByReportedIpAddresses" title="IP Addresses Reported By DNS">Reported</th>
                </tr>
            </thead>

            <xsl:choose>
                <xsl:when test="count($slowestRequests) &gt; 0">
                    <tfoot>
                        <tr class="totals">
                            <xsl:call-template name="create-totals-td">
                                <xsl:with-param name="rows-in-table" select="count($slowestRequests)"/>
                                <xsl:with-param name="class" select="'key colgroup1'"/>
                            </xsl:call-template>
                            <td/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td/>
                            <td/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                            <td/>
                            <td/>
                            <td class="colgroup1"/>
                            <td class="colgroup1"/>
                        </tr>
                        <xsl:call-template name="filtered-footer-row"/>
                    </tfoot>
                    <tbody>
                        <xsl:for-each select="$slowestRequests">

                            <xsl:variable name="name" select="name"/>
                            <xsl:variable name="mean" select="$requests[name=$name]/mean"/>
                            <xsl:variable name="p95" select="$requests[name=$name]/percentiles/p95"/>
                            <xsl:variable name="gid" select="generate-id(.)"/>

                            <tr>
                                <td class="key">
                                    <xsl:call-template name="slow-request-name-with-cluetip">
                                        <xsl:with-param name="name" select="$name"/>
                                        <xsl:with-param name="requestId" select="requestId"/>
                                        <xsl:with-param name="httpMethod" select="httpMethod"/>
                                        <xsl:with-param name="url" select="url"/>
                                        <xsl:with-param name="formDataEncoding" select="formDataEncoding"/>
                                        <xsl:with-param name="formData" select="formData"/>
                                        <xsl:with-param name="responseId" select="responseId"/>
                                        <xsl:with-param name="responseCode" select="responseCode"/>
                                        <xsl:with-param name="contentType" select="contentType"/>
                                        <xsl:with-param name="gid" select="$gid"/>
                                    </xsl:call-template>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(runtime, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number($mean, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number($p95, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(bytesSent, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(bytesReceived, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(dnsTime, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(connectTime, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(sendTime, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(serverBusyTime, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(receiveTime, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(timeToFirstBytes, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="format-number(timeToLastBytes, '#,##0')"/>
                                </td>
                                <td class="value number">
                                    <xsl:value-of select="responseCode"/>
                                </td>
                                <td class="value">
                                    <xsl:value-of select="time"/>
                                </td>
                                <td class="value">
                                    <xsl:value-of select="usedIpAddress"/>
                                </td>
                                <td class="value">
                                    <xsl:call-template name="ip-addresses">
                                        <xsl:with-param name="ipAddresses" select="ipAddresses"/>
                                        <xsl:with-param name="gid" select="$gid"/>
                                    </xsl:call-template>
                                </td>
                            </tr>

                        </xsl:for-each>
                    </tbody>
                </xsl:when>
                <xsl:otherwise>
                    <tfoot>
                        <td/>
                        <td/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td/>
                        <td/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                        <td/>
                        <td/>
                        <td class="colgroup1"/>
                        <td class="colgroup1"/>
                    </tfoot>
                    <tbody>
                        <tr>
                            <td class="no-data" colspan="17">No data available</td>
                        </tr>
                    </tbody>
                </xsl:otherwise>
            </xsl:choose>


        </table>

    </xsl:template>

    <xsl:template name="slow-request-name-with-cluetip">
        <xsl:param name="name"/>
        <xsl:param name="requestId"/>
        <xsl:param name="httpMethod"/>
        <xsl:param name="url"/>
        <xsl:param name="formDataEncoding"/>
        <xsl:param name="formData"/>
        <xsl:param name="responseId"/>
        <xsl:param name="responseCode"/>
        <xsl:param name="contentType"/>
        <xsl:param name="gid"/>

        <xsl:variable name="showRequestInfo" select="($requestId != '') or ($httpMethod != '') or ($url != '') or ($formDataEncoding != '') or ($formData != '')"/>
        <xsl:variable name="showResponseInfo" select="($responseId != '') or ($responseCode != '') or ($contentType != '')"/>

        <xsl:choose>
            <xsl:when test="$showRequestInfo or $showResponseInfo">
                <a>
                    <xsl:attribute name="href"/>
                    <xsl:attribute name="onclick">return false;</xsl:attribute>
                    <xsl:attribute name="data-rel">#request-details-<xsl:value-of select="$gid"/></xsl:attribute>
                    <xsl:attribute name="class">cluetip</xsl:attribute>
                    <xsl:value-of select="$name"/>
                </a>
                <xsl:text></xsl:text>
                <div id="request-details-{$gid}" class="cluetip-data cluetip-request-info">
                    <table class="cluetip-table">
                        <tbody>
                            <xsl:if test="$showRequestInfo">
                                <tr><th colspan="2">Request</th></tr>
                                <xsl:if test="$requestId != ''">
                                    <tr>
                                        <td>ID</td>
                                        <td><xsl:value-of select="$requestId" /></td>
                                    </tr>
                                </xsl:if>
                                <xsl:if test="$httpMethod != ''">
                                    <tr>
                                        <td>Method</td>
                                        <td><xsl:value-of select="$httpMethod" /></td>
                                    </tr>
                                </xsl:if>
                                <xsl:if test="$url != ''">
                                    <tr>
                                        <td>URL</td>
                                        <td>
                                            <a target="_blank">
                                                <xsl:attribute name="href"><xsl:value-of select="$url"/></xsl:attribute>
                                                <xsl:value-of select="$url" />
                                            </a>
                                        </td>
                                    </tr>
                                </xsl:if>
                                <xsl:if test="$formDataEncoding != ''">
                                    <tr>
                                        <td>Form Data Encoding</td>
                                        <td><xsl:value-of select="$formDataEncoding" /></td>
                                    </tr>
                                </xsl:if>
                                <xsl:if test="$formData != ''">
                                    <tr>
                                        <td>Form Data</td>
                                        <td><xsl:value-of select="$formData" /></td>
                                    </tr>
                                </xsl:if>
                            </xsl:if>
                            <xsl:if test="$showResponseInfo">
                                <tr><th colspan="2">Response</th></tr>
                                <xsl:if test="$responseId != ''">
                                    <tr>
                                        <td>ID</td>
                                        <td><xsl:value-of select="$responseId" /></td>
                                    </tr>
                                </xsl:if>
                                <xsl:if test="$responseCode != ''">
                                    <tr>
                                        <td>Code</td>
                                        <td><xsl:value-of select="$responseCode" /></td>
                                    </tr>
                                </xsl:if>
                                <xsl:if test="$contentType != ''">
                                    <tr>
                                        <td>Content Type</td>
                                        <td><xsl:value-of select="$contentType" /></td>
                                    </tr>
                                </xsl:if>
                            </xsl:if>
                        </tbody>
                    </table>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$name"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="ip-addresses">
        <xsl:param name="ipAddresses"/>
        <xsl:param name="gid"/>

        <xsl:variable name="ipAddressCount" select="count($ipAddresses/string)"/>

        <xsl:if test="$ipAddressCount &gt; 0">
            <xsl:choose>
                <xsl:when test="$ipAddressCount &gt; 1">
                    <a>
                        <xsl:attribute name="href"/>
                        <xsl:attribute name="onclick">return false;</xsl:attribute>
                        <xsl:attribute name="data-rel">#reported-ip-list-<xsl:value-of select="$gid"/></xsl:attribute>
                        <xsl:attribute name="class">cluetip</xsl:attribute>
                        <xsl:value-of select="$ipAddressCount"/> IP Addresses
                    </a>
                    <xsl:text></xsl:text>
                    <div id="reported-ip-list-{$gid}" class="cluetip-data cluetip-data-openleft">
                        <h4>
                            <xsl:text>Reported IP Addresses:</xsl:text>
                        </h4>
                        <ul>
                            <xsl:for-each select="$ipAddresses/string">
                                <li><xsl:value-of select="." /></li>
                            </xsl:for-each>
                        </ul>
                    </div>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$ipAddresses/string[1]"/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:if>

    </xsl:template>

</xsl:stylesheet>