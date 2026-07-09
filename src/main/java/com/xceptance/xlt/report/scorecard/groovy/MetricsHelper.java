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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A helper class injected into the Groovy scorecard script as 'selectors'.
 * It provides a fluent way to generate XPath selector strings for the most common
 * XLT test report metrics, avoiding the need for test engineers to write raw XPath.
 */
public final class MetricsHelper
{
    /**
     * Escapes single quotes in the regex to prevent XPath injection/syntax errors.
     *
     * @param regex the input regular expression
     * @return the escaped regex string, or empty string if input is null
     */
    private String escapeRegex(final String regex)
    {
        if (regex == null)
        {
            return "";
        }
        return regex.replace("'", "''");
    }

    /**
     * Constructs a generic aggregate XPath expression for a regex name match.
     *
     * @param collectionName the name of the XML collection element (e.g. "requests")
     * @param nodeName       the name of the XML node element (e.g. "request")
     * @param regex          the regex string matching the name attribute
     * @param metricPath     the path to the target metric (e.g. "percentiles/p95")
     * @return the constructed XPath expression
     */
    private String aggregateValue(final String collectionName, final String nodeName, final String regex, final String metricPath)
    {
        // Using max() safely aggregates the result if the regex matches multiple nodes.
        return String.format("max(//%s/%s[matches(name, '%s')]/%s)", 
                             collectionName, nodeName, escapeRegex(regex), metricPath);
    }

    /**
     * Constructs a generic aggregate XPath expression with arbitrary map conditions.
     *
     * @param collectionName the name of the XML collection element (e.g. "requests")
     * @param nodeName       the name of the XML node element (e.g. "request")
     * @param args           the conditions map (keys: name, excludeName, label, excludeLabel)
     * @param metricPath     the path to the target metric (e.g. "percentiles/p95")
     * @return the constructed XPath expression
     */
    private String aggregateValue(final String collectionName, final String nodeName, final Map<String, Object> args, final String metricPath)
    {
        final List<String> conditions = new ArrayList<>();
        
        if (args.containsKey("name"))
        {
            conditions.add(String.format("matches(name, '%s')", escapeRegex((String) args.get("name"))));
        }
        if (args.containsKey("excludeName"))
        {
            conditions.add(String.format("not(matches(name, '%s'))", escapeRegex((String) args.get("excludeName"))));
        }
        if (args.containsKey("label"))
        {
            conditions.add(String.format("labels = '%s'", escapeRegex((String) args.get("label"))));
        }
        if (args.containsKey("excludeLabel"))
        {
            conditions.add(String.format("labels != '%s'", escapeRegex((String) args.get("excludeLabel"))));
        }

        if (conditions.isEmpty())
        {
            throw new IllegalArgumentException("Must provide at least one parameter: 'name', 'excludeName', 'label', or 'excludeLabel'");
        }

        final String conditionString = String.join(" and ", conditions);

        return String.format("max(//%s/%s[%s]/%s)", collectionName, nodeName, conditionString, metricPath);
    }

    // =========================================================
    // Requests
    // =========================================================
    
    /**
     * Selects an arbitrary metric for a request.
     * 
     * @param regex      The regex matching the request name(s)
     * @param metricPath The relative path to the metric (e.g. 'bytesSent/mean')
     * @return the constructed XPath expression
     */
    public final String requestValue(final String regex, final String metricPath)
    {
        return aggregateValue("requests", "request", regex, metricPath);
    }

    /**
     * Selects an arbitrary metric for a request matching the given criteria.
     * 
     * @param args       The criteria map
     * @param metricPath The relative path to the metric (e.g. 'bytesSent/mean')
     * @return the constructed XPath expression
     */
    public final String requestValue(final Map<String, Object> args, final String metricPath)
    {
        return aggregateValue("requests", "request", args, metricPath);
    }

