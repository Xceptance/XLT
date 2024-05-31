<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="groups">
    <xsl:param name="definitions" />
    <xsl:param name="results" />

    <div class="section" id="scorecard-groups">
        <xsl:call-template name="headline-groups"/>

        <div class="content">
            <xsl:call-template name="description-groups" />

            <div class="data">
                <table>
                    <thead>
                        <tr>
                            <th>Group</th>
                            <th>Description</th>
                            <th>Enabled</th>
                            <th>Fails Test</th>
                            <th>Mode</th>
                            <th>Rule Count</th>
                            <th>Status</th>
                            <th>Message</th>
                            <th>Max Points</th>
                            <th>Points</th>
                        </tr>
                    </thead>
                    <xsl:variable name="count" select="count($results)"/>
                    <xsl:choose>
                        <xsl:when test="$count &gt; 0">
                            <tfoot>
                                <tr class="totals">
                                    <td class="key">Totals</td>
                                    <td />
                                    <td />
                                    <td />
                                    <td />
                                    <td class="value number"><xsl:value-of select="count($definitions//rule)" /></td>
                                    <td />
                                    <td />
                                    <td class="value number"><xsl:value-of select="sum($results/@totalPoints)" /></td>
                                    <td class="value number"><xsl:value-of select="sum($results/@points)" /></td>
                                </tr>
                            </tfoot>
                            <tbody>
                                <xsl:for-each select="$results">
                                    <xsl:variable name="groupId" select="@ref-id" />
                                    <xsl:variable name="groupDef" select="$definitions[@id=$groupId]" />

                                    <tr>
                                        <!-- Group Name/ID-->
                                        <td class="key">
                                            <xsl:call-template name="name-or-id">
                                                <xsl:with-param name="node" select="$groupDef" />
                                            </xsl:call-template>
                                        </td>
                                        <!-- Description -->
                                        <td class="value text">
                                            <xsl:value-of select="$groupDef/description" />
                                        </td>
                                        <!-- Enabled -->
                                        <td class="value">
                                            <xsl:value-of select="$groupDef/@enabled" />
                                        </td>
                                        <!-- Fails Test -->
                                        <td class="value">
                                            <xsl:value-of select="$groupDef/@failsTest" />
                                        </td>
                                        <!-- Mode -->
                                        <td class="value">
                                            <xsl:value-of select="$groupDef/@mode" />
                                        </td>
                                        <!-- Rule Count -->
                                        <td class="value number">
                                            <xsl:value-of select="count($groupDef/rules/rule)" />
                                        </td>
                                        <!-- Status -->
                                        <td class="value">
                                            <xsl:value-of select="status" />
                                        </td>
                                        <!-- Message -->
                                        <td class="value text">
                                            <xsl:value-of select="message" separator=";" />
                                        </td>
                                        <!-- Max Points -->
                                        <td class="value number">
                                            <xsl:value-of select="@totalPoints" />
                                        </td>
                                        <!-- Points -->
                                        <td class="value number">
                                            <xsl:value-of select="@points" />
                                        </td>
                                    </tr>
                                </xsl:for-each><!-- /$results -->
                            </tbody>
                        </xsl:when>
                        <xsl:otherwise>
                            <tbody>
                                <tr>
                                    <td class="no-data" colspan="10">No data available</td>
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
