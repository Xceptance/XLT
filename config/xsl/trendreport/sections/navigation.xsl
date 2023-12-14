<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template name="navigation">

		<nav>
			<input class="hamburger-btn" type="checkbox" id="hamburger-btn" />
			<label class="hamburger-icon" for="hamburger-btn"><span class="navicon"></span></label>
			<ul class="nav-menu">
				<li><a href="index.html">Overview</a></li>
                <li><a href="transactions.html">Transactions</a></li>
                <li><a href="actions.html">Actions</a></li>
                <li><a href="requests.html">Requests</a></li>
                <li><a href="page-load-timings.html">Page Load Timings</a></li>
                <li><a href="custom-timers.html">Custom Timers</a></li>
			</ul>
		</nav>
    
    </xsl:template>

</xsl:stylesheet>
