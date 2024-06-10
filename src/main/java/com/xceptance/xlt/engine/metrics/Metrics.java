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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.metrics.graphite.GraphiteMetricsReporter;
import com.xceptance.xlt.engine.metrics.otel.OtelMetricsReporter;

/**
 * The Metrics sub-system collects certain metrics and submits them periodically to a reporting system (Graphite in this
 * case) for real-time reporting.
 */
public class Metrics
{

    protected static final String PROP_REP_PREFIX = "xlt.reporting.";

    private static final String PROP_REP_ENABLED = PROP_REP_PREFIX + "enabled";

    private static final String PROP_REP_INTERVAL = PROP_REP_PREFIX + "interval";

    private static final String PROP_REP_BACKEND = PROP_REP_PREFIX + "backend";

    private static final String PROP_REP_METRIC_NAME_PREFIX = PROP_REP_PREFIX + "metricNamePrefix";

    private static final String PROP_REP_PREFIX_GRAPHITE = PROP_REP_PREFIX + "graphite.";

    private static final String PROP_REP_GRAPHITE_SERVER = PROP_REP_PREFIX_GRAPHITE + "host";

    private static final String PROP_REP_GRAPHITE_PORT = PROP_REP_PREFIX_GRAPHITE + "port";

    private static final String PROP_REP_PREFIX_OTEL = PROP_REP_PREFIX + "otel.";

    private static final String PROP_REP_OTEL_SERVER = PROP_REP_PREFIX_OTEL + "host";

    private static final String PROP_REP_OTEL_PORT = PROP_REP_PREFIX_OTEL + "port";

    private static final String PROP_REP_OTEL_SECRET = PROP_REP_PREFIX_OTEL + "secret";
    
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

    private final MetricsReporter _reporter;

    /**
     * Constructor.
     */
    protected Metrics()
    {
        // get configuration
        final XltProperties props = XltProperties.getInstance();

        final boolean enabled = props.getProperty(PROP_REP_ENABLED, false);
        final int reportingInterval = props.getProperty(PROP_REP_INTERVAL, 5);
        final String metricsNamePrefix = props.getProperty(PROP_REP_METRIC_NAME_PREFIX, "");

        final String metricsBackend = props.getProperty(PROP_REP_BACKEND, "graphite");
        final String graphiteHost = props.getProperty(PROP_REP_GRAPHITE_SERVER, "localhost");
        final int graphitePort = props.getProperty(PROP_REP_GRAPHITE_PORT, 2003);

        final String otelHost = props.getProperty(PROP_REP_OTEL_SERVER, "localhost");
        final int otelPort = props.getProperty(PROP_REP_OTEL_PORT, 12345);
        final String otelSecret = props.getProperty(PROP_REP_OTEL_SECRET);

        // start reporting if so configured and we are load testing
        MetricsReporter reporter = null;
        if (enabled && Session.getCurrent().isLoadTest())
        {
            try
            {
                if ("graphite".equalsIgnoreCase(metricsBackend))
                {
                    reporter = new GraphiteMetricsReporter(enabled, reportingInterval, graphiteHost, graphitePort, metricsNamePrefix);
                }
                else if ("otel".equalsIgnoreCase(metricsBackend))
                {
                    reporter = new OtelMetricsReporter(enabled, reportingInterval, otelHost, otelPort, metricsNamePrefix, otelSecret);
                }
            }
            catch (final Exception e)
            {
                log.error("Failed to start metrics reporter '%s'", metricsBackend, e);
            }

        }

        this._reporter = reporter;
    }

    /**
     * Updates selected metrics based on the given data.
     *
     * @param data
     *            the data
     */
    public void updateMetrics(final Data data)
    {
        if (this._reporter != null)
        {
            this._reporter.reportMetrics(data);
        }
    }
}
