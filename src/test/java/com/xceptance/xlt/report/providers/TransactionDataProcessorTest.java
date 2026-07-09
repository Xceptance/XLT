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
package com.xceptance.xlt.report.providers;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.TransactionData;

/**
 * Unit tests for {@link TransactionDataProcessor}.
 *
 * <p>This class tests the transaction-specific additions on top of the basic timer logic tested in
 * {@link BasicTimerDataProcessorTest}:
 * <ul>
 *   <li>Event counting — events are tracked separately from timer records</li>
 *   <li>Mixed transaction + event processing</li>
 *   <li>Inherited statistics (confirming the super-class logic works through the subclass)</li>
 * </ul>
 *
 * <p>Uses {@link DummyTransactionDataProcessor} to suppress chart/CSV directory creation.
 */
public class TransactionDataProcessorTest
{
    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link TransactionDataProcessor} backed by a dummy provider with charts disabled.
     */
    private TransactionDataProcessor createProcessor()
    {
        final TransactionsReportProvider provider = new TransactionsReportProvider();
        provider.setConfiguration(DummyReportGeneratorConfiguration.getDefault());
        return new DummyTransactionDataProcessor("TOrder", provider);
    }

    /**
     * Creates a {@link TransactionData} record with the given runtime and status.
     */
    private static TransactionData txnRecord(final int runTimeMs, final boolean failed)
    {
        final TransactionData data = new TransactionData("TOrder");
        data.setTime(1000000L);
        data.setRunTime(runTimeMs);
        data.setFailed(failed);
        return data;
    }

    /**
     * Creates an {@link EventData} record. Events are associated with a transaction by name.
     */
    private static EventData eventRecord()
    {
        final EventData data = new EventData("TOrderEvent");
        data.setTime(1000000L);
        return data;
    }

    // -------------------------------------------------------------------------
    // Test: pure transaction records — inherited statistics
    // -------------------------------------------------------------------------

    @Test
    public void testTransactionStatistics()
    {
        final TransactionDataProcessor proc = createProcessor();

        proc.processDataRecord(txnRecord(100, false));
        proc.processDataRecord(txnRecord(300, false));
        proc.processDataRecord(txnRecord(500, false));

        final TransactionReport report = (TransactionReport) proc.createTimerReport(false);

        Assert.assertEquals("count", 3, report.count);
        Assert.assertEquals("min", 100, report.min);
        Assert.assertEquals("max", 500, report.max);
        Assert.assertEquals("mean", 300.0, report.mean.doubleValue(), 0.01);
        Assert.assertEquals("errors", 0, report.errors);
        Assert.assertEquals("events", 0, report.events);
    }

    // -------------------------------------------------------------------------
    // Test: event counting
    // -------------------------------------------------------------------------

    @Test
    public void testEventCounting()
    {
        final TransactionDataProcessor proc = createProcessor();

        // 2 transaction records + 3 event records
        proc.processDataRecord(txnRecord(200, false));
        proc.processDataRecord(eventRecord());
        proc.processDataRecord(eventRecord());
        proc.processDataRecord(txnRecord(400, false));
        proc.processDataRecord(eventRecord());

        final TransactionReport report = (TransactionReport) proc.createTimerReport(false);

        // Only transactions count towards timer statistics
        Assert.assertEquals("count", 2, report.count);
        Assert.assertEquals("min", 200, report.min);
        Assert.assertEquals("max", 400, report.max);

        // Events are tracked separately
        Assert.assertEquals("events", 3, report.events);
    }

    // -------------------------------------------------------------------------
    // Test: zero events
    // -------------------------------------------------------------------------

    @Test
    public void testNoEvents()
    {
        final TransactionDataProcessor proc = createProcessor();
        proc.processDataRecord(txnRecord(150, false));

        final TransactionReport report = (TransactionReport) proc.createTimerReport(false);

        Assert.assertEquals("events", 0, report.events);
    }

    // -------------------------------------------------------------------------
    // Test: events only — no transaction records
    // -------------------------------------------------------------------------

    @Test
    public void testOnlyEvents()
    {
        final TransactionDataProcessor proc = createProcessor();
        proc.processDataRecord(eventRecord());
        proc.processDataRecord(eventRecord());

        final TransactionReport report = (TransactionReport) proc.createTimerReport(false);

        // events tracked but no timer data
        Assert.assertEquals("count", 0, report.count);
        Assert.assertEquals("events", 2, report.events);
    }

    // -------------------------------------------------------------------------
    // Test: errors in transactions do not affect event count
    // -------------------------------------------------------------------------

    @Test
    public void testErrorsDoNotAffectEvents()
    {
        final TransactionDataProcessor proc = createProcessor();

        proc.processDataRecord(txnRecord(100, true));
        proc.processDataRecord(txnRecord(200, false));
        proc.processDataRecord(eventRecord());

        final TransactionReport report = (TransactionReport) proc.createTimerReport(false);

        Assert.assertEquals("count", 2, report.count);
        Assert.assertEquals("errors", 1, report.errors);
        Assert.assertEquals("events", 1, report.events);
        Assert.assertEquals("errorPercentage", 50.0, report.errorPercentage.doubleValue(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test: TransactionReport type code
    // -------------------------------------------------------------------------

    @Test
    public void testTransactionReportTypeCode()
    {
        final TransactionDataProcessor proc = createProcessor();
        proc.processDataRecord(txnRecord(100, false));

        final TransactionReport report = (TransactionReport) proc.createTimerReport(false);

        Assert.assertEquals("typeCode", "T", report.getTypeCode());
    }
}
