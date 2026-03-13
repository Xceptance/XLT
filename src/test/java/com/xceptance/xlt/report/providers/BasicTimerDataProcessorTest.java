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

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.TimerData;

/**
 * Unit tests for {@link BasicTimerDataProcessor}.
 *
 * <p>These tests verify the core statistical aggregation logic: min, max, mean, standard deviation,
 * median, count, error tracking, and throughput calculations. Each test uses a hand-calculated data
 * set so the expected values are known independently of the code under test.
 *
 * <p>The tests use {@link DummyActionDataProcessor} (which extends {@link BasicTimerDataProcessor}
 * via {@link ActionDataProcessor}) with directory creation suppressed and chart generation disabled.
 */
public class BasicTimerDataProcessorTest
{
    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link BasicTimerDataProcessor} backed by a dummy provider with charts disabled.
     * Uses {@link DummyActionDataProcessor} since {@link BasicTimerDataProcessor} is abstract.
     */
    private BasicTimerDataProcessor createProcessor()
    {
        final ActionsReportProvider provider = new ActionsReportProvider();
        provider.setConfiguration(DummyReportGeneratorConfiguration.getDefault());
        return new DummyActionDataProcessor("TestTimer", provider);
    }

    /**
     * Creates an {@link ActionData} record with the given runtime and success/failure status.
     * Uses a fixed base time so that all records appear in the same second-window.
     */
    private static TimerData record(final int runTimeMs, final boolean failed)
    {
        return record(1000000L, runTimeMs, failed);
    }

    /**
     * Creates an {@link ActionData} record with a specific start time, runtime, and status.
     */
    private static TimerData record(final long startTimeMs, final int runTimeMs, final boolean failed)
    {
        final ActionData data = new ActionData("TestTimer");
        data.setTime(startTimeMs);
        data.setRunTime(runTimeMs);
        data.setFailed(failed);
        return data;
    }

    // -------------------------------------------------------------------------
    // Test: single record
    // -------------------------------------------------------------------------

    @Test
    public void testSingleRecord()
    {
        final BasicTimerDataProcessor proc = createProcessor();
        proc.processDataRecord(record(500, false));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("name", "TestTimer", report.name);
        Assert.assertEquals("count", 1, report.count);
        Assert.assertEquals("min", 500, report.min);
        Assert.assertEquals("max", 500, report.max);
        Assert.assertEquals("mean", 500.0, report.mean.doubleValue(), 0.001);
        Assert.assertEquals("deviation", 0.0, report.deviation.doubleValue(), 0.001);
        Assert.assertEquals("median", 500.0, report.median.doubleValue(), 1.0);
        Assert.assertEquals("errors", 0, report.errors);
        Assert.assertEquals("errorPercentage", 0.0, report.errorPercentage.doubleValue(), 0.001);
    }

    // -------------------------------------------------------------------------
    // Test: multiple records — known statistics
    // -------------------------------------------------------------------------

    /**
     * Feed 5 records with runtimes [100, 200, 300, 400, 500].
     *
     * <p>Expected hand-calculated values:
     * <ul>
     *   <li>count = 5</li>
     *   <li>min = 100, max = 500</li>
     *   <li>mean = (100+200+300+400+500)/5 = 300.0</li>
     *   <li>population std-dev = sqrt(((−200)²+(−100)²+0²+100²+200²)/5) = sqrt(20000) ≈ 141.42</li>
     *   <li>median ≈ 300 (middle value)</li>
     * </ul>
     */
    @Test
    public void testMultipleRecords_knownStatistics()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        proc.processDataRecord(record(100, false));
        proc.processDataRecord(record(200, false));
        proc.processDataRecord(record(300, false));
        proc.processDataRecord(record(400, false));
        proc.processDataRecord(record(500, false));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("count", 5, report.count);
        Assert.assertEquals("min", 100, report.min);
        Assert.assertEquals("max", 500, report.max);
        Assert.assertEquals("mean", 300.0, report.mean.doubleValue(), 0.01);
        // population standard deviation: sqrt(Σ(xi - μ)² / N)
        Assert.assertEquals("deviation", 141.42, report.deviation.doubleValue(), 1.0);
        Assert.assertEquals("median", 300.0, report.median.doubleValue(), 50.0);
    }

    // -------------------------------------------------------------------------
    // Test: error counting
    // -------------------------------------------------------------------------

    @Test
    public void testErrorCounting()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        // 3 records: 2 successful, 1 failed
        proc.processDataRecord(record(100, false));
        proc.processDataRecord(record(200, true));
        proc.processDataRecord(record(300, false));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("count", 3, report.count);
        Assert.assertEquals("errors", 1, report.errors);
        // error percentage: 1 / 3 ≈ 33.33%
        Assert.assertEquals("errorPercentage", 33.33, report.errorPercentage.doubleValue(), 1.0);
    }

    @Test
    public void testAllRecordsFailed()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        proc.processDataRecord(record(100, true));
        proc.processDataRecord(record(200, true));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("count", 2, report.count);
        Assert.assertEquals("errors", 2, report.errors);
        Assert.assertEquals("errorPercentage", 100.0, report.errorPercentage.doubleValue(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test: identical runtimes
    // -------------------------------------------------------------------------

    @Test
    public void testIdenticalRuntimes()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        proc.processDataRecord(record(250, false));
        proc.processDataRecord(record(250, false));
        proc.processDataRecord(record(250, false));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("count", 3, report.count);
        Assert.assertEquals("min", 250, report.min);
        Assert.assertEquals("max", 250, report.max);
        Assert.assertEquals("mean", 250.0, report.mean.doubleValue(), 0.001);
        Assert.assertEquals("deviation", 0.0, report.deviation.doubleValue(), 0.001);
        Assert.assertEquals("median", 250.0, report.median.doubleValue(), 1.0);
    }

    // -------------------------------------------------------------------------
    // Test: min/max with large spread
    // -------------------------------------------------------------------------

    @Test
    public void testMinMaxWithLargeSpread()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        proc.processDataRecord(record(1, false));
        proc.processDataRecord(record(10000, false));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("min", 1, report.min);
        Assert.assertEquals("max", 10000, report.max);
        Assert.assertEquals("mean", 5000.5, report.mean.doubleValue(), 0.5);
    }

    // -------------------------------------------------------------------------
    // Test: zero runtime
    // -------------------------------------------------------------------------

    @Test
    public void testZeroRuntime()
    {
        final BasicTimerDataProcessor proc = createProcessor();
        proc.processDataRecord(record(0, false));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("min", 0, report.min);
        Assert.assertEquals("max", 0, report.max);
        Assert.assertEquals("mean", 0.0, report.mean.doubleValue(), 0.001);
        Assert.assertEquals("count", 1, report.count);
    }

    // -------------------------------------------------------------------------
    // Test: failed records still count towards statistics
    // -------------------------------------------------------------------------

    @Test
    public void testFailedRecordsContributeToStatistics()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        // A failed request with runtime 1000 should still be part of min/max/mean
        proc.processDataRecord(record(100, false));
        proc.processDataRecord(record(1000, true));

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("count", 2, report.count);
        Assert.assertEquals("min", 100, report.min);
        Assert.assertEquals("max", 1000, report.max);
        Assert.assertEquals("mean", 550.0, report.mean.doubleValue(), 0.01);
        Assert.assertEquals("errors", 1, report.errors);
    }

    // -------------------------------------------------------------------------
    // Test: throughput metrics (countPerSecond, countPerMinute, etc.)
    // -------------------------------------------------------------------------

    /**
     * Tests throughput calculation. Start and end times are read from getConfiguration(),
     * which in DummyReportProviderConfiguration both return 0. The processor computes
     * duration = max(1, (endTime - startTime) / 1000) = max(1, 0) = 1 second.
     * With 4 records, we expect: countPerSecond = 4, countPerMinute = 240, etc.
     */
    @Test
    public void testThroughputMetrics()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        proc.processDataRecord(record(100, false));
        proc.processDataRecord(record(200, false));
        proc.processDataRecord(record(300, false));
        proc.processDataRecord(record(400, false));

        final TimerReport report = proc.createTimerReport(false);

        // duration is 1 second (due to dummy config returning 0 for start and end times)
        Assert.assertEquals("countPerSecond", 4.0, report.countPerSecond.doubleValue(), 0.01);
        Assert.assertEquals("countPerMinute", 240.0, report.countPerMinute.doubleValue(), 0.01);
        Assert.assertEquals("countPerHour", 14400.0, report.countPerHour.doubleValue(), 0.01);
        Assert.assertEquals("countPerDay", 345600.0, report.countPerDay.doubleValue(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test: many records for statistical stability
    // -------------------------------------------------------------------------

    @Test
    public void testManyRecords()
    {
        final BasicTimerDataProcessor proc = createProcessor();

        // Feed 1000 records with runtimes 1..1000
        for (int i = 1; i <= 1000; i++)
        {
            proc.processDataRecord(record(i, false));
        }

        final TimerReport report = proc.createTimerReport(false);

        Assert.assertEquals("count", 1000, report.count);
        Assert.assertEquals("min", 1, report.min);
        Assert.assertEquals("max", 1000, report.max);
        // mean = (1+2+...+1000)/1000 = 500.5
        Assert.assertEquals("mean", 500.5, report.mean.doubleValue(), 0.01);
        Assert.assertEquals("errors", 0, report.errors);
        // median should be close to 500
        Assert.assertEquals("median", 500.0, report.median.doubleValue(), 10.0);
    }
}
