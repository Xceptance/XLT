package com.xceptance.xlt.engine.metrics.otel;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.engine.metrics.MetricsReporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;

public class OtelMetricsReporter implements MetricsReporter
{
//    private static final String OTEL_SECRET_HEADER_NAME = "X-XLT-OTel-Secret";
//    
//    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OtelMetricsReporter.class);
//
//    private final boolean enabled;
//
//    private final int reportingInterval;

    private final OpenTelemetrySdk _otelSdk;

//    public OtelMetricsReporter(boolean enabled, int reportingInterval, String endpoint, String metricsNamePrefix,
//                               String secret)
//    {
//        this.enabled = enabled;
//        this.reportingInterval = reportingInterval;
//
//        if (enabled)
//        {
//            configureOpenTelemetry(endpoint, metricsNamePrefix, secret);
//        }
//    }
//
//    private void configureOpenTelemetry(String endPoint, String metricsNamePrefix, String secret)
//    {
//        final ProductInformation productInfo = ProductInformation.getProductInformation();
//
//        final Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, productInfo.getProductIdentifier())
//                                          .put(ResourceAttributes.SERVICE_VERSION, productInfo.getVersion())
//                                          .put(ResourceAttributes.SERVICE_INSTANCE_ID, Session.getCurrent().getAgentID()).build();
//        final OtlpHttpLogRecordExporterBuilder exporterBuilder = OtlpHttpLogRecordExporter.builder();
//        exporterBuilder.setEndpoint(endPoint);
//        // Example retry policy.
//        // TODO Clarify exact retry settings
//        exporterBuilder.setRetryPolicy(RetryPolicy.builder().setBackoffMultiplier(1.5).setMaxAttempts(3).setMaxBackoff(Duration.ofSeconds(30)).setInitialBackoff(Duration.ofSeconds(2)).build());
//        if (StringUtils.isNotBlank(secret))
//        {
//            exporterBuilder.addHeader(OTEL_SECRET_HEADER_NAME, secret);
//        }
//
//        final LogRecordProcessor logProcessor = BatchLogRecordProcessor.builder(exporterBuilder.build())
//                                                                       .setScheduleDelay(reportingInterval, TimeUnit.SECONDS)
//                                                                       .setMaxExportBatchSize(100)
//                                                                       .setMaxQueueSize(500)
//                                                                       .build();
//
//        final SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder().addLogRecordProcessor(logProcessor).setResource(resource)
//                                                                  .build();
//
//        final OpenTelemetrySdk otelSdk = OpenTelemetrySdk.builder().setLoggerProvider(loggerProvider).build();
//        synchronized (this)
//        {
//            this._otelSdk = otelSdk;
//        }
//    
//        if(otelSdk != null)
//        {
//            Runtime.getRuntime().addShutdownHook(new Thread(otelSdk::close));
//        }
//        if(log.isDebugEnabled())
//        {
//            log.debug("Configured OpenTelemetry SDK to export logs to '{}'", endPoint);
//        }
//    }
    
    public OtelMetricsReporter(final OpenTelemetrySdk otelSdk)
    {
        this._otelSdk = otelSdk;
    }

    @Override
    public void reportMetrics(Data data)
    {
//        if (enabled)
//        {
            if (data instanceof EventData)
            {
                reportEvent((EventData) data);
            }
            else if (data instanceof TransactionData)
            {
                reportTransaction((TransactionData) data);
            }
//        }
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
        if(txnData.hasFailed())
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
