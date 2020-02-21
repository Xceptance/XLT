package com.xceptance.xlt.report.providers;

import java.io.File;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYIntervalSeries;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.ConcurrentUsersTable;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.JFreeChartUtils.ColorSet;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.ValueSet;

/**
 * The {@link TransactionDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link TransactionData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * <li>creation of CSV files with all the data</li>
 * </ul>
 * In addition, this processor handles {@link EventData} objects, since events are shown in the response time chart as
 * well.
 */
public class TransactionDataProcessor extends BasicTimerDataProcessor
{
    /**
     * The value set maintaining the number of events per second.
     */
    private final ValueSet eventsPerSecond = new ValueSet();

    /**
     * The value set maintaining the current arrival rate per second.
     */
    private final ValueSet arrivalsPerHourPerSecond = new ValueSet();

    /**
     * The number of events.
     */
    private int numberOfEvents = 0;

    /**
     * Constructor.
     *
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public TransactionDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        setChartDir(new File(getChartDir(), "transactions"));
        setCsvDir(new File(getCsvDir(), "transactions"));

        // set capping parameters
        final ReportGeneratorConfiguration config = (ReportGeneratorConfiguration) getConfiguration();
        setChartCappingInfo(config.getTransactionChartCappingInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerReport createTimerReport(final boolean generateHistogram)
    {
        if (getConfiguration().shouldChartsGenerated())
        {
            // create additional charts
            TaskManager.getInstance().addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final ValueSet concurrentUsersVS = ConcurrentUsersTable.getInstance().getConcurrentUsersValueSet(getName());

                    final TimeSeries concurrentUsersTS = JFreeChartUtils.toMinMaxTimeSeries(concurrentUsersVS.toMinMaxValueSet(getChartWidth()),
                                                                                            "Concurrent Users");

                    createChart(concurrentUsersTS, true, getName(), "Users", getName() + "_ConcurrentUsers", getChartDir(),
                                "concurrent users", true);
                }
            });

            // create the arrival rate chart as an automatically generated moving average
            TaskManager.getInstance().addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final TimeSeries arrivalRateTS = JFreeChartUtils.toMinMaxTimeSeries(arrivalsPerHourPerSecond.toMinMaxValueSet(getChartWidth()),
                                                                                        "Current Arrival Rate");

                    final TimeSeries averagedArrivalRateTS = JFreeChartUtils.createMovingAverageTimeSeries(arrivalRateTS,
                                                                                                           getMovingAveragePercentage());
                    averagedArrivalRateTS.setKey("Current Arrival Rate");

                    createChart(averagedArrivalRateTS, true, getName(), "Arrival Rate [1/h]", getName() + "_ArrivalRate", getChartDir(),
                                "arrival rate", false);
                }
            });
        }

        // create the standard timer report
        final TransactionReport transactionReport = (TransactionReport) super.createTimerReport(generateHistogram);
        transactionReport.events = numberOfEvents;

        return transactionReport;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this works correctly only if only instances of {@link TransactionData} or {@link EventData} are given
     * as arguments for this processor class!
     * </p>
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof TransactionData)
        {
            super.processDataRecord(data);

            arrivalsPerHourPerSecond.addOrUpdateValue(data.getTime(), 3600);
        }
        else
        {
            // must be event data, don't count arrivals in this case
            eventsPerSecond.addOrUpdateValue(data.getTime(), 1);
            numberOfEvents++;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JFreeChart createResponseTimeAndErrorsChart(final String name, final TimeSeries responseTimeSeries,
                                                          final TimeSeries responseTimeAverageSeries,
                                                          final XYIntervalSeries responseTimeHistogramSeries,
                                                          final TimeSeries errorsSeries, final int chartCappingValue)
    {
        final JFreeChart chart = super.createResponseTimeAndErrorsChart(name, responseTimeSeries, responseTimeAverageSeries,
                                                                        responseTimeHistogramSeries, errorsSeries, chartCappingValue);

        // get the chart's left and right sub plots
        final CombinedRangeXYPlot outerCombinedPlot = (CombinedRangeXYPlot) chart.getXYPlot();
        final List<?> subPlots = outerCombinedPlot.getSubplots();
        final CombinedDomainXYPlot leftCombinedPlot = (CombinedDomainXYPlot) subPlots.get(0);
        final CombinedDomainXYPlot rightCombinedPlot = (CombinedDomainXYPlot) subPlots.get(1);

        final int minMaxValueSetSize = getChartWidth();

        /*
         * Error Rate
         */

