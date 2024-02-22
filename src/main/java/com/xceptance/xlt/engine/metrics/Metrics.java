/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.metrics.graphite.GraphiteReporter;
import com.xceptance.xlt.engine.metrics.graphite.PlainTextCarbonClient;

/**
 * The Metrics sub-system collects certain metrics and submits them periodically to a reporting system (Graphite in this
 * case) for real-time reporting.
 */
public class Metrics
{
    private static final int ONE_SEC = 1000;

    private static final int ONE_HOUR = 3600 * ONE_SEC;

    private static final String PROP_REP_PREFIX = "xlt.reporting.";

    private static final String PROP_REP_ENABLED = PROP_REP_PREFIX + "enabled";

    private static final String PROP_REP_INTERVAL = PROP_REP_PREFIX + "interval";

    private static final String PROP_REP_METRIC_NAME_PREFIX = PROP_REP_PREFIX + "metricNamePrefix";

    private static final String PROP_REP_PREFIX_GRAPHITE = PROP_REP_PREFIX + "graphite.";

    private static final String PROP_REP_GRAPHITE_SERVER = PROP_REP_PREFIX_GRAPHITE + "host";

    private static final String PROP_REP_GRAPHITE_PORT = PROP_REP_PREFIX_GRAPHITE + "port";

    private static final Logger log = LoggerFactory.getLogger(Metrics.class);

    public static class LazySingletonHolder
    {
        /**
         * The {@link Metrics} singleton.
         */
        private static final Metrics metrics = new Metrics();
    }

    /**
     * Returns the {@link Metrics} singleton, the entry point to the metrics sub-system.
     */
    public static Metrics getInstance()
    {
        return LazySingletonHolder.metrics;
    }

    /**
     * All the currently known metrics, keyed by metric name.
     */
    private final Map<String, Metric> metricsRegistry = new ConcurrentHashMap<String, Metric>();

    /**
     * Whether real-time reporting is enabled at all.
     */
    private final boolean enabled;

    /**
     * The configured reporting interval [ms].
     */
    private final int reportingInterval;

    /**
     * The current agent's ID. All characters illegal for Graphite have been sanitized.
     */
    private final String sanitizedAgentId;

    /**
     * Constructor.
     */
    protected Metrics()
    {
        // get configuration
        final XltProperties props = XltProperties.getInstance();

        enabled = props.getProperty(PROP_REP_ENABLED, false);
        reportingInterval = props.getProperty(PROP_REP_INTERVAL, 5) * ONE_SEC;
        final String host = props.getProperty(PROP_REP_GRAPHITE_SERVER, "localhost");
        final int port = props.getProperty(PROP_REP_GRAPHITE_PORT, 2003);
        String metricsNamePrefix = props.getProperty(PROP_REP_METRIC_NAME_PREFIX, "");

        // sanitize the metric name prefix and add a trailing dot if not present yet
        metricsNamePrefix = sanitizeFullMetricName(metricsNamePrefix);
        if (!metricsNamePrefix.endsWith("."))
        {
            metricsNamePrefix = metricsNamePrefix + ".";
        }

        // get and sanitize the current agent's ID now, we will need it often
        sanitizedAgentId = sanitizeMetricNamePart(Session.getCurrent().getAgentID());

        // start reporting if so configured and we are load testing
        if (enabled && Session.getCurrent().isLoadTest())
        {
            try
            {
                final PlainTextCarbonClient carbonClient = new PlainTextCarbonClient(host, port);
                final GraphiteReporter reporter = new GraphiteReporter(carbonClient, metricsRegistry, metricsNamePrefix, reportingInterval);
                reporter.start();

                if (log.isInfoEnabled())
                {
                    log.info(String.format("Started reporting metrics to Graphite server %s:%d every %d ms", host, port,
                                           reportingInterval));
                }
            }
            catch (final Exception e)
            {
                log.error("Failed to start Graphite reporter", e);
            }
        }
    }

    /**
     * Updates selected metrics based on the given data.
     *
     * @param data
     *            the data
     */
    public void updateMetrics(final Data data)
    {
        if (enabled)
        {
            // do the right thing depending on the data type
            if (data instanceof RequestData)
            {
                updateRequestMetrics((RequestData) data);
            }
            else if (data instanceof ActionData)
            {
                updateActionMetrics((ActionData) data);
            }
            else if (data instanceof TransactionData)
            {
                updateTransactionMetrics((TransactionData) data);
            }
            else if (data instanceof PageLoadTimingData)
            {
                updatePageLoadTimingMetrics((PageLoadTimingData) data);
            }
            else if (data instanceof CustomData)
            {
                updateCustomTimerMetrics((CustomData) data);
            }
            else if (data instanceof EventData)
            {
                updateEventMetrics((EventData) data);
            }
            else if (data instanceof JvmResourceUsageData)
            {
                updateJvmMetrics((JvmResourceUsageData) data);
            }
        }
    }

