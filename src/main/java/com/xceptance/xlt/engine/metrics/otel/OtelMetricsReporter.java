package com.xceptance.xlt.engine.metrics.otel;

import java.time.Instant;
import java.util.function.Consumer;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.engine.metrics.MetricsReporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.OpenTelemetrySdk;

public class OtelMetricsReporter implements MetricsReporter
{
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
            record.setAttribute(AttributeKey.stringKey("xlt.event.name"), eventData.getName());
            record.setAttribute(AttributeKey.stringKey("xlt.event.agent"), eventData.getAgentName());
            record.setAttribute(AttributeKey.stringKey("xlt.event.scenario"), eventData.getTestCaseName());
            record.setAttribute(AttributeKey.stringKey("xlt.event.transaction"), eventData.getTransactionName());
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
                record.setAttribute(AttributeKey.stringKey("xlt.error.action"), txnData.getFailedActionName());
                record.setAttribute(AttributeKey.stringKey("xlt.error.agent"), txnData.getAgentName());
                record.setAttribute(AttributeKey.stringKey("xlt.error.message"), txnData.getFailureMessage());
                record.setAttribute(AttributeKey.longKey("xlt.error.user"), Long.valueOf(txnData.getTestUserNumber()));
                record.setAttribute(AttributeKey.stringKey("xlt.error.transaction"), txnData.getTransactionName());
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
