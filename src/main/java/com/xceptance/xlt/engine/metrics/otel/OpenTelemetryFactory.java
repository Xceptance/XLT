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
 * Factory for creation of pre-configured implementations of OpenTelemetry.
 */
public final class OpenTelemetryFactory
{
    /** Default c'tor. Declared private to prevent external instantiation. */
    private OpenTelemetryFactory()
    {
        // empty on purpose
    }

    /**
     * Creates a new pre-configured OpenTelemetry instance using the properties found in given {@link XltProperties}.
     *
     * @param props
     *            the properties to use for configuration
     * @param propsCustomizer
     *            an optional property customizer
     * @return ready-to-use OpenTelemetry instance
     */
    public static OpenTelemetry create(final XltProperties props, final Consumer<Map<String, String>> propsCustomizer)
    {
        final Map<String, String> otelProps = lookupProperties(props);

        if (propsCustomizer != null)
        {
            propsCustomizer.accept(otelProps);
        }

        return create(otelProps);
    }

    /**
     * Creates a new pre-configured OpenTelemetry instance using the properties found in given {@link XltProperties}.
     *
     * @param props
     *            the properties to use for configuration
     * @return ready-to-use OpenTelemetry instance
     */
    public static OpenTelemetry create(final XltProperties props)
    {
        return create(props, null);
    }

    /**
     * Creates a new pre-configured OpenTelemetry instance using the given properties
     *
     * @param properties
     *            the properties to use for configuration
     * @return ready-to-use OpenTelemetry instance
     */
    public static OpenTelemetry create(final Map<String, String> properties)
    {
        return AutoConfiguredOpenTelemetrySdk.builder().addPropertiesSupplier(() -> properties).build().getOpenTelemetrySdk();
    }

    private static Map<String, String> lookupProperties(final XltProperties props)
    {
        return props.getProperties().stringPropertyNames().stream().filter(OpenTelemetryFactory::isOTelProp)
                    .collect(Collectors.toMap((k) -> StringUtils.removeStart(k, XltConstants.SECRET_PREFIX), props::getProperty));
    }

    private static boolean isOTelProp(String key)
    {
        final String otelPropPrefix = "otel.";
        return StringUtils.startsWithAny(key, otelPropPrefix, XltConstants.SECRET_PREFIX + otelPropPrefix);
    }
}
