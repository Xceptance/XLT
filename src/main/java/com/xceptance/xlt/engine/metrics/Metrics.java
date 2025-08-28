/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.metrics.graphite.GraphiteMetricsReporter;
import com.xceptance.xlt.engine.metrics.otel.OpenTelemetryFactory;
import com.xceptance.xlt.engine.metrics.otel.OtelMetricsReporter;

/**
 * The Metrics sub-system collects certain metrics and submits them periodically to a reporting system for real-time
 * reporting.
 */
public class Metrics
{
    protected static final String PROP_REP_PREFIX = "xlt.reporting.";

    private static final String PROP_REP_ENABLED = PROP_REP_PREFIX + "enabled";

    private static final String PROP_REP_INTERVAL = PROP_REP_PREFIX + "interval";

    private static final String PROP_REP_METRIC_NAME_PREFIX = PROP_REP_PREFIX + "metricNamePrefix";

    private static final String PROP_REP_PREFIX_GRAPHITE = PROP_REP_PREFIX + "graphite.";

    private static final String PROP_REP_GRAPHITE_SERVER = PROP_REP_PREFIX_GRAPHITE + "host";

    private static final String PROP_REP_GRAPHITE_PORT = PROP_REP_PREFIX_GRAPHITE + "port";

    private static final String PROP_REP_PREFIX_OTEL = PROP_REP_PREFIX + "otel.";

    private static final Logger log = LoggerFactory.getLogger(Metrics.class);

    private static class LazySingletonHolder
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

    private final List<MetricsReporter> _reporters;

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

        // default to 'xlt.reporting.enabled' to ensure backward compatibility
        final boolean graphiteEnabled = props.getProperty(PROP_REP_PREFIX_GRAPHITE + "enabled", enabled);
        final String graphiteHost = props.getProperty(PROP_REP_GRAPHITE_SERVER, "localhost");
        final int graphitePort = props.getProperty(PROP_REP_GRAPHITE_PORT, 2003);

        final boolean otelEnabled = props.getProperty(PROP_REP_PREFIX_OTEL + "enabled", false);

        // start reporting if so configured and we are load testing
        final List<MetricsReporter> reporters = new ArrayList<>();
        if (Session.getCurrent().isLoadTest())
        {
            if (graphiteEnabled)
            {
                create("graphite", () -> new GraphiteMetricsReporter(reportingInterval, graphiteHost, graphitePort,
                                                                     metricsNamePrefix)).ifPresent(reporters::add);
            }
            if (otelEnabled)
            {
                create("otel",
                       () -> new OtelMetricsReporter(OpenTelemetryFactory.create(props,
                                                                                 (otelProps) -> Metrics.defaultOtelProps(otelProps)))).ifPresent(reporters::add);
            }
        }
        else
        {
            log.info("XLT does not run in load-test mode");
        }

        this._reporters = List.copyOf(reporters);
    }

    /**
     * Updates selected metrics based on the given data.
     *
     * @param data
     *            the data
     */
    public void updateMetrics(final Data data)
    {
        this._reporters.forEach((r) -> r.reportMetrics(data));
    }

    private static void defaultOtelProps(final Map<String, String> otelProps)
    {
        // add some default settings if not already present
        otelProps.putIfAbsent("otel.sdk.disabled", String.valueOf(false));
        otelProps.putIfAbsent("otel.exporter.otlp.endpoint", "http://localhost:4318");
        otelProps.putIfAbsent("otel.exporter.otlp.protocol", "http/protobuf");

        otelProps.putIfAbsent("otel.logs.exporter", "otlp");
        otelProps.putIfAbsent("otel.blrp.schedule.delay", String.valueOf(2000));

        otelProps.putIfAbsent("otel.metrics.exporter", "otlp");
        otelProps.putIfAbsent("otel.metric.export.interval", String.valueOf(5000));

        /*
         * Default settings same as in OpenTelemetry SDK (no need to set them)
         */
        // otelProps.putIfAbsent("otel.blrp.max.queue.size", String.valueOf(2048));
        // otelProps.putIfAbsent("otel.blrp.max.export.batch.size", String.valueOf(512));
        // otelProps.putIfAbsent("otel.blrp.export.timeout", String.valueOf(30000));
    }

    private static Optional<MetricsReporter> create(final String str, final Callable<MetricsReporter> reporterSupplier)
    {
        try
        {
            return Optional.ofNullable(reporterSupplier.call());
        }
        catch (final Throwable t)
        {
            log.error("Failed to start metrics reporter '{}'", str, t);
        }

        return Optional.empty();
    }

}
