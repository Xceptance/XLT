package com.xceptance.xlt.engine.metrics.otel;

import java.util.Map;
import java.util.function.Consumer;
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

    public static OpenTelemetrySdk initialize(final XltProperties props, final Consumer<Map<String, String>> propsCustomizer)
    {
        final Map<String, String> otelProps = props.getProperties().stringPropertyNames().stream().filter(p -> p.startsWith("otel."))
                                                   .collect(Collectors.toMap(Function.identity(), props::getProperty));


        if (propsCustomizer != null)
        {
            propsCustomizer.accept(otelProps);
        }

        return initialize(otelProps);
    }

    public static OpenTelemetrySdk initialize(final XltProperties props)
    {
        return initialize(props, null);
    }

    public static OpenTelemetrySdk initialize(final Map<String, String> properties)
    {
        return AutoConfiguredOpenTelemetrySdk.builder().addPropertiesSupplier(() -> properties).build().getOpenTelemetrySdk();
    }
}
