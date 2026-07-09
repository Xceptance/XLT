<#macro headline_load_profile>
    <h2>Load Profile</h2>
</#macro>
<#macro description_load_profile>
    <div class="description">
        <p>
            This section shows the load profile as configured in the properties files of your test suite.
        </p>
    </div>
</#macro>

<#macro headline_testcomment>
    <h2>Test Comment</h2>
</#macro>

<#macro headline_general>
    <h2>General Information</h2>
</#macro>
<#macro description_general>
    <div class="description">
        <p>
            Below you find basic characteristics of the load test: start time, end time, and total duration. The charts
            depict the number of concurrent users, requests per second, the request runtime, and the number of errors per
            second.
        </p>
    </div>
</#macro>

<#macro headline_agent_summary>
    <h2>Agent Summary</h2>
</#macro>
<#macro description_agent_summary>
    <div class="description">
        <p>
            The Agent Summary shows you the CPU usage status of all your agent machines. This allows you to quickly see if there are any problems that may or may not have negatively affected the load test result.
        </p>
    </div>
</#macro>

<#macro headline_summary>
    <h2>Performance Summary</h2>
</#macro>
<#macro description_summary>
    <div class="description">
        <p>
            To help you evaluate the test results quickly and easily, this table gathers general performance characteristics:
            the traffic volume the test created, the number of errors and events, and runtime numbers.
        </p>
    </div>
</#macro>

<#macro headline_network_summary>
    <h2>Network</h2>
</#macro>
<#macro description_network_summary>
    <div class="description">
        <@description_network gid="networkd525e1" />
    </div>
</#macro>

<#macro description_network gid="networkd525e1">
    <div class="description">
        <p>
            See below for data on the incoming and outgoing traffic during the load test.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                The data is captured right above the network layer, thus it comprises all traffic information including
                potential overhead due to encryption as well as savings due to compression. Depending on the test runtime,
                the numbers per hour and per day may be estimates based on a linear projection of the available data.
            </p>
            <@numbers_projected />
        </div>
    </div>
</#macro>

<#function ensureNumber node default="NaN">
    <#local val = (node[0]!"")?string?trim>
    <#if val?has_content && val != "NaN">
        <#-- Remove commas if present in the XML value (though usually they aren't) -->
        <#return val?replace(",", "")?number>
    </#if>
    <#return default>
</#function>

<#function formatNumber val pattern="#,##0">
    <#if val?is_number>
        <#return val?string(pattern)>
    </#if>
    <#return val?string>
</#function>

<#macro show_n_hide gid>
    <span id="more-${gid}-show" onclick="$('#more-${gid}').show();$('#more-${gid}-hide').show(); $(this).hide();" class="link more-show">More...</span>
    <span id="more-${gid}-hide" onclick="$('#more-${gid}').hide();$('#more-${gid}-show').show(); $(this).hide();" class="link more-hide">Hide...</span>
</#macro>

<#macro numbers_projected>
    <p>*) Numbers may be projected</p>
</#macro>

<#macro charts_explained>
    <p>
        Please note that the charts display compressed information which means that any kind of information has been mapped
        to chart pixels. Such a pixel can stand for any time period, depending on the total test duration. Therefore, the
        information you see is taken from several events within that time period and signifies a certain value range,
        defined by the minimum and the maximum value measured.
    </p>
    <p>
        If the upper border of a chart is highlighted, the chart display has been capped.
    </p>
</#macro>

<#macro headline_transaction_summary>
    <h2>Transactions</h2>
</#macro>
<#macro description_transaction_summary>
    <div class="description">
        <#local gid = "transactiond25e1">
        <p>
            A transaction is an executed and completed test case. Each test case consists of one or more actions.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                The runtime of a transaction includes the runtime of all actions within that particular test case, the think
                times, and the processing time of the test code itself. If the test path or the test case is heavily
                randomized, the runtime of transactions over time may vary significantly.
            </p>
            <p>
                The Summary chart section comprises all data across transactions to allow for a quick overview on transaction
                problems and performance. The tabs in this chart section contain single charts that refer to one
                transaction.
            </p>
            <p>
                In the following, all chart sections below visualize this information:
            </p>
            <ul>
                <li>Overview: moving average and individual transactions</li>
                <li>Averages: median, mean, and moving average only</li>
                <li>Counts/s: number of finished transactions per second</li>
                <li>Arrival Rate: mapping of the count/s number on a possible arrival rate to reflect how much load is being
                    generated if the same load persisted</li>
                <li>Concurrent Users: number of running transactions, usually the concurrent user count</li>
            </ul>

            <@charts_explained />
            <@numbers_projected />
        </div>
    </div>