        // generate the error rate time series
        final TimeSeries errorRateTimeSeries = JFreeChartUtils.calculateRateTimeSeries(getErrorsPerSecondValueSet(),
                                                                                       getCountPerSecondValueSet(), minMaxValueSetSize,
                                                                                       "Error Rate");
        final TimeSeries errorRateAverageTimeSeries = JFreeChartUtils.createMovingAverageTimeSeries(errorRateTimeSeries,
                                                                                                    getMovingAveragePercentage());

        // create the error rate plot
        final XYPlot errorRatePlot = JFreeChartUtils.createLinePlot(new TimeSeriesCollection(errorRateAverageTimeSeries), null,
                                                                    "Errors [%]", new ColorSet(JFreeChartUtils.COLOR_ERROR));

        final ValueAxis errorRateAxis = errorRatePlot.getRangeAxis();
        errorRateAxis.setLowerBound(0);

        final double maxRate = errorRateAverageTimeSeries.getMaxY();
        if (0 < maxRate && maxRate < 1)
        {
            // the max value is smaller than 1, so no tick mark will be shown with integer ticks -> switch to decimal
            errorRateAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        }

        // add the plot and a spacer plot
        leftCombinedPlot.add(errorRatePlot, 1);
        rightCombinedPlot.add(JFreeChartUtils.createSpacerPlot(), 1);

        /*
         * Events
         */

        // generate the event time series
        final TimeSeries eventsPerSecondTimeSeries = JFreeChartUtils.toStandardTimeSeries(eventsPerSecond.toMinMaxValueSet(minMaxValueSetSize),
                                                                                          "Events/s");
        final TimeSeriesCollection eventsPerSecondTimeSeriesCollection = new TimeSeriesCollection(eventsPerSecondTimeSeries);

        // create the event plot
        final XYPlot eventPlot = JFreeChartUtils.createBarPlot(eventsPerSecondTimeSeriesCollection, null, "Events",
                                                               JFreeChartUtils.COLOR_EVENT);

        // add the plot and a spacer plot
        leftCombinedPlot.add(eventPlot, 1);
        rightCombinedPlot.add(JFreeChartUtils.createSpacerPlot(), 1);

        return chart;
    }

    /**
     * Creates a chart from the given timer list and stores it to the passed directory.
     *
     * @param timeSeries
     *            the values
     * @param showMovingAverage
     *            whether or not to show a moving average
     * @param title
     *            the chart title
     * @param yAxisTitle
     *            the title of the y-axis
     * @param fileName
     *            the name of the chart file
     * @param outputDir
     *            the directory to which to save the chart
     * @param chartType
     *            the type of chart being generated (for output purposes only)
     * @param showDots
     *            whether to show the values as dots additionally
     */
    protected void createChart(final TimeSeries timeSeries, final boolean showMovingAverage, final String title, final String yAxisTitle,
                               final String fileName, final File outputDir, final String chartType, final boolean showDots)
    {
        final ReportProviderConfiguration config = getConfiguration();

        //System.out.printf("Creating %s chart for timer '%s' ...\n", chartType, title);

        // final long start = TimerUtils.getTime();

        final JFreeChart chart = JFreeChartUtils.createLineChart(title, yAxisTitle, timeSeries, config.getChartStartTime(),
                                                                 config.getChartEndTime(), showMovingAverage,
                                                                 config.getMovingAveragePercentage(), showDots);

        JFreeChartUtils.saveChart(chart, fileName, outputDir, config.getChartWidth(), config.getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", timeSeries.getItemCount(), TimerUtils.getTime() - start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerReport createTimerReport()
    {
        return new TransactionReport();
    }
}
