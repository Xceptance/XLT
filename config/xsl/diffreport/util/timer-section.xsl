<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template name="timer-section">
        <xsl:param name="elements"/>
        <xsl:param name="summaryElement"/>
        <xsl:param name="tableRowHeader"/>
        <xsl:param name="type"/>
        <div class="data">
            <table class="table-autosort:0 table-autostripe table-stripeclass:odd">
                <thead>
                    <tr>
                        <th rowspan="2" class="table-sortable:alphanumeric">
                            <xsl:value-of select="$tableRowHeader"/>
                            <br/>
                            <input class="filter" placeholder="Enter filter substrings"/>
                        </th>
                        <th colspan="5">Count</th>
                        <th>Errors</th>
                        <xsl:if test="$type = 'transaction'">
                            <th>Events</th>
                        </xsl:if>
                        <th colspan="5">Runtime [ms]</th>
                    </tr>
                    <tr>
                        <th class="table-sortable:numeric">Total</th>
                        <th class="table-sortable:numeric">1/s</th>
                        <th class="table-sortable:numeric">1/min</th>
                        <th class="table-sortable:numeric">1/h*</th>
                        <th class="table-sortable:numeric">1/d*</th>
                        <th class="table-sortable:numeric">Total</th>
                        <xsl:if test="$type = 'transaction'">
                            <th class="table-sortable:numeric">Total</th>
                        </xsl:if>
                        <th class="table-sortable:numeric" title="The median of the data series.">Med.</th>
                        <th class="table-sortable:numeric" title="The arithmetic mean of the data series.">Mean</th>
                        <th class="table-sortable:numeric" title="The smallest value of the data series.">Min.</th>
                        <th class="table-sortable:numeric" title="The largest value of the data series.">Max.</th>
                        <th class="table-sortable:numeric" title="The standard deviation of all data within this data series.">Dev.</th>
                    </tr>
                </thead>
                <xsl:variable name="count" select="count($elements)" />
                <xsl:choose>
                    <xsl:when test="$count > 0">
                        <tfoot>
                            <xsl:for-each select="$summaryElement">
                                <xsl:call-template name="timer-summary-row">
                                    <xsl:with-param name="type" select="$type"/>
                                    <xsl:with-param name="rows-in-table" select="$count"/>
                                </xsl:call-template>
                            </xsl:for-each>
                            <xsl:call-template name="filtered-footer-row" />
                        </tfoot>
                        <tbody>
                            <xsl:for-each select="$elements">
                                <xsl:sort select="name" data-type="number"/>
                                <xsl:call-template name="timer-row">
                                    <xsl:with-param name="type" select="$type"/>
                                </xsl:call-template>
                            </xsl:for-each>
                        </tbody>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="columns">
                            <xsl:choose>
                                <xsl:when test="$type = 'transaction'">
                                    13
                                </xsl:when>
                                <xsl:otherwise>
                                    12
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <tfoot>
                            <tr>
                                <td colspan="{$columns}"></td>
                            </tr>
                            <xsl:call-template name="filtered-footer-row" />
                        </tfoot>
                        <tbody class="table-nosort">
                            <tr>
                                <td colspan="{$columns}">
                                    There are no values to show in this table.
                                </td>
                            </tr>
                        </tbody>
                    </xsl:otherwise>
                </xsl:choose>
            </table>
        </div>

    </xsl:template>

</xsl:stylesheet>
