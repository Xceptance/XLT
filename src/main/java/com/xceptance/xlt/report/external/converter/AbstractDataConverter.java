package com.xceptance.xlt.report.external.converter;

import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;

import com.xceptance.xlt.api.report.ReportCreator;
import com.xceptance.xlt.report.external.config.ChartConfig;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.TimeSeriesConfiguration;

/**
 * Base class of all external data converters.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
abstract public class AbstractDataConverter implements ReportCreator
{
    /**
     * The report provider's configuration.
     */
    private ConverterConfiguration config;

    /**
     * Parse the data.
     * 
     * @param time
     *            timestamp of data
     * @param input
     *            named data
     */
    abstract public void parse(final long time, final Map<String, Object> input) throws IllegalArgumentException;

    /**
     * Create chart from configuration file and data series
     * 
     * @param axisCollections
     * @param chartConfig
     * @return chart file name
     */
    protected void createFloatingChart(final List<List<TimeSeriesConfiguration>> axisCollections, final ChartConfig chartConfig,
                                       final String fileName)
    {
        final JFreeChart chart = JFreeChartUtils.createBasicLineChart(chartConfig.getTitle(), chartConfig.getXAxisTitle(),
                                                                      getConfiguration().getChartStartTime(),
                                                                      getConfiguration().getChartEndTime());

        for (int axisIndex = 0; axisIndex < axisCollections.size(); axisIndex++)
        {
            if (!axisCollections.get(axisIndex).isEmpty())
            {
                final String yAxisTitle = axisIndex < 1 ? chartConfig.getYAxisTitle() : chartConfig.getYAxisTitle2();
                JFreeChartUtils.setAxisTimeSeriesCollection(chart, axisIndex, yAxisTitle, axisCollections.get(axisIndex));
            }
        }

        JFreeChartUtils.saveChart(chart, fileName, getConfiguration().getExternalChartsDir(), getConfiguration().getChartWidth(),
                                  getConfiguration().getChartHeight());
    }

    /**
     * Returns the converter configuration.
     * 
     * @return converter configuration
     */
    public ConverterConfiguration getConfiguration()
    {
        return config;
    }

    /**
     * Sets the converter configuration.
     * 
     * @param config
     *            the converter configuration to be set
     */
    public void setConfiguration(final ConverterConfiguration config)
    {
        this.config = config;
    }
}