    /**
     * Returns the XPath expression for the 50th percentile of the request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestP50(final String regex)
    {
        return requestValue(regex, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 50th percentile of the request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestP50(final Map<String, Object> args)
    {
        return requestValue(args, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestP95(final String regex)
    {
        return requestValue(regex, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestP95(final Map<String, Object> args)
    {
        return requestValue(args, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestP99(final String regex)
    {
        return requestValue(regex, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestP99(final Map<String, Object> args)
    {
        return requestValue(args, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99.9th percentile of the request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestP99_9(final String regex)
    {
        return requestValue(regex, "percentiles/p99.9");
    }

    /**
     * Returns the XPath expression for the 99.9th percentile of the request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestP99_9(final Map<String, Object> args)
    {
        return requestValue(args, "percentiles/p99.9");
    }

    /**
     * Returns the XPath expression for the mean request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestMean(final String regex)
    {
        return requestValue(regex, "mean");
    }

    /**
     * Returns the XPath expression for the mean request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestMean(final Map<String, Object> args)
    {
        return requestValue(args, "mean");
    }

    /**
     * Returns the XPath expression for the median request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestMedian(final String regex)
    {
        return requestValue(regex, "median");
    }

    /**
     * Returns the XPath expression for the median request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestMedian(final Map<String, Object> args)
    {
        return requestValue(args, "median");
    }

    /**
     * Returns the XPath expression for the minimum request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestMin(final String regex)
    {
        return requestValue(regex, "min");
    }

    /**
     * Returns the XPath expression for the minimum request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestMin(final Map<String, Object> args)
    {
        return requestValue(args, "min");
    }

    /**
     * Returns the XPath expression for the maximum request time.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestMax(final String regex)
    {
        return requestValue(regex, "max");
    }

    /**
     * Returns the XPath expression for the maximum request time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestMax(final Map<String, Object> args)
    {
        return requestValue(args, "max");
    }
    
    /**
     * Returns the XPath expression for the error count of the request.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestErrors(final String regex)
    {
        return requestValue(regex, "errors");
    }

    /**
     * Returns the XPath expression for the error count of the request matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestErrors(final Map<String, Object> args)
    {
        return requestValue(args, "errors");
    }

    /**
     * Returns the XPath expression for the error percentage of the request.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestErrorPercentage(final String regex)
    {
        return requestValue(regex, "errorPercentage");
    }

    /**
     * Returns the XPath expression for the error percentage of the request matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestErrorPercentage(final Map<String, Object> args)
    {
        return requestValue(args, "errorPercentage");
    }

    /**
     * Returns the XPath expression for the count of the request.
     * 
     * @param regex The regex matching the request name(s)
     * @return the constructed XPath expression
     */
    public final String requestCount(final String regex)
    {
        return requestValue(regex, "count");
    }

    /**
     * Returns the XPath expression for the count of the request matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String requestCount(final Map<String, Object> args)
    {
        return requestValue(args, "count");
    }

    // =========================================================
    // Transactions
    // =========================================================
    
    /**
     * Selects an arbitrary metric for a transaction.
     * 
     * @param regex      The regex matching the transaction name(s)
     * @param metricPath The relative path to the metric
     * @return the constructed XPath expression
     */
    public final String transactionValue(final String regex, final String metricPath)
    {
        return aggregateValue("transactions", "transaction", regex, metricPath);
    }

    /**
     * Selects an arbitrary metric for a transaction matching the given criteria.
     * 
     * @param args       The criteria map
     * @param metricPath The relative path to the metric
     * @return the constructed XPath expression
     */
    public final String transactionValue(final Map<String, Object> args, final String metricPath)
    {
        return aggregateValue("transactions", "transaction", args, metricPath);
    }

    /**
     * Returns the XPath expression for the 50th percentile of the transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionP50(final String regex)
    {
        return transactionValue(regex, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 50th percentile of the transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionP50(final Map<String, Object> args)
    {
        return transactionValue(args, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionP95(final String regex)
    {
        return transactionValue(regex, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionP95(final Map<String, Object> args)
    {
        return transactionValue(args, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionP99(final String regex)
    {
        return transactionValue(regex, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionP99(final Map<String, Object> args)
    {
        return transactionValue(args, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99.9th percentile of the transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionP99_9(final String regex)
    {
        return transactionValue(regex, "percentiles/p99.9");
    }

    /**
     * Returns the XPath expression for the 99.9th percentile of the transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionP99_9(final Map<String, Object> args)
    {
        return transactionValue(args, "percentiles/p99.9");
    }

    /**
     * Returns the XPath expression for the mean transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionMean(final String regex)
    {
        return transactionValue(regex, "mean");
    }

    /**
     * Returns the XPath expression for the mean transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionMean(final Map<String, Object> args)
    {
        return transactionValue(args, "mean");
    }

    /**
     * Returns the XPath expression for the median transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionMedian(final String regex)
    {
        return transactionValue(regex, "median");
    }

    /**
     * Returns the XPath expression for the median transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionMedian(final Map<String, Object> args)
    {
        return transactionValue(args, "median");
    }

    /**
     * Returns the XPath expression for the minimum transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionMin(final String regex)
    {
        return transactionValue(regex, "min");
    }

    /**
     * Returns the XPath expression for the minimum transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionMin(final Map<String, Object> args)
    {
        return transactionValue(args, "min");
    }

    /**
     * Returns the XPath expression for the maximum transaction time.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionMax(final String regex)
    {
        return transactionValue(regex, "max");
    }

    /**
     * Returns the XPath expression for the maximum transaction time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionMax(final Map<String, Object> args)
    {
        return transactionValue(args, "max");
    }
    
    /**
     * Returns the XPath expression for the error count of the transaction.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionErrors(final String regex)
    {
        return transactionValue(regex, "errors");
    }

    /**
     * Returns the XPath expression for the error count of the transaction matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionErrors(final Map<String, Object> args)
    {
        return transactionValue(args, "errors");
    }

    /**
     * Returns the XPath expression for the error percentage of the transaction.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionErrorPercentage(final String regex)
    {
        return transactionValue(regex, "errorPercentage");
    }

    /**
     * Returns the XPath expression for the error percentage of the transaction matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionErrorPercentage(final Map<String, Object> args)
    {
        return transactionValue(args, "errorPercentage");
    }

    /**
     * Returns the XPath expression for the count of the transaction.
     * 
     * @param regex The regex matching the transaction name(s)
     * @return the constructed XPath expression
     */
    public final String transactionCount(final String regex)
    {
        return transactionValue(regex, "count");
    }

