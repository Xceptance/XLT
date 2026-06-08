/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report.scorecard.groovy;

/**
 * A helper class injected into the Groovy scorecard script as 'metrics'.
 * It provides a fluent way to generate XPath selector strings for the most common
 * XLT test report metrics, avoiding the need for test engineers to write raw XPath.
 */
public class MetricsHelper
{
    /**
     * Escapes single quotes in the regex to prevent XPath injection/syntax errors.
     */
    private String escapeRegex(String regex)
    {
        if (regex == null) return "";
        return regex.replace("'", "''");
    }

    /**
     * Constructs a generic aggregate XPath expression for a regex name match.
     */
    private String aggregateValue(String collectionName, String nodeName, String regex, String metricPath)
    {
        // Using max() safely aggregates the result if the regex matches multiple nodes.
        return String.format("max(//%s/%s[matches(name, '%s')]/%s)", 
                             collectionName, nodeName, escapeRegex(regex), metricPath);
    }

    /**
     * Constructs a generic aggregate XPath expression with arbitrary map conditions.
     */
    private String aggregateValue(String collectionName, String nodeName, java.util.Map<String, Object> args, String metricPath)
    {
        java.util.List<String> conditions = new java.util.ArrayList<>();
        
        if (args.containsKey("name")) {
            conditions.add(String.format("matches(name, '%s')", escapeRegex((String)args.get("name"))));
        }
        if (args.containsKey("excludeName")) {
            conditions.add(String.format("not(matches(name, '%s'))", escapeRegex((String)args.get("excludeName"))));
        }
        if (args.containsKey("label")) {
            conditions.add(String.format("labels = '%s'", escapeRegex((String)args.get("label"))));
        }
        if (args.containsKey("excludeLabel")) {
            conditions.add(String.format("labels != '%s'", escapeRegex((String)args.get("excludeLabel"))));
        }

        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one parameter: 'name', 'excludeName', 'label', or 'excludeLabel'");
        }

        String conditionString = String.join(" and ", conditions);

