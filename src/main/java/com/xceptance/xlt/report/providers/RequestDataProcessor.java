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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.xceptance.common.collection.FastHashMap;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.labelingrules.LabelingRuleProcessor;
import com.xceptance.xlt.report.util.HistogramValueSet;
import com.xceptance.xlt.report.util.IntMinMaxValueSet;
import com.xceptance.xlt.report.util.IntSummaryStatistics;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.SegmentationValueSet;
import com.xceptance.xlt.report.util.TaskManager;

/**
 * The {@link RequestDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link RequestData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * <li>creation of CSV files with all the data</li>
 * </ul>
 */
public class RequestDataProcessor extends BasicTimerDataProcessor
{
    /**
     * The maximum number of distinct URLs stored for each request name.
     */
    private static final int MAXIMUM_NUMBER_OF_URLS = 10;

    /**
     * Initial capacity of the open-addressed exact URL hash table (must be a power of two).
     * Starts at 16 ints (64 bytes) and only grows if a timer observes more than 8 distinct URLs.
     */
    static final int INITIAL_URL_HASH_CAPACITY = 16;

    /**
     * Maximum capacity of the open-addressed exact URL hash table (must be a power of two).
     * At a 50% maximum load factor, this holds up to {@link #MAX_EXACT_URL_HASHES} (512) distinct
     * non-zero URL hash codes in 4 KB before spilling into a 4 KB {@link #distinctUrlsHLL} register array.
     */
    static final int MAX_EXACT_URL_HASH_CAPACITY = 1024;

    /**
     * Maximum number of distinct non-zero URL hash codes tracked exactly before spilling to {@link #distinctUrlsHLL}.
     */
    static final int MAX_EXACT_URL_HASHES = MAX_EXACT_URL_HASH_CAPACITY >>> 1;

    /**
     * Log2 of the number of HyperLogLog registers (12 bits = 4,096 registers = 4 KB per spilled timer).
     */
    static final int HLL_PRECISION = 12;

    /**
     * Number of HyperLogLog registers ({@code 1 << HLL_PRECISION} = 4,096).
     */
    static final int HLL_REGISTERS = 1 << HLL_PRECISION;

    /**
     * Precomputed bias-correction constant {@code alpha_m * m^2} for {@link #HLL_REGISTERS} = 4,096.
     */
    private static final double HLL_ALPHA_MM = (0.7213 / (1.0 + 1.079 / HLL_REGISTERS)) * HLL_REGISTERS * HLL_REGISTERS;

    /**
     * The value set holding the bytes received.
     */
    final IntMinMaxValueSet responseSizeValueSet;

    /**
     * Lazily allocated 4,096-byte HyperLogLog register array used only when a request timer exceeds
     * {@link #MAX_EXACT_URL_HASHES} (512) distinct non-zero URL hash codes. Replaces the 4 KB
     * {@link #distinctUrlHashes} table with zero net heap growth and zero object allocations on update or merge.
     */
    byte[] distinctUrlsHLL;

    /**
     * Lazily allocated open-addressed linear-probing hash set (capacity {@code 16 .. 1024} at 50% load factor)
     * storing up to {@link #MAX_EXACT_URL_HASHES} (512) distinct non-zero URL hash codes while
     * {@link #distinctUrlsHLL} is {@code null}. Freed ({@code null}) once spilled to {@link #distinctUrlsHLL}.
     */
    int[] distinctUrlHashes;

    /**
     * Exact count of distinct non-zero URL hash codes stored in {@link #distinctUrlHashes} while
     * {@link #distinctUrlsHLL} is {@code null}.
     */
    private int distinctUrlHashCount;

    /**
     * Whether a URL with {@code hashCodeOfUrlWithoutFragment() == 0} has been observed.
     * Tracked separately so {@code 0} can serve as the empty-slot sentinel in {@link #distinctUrlHashes}.
     */
    boolean hasZeroUrlHash;

    /**
     * Single-item URL reference cache to bypass sample URL map lookups for consecutive identical URLs.
     */
    private String lastUrl;

    /**
     * Single-item URL hash cache to bypass hash set / HLL updates for consecutive identical URL hashes.
     */
    private int lastUrlHash;

