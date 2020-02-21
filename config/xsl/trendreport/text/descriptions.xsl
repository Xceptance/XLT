<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!--
    This file contains the description sections for the report as well as
    the headline for the section.
    
    The format is always <h2> as mean headline, <h3> as description headline 
    and some <p> or <ul> as  text.
-->

<!--- ## Description: General ## -->
<xsl:template name="headline-general">
    <h2>Overview</h2>
</xsl:template>

<xsl:template name="description-general">
    <div class="description">   
        <p>
            A trend report shows the development of performance over time. Multiple measurements are taken into account and evaluated against each other. 
            An XLT trend reports shows you, how your system performs over time, how your tuning effort pays out, and how your live environment acts under
            changing load situation, if used as monitoring.	Two trend report types are available: <em>Difference to Base Run</em> 
            and <em>Difference to Previous Run</em>. 
        </p>
        <p>
            The <em>Difference to Baseline</em> information visualizes the changes compared to your first test run, mostly referred to as baseline.
            Each table column will show you the difference between your baseline run and the run your are interested in. The quality of your baseline 
            run defines how valuable this report may be. You can also see this as long-term performance trend report. The baseline column lists the
            initial values for this data group.
        </p>
        <p>
        	The <em>Difference to Previous Run</em> section visualizes the improvements between two adjacent test runs. This shows you, how your last 
        	change or tuning effort payed out in comparison to the previous run. It helps you to see whether or not you are on the right track 
        	improving the performance of your application. The report also emphasizes sudden improvements or set-backs. This report can also be seen
        	as a short-term performance trend report. The first data column lists the initial measurement values and helps to improve the 
        	interpretation of the differences.
        </p>
        <p>
        	When you hover the mouse over the columns of the trend report table, you will see the actual measurement results. This will give you
        	a better impression whether or not the reported percentage change is significant or not. 
        </p>
        <p>
        	Please keep in mind, that changes up to 10% can be measurement fluctuations.
        </p>
    </div>
</xsl:template>

<!--- ## Description: Transaction Summary ## -->
<xsl:template name="headline-transaction-summary">
    <h2>Transactions</h2>
</xsl:template>

<xsl:template name="description-transaction-summary">
    <div class="description">   
        <p>
            A transaction is a completed test case. The test case consists of one or more actions. The run time of a transaction contains the runtime of all 
            actions within the test case, thinktimes, and the processing time of the test code itself. If the test path of the test case is heavily randomized, 
            the runtime of transactions might vary significantly. The average runtime shows the development of tests over time and especially 
            helps to evaluate the outcome of long running tests.
        </p>
    </div>
</xsl:template>

<!--- ## Description: Action Summary ## -->
<xsl:template name="headline-action-summary">
    <h2>Actions</h2>
</xsl:template>

<xsl:template name="description-action-summary">
    <div class="description">   
        <p>
            An action is part of a test case and consists of a prevalidation, an execution, and a postvalidation part.
            While the prevalidation ensures, that the necessary data is available for the execution of this test step, 
            the postvalidation evaluates the results and matches it against expectations. The data shown here is the 
            time spent in the execution routine of an action. Therefore its runtime includes the runtime of a request, 
            e.g. an http operation, and the necessary time to prepare, sent, wait, and receive the data.
        </p>
    </div>
</xsl:template>

<!--- ## Description: Request Summary ## -->
<xsl:template name="headline-request-summary">
    <h2>Requests</h2>
</xsl:template>

<xsl:template name="description-request-summary">
    <div class="description">   
        <p>
            The request section is the most important statistics section when testing web applications. 
            It directly reflects the loading  time of pages or page components.
            The runtime section of the table shows the median, the arithmetic mean, as well as the 
            minimum and maximum runtime encountered of all data within that series. Additionally the error count
            is visualized to permit a better evaluation of the test run's quality.
        </p>
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
    </div>
</xsl:template>

<!--- ## Description: Custom Timer Summary ## -->
<xsl:template name="headline-custom-timer-summary">
    <h2>Custom Timers</h2>
</xsl:template>

<xsl:template name="description-custom-timer-summary">
    <div class="description">   
        <p>
            The custom timers includes all timers that have been placed individually within 
            the test code. Chart and data descriptions are identical to the request section.
        </p>
    </div>
</xsl:template>

<!--- ## Description: Name for the table headline with absolute colors ## --> 
<xsl:template name="table-headline-absolute">
	<xsl:text>Difference to Baseline</xsl:text>
</xsl:template>

<!--- ## Description: Name for the table headline with relative colors ## --> 
<xsl:template name="table-headline-relative">
	<xsl:text>Difference to Previous Run</xsl:text>
</xsl:template>

<!--- ## Description: Name for the table headline that indicates the base run of the absolute data ## --> 
<xsl:template name="table-headline-absolute-base">
	<xsl:text>Baseline</xsl:text>
</xsl:template>

<!--- ## Description: Name for the table headline that indicates the first run of the relative data ## --> 
<xsl:template name="table-headline-relative-base">
	<xsl:text>First</xsl:text>
</xsl:template>

</xsl:stylesheet>
