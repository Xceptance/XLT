<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Magic to group events by their name for counting them later on. -->
    <xsl:key name="eventsByName" match="events/event" use="name"/>

    <xsl:template name="events">
        <xsl:param name="rootNode"/>

        <div class="section" id="event-summary">
            <xsl:call-template name="headline-event-summary"/>

            <div class="content">
                <xsl:call-template name="description-event-summary"/>

                <div class="data">

                    <div class="charts">
                        <div class="chart">
                            <img src="charts/Events.png" alt="Events"/>
                        </div>
                    </div>

                    <h3 id="event-overview">Overview</h3>
                    <table class="table-autosort:1 table-autosort-order:desc table-autostripe table-stripeclass:odd">
                        <thead>
                            <tr>
                                <th class="table-sortable:alphanumeric">Event</th>
                                <th class="table-sortable:numeric">Count</th>
                                <th class="table-sortable:numeric">Percentage</th>
                            </tr>
                        </thead>
                        <xsl:choose>
                            <xsl:when test="count($rootNode/event) > 0">
                                <xsl:variable name="totalEventCount">
                                    <xsl:value-of select="sum($rootNode/event/totalCount)"/>
                                </xsl:variable>
                                <xsl:variable name="eventRepresentativesWithDistinctNames" select="events/event[generate-id() = generate-id(key('eventsByName', name)[1])]" />
                                <xsl:variable name="countDistinctEventNames" select="count($eventRepresentativesWithDistinctNames)" />
                                <tfoot>
                                    <tr class="totals">
                                        <xsl:call-template name="create-totals-td">
                                            <xsl:with-param name="rows-in-table" select="$countDistinctEventNames" />
                                        </xsl:call-template>
                                        
                                        <td class="value number">
                                            <xsl:value-of select="format-number($totalEventCount, '#,##0')"/>
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(1, '#0.0%')"/>
                                        </td>
                                    </tr>
                                </tfoot>
                                <tbody>
                                    <!-- Here the grouping magic continues ... -->
                                    <xsl:for-each select="$eventRepresentativesWithDistinctNames">
                                        <xsl:sort select="name"/>

                                        <xsl:variable name="eventCountByName">
                                            <xsl:value-of select="sum(key('eventsByName', name)/totalCount)"/>
                                        </xsl:variable>

                                        <tr>
                                            <td class="value text forcewordbreak">
                                                <xsl:value-of select="name"/>
                                            </td>
                                            <td class="value number count">
                                                <xsl:value-of select="format-number($eventCountByName, '#,##0')"/>
                                            </td>
                                            <td class="value number count">
                                                <xsl:value-of select="format-number($eventCountByName div $totalEventCount, '#0.0%')"/>
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

                    <h3 id="event-details">Details</h3>
                    <table>
                        <thead>
                            <tr>
                                <th rowspan="2">Test Case</th>
                                <th rowspan="2">Event</th>
                                <th rowspan="2">Total Count</th>
                                <th colspan="2">Event Information</th>
                            </tr>
                            <tr>
                                <th>Count</th>
                                <th>Message</th>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <xsl:choose>
                                <xsl:when test="count($rootNode/event) > 0">
                                    <xsl:for-each select="$rootNode/event">
                                        <xsl:sort select="totalCount" order="descending" data-type="number"/>

                                        <xsl:variable name="even-or-odd">
                                            <xsl:choose>
                                                <xsl:when test="position() mod 2 = 0">
                                                    <xsl:text>odd</xsl:text>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:text>even</xsl:text>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:variable name="messageCount" select="count(messages/message)"/>

                                        <xsl:for-each select="messages/message">
                                            <xsl:sort select="count" order="descending" data-type="number"/>

                                            <tr class="{$even-or-odd}">
                                                <xsl:choose>
                                                    <xsl:when test="position() = 1">
                                                        <td class="value text">
                                                            <xsl:attribute name="rowspan">
                                                                <xsl:value-of select="$messageCount"/>       														
                                                            </xsl:attribute>
                                                            <xsl:value-of select="../../testCaseName"/>
                                                        </td>
                                                        <td class="value text">
                                                            <xsl:attribute name="rowspan">
                                                                <xsl:value-of select="$messageCount"/>
                                                            </xsl:attribute>
                                                            <xsl:value-of select="../../name"/>
                                                        </td>
                                                        <td class="value number">
                                                            <xsl:attribute name="rowspan">
                                                                <xsl:value-of select="$messageCount"/>
                                                            </xsl:attribute>
                                                            <xsl:value-of select="format-number(../../totalCount, '#,##0')"/>
                                                        </td>
                                                    </xsl:when>
                                                </xsl:choose>

                                                <td class="value number">
                                                    <xsl:value-of select="format-number(count, '#,##0')"/>
                                                </td>
                                                <td class="value text forcewordbreak">
                                                    <xsl:value-of select="info"/>
                                                </td>
                                            </tr>
                                        </xsl:for-each>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <tr>
                                        <td class="value text" colspan="5">There are no values to show in this table.</td>
                                    </tr>
                                </xsl:otherwise>
                            </xsl:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </xsl:template>

</xsl:stylesheet>