</#macro>

<#macro headline_action_summary>
    <h2>Actions</h2>
</#macro>
<#macro description_action_summary>
    <div class="description">
        <#local gid = "actiond0e0">
        <p>
            An action is part of a transaction and usually consists of one or more requests. When testing web applications,
            an action resembles a page view.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                The data shown here reflects the time spent on the execution routine of an action. Therefore, the action's
                runtime includes the runtime of a request (an HTTP operation, for instance) and the time required to
                prepare, send, wait for, and receive the data. If JavaScript is activated, these times are part of the
                measurement as well. If you actively wait for elements to appear or if you add pauses, the resulting waiting
                times are also included.
            </p>
            <p>
                The error count defines the number of errors detected during the validation of the page or errors that
                occured during loading, such as timeouts or connection resets. See the Request Section for further details
                on connection resets.
            </p>

            <p>
                For each action the corresponding
                <em>Apdex</em>
                value is listed. The Apdex, short for Application Performance Index, is a measure to express the degree of
                user satisfaction with the performance of a certain operation (see
                <a href="https://en.wikipedia.org/wiki/Apdex">Wikipedia</a>
                or the
                <a href="http://www.apdex.org/">Apdex Alliance</a>
                for more information). The general format of an Apdex value is
                <em>P [T]</em>
                (for instance: 0.75 [2.5]).
                <em>P</em>
                is the degree of user satisfaction in percent, represented by a number between 0 and 1.
                <em>T</em>
                is the configurable threshold in seconds for the runtime of a certain
                operation above which the user is
                usually no longer satisfied with the performance. See below for the resulting ratings based on P and how
                they are color-coded:
            </p>
            <ul>
                <li>
                    <span class="apdex-excellent">&#x2003;
                    </span>
                    <#local nbsp = "&#x2003;">
                    ${nbsp?no_esc}0.94...1.00 / Excellent
                </li>
                <li>
                    <span class="apdex-good">&#x2003;
                    </span>
                    ${nbsp?no_esc}0.85...0.93 / Good
                </li>
                <li>
                    <span class="apdex-fair">&#x2003;
                    </span>
                    ${nbsp?no_esc}0.70...0.84 / Fair
                </li>
                <li>
                    <span class="apdex-poor">&#x2003;
                    </span>
                    ${nbsp?no_esc}0.50...0.69 / Poor
                </li>
                <li>
                    <span class="apdex-unacceptable">&#x2003;
                    </span>
                    ${nbsp?no_esc}0.00...0.49 / Unacceptable
                </li>
            </ul>

            <p>
                The Summary chart section comprises all data across actions to allow for a quick overview on action problems
                and performance. The tabs in this chart section contain single charts that refer to one action.
            </p>
            <p>
                In the following, all chart sections below visualize this information:
            </p>
            <ul>
                <li>Overview: moving average and individual actions</li>
                <li>Averages: median, mean, and moving average only</li>
                <li>Counts/s: number of finished actions per second</li>
            </ul>

            <@charts_explained />
            <@numbers_projected />
        </div>
    </div>
</#macro>

<#macro headline_request_summary>
    <h2>Requests</h2>
