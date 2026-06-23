<#ftl output_format="plainText">
<#--
    AI Summary Template — generates ai-summary.md
    Hybrid YAML+Markdown format optimized for LLM token efficiency.
-->
<#function v node>
    <#if node?is_sequence><#if node?size gt 0><#return node[0]?string?trim></#if><#return "-">
    <#else><#return node?string?trim>
    </#if>
</#function>
<#assign tr = report.testreport>
<#-- ===== Test Metadata ===== -->
<#if tr.general?has_content>
<#assign g = tr.general[0]>
# Test Metadata

```yaml
startTime: "${v(g.startTime)}"
endTime: "${v(g.endTime)}"
duration: ${v(g.duration)}
bytesSent: ${v(g.bytesSent)}
bytesReceived: ${v(g.bytesReceived)}
hits: ${v(g.hits)}
```

</#if>
<#-- ===== Configuration ===== -->
<#if tr.configuration?has_content>
<#assign cfg = tr.configuration[0]>
<#if cfg.version?has_content>
# XLT Version

```yaml
product: "${v(cfg.version[0].productName)}"
version: "${v(cfg.version[0].version)}"
```

</#if>
<#if cfg.projectName?has_content>
<#assign pn = v(cfg.projectName)>
<#if pn != "">
# Project

```yaml
name: "${pn}"
```

</#if>
</#if>
<#if cfg.comments?has_content && cfg.comments[0].string?has_content>
# Comments

<#list cfg.comments[0].string as c>
- ${v(c)}
</#list>

</#if>
<#if cfg.testCases?has_content && cfg.testCases[0].testCase?has_content>
# Load Profile

| Test Case | Users | Iterations | Measurement [s] | Ramp-Up [s] | Shutdown [s] |
| --- | ---: | ---: | ---: | ---: | ---: |
<#list cfg.testCases[0].testCase as tc>
| ${v(tc.userName!tc.testCaseClassName)} | ${v(tc.numberOfUsers)} | ${v(tc.numberOfIterations)} | ${v(tc.measurementPeriod)} | ${v(tc.rampUpPeriod)} | ${v(tc.shutdownPeriod)} |
</#list>

</#if>
</#if>
<#-- ===== Macro: Timer Table ===== -->
<#macro timer_table elements>
<#local pcts = []>
<#if elements?size gt 0 && elements[0].percentiles?has_content && elements[0].percentiles[0]?children?size gt 0>
<#local pcts = elements[0].percentiles[0]?children?filter(c -> c?node_type == "element")>
</#if>
| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev<#list pcts as p> | P${p?node_name?remove_beginning("p")}</#list> |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---:<#list pcts as p> | ---:</#list> |
<#list elements as el>
| ${v(el.name)} | ${v(el.count)} | ${v(el.countPerSecond)} | ${v(el.errors)} | ${v(el.errorPercentage)} | ${v(el.min)} | ${v(el.max)} | ${v(el.mean)} | ${v(el.median)} | ${v(el.deviation)}<#if el.percentiles?has_content && el.percentiles[0]?children?size gt 0><#list el.percentiles[0]?children?filter(c -> c?node_type == "element") as p> | ${v(p)}</#list></#if> |
</#list>

</#macro>
<#-- ===== Transactions ===== -->
<#if tr.transactions?has_content>
<#assign txns = tr.transactions[0]?children?filter(c -> c?node_type == "element")>
<#if txns?size gt 0>
# Transactions

<@timer_table elements=txns />
</#if>
</#if>
<#-- ===== Actions ===== -->
<#if tr.actions?has_content>
<#assign acts = tr.actions[0]?children?filter(c -> c?node_type == "element")>
<#if acts?size gt 0>
# Actions

<@timer_table elements=acts />
</#if>
</#if>
<#-- ===== Requests ===== -->
<#if tr.requests?has_content>
<#assign reqs = tr.requests[0]?children?filter(c -> c?node_type == "element")>
<#if reqs?size gt 0>
<#assign pcts = []>
<#if reqs[0].percentiles?has_content && reqs[0].percentiles[0]?children?size gt 0>
<#assign pcts = reqs[0].percentiles[0]?children?filter(c -> c?node_type == "element")>
</#if>
# Requests

| Name | Count | Count/s | Errors | Error% | Min | Max | Mean | Median | Dev<#list pcts as p> | P${p?node_name?remove_beginning("p")}</#list> | DNS | Connect | Send | ServerBusy | Receive | TTFB | BytesSent | BytesRecv |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---:<#list pcts as p> | ---:</#list> | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
<#list reqs as el>
| ${v(el.name)} | ${v(el.count)} | ${v(el.countPerSecond)} | ${v(el.errors)} | ${v(el.errorPercentage)} | ${v(el.min)} | ${v(el.max)} | ${v(el.mean)} | ${v(el.median)} | ${v(el.deviation)}<#if el.percentiles?has_content && el.percentiles[0]?children?size gt 0><#list el.percentiles[0]?children?filter(c -> c?node_type == "element") as p> | ${v(p)}</#list></#if> | ${(el.dnsTime?has_content)?then(v(el.dnsTime[0].mean), "-")} | ${(el.connectTime?has_content)?then(v(el.connectTime[0].mean), "-")} | ${(el.sendTime?has_content)?then(v(el.sendTime[0].mean), "-")} | ${(el.serverBusyTime?has_content)?then(v(el.serverBusyTime[0].mean), "-")} | ${(el.receiveTime?has_content)?then(v(el.receiveTime[0].mean), "-")} | ${(el.timeToFirstBytes?has_content)?then(v(el.timeToFirstBytes[0].mean), "-")} | ${(el.bytesSent?has_content)?then(v(el.bytesSent[0].mean), "-")} | ${(el.bytesReceived?has_content)?then(v(el.bytesReceived[0].mean), "-")} |
</#list>