    /**
     * Returns the XPath expression for the count of the transaction matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String transactionCount(final Map<String, Object> args)
    {
        return transactionValue(args, "count");
    }

    // =========================================================
    // Actions
    // =========================================================
    
    /**
     * Selects an arbitrary metric for an action.
     * 
     * @param regex      The regex matching the action name(s)
     * @param metricPath The relative path to the metric
     * @return the constructed XPath expression
     */
    public final String actionValue(final String regex, final String metricPath)
    {
        return aggregateValue("actions", "action", regex, metricPath);
    }

    /**
     * Selects an arbitrary metric for an action matching the given criteria.
     * 
     * @param args       The criteria map
     * @param metricPath The relative path to the metric
     * @return the constructed XPath expression
     */
    public final String actionValue(final Map<String, Object> args, final String metricPath)
    {
        return aggregateValue("actions", "action", args, metricPath);
    }

    /**
     * Returns the XPath expression for the 50th percentile of the action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionP50(final String regex)
    {
        return actionValue(regex, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 50th percentile of the action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionP50(final Map<String, Object> args)
    {
        return actionValue(args, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionP95(final String regex)
    {
        return actionValue(regex, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionP95(final Map<String, Object> args)
    {
        return actionValue(args, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionP99(final String regex)
    {
        return actionValue(regex, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionP99(final Map<String, Object> args)
    {
        return actionValue(args, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99.9th percentile of the action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionP99_9(final String regex)
    {
        return actionValue(regex, "percentiles/p99.9");
    }

    /**
     * Returns the XPath expression for the 99.9th percentile of the action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionP99_9(final Map<String, Object> args)
    {
        return actionValue(args, "percentiles/p99.9");
    }

    /**
     * Returns the XPath expression for the mean action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionMean(final String regex)
    {
        return actionValue(regex, "mean");
    }

    /**
     * Returns the XPath expression for the mean action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionMean(final Map<String, Object> args)
    {
        return actionValue(args, "mean");
    }

    /**
     * Returns the XPath expression for the median action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionMedian(final String regex)
    {
        return actionValue(regex, "median");
    }

    /**
     * Returns the XPath expression for the median action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionMedian(final Map<String, Object> args)
    {
        return actionValue(args, "median");
    }

    /**
     * Returns the XPath expression for the minimum action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionMin(final String regex)
    {
        return actionValue(regex, "min");
    }

    /**
     * Returns the XPath expression for the minimum action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionMin(final Map<String, Object> args)
    {
        return actionValue(args, "min");
    }

    /**
     * Returns the XPath expression for the maximum action time.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionMax(final String regex)
    {
        return actionValue(regex, "max");
    }

    /**
     * Returns the XPath expression for the maximum action time matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionMax(final Map<String, Object> args)
    {
        return actionValue(args, "max");
    }
    
    /**
     * Returns the XPath expression for the error count of the action.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionErrors(final String regex)
    {
        return actionValue(regex, "errors");
    }

    /**
     * Returns the XPath expression for the error count of the action matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionErrors(final Map<String, Object> args)
    {
        return actionValue(args, "errors");
    }

    /**
     * Returns the XPath expression for the error percentage of the action.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionErrorPercentage(final String regex)
    {
        return actionValue(regex, "errorPercentage");
    }

    /**
     * Returns the XPath expression for the error percentage of the action matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionErrorPercentage(final Map<String, Object> args)
    {
        return actionValue(args, "errorPercentage");
    }

    /**
     * Returns the XPath expression for the count of the action.
     * 
     * @param regex The regex matching the action name(s)
     * @return the constructed XPath expression
     */
    public final String actionCount(final String regex)
    {
        return actionValue(regex, "count");
    }