</#macro>
<#macro description_request_summary>
    <div class="description">
        <#local gid = "requestd0e0">
        <p>
            The Request section is the most important statistics section when testing web applications. It directly reflects
            the loading time of pages or page components.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <h3>Overview</h3>
            <p>
                Each row in the Overview tab displays the data for one specific request. Its name is derived from the action
                name and may have been modified using request filter and transformation rules.
            </p>
            <p>
                When hovering over the request name, a pop-up will show how many distinct URLs have been summarized under
                this name and will list up to 10 examplary URLs. Note that any fragment part (the "hash") has been stripped
                from the URL before calculating that data as the fragment part is not transferred to the server anyway.
            </p>
            <p>
                The Count column of the table shows the total number of executions (Total), the calculated executions per
                second (1/s) and per minute (1/min) as well as projections or calculations of the executions per hour (1/h)
                and per day (1/d).
            </p>
            <p>
                The Errors column shows the total number of errors that occurred during the loading of a request (Total). The
                error count does not include errors detected during the post validation of the received data. Typical error
                situations are HTTP response codes indicating server-side errors (5xx), timeouts, or connection resets.
            </p>
            <p>
                The Runtime column shows the median and the arithmetic mean, the minimum and maximum runtime encountered, and
                the standard deviation of all data within that series. The Runtime Segmentation shows several runtime
                segments and the number of requests within the segment’s definition.
            </p>
            <p>
                If the runtime of the test case is shorter than a displayed time period, for instance, if the test runtime
                was 30 minutes and the time period is given in hours, then the numbers are a linear projection. That means
                they show a possible outcome of a longer test run, assuming load and application behavior remain the same.
            </p>
            <p>
                The Runtime Segmentation column helps you evaluate SLA and test success definitions. You can change the setup
                in the report generator properties.
            </p>

            <h3>Bandwidth</h3>
            <p>
                The Bandwidth tab shows the incoming and outgoing bandwidth per request. XLT measures on socket level. Thus,
                actual incoming and outgoing data is recorded. XLT does not analyze or modify that data when taking the
                measurements.
            </p>
            <p>
                The Bytes Sent column comprises all data sent out of the application including overhead such as HTTP(S)
                headers and SSL protocol data. The Bytes Received column includes all received data and the connected
                overhead. As the data is measured right above the socket level and before it gets decoded by the
                application, any compressed traffic is measured as seen by the network layer. The data size you see is not
                the expanded data as seen by the DOM parser.
            </p>

            <h3>Network Timing</h3>
            <p>
                The Network Timing tab displays all low-level network timing data that has been measured on socket level.
                Each measurement point includes the minimum and maximum times and the mean of all gathered data. The
                following measurement points are shown:
            </p>
            <ul>
                <li>
                    Connect Time: Time needed to establish a connection to the other system. Note that when you use
                    keep-alive semantics during testing, the connect time will mainly be 0 except for the first request of a
                    transaction.
                </li>
                <li>
                    Send Time: Time needed to send the request to the other system. Depending on the payload and the network
                    speed, this data often amounts to zero or very small values.
                </li>
                <li>
                    Server Busy Time: Time spent waiting from sending the last bytes to receiving the first bytes.
                </li>
                <li>
                    Receive Time: Time spent receiving data from the first to the last bytes received.
                </li>
                <li>
                    Time to First Bytes: Total time from the connection start until the first bytes are received. Includes
                    Connect, Send, Server Busy, and Receive Time.
                </li>
                <li>
                    Time to Last Bytes: Total time from the connection start until the last bytes are received. This is the
                    time needed to connect, send, and receive data. Often it is called network runtime. In contrast, the
                    request runtime includes the network runtime and the application time needed to process header and
                    protocol information and transfer the data from socket level to the application level.
                </li>
            </ul>

            <h3>Charts</h3>
            <p>
                The Summary charts comprise all data across requests to allow for a quick overview on request problems and
                performance. The tabs in this chart section contain single charts that refer to one request.
            </p>
            <p>
                In the following, all chart sections below visualize this information:
            </p>
            <ul>
                <li>Overview: moving average and individual requests</li>
                <li>Averages: median, mean, and moving average only</li>
                <li>Counts/s: number of finished requests per second</li>
                <li>Distribution: runtime segmentation</li>
                <li>Network: incoming traffic and moving average</li>
            </ul>

            <@charts_explained />
            <@numbers_projected />
            
            <p>**) Numbers are estimated using <a href="https://en.wikipedia.org/wiki/HyperLogLog">HyperLogLog</a> and can be off by up to 0.5%, but only for distinct counts larger than 100,000. A difference of up to 2%
            can occur for distinct counts larger than 1,000,000.</p>
        </div>
    </div>
</#macro>
<#macro headline_hosts>
    <h2>Hosts</h2>
</#macro>
<#macro description_hosts>
    <div class="description">
        <p>
            See below for a list of all hosts that have been used during the test.
        </p>
    </div>
</#macro>

<#macro headline_ips>
    <h2>IP Addresses</h2>
</#macro>
<#macro description_ips>
    <div class="description">
        <p>
            See below for a list of all IP addresses that have been used during the test.
        </p>
    </div>
