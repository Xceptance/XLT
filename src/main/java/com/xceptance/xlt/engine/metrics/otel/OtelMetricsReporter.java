package com.xceptance.xlt.engine.metrics.otel;

import java.time.Instant;
import java.util.function.Consumer;

import org.apache.commons.lang3.CharUtils;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.engine.metrics.MetricsReporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.OpenTelemetrySdk;

public class OtelMetricsReporter implements MetricsReporter
{
    private final OpenTelemetrySdk _otelSdk;

    // private final String _metricNamePrefix;

    public OtelMetricsReporter(final OpenTelemetrySdk otelSdk, final String metricNamePrefix)
    {
        this._otelSdk = otelSdk;
        // this._metricNamePrefix = sanitizeInstrumentName(metricNamePrefix);
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
    }

    private void reportEvent(final EventData eventData)
    {
        logRecord((record) -> {
            record.setObservedTimestamp(Instant.now());
            record.setTimestamp(Instant.ofEpochMilli(eventData.getTime()));
            record.setSeverity(Severity.WARN);
            record.setBody(eventData.getMessage());
            // record.setAttribute(AttributeKey.stringKey("xlt.log_type"), "event");
            record.setAttribute(AttributeKey.stringKey("xlt.event.name"), eventData.getName());
            record.setAttribute(AttributeKey.stringKey("xlt.event.scenario"), eventData.getTestCaseName());
            record.setAttribute(AttributeKey.longKey("xlt.event.user"), Long.valueOf(Session.getCurrent().getUserNumber()));
        });
    }

    private void reportTransaction(final TransactionData txnData)
    {
        // Only process failed transactions
        if (txnData.hasFailed())
        {
            logRecord((record) -> {
                record.setObservedTimestamp(Instant.now());
                record.setTimestamp(Instant.ofEpochMilli(txnData.getTime()));
                record.setSeverity(Severity.ERROR);
                record.setBody(txnData.getFailureStackTrace());
                // record.setAttribute(AttributeKey.stringKey("xlt.log_type"), "error");
                record.setAttribute(AttributeKey.stringKey("xlt.error.action"), txnData.getFailedActionName());
                record.setAttribute(AttributeKey.stringKey("xlt.error.message"), txnData.getFailureMessage());
                record.setAttribute(AttributeKey.longKey("xlt.error.user"), Long.valueOf(txnData.getTestUserNumber()));
                record.setAttribute(AttributeKey.stringKey("xlt.error.scenario"), txnData.getName());
            });
        }
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
        return this._otelSdk.getSdkLoggerProvider().get(getClass().getName());
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
     * @return
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
