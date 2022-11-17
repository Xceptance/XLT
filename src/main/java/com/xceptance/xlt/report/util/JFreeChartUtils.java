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
package com.xceptance.xlt.report.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalDataItem;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luciad.imageio.webp.WebPWriteParam;
import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartCappingInfo;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartCappingInfo.ChartCappingMode;
import com.xceptance.xlt.report.ReportGeneratorConfiguration.ChartScale;

/**
 * The JFreeChartUtils class simplifies generating charts via the JFreeChart library.
 */
public final class JFreeChartUtils
{
    /**
     * chart color sets
     */
    public static class ColorSet
    {
        /**
         * blue, ...
         */
        public static final ColorSet AVERAGES = new ColorSet(COLOR_MOVING_AVERAGE, COLOR_MEDIAN, COLOR_MEAN);

        /**
         * blue, gray, magenta, green, red
         */
        public static final ColorSet A = new ColorSet(Color.BLUE, MoreColors.GRAY.getColor(), Color.MAGENTA, MoreColors.GREEN.getColor(),
                                                      Color.RED);

        /**
         * black, light green, brown, steel blue, light gray
         */
        public static final ColorSet B = new ColorSet(Color.BLACK, MoreColors.LIGHT_GREEN.getColor(), MoreColors.BROWN.getColor(),
                                                      MoreColors.STEEL_BLUE.getColor(), MoreColors.LIGHT_GRAY.getColor());

        /**
         * orange, cyan, pink, lilac, yellow
         */
        public static final ColorSet C = new ColorSet(MoreColors.ORANGE.getColor(), Color.CYAN, Color.PINK, MoreColors.LILAC.getColor(),
                                                      Color.YELLOW);

        private final List<Color> colors = new ArrayList<Color>(5);

        public ColorSet(final Color... colors)
        {
            final int count = colors.length;
            for (int i = 0; i < count; i++)
            {
                this.colors.add(colors[i]);
            }
        }

        /**
         * get the color from specified index
         *
         * @param index
         *            index of color in color set
         * @return the color from specified index
         * @throws IndexOutOfBoundsException
         *             if the index is out of range (index < 0 || index >= size())
         */
        public Color get(final int index) throws IndexOutOfBoundsException
        {
            return colors.get(index);
        }

        /**
         * get all colors from this set
         *
         * @return
         */
        public List<Color> getColors()
        {
            return Collections.unmodifiableList(colors);
        }

        /**
         * how many colors are in this set
         *
         * @return size of color set
         */
        public int size()
        {
            return colors.size();
        }
    }

    /**
     * self defined colors
     */
    public enum MoreColors
    {
        BROWN(0xB97A57),
        GRAY(0xAAAAAA),
        GREEN(0x00AA00),
        LIGHT_GRAY(0x757575),
        LIGHT_GREEN(0xB5E61D),
        LILAC(0xC8BFE7),
        ORANGE(0xFF9900),
        STEEL_BLUE(0x7092BE);

        private final Color color;

        private MoreColors(final int color)
        {
            this.color = new Color(color);
        }

        public Color getColor()
        {
            return color;
        }
    }

    /**
     * The capping color in the charts (dark red).
     */
    public static final Color COLOR_CAP = new Color(0xAA0000);

    /**
     * The color of error bars in the charts (red).
     */
    public static final Color COLOR_ERROR = Color.RED;

    /**
     * The color of event bars in the charts (orange).
     */
    public static final Color COLOR_EVENT = new Color(0xFFA500);

    /**
     * The color of histogram bars in the charts (gray-green).
     */
    public static final Color COLOR_HISTOGRAM = new Color(0x82C17D);

    /**
     * The color of a standard value line in the charts (gray).
     */
    public static final Color COLOR_LINE = new Color(0xAAAAAA);

    /**
     * The color of a dimmed value line in the charts (light gray).
     */
    public static final Color COLOR_LINE_DIMMED = new Color(0xDDDDDD);

    /**
     * The color of a mean line in the charts (dark magenta).
     */
    public static final Color COLOR_MEAN = new Color(0xCD3333);

    /**
     * The color of a median line in the charts (dark turquoise).
     */
    public static final Color COLOR_MEDIAN = new Color(0x62C0E0);

    /**
     * The color of a moving average line in the charts (dark blue).
     */
    public static final Color COLOR_MOVING_AVERAGE = new Color(0x1C1CBF);

    /**
     * The default chart theme.
     */
    private static final XltChartTheme DEFAULT_CHART_THEME = new XltChartTheme();

    /**
     * The default title of time axes.
     */
    private static final String DEFAULT_DATE_AXIS_TITLE = "Time";

    /**
     * The default title of value axes.
     */
    private static final String DEFAULT_VALUE_AXIS_TITLE = "Values";

    /**
     * The shape of a line in the chart's legend.
     */
    private static final Double LEGEND_LINE_SHAPE = new Rectangle2D.Double(-7.0, 0.0, 14.0, 1.0);

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(JFreeChartUtils.class);

    /**
     * Maps a long value to its corresponding {@link Second} object.
     */
    private static final Map<Long, Second> secondsCache = new HashMap<Long, Second>();

    /**
     * The watermark color.
     */
    private static final Color WATERMARK_COLOR = new Color(0xABABAB);

    /**
     * The watermark text.
     */
    private static final String WATERMARK_TEXT = "Xceptance LoadTest";

    /**
     * The compression quality in percent, to use when creating WebP images, where 1 is best quality and 0 is highest compression (default: 0.75f).
     */
    private static float webpCompressionQuality = 0.75f;

    /**
     * The replacement value for negative/0 values when making a series fit for logarithmic axes.
     */
    private static final double MIN_VALUE_FOR_LOGARITHMIC_AXES = 0.00000001;

