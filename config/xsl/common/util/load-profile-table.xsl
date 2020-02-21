<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:import href="max.xsl"/>
    <xsl:import href="min.xsl"/>

    <!-- Calculates the sum of multiple value ranges: "1...10"/"1"/"1...5" => "3...16" -->
    <xsl:template name="sum-of-ranges">
        <xsl:param name="seq"/>
        <xsl:param name="minSumSoFar" select="0"/>
        <xsl:param name="maxSumSoFar" select="0"/>
        <xsl:choose>
            <xsl:when test="$seq">
                <xsl:variable name="min">
                    <xsl:choose>
                        <xsl:when test="contains($seq[1], '...')">
                            <xsl:value-of select="substring-before($seq[1], '...')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$seq[1]"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="max">
                    <xsl:choose>
                        <xsl:when test="contains($seq[1], '...')">
                            <xsl:value-of select="substring-after($seq[1], '...')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$seq[1]"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:call-template name="sum-of-ranges">
                    <xsl:with-param name="seq" select="$seq[position()!=1]"/>
                    <xsl:with-param name="minSumSoFar" select="$minSumSoFar + translate($min, ',', '')"/>
                    <xsl:with-param name="maxSumSoFar" select="$maxSumSoFar + translate($max, ',', '')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$maxSumSoFar = 0">
                        <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                    </xsl:when>
                    <xsl:when test="$minSumSoFar = $maxSumSoFar">
                        <xsl:value-of select="format-number($minSumSoFar, '#,##0')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="format-number($minSumSoFar, '#,##0')"/>
                        <xsl:text>...</xsl:text>
                        <xsl:value-of select="format-number($maxSumSoFar, '#,##0')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Calculates a range from time values: "00:00:30"/"00:05:00" => "00:00:30...00:05:00" -->
    <xsl:template name="time-range">
        <xsl:param name="seq"/>
        <xsl:variable name="min">
            <xsl:call-template name="min">
                <xsl:with-param name="seq" select="$seq"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="max">
            <xsl:call-template name="max">
                <xsl:with-param name="seq" select="$seq"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$max &lt;= 0">
                <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
            </xsl:when>
            <xsl:when test="$min = $max">
                <xsl:call-template name="format-msec-to-h">
                    <xsl:with-param name="n1" select="$min * 1000"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$min &lt; 0">
                        <xsl:call-template name="format-msec-to-h">
                            <xsl:with-param name="n1" select="0"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="format-msec-to-h">
                            <xsl:with-param name="n1" select="$min * 1000"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>...</xsl:text>
                <xsl:call-template name="format-msec-to-h">
                    <xsl:with-param name="n1" select="$max * 1000"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Calculates the maximum think time range from the passed test case configurations => "3,000...7,000" -->
    <xsl:template name="think-time-range">
        <xsl:param name="seq"/>
        <xsl:param name="minSoFar"/>
        <xsl:param name="maxSoFar"/>
        <xsl:choose>
            <xsl:when test="$seq">
                <xsl:variable name="minimumActionThinkTime">
                    <xsl:choose>
                        <xsl:when test="$seq[1]/actionThinkTime - $seq[1]/actionThinkTimeDeviation &lt; 0">
                            <xsl:value-of select="0"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$seq[1]/actionThinkTime - $seq[1]/actionThinkTimeDeviation"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="maximumActionThinkTime" select="$seq[1]/actionThinkTime + $seq[1]/actionThinkTimeDeviation"/>
                <xsl:variable name="min">
                    <xsl:choose>
                        <xsl:when test="string-length($minSoFar) = 0 or $minimumActionThinkTime &lt; $minSoFar">
                            <xsl:value-of select="$minimumActionThinkTime"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$minSoFar"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="max">
                    <xsl:choose>
                        <xsl:when test="string-length($maxSoFar) = 0 or $maximumActionThinkTime &gt; $maxSoFar">
                            <xsl:value-of select="$maximumActionThinkTime"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$maxSoFar"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:call-template name="think-time-range">
                    <xsl:with-param name="seq" select="$seq[position()!=1]"/>
                    <xsl:with-param name="minSoFar" select="$min"/>
                    <xsl:with-param name="maxSoFar" select="$max"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="string-length($maxSoFar) = 0">
                        <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                    </xsl:when>
                    <xsl:when test="$minSoFar = $maxSoFar">
                        <xsl:value-of select="format-number($maxSoFar, '#,##0')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="format-number($minSoFar, '#,##0')"/>
                        <xsl:text>...</xsl:text>
                        <xsl:value-of select="format-number($maxSoFar, '#,##0')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="load-profile-table">
        <xsl:param name="rootNode"/>

        <table class="table-autostripe table-stripeclass:odd">
            <thead>
                <tr>
                    <th>Transaction Name</th>
                    <th>Users</th>
                    <th>Iterations</th>
                    <th>Arrival Rate [1/h]</th>
                    <th>Initial Delay</th>
                    <th>Ramp-Up Period</th>
                    <th>Warm-Up Period</th>
                    <th>Measurement Period</th>
                    <th>Shutdown Period</th>
                    <th title="Thinking time between actions">Think Time [ms]</th>
                </tr>
            </thead>
            <xsl:variable name="count" select="count($rootNode/testCase)"/>
            <xsl:choose>
                <xsl:when test="$count > 0">
                    <tfoot>
                        <tr class="totals">
                            <xsl:call-template name="create-totals-td">
                                <xsl:with-param name="rows-in-table" select="$count" />
                            </xsl:call-template>
                            
                            <td class="value number">
                                <xsl:call-template name="sum-of-ranges">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/numberOfUsers"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="sum-of-ranges">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/numberOfIterations"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="sum-of-ranges">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/arrivalRate"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="time-range">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/initialDelay"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="time-range">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/rampUpPeriod"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="time-range">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/warmUpPeriod"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="time-range">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/measurementPeriod"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="time-range">
                                    <xsl:with-param name="seq" select="$rootNode/testCase/shutdownPeriod"/>
                                </xsl:call-template>
                            </td>
                            <td class="value number">
                                <xsl:call-template name="think-time-range">
                                    <xsl:with-param name="seq" select="$rootNode/testCase"/>
                                </xsl:call-template>
                            </td>
                        </tr>
                    </tfoot>
                    <tbody>
                        <xsl:for-each select="$rootNode/testCase">
                            <xsl:sort select="userName"/>

                            <xsl:variable name="mode">
                                <xsl:choose>
                                    <xsl:when test="numberOfIterations = 0">
                                        <xsl:text>duration</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:text>iteration</xsl:text>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>

                            <tr>
                                <td class="value text">
                                    <xsl:attribute name="title"><xsl:value-of select="testCaseClassName"/></xsl:attribute>
                                    <xsl:value-of select="userName"/>
                                </td>
                                <td class="value number">
                                    <xsl:if test="string-length(arrivalRate) = 0">
                                        <xsl:if test="complexLoadFunction">
                                            <xsl:attribute name="title"><xsl:value-of select="complexLoadFunction"/></xsl:attribute>
                                        </xsl:if>
                                    </xsl:if>
                                    <xsl:value-of select="numberOfUsers"/>
                                </td>
                                <td class="value number">
                                    <xsl:choose>
                                        <xsl:when test="$mode = 'iteration'">
                                            <xsl:value-of select="format-number(numberOfIterations, '#,##0')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value number">
                                    <xsl:choose>
                                        <xsl:when test="$mode = 'duration'">
                                            <xsl:choose>
                                                <xsl:when test="string-length(arrivalRate) != 0">
                                                    <xsl:if test="complexLoadFunction">
                                                        <xsl:attribute name="title"><xsl:value-of select="complexLoadFunction"/></xsl:attribute>
                                                    </xsl:if>
                                                    <xsl:value-of select="arrivalRate"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value number">
                                    <xsl:choose>
                                        <xsl:when test="initialDelay > 0">
                                            <xsl:call-template name="format-msec-to-h">
                                                <xsl:with-param name="n1" select="initialDelay * 1000"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value number">
                                    <xsl:choose>
                                        <xsl:when test="rampUpPeriod > 0">
                                            <xsl:call-template name="format-msec-to-h">
                                                <xsl:with-param name="n1" select="rampUpPeriod * 1000"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value number">
                                    <xsl:choose>
                                        <xsl:when test="warmUpPeriod > 0">
                                            <xsl:call-template name="format-msec-to-h">
                                                <xsl:with-param name="n1" select="warmUpPeriod * 1000"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value number">
                                    <xsl:call-template name="format-msec-to-h">
                                        <xsl:with-param name="n1" select="measurementPeriod * 1000"/>
                                    </xsl:call-template>
                                </td>
                                <td class="value number">
                                    <xsl:choose>
                                        <xsl:when test="shutdownPeriod > 0">
                                            <xsl:call-template name="format-msec-to-h">
                                                <xsl:with-param name="n1" select="shutdownPeriod * 1000"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text disable-output-escaping="yes">&amp;ndash;</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td class="value number">
                                    <xsl:call-template name="think-time-range">
                                        <xsl:with-param name="seq" select="."/>
                                    </xsl:call-template>
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
                            <td></td>
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
                            <td class="value text" colspan="9">There is no load profile defined?!?</td>
                        </tr>
                    </tbody>
                </xsl:otherwise>
            </xsl:choose>
        </table>

    </xsl:template>

</xsl:stylesheet>
