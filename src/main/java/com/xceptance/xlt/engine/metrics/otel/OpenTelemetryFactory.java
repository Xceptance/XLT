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