    /**
     * Creates a new line plot and adds it to the given chart.
     *
     * @param chart
     *            the chart to modify
     * @param rangeAxisTitle
     *            the title shown at the plot's range axis
     * @param dataset
     *            the data to show
     * @see #createCombinedPlotChart(String, long, long)
     */
    public static void addLinePlotToCombinedPlotChart(final JFreeChart chart, final String rangeAxisTitle, final XYDataset dataset)
    {
        final CombinedDomainXYPlot combinedPlot = ((CombinedDomainXYPlot) chart.getPlot());

        // choose a color set
        final int subPlotCount = combinedPlot.getSubplots().size();
        final int decider = subPlotCount % 3;
        final ColorSet colorSet = decider == 0 ? ColorSet.A : decider == 1 ? ColorSet.B : ColorSet.C;

        // create and add plot
        final XYPlot plot = createLinePlot(dataset, null, rangeAxisTitle, colorSet);
        combinedPlot.add(plot, 1);
    }

    /**
     * Caps a plot at the given value on the range axis and draws a cap marker. Optionally, a cap item can be added to
     * the legend.
     *
     * @param plot
     *            the plot to cap
     * @param cappingValue
     *            the value to cap the plot at (must be greater than 0 to take effect)
     * @param addLegendItem
     *            whether to add a legend item for the cap
     */
    public static void capPlot(final XYPlot plot, final int cappingValue, final boolean addLegendItem)
    {
        // cap the plot if necessary
        if (cappingValue > 0)
        {
            // cap the plot
            plot.getRangeAxis().setUpperBound(cappingValue);

            // draw a cap marker
            final Marker capMarker = new ValueMarker(cappingValue);
            capMarker.setAlpha(1.0f);
            capMarker.setPaint(COLOR_CAP);
            capMarker.setStroke(new BasicStroke(4.0f));
            plot.addRangeMarker(capMarker);

            // add a cap legend item
            if (addLegendItem)
            {
                final LegendItemCollection legendItems = plot.getLegendItems();

                final LegendItem capLegendItem = new LegendItem("Cap", COLOR_CAP);
                capLegendItem.setShape(new Rectangle2D.Double(-7.0, 0.0, 14.0, 2.0));
                legendItems.add(capLegendItem);

                plot.setFixedLegendItems(legendItems);
            }
        }
    }

    /**
     * Creates an average chart with the moving average, the median, and the mean, but not the actual values.
     *
     * @param seriesName
     *            the name of the series
     * @param chartTitle
     *            the title of the chart
     * @param yAxisTitle
     *            the title of the range axis
     * @param valueSeries
     *            the value series
     * @param averageValueSeries
     *            the average value series
     * @param median
     *            the median of the values in the value series
     * @param mean
     *            the mean of the values in the value series
     * @param startTime
     *            chart start time
     * @param endTime
     *            chart end time
     */
    public static JFreeChart createAverageLineChart(final String seriesName, final String chartTitle, final String yAxisTitle,
                                                    final TimeSeries valueSeries, final TimeSeries averageValueSeries, final double median,
                                                    final double mean, final long startTime, final long endTime)
    {
        final TimeSeries medianSeries = new TimeSeries(seriesName + " (Median)");
        final TimeSeries meanSeries = new TimeSeries(seriesName + " (Mean)");

        final TimeSeriesCollection seriesCollection = new TimeSeriesCollection();
        seriesCollection.addSeries(averageValueSeries);
        seriesCollection.addSeries(medianSeries);
        seriesCollection.addSeries(meanSeries);

        // only add graphs for median / mean if there are some values
        final int count = valueSeries.getItemCount();
        if (count > 1)
        {
            final TimeSeriesDataItem firstItem = valueSeries.getDataItem(0);
            final TimeSeriesDataItem lastItem = valueSeries.getDataItem(count - 1);

            medianSeries.add(firstItem.getPeriod(), median);
            medianSeries.add(lastItem.getPeriod(), median);

            meanSeries.add(firstItem.getPeriod(), mean);
            meanSeries.add(lastItem.getPeriod(), mean);
        }

        // create and customize the chart
        final JFreeChart chart = createLineChart(chartTitle, yAxisTitle, seriesCollection, startTime, endTime, ColorSet.AVERAGES);

        final NumberAxis axis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        axis.setStandardTickUnits(NumberAxis.createStandardTickUnits());

        return chart;
    }

    /**
     * Creates a new bar chart.
     *
     * @param chartTitle
     *            the chart title
     * @param dataset
     *            the data to show
     * @param rangeAxisTitle
     *            the title of the range
     * @param barColor
     *            the bar color
     * @param startTime
     *            chart start time
     * @param endTime
     *            chart end time
     * @return the chart
     */
    public static JFreeChart createBarChart(final String chartTitle, final XYDataset dataset, final String rangeAxisTitle,
                                            final Color barColor, final long startTime, final long endTime)
    {
        final DateAxis timeAxis = createTimeAxis(startTime, endTime);
        final XYPlot barPlot = createBarPlot(dataset, timeAxis, rangeAxisTitle, barColor);

        return createChart(chartTitle, barPlot);
    }

