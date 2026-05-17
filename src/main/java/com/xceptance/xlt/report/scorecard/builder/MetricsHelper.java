package com.xceptance.xlt.report.scorecard.builder;

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
     * Constructs a generic aggregate XPath expression.
     */
    private String aggregateValue(String collectionName, String nodeName, String regex, String metricPath)
    {
        // Using max() safely aggregates the result if the regex matches multiple nodes.
        return String.format("max(//%s/%s[matches(name, '%s')]/%s)", 
                             collectionName, nodeName, escapeRegex(regex), metricPath);
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

    public String requestP50(String regex) { return requestValue(regex, "percentiles/p50"); }
    public String requestP95(String regex) { return requestValue(regex, "percentiles/p95"); }
    public String requestP99(String regex) { return requestValue(regex, "percentiles/p99"); }
    public String requestP99_9(String regex) { return requestValue(regex, "percentiles/p99.9"); }

    public String requestMean(String regex) { return requestValue(regex, "mean"); }
    public String requestMedian(String regex) { return requestValue(regex, "median"); }
    public String requestMin(String regex) { return requestValue(regex, "min"); }
    public String requestMax(String regex) { return requestValue(regex, "max"); }
    
    public String requestErrors(String regex) { return requestValue(regex, "errors"); }
    public String requestErrorPercentage(String regex) { return requestValue(regex, "errorPercentage"); }
    public String requestCount(String regex) { return requestValue(regex, "count"); }

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

    public String transactionP50(String regex) { return transactionValue(regex, "percentiles/p50"); }
    public String transactionP95(String regex) { return transactionValue(regex, "percentiles/p95"); }
    public String transactionP99(String regex) { return transactionValue(regex, "percentiles/p99"); }
    public String transactionP99_9(String regex) { return transactionValue(regex, "percentiles/p99.9"); }

    public String transactionMean(String regex) { return transactionValue(regex, "mean"); }
    public String transactionMedian(String regex) { return transactionValue(regex, "median"); }
    public String transactionMin(String regex) { return transactionValue(regex, "min"); }
    public String transactionMax(String regex) { return transactionValue(regex, "max"); }
    
    public String transactionErrors(String regex) { return transactionValue(regex, "errors"); }
    public String transactionErrorPercentage(String regex) { return transactionValue(regex, "errorPercentage"); }
    public String transactionCount(String regex) { return transactionValue(regex, "count"); }

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

    public String actionP50(String regex) { return actionValue(regex, "percentiles/p50"); }
    public String actionP95(String regex) { return actionValue(regex, "percentiles/p95"); }
    public String actionP99(String regex) { return actionValue(regex, "percentiles/p99"); }
    public String actionP99_9(String regex) { return actionValue(regex, "percentiles/p99.9"); }

    public String actionMean(String regex) { return actionValue(regex, "mean"); }
    public String actionMedian(String regex) { return actionValue(regex, "median"); }
    public String actionMin(String regex) { return actionValue(regex, "min"); }
    public String actionMax(String regex) { return actionValue(regex, "max"); }
    
    public String actionErrors(String regex) { return actionValue(regex, "errors"); }
    public String actionErrorPercentage(String regex) { return actionValue(regex, "errorPercentage"); }
    public String actionCount(String regex) { return actionValue(regex, "count"); }

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

    public String customTimerP50(String regex) { return customTimerValue(regex, "percentiles/p50"); }
    public String customTimerP95(String regex) { return customTimerValue(regex, "percentiles/p95"); }
    public String customTimerP99(String regex) { return customTimerValue(regex, "percentiles/p99"); }
    
    public String customTimerMean(String regex) { return customTimerValue(regex, "mean"); }
    public String customTimerMax(String regex) { return customTimerValue(regex, "max"); }

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
