<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<xsl:template name="issues">
    <xsl:param name="issues" />

    <div class="section" id="scorecard-issues">
        <h2>Evaluation Issues</h2>

        <div class="content">
            <div class="description">
                <p>The following non-fatal issues were encountered while evaluating rule check expressions. These checks were evaluated as errors, but did not prevent the rest of the scorecard from being calculated.</p>
            </div>

            <div class="data">
                <table class="no-auto-stripe">
                    <thead>
                        <tr>
                            <th style="width: 15%;">Severity</th>
                            <th style="width: 35%;">Location</th>
                            <th>Message</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="$issues">
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
                            <tr class="{$stripeClass}">
                                <td class="value text" style="color: #c0392b; font-weight: bold;"><xsl:value-of select="@severity"/></td>
                                <td class="value text"><xsl:value-of select="location"/></td>
                                <td class="value text"><xsl:value-of select="message"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </div><!-- /data -->
        </div><!-- /content -->
    </div><!-- /section -->

</xsl:template>

</xsl:stylesheet>
