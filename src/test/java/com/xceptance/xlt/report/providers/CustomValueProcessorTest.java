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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.xlt.api.engine.CustomValue;

/**
 * Unit tests for {@link CustomValueProcessor}.
 *
 * <p>Verifies the aggregation logic for custom double-valued metrics:
 * <ul>
 *   <li>Min / max / mean / standard deviation calculations</li>
 *   <li>Count and throughput metrics</li>
 *   <li>Single record edge case</li>
 *   <li>Negative and zero values</li>
 * </ul>
 *
 * <p>Uses a {@link DummyReportGeneratorConfiguration} with {@code reportDirectory}
 * set to a temp folder so chart/CSV directories are properly initialized.
 */
public class CustomValueProcessorTest
{
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private CustomValueProcessor createProcessor() throws Exception
    {
        final DummyReportGeneratorConfiguration config = DummyReportGeneratorConfiguration.getDefault();
        config.setReportDirectory(tempDir.newFolder("report"));

        final CustomValuesReportProvider provider = new CustomValuesReportProvider();
        provider.setConfiguration(config);

        return new CustomValueProcessor("MySampler", provider);
    }

    private static CustomValue sample(final double value)
    {
        final CustomValue cv = new CustomValue("MySampler");
        cv.setTime(1000000L);
        cv.setValue(value);
        return cv;
    }

    // -------------------------------------------------------------------------
    // Test: single sample
    // -------------------------------------------------------------------------

    @Test
    public void testSingleSample() throws Exception
    {
        final CustomValueProcessor proc = createProcessor();
        proc.processDataRecord(sample(42.5));

        final CustomValueReport report = proc.createReportFragment();

        Assert.assertEquals("name", "MySampler", report.name);
        Assert.assertEquals("count", 1, report.count);
        Assert.assertEquals("min", 42.5, report.min.doubleValue(), 0.001);
        Assert.assertEquals("max", 42.5, report.max.doubleValue(), 0.001);
        Assert.assertEquals("mean", 42.5, report.mean.doubleValue(), 0.001);
        Assert.assertEquals("deviation", 0.0, report.standardDeviation.doubleValue(), 0.001);
    }

    // -------------------------------------------------------------------------
    // Test: multiple samples — known statistics
    // -------------------------------------------------------------------------

    /**
     * Samples: [10.0, 20.0, 30.0, 40.0, 50.0]
     * mean = 30.0, population stddev = sqrt(200) ~ 14.14
     */
    @Test
    public void testMultipleSamples_knownStatistics() throws Exception
    {
        final CustomValueProcessor proc = createProcessor();

        proc.processDataRecord(sample(10.0));
        proc.processDataRecord(sample(20.0));
        proc.processDataRecord(sample(30.0));
        proc.processDataRecord(sample(40.0));
        proc.processDataRecord(sample(50.0));

        final CustomValueReport report = proc.createReportFragment();

        Assert.assertEquals("count", 5, report.count);
        Assert.assertEquals("min", 10.0, report.min.doubleValue(), 0.001);
        Assert.assertEquals("max", 50.0, report.max.doubleValue(), 0.001);
        Assert.assertEquals("mean", 30.0, report.mean.doubleValue(), 0.01);
        Assert.assertEquals("deviation", 14.14, report.standardDeviation.doubleValue(), 1.0);
    }

    // -------------------------------------------------------------------------
    // Test: identical samples
    // -------------------------------------------------------------------------

    @Test
    public void testIdenticalSamples() throws Exception
    {
        final CustomValueProcessor proc = createProcessor();

        proc.processDataRecord(sample(7.77));
        proc.processDataRecord(sample(7.77));
        proc.processDataRecord(sample(7.77));

        final CustomValueReport report = proc.createReportFragment();

        Assert.assertEquals("min", 7.77, report.min.doubleValue(), 0.001);
        Assert.assertEquals("max", 7.77, report.max.doubleValue(), 0.001);
        Assert.assertEquals("mean", 7.77, report.mean.doubleValue(), 0.001);
        Assert.assertEquals("deviation", 0.0, report.standardDeviation.doubleValue(), 0.001);
    }

    // -------------------------------------------------------------------------
    // Test: zero and negative values
    // -------------------------------------------------------------------------

    @Test
    public void testZeroAndNegativeValues() throws Exception
    {
        final CustomValueProcessor proc = createProcessor();

        proc.processDataRecord(sample(-10.0));
        proc.processDataRecord(sample(0.0));
        proc.processDataRecord(sample(10.0));

        final CustomValueReport report = proc.createReportFragment();

        Assert.assertEquals("count", 3, report.count);
        Assert.assertEquals("min", -10.0, report.min.doubleValue(), 0.001);
        Assert.assertEquals("max", 10.0, report.max.doubleValue(), 0.001);
        Assert.assertEquals("mean", 0.0, report.mean.doubleValue(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test: throughput (with dummy config, duration = 1 second)
    // -------------------------------------------------------------------------

    @Test
    public void testThroughput() throws Exception
    {
        final CustomValueProcessor proc = createProcessor();

        proc.processDataRecord(sample(1.0));
        proc.processDataRecord(sample(2.0));
        proc.processDataRecord(sample(3.0));

        final CustomValueReport report = proc.createReportFragment();

        Assert.assertEquals("countPerSecond", 3.0, report.countPerSecond.doubleValue(), 0.01);
        Assert.assertEquals("countPerMinute", 180.0, report.countPerMinute.doubleValue(), 0.01);
        Assert.assertEquals("countPerHour", 10800.0, report.countPerHour.doubleValue(), 0.01);
        Assert.assertEquals("countPerDay", 259200.0, report.countPerDay.doubleValue(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test: fractional precision
    // -------------------------------------------------------------------------

    /**
     * Tests fractional precision. Note: {@code ReportUtils.convertToBigDecimal} rounds to 3 decimal
     * places, so we use values that survive this rounding cleanly.
     */
    @Test
    public void testFractionalPrecision() throws Exception
    {
        final CustomValueProcessor proc = createProcessor();

        proc.processDataRecord(sample(0.100));
        proc.processDataRecord(sample(0.200));

        final CustomValueReport report = proc.createReportFragment();

        Assert.assertEquals("min", 0.100, report.min.doubleValue(), 0.001);
        Assert.assertEquals("max", 0.200, report.max.doubleValue(), 0.001);
        Assert.assertEquals("mean", 0.150, report.mean.doubleValue(), 0.001);
    }
}
