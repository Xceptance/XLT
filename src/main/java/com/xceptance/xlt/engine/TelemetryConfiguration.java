package com.xceptance.xlt.engine;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.xceptance.xlt.api.util.XltProperties;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class TelemetryConfiguration
{
    public static OpenTelemetrySdk initialize(final XltProperties properties)
    {
        final Map<String,String> otelProps = Objects.requireNonNull(properties).getProperties().stringPropertyNames().stream().filter(p -> p.startsWith("otel.")).collect(Collectors.toMap(Function.identity(), properties::getProperty));
        final AutoConfiguredOpenTelemetrySdk autoConfOTel = AutoConfiguredOpenTelemetrySdk.builder().addPropertiesSupplier(() -> otelProps).build();

        return autoConfOTel.getOpenTelemetrySdk();
    }
}
