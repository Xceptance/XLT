package com.xceptance.xlt.engine.metrics.otel;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.xceptance.xlt.api.util.XltProperties;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public final class OpenTelemetryConfiguration
{
    /** Default c'tor. Declared private to prevent external instantiation. */
    private OpenTelemetryConfiguration()
    {
        // empty on purpose
    }

    public static OpenTelemetrySdk initialize(final XltProperties props, final int reportingIntervalSeconds)
    {
        final Map<String, String> otelProps = props.getProperties().stringPropertyNames().stream().filter(p -> p.startsWith("otel."))
                                                   .collect(Collectors.toMap(Function.identity(), props::getProperty));
        // add some default settings if not already present
        otelProps.putIfAbsent("otel.sdk.disabled", String.valueOf(false));
        if (reportingIntervalSeconds > 0)
        {
            final String exportScheduleMillis = String.valueOf(Duration.ofSeconds(reportingIntervalSeconds).toMillis());
            otelProps.putIfAbsent("otel.blrp.schedule.delay", exportScheduleMillis);
            otelProps.putIfAbsent("otel.metric.export.interval", exportScheduleMillis);
        }

        return initialize(otelProps);
    }

    public static OpenTelemetrySdk initialize(final XltProperties props)
    {
        return initialize(props, -1);
    }

    public static OpenTelemetrySdk initialize(final Map<String, String> properties)
    {
        return AutoConfiguredOpenTelemetrySdk.builder().addPropertiesSupplier(() -> properties).build().getOpenTelemetrySdk();
    }
}
