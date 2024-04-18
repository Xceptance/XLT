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

import java.io.File;
import java.util.Date;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.report.util.ConcurrentUsersTable;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.JFreeChartUtils.ColorSet;
import com.xceptance.xlt.report.util.IntMinMaxValueSet;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.ValueSet;

/**
 *
 */
public class GeneralReportProvider extends AbstractReportProvider
{
    private final ValueSet bytesReceivedValueSet = new ValueSet();

    private final ValueSet bytesSentValueSet = new ValueSet();

    private final ValueSet requestsValueSet = new ValueSet();

    private long totalBytesReceived = 0;

    private long totalBytesSent = 0;

    private long totalRequests = 0;

    private final ValueSet failedTransactionsValueSet = new ValueSet();

    private final ValueSet totalTransactionsValueSet = new ValueSet();

    private IntMinMaxValueSet requestRunTimeValueSet;

    private int minMaxValueSetSize;

    private SlowestRequestsTracker slowestRequestsTracker;

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralReport createReportFragment()
    {
        final ReportProviderConfiguration config = getConfiguration();
        final File chartsDir = config.getChartDirectory();

        // create the charts
        if (config.shouldChartsGenerated())
        {
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    JFreeChartUtils.createPlaceholderChart(chartsDir, config.getChartWidth(), config.getChartHeight());
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final IntMinMaxValueSet bytesReceived = bytesReceivedValueSet.toMinMaxValueSet(minMaxValueSetSize);
                    createChart(JFreeChartUtils.toMinMaxTimeSeries(bytesReceived, "Received Bytes/s"), true, "Received Bytes Per Second",
                                "Bytes", "ReceivedBytesPerSecond", chartsDir);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final IntMinMaxValueSet bytesSent = bytesSentValueSet.toMinMaxValueSet(minMaxValueSetSize);
                    createChart(JFreeChartUtils.toMinMaxTimeSeries(bytesSent, "Sent Bytes/s"), true, "Sent Bytes Per Second", "Bytes",
                                "SentBytesPerSecond", chartsDir);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final IntMinMaxValueSet requests = requestsValueSet.toMinMaxValueSet(minMaxValueSetSize);
                    createChart(JFreeChartUtils.toMinMaxTimeSeries(requests, "Requests/s"), true, "Requests Per Second", "Requests",
                                "RequestsPerSecond", chartsDir);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final ValueSet concurrentUsersValueSet = ConcurrentUsersTable.getInstance().getConcurrentUsersValueSet();
                    final IntMinMaxValueSet concurrentUsers = concurrentUsersValueSet.toMinMaxValueSet(minMaxValueSetSize);
                    createChart(JFreeChartUtils.toMinMaxTimeSeries(concurrentUsers, "Concurrent Users"), true, "Concurrent Users", "Users",
                                "ConcurrentUsers", chartsDir);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    createChart(JFreeChartUtils.toMinMaxTimeSeries(requestRunTimeValueSet, "Request Runtime"), true, "Request Runtime",
                                "Runtime [ms]", "RequestRuntime", chartsDir);
                }
            });

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final IntMinMaxValueSet failedTransactions = failedTransactionsValueSet.toMinMaxValueSet(minMaxValueSetSize);
                    final TimeSeries failedTransactionsTimeSeries = JFreeChartUtils.toStandardTimeSeries(failedTransactions,
                                                                                                         "Transaction Errors/s");

                    final TimeSeries errorRateTimeSeries = JFreeChartUtils.calculateRateTimeSeries(failedTransactionsValueSet,
                                                                                                   totalTransactionsValueSet,
                                                                                                   minMaxValueSetSize, "Error Rate");
                    final TimeSeries errorRateAverageTimeSeries = JFreeChartUtils.createMovingAverageTimeSeries(errorRateTimeSeries,
                                                                                                                getConfiguration().getMovingAveragePercentage());

                    createErrorsChart(failedTransactionsTimeSeries, errorRateAverageTimeSeries, "Transaction Errors", "TransactionErrors",
                                      chartsDir);
                }
            });
        }

        // create the report fragment
        final GeneralReport report = new GeneralReport();

        final long testStartTime = config.getChartStartTime();
        final long testEndTime = config.getChartEndTime();

        report.startTime = new Date(testStartTime);
        report.endTime = new Date(testEndTime);
        report.duration = Math.round((testEndTime - testStartTime) / 1000.0F);
        report.hits = totalRequests;
        report.bytesSent = totalBytesSent;
        report.bytesReceived = totalBytesReceived;

        if (slowestRequestsTracker != null)
        {
            report.slowestRequests = slowestRequestsTracker.getSlowestRequests();
        }

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            final RequestData reqData = (RequestData) data;
            final long time = reqData.getTime();
            final long endTime = reqData.getEndTime();
            final int runTime = reqData.getRunTime();
            final long sendCompletedAt = time + reqData.getConnectTime() + reqData.getSendTime();

            totalBytesSent += reqData.getBytesSent();
            totalBytesReceived += reqData.getBytesReceived();
            bytesSentValueSet.addOrUpdateValue(sendCompletedAt, reqData.getBytesSent());
            bytesReceivedValueSet.addOrUpdateValue(endTime, reqData.getBytesReceived());

            totalRequests++;
            requestsValueSet.addOrUpdateValue(endTime, 1);

            requestRunTimeValueSet.addOrUpdateValue(endTime, runTime);

            if (slowestRequestsTracker != null)
            {
                slowestRequestsTracker.update(reqData);
            }
        }
        else if (data instanceof TransactionData)
        {
            final TransactionData txnData = (TransactionData) data;
            final long time = txnData.getTime();
            final long endTime = txnData.getEndTime();

            ConcurrentUsersTable.getInstance().recordUserActivity(time, endTime, txnData.getName(), txnData.getTestUserNumber());

            // count the transaction at the time it has finished
            totalTransactionsValueSet.addOrUpdateValue(endTime, 1);

            if (txnData.hasFailed())
            {
                failedTransactionsValueSet.addOrUpdateValue(endTime, 1);
            }
        }
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
     */
    protected void createChart(final TimeSeries timeSeries, final boolean showMovingAverage, final String title, final String yAxisTitle,
                               final String fileName, final File outputDir)
    {
        final ReportProviderConfiguration config = getConfiguration();

        // System.out.printf("Creating '%s' chart ... \n", title);

        // final long start = TimerUtils.getTime();

        final JFreeChart chart = JFreeChartUtils.createLineChart(title, yAxisTitle, timeSeries, config.getChartStartTime(),
                                                                 config.getChartEndTime(), showMovingAverage,
                                                                 config.getMovingAveragePercentage());

        JFreeChartUtils.saveChart(chart, fileName, outputDir, config.getChartWidth(), config.getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", timeSeries.getItemCount(), TimerUtils.getTime() - start);
    }

    /**
     * Creates a chart showing both the number of transaction errors in a second and the corresponding error rate.
     *
     * @param errorCountTimeSeries
     *            the error count values
     * @param errorRateTimeSeries
     *            the error rate values
     * @param title
     *            the chart title
     * @param fileName
     *            the name of the chart file
     * @param outputDir
     *            the directory to which to save the chart
     */
    protected void createErrorsChart(final TimeSeries errorCountTimeSeries, final TimeSeries errorRateTimeSeries, final String title,
                                     final String fileName, final File outputDir)
    {
        final ReportProviderConfiguration config = getConfiguration();

        // System.out.printf("Creating '%s' chart ... \n", title);

        // final long start = TimerUtils.getTime();

        final XYPlot errorCountPlot = JFreeChartUtils.createBarPlot(new TimeSeriesCollection(errorCountTimeSeries), null, "Error Count",
                                                                    JFreeChartUtils.COLOR_ERROR);

        final XYPlot errorRatePlot = JFreeChartUtils.createLinePlot(new TimeSeriesCollection(errorRateTimeSeries), null, "Error Rate [%]",
                                                                    new ColorSet(JFreeChartUtils.COLOR_ERROR, JFreeChartUtils.COLOR_LINE));

        final ValueAxis errorRateAxis = errorRatePlot.getRangeAxis();
        errorRateAxis.setLowerBound(0);

        final double maxRate = errorRateTimeSeries.getMaxY();
        if (0 < maxRate && maxRate < 1)
        {
            // the max value is smaller than 1, so no tick mark will be shown with integer ticks -> switch to decimal
            errorRateAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        }

        final CombinedDomainXYPlot combinedPlot = JFreeChartUtils.createCombinedPlot(config.getChartStartTime(), config.getChartEndTime());
        combinedPlot.add(errorCountPlot);
        combinedPlot.add(errorRatePlot);

        final JFreeChart chart = JFreeChartUtils.createChart(title, combinedPlot);

        JFreeChartUtils.saveChart(chart, fileName, outputDir, config.getChartWidth(), config.getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", timeSeries.getItemCount(), TimerUtils.getTime() - start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        // setup run time value set
        minMaxValueSetSize = getConfiguration().getChartWidth();
        requestRunTimeValueSet = new IntMinMaxValueSet(minMaxValueSetSize);

        // for now: track slowest requests only if the magic property is set
        if (config.getProperties().getProperty("monitoring.trackSlowestRequests") != null)
        {
            slowestRequestsTracker = new SlowestRequestsTracker(10);
        }
    }
}
