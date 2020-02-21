<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- Not that this template is used for the summary table on the overview page of the load test report. 
         It is not used to create the summary rows for the other timer tables -->
    <xsl:template name="summary-timer-row">

        <xsl:param name="name"/>
        <xsl:param name="element"/>
        <xsl:param name="link"/>

        <tr>
            <!-- name -->
            <td class="key">
                <a href="{$link}.html">
                    <xsl:value-of select="$name"/>
                </a>
            </td>

            <!-- count -->
            <td class="value number">
                <xsl:value-of select="format-number($element/count, '#,##0')"/>
            </td>

            <!-- count per sec -->
            <td class="value number">
                <xsl:value-of select="format-number($element/countPerSecond, '#,##0.0')"/>
            </td>

            <!-- count per hour -->
            <td class="value number">
                <xsl:value-of select="format-number($element/countPerHour, '#,##0')"/>
            </td>

            <!-- count per day -->
            <td class="value number">
                <xsl:value-of select="format-number($element/countPerDay, '#,##0')"/>
            </td>

            <!-- errors -->
            <td class="value number">
                <xsl:if test="$element/errors &gt; 0">
                    <xsl:attribute name="class">value number error</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="format-number($element/errors, '#,##0')"/>
            </td>

            <!-- % errors -->
            <xsl:variable name="error-percentage">
                <xsl:call-template name="percentage">
                    <xsl:with-param name="n1" select="$element/count"/>
                    <xsl:with-param name="n2" select="$element/errors"/>
                </xsl:call-template>
            </xsl:variable>
            <td class="value number">
                <xsl:if test="$element/errors &gt; 0">
                    <xsl:attribute name="class">value number error</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="format-number($error-percentage, '#,##0.00')"/>
                <xsl:text>%</xsl:text>
            </td>

            <!-- events -->
            <td class="value number">
                <xsl:choose>
                    <xsl:when test="$element/events">
                        <xsl:if test="$element/events &gt; 0">
                            <xsl:attribute name="class">value number colgroup1 event</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="format-number($element/events, '#,##0')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </td>

            <!-- mean -->
            <td class="value number">
                <xsl:value-of select="format-number($element/mean, '#,##0')"/>
            </td>

            <!-- min -->
            <td class="value number">
                <xsl:value-of select="format-number($element/min, '#,##0')"/>
            </td>

            <!-- max -->
            <td class="value number">
                <xsl:value-of select="format-number($element/max, '#,##0')"/>
            </td>

            <!-- deviation -->
            <td class="value number">
                <xsl:value-of select="format-number($element/deviation, '#,##0')"/>
            </td>

            <!-- runtime percentiles -->
            <xsl:for-each select="$element/percentiles/*">
                <td class="value number">
                    <xsl:value-of select="format-number(current(), '#,##0')"/>
                </td>
            </xsl:for-each>
        </tr>
    </xsl:template>

</xsl:stylesheet>