    /**
     * A set of distinct URLs. Contains at most {@link #MAXIMUM_NUMBER_OF_URLS} entries.
     */
    private final FastHashMap<String, String> distinctUrlSet = new FastHashMap<>(MAXIMUM_NUMBER_OF_URLS, 0.5f);

    /**
     * The configured runtime segment boundaries. May be an empty array.
     */
    private final int[] boundaries;

    /**
     * The request counts per segment. Will be <code>null</code> if no segment boundaries have been configured.
     */
    private final SegmentationValueSet countPerSegment;

    /**
     * The histogram value set limited by the highest runtime segment boundary. Will be <code>null</code> if no segment
     * boundaries have been configured.
     */
    final HistogramValueSet runTimeHistogramValueSet;

    /**
     * The statistics for the "bytesSent" values.
     */
    private final IntSummaryStatistics bytesSentStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "bytesReceived" values.
     */
    private final IntSummaryStatistics bytesReceivedStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "connectTime" values.
     */
    private final IntSummaryStatistics connectTimeStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "sendTime" values.
     */
    private final IntSummaryStatistics sendTimeStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "serverBusyTime" values.
     */
    private final IntSummaryStatistics serverBusyTimeStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "receiveTime" values.
     */
    private final IntSummaryStatistics receiveTimeStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "timeToFirstBytes" values.
     */
    private final IntSummaryStatistics timeToFirstBytesStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "timeToLastBytes" values.
     */
    private final IntSummaryStatistics timeToLastBytesStatistics = new IntSummaryStatistics();

    /**
     * The statistics for the "dnsTime" values.
     */
    private final IntSummaryStatistics dnsTimeStatistics = new IntSummaryStatistics();

    /**
     * Whether distinct URLs should be counted.
     */
    private final boolean countDistinctUrls;

    /**
     * Avoid to ask the set again for the size
     */
    private int distinctUrlSetLimitedSize;

    private final LabelingRuleProcessor labelingRuleProcessor;