    private void updateTransactionMetrics(final TransactionData transactionData)
    {
        // metrics per transaction name
        final String sanitizedName = sanitizeMetricNamePart(transactionData.getName());
        final String metricPrefix = sanitizedAgentId + ".transactions." + sanitizedName + ".";

        updateValueMetric(metricPrefix + "runtime", (int) transactionData.getRunTime());
        updateCounterMetric(metricPrefix + "errors", transactionData.hasFailed() ? 1 : 0);
        updateRateMetric(metricPrefix + "arrivals_1h", 1, ONE_HOUR, reportingInterval);

        // summary metrics
        final String summaryMetricPrefix = sanitizedAgentId + ".summary.transactions.";

        updateValueMetric(summaryMetricPrefix + "runtime", (int) transactionData.getRunTime());
        updateCounterMetric(summaryMetricPrefix + "count", 1);
        updateCounterMetric(summaryMetricPrefix + "errors", transactionData.hasFailed() ? 1 : 0);
        updateRateMetric(summaryMetricPrefix + "arrivals_1h", 1, ONE_HOUR, reportingInterval);
    }

    private void updateActionMetrics(final ActionData actionData)
    {
        // metrics per action name
        final String sanitizedName = sanitizeMetricNamePart(actionData.getName());
        final String metricPrefix = sanitizedAgentId + ".actions." + sanitizedName + ".";

        updateValueMetric(metricPrefix + "runtime", (int) actionData.getRunTime());
        updateCounterMetric(metricPrefix + "errors", actionData.hasFailed() ? 1 : 0);

        // summary metrics
        final String summaryMetricPrefix = sanitizedAgentId + ".summary.actions.";

        updateValueMetric(summaryMetricPrefix + "runtime", (int) actionData.getRunTime());
        updateCounterMetric(summaryMetricPrefix + "count", 1);
        updateCounterMetric(summaryMetricPrefix + "errors", actionData.hasFailed() ? 1 : 0);
    }

    private void updateRequestMetrics(final RequestData requestData)
    {
        // first get rid of the sub request numbering ("Foo.1.1" -> "Foo")
        final String strippedRequestName = StringUtils.substringBefore(requestData.getName(), ".");

        // metrics per request name
        final String sanitizedName = sanitizeMetricNamePart(strippedRequestName);
        final String metricPrefix = sanitizedAgentId + ".requests." + sanitizedName + ".";

        updateValueMetric(metricPrefix + "runtime", (int) requestData.getRunTime());
        updateCounterMetric(metricPrefix + "errors", requestData.hasFailed() ? 1 : 0);

        // summary metrics
        final String summaryMetricPrefix = sanitizedAgentId + ".summary.requests.";

        updateValueMetric(summaryMetricPrefix + "runtime", (int) requestData.getRunTime());
        updateCounterMetric(summaryMetricPrefix + "count", 1);
        updateCounterMetric(summaryMetricPrefix + "errors", requestData.hasFailed() ? 1 : 0);
        updateRateMetric(summaryMetricPrefix + "bytesSent_1s", requestData.getBytesSent(), ONE_SEC, reportingInterval);
        updateRateMetric(summaryMetricPrefix + "bytesReceived_1s", requestData.getBytesReceived(), ONE_SEC, reportingInterval);
    }

    private void updatePageLoadTimingMetrics(final PageLoadTimingData pageLoadTimingData)
    {
        // metrics per page load timing name
        final String sanitizedName = sanitizeMetricNamePart(pageLoadTimingData.getName());
        final String metricPrefix = sanitizedAgentId + ".pageLoadTimings." + sanitizedName + ".";

        updateValueMetric(metricPrefix + "runtime", (int) pageLoadTimingData.getRunTime());
        updateCounterMetric(metricPrefix + "errors", pageLoadTimingData.hasFailed() ? 1 : 0);

        // summary metrics
        final String summaryMetricPrefix = sanitizedAgentId + ".summary.pageLoadTimings.";

        updateValueMetric(summaryMetricPrefix + "runtime", (int) pageLoadTimingData.getRunTime());
        updateCounterMetric(summaryMetricPrefix + "count", 1);
        updateCounterMetric(summaryMetricPrefix + "errors", pageLoadTimingData.hasFailed() ? 1 : 0);
    }

    private void updateCustomTimerMetrics(final CustomData customData)
    {
        // metrics per custom timer name
        final String sanitizedName = sanitizeMetricNamePart(customData.getName());
        final String metricPrefix = sanitizedAgentId + ".custom." + sanitizedName + ".";

        updateValueMetric(metricPrefix + "runtime", (int) customData.getRunTime());
        updateCounterMetric(metricPrefix + "errors", customData.hasFailed() ? 1 : 0);

        // summary metrics
        final String summaryMetricPrefix = sanitizedAgentId + ".summary.custom.";

        updateValueMetric(summaryMetricPrefix + "runtime", (int) customData.getRunTime());
        updateCounterMetric(summaryMetricPrefix + "count", 1);
        updateCounterMetric(summaryMetricPrefix + "errors", customData.hasFailed() ? 1 : 0);
    }

