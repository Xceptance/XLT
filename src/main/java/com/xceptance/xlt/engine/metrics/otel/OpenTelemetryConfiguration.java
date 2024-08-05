package com.xceptance.xlt.engine.metrics.otel;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

/**
 * All SDK management takes place here, away from the instrumentation code, which should only access the OpenTelemetry
 * APIs.
 */
public final class OpenTelemetryConfiguration
{
    /** Default c'tor. Declared private to prevent external instantiation. */
    private OpenTelemetryConfiguration()
    {
        // empty on purpose
    }

    /**
     * Initializes the OpenTelemetry SDK according to properties found in given {@link XltProperties} instance supported
     * by {@link AutoConfiguredOpenTelemetrySdk}.
     *
     * @param props
     *            the properties to use for configuration
     * @param propsCustomizer
     *            an optional property customizer
     * @return ready-to-use OpenTelemetry instance
     */
    public static OpenTelemetry initialize(final XltProperties props, final Consumer<Map<String, String>> propsCustomizer)
    {
        final Map<String, String> otelProps = lookupProperties(props);

        if (propsCustomizer != null)
        {
            propsCustomizer.accept(otelProps);
        }

        return initialize(otelProps);
    }

    /**
     * Initializes the OpenTelemetry SDK according to properties found in given {@link XltProperties} instance supported
     * by {@link AutoConfiguredOpenTelemetrySdk}.
     *
     * @param props
     *            the properties to use for configuration
     * @return ready-to-use OpenTelemetry instance
     */
    public static OpenTelemetry initialize(final XltProperties props)
    {
        return initialize(props, null);
    }

    /**
     * Initializes the OpenTelemetry SDK according to given properties supported by
     * {@link AutoConfiguredOpenTelemetrySdk}.
     *
     * @param properties
     *            the properties to use for configuration
     * @return ready-to-use OpenTelemetry instance
     */
    public static OpenTelemetry initialize(final Map<String, String> properties)
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