    /**
     * Constructor.
     *
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public <T extends AbstractDataProcessor> RequestDataProcessor(final String name, final AbstractReportProvider provider)
    {
        this(name, provider, true);
    }

    /**
     * Constructor.
     *
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     * @param countDistinctUrls
     *            whether to count distinct URLs
     */
    public <T extends AbstractDataProcessor> RequestDataProcessor(final String name, final AbstractReportProvider provider,
                                                                  final boolean countDistinctUrls)
    {
        super(name, provider);

        this.countDistinctUrls = countDistinctUrls;

        setChartDir(new File(getChartDir(), "requests"));
        setCsvDir(new File(getCsvDir(), "requests"));

        // setup histogram value set and segment counter (only if needed)
        final ReportGeneratorConfiguration config = (ReportGeneratorConfiguration) getConfiguration();

        boundaries = config.getRuntimeIntervalBoundaries();
        if (boundaries.length > 0)
        {
            final int numberOfBins = 101;
            final double maxValue = ((double) boundaries[boundaries.length - 1]) * numberOfBins / (numberOfBins - 1);

            runTimeHistogramValueSet = new HistogramValueSet(0, maxValue, numberOfBins);
            countPerSegment = new SegmentationValueSet(boundaries);
        }
        else
        {
            runTimeHistogramValueSet = null;
            countPerSegment = null;
        }

        // setup response size value set
        final int minMaxValueSetSize = getChartWidth();
        responseSizeValueSet = new IntMinMaxValueSet(minMaxValueSetSize);

        // set capping parameters
        setChartCappingInfo(config.getRequestChartCappingInfo());

        // labeling rules
        labelingRuleProcessor = new LabelingRuleProcessor(config.getLabelingRules());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerReport createTimerReport(final boolean generateHistograms)
    {
        // create the charts
        if (getConfiguration().shouldChartsGenerated())
        {
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final TimeSeries responseSizeTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(responseSizeValueSet, "Response Size");

                    createResponseSizeChart(getName(), responseSizeTimeSeries);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    // determine which histogram series to use
                    final String seriesName = "Number of Requests";
                    final XYIntervalSeries histogramSeries;

                    if (runTimeHistogramValueSet != null)
                    {
                        // use the one bounded by the runtime segmentation
                        histogramSeries = runTimeHistogramValueSet.toSeries(seriesName);
                    }
                    else
                    {
                        // use the unbounded one, maintained by the super class
                        histogramSeries = getTimeSeries().toVerticalHistogramSeries(seriesName, getChartHeight());
                    }

                    saveResponseTimeHistogramChart(getName(), histogramSeries, boundaries);
                }
            });
        }

        // create the timer report
        final RequestReport timerReport = (RequestReport) super.createTimerReport(generateHistograms);

        // just int is safe, more than 2 billion urls is unlikely
        timerReport.urls = getUrlList(distinctUrlSet, getDistinctUrlCount());
        timerReport.countPerInterval = countPerSegment != null ? countPerSegment.getCountPerSegment() : ArrayUtils.EMPTY_INT_ARRAY;
        timerReport.percentagePerInterval = countPerSegment != null ? new BigDecimal[countPerSegment.getCountPerSegment().length]
                                                                    : new BigDecimal[] {};
        if (countPerSegment != null)
        {
            for (int n = 0; n < countPerSegment.getCountPerSegment().length; n++)
            {
                timerReport.percentagePerInterval[n] = ReportUtils.calculatePercentage(countPerSegment.getCountPerSegment()[n],
                                                                                       timerReport.count);
            }
        }

        final long duration = Math.max((getConfiguration().getChartEndTime() - getConfiguration().getChartStartTime()) / 1000, 1);

        timerReport.bytesSent = createExtendedStatisticsReport(bytesSentStatistics, duration);
        timerReport.bytesReceived = createExtendedStatisticsReport(bytesReceivedStatistics, duration);
        timerReport.dnsTime = createStatisticsReport(dnsTimeStatistics);
        timerReport.connectTime = createStatisticsReport(connectTimeStatistics);
        timerReport.sendTime = createStatisticsReport(sendTimeStatistics);
        timerReport.serverBusyTime = createStatisticsReport(serverBusyTimeStatistics);
        timerReport.receiveTime = createStatisticsReport(receiveTimeStatistics);
        timerReport.timeToFirstBytes = createStatisticsReport(timeToFirstBytesStatistics);
        timerReport.timeToLastBytes = createStatisticsReport(timeToLastBytesStatistics);

        // apply labeling rules
        labelingRuleProcessor.process(timerReport);

        return timerReport;
    }

    /**
     * High-performance processing of a strongly typed {@link RequestData} record.
     * <p>
     * <b>Performance Design Rationale:</b>
     * Direct invocation with {@link RequestData} bypasses the generic {@link #processDataRecord(Data)}
     * interface method, eliminating double-casting (first to {@code TimerData} in the superclass,
     * then to {@code RequestData} here). All field accesses (endpoints, timestamps, durations, and byte
     * counts) occur on the direct concrete reference, allowing the JIT compiler to inline and vectorize
     * accumulator updates.
     *
     * @param reqData
     *            the concrete HTTP request data record to process
     */
    public void processDataRecord(final RequestData reqData)
    {
        final long endTime = reqData.getEndTime();
        final int runTime = reqData.getRunTime();
        final boolean failed = reqData.hasFailed();

        // Record response time data directly into the time series without superclass dispatch
        timeSeries.addValue(endTime, runTime, failed);

        if (runTimeHistogramValueSet != null)
        {
            runTimeHistogramValueSet.addValue(runTime);
            countPerSegment.addValue(runTime);
        }

        responseSizeValueSet.addOrUpdateValue(endTime, reqData.getBytesReceived());

        if (countDistinctUrls)
        {
            if (reqData.hasUrlBytes() || reqData.getUrl() != null)
            {
                final int urlHash = reqData.hashCodeOfUrlWithoutFragment();
                if (urlHash != lastUrlHash || lastUrl == null)
                {
                    recordUrlHash(urlHash);
                    lastUrlHash = urlHash;
                }

                // Remember up to MAXIMUM_NUMBER_OF_URLS sample URLs.
                // Only materialize reqData.getUrl() while the sample set has fewer than 10 entries;
                // once 10 sample URLs are collected, avoid allocating String instances from raw CSV urlBytes.
                if (distinctUrlSetLimitedSize < MAXIMUM_NUMBER_OF_URLS)
                {
                    final String url = reqData.getUrl();
                    if (url != null && url != lastUrl)
                    {
                        addSampleUrl(url);
                        lastUrl = url;
                    }
                }
                else if (lastUrl == null)
                {
                    lastUrl = "";
                }
            }
        }

        bytesSentStatistics.addValue(reqData.getBytesSent());
        bytesReceivedStatistics.addValue(reqData.getBytesReceived());
        dnsTimeStatistics.addValue(reqData.getDnsTime());
        connectTimeStatistics.addValue(reqData.getConnectTime());
        sendTimeStatistics.addValue(reqData.getSendTime());
        serverBusyTimeStatistics.addValue(reqData.getServerBusyTime());
        receiveTimeStatistics.addValue(reqData.getReceiveTime());
        timeToFirstBytesStatistics.addValue(reqData.getTimeToFirstBytes());
        timeToLastBytesStatistics.addValue(reqData.getTimeToLastBytes());
    }

    /**
     * Adds a sample URL string to {@link #distinctUrlSet} and updates {@link #distinctUrlSetLimitedSize}.
     */
    private void addSampleUrl(final String url)
    {
        distinctUrlSet.put(url, url);
        distinctUrlSetLimitedSize = distinctUrlSet.size();
    }

    /**
     * Mixes a 32-bit hash code using Murmur3-style bit avalanching so URLs sharing common prefixes
     * or sequential suffixes distribute uniformly across power-of-two table slots.
     */
    static int mixHash(final int h)
    {
        final int x = h ^ (h >>> 16);
        return x * 0x85ebca6b;
    }

    /**
     * Updates a 4,096-register HyperLogLog sketch with a 32-bit URL hash code using 64-bit SplitMix64
     * bit avalanching and zero heap allocations.
     */
    static void updateHll(final byte[] registers, final int urlHash)
    {
        long z = (urlHash & 0xFFFFFFFFL) + 0x9e3779b97f4a7c15L;
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        z = z ^ (z >>> 31);

        final int idx = (int) (z >>> (64 - HLL_PRECISION));
        final long w = (z << HLL_PRECISION) | (1L << (HLL_PRECISION - 1));
        final byte rho = (byte) (Long.numberOfLeadingZeros(w) + 1);
        if (rho > registers[idx])
        {
            registers[idx] = rho;
        }
    }

    /**
     * Estimates the distinct URL cardinality from a 4,096-register HyperLogLog array using
     * small-range LinearCounting and large-range harmonic mean estimation.
     */
    static int estimateHll(final byte[] registers)
    {
        double sum = 0.0;
        int zeros = 0;
        for (int i = 0; i < HLL_REGISTERS; i++)
        {
            final int val = registers[i];
            sum += 1.0 / (1L << val);
            if (val == 0)
            {
                zeros++;
            }
        }

        double estimate = HLL_ALPHA_MM / sum;
        if (estimate <= 2.5 * HLL_REGISTERS && zeros > 0)
        {
            estimate = HLL_REGISTERS * Math.log((double) HLL_REGISTERS / zeros);
        }
        return (int) Math.round(estimate);
    }

    /**
     * Records a fragment-free URL hash code in the two-tier distinct URL tracker.
     *
     * @param urlHash
     *            the value of {@link RequestData#hashCodeOfUrlWithoutFragment()}
     */
    void recordUrlHash(final int urlHash)
    {
        if (urlHash == 0)
        {
            if (!hasZeroUrlHash)
            {
                hasZeroUrlHash = true;
                if (distinctUrlsHLL != null)
                {
                    updateHll(distinctUrlsHLL, 0);
                }
            }
            return;
        }

        if (distinctUrlsHLL != null)
        {
            updateHll(distinctUrlsHLL, urlHash);
            return;
        }

        if (distinctUrlHashes == null)
        {
            distinctUrlHashes = new int[INITIAL_URL_HASH_CAPACITY];
            distinctUrlHashes[mixHash(urlHash) & (INITIAL_URL_HASH_CAPACITY - 1)] = urlHash;
            distinctUrlHashCount = 1;
            return;
        }

        final int[] table = distinctUrlHashes;
        final int mask = table.length - 1;
        int slot = mixHash(urlHash) & mask;
        while (true)
        {
            final int existing = table[slot];
            if (existing == urlHash)
            {
                return;
            }
            if (existing == 0)
            {
                break;
            }
            slot = (slot + 1) & mask;
        }

        if (distinctUrlHashCount < MAX_EXACT_URL_HASHES)
        {
            table[slot] = urlHash;
            distinctUrlHashCount++;
            if (distinctUrlHashCount > (table.length >>> 1))
            {
                growUrlHashes();
            }
            return;
        }

        spillToHll();
        updateHll(distinctUrlsHLL, urlHash);
    }

    /**
     * Doubles the capacity of the open-addressed {@link #distinctUrlHashes} table and re-inserts
     * all non-zero hash codes.
     */
    private void growUrlHashes()
    {
        final int[] oldTable = distinctUrlHashes;
        final int newCapacity = oldTable.length << 1;
        final int newMask = newCapacity - 1;
        final int[] newTable = new int[newCapacity];

        for (final int h : oldTable)
        {
            if (h != 0)
            {
                int slot = mixHash(h) & newMask;
                while (newTable[slot] != 0)
                {
                    slot = (slot + 1) & newMask;
                }
                newTable[slot] = h;
            }
        }
        distinctUrlHashes = newTable;
    }

    /**
     * Transitions this processor from the exact open-addressed {@code int[]} hash set to a
     * 4 KB {@link #distinctUrlsHLL} register array once more than {@link #MAX_EXACT_URL_HASHES} (512)
     * distinct non-zero URL hash codes have been observed, releasing {@link #distinctUrlHashes}.
     */
    private void spillToHll()
    {
        final byte[] hll = new byte[HLL_REGISTERS];
        if (hasZeroUrlHash)
        {
            updateHll(hll, 0);
        }
        final int[] oldTable = distinctUrlHashes;
        for (final int h : oldTable)
        {
            if (h != 0)
            {
                updateHll(hll, h);
            }
        }
        distinctUrlHashes = null;
        distinctUrlHashCount = 0;
        distinctUrlsHLL = hll;
    }

    /**
     * Returns the exact or HyperLogLog-estimated count of distinct fragment-free URLs observed.
     */
    int getDistinctUrlCount()
    {
        if (distinctUrlsHLL != null)
        {
            return estimateHll(distinctUrlsHLL);
        }
        return distinctUrlHashCount + (hasZeroUrlHash ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            processDataRecord((RequestData) data);
        }
        else
        {
            super.processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final BasicTimerDataProcessor other)
    {
        if (other instanceof RequestDataProcessor)
        {
            merge((RequestDataProcessor) other);
        }
        else
        {
            super.merge(other);
        }
    }

    /**
     * Merges another {@link RequestDataProcessor} into this instance.
     *
     * @param other
     *            the other processor to merge
     */
    public void merge(final RequestDataProcessor other)
    {
        if (other == null)
        {
            return;
        }

        super.merge(other);
        mergeRequestSpecific(other);
    }

    private void mergeRequestSpecific(final RequestDataProcessor other)
    {
        responseSizeValueSet.merge(other.responseSizeValueSet);

        if (runTimeHistogramValueSet != null && other.runTimeHistogramValueSet != null)
        {
            runTimeHistogramValueSet.merge(other.runTimeHistogramValueSet);
            countPerSegment.merge(other.countPerSegment);
        }

        if (countDistinctUrls)
        {
            if (other.distinctUrlsHLL == null)
            {
                if (other.hasZeroUrlHash)
                {
                    recordUrlHash(0);
                }
                if (other.distinctUrlHashes != null)
                {
                    for (final int h : other.distinctUrlHashes)
                    {
                        if (h != 0)
                        {
                            recordUrlHash(h);
                        }
                    }
                }
            }
            else if (this.distinctUrlsHLL == null)
            {
                final boolean oldHasZero = this.hasZeroUrlHash;
                final int[] oldHashes = this.distinctUrlHashes;

                this.distinctUrlsHLL = other.distinctUrlsHLL;
                this.distinctUrlHashes = null;
                this.hasZeroUrlHash = other.hasZeroUrlHash;
                this.distinctUrlHashCount = 0;

                if (oldHasZero)
                {
                    recordUrlHash(0);
                }
                if (oldHashes != null)
                {
                    for (final int h : oldHashes)
                    {
                        if (h != 0)
                        {
                            recordUrlHash(h);
                        }
                    }
                }
            }
            else
            {
                final byte[] targetHll = this.distinctUrlsHLL;
                final byte[] sourceHll = other.distinctUrlsHLL;
                for (int i = 0; i < HLL_REGISTERS; i++)
                {
                    if (sourceHll[i] > targetHll[i])
                    {
                        targetHll[i] = sourceHll[i];
                    }
                }
                this.hasZeroUrlHash |= other.hasZeroUrlHash;
            }

            for (final String url : other.distinctUrlSet.keys())
            {
                if (distinctUrlSetLimitedSize < MAXIMUM_NUMBER_OF_URLS)
                {
                    addSampleUrl(url);
                }
                else
                {
                    break;
                }
            }

            other.distinctUrlsHLL = null;
            other.distinctUrlHashes = null;
            other.distinctUrlHashCount = 0;
            other.hasZeroUrlHash = false;
            other.distinctUrlSet.clear();
        }

        bytesSentStatistics.merge(other.bytesSentStatistics);
        bytesReceivedStatistics.merge(other.bytesReceivedStatistics);
        dnsTimeStatistics.merge(other.dnsTimeStatistics);
        connectTimeStatistics.merge(other.connectTimeStatistics);
        sendTimeStatistics.merge(other.sendTimeStatistics);
        serverBusyTimeStatistics.merge(other.serverBusyTimeStatistics);
        receiveTimeStatistics.merge(other.receiveTimeStatistics);
        timeToFirstBytesStatistics.merge(other.timeToFirstBytesStatistics);
        timeToLastBytesStatistics.merge(other.timeToLastBytesStatistics);
    }

    /**
     * Creates a chart from the passed bytes received values. The chart's title and file name are derived from the
     * specified timer name. The chart is generated to the charts directory.
     *
     * @param timerName
     *            the name of the timer
     * @param timeSeries
     *            the value list
     */
    protected void createResponseSizeChart(final String timerName, final TimeSeries timeSeries)
    {
        // System.out.println("Creating response size chart for timer '" + timerName + "' ... ");

        // final long start = TimerUtils.getTime();

        final JFreeChart chart = JFreeChartUtils.createLineChart(timerName, "Bytes", timeSeries, getStartTime(), getEndTime(), true,
                                                                 getCommonMovingAverageConfig());
        JFreeChartUtils.saveChart(chart, timerName + "_ResponseSize", getChartDir(), getChartWidth(), getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", timeSeries.getItemCount(), TimerUtils.getTime() - start);
    }

    /**
     * Creates a histogram chart from the given timer list and stores it to the passed directory. The histogram shows
     * the number of requests that fall into certain runtime intervals.
     *
     * @param chartTitle
     *            the chart title
     * @param histogramSeries
     *            the histogram series data
     * @param runtimeSegmentBoundaries
     *            the runtime segments to show in the chart
     */
    protected JFreeChart createHistogramChart(final String chartTitle, final XYIntervalSeries histogramSeries,
                                              final int[] runtimeSegmentBoundaries)
    {
        // create the data set
        final XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        dataset.addSeries(histogramSeries);

        // determine the maximum x-value to show in the chart from the series
        final double domainAxisUpperBound = histogramSeries.getXHighValue(histogramSeries.getItemCount() - 1);

        // create the histogram chart
        final NumberAxis xAxis = new NumberAxis("Runtime [ms]");
        xAxis.setRange(0, domainAxisUpperBound);

        final XYPlot plot = JFreeChartUtils.createBarPlot(dataset, xAxis, "Count", JFreeChartUtils.COLOR_HISTOGRAM);
        plot.setOrientation(PlotOrientation.VERTICAL);

        // draw vertical lines that mark the runtime segment boundaries
        if (boundaries.length > 0)
        {
            // draw the segment boundaries
            for (final int boundary : runtimeSegmentBoundaries)
            {
                final Marker marker = new ValueMarker(boundary);
                marker.setPaint(Color.GRAY);
                plot.addDomainMarker(marker, Layer.BACKGROUND);
            }

            // draw an annotation for the last bin
            final int maxValue = runtimeSegmentBoundaries[runtimeSegmentBoundaries.length - 1];
            final String annotationText = String.format("All requests > %,d ms", maxValue);

            final double binWidth = domainAxisUpperBound / histogramSeries.getItemCount();
            final double x = domainAxisUpperBound - binWidth / 2;
            final double y = plot.getRangeAxis().getUpperBound() * 0.75;

            final XYPointerAnnotation annotation = new XYPointerAnnotation(annotationText, x, y, 3.92);
            annotation.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            annotation.setPaint(Color.GRAY);
            annotation.setArrowPaint(Color.GRAY);
            annotation.setArrowLength(0); // do not show an arrowhead
            annotation.setTipRadius(0);   // no distance between arrow and target coordinates
            plot.addAnnotation(annotation);
        }

        final JFreeChart chart = JFreeChartUtils.createChart(chartTitle, plot);

        return chart;
    }

    /**
     * Creates a histogram chart from the run time values in the passed value set. The chart also contains indicators
     * that mark the runtime segmentation boundaries as configured in the properties.
     *
     * @param timerName
     *            the name of the timer
     * @param histogramSeries
     *            the histogram series data
     * @param boundaries
     *            the values marking the segmentation boundaries
     */
    protected void saveResponseTimeHistogramChart(final String timerName, final XYIntervalSeries histogramSeries, final int[] boundaries)
    {
        // System.out.println("Creating histogram chart for timer '" + timerName + "' ... ");

        // final long start = TimerUtils.getTime();

        final JFreeChart chart = createHistogramChart(timerName, histogramSeries, boundaries);
        JFreeChartUtils.saveChart(chart, timerName + "_Histogram", getChartDir(), getChartWidth(), getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", runtimeHistogramValueSet.getNumberOfBins(),
        // TimerUtils.getTime() - start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerReport createTimerReport()
    {
        return new RequestReport();
    }

    /**
     * Returns a list of different URLs used for requests handled by this data processor.
     *
     * @param urls
     *            a set of distinct URL strings
     * @param totalUrlCount
     *            the total number of distinct URLs
     * @return the URL list
     */
    private UrlData getUrlList(final FastHashMap<String, String> urls, final int totalUrlCount)
    {
        final UrlData urlData = new UrlData();

        urlData.total = totalUrlCount;
        urlData.list = new ArrayList<>(urls.keys());

        return urlData;
    }

    private ExtendedStatisticsReport createExtendedStatisticsReport(final IntSummaryStatistics statistics, final long duration)
    {
        final ExtendedStatisticsReport statisticsReport = new ExtendedStatisticsReport();

        statisticsReport.mean = ReportUtils.convertToBigDecimal(statistics.getMean());
        statisticsReport.min = statistics.getMinimum();
        statisticsReport.max = statistics.getMaximum();
        statisticsReport.deviation = ReportUtils.convertToBigDecimal(statistics.getStandardDeviation());

        final double sum = statistics.getSum();
        statisticsReport.totalCount = BigInteger.valueOf((long) sum);
        statisticsReport.countPerSecond = ReportUtils.convertToBigDecimal(sum / duration);
        statisticsReport.countPerMinute = ReportUtils.convertToBigDecimal(sum * 60 / duration);
        statisticsReport.countPerHour = ReportUtils.convertToBigDecimal(sum * 3600 / duration);
        statisticsReport.countPerDay = ReportUtils.convertToBigDecimal(sum * 86400 / duration);

        return statisticsReport;
    }

    private StatisticsReport createStatisticsReport(final IntSummaryStatistics statistics)
    {
        final StatisticsReport statisticsReport = new StatisticsReport();

        statisticsReport.mean = ReportUtils.convertToBigDecimal(statistics.getMean());
        statisticsReport.min = statistics.getMinimum();
        statisticsReport.max = statistics.getMaximum();
        statisticsReport.deviation = ReportUtils.convertToBigDecimal(statistics.getStandardDeviation());

        return statisticsReport;
    }
}