    private void updateEventMetrics(final EventData eventData)
    {
        // summary metrics
        final String summaryMetricPrefix = sanitizedAgentId + ".summary.events.";

        updateCounterMetric(summaryMetricPrefix + "count", 1);
    }

    private void updateJvmMetrics(final JvmResourceUsageData jvmData)
    {
        // metrics per agent
        final String metricPrefix = sanitizedAgentId + ".agent.";

        updateValueMetric(metricPrefix + "heapUsage", (int) jvmData.getHeapUsage());
        updateValueMetric(metricPrefix + "totalCpuUsage", (int) jvmData.getTotalCpuUsage());
    }

    /**
     * Updates the counter metric with the given name.
     *
     * @param metricName
     *            the name of the metric
     * @param value
     *            the value to add to the counter
     */
    private void updateCounterMetric(final String metricName, final int value)
    {
        Metric metric = metricsRegistry.get(metricName);
        if (metric == null)
        {
            metric = getExistingOrAddNewMetric(metricName, new CounterMetric());
        }

        metric.update(value);
    }

    /**
     * Updates the rate metric with the given name.
     *
     * @param metricName
     *            the name of the metric
     * @param value
     *            the value to add
     * @param rateInterval
     *            the rate interval [ms]
     * @param reportingInterval
     *            the reporting interval [ms]
     */
    private void updateRateMetric(final String metricName, final int value, final int rateInterval, final int reportingInterval)
    {
        Metric metric = metricsRegistry.get(metricName);
        if (metric == null)
        {
            metric = getExistingOrAddNewMetric(metricName, new RateMetric(rateInterval, reportingInterval));
        }

        metric.update(value);
    }

    /**
     * Updates the value metric with the given name.
     *
     * @param metricName
     *            the name of the metric
     * @param value
     *            the value
     */
    private void updateValueMetric(final String metricName, final int value)
    {
        Metric metric = metricsRegistry.get(metricName);
        if (metric == null)
        {
            metric = getExistingOrAddNewMetric(metricName, new ValueMetric());
        }

        metric.update(value);
    }

    /**
     * Gets a metric from the metrics registry, or if it does not exist yet, adds the given metric to the registry and
     * returns that one.
     *
     * @param metricName
     *            the name of the metric
     * @param newMetric
     *            the metric to add in case there is no such metric yet
     */
    private synchronized Metric getExistingOrAddNewMetric(final String metricName, final Metric newMetric)
    {
        Metric metric = metricsRegistry.get(metricName);
        if (metric == null)
        {
            metric = newMetric;
            metricsRegistry.put(metricName, metric);
        }

        return metric;
    }

    /**
     * Sanitizes the given full metric name such that any invalid character is replaced with an underscore. Valid
     * characters are letters, numbers, the underscore, the minus sign, and the dot.
     *
     * @param metricName
     *            the name of the metric
     * @return the sanitized metric name
     */
    private String sanitizeFullMetricName(final String metricName)
    {
        return sanitizeMetricName(metricName, true);
    }

    /**
     * Sanitizes the given metric name part such that any invalid character is replaced with an underscore. Valid
     * characters are letters, numbers, the underscore, and the minus sign.
     *
     * @param metricNamePart
     *            one part of a full metric name
     * @return the sanitized metric name part
     */
    private String sanitizeMetricNamePart(final String metricNamePart)
    {
        return sanitizeMetricName(metricNamePart, false);
    }

    /**
     * Sanitizes the given metric name such that any invalid character is replaced with an underscore. Valid characters
     * are letters, numbers, the underscore, and the minus sign. Whether or not the dot is valid as well can be
     * controlled by the second parameter.
     * <p>
     * Note that Graphite may allow more characters, but not always in all combinations (e.g., aaa[] is OK, while aaa[a]
     * is not). That's why we are somewhat over-restrictive here. Better safe than sorry.
     *
     * @param metricName
     *            the name of the metric
     * @param dotsAllowed
     *            whether a dot is also a valid character
     * @return the sanitized metric name
     */
    private String sanitizeMetricName(final String metricName, boolean dotsAllowed)
    {
        final char[] chars = metricName.toCharArray();

        for (int i = 0; i < chars.length; i++)
        {
            final char c = chars[i];
            final boolean isValidChar = CharUtils.isAsciiAlphanumeric(c) || c == '_' || c == '-' || (c == '.' && dotsAllowed);

            if (!isValidChar)
            {
                chars[i] = '_';
            }
        }

        return new String(chars);
    }
}
