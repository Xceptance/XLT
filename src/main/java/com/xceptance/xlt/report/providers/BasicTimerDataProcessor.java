/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYIntervalSeries;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartScale;
import com.xceptance.xlt.report.util.FixedSizeHistogramValueSet;
import com.xceptance.xlt.report.util.IntMinMaxValueSet;
import com.xceptance.xlt.report.util.IntSummaryStatistics;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.RuntimeHistogram;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.ValueSet;

/**
 * The {@link BasicTimerDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link TimerData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * </ul>
 */
public class BasicTimerDataProcessor extends AbstractDataProcessor
{
    private final ValueSet countPerSecondValueSet = new ValueSet();

    private final ValueSet errorsPerSecondValueSet = new ValueSet();

    private final IntSummaryStatistics runTimeStatistics = new IntSummaryStatistics();

    private final RuntimeHistogram runTimeHistogram = new RuntimeHistogram(10);

    private double[] percentiles;

    private final IntMinMaxValueSet runTimeValueSet;

    private final FixedSizeHistogramValueSet histogramValueSet;

    private int totalErrors = 0;

    private final int minMaxValueSetSize;

    private String label;

    /**
     * Constructor.
     *
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public BasicTimerDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        // setup run time value set
        minMaxValueSetSize = getChartWidth();
        runTimeValueSet = new IntMinMaxValueSet(minMaxValueSetSize);

        // setup histogram value set
        histogramValueSet = new FixedSizeHistogramValueSet(getChartHeight());

        // get percentile configuration
        percentiles = ((ReportGeneratorConfiguration) getConfiguration()).getRuntimePercentiles();
    }

    /**
     * Generates a run time timer report from the passed list of data records.
     *
     * @param generateHistogram
     *            whether histogram charts are to be generated as well
     * @return the timer report
     */
    public TimerReport createTimerReport(final boolean generateHistogram)
    {
        final String name = getName();

        // create report
        final TimerReport timerReport = createTimerReport();

        timerReport.mean = ReportUtils.convertToBigDecimal(runTimeStatistics.getMean());
        timerReport.errors = totalErrors;
        timerReport.max = runTimeStatistics.getMaximum();
        timerReport.min = runTimeStatistics.getMinimum();
        timerReport.name = name;
        timerReport.labels = (label == null) ? null : Set.of(label);
        timerReport.deviation = ReportUtils.convertToBigDecimal(runTimeStatistics.getStandardDeviation());
        timerReport.median = ReportUtils.convertToBigDecimal(runTimeHistogram.getMedianValue());

        // set the percentiles
        for (double percentile : percentiles)
        {
            timerReport.percentiles.put("p" + ReportUtils.formatValue(percentile),
                                        ReportUtils.convertToBigDecimal(runTimeHistogram.getPercentile(percentile)));
        }

        // set the counts
        final double count = runTimeStatistics.getCount();
        final long duration = Math.max((getEndTime() - getStartTime()) / 1000, 1);

        timerReport.errorPercentage = ReportUtils.calculatePercentage(totalErrors, (int) count);
        timerReport.count = (int) count;
        timerReport.countPerSecond = ReportUtils.convertToBigDecimal(count / duration);
        timerReport.countPerMinute = ReportUtils.convertToBigDecimal(count * 60 / duration);
        timerReport.countPerHour = ReportUtils.convertToBigDecimal(count * 3600 / duration);
        timerReport.countPerDay = ReportUtils.convertToBigDecimal(count * 86400 / duration);

        if (getConfiguration().shouldChartsGenerated())
        {
            // post-process the run time series now as they will be needed for multiple charts
            final TimeSeries runTimeTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(runTimeValueSet, "Runtime");
            final TimeSeries runTimeAverageTimeSeries = JFreeChartUtils.createMovingAverageTimeSeries(runTimeTimeSeries,
                                                                                                      getMovingAveragePercentage());

            // create charts asynchronously
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    // determine the capping value
                    final int chartCappingValue = JFreeChartUtils.getChartCappingValue(getChartCappingInfo(), runTimeStatistics.getMean(),
                                                                                       runTimeStatistics.getMaximum());

                    final XYIntervalSeries runTimeHistogramSeries = histogramValueSet.toSeries("Distribution");

                    final TimeSeries errorsPerSecondTimeSeries = JFreeChartUtils.toStandardTimeSeries(errorsPerSecondValueSet.toMinMaxValueSet(minMaxValueSetSize),
                                                                                                      "Errors/s");

                    saveResponseTimeChart(name, runTimeTimeSeries, runTimeAverageTimeSeries, runTimeHistogramSeries,
                                          errorsPerSecondTimeSeries, chartCappingValue);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    saveResponseTimeAverageChart(name, runTimeTimeSeries, runTimeAverageTimeSeries, timerReport.median.doubleValue(),
                                                 timerReport.mean.doubleValue());
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final TimeSeries countPerSecondTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(countPerSecondValueSet.toMinMaxValueSet(minMaxValueSetSize),
                                                                                                   "Count/s");

                    saveCountPerSecondChart(name, countPerSecondTimeSeries);
                }
            });
        }

        return timerReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        final TimerData timerStats = (TimerData) stat;

        // we record the data at the time the timer has finished
        final long endTime = timerStats.getEndTime();
        final int runTime = timerStats.getRunTime();

        // update the stats
        runTimeHistogram.addValue(runTime);
        runTimeStatistics.addValue(runTime);

        // update the time series
        runTimeValueSet.addOrUpdateValue(endTime, runTime);
        countPerSecondValueSet.addOrUpdateValue(endTime, 1);
        histogramValueSet.addValue(runTime);

        // handle errors
        if (timerStats.hasFailed())
        {
            totalErrors++;

            // we expect the timer to be failed around the same time as it has finished
            errorsPerSecondValueSet.addOrUpdateValue(endTime, 1);
        }

        // remember the latest label (it should always be the same)
        this.label = stat.getLabel();
    }

    protected ValueSet getCountPerSecondValueSet()
    {
        return countPerSecondValueSet;
    }

    protected ValueSet getErrorsPerSecondValueSet()
    {
        return errorsPerSecondValueSet;
    }

    protected FixedSizeHistogramValueSet getHistogramValueSet()
    {
        return histogramValueSet;
    }

    /**
     * Creates a combined chart with the given title where the response times as well as the errors are shown one upon
     * the other.
     *
     * @param chartTitle
     *            the chart title
     * @param responseTimeSeries
     *            the response time series
     * @param responseTimeAverageSeries
     *            the response time average series
     * @param responseTimeHistogramSeries
     *            the response time histogram series
     * @param errorsSeries
     *            the errors series
     * @param chartCappingValue
     *            the value at which to cap the chart's y-axis
     * @return the chart
     */
    protected JFreeChart createResponseTimeAndErrorsChart(final String chartTitle, final TimeSeries responseTimeSeries,
                                                          final TimeSeries responseTimeAverageSeries,
                                                          final XYIntervalSeries responseTimeHistogramSeries, final TimeSeries errorsSeries,
                                                          final int chartCappingValue)
    {
        final ChartScale chartScale = ((ReportGeneratorConfiguration) getConfiguration()).getChartScale();
        Range responseTimeRange = null;

        // create outer combined plot
        final CombinedRangeXYPlot outerCombinedPlot = new CombinedRangeXYPlot();
        outerCombinedPlot.setGap(4);
        outerCombinedPlot.getRangeAxis().setVisible(false);

        // add the left plot
        {
            // create the left stacked plot
            final CombinedDomainXYPlot leftCombinedPlot = JFreeChartUtils.createCombinedPlot(getStartTime(), getEndTime());

            outerCombinedPlot.add(leftCombinedPlot, 15);

            // add response time plot
            final XYPlot responseTimePlot = JFreeChartUtils.createLinePlot(responseTimeSeries, responseTimeAverageSeries, null,
                                                                           "Runtime [ms]", true, chartScale, chartCappingValue);

            leftCombinedPlot.add(responseTimePlot, 3);

            // add error plot
            final XYPlot errorPlot = JFreeChartUtils.createBarPlot(new TimeSeriesCollection(errorsSeries), null, "Errors",
                                                                   JFreeChartUtils.COLOR_ERROR);

            leftCombinedPlot.add(errorPlot, 1);

            // remember response time range as we need it for the histogram as well
            responseTimeRange = responseTimePlot.getRangeAxis().getRange();
        }

        // add the right plot
        {
            // create the right stacked plot
            final CombinedDomainXYPlot rightCombinedPlot = new CombinedDomainXYPlot();
            rightCombinedPlot.setGap(16.0);
            rightCombinedPlot.getDomainAxis().setVisible(false);

            outerCombinedPlot.add(rightCombinedPlot, 1);

            // add histogram plot
            final XYPlot histogramPlot = JFreeChartUtils.createHistogramPlot(responseTimeHistogramSeries, responseTimeRange, chartScale,
                                                                             chartCappingValue);

            rightCombinedPlot.add(histogramPlot, 3);

            // add spacer plot
            final XYPlot spacerPlot = JFreeChartUtils.createSpacerPlot();

            rightCombinedPlot.add(spacerPlot, 1);
        }

        // finally create the chart
        final JFreeChart jfreechart = JFreeChartUtils.createChart(chartTitle, outerCombinedPlot);

        return jfreechart;
    }

    /**
     * Creates a chart from the given run time values and stores it to the passed directory. The chart contains graphs
     * for the moving average, the median and the average, but not the actual timer values.
     *
     * @param name
     *            the chart title
     * @param responseTimeSeries
     *            the response time series
     * @param responseTimeAverageSeries
     *            the response time average series
     * @param median
     *            the median of the values in the response times series
     * @param mean
     *            the mean of the values in the response times series
     */
    private JFreeChart createResponseTimeAverageChart(final String name, final TimeSeries responseTimeSeries,
                                                      final TimeSeries responseTimeAverageSeries, final double median, final double mean)
    {
        // create and setup chart
        final JFreeChart chart = JFreeChartUtils.createAverageLineChart("Runtime", name, "Runtime [ms]", responseTimeSeries,
                                                                        responseTimeAverageSeries, median, mean, getStartTime(),
                                                                        getEndTime());

        return chart;
    }

    /**
     * Creates a new {@link TimerReport} instance. This method is intended to be overridden by sub classes.
     *
     * @return the timer report
     */
    protected TimerReport createTimerReport()
    {
        return new TimerReport();
    }

    /**
     * Creates a count per second chart from the values in the passed time series. The chart's title and file name are
     * derived from the specified timer name. The chart is generated to the charts directory.
     *
     * @param timerName
     *            the name of the timer
     * @param timeSeries
     *            the values
     */
    private void saveCountPerSecondChart(final String timerName, final TimeSeries timeSeries)
    {
        // System.out.println("Creating count per second chart for timer '" + timerName + "' ... ");

        // final long start = TimerUtils.getTime();

        final JFreeChart chart = JFreeChartUtils.createLineChart(timerName, "Count", timeSeries, getStartTime(), getEndTime(), true,
                                                                 getMovingAveragePercentage());

        JFreeChartUtils.saveChart(chart, timerName + "_CountPerSecond", getChartDir(), getChartWidth(), getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", timeSeries.getItemCount(), TimerUtils.getTime() - start);
    }

    /**
     * Creates a chart from the run time values in the passed list of data records, which only contains graphs for the
     * moving average, the median and the average, but not the actual runtime values.
     *
     * @param timerName
     *            the name of the timer
     * @param responseTimeSeries
     *            the response time series
     * @param responseTimeAverageSeries
     *            the response time average series
     * @param median
     *            the median of the values in the response times series
     * @param mean
     *            the mean of the values in the response times series
     */
    private void saveResponseTimeAverageChart(final String timerName, final TimeSeries responseTimeSeries,
                                              final TimeSeries responseTimeAverageSeries, final double median, final double mean)
    {
        // System.out.println("Creating average chart for timer '" + timerName + "' ... ");

        // final long start = TimerUtils.getTime();

        final JFreeChart chart = createResponseTimeAverageChart(timerName, responseTimeSeries, responseTimeAverageSeries, median, mean);

        JFreeChartUtils.saveChart(chart, timerName + "_Average", getChartDir(), getChartWidth(), getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", runTimeTimeSeries.getItemCount(), TimerUtils.getTime() -
        // start);
    }

    /**
     * Creates a chart from the run time values in the passed list of data records. The chart contains not only the run
     * time values, but also indicators when an error had occurred. The chart's title and file name are derived from the
     * specified timer name. The chart is generated to the charts directory.
     *
     * @param timerName
     *            the name of the timer
     * @param responseTimeSeries
     *            the response time series
     * @param responseTimeAverageSeries
     *            the response time average series
     * @param responseTimeHistogramSeries
     *            the response time histogram series
     * @param errorsSeries
     *            the error series
     * @param chartCappingValue
     *            the value at which to cap the chart's y-axis
     */
    private void saveResponseTimeChart(final String timerName, final TimeSeries responseTimeSeries,
                                       final TimeSeries responseTimeAverageSeries, final XYIntervalSeries responseTimeHistogramSeries,
                                       final TimeSeries errorsSeries, final int chartCappingValue)
    {
        // System.out.println("Creating response time chart for timer '" + timerName + "' ... ");

        // final long start = TimerUtils.getTime();

        // create and save chart
        final JFreeChart chart = createResponseTimeAndErrorsChart(timerName, responseTimeSeries, responseTimeAverageSeries,
                                                                  responseTimeHistogramSeries, errorsSeries, chartCappingValue);

        // HACK: make transaction charts 150% of the height
        int height = getChartHeight();
        if (this instanceof TransactionDataProcessor)
        {
            height = height * 150 / 100;
        }

        JFreeChartUtils.saveChart(chart, timerName, getChartDir(), getChartWidth(), height);

        // System.out.printf("OK (%,d values, %,d ms)\n", runTimeTimeSeries.getItemCount(), TimerUtils.getTime() -
        // start);
    }
}