    /**
     * Returns the XPath expression for the count of the action matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String actionCount(final Map<String, Object> args)
    {
        return actionValue(args, "count");
    }

    // =========================================================
    // Custom Timers
    // =========================================================
    
    /**
     * Selects an arbitrary metric for a custom timer.
     * 
     * @param regex      The regex matching the custom timer name(s)
     * @param metricPath The relative path to the metric
     * @return the constructed XPath expression
     */
    public final String customTimerValue(final String regex, final String metricPath)
    {
        return aggregateValue("customTimers", "customTimer", regex, metricPath);
    }

    /**
     * Selects an arbitrary metric for a custom timer matching the given criteria.
     * 
     * @param args       The criteria map
     * @param metricPath The relative path to the metric
     * @return the constructed XPath expression
     */
    public final String customTimerValue(final Map<String, Object> args, final String metricPath)
    {
        return aggregateValue("customTimers", "customTimer", args, metricPath);
    }

    /**
     * Returns the XPath expression for the 50th percentile of the custom timer.
     * 
     * @param regex The regex matching the custom timer name(s)
     * @return the constructed XPath expression
     */
    public final String customTimerP50(final String regex)
    {
        return customTimerValue(regex, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 50th percentile of the custom timer matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String customTimerP50(final Map<String, Object> args)
    {
        return customTimerValue(args, "percentiles/p50");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the custom timer.
     * 
     * @param regex The regex matching the custom timer name(s)
     * @return the constructed XPath expression
     */
    public final String customTimerP95(final String regex)
    {
        return customTimerValue(regex, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 95th percentile of the custom timer matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String customTimerP95(final Map<String, Object> args)
    {
        return customTimerValue(args, "percentiles/p95");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the custom timer.
     * 
     * @param regex The regex matching the custom timer name(s)
     * @return the constructed XPath expression
     */
    public final String customTimerP99(final String regex)
    {
        return customTimerValue(regex, "percentiles/p99");
    }

    /**
     * Returns the XPath expression for the 99th percentile of the custom timer matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String customTimerP99(final Map<String, Object> args)
    {
        return customTimerValue(args, "percentiles/p99");
    }
    
    /**
     * Returns the XPath expression for the mean of the custom timer.
     * 
     * @param regex The regex matching the custom timer name(s)
     * @return the constructed XPath expression
     */
    public final String customTimerMean(final String regex)
    {
        return customTimerValue(regex, "mean");
    }

    /**
     * Returns the XPath expression for the mean of the custom timer matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String customTimerMean(final Map<String, Object> args)
    {
        return customTimerValue(args, "mean");
    }

    /**
     * Returns the XPath expression for the maximum of the custom timer.
     * 
     * @param regex The regex matching the custom timer name(s)
     * @return the constructed XPath expression
     */
    public final String customTimerMax(final String regex)
    {
        return customTimerValue(regex, "max");
    }

    /**
     * Returns the XPath expression for the maximum of the custom timer matching the given criteria.
     * 
     * @param args The criteria map
     * @return the constructed XPath expression
     */
    public final String customTimerMax(final Map<String, Object> args)
    {
        return customTimerValue(args, "max");
    }

    // =========================================================
    // Global Summaries
    // =========================================================
    
    /**
     * Selects the global error percentage for a specific component type.
     * 
     * @param type e.g., 'requests', 'transactions', 'actions'
     * @return the constructed XPath expression
     */
    public final String globalErrorPercentage(final String type)
    {
        return "/testreport/summary/" + type + "/errorPercentage";
    }

    /**
     * Selects the pre-computed count-per-hour from the global summary for a specific component type.
     * XLT calculates this automatically based on the actual test duration.
     * 
     * @param type e.g., 'requests', 'transactions', 'actions'
     * @return the constructed XPath expression
     */
    public final String globalCountPerHour(final String type)
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
     * 
     * @param statusRegex Regex matching the HTTP status code (e.g. "5.." or "5\d\d" for 5xx errors)
     * @return the constructed XPath expression
     */
    public final String httpErrorCount(final String statusRegex)
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
     * 
     * @param metricExpression The raw XPath expression returning a number (e.g. {@code "sum(//...) "})
     * @return An XPath expression dividing the metric by the test duration in hours.
     */
    public final String perHour(final String metricExpression)
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
     * 
     * @return the constructed XPath expression
     */
    public final String agentCpuMax()
    {
        return "max(//agents/agent/totalCpuUsage/max)";
    }

    /**
     * Counts how many agents had a mean CPU usage above the given threshold.
     * 
     * @param threshold the threshold value
     * @return the constructed XPath expression
     */
    public final String agentCpuMeanHigh(final Number threshold)
    {
        return "count(//agents/agent/totalCpuUsage/mean[number() > " + threshold + "])";
    }
}