    /**
     * Creates a new bar chart.
     *
     * @param chartTitle
     *            the chart title
     * @param dataset
     *            the data to show
     * @param rangeAxisTitle
     *            the title of the range
     * @param barColor
     *            the bar color
     * @param startTime
     *            chart start time
     * @param endTime
     *            chart end time
     * @param createLegend
     *            enable or disable the creation of a chart legend
     * @return the chart
     */
    public static JFreeChart createBarChart(final String chartTitle, final XYDataset dataset, final String rangeAxisTitle,
                                            final Color barColor, final long startTime, final long endTime, boolean createLegend,
                                            boolean createTimeAxis)
    {
        DateAxis timeAxis = new DateAxis();
        if (createTimeAxis)
        {
            timeAxis = createTimeAxis(startTime, endTime);
        }
        updateDateAxisMinMaxTime(timeAxis, startTime, endTime);

        NumberAxis rangeAxis = new NumberAxis();
        if (rangeAxisTitle != null)
        {
            rangeAxis = new NumberAxis(rangeAxisTitle);
        }
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        final XYPlot barPlot = createBarPlot(dataset, timeAxis, rangeAxis, barColor);

        return new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, barPlot, createLegend);
    }

    /**
     * Creates a new bar plot with standard settings applied (number axis set).
     *
     * @param dataset
     *            the data to show
     * @param domainAxis
     *            the domain axis (may be <code>null</code> if no axis is to be shown)
     * @param rangeAxis
     *            the range axis (may be <code>null</code> if no axis is to be shown)
     * @param barColor
     *            the bar color
     * @return the bar plot
     */
    public static XYPlot createBarPlot(final XYDataset dataset, final ValueAxis domainAxis, ValueAxis rangeAxis, final Color barColor)
    {
        return new XYPlot(dataset, domainAxis, rangeAxis, createBarRenderer(barColor));
    }

    /**
     * Creates a new bar plot with standard settings applied (number axis set).
     *
     * @param dataset
     *            the data to show
     * @param domainAxis
     *            the domain axis (may be <code>null</code> if no axis is to be shown)
     * @param rangeAxisTitle
     *            the title of the range axis
     * @param barColor
     *            the bar color
     * @return the bar plot
     */
    public static XYPlot createBarPlot(final XYDataset dataset, final ValueAxis domainAxis, final String rangeAxisTitle,
                                       final Color barColor)
    {
        return new XYPlot(dataset, domainAxis, createNumberAxis(rangeAxisTitle), createBarRenderer(barColor));
    }

    /**
     * Creates a new bar renderer with standard settings applied (no shadow).
     *
     * @param barColor
     *            the bar color
     * @return the bar renderer
     */
    public static XYBarRenderer createBarRenderer(final Color barColor)
    {
        final XYBarRenderer barRenderer = new XYBarRenderer();
        barRenderer.setBarPainter(new StandardXYBarPainter());
        barRenderer.setSeriesPaint(0, barColor);
        barRenderer.setShadowVisible(false);

        return barRenderer;
    }

    /**
     * Creates a basic line chart with only a time axis.
     *
     * @param chartTitle
     *            the chart title
     * @param timeAxisTitle
     *            the x-axis title
     * @param startTime
     *            chart start time
     * @param endTime
     *            chart end time
     * @return the chart
     * @see #setAxisTimeSeriesCollection(JFreeChart, int, String, List)
     */
    public static JFreeChart createBasicLineChart(final String chartTitle, final String timeAxisTitle, final long startTime,
                                                  final long endTime)
    {
        final DateAxis timeAxis = createTimeAxis(timeAxisTitle, startTime, endTime);
        final XYLineAndShapeRenderer lineRenderer = createLineRenderer(ColorSet.A);

        final XYPlot plot = new XYPlot(null, timeAxis, null, lineRenderer);

        return createChart(chartTitle, plot);
    }

    /**
     * Creates chart with the given title set and the passed plot embedded.
     *
     * @param chartTitle
     *            the chart title
     * @param plot
     *            the plot
     * @return the chart
     */
    public static JFreeChart createChart(final String chartTitle, final Plot plot)
    {
        final JFreeChart jfreechart = new JFreeChart(chartTitle, plot);

        return jfreechart;
    }

    /**
     * Creates a combined plot with standard settings applied (time axis).
     *
     * @param startTime
     *            chart start time
     * @param endTime
     *            chart end time
     * @return the combined plot
     */
    public static CombinedDomainXYPlot createCombinedPlot(final long startTime, final long endTime)
    {
        final CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(createTimeAxis(startTime, endTime));
        combinedPlot.setGap(16.0);

        return combinedPlot;
    }

    /**
     * Creates an empty combined plot chart. Plots can later be added to this chart and will be shown one below the
     * other.
     *
     * @param chartTitle
     *            the chart title
     * @param startTime
     *            chart start time
     * @param endTime
     *            chart end time
     * @return the chart
     * @see {@link JFreeChartUtils#addLinePlotToCombinedPlotChart(JFreeChart, String, TimeSeriesCollection)}
     */
    public static JFreeChart createCombinedPlotChart(final String chartTitle, final long startTime, final long endTime)
    {
        return createChart(chartTitle, createCombinedPlot(startTime, endTime));
    }

    /**
     * Creates a histogram plot for the given histogram series. The plot won't have any visible axes and is meant to be
     * combined with other plots.
     * 
     * @param histogramSeries
     *            the histogram series
     * @param range
     *            the range to show on the range axis
     * @param chartScale
     *            the chart scale to be used
     * @param plotCappingValue
     *            the value at which to cap the plot
     * @return the plot
     */
    public static XYPlot createHistogramPlot(final XYIntervalSeries histogramSeries, final Range range, final ChartScale chartScale,
                                             final int plotCappingValue)
    {
        // adjust the series if necessary
        if (chartScale == ChartScale.LOGARITHMIC)
        {
            adjustSeriesForLogarithmicAxes(histogramSeries);
        }

        // data set
        final XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        dataset.addSeries(histogramSeries);

        // domain axis
        final ValueAxis domainAxis = new NumberAxis();
        domainAxis.setVisible(false);

        // range axis
        final NumberAxis rangeAxis = (chartScale == ChartScale.LOGARITHMIC) ? new LogarithmicAxis(null) : new NumberAxis();
        rangeAxis.setVisible(false);
        rangeAxis.setRange(range);

        // bar renderer
        final XYBarRenderer barRenderer = createBarRenderer(COLOR_HISTOGRAM);
        barRenderer.setUseYInterval(true);

        // plot
        final XYPlot histogramPlot = new XYPlot(dataset, domainAxis, rangeAxis, barRenderer);
        histogramPlot.setDomainGridlinesVisible(false);
        capPlot(histogramPlot, plotCappingValue, true);

        return histogramPlot;
    }

    /**
     * Creates a chart from the passed time series and gives it the specified title.
     *
     * @param chartTitle
     *            the chart title
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param series
     *            the time series to show
     * @param startTime
     *            the start time of the x-axis
     * @param endTime
     *            the end time of the x-axis
     * @param includeMovingAverage
     *            whether or not an additional moving average time series should be included
     * @param percentage
     *            the percentaged amount of values for building the moving average
     * @return the chart
     */
    public static JFreeChart createLineChart(final String chartTitle, final String rangeAxisTitle, final TimeSeries series,
                                             final long startTime, final long endTime, final boolean includeMovingAverage,
                                             final int percentage)
    {
        return createLineChart(chartTitle, rangeAxisTitle, series, startTime, endTime, includeMovingAverage, percentage, true);
    }

    /**
     * Creates a chart from the passed time series and gives it the specified title.
     *
     * @param chartTitle
     *            the chart title
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param series
     *            the time series to show
     * @param startTime
     *            the start time of the x-axis
     * @param endTime
     *            the end time of the x-axis
     * @param includeMovingAverage
     *            whether or not an additional moving average time series should be included
     * @param percentage
     *            the percentaged amount of values for building the moving average
     * @param showDots
     *            whether to additionally visualize the values as dots
     * @return the chart
     */
    public static JFreeChart createLineChart(final String chartTitle, final String rangeAxisTitle, final TimeSeries series,
                                             final long startTime, final long endTime, final boolean includeMovingAverage,
                                             final int percentage, final boolean showDots)
    {
        final TimeSeries movingAverageSeries = includeMovingAverage ? createMovingAverageTimeSeries(series, percentage) : null;

        return createLineChart(chartTitle, rangeAxisTitle, series, movingAverageSeries, startTime, endTime, showDots, ChartScale.LINEAR, -1);
    }

    /**
     * Creates a chart from the passed time series collection and gives it the specified title. Any time series in the
     * collection is added to the chart. The chart legend will show the series names.
     *
     * @param chartTitle
     *            the chart title
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param series
     *            the time series to show
     * @param movingAverageTimeSeries
     *            the moving average time series
     * @param startTime
     *            the start time of the x-axis
     * @param endTime
     *            the end time of the x-axis
     * @param showDots
     *            whether to additionally visualize the values as dots
     * @param chartScale
     *            the chart scale to use
     * @param cappingValue
     *            the value at which to cap the chart
     * @return the chart
     */
    public static JFreeChart createLineChart(final String chartTitle, final String rangeAxisTitle, final TimeSeries timeSeries,
                                             final TimeSeries movingAverageTimeSeries, final long startTime, final long endTime,
                                             final boolean showDots, final ChartScale chartScale, final int cappingValue)
    {
        final DateAxis timeAxis = createTimeAxis(startTime, endTime);
        final XYPlot linePlot = createLinePlot(timeSeries, movingAverageTimeSeries, timeAxis, rangeAxisTitle, showDots, chartScale,
                                               cappingValue);

        return createChart(chartTitle, linePlot);
    }

    /**
     * Creates a line plot from the passed time series and average time series.
     *
     * @param series
     *            the series
     * @param movingAverageSeries
     *            the moving average series
     * @param domainAxis
     *            the domain axis (may be <code>null</code>)
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param showDots
     *            whether to additionally visualize the values as dots
     * @param chartScale
     *            the chart scale to use
     * @param cappingValue
     *            the value at which to cap the chart
     * @return the line plot
     */
    public static XYPlot createLinePlot(final TimeSeries series, final TimeSeries movingAverageSeries, final ValueAxis domainAxis,
                                        final String rangeAxisTitle, final boolean showDots, final ChartScale chartScale,
                                        final int cappingValue)
    {
        // adjust time series if necessary
        if (chartScale == ChartScale.LOGARITHMIC)
        {
            adjustSeriesForLogarithmicAxes(series);
            adjustSeriesForLogarithmicAxes(movingAverageSeries);
        }

        // response time axis
        final NumberAxis rangeAxis = chartScale == ChartScale.LOGARITHMIC ? new LogarithmicAxis(rangeAxisTitle)
                                                                         : new NumberAxis(rangeAxisTitle);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // response time plot
        final XYPlot plot = new XYPlot(null, domainAxis, rangeAxis, null);

        // moving average line renderer
        if (movingAverageSeries != null)
        {
            final XYLineAndShapeRenderer movingAverageLineRenderer = createLineRenderer(COLOR_MOVING_AVERAGE);

            plot.setRenderer(0, movingAverageLineRenderer);
            plot.setDataset(0, new TimeSeriesCollection(movingAverageSeries));
        }

        // dots renderer
        if (showDots)
        {
            final DotsRenderer dotsRenderer = new DotsRenderer();
            dotsRenderer.setSeriesPaint(0, COLOR_LINE);
            dotsRenderer.setDotWidth(2);
            dotsRenderer.setDotHeight(2);

            plot.setRenderer(1, dotsRenderer);
            plot.setDataset(1, new MinMaxTimeSeriesCollection(series));
        }

        // line renderer
        final Color lineColor = showDots ? COLOR_LINE_DIMMED : COLOR_LINE;
        final XYLineAndShapeRenderer lineRenderer = createLineRenderer(lineColor);
        lineRenderer.setSeriesVisibleInLegend(0, !showDots);

        plot.setRenderer(2, lineRenderer);
        plot.setDataset(2, new MinMaxTimeSeriesCollection(series));

        // cap the plot if necessary
        capPlot(plot, cappingValue, false);

        return plot;
    }

    /**
     * Creates a chart from the passed time series collection and gives it the specified title. Any time series in the
     * collection is added to the chart. The chart legend will show the series names.
     *
     * @param chartTitle
     *            the chart title
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param collection
     *            the time series collection to show
     * @param startTime
     *            minimum time of the chart
     * @param endTime
     *            maximum time of the chart
     * @return the chart
     */
    public static JFreeChart createLineChart(final String chartTitle, final String rangeAxisTitle, final TimeSeriesCollection collection,
                                             final long startTime, final long endTime)
    {
        return createLineChart(chartTitle, rangeAxisTitle, collection, startTime, endTime, ColorSet.A);
    }

    /**
     * Creates a chart from the passed time series collection and gives it the specified title. Any time series in the
     * collection is added to the chart. The chart legend will show the series names.
     *
     * @param chartTitle
     *            the chart title
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param dataset
     *            the time series collection to show
     * @param startTime
     *            minimum time of the chart
     * @param endTime
     *            maximum time of the chart
     * @param colorSet
     *            the colors to use
     * @return the chart
     */
    public static JFreeChart createLineChart(final String chartTitle, final String rangeAxisTitle, final TimeSeriesCollection dataset,
                                             final long startTime, final long endTime, final ColorSet colorSet)
    {
        final DateAxis timeAxis = createTimeAxis(startTime, endTime);
        final XYPlot linePlot = createLinePlot(dataset, timeAxis, rangeAxisTitle, colorSet);

        return createChart(chartTitle, linePlot);
    }

    /**
     * Creates a line plot from the passed time series collection. Any time series in the collection is added to the
     * plot.
     *
     * @param dataset
     *            the time series collection to show
     * @param domainAxis
     *            the domain axis (may be <code>null</code>)
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param colorSet
     *            the colors to use
     * @return the chart
     */
    public static XYPlot createLinePlot(final XYDataset dataset, final ValueAxis domainAxis, final String rangeAxisTitle,
                                        final ColorSet colorSet)
    {
        return new XYPlot(dataset, domainAxis, createNumberAxis(rangeAxisTitle), createLineRenderer(colorSet));
    }

    /**
     * Creates a new line renderer with standard settings applied. The line renderer is capable of rendering min/max
     * values.
     *
     * @param colorSet
     *            the line colors (may be <code>null</code>)
     * @return the line renderer
     */
    public static XYLineAndShapeRenderer createLineRenderer(final ColorSet colorSet)
    {
        // line renderer
        final XYLineAndShapeRenderer renderer = new MinMaxRenderer(true, false);
        renderer.setLegendLine(LEGEND_LINE_SHAPE);

        // set the colors
        if (colorSet != null)
        {
            int index = 0;
            for (final Color color : colorSet.getColors())
            {
                renderer.setSeriesPaint(index++, color);
            }
        }

        return renderer;
    }

    /**
     * Creates a new line renderer with standard settings applied. The line renderer is capable of rendering min/max
     * values.
     *
     * @param color
     *            the line color
     * @return the line renderer
     */
    public static XYLineAndShapeRenderer createLineRenderer(final Color color)
    {
        return createLineRenderer(new ColorSet(color));
    }

    /**
     * Creates a "moving average" time series from the given time series.
     *
     * @param series
     *            the source series
     * @param percentage
     *            the percentaged amount of values for building the moving average
     * @return the time series
     */
    public static TimeSeries createMovingAverageTimeSeries(final TimeSeries series, final int percentage)
    {
        // take the last X percent of the values
        final int samples = Math.max(2, series.getItemCount() * percentage / 100);

        // derive the name from the source series
        final String avgSeriesName = series.getKey() + " (Moving Average)";

        // return MovingAverage.createMovingAverage(series, avgSeriesName, samples, samples);
        return MovingAverage.createPointMovingAverage(series, avgSeriesName, samples);
    }

    /**
     * Creates a number axis with standard settings applied.
     *
     * @param axisTitle
     *            the axis title (in case of <code>null</code>, a default title will be set)
     * @return the axis
     */
    public static NumberAxis createNumberAxis(String axisTitle)
    {
        if (StringUtils.isBlank(axisTitle))
        {
            axisTitle = DEFAULT_VALUE_AXIS_TITLE;
        }

        final NumberAxis numberAxis = new NumberAxis(axisTitle);
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return numberAxis;
    }

    /**
     * Creates a placeholder chart and stores it to the specified directory. Actually the placeholder chart is an empty
     * WebP file with the same dimensions as the regular charts.
     *
     * @param outputDir
     *            the directory to which to save the chart
     */
    public static void createPlaceholderChart(final File outputDir, final int width, final int height)
    {
        final File outputFile = new File(outputDir, XltConstants.REPORT_CHART_PLACEHOLDER_FILENAME);

        try
        {
            // create the image with the correct dimensions
            final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // set white background
            final Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setBackground((Color) DEFAULT_CHART_THEME.getChartBackgroundPaint());
            graphics.clearRect(0, 0, width, height);

            // draw some info text
            final Font font = new Font("SansSerif", Font.BOLD, 32);
            graphics.setFont(font);
            final FontMetrics fontMetrics = graphics.getFontMetrics();
            final int stringWidth = fontMetrics.stringWidth(XltConstants.REPORT_CHART_PLACEHOLDER_MESSAGE);
            final int stringHeight = fontMetrics.getAscent();
            graphics.setPaint(new Color(0xcccccc));
            graphics.drawString(XltConstants.REPORT_CHART_PLACEHOLDER_MESSAGE, (width - stringWidth) / 2, height / 2 + stringHeight / 4);
            graphics.dispose();

            // Encode image as webp using default settings and save it as webp file
            ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
         
            // Set parameters for lossless webp files
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());

            // Notify encoder to consider WebPWriteParams
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            // Set lossless compression
            writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);
         
            // Set quality of images
            writeParam.setCompressionQuality(webpCompressionQuality);

            // Save the image
            writer.setOutput(new FileImageOutputStream(outputFile));
            writer.write(null, new IIOImage(bufferedImage, null, null), writeParam);
        }
        catch (final IOException e)
        {
            log.error("Failed to save placeholder chart to file: " + outputFile, e);
        }
    }

    /**
     * Creates an invisible plot which can be used to fill up space in combined plots.
     *
     * @return the spacer plot
     */
    public static XYPlot createSpacerPlot()
    {
        final XYPlot spacerPlot = new XYPlot();

        spacerPlot.setForegroundAlpha(0);
        spacerPlot.setBackgroundAlpha(0);
        spacerPlot.setOutlineVisible(false);

        return spacerPlot;
    }

    /**
     * Creates a time axis with standard settings applied. This includes limiting the visible time range and appending
     * the time zone to the axis title.
     *
     * @param startTime
     *            the minimum time to show
     * @param endTime
     *            the maximum time to show
     * @return the axis
     */
    public static DateAxis createTimeAxis(final long startTime, final long endTime)
    {
        return createTimeAxis(DEFAULT_DATE_AXIS_TITLE, startTime, endTime);
    }

    /**
     * Creates a time axis with standard settings applied. This includes limiting the visible time range and appending
     * the time zone to the axis title.
     *
     * @param axisTitle
     *            the axis title (in case of <code>null</code>, a default title will be set)
     * @param startTime
     *            the minimum time to show
     * @param endTime
     *            the maximum time to show
     * @return the axis
     */
    public static DateAxis createTimeAxis(String axisTitle, final long startTime, final long endTime)
    {
        final Date startDate = new Date(startTime);

        // determine the time axis title
        final TimeZone tz = TimeZone.getDefault();

        if (StringUtils.isBlank(axisTitle))
        {
            axisTitle = DEFAULT_DATE_AXIS_TITLE;
        }

        axisTitle = axisTitle + " [" + tz.getDisplayName(tz.inDaylightTime(startDate), TimeZone.SHORT, Locale.US) + "]";

        // create the time axis
        final DateAxis timeAxis = new DateAxis(axisTitle);
        updateDateAxisMinMaxTime(timeAxis, startTime, endTime);

        return timeAxis;
    }

    private static void updateDateAxisMinMaxTime(final DateAxis timeAxis, final long startTime, final long endTime)
    {
        // set the visible time range
        if (startTime > 0 && endTime < Long.MAX_VALUE)
        {
            // add 1% of the time range as right margin, so details drawn at the very edge will become visible
            final long margin = (endTime - startTime) / 100;

            timeAxis.setMinimumDate(new Date(startTime));
            timeAxis.setMaximumDate(new Date(endTime + margin));
        }
    }

    /**
     * Computes the value at which the y-axis of a chart should been capped.
     *
     * @param cappingInfo
     *            the capping settings
     * @param averageValue
     *            the average value of the data set
     * @param maxValue
     *            the maximum value of the data set
     * @return the capping value, or -1 if the chart should not be capped
     */
    public static int getChartCappingValue(final ChartCappingInfo cappingInfo, final double averageValue, final int maxValue)
    {
        int effectiveCappingValue;

        // first calculate the capping value according to the capping method
        switch (cappingInfo.method)
        {
            case ABSOLUTE:
                effectiveCappingValue = (int) cappingInfo.parameter;
                break;
            case NFOLD_OF_AVERAGE:
                effectiveCappingValue = (int) (averageValue * cappingInfo.parameter);
                break;
            default:
                effectiveCappingValue = -1;
                break;
        }

        // now let the capping mode modify the capping value
        if (effectiveCappingValue >= maxValue && cappingInfo.mode == ChartCappingMode.SMART)
        {
            effectiveCappingValue = -1;
        }

        return effectiveCappingValue;
    }

    /**
     * Returns the {@link Second} object that corresponds to the given millisecond value. This method creates and caches
     * a new object only if it has not been requested so far and returns a cached object in any subsequent call.
     * Creating object is expensive.
     *
     * @param time
     *            the time in milliseconds
     * @return the corresponding {@link Second} object
     */
    public static synchronized Second getSecond(long time)
    {
        // rub out milliseconds -> we have seconds precision only
        time = time / 1000 * 1000;

        // lookup/create a Second for this time value
        Second second = secondsCache.get(time);
        if (second == null)
        {
            second = new Second(new Date(time));
            secondsCache.put(time, second);
        }

        return second;
    }

    /**
     * Saves the given chart in the WebP format to a given file.
     *
     * @param chart
     *            the chart
     * @param outputFile
     *            the target file
     * @param chartWidth
     *            the chart width
     * @param chartHeight
     *            the chart height
     */
    public static void saveChart(final JFreeChart chart, final File outputFile, final int chartWidth, final int chartHeight)
    {
        // first of all apply the XLT chart theme to the chart
        DEFAULT_CHART_THEME.apply(chart);

        try
        {
            // brand chart
            final BufferedImage bufferedImage = chart.createBufferedImage(chartWidth, chartHeight);
            final Graphics2D g2d = bufferedImage.createGraphics();

            // prepare watermark settings
            g2d.setFont(DEFAULT_CHART_THEME.getSmallFont());
            g2d.setColor(WATERMARK_COLOR);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // insert watermark
            final FontMetrics fontMetrics = g2d.getFontMetrics();
            final int textWidth = (int) fontMetrics.getStringBounds(WATERMARK_TEXT, g2d).getWidth();
            final int x = chartWidth - (1 + 8 + textWidth);
            final int y = 1 + 8 + fontMetrics.getAscent();
            g2d.drawString(WATERMARK_TEXT, x, y);
            g2d.dispose();

            // Encode image as webp using default settings and save it as webp file
            ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
         
            // Set parameters for lossless webp files
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());

            // Notify encoder to consider WebPWriteParams
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            // Set lossless compression
            writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);

            // Set quality of images
            writeParam.setCompressionQuality(webpCompressionQuality);
         
            // Save the image
            writer.setOutput(new FileImageOutputStream(outputFile));
            writer.write(null, new IIOImage(bufferedImage, null, null), writeParam);
        }
        catch (final IOException e)
        {
            log.error("Failed to save chart to file: " + outputFile, e);
        }
    }

    /**
     * Saves the given chart in the WebP format to a file with the passed name in the specified directory.
     *
     * @param chart
     *            the chart
     * @param name
     *            the file name (excluding the .webp extension)
     * @param outputDir
     *            the target directory
     * @param chartWidth
     *            the chart width
     * @param chartHeight
     *            the chart height
     */
    public static void saveChart(final JFreeChart chart, final String name, final File outputDir, final int chartWidth,
                                 final int chartHeight)
    {
        final File outputFile = new File(outputDir, FileUtils.convertIllegalCharsInFileName(name) + ".webp");

        saveChart(chart, outputFile, chartWidth, chartHeight);
    }

    /**
     * Adds the given time series collection and axis title to the given chart.
     *
     * @param chart
     *            the chart to modify
     * @param rangeAxisTitle
     *            the name of the y-axis
     * @param seriesCollection
     *            the time series collection to show
     */
    public static void setAxisTimeSeriesCollection(final JFreeChart chart, final int axisIndex, final String rangeAxisTitle,
                                                   final List<TimeSeriesConfiguration> seriesConfigurations)
    {
        final XYPlot plot = (XYPlot) chart.getPlot();
        final int dsCount = plot.getDatasetCount();

        // initialize renderer and series collection
        final XYItemRenderer renderer = createLineRenderer((ColorSet) null);
        final TimeSeriesCollection seriesCollection = new TimeSeriesCollection();

        // setup renderer and build series collection
        for (int index = 0; index < seriesConfigurations.size(); index++)
        {
            final TimeSeriesConfiguration seriesConfig = seriesConfigurations.get(index);

            // add series to collection
            seriesCollection.addSeries(seriesConfig.getTimeSeries());

            // update renderer (line style)
            final TimeSeriesConfiguration.Style style = seriesConfig.getStyle();
            if (style != TimeSeriesConfiguration.Style.LINE)
            {
                final float lineWidth = 0.5f;
                final float dash[] =
                    {
                        5.0f
                    };
                final float dot[] =
                    {
                        lineWidth
                    };

                BasicStroke stroke;
                switch (style)
                {
                    case DASH:
                    {
                        stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
                        break;
                    }
                    case DOT:
                    {
                        stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
                        break;
                    }
                    default:
                    {
                        log.warn("unknown line style '" + style + "'. Use LINE style by default.");
                        stroke = new BasicStroke(lineWidth);
                    }
                }
                renderer.setSeriesStroke(index, stroke);
            }

            // define color
            final ColorSet colorSet = axisIndex < 1 ? ColorSet.A : ColorSet.B;
            final Color color = seriesConfig.getColor() != null ? seriesConfig.getColor() : colorSet.get((index + colorSet.size()) %
                                                                                                         colorSet.size());

            // update renderer (color)
            renderer.setSeriesPaint(index, color);
        }

        // set y-Axis
        final NumberAxis yAxis = createNumberAxis(rangeAxisTitle != null ? rangeAxisTitle : DEFAULT_VALUE_AXIS_TITLE);

        // update plot
        plot.setDataset(dsCount, seriesCollection);
        plot.setRangeAxis(axisIndex == 1 ? 1 : 0, yAxis);
        plot.mapDatasetToRangeAxis(dsCount, axisIndex == 1 ? 1 : 0);
        plot.setRenderer(dsCount, renderer);
    }

    /**
     * Creates a new time series with the given name from the passed min-max value set. The data items in the time
     * series will be {@link DoubleMinMaxTimeSeriesDataItem} objects, so the minimum/maximum/count/accumulated value
     * properties of a {@link DoubleMinMaxValue} will still be available.
     *
     * @param minMaxValueSet
     *            the source min-max value set
     * @param timeSeriesName
     *            the name of the time series
     * @return the time series
     */
    public static TimeSeries toMinMaxTimeSeries(final DoubleMinMaxValueSet valueSet, final String timeSeriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(timeSeriesName);

        if (valueSet.getValueCount() > 0)
        {
            final DoubleMinMaxValue[] values = valueSet.getValues();
            long time = valueSet.getMinimumTime();
            final int timeIncrement = valueSet.getScale() * 1000;

            for (int i = 0; i < values.length; i++)
            {
                final DoubleMinMaxValue value = values[i];
                if (value != null)
                {
                    final Second second = getSecond(time);
                    timeSeries.add(new DoubleMinMaxTimeSeriesDataItem(second, value));
                }

                time += timeIncrement;
            }
        }

        return timeSeries;
    }

    /**
     * Creates a new time series with the given name from the passed min-max value set. The data items in the time
     * series will be {@link MinMaxTimeSeriesDataItem} objects, so the minimum/maximum/count/accumulated value
     * properties of a {@link MinMaxValue} will still be available.
     *
     * @param minMaxValueSet
     *            the source min-max value set
     * @param timeSeriesName
     *            the name of the time series
     * @return the time series
     */
    public static TimeSeries toMinMaxTimeSeries(final MinMaxValueSet minMaxValueSet, final String timeSeriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(timeSeriesName);

        if (minMaxValueSet.getValueCount() > 0)
        {
            final MinMaxValue[] values = minMaxValueSet.getValues();
            long time = minMaxValueSet.getMinimumTime();
            final int scale = minMaxValueSet.getScale();

            for (int i = 0; i < values.length; i++)
            {
                final MinMaxValue value = values[i];

                if (value != null)
                {
                    final Second second = getSecond(time);

                    timeSeries.add(new MinMaxTimeSeriesDataItem(second, value));
                }

                time = time + scale * 1000;
            }
        }

        return timeSeries;
    }

    /**
     * Creates a new time series with the given name from the passed min-max value set. The data items in the time
     * series will get the maximum value from the corresponding value in the value set. This is especially useful for
     * bar charts.
     *
     * @param minMaxValueSet
     *            the source min-max value set
     * @param timeSeriesName
     *            the name of the time series
     * @return the time series
     */
    public static TimeSeries toStandardTimeSeries(final MinMaxValueSet minMaxValueSet, final String timeSeriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(timeSeriesName);

        if (minMaxValueSet.getValueCount() > 0)
        {
            final MinMaxValue[] values = minMaxValueSet.getValues();
            long time = minMaxValueSet.getMinimumTime();
            final int scale = minMaxValueSet.getScale();

            for (int i = 0; i < values.length; i++)
            {
                final MinMaxValue value = values[i];

                if (value != null)
                {
                    final Second second = getSecond(time);

                    timeSeries.add(second, value.getMaximumValue());
                }

                time = time + scale * 1000;
            }
        }

        return timeSeries;
    }

    /**
     * Creates a time series with rate values calculated from the given count/total count values sets. This method
     * assumes that the count value set is always a sub set of the total count value set.
     *
     * @param countValueSet
     *            the count values
     * @param totalCountValueSet
     *            the total count values
     * @param minMaxValueSetSize
     *            the size to use for min-max value sets
     * @return the rate time series
     */
    public static TimeSeries calculateRateTimeSeries(final ValueSet countValueSet, final ValueSet totalCountValueSet,
                                                     final int minMaxValueSetSize, final String seriesName)
    {
        final TimeSeries rateTimeSeries = new TimeSeries(seriesName);

        // leave early if there are no total count values
        if (totalCountValueSet.getValueCount() == 0)
        {
            return rateTimeSeries;
        }

        // make the count value set the same size as the total count set by simply adding 0 values
        countValueSet.addOrUpdateValue(totalCountValueSet.getMinimumTime(), 0);
        countValueSet.addOrUpdateValue(totalCountValueSet.getMaximumTime(), 0);

        // get start time (values are equal for both input value sets)
        long time = totalCountValueSet.getMinimumTime();

        // calculate the rate time series
        final int[] counts = countValueSet.getValues();
        final int[] totalCounts = totalCountValueSet.getValues();

        for (int i = 0; i < counts.length; i++)
        {
            // create rate values only when there have been finished transactions
            if (totalCounts[i] > 0)
            {
                final double rate = 100.0 * counts[i] / totalCounts[i];
                rateTimeSeries.add(getSecond(time), rate);
            }

            time = time + 1000;
        }

        return rateTimeSeries;
    }

    /**
     * Fixes the given time series such that negative or 0 values are replaced with a very small positive number so the
     * time series can be used together with logarithmic axes.
     *
     * @param timeSeries
     *            the time series
     */
    public static void adjustSeriesForLogarithmicAxes(final TimeSeries timeSeries)
    {
        for (int i = 0; i < timeSeries.getItemCount(); i++)
        {
            final TimeSeriesDataItem dataItem = timeSeries.getDataItem(i);

            if ((double) dataItem.getValue() <= 0)
            {
                dataItem.setValue(MIN_VALUE_FOR_LOGARITHMIC_AXES);
            }
        }
    }

    /**
     * Sets the compression quality (1 -> best quality, 0 -> highest compression) to use when creating Webp images.
     *
     * @param quality the compression quality
     */
    public static void setWebpCompressionLevel(final float quality)
    {
        if (0 <= quality && quality <= 1)
        {
            webpCompressionQuality = quality;
        }
        else
        {
            throw new IllegalArgumentException("The Webp compression quality must be between 0...1");
        }
    }

    /**
     * Returns the compression quality to use when creating Webp images.
     *
     * @return the compression quality
     */
    public static float getWebpCompressionLevel()
    {
        return webpCompressionQuality;
    }

    /**
     * Fixes the given interval series such that negative or 0 values are replaced with a very small positive number so
     * the interval series can be used together with logarithmic axes.
     *
     * @param intervalSeries
     *            the interval series
     */
    public static void adjustSeriesForLogarithmicAxes(final XYIntervalSeries intervalSeries)
    {
        for (int i = 0; i < intervalSeries.getItemCount(); i++)
        {
            final XYIntervalDataItem dataItem = (XYIntervalDataItem) intervalSeries.getDataItem(i);

            final java.lang.Double x = dataItem.getX();
            double yLow = dataItem.getYLowValue();
            double y = dataItem.getYLowValue();
            double yHigh = dataItem.getYLowValue();

            // check if the data item has to be adjusted
            if (yLow <= 0 || y <= 0 || yHigh <= 0)
            {
                // remove the old data item
                intervalSeries.remove(x);

                // limit the y values
                yLow = (yLow <= 0) ? MIN_VALUE_FOR_LOGARITHMIC_AXES : yLow;
                y = (y <= 0) ? MIN_VALUE_FOR_LOGARITHMIC_AXES : y;
                yHigh = (yHigh <= 0) ? MIN_VALUE_FOR_LOGARITHMIC_AXES : yHigh;

                // add a new data item
                intervalSeries.add(x, dataItem.getXLowValue(), dataItem.getXHighValue(), y, yLow, yHigh);
            }
        }
    }

    /**
     * Private constructor to avoid object instantiation.
     */
    private JFreeChartUtils()
    {
    }
}
