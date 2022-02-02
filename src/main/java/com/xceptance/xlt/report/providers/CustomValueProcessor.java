/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYIntervalSeries;

import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartScale;
import com.xceptance.xlt.report.util.DoubleMinMaxValueSet;
import com.xceptance.xlt.report.util.DoubleSummaryStatistics;
import com.xceptance.xlt.report.util.FixedSizeHistogramDoubleValueSet;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.util.PropertyHierarchy;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomValueProcessor extends AbstractDataProcessor
{
    private final String CUSTOM_SAMPLER_DOMAIN = "com.xceptance.xlt.customSamplers";

    private final DoubleMinMaxValueSet vSet = new DoubleMinMaxValueSet();

    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

    private final PSquarePercentile median = new PSquarePercentile(50);

    private final FixedSizeHistogramDoubleValueSet histogram;

    private final Map<Double, PSquarePercentile> percentiles = new LinkedHashMap<>();

    /**
     * Constructor.
     * 
     * @param name
     *            sampler name
     * @param reportProvider
     *            report provider
     */
    public CustomValueProcessor(final String name, final AbstractReportProvider reportProvider)
    {
        super(name, reportProvider);
        setChartDir(new File(getChartDir(), "customvalues"));

        histogram = new FixedSizeHistogramDoubleValueSet(getChartHeight());

        // percentiles
        final double[] p = ((ReportGeneratorConfiguration) getConfiguration()).getRuntimePercentiles();

        for (final double d : p)
        {
            percentiles.put(d, new PSquarePercentile(d));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        final CustomValue sample = (CustomValue) data;

        final double value = sample.getValue();

        vSet.addOrUpdateValue(sample.getTime(), value);
        stats.addValue(value);
        median.increment(value);
        histogram.addValue(value);

        for (final PSquarePercentile percentile : percentiles.values())
        {
            percentile.increment(value);
        }
    }

    /**
     * create report
     * 
     * @return report fragment
     */
    public CustomValueReport createReportFragment()
    {
        CustomValueReport reportFragment = null;

        final String seriesName = getName();

        // get custom sampler property hierarchy
        final PropertyHierarchy propH = new PropertyHierarchy(CUSTOM_SAMPLER_DOMAIN);
        propH.set(XltProperties.getInstance().getPropertiesForKey(CUSTOM_SAMPLER_DOMAIN));

        // build report fragment for each sampler
        final Set<String> samplerKeys = propH.getChildKeyFragments();
        for (final String samplerKey : samplerKeys)
        {
            final PropertyHierarchy samplerConfig = propH.get(samplerKey);
            if (samplerConfig != null)
            {
                final String samplerName = samplerConfig.getKeyValue("name");
                if (samplerName != null && samplerName.equals(seriesName))
                {
                    final String description = samplerConfig.getKeyValue("description");
                    String chartTitle = seriesName;
                    String yAxisTitle = "Value";

                    // the sampler's chart configuration (if present) may override the defaults
                    final PropertyHierarchy chartConfig = samplerConfig.get("chart");
                    if (chartConfig != null)
                    {
                        chartTitle = ObjectUtils.defaultIfNull(chartConfig.getKeyValue("title"), chartTitle);
                        yAxisTitle = ObjectUtils.defaultIfNull(chartConfig.getKeyValue("yAxisTitle"), yAxisTitle);
                    }

                    reportFragment = createReportFragment(seriesName, chartTitle, description, yAxisTitle);

                    break;
                }
            }
        }

        if (reportFragment == null)
        {
            reportFragment = createReportFragment(seriesName, seriesName, "", "Value");
        }

        return reportFragment;
    }

    /**
     * Creates the report fragment based on the given data.
     * 
     * @param samplerName
     *            sampler name / series title
     * @param chartTitle
     *            chart title
     * @param description
     *            description
     * @param yAxisTitle
     *            Y-axis title
     * @return created report fragment
     */
    private CustomValueReport createReportFragment(final String samplerName, final String chartTitle, final String description,
                                                   final String yAxisTitle)
    {
        final String chartFileName = UUID.randomUUID().toString();
        if (getConfiguration().shouldChartsGenerated())
        {
            // create the value series now as they will be needed for multiple charts
            final TimeSeries valueSeries = JFreeChartUtils.toMinMaxTimeSeries(vSet, samplerName);
            final TimeSeries averageValueSeries = JFreeChartUtils.createMovingAverageTimeSeries(valueSeries, getMovingAveragePercentage());
            final XYIntervalSeries histogramSeries = histogram.toSeries("Counts");

            // create charts asynchronously
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    createCustomValueChart(samplerName, chartTitle, yAxisTitle, chartFileName, valueSeries, averageValueSeries,
                                           histogramSeries, -1);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    createCustomValueAverageChart(samplerName, chartTitle, yAxisTitle, chartFileName, valueSeries, averageValueSeries,
                                                  median.getResult(), stats.getMean());
                }
            });
        }

        final CustomValueReport reportFragment = new CustomValueReport();
        reportFragment.name = samplerName;
        reportFragment.description = description;
        reportFragment.min = ReportUtils.convertToBigDecimal(stats.getMinimum());
        reportFragment.mean = ReportUtils.convertToBigDecimal(stats.getMean());
        reportFragment.max = ReportUtils.convertToBigDecimal(stats.getMaximum());
        reportFragment.standardDeviation = ReportUtils.convertToBigDecimal(stats.getStandardDeviation());

        reportFragment.chartFilename = chartFileName;

        // set the counts
        final double count = stats.getCount();
        final long duration = Math.max((getEndTime() - getStartTime()) / 1000, 1);

        reportFragment.count = (int) count;
        reportFragment.countPerSecond = ReportUtils.convertToBigDecimal(count / duration);
        reportFragment.countPerMinute = ReportUtils.convertToBigDecimal(count * 60 / duration);
        reportFragment.countPerHour = ReportUtils.convertToBigDecimal(count * 3600 / duration);
        reportFragment.countPerDay = ReportUtils.convertToBigDecimal(count * 86400 / duration);

        // set the percentiles
        for (final Entry<Double, PSquarePercentile> percentileEntry : percentiles.entrySet())
        {
            final double p = percentileEntry.getKey();
            final double res = percentileEntry.getValue().getResult();

            reportFragment.percentiles.put("p" + ReportUtils.formatValue(p), ReportUtils.convertToBigDecimal(res));
        }

        return reportFragment;
    }

    /**
     * Creates the chart for the custom value and saves it to the chart directory.
     * 
     * @param samplerName
     *            the name of the custom value
     * @param chartTitle
     *            the title of the chart
     * @param yAxisTitle
     *            the title of the range axis
     * @param chartFileName
     *            the name of the chart file
     * @param valueSeries
     *            the value series
     * @param histogramSeries
     *            the histogram series
     * @param chartCappingValue
     *            the value at which to cap the chart
     */
    private void createCustomValueChart(final String samplerName, final String chartTitle, final String yAxisTitle,
                                        final String chartFileName, final TimeSeries timeSeries, final TimeSeries averageTimeSeries,
                                        final XYIntervalSeries histogramSeries, final int chartCappingValue)
    {
        // System.out.println("Creating chart for custom value '" + samplerName + "' ...");

        final ChartScale chartScale = ((ReportGeneratorConfiguration) getConfiguration()).getChartScale();

        // create the value plot
        final DateAxis timeAxis = JFreeChartUtils.createTimeAxis(getStartTime(), getEndTime());
        final XYPlot customValuePlot = JFreeChartUtils.createLinePlot(timeSeries, averageTimeSeries, timeAxis, yAxisTitle, true,
                                                                      chartScale, chartCappingValue);
        NumberAxis rangeAxis = (NumberAxis) customValuePlot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        // rangeAxis.setAutoRangeIncludesZero(false);

        // create the histogram plot
        final Range valueRange = customValuePlot.getRangeAxis().getRange();
        final XYPlot histogramPlot = JFreeChartUtils.createHistogramPlot(histogramSeries, valueRange, chartScale, chartCappingValue);

        // create the outer combined plot
        final CombinedRangeXYPlot combinedPlot = new CombinedRangeXYPlot();
        combinedPlot.setGap(4);
        combinedPlot.setRangeAxis(rangeAxis);
        combinedPlot.add(customValuePlot, 15);
        combinedPlot.add(histogramPlot, 1);

        // create and save the chart
        final JFreeChart chart = JFreeChartUtils.createChart(chartTitle, combinedPlot);
        JFreeChartUtils.saveChart(chart, chartFileName, getChartDir(), getChartWidth(), getChartHeight());
    }

    /**
     * Creates an average chart and stores it to disk. The chart contains graphs for the moving average, the median and
     * the mean, but not the actual custom values.
     * 
     * @param name
     *            the name of the custom value
     * @param chartTitle
     *            the title of the chart
     * @param yAxisTitle
     *            the title of the range axis
     * @param chartFileName
     *            the name of the chart file
     * @param valueSeries
     *            the value series
     * @param averageValueSeries
     *            the average value series
     * @param median
     *            the median of the values in the value series
     * @param mean
     *            the mean of the values in the value series
     */
    private void createCustomValueAverageChart(final String name, final String chartTitle, final String yAxisTitle,
                                               final String chartFileName, final TimeSeries valueSeries,
                                               final TimeSeries averageValueSeries, final double median, final double mean)
    {
        // System.out.println("Creating average chart for custom value '" + name + "' ...");

        // create and customize the chart
        final JFreeChart chart = JFreeChartUtils.createAverageLineChart(name, chartTitle, yAxisTitle, valueSeries, averageValueSeries,
                                                                        median, mean, getStartTime(), getEndTime());
        chart.getXYPlot().getRangeAxis().setStandardTickUnits(NumberAxis.createStandardTickUnits());

        // finally save the chart
        JFreeChartUtils.saveChart(chart, chartFileName + "_Average", getChartDir(), getChartWidth(), getChartHeight());
    }
}
