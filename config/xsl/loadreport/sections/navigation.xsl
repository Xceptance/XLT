<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template name="navigation">
    <xsl:param name="current" />

	<xsl:text disable-output-escaping="yes">
	<![CDATA[

		<nav>
			<ul>
				<li><a href="index.html">Overview</a>
					<ul>
						<li><a href="index.html#load-profile">Load Profile</a></li>
						<li><a href="index.html#comment">Test Comment</a></li>
						<li><a href="index.html#general">General Information</a></li>
                        <li><a href="index.html#summary">Performance Summary</a></li>
						<li><a href="index.html#network">Network</a></li>
					</ul>
				</li>
				<li><a href="transactions.html">Transactions</a></li>
				<li><a href="actions.html">Actions</a></li>
				<li><a href="requests.html">Requests</a></li>
                <li><a href="network.html">Network</a>
                    <ul>
                        <li><a href="network.html#network">Network</a></li>
                        <li><a href="network.html#hosts">Hosts</a></li>
                        <li><a href="network.html#http-request-methods">HTTP Request Methods</a></li>
                        <li><a href="network.html#http-response-codes">HTTP Response Codes</a></li>
                        <li><a href="network.html#content-types">Content Types</a></li>
                    </ul>
                </li>
				<li><a href="page-load-timings.html">Page Load Timings</a></li>
                <li><a href="custom-timers.html">Custom Timers</a></li>
                <li><a href="custom-values.html">Custom Values</a></li>
				<li><a href="external.html">External Data</a></li>
				<li><a href="errors.html">Errors</a>
					<ul>
						<li><a href="errors.html#request-errors">Request Errors</a></li>
                        <li><a href="errors.html#transaction-error-overview">Transaction Error Overview</a></li>
						<li><a href="errors.html#transaction-error-details">Transaction Error Details</a></li>
					</ul>
				</li>
                <li><a href="events.html">Events</a>
                    <ul>
                        <li><a href="events.html#event-overview">Overview</a></li>
                        <li><a href="events.html#event-details">Details</a></li>
                    </ul>
                </li>
                <li><a href="agents.html">Agents</a></li>
				<li><a href="configuration.html">Configuration</a>
					<ul>
						<li><a href="configuration.html#load-profile">Load Profile</a></li>
						<li><a href="configuration.html#configuration">Settings</a></li>
					</ul>
				</li>
			</ul>
		</nav>
	]]>
	</xsl:text>
</xsl:template>

</xsl:stylesheet>