</#macro>

<#macro headline_http_response_codes>
    <h2>HTTP Response Codes</h2>
</#macro>
<#macro description_http_response_codes>
    <div class="description">
        <#local gid = "httpresponse" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            This table lists HTTP response codes of web activities or other activities during testing that are based on HTTP
            communication.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                It shows the HTTP response code, a short explanation of what it signifies, the number of its occurrences, and
                its percentage in relation to the total number of response codes. 0 indicates the absence of HTTP responses
                – an event that occurs when the connection times out or could not be established.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_http_request_methods>
    <h2>HTTP Request Methods</h2>
</#macro>
<#macro description_http_request_methods>
    <div class="description">
        <#local gid = "httprequest" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            See below for a list of all HTTP request methods that have been used during the test.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                It shows the HTTP request method, the number of its occurrences, and its percentage in 
                relation to the total number of requests.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_content_types>
    <h2>Content Types</h2>
</#macro>
<#macro description_content_types>
    <div class="description">
        <#local gid = "contentType" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            See below for a list of all content types that have been returned during the test.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                An HTTP response received from the server typically specifies a content type which then indicates the type of
                data it carries. Next to the returned content type, the table includes its number of occurrences and the
                share of responses for each of these content types to give you a clue as to how the network traffic is
                composed content-wise.
            </p>
            <p>
                Please note that the table lists the content type announced in the respective response header and not the
                actual content type of the transferred data.
            </p>
        </div>
    </div>
</#macro>
<#macro headline_custom_timer_summary>
    <h2>Custom Timers</h2>
</#macro>
<#macro description_custom_timer_summary>
    <div class="description">
        <p>
            The custom timers include all timers that have been placed individually within the test code. The chart and data
            description is identical to the Actions section.
        </p>
        <@numbers_projected />
    </div>
</#macro>

<#macro headline_custom_values_summary>
    <h2>Custom Values</h2>
</#macro>
<#macro description_custom_values_summary>
    <div class="description">
        <p>
            The custom values include all values that have been recorded by your custom samplers.
        </p>
        <@numbers_projected />
    </div>
</#macro>

<#macro headline_custom_data_logs_summary>
    <h2>Custom Data Logs</h2>
</#macro>
<#macro description_custom_data_logs_summary>
    <div class="description">
        <p>
            The custom data logs contain custom data collected by test scripts.
        </p>
    </div>
</#macro>

<#macro headline_error_summary>
    <h2>Errors</h2>
</#macro>
<#macro description_error_summary>
    <div class="description">
        <#local gid = "errors" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            This section lists all errors that occurred during the load test. The data listed here helps you identify
            application errors or test problems.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                Errors are hard failures in the flow of testing. They stop the current execution of the test case (or
                transaction), log an error, and eventually move on to the next test execution if scheduled to do so.
            </p>
            <p>
                Errors can occur on transaction, action, or request level. Errors on the request level are counted on action
                level as well. Action level errors are also counted as transaction errors. A connection reset, for instance,
                counts as request error and causes the action to fail (counted). As a result, the transaction fails as well
                which again is counted and logged.
            </p>
            <p>
                The Overview section below lists the error message and the count. It ignores the stack trace to sum up common
                problems without relating them to the test case. The Details section beneath lists the full stack trace next
                to the test case and the directory in which you can find the data dump for further analysis.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_event_summary>
    <h2>Events</h2>
</#macro>
<#macro description_event_summary>
    <div class="description">
        <#local gid = "events" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            The tables below show all events that occurred during the load test. Events are used to indicate that the test
            has encountered a situation that is not an error but too important to ignore or to write to the log only. Events can
            also be used to report certain conditions during the load test.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                Events are soft errors or warnings that don't interrupt the flow of testing. Request-level errors also log an
                event because it depends on the test configuration if they turn into a hard failure as well.
            </p>
            <p>
                You can use the XLT API and create your own events to learn about certain conditions, such as an unavailable
                product that doesn't cause the test to stop but that needs your attention. A few out-of-stock products may
                be okay while too many of them could affect the test behavior.
            </p>
            <p>
                The Overview section below lists all events and their respective count in general. The Details section
                beneath lists and counts all events grouped by test case name, event name, and the particular event message
                (URLs, for example).
            </p>
            <p>
                In case of too many events, the XLT report will limit the number of collected data points and report dropped counts
                instead. You can use the reportgenerator.properties to control these limits. If the report displays "XLT::Dropped events due to bad naming",
                you might have used the name of an event to communicate dynamic data and XLT limited the data collection to avoid
                memory problems. You can raise the limit, in case you have a higher number of legit event names.
            </p>
        </div>
    </div>