        return String.format("max(//%s/%s[%s]/%s)", collectionName, nodeName, conditionString, metricPath);
    }

    // =========================================================
    // Requests
    // =========================================================
    
    /**
     * Selects an arbitrary metric for a request.
     * @param regex The regex matching the request name(s)
     * @param metricPath The relative path to the metric (e.g. 'bytesSent/mean')
     */
    public String requestValue(String regex, String metricPath)
    {
        return aggregateValue("requests", "request", regex, metricPath);
    }

    public String requestValue(java.util.Map<String, Object> args, String metricPath)
    {
        return aggregateValue("requests", "request", args, metricPath);
    }

    public String requestP50(String regex) { return requestValue(regex, "percentiles/p50"); }
    public String requestP50(java.util.Map<String, Object> args) { return requestValue(args, "percentiles/p50"); }

    public String requestP95(String regex) { return requestValue(regex, "percentiles/p95"); }
    public String requestP95(java.util.Map<String, Object> args) { return requestValue(args, "percentiles/p95"); }

    public String requestP99(String regex) { return requestValue(regex, "percentiles/p99"); }
    public String requestP99(java.util.Map<String, Object> args) { return requestValue(args, "percentiles/p99"); }

    public String requestP99_9(String regex) { return requestValue(regex, "percentiles/p99.9"); }
    public String requestP99_9(java.util.Map<String, Object> args) { return requestValue(args, "percentiles/p99.9"); }

    public String requestMean(String regex) { return requestValue(regex, "mean"); }
    public String requestMean(java.util.Map<String, Object> args) { return requestValue(args, "mean"); }

    public String requestMedian(String regex) { return requestValue(regex, "median"); }
    public String requestMedian(java.util.Map<String, Object> args) { return requestValue(args, "median"); }

    public String requestMin(String regex) { return requestValue(regex, "min"); }
    public String requestMin(java.util.Map<String, Object> args) { return requestValue(args, "min"); }

    public String requestMax(String regex) { return requestValue(regex, "max"); }
    public String requestMax(java.util.Map<String, Object> args) { return requestValue(args, "max"); }
    
    public String requestErrors(String regex) { return requestValue(regex, "errors"); }
    public String requestErrors(java.util.Map<String, Object> args) { return requestValue(args, "errors"); }

    public String requestErrorPercentage(String regex) { return requestValue(regex, "errorPercentage"); }
    public String requestErrorPercentage(java.util.Map<String, Object> args) { return requestValue(args, "errorPercentage"); }

    public String requestCount(String regex) { return requestValue(regex, "count"); }
    public String requestCount(java.util.Map<String, Object> args) { return requestValue(args, "count"); }

    // =========================================================
    // Transactions
    // =========================================================
    
    /**
     * Selects an arbitrary metric for a transaction.
     */
    public String transactionValue(String regex, String metricPath)
    {
        return aggregateValue("transactions", "transaction", regex, metricPath);
    }

    public String transactionValue(java.util.Map<String, Object> args, String metricPath)
    {
        return aggregateValue("transactions", "transaction", args, metricPath);
    }

    public String transactionP50(String regex) { return transactionValue(regex, "percentiles/p50"); }
    public String transactionP50(java.util.Map<String, Object> args) { return transactionValue(args, "percentiles/p50"); }

    public String transactionP95(String regex) { return transactionValue(regex, "percentiles/p95"); }
    public String transactionP95(java.util.Map<String, Object> args) { return transactionValue(args, "percentiles/p95"); }

    public String transactionP99(String regex) { return transactionValue(regex, "percentiles/p99"); }
    public String transactionP99(java.util.Map<String, Object> args) { return transactionValue(args, "percentiles/p99"); }

    public String transactionP99_9(String regex) { return transactionValue(regex, "percentiles/p99.9"); }
    public String transactionP99_9(java.util.Map<String, Object> args) { return transactionValue(args, "percentiles/p99.9"); }

    public String transactionMean(String regex) { return transactionValue(regex, "mean"); }
    public String transactionMean(java.util.Map<String, Object> args) { return transactionValue(args, "mean"); }

    public String transactionMedian(String regex) { return transactionValue(regex, "median"); }
    public String transactionMedian(java.util.Map<String, Object> args) { return transactionValue(args, "median"); }

    public String transactionMin(String regex) { return transactionValue(regex, "min"); }
    public String transactionMin(java.util.Map<String, Object> args) { return transactionValue(args, "min"); }

    public String transactionMax(String regex) { return transactionValue(regex, "max"); }
    public String transactionMax(java.util.Map<String, Object> args) { return transactionValue(args, "max"); }
    
    public String transactionErrors(String regex) { return transactionValue(regex, "errors"); }
    public String transactionErrors(java.util.Map<String, Object> args) { return transactionValue(args, "errors"); }

    public String transactionErrorPercentage(String regex) { return transactionValue(regex, "errorPercentage"); }
    public String transactionErrorPercentage(java.util.Map<String, Object> args) { return transactionValue(args, "errorPercentage"); }

    public String transactionCount(String regex) { return transactionValue(regex, "count"); }
    public String transactionCount(java.util.Map<String, Object> args) { return transactionValue(args, "count"); }

    // =========================================================
    // Actions
    // =========================================================
    
    /**
     * Selects an arbitrary metric for an action.
     */
    public String actionValue(String regex, String metricPath)
    {
        return aggregateValue("actions", "action", regex, metricPath);
    }

    public String actionValue(java.util.Map<String, Object> args, String metricPath)
    {
        return aggregateValue("actions", "action", args, metricPath);
    }

    public String actionP50(String regex) { return actionValue(regex, "percentiles/p50"); }
    public String actionP50(java.util.Map<String, Object> args) { return actionValue(args, "percentiles/p50"); }

    public String actionP95(String regex) { return actionValue(regex, "percentiles/p95"); }
    public String actionP95(java.util.Map<String, Object> args) { return actionValue(args, "percentiles/p95"); }

    public String actionP99(String regex) { return actionValue(regex, "percentiles/p99"); }
    public String actionP99(java.util.Map<String, Object> args) { return actionValue(args, "percentiles/p99"); }

    public String actionP99_9(String regex) { return actionValue(regex, "percentiles/p99.9"); }
    public String actionP99_9(java.util.Map<String, Object> args) { return actionValue(args, "percentiles/p99.9"); }

    public String actionMean(String regex) { return actionValue(regex, "mean"); }
    public String actionMean(java.util.Map<String, Object> args) { return actionValue(args, "mean"); }

    public String actionMedian(String regex) { return actionValue(regex, "median"); }
    public String actionMedian(java.util.Map<String, Object> args) { return actionValue(args, "median"); }

    public String actionMin(String regex) { return actionValue(regex, "min"); }
    public String actionMin(java.util.Map<String, Object> args) { return actionValue(args, "min"); }

    public String actionMax(String regex) { return actionValue(regex, "max"); }
    public String actionMax(java.util.Map<String, Object> args) { return actionValue(args, "max"); }
    
    public String actionErrors(String regex) { return actionValue(regex, "errors"); }
    public String actionErrors(java.util.Map<String, Object> args) { return actionValue(args, "errors"); }

    public String actionErrorPercentage(String regex) { return actionValue(regex, "errorPercentage"); }
    public String actionErrorPercentage(java.util.Map<String, Object> args) { return actionValue(args, "errorPercentage"); }

    public String actionCount(String regex) { return actionValue(regex, "count"); }
    public String actionCount(java.util.Map<String, Object> args) { return actionValue(args, "count"); }

    // =========================================================
    // Custom Timers
    // =========================================================
    
    /**
     * Selects an arbitrary metric for a custom timer.
     */
    public String customTimerValue(String regex, String metricPath)
    {
        return aggregateValue("customTimers", "customTimer", regex, metricPath);
    }

    public String customTimerValue(java.util.Map<String, Object> args, String metricPath)
    {
        return aggregateValue("customTimers", "customTimer", args, metricPath);
    }

    public String customTimerP50(String regex) { return customTimerValue(regex, "percentiles/p50"); }
    public String customTimerP50(java.util.Map<String, Object> args) { return customTimerValue(args, "percentiles/p50"); }

    public String customTimerP95(String regex) { return customTimerValue(regex, "percentiles/p95"); }
    public String customTimerP95(java.util.Map<String, Object> args) { return customTimerValue(args, "percentiles/p95"); }

    public String customTimerP99(String regex) { return customTimerValue(regex, "percentiles/p99"); }
    public String customTimerP99(java.util.Map<String, Object> args) { return customTimerValue(args, "percentiles/p99"); }
    
    public String customTimerMean(String regex) { return customTimerValue(regex, "mean"); }
    public String customTimerMean(java.util.Map<String, Object> args) { return customTimerValue(args, "mean"); }

    public String customTimerMax(String regex) { return customTimerValue(regex, "max"); }
    public String customTimerMax(java.util.Map<String, Object> args) { return customTimerValue(args, "max"); }

    // =========================================================
    // Global Summaries
    // =========================================================
    
    /**
     * Selects the global error percentage for a specific component type.
     * @param type e.g., 'requests', 'transactions', 'actions'
     */
    public String globalErrorPercentage(String type)
    {
        return "/testreport/summary/" + type + "/errorPercentage";
    }

    /**
     * Selects the pre-computed count-per-hour from the global summary for a specific component type.
     * XLT calculates this automatically based on the actual test duration.
     * @param type e.g., 'requests', 'transactions', 'actions'
     */
    public String globalCountPerHour(String type)
    {
        return "/testreport/summary/" + type + "/countPerHour";
    }

    /**
     * Returns the total number of errors whose HTTP response code matches the
     * provided regular expression.
     * <p>
     * XLT aggregates HTTP response codes in {@code //responseCodes/responseCode} with
     * {@code <code>} and {@code <count>} elements. This method matches the given
     * regex against the code and sums the corresponding {@code <count>} values.
     * </p>
     * @param statusRegex Regex matching the HTTP status code (e.g. "5.." or "5\d\d" for 5xx errors)
     */
    public String httpErrorCount(String statusRegex)
    {
        // XPath: sum counts of response codes that match the regex.
        return String.format(
            "sum(//responseCodes/responseCode[matches(code, '^%s$')]/count)",
            escapeRegex(statusRegex));
    }

    /**
     * Wraps any XPath numeric expression to calculate its per-hour rate, using the 
     * overall test duration.
     * <p>
     * Note: XLT provides {@code <duration>} in seconds under {@code /testreport/general/duration}.
     * </p>
     * @param metricExpression The raw XPath expression returning a number (e.g. {@code "sum(//...) "})
     * @return An XPath expression dividing the metric by the test duration in hours.
     */
    public String perHour(String metricExpression)
    {
        return String.format(
            "((%s) div (number(/testreport/general/duration) div 3600))",
            metricExpression);
    }

    // =========================================================
    // Agents / CPU
    // =========================================================
    
    /**
     * Selects the maximum CPU usage across all agents.
     */
    public String agentCpuMax()
    {
        return "max(//agents/agent/totalCpuUsage/max)";
    }

    /**
     * Counts how many agents had a mean CPU usage above the given threshold.
     */
    public String agentCpuMeanHigh(Number threshold)
    {
        return "count(//agents/agent/totalCpuUsage/mean[number() > " + threshold + "])";
    }
}