</#if>
</#if>
<#-- ===== Page Load Timings ===== -->
<#if tr.pageLoadTimings?has_content>
<#assign plts = tr.pageLoadTimings[0]?children?filter(c -> c?node_type == "element")>
<#if plts?size gt 0>
# Page Load Timings

<@timer_table elements=plts />
</#if>
</#if>
<#-- ===== Custom Timers ===== -->
<#if tr.customTimers?has_content>
<#assign cts = tr.customTimers[0]?children?filter(c -> c?node_type == "element")>
<#if cts?size gt 0>
# Custom Timers

<@timer_table elements=cts />
</#if>
</#if>
<#-- ===== Custom Values ===== -->
<#if tr.customValues?has_content>
<#assign cvs = tr.customValues[0]?children?filter(c -> c?node_type == "element")>
<#if cvs?size gt 0>
# Custom Values

| Name | Count | Count/s | Min | Max | Mean | StdDev |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
<#list cvs as cv>
| ${v(cv.name)} | ${v(cv.count)} | ${v(cv.countPerSecond)} | ${v(cv.min)} | ${v(cv.max)} | ${v(cv.mean)} | ${v(cv.standardDeviation)} |
</#list>

</#if>
</#if>
<#-- ===== Errors ===== -->
<#if tr.errors?has_content>
<#assign errs = tr.errors[0]?children?filter(c -> c?node_type == "element")>
# Errors

<#if errs?size == 0>
No errors recorded.
<#else>
<#list errs as err>
## Error: ${v(err.message)}

- **Test Case**: ${v(err.testCaseName)}
- **Action**: ${(err.actionName?has_content)?then(v(err.actionName), "n/a")}
- **Count**: ${v(err.count)}
<#if err.trace?has_content>
<#assign traceText = v(err.trace)>
<#if traceText?has_content && traceText != "-">
- **Stack Trace**:
```
${(traceText?length gt 1000)?then(traceText?substring(0, 1000) + "...", traceText)}
```
</#if>
</#if>

</#list>
</#if>
</#if>
<#-- ===== Events ===== -->
<#if tr.events?has_content>
<#assign evts = tr.events[0]?children?filter(c -> c?node_type == "element")>
<#if evts?size gt 0>
# Events

| Test Case | Event Name | Count |
| --- | --- | ---: |
<#list evts as evt>
| ${(evt.testCaseName?has_content)?then(v(evt.testCaseName), "n/a")} | ${v(evt.name)} | ${v(evt.totalCount)} |
</#list>

</#if>
</#if>
<#-- ===== Agents ===== -->
<#if tr.agents?has_content>
<#assign agts = tr.agents[0]?children?filter(c -> c?node_type == "element")>
<#if agts?size gt 0>
# Agents

| Agent | Transactions | Errors | Error% | CPU% (Mean) |
| --- | ---: | ---: | ---: | ---: |
<#list agts as agt>
| ${v(agt.name)} | ${v(agt.transactions)} | ${v(agt.transactionErrors)} | ${v(agt.transactionErrorPercentage)} | ${(agt.cpuUsage?has_content)?then(v(agt.cpuUsage[0].mean), "n/a")} |
</#list>

</#if>
</#if>
<#-- ===== Web Vitals ===== -->
<#if tr.webVitalsList?has_content>
<#assign wvs = tr.webVitalsList[0]?children?filter(c -> c?node_type == "element")>
<#if wvs?size gt 0>
# Web Vitals

| Action | CLS Score | CLS Rating | FCP Score | FCP Rating | LCP Score | LCP Rating | INP Score | INP Rating | TTFB Score | TTFB Rating |
| --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- | ---: | --- |
<#list wvs as wv>
| ${v(wv.name)} | ${(wv.cls?has_content)?then(v(wv.cls[0].score), "-")} | ${(wv.cls?has_content)?then(v(wv.cls[0].rating), "-")} | ${(wv.fcp?has_content)?then(v(wv.fcp[0].score), "-")} | ${(wv.fcp?has_content)?then(v(wv.fcp[0].rating), "-")} | ${(wv.lcp?has_content)?then(v(wv.lcp[0].score), "-")} | ${(wv.lcp?has_content)?then(v(wv.lcp[0].rating), "-")} | ${(wv.inp?has_content)?then(v(wv.inp[0].score), "-")} | ${(wv.inp?has_content)?then(v(wv.inp[0].rating), "-")} | ${(wv.ttfb?has_content)?then(v(wv.ttfb[0].score), "-")} | ${(wv.ttfb?has_content)?then(v(wv.ttfb[0].rating), "-")} |
</#list>

</#if>
</#if>
