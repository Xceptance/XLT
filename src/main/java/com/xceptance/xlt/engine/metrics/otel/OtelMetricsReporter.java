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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.engine.metrics.MetricsReporter;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;

public class OtelMetricsReporter implements MetricsReporter
{
    /** The (fix and non-configurable) prefix used for OpenTelemetry instruments names managed by XLT. */
    static final String INSTRUMENT_NAME_PREFIX = "xlt.";

    static final class LogRecordAttributes
    {
        static final AttributeKey<String> LOG_TYPE = AttributeKey.stringKey("xlt.log_type");

        static final AttributeKey<String> ERROR_ACTION = AttributeKey.stringKey("xlt.error.action");

        static final AttributeKey<String> ERROR_MESSAGE = AttributeKey.stringKey("xlt.error.message");

        static final AttributeKey<String> ERROR_SCENARIO = AttributeKey.stringKey("xlt.error.scenario");

        static final AttributeKey<Long> ERROR_USER = AttributeKey.longKey("xlt.error.user");

        static final AttributeKey<String> EVENT_NAME = AttributeKey.stringKey("xlt.event.name");

        static final AttributeKey<String> EVENT_SCENARIO = AttributeKey.stringKey("xlt.event.scenario");

        static final AttributeKey<Long> EVENT_USER = AttributeKey.longKey("xlt.event.user");
    }

    static final class MetricDataAttributes
    {
        static final AttributeKey<String> TIMER_NAME = AttributeKey.stringKey("xlt.timer.name");

        static final AttributeKey<String> REQUEST_METHOD = AttributeKey.stringKey("xlt.request.method");

        static final AttributeKey<Long> REQUEST_STATUS_CODE = AttributeKey.longKey("xlt.request.status_code");

        static final AttributeKey<String> REQUEST_HOST = AttributeKey.stringKey("xlt.request.target_host");

        static final AttributeKey<List<String>> REQUEST_IPS = AttributeKey.stringArrayKey("xlt.request.target_ip_address");
    }

    private static final String LOG_TYPE_ERROR = "error";

    private static final String LOG_TYPE_EVENT = "event";

    private static final String INSTRUMENTATION_SCOPE_NAME = OtelMetricsReporter.class.getName();

    private final OpenTelemetry _otel;

    private final Map<String, Object> instruments = new ConcurrentHashMap<>();

    public OtelMetricsReporter(final OpenTelemetry otel)
    {
        this._otel = otel;
    }