</#macro>
<#macro headline_agents>
    <h2>Agent Information</h2>
</#macro>
<#macro description_agents>
    <div class="description">
        <#local gid = "agents" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            The Agent Information section reports mainly on the resource utilization of each user agent in terms of CPU and memory
            usage.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                It helps you identify potential resource bottlenecks that might have influenced the load test. Please note
                that all data is local to the Java Virtual Machine of the agent and therefore covers only the inner process
                view.
            </p>
        </div>
    </div>
</#macro>

<#macro headline_configuration>
    <h2>Load Test Settings</h2>
</#macro>
<#macro description_configuration>
    <div class="description">
        <p>
            See the table below for details on the test configuration used to run this test. It helps to make the test
            reproducible and preserves the test settings for later test evaluation.
        </p>
    </div>
</#macro>

<#macro headline_jvm_configuration>
    <h2>Agent JVM Settings</h2>
</#macro>
<#macro description_jvm_configuration>
    <div class="description">
        <p>
            This section lists custom JVM settings for the agents as specified in the <code>jvmargs.cfg</code> file of the test suite.
        </p>
    </div>
</#macro>

<#macro headline_slowest_requests_summary>
    <h2>Slowest Requests</h2>
</#macro>
<#macro description_slowest_requests_summary rootNode>
    <div class="description">
        <#local gid = "slowest-requests" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <#local config = rootNode.configuration.reportGeneratorConfiguration>
        
        <#local minRuntime = ensureNumber(config.slowestRequestsMinRuntime)>
        <#local maxRuntime = ensureNumber(config.slowestRequestsMaxRuntime)>
        <#local requestsPerBucket = ensureNumber(config.slowestRequestsPerBucket)>
        <#local totalRequests = ensureNumber(config.slowestRequestsTotal)>
        <p>
            This section lists the requests with the slowest runtime recorded during the test run.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <h3>Overview</h3>
            <p>
                The table lists the requests with the slowest overall runtime matching the following criteria (as configured
                in the reportgenerator.properties):
                <ul>
                    <li>Request runtime must be between ${formatNumber(minRuntime)} ms and
                        ${formatNumber(maxRuntime)} ms to be listed.</li>
                    <li>A maximum of ${formatNumber(requestsPerBucket)} requests per
                        request name are listed.</li>
                    <li>A total maximum of ${formatNumber(totalRequests)} requests are listed.</li>
                </ul>
            </p>
            <p>
                Each row in the table displays the data for one individual request. The request names are derived from
                the action names and may have been modified using request filter and transformation rules. When hovering
                over the request name, a pop-up will show further request information if available (e.g. the URL, HTTP method,
                or form data).
            </p>

            <h3>Baseline</h3>
            <p>
                The Baseline columns show the mean and p95 runtimes across all requests with the respective name to allow
                easy comparison with the runtime of the respective slow requests. These are the same mean and p95 values
                displayed in the Requests section.
            </p>

            <h3>Bandwidth</h3>
            <p>
                The Bandwidth columns show the incoming and outgoing bandwidth per request. XLT measures on socket level. Thus,
                actual incoming and outgoing data is recorded. XLT does not analyze or modify that data when taking the
                measurements.
            </p>
            <p>
                The Bytes Sent column comprises all data sent out of the application including overhead such as HTTP(S)
                headers and SSL protocol data. The Bytes Received column includes all received data and the connected
                overhead. As the data is measured right above the socket level and before it gets decoded by the
                application, any compressed traffic is measured as seen by the network layer. The data size you see is not
                the expanded data as seen by the DOM parser.
            </p>

            <h3>Network Timing</h3>
            <p>
                The Network Timing columns display all low-level network timing data that has been measured on socket level.
                The following measurement points are shown:
            </p>
            <ul>
                <li>
                    Connect Time: Time needed to establish a connection to the other system. Note that when you use
                    keep-alive semantics during testing, the connect time will mainly be 0 except for the first request of a
                    transaction.
                </li>
                <li>
                    Send Time: Time needed to send the request to the other system. Depending on the payload and the network
                    speed, this data often amounts to zero or very small values.
                </li>
                <li>
                    Server Busy Time: Time spent waiting from sending the last bytes to receiving the first bytes.
                </li>
                <li>
                    Receive Time: Time spent receiving data from the first to the last bytes received.
                </li>
                <li>
                    Time to First Bytes: Total time from the connection start until the first bytes are received. Includes
                    Connect, Send, Server Busy, and Receive Time.
                </li>
                <li>
                    Time to Last Bytes: Total time from the connection start until the last bytes are received. This is the
                    time needed to connect, send, and receive data. Often it is called network runtime. In contrast, the
                    request runtime includes the network runtime and the application time needed to process header and
                    protocol information and transfer the data from socket level to the application level.
                </li>
            </ul>

            <h3>Request Details</h3>
            <p>
                The Request Details columns show the HTTP response code and the start date and time of the request.
            </p>

            <h3>IP Addresses</h3>
            <p>
                The IP Addresses columns show the IP address used to perform the request, as well as all IP addresses
                reported by the DNS (if the information is available). If more than one address was reported, hover
                over the field to see a list of all the IP addresses.
            </p>

        </div>
    </div>
