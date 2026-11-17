/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.TransactionData;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;

public class OtelMetricsReporterTest
{
    private List<LogRecordData> exportedLogs;

    private OpenTelemetry openTelemetry;

    @Before
    public void setUp()
    {
        exportedLogs = new CopyOnWriteArrayList<>();

        final LogRecordProcessor recordingProcessor = new LogRecordProcessor()
        {
            @Override
            public void onEmit(Context context, ReadWriteLogRecord logRecord)
            {
                exportedLogs.add(logRecord.toLogRecordData());
            }

            @Override
            public CompletableResultCode forceFlush()
            {
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode shutdown()
            {
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public void close()
            {
            }
        };

        final SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                                                                  .addLogRecordProcessor(recordingProcessor)
                                                                  .build();

        final SdkMeterProvider meterProvider = SdkMeterProvider.builder().build();

        openTelemetry = OpenTelemetrySdk.builder()
                                        .setLoggerProvider(loggerProvider)
                                        .setMeterProvider(meterProvider)
                                        .build();
    }

    @Test
    public void testLogEvents()
    {
        final EventData eventData = new EventData("OrderPlaced");
        eventData.setMessage("Order #123 placed successfully");
        eventData.setTestCaseName("TOrder");

        // Disabled
        new OtelMetricsReporter(openTelemetry, false, false).reportMetrics(eventData);
        Assert.assertTrue("No logs should be emitted when logEvents is disabled", exportedLogs.isEmpty());

        // Enabled
        new OtelMetricsReporter(openTelemetry, false, true).reportMetrics(eventData);
        Assert.assertEquals(1, exportedLogs.size());

        final LogRecordData logRecord = exportedLogs.get(0);
        Assert.assertEquals(Severity.WARN, logRecord.getSeverity());
        Assert.assertEquals("Order #123 placed successfully", logRecord.getBody().asString());
        Assert.assertEquals("event", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.LOG_TYPE));
        Assert.assertEquals("OrderPlaced", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.EVENT_NAME));
        Assert.assertEquals("TOrder", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.EVENT_SCENARIO));
    }

    @Test
    public void testLogErrors()
    {
        final TransactionData failedTxn = new TransactionData("TCheckout");
        failedTxn.setFailed(true);
        failedTxn.setFailedActionName("SubmitPayment");
        failedTxn.setFailureStackTrace("java.lang.RuntimeException: Payment timeout\n\tat foo.Bar.test(Bar.java:10)");
        failedTxn.setTestUserNumber("42");

        final TransactionData successTxn = new TransactionData("TCheckout");
        successTxn.setFailed(false);

        // Disabled: failed transaction does not emit a log
        new OtelMetricsReporter(openTelemetry, false, false).reportMetrics(failedTxn);
        Assert.assertTrue("No logs should be emitted when logErrors is disabled", exportedLogs.isEmpty());

        // Enabled: successful transaction does not emit a log
        final OtelMetricsReporter reporter = new OtelMetricsReporter(openTelemetry, true, false);
        reporter.reportMetrics(successTxn);
        Assert.assertTrue("No logs should be emitted for successful transaction", exportedLogs.isEmpty());

        // Enabled: failed transaction emits error log
        reporter.reportMetrics(failedTxn);
        Assert.assertEquals(1, exportedLogs.size());

        final LogRecordData logRecord = exportedLogs.get(0);
        Assert.assertEquals(Severity.ERROR, logRecord.getSeverity());
        Assert.assertEquals(failedTxn.getFailureStackTrace(), logRecord.getBody().asString());
        Assert.assertEquals("error", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.LOG_TYPE));
        Assert.assertEquals("SubmitPayment", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.ERROR_ACTION));
        Assert.assertEquals("Payment timeout", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.ERROR_MESSAGE));
        Assert.assertEquals("TCheckout", logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.ERROR_SCENARIO));
        Assert.assertEquals(Long.valueOf(42), logRecord.getAttributes().get(OtelMetricsReporter.LogRecordAttributes.ERROR_USER));
    }
}
