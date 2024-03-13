<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!--
    This file contains the description sections for the report as well as
    the headlines for the section.
    
    The format is always <h2> as main headline, <h3> as description headline 
    and some <p> or <ul> as text.
-->

<!--- ## Description: General ## -->
<xsl:template name="headline-general">
    <h2>Overview</h2>
</xsl:template>
<xsl:template name="description-general">
    <div class="description"> 
    	<xsl:variable name="gid" select="concat('overview', generate-id(.))"/>  
        <p>
            The overview section shows some general information about both load tests. This enables you to compare settings, 
            runtime, and profiles. 
            <xsl:call-template name="show-n-hide">
            	<xsl:with-param name="gid" select="$gid"/>
            </xsl:call-template>
        </p>
        <div id="more-{$gid}" class="more">
	        <p>   
	            The initial test run is labeled <em>baseline</em>. The test run that is compared to the baseline is 
	            labeled <em>measurement run</em>.
	        </p>
	        <p>
	        	In the later sections, the percentage values show you the development of the performance in comparison to the baseline. 
	        	Please note that the total columns (total throughput and total errors) might present misleading values 
	        	if the two load tests used different runtime configurations. All other values are normalized 
	        	in respect to the runtime and therefore easily comparable. Positive numbers in the count section mean an improvement 
	        	of the throughput over the baseline. Negative values indicate a decrease of throughput. An increase in the number of errors is 
	        	indicated with positive numbers, while a decrease in errors is shown in negative numbers. 
	        	An infinite sign indicates the occurrence of errors in comparison to an error-free baseline.<br/>
	        	For all runtime numbers, positive values indicate a poorer performance, while negative value show an improvement - 
	        	smaller runtime values - over the baseline.
	        </p>	 
	        <p>
	        	Added or removed transactions, actions, or requests are indicated. No comparison is provided for these.
	        </p>
        </div>
    </div>
</xsl:template>

<!-- ## Headline for Report1 -->
<xsl:template name="headline-general-report1">
    <h3 id="anchor-general-report1">Baseline</h3>
</xsl:template>

<!-- ## Headline for Report2 -->
<xsl:template name="headline-general-report2">
    <h3 id="anchor-general-report2">Measurement Run</h3>
</xsl:template>

<!--- ## Description: Transaction Summary ## -->
<xsl:template name="headline-transaction-summary">
    <h2>Transactions</h2>
</xsl:template>
<xsl:template name="description-transaction-summary">
    <div class="description">   
    	<xsl:variable name="gid" select="concat('transaction', generate-id(.))"/>
        <p>
            A transaction is a completed test case, and a test case consists of one or more actions. 
            <xsl:call-template name="show-n-hide">
            	<xsl:with-param name="gid" select="$gid"/>
            </xsl:call-template>
        </p>
        <div id="more-{$gid}" class="more">
	        <p>
	            The runtime of a transaction contains the runtime of all actions within the test case, think 
	            times, and the processing time of the test code itself. If the test path of the test case is 
	            heavily randomized, the runtime of transactions might vary significantly. The average runtime 
	            shows the development of tests over time and is especially helpful in evaluating the outcome 
	            of long-running tests.
	        </p>
	        <p>*) numbers might be projected</p>
        </div>
    </div>
</xsl:template>

<!--- ## Description: Action Summary ## -->
<xsl:template name="headline-action-summary">
    <h2>Actions</h2>
</xsl:template>
<xsl:template name="description-action-summary">
    <div class="description">   
    	<xsl:variable name="gid" select="concat('action', generate-id(.))"/>
        <p>
            An action is part of a test case and consists of a prevalidation, an execution, and a postvalidation.
            <xsl:call-template name="show-n-hide">
                <xsl:with-param name="gid" select="$gid"/>
            </xsl:call-template> 
        </p>
        <div id="more-{$gid}" class="more">
            <p>
            While the prevalidation ensures that the necessary data is available for the execution of this test step, 
            the postvalidation evaluates the results and compares them to the expectations. 
            The data shown here is the time spent in the execution routine of an action. 
            Therefore its runtime includes the runtime of a request, e.g. an http operation, and 
            the necessary time to prepare, send, wait for, and receive the data.
	        </p>
	        <p>*) numbers might be projected</p>
        </div>
    </div>
</xsl:template>

<!--- ## Description: Request Summary ## -->
<xsl:template name="headline-request-summary">
    <h2>Requests</h2>
</xsl:template>
<xsl:template name="description-request-summary">
    <div class="description"> 
    	<xsl:variable name="gid" select="concat('request', generate-id(.))"/>  
        <p>
            The request section is the most important statistical section when testing web applications.
            <xsl:call-template name="show-n-hide">
                <xsl:with-param name="gid" select="$gid"/>
            </xsl:call-template> 
        </p>
        <div id="more-{$gid}" class="more">
	        <p>
	            It directly reflects the loading time of pages or page components. Each row holds the 
	            data of one specific request. Its name is defined within the test case as timer name. 
	            The <em>Count</em> section of the table shows the total number of executions 
	            (<em>Total</em>), the calculated executions per seconds (<em>1/s</em>) and per minute 
	            (<em>1/min</em>), as well as projections or calculations of the executions per hour 
	            (<em>1/h</em>) and day (<em>1/d</em>). The <em>Error</em> section shows the total number 
	            (<em>Total</em>) of errors that occurred during loading of the page or page component. 
	            The error count does not include errors detected during the postvalidation of the data 
	            received. Typical error situations are http-response codes such as 404 and 505, timeouts, 
	            or connection resets.
	        </p>
	        <p>
	            The runtime section of the table shows the median, the arithmetic mean, and the minimum 
	            and maximum runtime encountered, as well as the standard deviation of all data within 
	            that series. The runtime segmentation section shows several runtime segments and the 
	            number of requests within the segment's definition.
	        </p>
	        <p>
	            If the runtime of the test case is shorter than a displayed time period--for example if 
	            the test runtime was 30 minutes and the time period is in hours--the numbers are a linear 
	            projection. That means they show a possible outcome of a longer test run, assuming load 
	            and application behavior remain the same.
	        </p>
	        <p>*) numbers might be projected</p>
	        <p>**) Numbers are estimated using <a href="https://en.wikipedia.org/wiki/HyperLogLog">HyperLogLog</a> and can be off by up to 0.5%, but only for distinct counts larger than 100,000. A difference of up to 2%
                can occur for distinct counts larger than 1,000,000.</p>
        </div>
    </div>
