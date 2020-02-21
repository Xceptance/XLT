<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="filtered-footer-row">
        <!-- filtered totals (will be made visible when a filter is applied)  -->
        <tr class="totals filtered" style="display: none;">
            <!-- the contents will be created dynamically (xlt.js, function recalculateFilteredFooter) -->
        </tr>
    </xsl:template>

</xsl:stylesheet>