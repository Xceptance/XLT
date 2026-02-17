<#import "macros.ftl" as macros>

<#macro headline_general>
    <h2>Overview</h2>
</#macro>

<#macro description_general>
    <div class="description">   
        <p>
            A trend report shows the development of performance over time. 
        </p>
        <p>
            Multiple measurements are taken into account and evaluated against each other. 
            An XLT trend reports shows you, how your system performs over time, how your tuning effort pays out, and how your live environment acts under
            changing load situation, if used as monitoring. Two trend report types are available: <em>Difference to Base Run</em> 
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
</#macro>

<#macro headline_transaction_summary>
    <h2>Transactions</h2>
</#macro>

<#macro description_transaction_summary gid>
    <div class="description"> 
        <p>
            A transaction is a completed test case. The test case consists of one or more actions.
            <@macros.show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                The run time of a transaction contains the runtime of all actions within the test case, 
                thinktimes, and the processing time of the test code itself. If the test path of the test 
                case is heavily randomized, the runtime of transactions might vary significantly. The 
                average runtime shows the development of tests over time and especially helps to evaluate 
                the outcome of long running tests.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_action_summary>
    <h2>Actions</h2>
</#macro>

<#macro description_action_summary gid>
    <div class="description">  
        <p>
            An action is part of a test case and consists of a prevalidation, an execution, and a postvalidation part.
            <@macros.show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p> 
                While the prevalidation ensures, that the necessary data is available for the execution of this test step, 
                the postvalidation evaluates the results and matches it against expectations. The data shown here is the 
                time spent in the execution routine of an action. Therefore its runtime includes the runtime of a request, 
                e.g. an http operation, and the necessary time to prepare, sent, wait, and receive the data.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_request_summary>
    <h2>Requests</h2>
</#macro>

<#macro description_request_summary gid>
    <div class="description"> 
        <p>
            The request section is the most important statistical section when testing web applications.
            <@macros.show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>  
                It directly reflects the loading time of pages or page components.
                The runtime section of the table shows the median, the arithmetic mean, as well as the 
                minimum and maximum runtime encountered of all data within that series. Additionally the error count
                is visualized to permit a better evaluation of the test run's quality.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_page_load_timing_summary>
    <h2>Page Load Timings</h2>
</#macro>

<#macro description_page_load_timing_summary>
    <div class="description">   
        <p>
            This section offers a deeper insight into the page loading performance of real browsers. This
            data helps you to assess how fast a page is loaded from a human user's perspective.
        </p>
    </div>
</#macro>

<#macro headline_custom_timer_summary>
    <h2>Custom Timers</h2>
</#macro>

<#macro description_custom_timer_summary>
    <div class="description">   
        <p>
            The custom timers includes all timers that have been placed individually within 
            the test code. Chart and data descriptions are identical to the request section.
        </p>
    </div>
</#macro>

<#macro table_headline_absolute>
    Difference to Baseline
</#macro>

<#macro table_headline_relative>
    Difference to Previous Run
</#macro>

<#macro table_headline_absolute_base>
    Baseline
</#macro>

<#macro table_headline_relative_base>
    First
</#macro>