</xsl:template>

<!--- ## Description: Page Load Timing Summary ## -->
<xsl:template name="headline-page-load-timing-summary">
    <h2>Page Load Timings</h2>
</xsl:template>
<xsl:template name="description-page-load-timing-summary">
    <div class="description">
        <p>
            This section offers a deeper insight into the page loading performance of real browsers. This
            data helps you to assess how fast a page is loaded from a human user's perspective.
        </p>
        <p>*) numbers might be projected</p>
    </div>
</xsl:template>

<!--- ## Description: Web Vitals Summary ## -->
<xsl:template name="headline-web-vitals-summary">
    <h2>Web Vitals</h2>
</xsl:template>
<xsl:template name="description-web-vitals-summary">
    <div class="description">
        <xsl:variable name="gid" select="concat('web-vitals', generate-id(.))"/>
        <p>
            The Web Vitals section helps you evaluate how well your pages perform on aspects that are important for a great user experience.
            <xsl:call-template name="show-n-hide">
                <xsl:with-param name="gid" select="$gid"/>
            </xsl:call-template>
        </p>
        <div id="more-{$gid}" class="more">
<p>
	<a href="https://web.dev/articles/vitals">Web Vitals</a> is
	a Google initiative to provide unified guidance for web page
	quality signals that are essential to delivering a great
	user experience on the web. It aims to simplify the wide
	variety of available performance-measuring tools, and help
	site owners focus on the metrics that matter most, the Core
	Web Vitals.</p>

<h4>Core Web Vitals</h4>

<p>
	<a href="https://web.dev/articles/lcp">Largest
		Contentful Paint</a> · <i>Perceived Load
		Speed</i><br/> LCP reports the render time of the
	largest image or text block visible in the viewport,
	relative to when the user first navigated to the page.</p>

<p>
	<a href="https://web.dev/articles/fid">First Input
		Delay</a> · <i>Interactivity</i><br/> FID measures the
	time from when a user first interacts with a page (that is,
	when they click a link, tap on a button, or use a custom,
	JavaScript-powered control) to the time when the browser is
	actually able to begin processing event handlers in response
	to that interaction.</p>

<p>
	<a href="https://web.dev/articles/cls">Cumulative
		Layout Shift</a> · <i>Visual Stability</i><br/> CLS is a
	measure of the largest burst of layout shift scores for
	every unexpected layout shift that occurs during the
	lifespan of a page.</p>

<h4>Other Web Vitals</h4>

<p>
	<a href="https://web.dev/articles/fcp">First
		Contentful Paint</a> · <i>Perceived Load
		Speed</i><br/> The FCP metric measures the time from
	when the user first navigated to the page to when any part
	of the page's content is rendered on the screen.</p>

<p>
	<a href="https://web.dev/articles/inp">Interaction to
		Next Paint</a> · <i>Interactivity</i><br/> INP is a
	metric that assesses a page's overall responsiveness to user
	interactions by observing the latency of all click, tap, and
	keyboard interactions that occur throughout the lifespan of
	a user's visit to a page. The final INP value is the longest
	interaction observed, ignoring outliers.</p>

<p>
	<a href="https://web.dev/articles/ttfb">Time to First
		Byte</a> · <i>Server Responsiveness</i><br/> TTFB is a
	metric that measures the time between the request for a
	resource and when the first byte of a response begins to
	arrive.</p>

<h4>Scores</h4>

<p>
	The displayed score value for a Web Vital is the 75th
	percentile (estimated) of all measurements in a given
	action. In addition, the scores are rated using Web
	Vital-specific thresholds and colorized accordingly
	(<span class="web-vital-score-good">good</span>,
	<span class="web-vital-score-improve">needs improvement</span>,
	<span class="web-vital-score-poor">poor</span>).
	</p>
</div>
     </div>
 </xsl:template>

<!--- ## Description: Custom Timer Summary ## -->
<xsl:template name="headline-custom-timer-summary">
    <h2>Custom Timers</h2>
</xsl:template>
<xsl:template name="description-custom-timer-summary">
    <div class="description">   
        <p>
            The custom timers includes all timers that have been placed individually within the test code. The chart and data description is identical to the request section.
        </p>
        <p>*) numbers might be projected</p>
    </div>
</xsl:template>

<!-- The show and hide part -->
<xsl:template name="show-n-hide">
    <xsl:param name="gid"/>
    <span id="more-{$gid}-show" onclick="$('#more-{$gid}').show();$('#more-{$gid}-hide').show(); $(this).hide();"
        class="link more-show">More...</span>
    <span id="more-{$gid}-hide" onclick="$('#more-{$gid}').hide();$('#more-{$gid}-show').show(); $(this).hide();"
        class="link more-hide">Hide...</span>
</xsl:template>

</xsl:stylesheet>
