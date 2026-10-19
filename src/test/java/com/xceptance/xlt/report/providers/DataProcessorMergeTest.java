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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYIntervalSeries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.labelingrules.LabelingRule;
import com.xceptance.xlt.report.util.TaskManager;

/**
 * Tests the processing, URL deduplication, chart generation, and worker-to-master merge behavior
 * of {@link RequestDataProcessor}, {@link AbstractDataProcessorBasedReportProvider}, and sibling timer processors.
 */
public class DataProcessorMergeTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private static final class ConfigurableTestReportConfiguration extends DummyReportGeneratorConfiguration
    {
        private int[] customBoundaries;
        private List<LabelingRule> customRules = Collections.emptyList();

        ConfigurableTestReportConfiguration() throws java.io.IOException
        {
            super();
        }

        void setRuntimeIntervalBoundaries(final int[] boundaries)
        {
            this.customBoundaries = boundaries;
        }

        void setLabelingRules(final List<LabelingRule> rules)
        {
            this.customRules = rules;
        }

        @Override
        public int[] getRuntimeIntervalBoundaries()
        {
            return customBoundaries != null ? customBoundaries : super.getRuntimeIntervalBoundaries();
        }

        @Override
        public List<LabelingRule> getLabelingRules()
        {
            return customRules;
        }
    }

    private ConfigurableTestReportConfiguration config;
    private RequestsReportProvider requestsProvider;
    private TransactionsReportProvider transactionsProvider;
    private ActionsReportProvider actionsProvider;

    @Before
    public void setUp() throws Exception
    {
        config = new ConfigurableTestReportConfiguration();
        config.setChartStartTime(1_000L);
        config.setChartEndTime(21_000L); // 20 seconds duration
        config.setReportDirectory(tempFolder.newFolder("report"));
        config.setLabelingRules(List.of(new LabelingRule("ReqLabel", "R", "Req.*", null, true, null, null)));

        requestsProvider = new RequestsReportProvider();
        requestsProvider.setConfiguration(config);

        transactionsProvider = new TransactionsReportProvider();
        transactionsProvider.setConfiguration(config);

        actionsProvider = new ActionsReportProvider();
        actionsProvider.setConfiguration(config);
    }

    /**
     * Creates a synthetic RequestData record whose network timing and byte counters vary with {@code seq}
     * so merging two processors alters min, max, mean, and standard deviation of every sub-statistic.
     */
    private RequestData createRequest(final String name, final long time, final int runTime, final boolean failed,
                                      final int responseCode, final int bytesReceived, final String url, final int seq)
    {
        final RequestData req = new RequestData(name);
        req.setTime(time);
        req.setRunTime(runTime);
        req.setFailed(failed);
        req.setResponseCode(responseCode);
        req.setBytesReceived(bytesReceived);
        req.setBytesSent(64 + seq * 3);
        req.setUrl(url);
        req.setDnsTime(2 + seq);
        req.setConnectTime(5 + seq * 2);
        req.setSendTime(3 + seq);
        req.setServerBusyTime(10 + seq * 3);
        req.setReceiveTime(8 + seq * 2);
        req.setTimeToFirstBytes(15 + seq * 4);
        req.setTimeToLastBytes(runTime + seq);
        return req;
    }

    private RequestData createRequest(final String name, final long time, final int runTime, final boolean failed,
                                      final int responseCode, final int bytesReceived, final String url)
    {
        return createRequest(name, time, runTime, failed, responseCode, bytesReceived, url, 1);
    }

    private TransactionData createTransaction(final String name, final long time, final int runTime, final boolean failed)
    {
        final TransactionData txn = new TransactionData(name);
        txn.setTime(time);
        txn.setRunTime(runTime);
        txn.setFailed(failed);
        return txn;
    }

    private ActionData createAction(final String name, final long time, final int runTime, final boolean failed)
    {
        final ActionData act = new ActionData(name);
        act.setTime(time);
        act.setRunTime(runTime);
        act.setFailed(failed);
        return act;
    }

    /**
     * Verifies that merging two {@link RequestDataProcessor} instances produces the exact same
     * report statistics, segmentation intervals, chart files, and labeling rules as a single processor
     * that observed the combined stream, while evacuating the worker processor's URL structures.
     */
    @Test
    public void testRequestDataProcessorMerge() throws Exception
    {
        final RequestDataProcessor p1 = new RequestDataProcessor("Req1", requestsProvider);
        final RequestDataProcessor p2 = new RequestDataProcessor("Req1", requestsProvider, true);
        final RequestDataProcessor pExpected = new RequestDataProcessor("Req1", requestsProvider, true);

        // Verify constructor setup: chart/csv subdirectories, capping info, and bounded histogram max value
        Assert.assertEquals("requests", p1.getChartDir().getName());
        Assert.assertEquals("requests", p1.getCsvDir().getName());
        Assert.assertSame(config.getRequestChartCappingInfo(), p1.getChartCappingInfo());
        final int[] defaultBounds = config.getRuntimeIntervalBoundaries();
        final double expectedMaxVal = ((double) defaultBounds[defaultBounds.length - 1]) * 101 / 100;
        Assert.assertEquals(expectedMaxVal, p1.runTimeHistogramValueSet.getMaxValue(), 0.0001);

        // Feed batch 1 to p1 (6 distinct URLs so p1 has fewer than MAXIMUM_NUMBER_OF_URLS=10 before merge)
        for (int i = 0; i < 50; i++)
        {
            final RequestData req = createRequest("Req1", 1000L + i * 100, 100 + i * 25, i % 10 == 0,
                                                  i % 10 == 0 ? 500 : 200, 1024 + i * 10, "http://host/path" + (i % 6), i + 10);
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        // Feed batch 2 to p2 (includes smaller seq=0..19 values and larger seq=50..99 values so both min and max change on merge,
        // and 10 sample URLs path4..path13 so merge adds 4 new sample URLs to p1 and then hits the MAXIMUM_NUMBER_OF_URLS=10 break)
        for (int i = 0; i < 70; i++)
        {
            final int seq = (i < 20) ? i : (i + 50);
            final RequestData req = createRequest("Req1", 6000L + i * 100, 50 + i * 60, i % 7 == 0,
                                                  i % 7 == 0 ? 503 : 200, 500 + i * 20, "http://host/path" + (4 + (i % 30)), seq);
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        // Merge p2 into p1
        p1.merge(p2);

        // Verify p2's URL state was evacuated during merge so worker memory is released
        Assert.assertNull(p2.distinctUrlHashes);
        Assert.assertEquals(0, p2.getDistinctUrlCount());
        Assert.assertTrue(((RequestReport) p2.createTimerReport(false)).urls.list.isEmpty());

        // Verify responseSizeValueSet and runTimeHistogramValueSet merged accurately and actually recorded all 120 values
        Assert.assertEquals(120, p1.responseSizeValueSet.getValueCount());
        Assert.assertEquals(pExpected.responseSizeValueSet.getValueCount(), p1.responseSizeValueSet.getValueCount());
        Assert.assertArrayEquals(pExpected.responseSizeValueSet.getValues(), p1.responseSizeValueSet.getValues());
        int totalHistogramBins = 0;
        for (final int binCount : p1.runTimeHistogramValueSet.getCountPerBin())
        {
            totalHistogramBins += binCount;
        }
        Assert.assertEquals(120, totalHistogramBins);
        Assert.assertArrayEquals(pExpected.runTimeHistogramValueSet.getCountPerBin(), p1.runTimeHistogramValueSet.getCountPerBin());

        final RequestReport rActual = (RequestReport) p1.createTimerReport(true);
        final RequestReport rExpected = (RequestReport) pExpected.createTimerReport(true);

        // Wait for asynchronous chart generation tasks to finish and verify chart files on disk
        TaskManager.getInstance().waitForAllTasksToComplete();
        final File responseSizeChartFile = new File(p1.getChartDir(), "Req1_ResponseSize.webp");
        final File histogramChartFile = new File(p1.getChartDir(), "Req1_Histogram.webp");
        Assert.assertTrue("Expected ResponseSize chart file to be generated", responseSizeChartFile.isFile() && responseSizeChartFile.length() > 0);
        Assert.assertTrue("Expected Histogram chart file to be generated", histogramChartFile.isFile() && histogramChartFile.length() > 0);

        Assert.assertEquals("Count mismatch", rExpected.count, rActual.count);
        Assert.assertEquals("Errors mismatch", rExpected.errors, rActual.errors);
        Assert.assertEquals("Min runtime mismatch", rExpected.min, rActual.min);
        Assert.assertEquals("Max runtime mismatch", rExpected.max, rActual.max);
        Assert.assertEquals("Mean runtime mismatch", rExpected.mean.doubleValue(), rActual.mean.doubleValue(), 0.001);

        // Verify labeling rules were applied
        Assert.assertEquals("ReqLabel", rActual.labels);

        // Verify segmentation counts and percentages per interval
        Assert.assertTrue(rActual.countPerInterval.length > 0);
        Assert.assertArrayEquals(rExpected.countPerInterval, rActual.countPerInterval);
        Assert.assertArrayEquals(rExpected.percentagePerInterval, rActual.percentagePerInterval);
        int sumIntervals = 0;
        for (int n = 0; n < rActual.countPerInterval.length; n++)
        {
            sumIntervals += rActual.countPerInterval[n];
            Assert.assertNotNull(rActual.percentagePerInterval[n]);
        }
        Assert.assertTrue(sumIntervals > 0);

        // Verify extended statistics (bytesSent and bytesReceived) including rates per second/minute/hour/day
        assertExtendedStatisticsEquals("bytesSent", rExpected.bytesSent, rActual.bytesSent, 20L);
        assertExtendedStatisticsEquals("bytesReceived", rExpected.bytesReceived, rActual.bytesReceived, 20L);

        // Verify all 7 network timing sub-statistics (min, max, mean, deviation)
        assertStatisticsEquals("dnsTime", rExpected.dnsTime, rActual.dnsTime);
        assertStatisticsEquals("connectTime", rExpected.connectTime, rActual.connectTime);
        assertStatisticsEquals("sendTime", rExpected.sendTime, rActual.sendTime);
        assertStatisticsEquals("serverBusyTime", rExpected.serverBusyTime, rActual.serverBusyTime);
        assertStatisticsEquals("receiveTime", rExpected.receiveTime, rActual.receiveTime);
        assertStatisticsEquals("timeToFirstBytes", rExpected.timeToFirstBytes, rActual.timeToFirstBytes);
        assertStatisticsEquals("timeToLastBytes", rExpected.timeToLastBytes, rActual.timeToLastBytes);

        // Check distinct URLs count and sample URL cap (6 from p1 + 4 from p2 = 10 max, 34 distinct total: path0..path33)
        Assert.assertEquals(34, rActual.urls.total);
        Assert.assertEquals("Distinct URLs mismatch", rExpected.urls.total, rActual.urls.total);
        Assert.assertEquals(10, rActual.urls.list.size());
    }

    private void assertExtendedStatisticsEquals(final String label, final ExtendedStatisticsReport expected,
                                                final ExtendedStatisticsReport actual, final long expectedDurationSec)
    {
        assertStatisticsEquals(label, expected, actual);
        Assert.assertEquals(label + " totalCount", expected.totalCount, actual.totalCount);
        Assert.assertTrue(label + " totalCount > 0", actual.totalCount.longValue() > 0);

        final double sum = actual.totalCount.doubleValue();
        Assert.assertEquals(label + " countPerSecond", sum / expectedDurationSec, actual.countPerSecond.doubleValue(), 0.2);
        Assert.assertEquals(label + " countPerMinute", sum * 60.0 / expectedDurationSec, actual.countPerMinute.doubleValue(), 0.2);
        Assert.assertEquals(label + " countPerHour", sum * 3600.0 / expectedDurationSec, actual.countPerHour.doubleValue(), 0.2);
        Assert.assertEquals(label + " countPerDay", sum * 86400.0 / expectedDurationSec, actual.countPerDay.doubleValue(), 0.2);
    }

    private void assertStatisticsEquals(final String label, final StatisticsReport expected, final StatisticsReport actual)
    {
        Assert.assertEquals(label + " min", expected.min, actual.min);
        Assert.assertEquals(label + " max", expected.max, actual.max);
        Assert.assertTrue(label + " min < max", actual.min < actual.max);
        Assert.assertEquals(label + " mean", expected.mean.doubleValue(), actual.mean.doubleValue(), 0.001);
        Assert.assertEquals(label + " deviation", expected.deviation.doubleValue(), actual.deviation.doubleValue(), 0.001);
        Assert.assertTrue(label + " deviation > 0", actual.deviation.doubleValue() > 0.0);
    }

    /**
     * Directly verifies {@link RequestDataProcessor#createHistogramChart} both with and without
     * runtime segmentation boundaries, checking axis bounds, domain markers, and pointer annotations.
     */
    @Test
    public void testCreateHistogramChartWithAndWithoutBoundaries()
    {
        // 1. With runtime interval boundaries configured
        final int[] bounds = new int[] { 1000, 2000, 3000 };
        config.setRuntimeIntervalBoundaries(bounds);
        final RequestDataProcessor pBounded = new RequestDataProcessor("BoundedChart", requestsProvider, false);
        pBounded.processDataRecord(createRequest("BoundedChart", 2000L, 500, false, 200, 256, "http://host/1"));
        pBounded.processDataRecord(createRequest("BoundedChart", 3000L, 2500, false, 200, 256, "http://host/2"));

        final XYIntervalSeries boundedSeries = pBounded.runTimeHistogramValueSet.toSeries("Number of Requests");
        final JFreeChart boundedChart = pBounded.createHistogramChart("BoundedTitle", boundedSeries, bounds);
        final XYPlot boundedPlot = boundedChart.getXYPlot();

        Assert.assertEquals(PlotOrientation.VERTICAL, boundedPlot.getOrientation());
        Assert.assertEquals(1, boundedPlot.getDataset().getSeriesCount());
        final double expectedUpper = boundedSeries.getXHighValue(boundedSeries.getItemCount() - 1);
        Assert.assertEquals(0.0, boundedPlot.getDomainAxis().getLowerBound(), 0.0001);
        Assert.assertEquals(expectedUpper, boundedPlot.getDomainAxis().getUpperBound(), 0.0001);

        final Collection<?> markers = boundedPlot.getDomainMarkers(Layer.BACKGROUND);
        Assert.assertEquals(bounds.length, markers.size());
        int idx = 0;
        for (final Object m : markers)
        {
            final ValueMarker vm = (ValueMarker) m;
            Assert.assertEquals(bounds[idx++], vm.getValue(), 0.0001);
            Assert.assertEquals(Color.GRAY, vm.getPaint());
        }

        final List<?> annotations = boundedPlot.getAnnotations();
        Assert.assertEquals(1, annotations.size());
        final XYPointerAnnotation annotation = (XYPointerAnnotation) annotations.get(0);
        Assert.assertTrue(annotation.getText().contains("3,000") || annotation.getText().contains("3.000"));
        final double binWidth = expectedUpper / boundedSeries.getItemCount();
        Assert.assertEquals(expectedUpper - binWidth / 2.0, annotation.getX(), 0.0001);
        Assert.assertEquals(boundedPlot.getRangeAxis().getUpperBound() * 0.75, annotation.getY(), 0.0001);
        Assert.assertEquals(TextAnchor.BOTTOM_RIGHT, annotation.getTextAnchor());
        Assert.assertEquals(Color.GRAY, annotation.getPaint());
        Assert.assertEquals(Color.GRAY, annotation.getArrowPaint());
        Assert.assertEquals(0.0, annotation.getArrowLength(), 0.0001);
        Assert.assertEquals(0.0, annotation.getTipRadius(), 0.0001);

        // 2. Without runtime interval boundaries configured
        config.setRuntimeIntervalBoundaries(new int[0]);
        final RequestDataProcessor pUnbounded = new RequestDataProcessor("UnboundedChart", requestsProvider, false);
        pUnbounded.processDataRecord(createRequest("UnboundedChart", 2000L, 400, false, 200, 256, "http://host/1"));
        final XYIntervalSeries unboundedSeries = pUnbounded.getTimeSeries().toVerticalHistogramSeries("Number of Requests", 300);
        final JFreeChart unboundedChart = pUnbounded.createHistogramChart("UnboundedTitle", unboundedSeries, new int[0]);
        final XYPlot unboundedPlot = unboundedChart.getXYPlot();

        Assert.assertNull(unboundedPlot.getDomainMarkers(Layer.BACKGROUND));
        Assert.assertTrue(unboundedPlot.getAnnotations().isEmpty());
    }

    /**
     * Verifies RequestDataProcessor when runtime interval boundaries are empty (unbounded histogram mode)
     * and when chart generation is both enabled and disabled.
     */
    @Test
    public void testRequestDataProcessorWithoutBoundariesAndChartsDisabled() throws Exception
    {
        config.setRuntimeIntervalBoundaries(new int[0]);
        config.setChartStartTime(5_000L);
        config.setChartEndTime(5_200L); // duration < 1s -> clamped to 1s via Math.max(..., 1)

        final RequestDataProcessor pWithCharts = new RequestDataProcessor("NoBounds", requestsProvider, false);
        pWithCharts.processDataRecord((Data) createRequest("NoBounds", 5000L, 120, false, 200, 512, "http://host/a"));
        // Also exercise non-RequestData fallback in processDataRecord(Data)
        pWithCharts.processDataRecord((Data) createAction("NoBounds", 5100L, 80, false));

        final RequestReport report1 = (RequestReport) pWithCharts.createTimerReport(true);
        TaskManager.getInstance().waitForAllTasksToComplete();

        final File noBoundsHistogram = new File(pWithCharts.getChartDir(), "NoBounds_Histogram.webp");
        Assert.assertTrue("Expected unbounded histogram chart file", noBoundsHistogram.isFile() && noBoundsHistogram.length() > 0);
        Assert.assertEquals(2, report1.count);
        Assert.assertEquals(0, report1.countPerInterval.length);
        Assert.assertEquals(0, report1.percentagePerInterval.length);
        Assert.assertEquals(0, report1.urls.total);
        Assert.assertTrue(report1.urls.list.isEmpty());
        // With duration clamped to 1s, countPerSecond equals totalCount
        Assert.assertEquals(512.0, report1.bytesReceived.countPerSecond.doubleValue(), 0.001);

        // Now test with charts disabled and merging two processors via merge(BasicTimerDataProcessor)
        config.disableChartsGeneration();
        final RequestDataProcessor pNoCharts1 = new RequestDataProcessor("NoCharts", requestsProvider, false);
        final RequestDataProcessor pNoCharts2 = new RequestDataProcessor("NoCharts", requestsProvider, true);
        pNoCharts1.processDataRecord(createRequest("NoCharts", 5000L, 50, false, 200, 100, "http://host/1", 1));
        pNoCharts2.processDataRecord(createRequest("NoCharts", 5100L, 70, false, 200, 200, "http://host/2", 5));

        pNoCharts1.merge((BasicTimerDataProcessor) pNoCharts2);
        pNoCharts1.merge((BasicTimerDataProcessor) null);
        pNoCharts1.merge((RequestDataProcessor) null);
        // Because pNoCharts1 has countDistinctUrls == false, merging pNoCharts2 must not record URLs into pNoCharts1
        Assert.assertEquals(0, pNoCharts1.getDistinctUrlCount());
        // Merge a non-RequestDataProcessor BasicTimerDataProcessor
        final TransactionDataProcessor txnProc = new TransactionDataProcessor("Txn", transactionsProvider);
        txnProc.processDataRecord(createTransaction("Txn", 5050L, 60, false));
        pNoCharts1.merge((BasicTimerDataProcessor) txnProc);

        final RequestReport report2 = (RequestReport) pNoCharts1.createTimerReport(false);
        TaskManager.getInstance().waitForAllTasksToComplete();
        Assert.assertFalse("Expected no ResponseSize chart when charts are disabled",
                           new File(pNoCharts1.getChartDir(), "NoCharts_ResponseSize.webp").exists());
        Assert.assertFalse("Expected no Histogram chart when charts are disabled",
                           new File(pNoCharts1.getChartDir(), "NoCharts_Histogram.webp").exists());
        Assert.assertEquals(3, report2.count);
        // Verify mergeRequestSpecific was invoked by merge(BasicTimerDataProcessor)
        Assert.assertEquals(300L, report2.bytesReceived.totalCount.longValue());

        // Also test merging when one processor has boundaries and the other does not (in both directions on fresh instances)
        config.setRuntimeIntervalBoundaries(new int[] { 100, 200 });
        final RequestDataProcessor pWithBounds = new RequestDataProcessor("MixedBounds", requestsProvider, true);
        pWithBounds.merge(pNoCharts1);
        config.setRuntimeIntervalBoundaries(new int[0]);
        final RequestDataProcessor pWithoutBounds = new RequestDataProcessor("MixedBounds2", requestsProvider, true);
        pWithoutBounds.merge(pWithBounds);
    }

    /**
     * Verifies URL deduplication edge cases: {@link RequestDataProcessor#mixHash(int)}, null URLs,
     * consecutive identical URLs, URLs differing only by #fragment, zero hash codes, wrap-around
     * linear-probing collisions, exact capacity doubling boundaries, and spilling to HllSketch above 512 URLs.
     */
    @Test
    public void testUrlDeduplicationZeroHashFragmentsAndHllSpill()
    {
        // 0. Verify mixHash bit avalanching directly
        final int inputHash = 0x12345678;
        final int expectedMixed = (inputHash ^ (inputHash >>> 16)) * 0x85ebca6b;
        Assert.assertEquals(expectedMixed, RequestDataProcessor.mixHash(inputHash));

        // Verify wrap-around linear probing in slot 15 (of 16) and slot 31 (of 32)
        final RequestDataProcessor probeProc = new RequestDataProcessor("ProbeTest", requestsProvider, true);
        final int[] collidingAt31 = new int[10];
        int found = 0;
        for (int candidate = 1; found < collidingAt31.length; candidate++)
        {
            if ((RequestDataProcessor.mixHash(candidate) & 31) == 31)
            {
                collidingAt31[found++] = candidate;
            }
        }
        // Insert 8 colliding hashes (all map to slot 15 in the 16-slot table and wrap around via (slot + 1) & mask)
        for (int i = 0; i < 8; i++)
        {
            probeProc.recordUrlHash(collidingAt31[i]);
            probeProc.recordUrlHash(collidingAt31[i]);
            Assert.assertEquals(i + 1, probeProc.getDistinctUrlCount());
        }
        // At 8 elements (50% of 16), the table must NOT have grown yet
        Assert.assertEquals(RequestDataProcessor.INITIAL_URL_HASH_CAPACITY, probeProc.distinctUrlHashes.length);
        // Inserting the 9th element exceeds 50% load factor and doubles capacity to 32, re-probing from slot 31 -> 0..8
        probeProc.recordUrlHash(collidingAt31[8]);
        Assert.assertEquals(32, probeProc.distinctUrlHashes.length);
        Assert.assertEquals(9, probeProc.getDistinctUrlCount());
        for (int i = 0; i < 9; i++)
        {
            probeProc.recordUrlHash(collidingAt31[i]);
            Assert.assertEquals("Expected hash to be found after growUrlHashes", 9, probeProc.getDistinctUrlCount());
        }

        final RequestDataProcessor proc = new RequestDataProcessor("UrlEdgeCases", requestsProvider, true);

        // 1. Null URL is ignored (both initially when lastUrl == null and after a non-null URL when lastUrl != null)
        proc.processDataRecord(createRequest("UrlEdgeCases", 1000L, 10, false, 200, 100, null));
        Assert.assertEquals(0, proc.getDistinctUrlCount());

        // 2. Empty URL "" or "#frag" produces hashCodeOfUrlWithoutFragment() == 0
        final RequestData zeroHashReq1 = createRequest("UrlEdgeCases", 1010L, 10, false, 200, 100, "#frag1");
        final RequestData zeroHashReq2 = createRequest("UrlEdgeCases", 1020L, 10, false, 200, 100, "#frag2");
        Assert.assertEquals(0, zeroHashReq1.hashCodeOfUrlWithoutFragment());
        Assert.assertEquals(0, zeroHashReq2.hashCodeOfUrlWithoutFragment());

        proc.processDataRecord(zeroHashReq1);
        // Null URL after a non-null URL (when lastUrl != null) must also be ignored without throwing NPE
        proc.processDataRecord(createRequest("UrlEdgeCases", 1015L, 10, false, 200, 100, null));
        // Consecutive identical reference fast-path (url == lastUrl)
        proc.processDataRecord(zeroHashReq1);
        // Different #fragment on the same base URL: total distinct fragment-free URLs stays 1,
        // but both full URLs are captured in the sample URL list!
        proc.processDataRecord(zeroHashReq2);
        // Repeat zeroHashReq1 so addSampleUrl sees an already-present URL in distinctUrlSet
        proc.processDataRecord(zeroHashReq1);

        Assert.assertEquals(1, proc.getDistinctUrlCount());

        // 3. Add URLs with non-zero hashes that share fragment-free base URL
        final RequestData fragA = createRequest("UrlEdgeCases", 1030L, 10, false, 200, 100, "http://example.com/item#a");
        final RequestData fragB = createRequest("UrlEdgeCases", 1040L, 10, false, 200, 100, "http://example.com/item#b");
        proc.processDataRecord(fragA);
        proc.processDataRecord(fragB);
        proc.processDataRecord(fragA);
        Assert.assertEquals(2, proc.getDistinctUrlCount());

        // 4. Grow through all exact table capacities up to MAX_EXACT_URL_HASHES (512 non-zero + 1 zero = 513)
        final RequestDataProcessor procWithoutZero = new RequestDataProcessor("NoZero", requestsProvider, true);
        procWithoutZero.processDataRecord(fragA);
        for (int i = 1; i < RequestDataProcessor.MAX_EXACT_URL_HASHES; i++)
        {
            final String u = "http://example.com/page/" + i;
            proc.processDataRecord(createRequest("UrlEdgeCases", 1100L + i, 10, false, 200, 100, u));
            procWithoutZero.processDataRecord(createRequest("NoZero", 1100L + i, 10, false, 200, 100, u));
            // Duplicate with a different preceding URL to test open-addressed table hit
            if (i == 5)
            {
                proc.processDataRecord(createRequest("UrlEdgeCases", 1100L + i, 10, false, 200, 100, "http://example.com/page/1"));
                proc.processDataRecord(createRequest("UrlEdgeCases", 1100L + i, 10, false, 200, 100, u));
            }
        }
        // At exactly 512 non-zero URLs, neither processor has spilled to HLL yet
        Assert.assertNull(proc.distinctUrlsHLL);
        Assert.assertNull(procWithoutZero.distinctUrlsHLL);
        Assert.assertEquals(RequestDataProcessor.MAX_EXACT_URL_HASHES + 1, proc.getDistinctUrlCount());
        Assert.assertEquals(RequestDataProcessor.MAX_EXACT_URL_HASHES, procWithoutZero.getDistinctUrlCount());

        // Verify the 4 URLs with distinct fragments (#frag1, #frag2, #a, #b) were all recorded in sample URLs and capped at 10
        final RequestReport beforeSpillReport = (RequestReport) proc.createTimerReport(false);
        Assert.assertEquals(10, beforeSpillReport.urls.list.size());
        Assert.assertTrue(beforeSpillReport.urls.list.contains("#frag1"));
        Assert.assertTrue(beforeSpillReport.urls.list.contains("#frag2"));
        Assert.assertTrue(beforeSpillReport.urls.list.contains("http://example.com/item#a"));
        Assert.assertTrue(beforeSpillReport.urls.list.contains("http://example.com/item#b"));

        // 5. Trigger spill to HllSketch on the 513th non-zero URL hash (comparing with and without hasZeroUrlHash)
        final String spillUrl = "http://example.com/page/spill_513";
        proc.processDataRecord(createRequest("UrlEdgeCases", 2000L, 10, false, 200, 100, spillUrl));
        procWithoutZero.processDataRecord(createRequest("NoZero", 2000L, 10, false, 200, 100, spillUrl));
        Assert.assertNotNull(proc.distinctUrlsHLL);
        Assert.assertNotNull(procWithoutZero.distinctUrlsHLL);

        // At HLL_LOG_K=20, 513 and 514 distinct items have exact integer estimates (513 and 514)
        Assert.assertEquals(513, procWithoutZero.getDistinctUrlCount());
        Assert.assertEquals(514, proc.getDistinctUrlCount());

        // Re-insert an already-filtered URL in HLL mode (hits the 1024-slot direct-mapped filter)
        proc.recordUrlHash(spillUrl.hashCode());
        proc.processDataRecord(createRequest("UrlEdgeCases", 2001L, 10, false, 200, 100, "http://example.com/page/1"));
        proc.processDataRecord(createRequest("UrlEdgeCases", 2002L, 10, false, 200, 100, spillUrl));
        // Re-record zero hash in HLL mode
        proc.recordUrlHash(0);
        Assert.assertEquals(514, proc.getDistinctUrlCount());
    }

    /**
     * Verifies all merge combinations between exact-mode and HLL-spilled RequestDataProcessors:
     * (exact + exact), (exact + HLL), (empty + HLL), (HLL + exact), and (HLL + HLL).
     */
    @Test
    public void testMergeUrlHashStatesExactAndHll()
    {
        // Processor A: spills to HLL with 1,500 non-zero hashes (without zero hash)
        final RequestDataProcessor pHll1 = new RequestDataProcessor("MergeHll", requestsProvider, true);
        for (int i = 1; i <= 1500; i++)
        {
            pHll1.recordUrlHash(i * 17);
        }
        final int countHll1 = pHll1.getDistinctUrlCount();
        Assert.assertTrue("Expected ~1500 in pHll1, got " + countHll1, countHll1 >= 1495 && countHll1 <= 1505);

        // Processor B: exact mode with zero hash and 50 non-zero hashes disjoint from pHll1
        final RequestDataProcessor pExactWithZero = new RequestDataProcessor("MergeHll", requestsProvider, true);
        pExactWithZero.recordUrlHash(0);
        for (int i = 1; i <= 50; i++)
        {
            pExactWithZero.recordUrlHash(900_000 + i);
        }

        // Merge exact (with zero hash) into another exact processor
        final RequestDataProcessor pTargetExact = new RequestDataProcessor("MergeHll", requestsProvider, true);
        // Merge an empty processor into pTargetExact first
        final RequestDataProcessor pEmpty = new RequestDataProcessor("MergeHll", requestsProvider, true);
        pTargetExact.merge(pEmpty);
        Assert.assertEquals(0, pTargetExact.getDistinctUrlCount());
        pTargetExact.merge(pExactWithZero);
        Assert.assertEquals(51, pTargetExact.getDistinctUrlCount());

        // Merge HLL (other.distinctUrlsHLL != null, other.hasZeroUrlHash == false) into exact (oldHasZero == true, oldHashes != null)
        pTargetExact.merge(pHll1);
        Assert.assertTrue(pTargetExact.hasZeroUrlHash);
        Assert.assertEquals(countHll1 + 51, pTargetExact.getDistinctUrlCount());

        // Test merging HLL (without zero hash) into an exact processor that has non-zero hashes and oldHasZero == false
        // (verifies empty 0 slots in oldHashes are NOT recorded as hash 0!)
        final RequestDataProcessor pExactNoZero = new RequestDataProcessor("MergeHll", requestsProvider, true);
        pExactNoZero.recordUrlHash(888_881);
        pExactNoZero.recordUrlHash(888_882);
        final RequestDataProcessor pHllNoZero = new RequestDataProcessor("MergeHll", requestsProvider, true);
        for (int i = 1; i <= 600; i++)
        {
            pHllNoZero.recordUrlHash(i * 23);
        }
        final int countHllNoZero = pHllNoZero.getDistinctUrlCount();
        pExactNoZero.merge(pHllNoZero);
        Assert.assertFalse("Empty 0 slots in oldHashes must not set hasZeroUrlHash", pExactNoZero.hasZeroUrlHash);
        Assert.assertEquals(countHllNoZero + 2, pExactNoZero.getDistinctUrlCount());
        // Now record 0 while already in HLL mode (covers hasZeroUrlHash == false && distinctUrlsHLL != null)
        pExactNoZero.recordUrlHash(0);
        Assert.assertTrue(pExactNoZero.hasZeroUrlHash);
        Assert.assertEquals(countHllNoZero + 3, pExactNoZero.getDistinctUrlCount());

        // Also test merging HLL into a completely empty exact processor (oldHasZero == false, oldHashes == null)
        final RequestDataProcessor pHll2 = new RequestDataProcessor("MergeHll", requestsProvider, true);
        for (int i = 1; i <= 1500; i++)
        {
            pHll2.recordUrlHash(i * 31 + 100_000);
        }
        final RequestDataProcessor pEmptyTarget = new RequestDataProcessor("MergeHll", requestsProvider, true);
        pEmptyTarget.merge(pHll2);
        Assert.assertTrue(pEmptyTarget.getDistinctUrlCount() >= 1495);
        Assert.assertFalse(pEmptyTarget.hasZeroUrlHash);

        // Build a reference processor with the exact union of pEmptyTarget + pTargetExact to verify HLL+HLL Union merge
        final RequestDataProcessor pUnionRef = new RequestDataProcessor("MergeHll", requestsProvider, true);
        for (int i = 1; i <= 1500; i++)
        {
            pUnionRef.recordUrlHash(i * 31 + 100_000);
        }
        pUnionRef.recordUrlHash(0);
        for (int i = 1; i <= 50; i++)
        {
            pUnionRef.recordUrlHash(900_000 + i);
        }
        for (int i = 1; i <= 1500; i++)
        {
            pUnionRef.recordUrlHash(i * 17);
        }

        // Merge pTargetExact (which has hasZeroUrlHash == true) into pEmptyTarget (which has hasZeroUrlHash == false)
        pEmptyTarget.merge(pTargetExact);
        Assert.assertTrue(pEmptyTarget.hasZeroUrlHash);
        Assert.assertEquals(pUnionRef.getDistinctUrlCount(), pEmptyTarget.getDistinctUrlCount());

        // Also merge an exact processor into an already-HLL processor
        final RequestDataProcessor pSmallExact = new RequestDataProcessor("MergeHll", requestsProvider, true);
        pSmallExact.recordUrlHash(777_777);
        final int beforeSmall = pEmptyTarget.getDistinctUrlCount();
        pEmptyTarget.merge(pSmallExact);
        Assert.assertEquals(beforeSmall + 1, pEmptyTarget.getDistinctUrlCount());
    }

    /**
     * Verifies L1, L2, and L3 processor caching, alphabetical sorting of {@link AbstractDataProcessorBasedReportProvider#getProcessors()},
     * worker cache clearing during merge, null timer name handling, and reflection failure handling.
     */
    @Test
    public void testAbstractDataProcessorBasedReportProviderCachingAndEdgeCases()
    {
        // 1. L1 cache hit (same String reference consecutively)
        final String name1 = "TimerOne";
        final RequestDataProcessor p1a = requestsProvider.getProcessor(name1);
        final RequestDataProcessor p1b = requestsProvider.getProcessor(name1);
        Assert.assertSame(p1a, p1b);

        // 2. L2 cache hit via reference equality and via String.equals (distinct String instance)
        final String name2 = "TimerTwo";
        final RequestDataProcessor p2 = requestsProvider.getProcessor(name2);
        Assert.assertSame(p1a, requestsProvider.getProcessor(name1));
        Assert.assertSame(p2, requestsProvider.getProcessor(new String("TimerTwo")));

        // 3. L2 slot collision -> L3 map lookup hit for an already-created processor
        final int targetSlot = name1.hashCode() & 31;
        String collidingName = null;
        for (int i = 0; i < 500; i++)
        {
            final String candidate = "A_Collider_" + i;
            if ((candidate.hashCode() & 31) == targetSlot && !candidate.equals(name1))
            {
                collidingName = candidate;
                break;
            }
        }
        Assert.assertNotNull(collidingName);
        final RequestDataProcessor pCollider = requestsProvider.getProcessor(collidingName);
        Assert.assertNotSame(p1a, pCollider);
        // Now requesting name1 again misses L1 and L2 (occupied by collidingName), hitting L3 map!
        Assert.assertSame(p1a, requestsProvider.getProcessor(name1));

        // 4. Null timer name fallback (first call instantiates, second call returns cached nullProcessor)
        final RequestDataProcessor pNull1 = requestsProvider.getProcessor(null);
        final RequestDataProcessor pNull2 = requestsProvider.getProcessor(null);
        Assert.assertNotNull(pNull1);
        Assert.assertSame(pNull1, pNull2);

        // 5. Base processAll delegation and sorted order of getProcessors()
        final PostProcessedDataContainer nonRequestContainer = new PostProcessedDataContainer(4, 1);
        nonRequestContainer.typeCode = 'A';
        nonRequestContainer.add(createRequest("B_FromContainer", 2000L, 25, false, 200, 100, "http://host/c"));
        requestsProvider.processAll(nonRequestContainer);

        final Collection<RequestDataProcessor> allProcs = requestsProvider.getProcessors();
        Assert.assertEquals(4, allProcs.size());
        final Iterator<RequestDataProcessor> it = allProcs.iterator();
        Assert.assertEquals(collidingName, it.next().getName());
        Assert.assertEquals("B_FromContainer", it.next().getName());
        Assert.assertEquals("TimerOne", it.next().getName());
        Assert.assertEquals("TimerTwo", it.next().getName());

        // 6. Verify merge clears worker L1/L2 caches and map entries
        final RequestsReportProvider workerProvider = new RequestsReportProvider();
        workerProvider.setConfiguration(config);
        final RequestDataProcessor workerProc = workerProvider.getProcessor("WorkerTimer");
        workerProc.processDataRecord(createRequest("WorkerTimer", 3000L, 40, false, 200, 128, "http://host/w"));
        Assert.assertNotNull(workerProvider.lastTimerName);
        Assert.assertNotNull(workerProvider.lastProcessor);

        requestsProvider.merge(workerProvider);
        Assert.assertNull(workerProvider.lastTimerName);
        Assert.assertNull(workerProvider.lastProcessor);
        Assert.assertEquals(0, workerProvider.processors.size());
        for (int i = 0; i < workerProvider.cachedNames.length; i++)
        {
            Assert.assertNull(workerProvider.cachedNames[i]);
            Assert.assertNull(workerProvider.cachedProcessors[i]);
        }
        // Adopted processor must now point to requestsProvider, not workerProvider
        Assert.assertSame(requestsProvider, workerProc.getReportProvider());

        requestsProvider.merge((com.xceptance.xlt.api.report.ReportProvider) new GeneralReportProvider());
        requestsProvider.merge((AbstractDataProcessorBasedReportProvider<RequestDataProcessor>) null);

        // 7. Reflection instantiation failure when processor class lacks expected constructor
        class BrokenProcessor extends AbstractDataProcessor
        {
            BrokenProcessor()
            {
                super("broken", requestsProvider);
            }

            @Override
            public void processDataRecord(final Data data)
            {
            }

            @Override
            public void merge(final AbstractDataProcessor other)
            {
            }
        }

        final AbstractDataProcessorBasedReportProvider<BrokenProcessor> brokenProvider =
            new AbstractDataProcessorBasedReportProvider<BrokenProcessor>(BrokenProcessor.class)
            {
                @Override
                public Object createReportFragment()
                {
                    return null;
                }
            };
        try
        {
            brokenProvider.getProcessor("test");
            Assert.fail("Expected RuntimeException when processor constructor is missing");
        }
        catch (final RuntimeException expected)
        {
            Assert.assertTrue(expected.getMessage().contains("Failed to instantiate processor"));
        }
    }

    @Test
    public void testTransactionDataProcessorMerge()
    {
        final TransactionDataProcessor p1 = new TransactionDataProcessor("Txn1", transactionsProvider);
        final TransactionDataProcessor p2 = new TransactionDataProcessor("Txn1", transactionsProvider);
        final TransactionDataProcessor pExpected = new TransactionDataProcessor("Txn1", transactionsProvider);

        for (int i = 0; i < 40; i++)
        {
            final TransactionData txn = createTransaction("Txn1", 5000L + i * 200, 500 + i * 10, i % 5 == 0);
            p1.processDataRecord(txn);
            pExpected.processDataRecord(txn);
        }

        for (int i = 40; i < 100; i++)
        {
            final TransactionData txn = createTransaction("Txn1", 5000L + i * 200, 500 + i * 10, i % 8 == 0);
            p2.processDataRecord(txn);
            pExpected.processDataRecord(txn);
        }

        p1.merge(p2);

        final TransactionReport rActual = (TransactionReport) p1.createTimerReport(false);
        final TransactionReport rExpected = (TransactionReport) pExpected.createTimerReport(false);

        Assert.assertEquals("Count mismatch", rExpected.count, rActual.count);
        Assert.assertEquals("Errors mismatch", rExpected.errors, rActual.errors);
        Assert.assertEquals("Min runtime mismatch", rExpected.min, rActual.min);
        Assert.assertEquals("Max runtime mismatch", rExpected.max, rActual.max);
        Assert.assertEquals("Mean runtime mismatch", rExpected.mean.doubleValue(), rActual.mean.doubleValue(), 0.001);
    }

    @Test
    public void testActionDataProcessorMerge()
    {
        final ActionDataProcessor p1 = new ActionDataProcessor("Act1", actionsProvider);
        final ActionDataProcessor p2 = new ActionDataProcessor("Act1", actionsProvider);
        final ActionDataProcessor pExpected = new ActionDataProcessor("Act1", actionsProvider);

        for (int i = 0; i < 30; i++)
        {
            final ActionData act = createAction("Act1", 2000L + i * 300, 200 + i * 20, i % 6 == 0);
            p1.processDataRecord(act);
            pExpected.processDataRecord(act);
        }

        for (int i = 30; i < 75; i++)
        {
            final ActionData act = createAction("Act1", 2000L + i * 300, 200 + i * 20, i % 4 == 0);
            p2.processDataRecord(act);
            pExpected.processDataRecord(act);
        }

        p1.merge(p2);

        final ActionReport rActual = (ActionReport) p1.createTimerReport(false);
        final ActionReport rExpected = (ActionReport) pExpected.createTimerReport(false);

        Assert.assertEquals("Count mismatch", rExpected.count, rActual.count);
        Assert.assertEquals("Errors mismatch", rExpected.errors, rActual.errors);
        Assert.assertEquals("Min runtime mismatch", rExpected.min, rActual.min);
        Assert.assertEquals("Max runtime mismatch", rExpected.max, rActual.max);
        Assert.assertEquals("Mean runtime mismatch", rExpected.mean.doubleValue(), rActual.mean.doubleValue(), 0.001);
        Assert.assertEquals("Apdex mismatch", rExpected.apdex.value.doubleValue(), rActual.apdex.value.doubleValue(), 0.01);
    }
}
