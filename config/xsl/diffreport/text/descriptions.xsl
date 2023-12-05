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
