<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="ratings">
    <xsl:param name="elements" />
    <xsl:param name="active" />

    <div class="section" id="scorecard-ratings">
        <xsl:call-template name="headline-ratings"/>

        <div class="content">
            <xsl:call-template name="description-ratings" />

            <div class="data">

                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Description</th>
                            <th>Enabled</th>
                            <th>Fails Test</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                    <xsl:variable name="count" select="count($elements)"/>
                    <xsl:choose>
                        <xsl:when test="$count > 0">
                            <tbody>
                                <xsl:for-each select="$elements">
                                    <xsl:element name="tr">
                                        <xsl:if test="@name != $active or @enabled != 'true'">
                                            <xsl:attribute name="class" select="'inactive'" />
                                        </xsl:if>
                                        <!-- Name -->
                                        <td class="key">
                                            <xsl:value-of select="@name" />
                                        </td>
                                        <!-- Description -->
                                        <td class="value text">
                                            <xsl:value-of select="description" />
                                        </td>
                                        <!-- Enabled -->
                                        <td class="value">
                                            <xsl:value-of select="@enabled" />
                                        </td>
                                        <!-- Fails Test -->
                                        <td class="value">
                                            <xsl:value-of select="@failsTest" />
                                        </td>
                                        <!-- Value (Threshold) -->
                                        <td class="value number">
                                            <xsl:value-of select="@value" />
                                        </td>
                                    </xsl:element>
                                </xsl:for-each>
                            </tbody>
                        </xsl:when>
                        <xsl:otherwise>
                            <tbody>
                                <tr>
                                    <td class="no-data" colspan="5">No data available</td>
                                </tr>
                            </tbody>
                        </xsl:otherwise>
                    </xsl:choose>
                </table>

            </div><!-- /data -->

        </div><!-- /content -->

    </div><!-- /section -->

</xsl:template>

</xsl:stylesheet>
