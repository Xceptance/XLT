/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.metrics.graphite;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.engine.metrics.CounterMetric;
import com.xceptance.xlt.engine.metrics.Metric;
import com.xceptance.xlt.engine.metrics.RateMetric;
import com.xceptance.xlt.engine.metrics.ValueMetric;
import com.xceptance.xlt.engine.metrics.ValueMetric.Snapshot;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * A reporter which periodically publishes metric values to a Graphite Carbon server.
 */
public class GraphiteReporter
{
    private static final Log log = LogFactory.getLog(GraphiteReporter.class);

    private final PlainTextCarbonClient carbonClient;

    private final String metricNamePrefix;

    private final Map<String, Metric> metrics;

    private int metricCount;

    private final long interval;

    private volatile long lastReportingTime;

    /**
     * Creates the reporter.
     *
     * @param carbonClient
     *            the Carbon client
     * @param metrics
     *            all the metrics keyed by metric name
     * @param metricNamePrefix
     *            the prefix to add to the metric's name before publishing the metric
     * @param interval
     *            the publishing interval [ms]
     */
    public GraphiteReporter(final PlainTextCarbonClient carbonClient, final Map<String, Metric> metrics, final String metricNamePrefix,
                            final int interval)
    {
        this.metrics = metrics;
        this.carbonClient = carbonClient;
        this.metricNamePrefix = metricNamePrefix;
        this.interval = interval;

        lastReportingTime = GlobalClock.getInstance().getTime();
    }

    /**
     * Starts the background task that periodically publishes metrics to the server.
     */
    public void start()
    {
        // create the timer
        final Timer timer = new Timer("GraphiteReporter", true);

        // register a clean-up routine for JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                // stop the timer
                timer.cancel();

                // report any "last-minute" updates now, but pretend that they were sent the normal way at the next
                // regular reporting time
                report(lastReportingTime + interval);
            }
        });

        // create the timer task that reports the metrics
        final TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                // remember the current time, we will need it as reference time during shutdown
                lastReportingTime = GlobalClock.getInstance().getTime();

                // report the metrics with the current time
                report(lastReportingTime);
            }
        };

        // schedule the task to run periodically (with a random initial delay)
        final long initialDelay = (long) (Math.random() * interval);
        timer.schedule(timerTask, initialDelay, interval);
    }

    /**
     * Publishes metrics to the server.
     * 
     * @param time
     *            the time [ms]
     */
    private void report(long time)
    {
        // reset the metric counter
        metricCount = 0;

        try
        {
            final long startTime = TimerUtils.getTime();
            long connectTime = 0;
            long sentTime = 0;
            long closeTime = 0;

            // connect
            {
                carbonClient.connect();
                connectTime = TimerUtils.getTime();
            }

            // send data
            {
                // the current timestamp (in seconds!)
                final long timestamp = time / 1000;

                for (final Entry<String, Metric> entry : metrics.entrySet())
                {
                    final String metricName = entry.getKey();
                    final Metric metric = entry.getValue();

                    if (metric instanceof ValueMetric)
                    {
                        reportTimerMetric(metricName, (ValueMetric) metric, timestamp);
                    }
                    else if (metric instanceof CounterMetric)
                    {
                        reportCounterMetric(metricName, (CounterMetric) metric, timestamp);
                    }
                    else if (metric instanceof RateMetric)
                    {
                        reportRateMetric(metricName, (RateMetric) metric, timestamp);
                    }
                    else
                    {
                        if (log.isWarnEnabled())
                        {
                            log.warn("Skipping unknown metric class: " + metric.getClass().getName());
                        }
                    }
                }

                sentTime = TimerUtils.getTime();
            }

            // close connection
            {
                carbonClient.close();
                closeTime = TimerUtils.getTime();
            }

            // log some statistics
            if (log.isDebugEnabled())
            {
                log.debug(String.format("%d metrics sent within %d ms (%d/%d/%d)", metricCount, closeTime - startTime,
                                        connectTime - startTime, sentTime - connectTime, closeTime - sentTime));
            }
        }
        catch (final IOException e)
        {
            carbonClient.close();
            log.warn("Failed to report to Graphite: " + e);
        }
    }

    /**
     * Reports a value metric to the server.
     *
     * @param metricName
     *            the metric base name
     * @param metric
     *            the metric
     * @param timestamp
     *            the timestamp
     * @throws IOException
     */
    private void reportTimerMetric(final String metricName, final ValueMetric metric, final long timestamp) throws IOException
    {
        final Snapshot snapshot = metric.getSnapshotAndClear();

        // report metrics only if values have been added at all
        if (snapshot.getCount() > 0)
        {
            carbonClient.send(prefix(metricName, "mean"), format(snapshot.getMean()), timestamp);
            carbonClient.send(prefix(metricName, "max"), format(snapshot.getMaximum()), timestamp);
            carbonClient.send(prefix(metricName, "min"), format(snapshot.getMinimum()), timestamp);

            metricCount += 3;
        }
    }

    /**
     * Reports a counter metric to the server.
     *
     * @param metricName
     *            the metric base name
     * @param metric
     *            the metric
     * @param timestamp
     *            the timestamp
     * @throws IOException
     */
    private void reportCounterMetric(final String metricName, final CounterMetric metric, final long timestamp) throws IOException
    {
        final Long count = metric.getCountAndClear();

        // report metrics only if values have been added at all
        if (count != null)
        {
            carbonClient.send(prefix(metricName), format(count), timestamp);
            metricCount++;
        }
    }

    /**
     * Reports a rate metric to the server.
     *
     * @param metricName
     *            the metric base name
     * @param metric
     *            the metric
     * @param timestamp
     *            the timestamp
     * @throws IOException
     */
    private void reportRateMetric(final String metricName, final RateMetric metric, final long timestamp) throws IOException
    {
        final Double rate = metric.getRateAndClear();

        // report metrics only if values have been added at all
        if (rate != null)
        {
            carbonClient.send(prefix(metricName), format(rate), timestamp);
            metricCount++;
        }
    }

    /**
     * Builds the full metric name by concatenating the given components and finally prefixing them with the base name.
     *
     * @param components
     *            the metric name components
     * @return the full metric name
     */
    private String prefix(final String... components)
    {
        return metricNamePrefix + StringUtils.join(components, '.');
    }

    /**
     * Converts a long value to a formatted string as expected by Graphite.
     *
     * @param value
     *            the value
     * @return the formatted string
     */
    private String format(final long value)
    {
        return Long.toString(value);
    }

    /**
     * Converts a double value to a formatted string as expected by Graphite.
     *
     * @param value
     *            the value
     * @return the formatted string
     */
    private String format(final double value)
    {
        return String.format("%.2f", value);
    }
}