</#macro>

<#macro headline_page_load_timings_summary>
    <h2>Page Load Timings</h2>
</#macro>
<#macro description_page_load_timings_summary>
    <div class="description">
        <#local gid = "pageload" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            The Page Load Timings section offers a deeper insight into the page loading performance of real browsers. This
            data helps you to assess how fast a page is loaded from a human user's perspective.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
            <p>
                During a page load, a browser typically goes through different phases and reaches different states. This
                section outlines what time it took to reach a certain state. The timings listed here include primarily page
                load timings as defined in the
                <a href="https://www.w3.org/TR/navigation-timing-2/">Navigation Timing</a>
                specification. Since the
                <em>perceived</em>
                page loading performance is often influenced by how fast something is displayed on the page, paint timings
                are listed here as well. See the
                <a href="https://www.w3.org/TR/paint-timing/">Paint Timing</a>
                specification for more details.
            </p>
            <p>
                The following timings will be recorded:
            </p>
            <ul>
                <li>DomLoading (deprecated)</li>
                <li>DomInteractive</li>
                <li>DomComplete</li>
                <li>DomContentLoadedEventStart</li>
                <li>DomContentLoadedEventEnd</li>
                <li>LoadEventStart</li>
                <li>LoadEventEnd</li>
                <li>FirstPaint (Chrome only)</li>
                <li>FirstContentfulPaint (Chrome only)</li>
            </ul>
            <p>
                The individual timings for a page load are prefixed with the name of the action that triggered the page load.
                All timing values are relative to the start of the page load.
            </p>
            <p>
                Note that these timings will be recorded only when using
                <code>XltChromeDriver</code>
                or
                <code>XltFirefoxDriver</code>
                to run the browser. These are special WebDriver implementations that install an extension into the browser
                which is able to gather all the timings and report them to XLT.
            </p>
            <@numbers_projected />
        </div>
    </div>
</#macro>

<#macro headline_web_vitals_summary>
    <h2>Web Vitals</h2>
</#macro>
<#macro description_web_vitals_summary>
    <div class="description">
        <#local gid = "web-vitals" + .now?string["yyyyMMddHHmmss"] + (1000 + .now?string["SSS"]?number)?string["0"]>
        <p>
            The Web Vitals section helps you evaluate how well your pages perform on aspects that are important for a great user experience.
            <@show_n_hide gid=gid />
        </p>
        <div id="more-${gid}" class="more">
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
</#macro>

<#macro headline_external>
    <h2>External Data</h2>
</#macro>
<#macro description_external>
    <div class="description">
        <p>
            This section shows external data. Currently there is nothing to display here. Please see the manual on how to
            configure and write your own reports.
        </p>
    </div>
</#macro>

<#macro external_data_section_description description>
    <div class="description">
        <p>
            ${description}
        </p>
    </div>
</#macro>

<#macro description_external_summary>
    <div class="description">
        <p>
            This section shows external data. Currently there is nothing to display here. Please see the manual on how to
            configure and write your own reports.
        </p>
    </div>
</#macro>
