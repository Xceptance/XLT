<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="logs">
    <xsl:param name="logs" />

    <div class="section" id="scorecard-logs">
        <h2>Logging</h2>

        <div class="content">
            <div class="description">
                <p>The following log messages were captured during the scorecard evaluation script run.</p>
            </div>

            <div class="data">
                <table class="no-auto-stripe">
                    <thead>
                        <tr>
                            <th style="width: 15%;">Level</th>
                            <th>Message</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="$logs">
                            <xsl:variable name="stripeClass">
                                <xsl:choose>
                                    <xsl:when test="position() mod 2 = 0">
                                        <xsl:value-of select="'even'" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="'odd'" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:variable name="levelColor">
                                <xsl:choose>
                                    <xsl:when test="@level = 'ERROR'">
                                        <xsl:value-of select="'#c0392b'" />
                                    </xsl:when>
                                    <xsl:when test="@level = 'WARN'">
                                        <xsl:value-of select="'#d35400'" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="'#27ae60'" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <tr class="{$stripeClass}">
                                <td class="value text" style="color: {$levelColor}; font-weight: bold;"><xsl:value-of select="@level"/></td>
                                <td class="value text"><pre style="margin: 0; white-space: pre-wrap; font-family: inherit;"><xsl:value-of select="."/></pre></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div><!-- /data -->
        </div><!-- /content -->
    </div><!-- /section -->

</xsl:template>

</xsl:stylesheet>
