<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="navigation">
    <xsl:param name="current" />

	<xsl:text disable-output-escaping="yes">
	<![CDATA[
		<nav>
		    <ul>
				<li><a href="#general" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Overview</a></li>
				<li><a href="#anchor-general-report1" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Baseline Profile</a></li>
				<li><a href="#anchor-general-report2" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Measurement Profile</a></li>
				<li><a href="#transaction-summary" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Transactions</a></li>
				<li><a href="#action-summary" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Actions</a></li>
				<li><a href="#request-summary" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Requests</a></li>
				<li><a href="#page-load-timing-summary" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Page Load Timings</a></li>
                <li><a href="#custom-timer-summary" onclick="$.scrollTo( $(this).attr('href'), 250, {easing:'swing', offset: {top: -25}}); return false;">Custom Timers</a></li>
		    </ul>
		</nav>
	]]>
	</xsl:text>
</xsl:template>

</xsl:stylesheet>