    @Override
    public void reportMetrics(Data data)
    {
        if (data instanceof EventData)
        {
            reportEvent((EventData) data);
        }
        else if (data instanceof TransactionData)
        {
            reportTransaction((TransactionData) data);
        }
        else if (data instanceof ActionData)
        {
            reportAction((ActionData) data);
        }
        else if (data instanceof RequestData)
        {
            reportRequest((RequestData) data);
        }
        else if (data instanceof CustomData)
        {
            reportCustom((CustomData) data);
        }
        else if (data instanceof PageLoadTimingData)
        {
            reportPageLoadTiming((PageLoadTimingData) data);
        }
        else if (data instanceof JvmResourceUsageData)
        {
            reportJvmResourceUsage((JvmResourceUsageData) data);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrCreateInstrument(final String name, Function<String, T> instrumentCreator)
    {
        final String nameSanitized = sanitizeInstrumentName(INSTRUMENT_NAME_PREFIX + name);
        T t = (T) instruments.get(nameSanitized);
        if (t == null)
        {
            synchronized (this)
            {
                t = (T) instruments.computeIfAbsent(nameSanitized, instrumentCreator);
            }
        }

        return t;
    }

    private void reportEvent(final EventData eventData)
    {
        logRecord((record) -> {
            record.setObservedTimestamp(Instant.now());
            record.setTimestamp(Instant.ofEpochMilli(eventData.getTime()));
            record.setSeverity(Severity.WARN);
            record.setBody(eventData.getMessage());
            record.setAttribute(LogRecordAttributes.LOG_TYPE, LOG_TYPE_EVENT);
            record.setAttribute(LogRecordAttributes.EVENT_NAME, eventData.getName());
            record.setAttribute(LogRecordAttributes.EVENT_SCENARIO, eventData.getTestCaseName());
            record.setAttribute(LogRecordAttributes.EVENT_USER, Long.valueOf(Session.getCurrent().getUserNumber()));
        });

        getOrCreateInstrument("events_total.count",
                              (name) -> getMeter().counterBuilder(name).setUnit(sanitizeInstrumentUnit("{event}")).build()).add(1L);
    }

    private void reportTransaction(final TransactionData txnData)
    {
        // Log failed transactions only
        if (txnData.hasFailed())
        {
            logRecord((record) -> {
                record.setObservedTimestamp(Instant.now());
                record.setTimestamp(Instant.ofEpochMilli(txnData.getEndTime()));
                record.setSeverity(Severity.ERROR);
                record.setBody(txnData.getFailureStackTrace());
                record.setAttribute(LogRecordAttributes.LOG_TYPE, LOG_TYPE_ERROR);
                record.setAttribute(LogRecordAttributes.ERROR_ACTION, txnData.getFailedActionName());
                record.setAttribute(LogRecordAttributes.ERROR_MESSAGE, txnData.getFailureMessage());
                record.setAttribute(LogRecordAttributes.ERROR_USER, Long.valueOf(txnData.getTestUserNumber()));
                record.setAttribute(LogRecordAttributes.ERROR_SCENARIO, txnData.getName());
            });
        }

        updateTimerMetricInstruments("transaction", "transactions_total", txnData, "{transaction}", transactionRuntimeBuckets(),
                                     getTransactionAttributes(txnData));
    }

    private void reportAction(final ActionData axnData)
    {
        updateTimerMetricInstruments("action", "actions_total", axnData, "{action}", actionRuntimeBuckets(), getActionAttributes(axnData));
    }

    private void reportRequest(final RequestData reqData)
    {
        final String totalMetricsNamePrefix = "requests_total";
        updateTimerMetricInstruments("request", totalMetricsNamePrefix, reqData, "{request}", requestRuntimeBuckets(),
                                     getRequestAttributes(reqData));

        getOrCreateInstrument(totalMetricsNamePrefix + ".bytes_sent",
                              (name) -> getMeter().counterBuilder(name).setUnit(sanitizeInstrumentUnit("By")).build())
                                                                                                                      .add(reqData.getBytesSent());
        getOrCreateInstrument(totalMetricsNamePrefix + ".bytes_received", (name) -> getMeter().counterBuilder(name)
                                                                                              .setUnit(sanitizeInstrumentUnit("By"))
                                                                                              .build()).add(reqData.getBytesReceived());
    }

    private void reportCustom(final CustomData customData)
    {
        updateTimerMetricInstruments("custom_timer", "custom_timers_total", customData, "{customTimer}", customTimerRuntimeBuckets(),
                                     getCustomAttributes(customData));
    }

    private void reportPageLoadTiming(final PageLoadTimingData pageLoadTimingData)
    {
        updateTimerMetricInstruments("page_load", "page_loads_total", pageLoadTimingData, "{pageLoad}", pageLoadTimingRuntimeBuckets(),
                                     getPageLoadAttributes(pageLoadTimingData));
    }

    private void updateTimerMetricInstruments(final String instrumentNamePrefix, final String totalsInstrumentNamePrefix,
                                              final TimerData data, final String countUnitName, final List<Double> runtimeBuckets,
                                              final Attributes attributes)
    {
        final boolean timerFailed = data.hasFailed();
        final double timerRuntime = data.getRunTime() / 1000d;

        doUpdateTimerMetricInstruments(totalsInstrumentNamePrefix, timerFailed, timerRuntime, countUnitName, runtimeBuckets, null);
        doUpdateTimerMetricInstruments(instrumentNamePrefix, timerFailed, timerRuntime, countUnitName, runtimeBuckets, attributes);
    }

    private void doUpdateTimerMetricInstruments(final String prefix, final boolean hasFailed, final double runtime,
                                                final String countUnitName, final List<Double> runtimeBuckets, final Attributes attributes)
    {
        final Attributes atts = Objects.requireNonNullElseGet(attributes, Attributes::empty);

        getOrCreateInstrument(prefix + ".count",
                              (name) -> getMeter().counterBuilder(name).setUnit(sanitizeInstrumentUnit(countUnitName)).build()).add(1L,
                                                                                                                                    atts);
        getOrCreateInstrument(prefix + ".errors", (name) -> getMeter().counterBuilder(name).setUnit(sanitizeInstrumentUnit("{error}"))
                                                                      .build()).add(hasFailed ? 1L : 0L, atts);
        getOrCreateInstrument(prefix + ".runtime",
                              (name) -> getMeter().gaugeBuilder(name).setUnit(sanitizeInstrumentUnit("s")).build()).set(runtime, atts);
        getOrCreateInstrument(prefix + ".runtime_histo",
                              (name) -> getMeter().histogramBuilder(name).setUnit(sanitizeInstrumentUnit("s"))
                                                  .setExplicitBucketBoundariesAdvice(runtimeBuckets).build()).record(runtime, atts);
    }

    private void reportJvmResourceUsage(final JvmResourceUsageData jvmResourceUsageData)
    {
        getOrCreateInstrument("jvm.memory.usage", (name) -> getMeter().gaugeBuilder(name).build()).set(jvmResourceUsageData.getHeapUsage());
        getOrCreateInstrument("jvm.memory.usage_histo", (name) -> getMeter().histogramBuilder(name)
                                                                            .setExplicitBucketBoundariesAdvice(resourceUsageBuckets())
                                                                            .build()).record(jvmResourceUsageData.getHeapUsage());
        getOrCreateInstrument("jvm.cpu.usage", (name) -> getMeter().gaugeBuilder(name).build()).set(jvmResourceUsageData.getCpuUsage());
        getOrCreateInstrument("jvm.cpu.usage_histo", (name) -> getMeter()
                                                                         .histogramBuilder(name).setExplicitBucketBoundariesAdvice(
                                                                                                                                   resourceUsageBuckets())
                                                                         .build()).record(jvmResourceUsageData.getCpuUsage());
    }

    private static List<Double> resourceUsageBuckets()
    {
        return List.of(10d, 25d, 50d, 75d, 100d);
    }

    private static List<Double> transactionRuntimeBuckets()
    {
        return List.of(5d, 10d, 15d, 30d, 60d);
    }

    private static List<Double> actionRuntimeBuckets()
    {
        return List.of(5d, 10d, 15d, 30d);
    }

    private static List<Double> requestRuntimeBuckets()
    {
        return List.of(0.5d, 1d, 3d, 5d);
    }

    private static List<Double> customTimerRuntimeBuckets()
    {
        // use same buckets as for actions
        return actionRuntimeBuckets();
    }

    private static List<Double> pageLoadTimingRuntimeBuckets()
    {
        // use same buckets as for requests
        return requestRuntimeBuckets();
    }

    private void logRecord(final Consumer<LogRecordBuilder> logRecordConfigurator)
    {
        logRecordConfigurator.andThen(LogRecordBuilder::emit).accept(getLogger().logRecordBuilder());
    }

    /**
     * @return
     */
    private io.opentelemetry.api.logs.Logger getLogger()
    {
        return this._otel.getLogsBridge().get(INSTRUMENTATION_SCOPE_NAME);
    }

    private io.opentelemetry.api.metrics.Meter getMeter()
    {
        return this._otel.getMeterProvider().get(INSTRUMENTATION_SCOPE_NAME);
    }

    private Attributes getTransactionAttributes(final TransactionData txnData)
    {
        return getCommonAttributes(txnData);
    }

    private Attributes getActionAttributes(final ActionData axnData)
    {
        return getCommonAttributes(axnData);
    }

    private Attributes getRequestAttributes(final RequestData reqData)
    {
        // get rid of the sub request numbering ("Foo.1.1" -> "Foo")
        final String requestName = StringUtils.substringBefore(reqData.getName(), ".");

        final AttributesBuilder builder = Attributes.builder();
        builder.put(MetricDataAttributes.TIMER_NAME, requestName);
        if (reqData.getHost() != null)
        {
            builder.put(MetricDataAttributes.REQUEST_HOST, reqData.getHost().toString());
        }
        if (reqData.getHttpMethod() != null)
        {
            builder.put(MetricDataAttributes.REQUEST_METHOD, reqData.getHttpMethod().toString());
        }
        builder.put(MetricDataAttributes.REQUEST_STATUS_CODE, reqData.getResponseCode());
        if (reqData.getIpAddresses() != null)
        {
            builder.put(MetricDataAttributes.REQUEST_IPS, reqData.getIpAddresses());

        }
        return builder.build();
    }

    private Attributes getCustomAttributes(final CustomData customData)
    {
        return getCommonAttributes(customData);
    }

    private Attributes getPageLoadAttributes(final PageLoadTimingData pageLoadTimingData)
    {
        return getCommonAttributes(pageLoadTimingData);
    }

    private Attributes getCommonAttributes(final Data data)
    {
        return Attributes.of(MetricDataAttributes.TIMER_NAME, data.getName());
    }

    /**
     * Sanitizes the given instrument name.
     * <p>
     * OpenTelemetry instrument names must satisfy the following constraints:
     * <ul>
     * <li>They are not null or empty strings.</li>
     * <li>They are case-insensitive, ASCII strings.</li>
     * <li>The first character must be an alphabetic character.</li>
     * <li>Subsequent characters must belong to the alphanumeric characters, ‘_’, ‘.’, ‘-’, and ‘/’.</li>
     * <li>They can have a maximum length of 255 characters.</li>
     * </ul>
     *
     * @param namePart
     *            instrument name part
     * @return sanitized (and lower-cased) instrument name part
     */
    private static String sanitizeInstrumentName(final String namePart)
    {
        if (namePart == null || namePart.trim().length() == 0)
        {
            throw new IllegalArgumentException("Instrument names must neither be null nor empty or blank");
        }

        final String nameLC = namePart.toLowerCase().trim();
        if (nameLC.length() > 255)
        {
            throw new IllegalArgumentException("Instrument names must not exceed the maximum length of 255 characters");
        }

        final char[] chars = nameLC.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            final char c = chars[i];
            if (i == 0)
            {
                if (!CharUtils.isAsciiAlpha(c))
                {
                    chars[i] = 'x';
                }

                continue;
            }

            final boolean valid = CharUtils.isAsciiAlphanumeric(c) || c == '-' || c == '.' || c == '/' || c == '_';
            if (!valid)
            {
                chars[i] = '_';
            }

        }

        return new String(chars);
    }

    /**
     * Sanitizes the given instrument unit.
     * <p>
     * OpenTelemetry instrument units must satisfy the following constraints:
     * <ul>
     * <li>case-sensitive ASCII string</li>
     * <li>maximum length of 63 characters</li>
     * </ul>
     *
     * @param unit
     *            instrument unit
     * @return sanitized instrument unit
     */
    private static String sanitizeInstrumentUnit(final String unit)
    {
        if (unit == null || unit.trim().length() == 0)
        {
            return unit;
        }

        if (unit.length() > 63)
        {
            throw new IllegalArgumentException("Instrument units must not exceed the maximum length of 63 characters");
        }

        final char[] chars = unit.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            final char c = chars[i];
            final boolean valid = CharUtils.isAscii(c);
            if (!valid)
            {
                chars[i] = '?';
            }
        }

        return new String(chars);
    }

}
