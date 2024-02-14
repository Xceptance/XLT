<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="configuration">
        <xsl:param name="rootNode"/>

        <div class="section" id="configuration">
            <xsl:call-template name="headline-configuration"/>

            <div class="content">
                <xsl:call-template name="description-configuration"/>

                <div class="data">
                    <table class="properties">
                        <thead>
                            <tr>
                                <th>Property Name</th>
                                <th>Property Value</th>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <td></td>
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <xsl:for-each select="$rootNode/properties/property">
                                <xsl:sort select="@name"/>
                                <tr>
                                    <td class="key">
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td class="value text wrap">
                                        <xsl:value-of select="@value"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="section" id="jvm-configuration">
            <xsl:call-template name="headline-jvm-configuration"/>

            <div class="content">
                <xsl:call-template name="description-jvm-configuration"/>

                <div class="data">
                    <table class="">
                        <thead>
                            <tr>
                                <th>Settings</th>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <td></td>
                            </tr>
                        </tfoot>
                        <tbody>
                            <xsl:choose>
                                <xsl:when test="count($rootNode/customJvmArgs/string) > 0">
                                    <xsl:for-each select="$rootNode/customJvmArgs/string">
                                        <tr>
                                            <td class="value text">
                                                <xsl:value-of select="current()"/>
                                            </td>
                                        </tr>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <tr>
                                        <td class="no-data">No data available</td>
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
