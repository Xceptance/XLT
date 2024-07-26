package com.xceptance.xlt.engine.metrics.otel;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

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
        final Map<String, String> otelProps = lookupProperties(props);

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

    private static Map<String, String> lookupProperties(final XltProperties props)
    {
        return props.getProperties().stringPropertyNames().stream().filter(OpenTelemetryConfiguration::isOTelProp)
                    .collect(Collectors.toMap((k) -> StringUtils.removeStart(k, XltConstants.SECRET_PREFIX), props::getProperty));
    }

    private static boolean isOTelProp(String key)
    {
        final String otelPropPrefix = "otel.";
        return StringUtils.startsWithAny(key, otelPropPrefix, XltConstants.SECRET_PREFIX + otelPropPrefix);
    }
}
