package com.xceptance.xlt.engine.metrics.otel;

import java.time.Instant;
import java.util.function.Consumer;

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

    private static final String LOG_TYPE_ERROR = "error";

    private static final String LOG_TYPE_EVENT = "event";

    private final OpenTelemetrySdk _otelSdk;

    public OtelMetricsReporter(final OpenTelemetrySdk otelSdk)
    {
        this._otelSdk = otelSdk;
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
            record.setAttribute(LogRecordAttributes.LOG_TYPE, LOG_TYPE_EVENT);
            record.setAttribute(LogRecordAttributes.EVENT_NAME, eventData.getName());
            record.setAttribute(LogRecordAttributes.EVENT_SCENARIO, eventData.getTestCaseName());
            record.setAttribute(LogRecordAttributes.EVENT_USER, Long.valueOf(Session.getCurrent().getUserNumber()));
        });
    }

    private void reportTransaction(final TransactionData txnData)
    {
        // Only process failed transactions
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
}
