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
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.datasketches.hll.HllSketch;
import org.apache.datasketches.hll.TgtHllType;
import org.apache.datasketches.hll.Union;
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
import com.xceptance.xlt.api.util.XltCharBuffer;
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
     * The value set holding the bytes received.
     */
    private final IntMinMaxValueSet responseSizeValueSet;

    /**
     * Using HyperLogLog algorithm for counting distinct urls. We use the 8 bit version which is the fastest.
     */
    private HllSketch distinctUrlsHLL = new HllSketch(20, TgtHllType.HLL_8);

    /**
     * Direct-mapped 16-entry cache of recently observed URL buffers for this processor.
     * In typical load tests, request streams and sample URLs alternate between multiple URLs
     * (e.g. 2-5 distinct URLs per timer name). A 16-slot direct-mapped cache indexed by the
     * URL buffer hash code ensures hits even when alternating between different URLs, completely
     * eliminating redundant MurmurHash operations, HyperLogLog updates, and Map lookups.
     */
    private final XltCharBuffer[] recentUrls = new XltCharBuffer[16];

    /**
     * Single-item URL reference cache to bypass hashing and array lookup for consecutive identical URLs.
     */
    private XltCharBuffer lastUrl;

    /**
     * A set of distinct URLs. Contains at most {@link #MAXIMUM_NUMBER_OF_URLS} entries.
     */
    private final FastHashMap<XltCharBuffer, XltCharBuffer> distinctUrlSet = new FastHashMap<>(2 * MAXIMUM_NUMBER_OF_URLS + 1, 0.5f);

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
    private final HistogramValueSet runTimeHistogramValueSet;

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
        timerReport.urls = getUrlList(distinctUrlSet, (int) distinctUrlsHLL.getEstimate());
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
            final XltCharBuffer url = reqData.getUrl();
            if (url != null && url != lastUrl)
            {
                final int slot = url.hashCode() & 15;
                final XltCharBuffer cached = recentUrls[slot];

                // Check whether this URL matches the cached URL for this hash slot.
                // If it matches by identity or equality, we can completely bypass the expensive
                // HyperLogLog update and Map lookup.
                if (cached == null || (cached != url && !url.equals(cached)))
                {
                    // New or evicted distinct URL encountered for this processor slot
                    recentUrls[slot] = url;
                    distinctUrlsHLL.update(reqData.hashCodeOfUrlWithoutFragment());

                    // remember some URLs (up to the limit)
                    if (distinctUrlSetLimitedSize < MAXIMUM_NUMBER_OF_URLS)
                    {
                        if (distinctUrlSet.get(url) == null)
                        {
                            distinctUrlSet.put(url, url);
                            distinctUrlSetLimitedSize = distinctUrlSet.size();
                        }
                    }
                }

                lastUrl = url;
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
        if (other == null)
        {
            return;
        }

        super.merge(other);

        if (other instanceof RequestDataProcessor)
        {
            mergeRequestSpecific((RequestDataProcessor) other);
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

        if (countDistinctUrls && other.countDistinctUrls)
        {
            final Union union = new Union(20);
            union.update(distinctUrlsHLL);
            union.update(other.distinctUrlsHLL);
            distinctUrlsHLL = union.getResult(TgtHllType.HLL_8);

            for (final XltCharBuffer url : other.distinctUrlSet.keys())
            {
                if (distinctUrlSetLimitedSize < MAXIMUM_NUMBER_OF_URLS)
                {
                    if (distinctUrlSet.get(url) == null)
                    {
                        distinctUrlSet.put(url, url);
                        distinctUrlSetLimitedSize = distinctUrlSet.size();
                    }
                }
                else
                {
                    break;
                }
            }
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
    private UrlData getUrlList(final FastHashMap<XltCharBuffer, XltCharBuffer> urls, final int totalUrlCount)
    {
        final UrlData urlData = new UrlData();

        urlData.total = totalUrlCount;
        urlData.list = urls.keys().stream().map(XltCharBuffer::toString).collect(Collectors.toList());

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
