<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="timer-summary-row">
        <xsl:param name="type"/>
        <xsl:param name="rows-in-table"/>

        <tr class="totals">
            <xsl:call-template name="create-totals-td">
                <xsl:with-param name="rows-in-table" select="$rows-in-table"/>
                <xsl:with-param name="class" select="'key colgroup1'"/>
            </xsl:call-template>

            <!-- count -->
            <td class="value number">
                <xsl:value-of select="format-number(count, '#,##0')"/>
            </td>

            <!-- distinct -->
            <xsl:if test="$type = 'request'">
                <td class="value number">
                    <!-- value not available in the summary element -->
                </td>
            </xsl:if>

            <!-- count per sec -->
            <td class="value number">
                <xsl:value-of select="format-number(countPerSecond, '#,##0.0')"/>
            </td>

            <!-- count per min -->
            <td class="value number">
                <xsl:value-of select="format-number(countPerMinute, '#,##0')"/>
            </td>

            <!-- count per hour -->
            <td class="value number">
                <xsl:value-of select="format-number(countPerHour, '#,##0')"/>
            </td>

            <!-- errors -->
            <td class="value number colgroup1">
                <xsl:if test="errors &gt; 0">
                    <xsl:attribute name="class">value number colgroup1 error</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="format-number(errors, '#,##0')"/>
            </td>
            <td class="value number colgroup1">
                <xsl:if test="errors &gt; 0">
                    <xsl:attribute name="class">value number colgroup1 error</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="format-number(errorPercentage, '#,##0.00')"/>
                <xsl:text>%</xsl:text>
            </td>

            <!-- events -->
            <xsl:if test="$type = 'transaction'">
                <td class="value number colgroup1">
                    <xsl:if test="events &gt; 0">
                        <xsl:attribute name="class">value number colgroup1 event</xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="format-number(events, '#,##0')"/>
                </td>
            </xsl:if>

            <!-- mean -->
            <td class="value number">
                <xsl:value-of select="format-number(mean, '#,##0')"/>
            </td>

            <!-- min -->
            <td class="value number">
                <xsl:value-of select="format-number(min, '#,##0')"/>
            </td>

            <!-- max -->
            <td class="value number">
                <xsl:value-of select="format-number(max, '#,##0')"/>
            </td>

            <!-- deviation -->
            <td class="value number">
                <xsl:value-of select="format-number(deviation, '#,##0')"/>
            </td>

            <!-- runtime percentiles -->
            <xsl:for-each select="percentiles/*">
                <td class="value number colgroup1">
                    <xsl:value-of select="format-number(current(), '#,##0')"/>
                </td>
            </xsl:for-each>

            <!-- apdex -->
            <xsl:if test="$type = 'action'">
                <xsl:variable name="apdexColor">
                    <xsl:call-template name="convert-apdex-to-color">
                        <xsl:with-param name="apdex" select="apdex/value"/>
                    </xsl:call-template>
                </xsl:variable>
                <td class="value number {$apdexColor}">
                    <xsl:value-of select="apdex/longValue"/>
                </td>
            </xsl:if>

            <!-- runtime segmentation -->
            <xsl:if test="$type = 'request'">
                <xsl:for-each select="countPerInterval/int">
                	<xsl:variable name="position" select="position()" />
                	<xsl:variable name="percentage"
						select="../../percentagePerInterval/big-decimal[$position]" />
                    <td class="value number">
                        <span>
                            <xsl:attribute name="title">
                                <xsl:value-of select="format-number(current(), '#,##0')"/>
                                <xsl:text> (</xsl:text>
                                <xsl:value-of select="format-number($percentage, '#,##0.00')"/>
                                <xsl:text>%)</xsl:text>
                            </xsl:attribute>
                            <xsl:value-of select="format-number($percentage, '#,##0.00')"/>
                            <xsl:text>%</xsl:text>
                        </span>
                    </td>
                </xsl:for-each>
            </xsl:if>
        </tr>
    </xsl:template>

</xsl:stylesheet>
