/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.ArithmeticMean;
import com.xceptance.xlt.report.util.DoubleMinMaxValueSet;
import com.xceptance.xlt.report.util.DoubleSummaryStatistics;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.MinMaxTimeSeriesCollection;
import com.xceptance.xlt.report.util.MinMaxValueSet;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.TaskManager;

/**
 * The {@link AgentDataProcessor} is responsible to calculate statistic information for exactly one agent.
 */

public class AgentDataProcessor extends AbstractDataProcessor
{
    private final MinMaxValueSet blockedThreadsValueSet;

    private final DoubleMinMaxValueSet cpuUsageValueSet;

    private long fullGcCount;

    private final ArithmeticMean fullGcCpuUsageMean = new ArithmeticMean();

    private long fullGcTime;

    private final DoubleMinMaxValueSet gcCpuUsageValueSet;

    private long minorGcCount;

    private final ArithmeticMean minorGcCpuUsageMean = new ArithmeticMean();

    private long minorGcTime;

    private final MinMaxValueSet runnableThreadsValueSet;

    private final MinMaxValueSet totalHeapValueSet;

    private final MinMaxValueSet totalThreadsValueSet;

    private final MinMaxValueSet usedHeapValueSet;

    // private final MinMaxValueSet usedMemValueSet;

    private final MinMaxValueSet waitingThreadsValueSet;

    private final MinMaxValueSet minorGcTimeValueSet;

    private final MinMaxValueSet fullGcTimeValueSet;

    private final DoubleMinMaxValueSet totalCpuUsageValueSet;

    private int transactions;

    private int transactionErrors;

    /**
     * Creates a new {@link AbstractDataProcessor} instance.
     * 
     * @param name
     *            the agent name
     * @param provider
     *            the report provider owning this data processor
     */
    public AgentDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        setChartDir(new File(getChartDir(), "agents"));
        setCsvDir(new File(getCsvDir(), "agents"));

        final int minMaxValueSetSize = getChartWidth();

        cpuUsageValueSet = new DoubleMinMaxValueSet(minMaxValueSetSize);
        totalCpuUsageValueSet = new DoubleMinMaxValueSet(minMaxValueSetSize);
        gcCpuUsageValueSet = new DoubleMinMaxValueSet(minMaxValueSetSize);

        usedHeapValueSet = new MinMaxValueSet(minMaxValueSetSize);
        // usedMemValueSet = new MinMaxValueSet(minMaxValueSetSize);
        totalHeapValueSet = new MinMaxValueSet(minMaxValueSetSize);

        runnableThreadsValueSet = new MinMaxValueSet(minMaxValueSetSize);
        blockedThreadsValueSet = new MinMaxValueSet(minMaxValueSetSize);
        waitingThreadsValueSet = new MinMaxValueSet(minMaxValueSetSize);
        totalThreadsValueSet = new MinMaxValueSet(minMaxValueSetSize);

        minorGcTimeValueSet = new MinMaxValueSet(minMaxValueSetSize);
        fullGcTimeValueSet = new MinMaxValueSet(minMaxValueSetSize);
    }

    /**
     * Generates an agent report for the agent for which this data processor is responsible.
     * 
     * @return the agent report
     */
    public AgentReport createAgentReport()
    {
        final String name = getName();

        // create the charts
        if (((ReportGeneratorConfiguration) getConfiguration()).agentChartsEnabled())
        {
            final File agentChartDir = new File(getChartDir(), name);
            agentChartDir.mkdirs();

            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(() -> createMemoryUsageChart(name, agentChartDir));
            taskManager.addTask(() -> createCpuUsageChart(name, agentChartDir));
            taskManager.addTask(() -> createThreadsChart(name, agentChartDir));
        }

        // get statistics from value sets
        final DoubleSummaryStatistics cpuUsageStats = ReportUtils.toSummaryStatistics(cpuUsageValueSet);
        final DoubleSummaryStatistics totalCpuUsageStats = ReportUtils.toSummaryStatistics(totalCpuUsageValueSet);

        // create the agent report
        final AgentReport agentReport = new AgentReport();

        agentReport.name = name;

        agentReport.minorGcCount = minorGcCount;
        agentReport.minorGcTime = minorGcTime;
        agentReport.minorGcCpuUsage = ReportUtils.convertToBigDecimal(minorGcCpuUsageMean.getMean());

        agentReport.fullGcCount = fullGcCount;
        agentReport.fullGcTime = fullGcTime;
        agentReport.fullGcCpuUsage = ReportUtils.convertToBigDecimal(fullGcCpuUsageMean.getMean());

        agentReport.cpuUsage = createStatisticsReport(cpuUsageStats);
        agentReport.totalCpuUsage = createStatisticsReport(totalCpuUsageStats);

        agentReport.transactions = transactions;
        agentReport.transactionErrors = transactionErrors;

        return agentReport;
    }

    private DoubleStatisticsReport createStatisticsReport(final DoubleSummaryStatistics statistics)
    {
        final DoubleStatisticsReport statisticsReport = new DoubleStatisticsReport();

        statisticsReport.mean = ReportUtils.convertToBigDecimal(statistics.getMean());
        statisticsReport.min = ReportUtils.convertToBigDecimal(statistics.getMinimum());
        statisticsReport.max = ReportUtils.convertToBigDecimal(statistics.getMaximum());
        statisticsReport.deviation = ReportUtils.convertToBigDecimal(statistics.getStandardDeviation());

        return statisticsReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        final JvmResourceUsageData usageStats = (JvmResourceUsageData) stat;

        final long time = usageStats.getTime();

        // memory
        totalHeapValueSet.addOrUpdateValue(time, (int) (usageStats.getTotalHeapSize() / 1024 / 1024));
        usedHeapValueSet.addOrUpdateValue(time, (int) (usageStats.getUsedHeapSize() / 1024 / 1024));
        // usedMemValueSet.addOrUpdateValue(time, (int) (usageStats.getCommittedMemorySize() / 1024 / 1024));

        // CPU
        final double cpuUsage = usageStats.getCpuUsage();
        cpuUsageValueSet.addOrUpdateValue(time, cpuUsage);

        final double totalCpuUsage = usageStats.getTotalCpuUsage();
        totalCpuUsageValueSet.addOrUpdateValue(time, totalCpuUsage);

        // threads
        final int runnable = usageStats.getRunnableThreadCount();
        final int blocked = usageStats.getBlockedThreadCount();
        final int waiting = usageStats.getWaitingThreadCount();
        final int total = runnable + blocked + waiting;

        totalThreadsValueSet.addOrUpdateValue(time, total);
        waitingThreadsValueSet.addOrUpdateValue(time, waiting);
        blockedThreadsValueSet.addOrUpdateValue(time, blocked);
        runnableThreadsValueSet.addOrUpdateValue(time, runnable);

        // GC
        final double minorGcCpuUsage = usageStats.getMinorGcCpuUsage();
        final double fullGcCpuUsage = usageStats.getFullGcCpuUsage();

        gcCpuUsageValueSet.addOrUpdateValue(time, (minorGcCpuUsage + fullGcCpuUsage));
        minorGcCpuUsageMean.addValue(minorGcCpuUsage);
        fullGcCpuUsageMean.addValue(fullGcCpuUsage);
        minorGcTimeValueSet.addOrUpdateValue(time, (int) Math.round(usageStats.getMinorGcTimeDiff() /
                                                                    Math.max(1.0, usageStats.getMinorGcCountDiff())));
        fullGcTimeValueSet.addOrUpdateValue(time, (int) Math.round(usageStats.getFullGcTimeDiff() /
                                                                   Math.max(1.0, usageStats.getFullGcCountDiff())));

        minorGcCount = Math.max(minorGcCount, usageStats.getMinorGcCount());
        minorGcTime = Math.max(minorGcTime, usageStats.getMinorGcTime());
        fullGcCount = Math.max(fullGcCount, usageStats.getFullGcCount());
        fullGcTime = Math.max(fullGcTime, usageStats.getFullGcTime());
    }

    /**
     * Increments the transaction counter (and maybe the transaction error counter) of the agent this data processor is
     * responsible for.
     * 
     * @param failed
     *            whether the transaction to be added has failed
     */
    void incrementTransactionCounters(final boolean failed)
    {
        transactions++;

        if (failed)
        {
            transactionErrors++;
        }
    }

    /**
     * Creates a CPU usage chart with the given title and stores it to the passed directory.
     * 
     * @param agentName
     *            the agent name
     * @param outputDir
     *            the target directory
     */
    protected void createCpuUsageChart(final String agentName, final File outputDir)
    {
        // System.out.println("Creating CPU usage chart for agent '" + agentName + "'... ");

        // final long start = TimerUtils.getTime();

        final TimeSeriesCollection cpuTimeSeriesCollection = new TimeSeriesCollection();
        final TimeSeries cpuTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(cpuUsageValueSet, "Agent CPU Usage");
        final TimeSeries avgCpuTimeSeries = JFreeChartUtils.createMovingAverageTimeSeries(cpuTimeSeries, getMovingAveragePercentage());
        final TimeSeries gcCpuTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(gcCpuUsageValueSet, "Agent GC CPU Usage");
        final TimeSeries totalCpuUsageTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(totalCpuUsageValueSet, "Total CPU Usage");

        cpuTimeSeriesCollection.addSeries(avgCpuTimeSeries);
        cpuTimeSeriesCollection.addSeries(cpuTimeSeries);
        cpuTimeSeriesCollection.addSeries(gcCpuTimeSeries);
        cpuTimeSeriesCollection.addSeries(totalCpuUsageTimeSeries);

        final JFreeChart chart = JFreeChartUtils.createLineChart(agentName + " -- CPU Usage", "CPU Usage [%]", cpuTimeSeriesCollection,
                                                                 getStartTime(), getEndTime());
        JFreeChartUtils.saveChart(chart, "CpuUsage", outputDir, getChartWidth(), getChartHeight());

        // System.out.printf("OK (%,d ms)\n", TimerUtils.getTime() - start);
    }

    /**
     * Creates a memory chart from the given timer list and stores it to the passed directory.
     * 
     * @param name
     *            the chart title
     * @param outputDir
     *            the target directory
     */
    protected void createMemoryUsageChart(final String name, final File outputDir)
    {
        // System.out.println("Creating memory usage chart for agent '" + name + "'... ");

        // final long start = TimerUtils.getTime();

        final MinMaxTimeSeriesCollection memoryCollection = new MinMaxTimeSeriesCollection();

        final TimeSeries usedHeapSeries = JFreeChartUtils.toMinMaxTimeSeries(usedHeapValueSet, "Used Heap");
        final TimeSeries totalHeapSeries = JFreeChartUtils.toMinMaxTimeSeries(totalHeapValueSet, "Total Heap");
        // final TimeSeries usedMemSeries = JFreeChartUtils.toMinMaxTimeSeries(usedMemValueSet, "Used Physical Memory");

        final TimeSeries usedHeapAvgSeries = JFreeChartUtils.createMovingAverageTimeSeries(usedHeapSeries, getMovingAveragePercentage());

        memoryCollection.addSeries(usedHeapAvgSeries);
        memoryCollection.addSeries(usedHeapSeries);
        memoryCollection.addSeries(totalHeapSeries);
        // memoryCollection.addSeries(usedMemSeries);

        final TimeSeriesCollection fullGcCollection = new TimeSeriesCollection();
        fullGcCollection.addSeries(JFreeChartUtils.toMinMaxTimeSeries(fullGcTimeValueSet, "Full GC time"));

        final TimeSeriesCollection minorGcCollection = new TimeSeriesCollection();
        minorGcCollection.addSeries(JFreeChartUtils.toMinMaxTimeSeries(minorGcTimeValueSet, "Minor GC time"));

        final JFreeChart chart = JFreeChartUtils.createCombinedPlotChart(name + " -- Memory Usage and Garbage Collection Time",
                                                                         getStartTime(), getEndTime());
        JFreeChartUtils.addLinePlotToCombinedPlotChart(chart, "Memory Usage [MB]", memoryCollection);
        JFreeChartUtils.addLinePlotToCombinedPlotChart(chart, "Full GC Time [ms]", fullGcCollection);
        JFreeChartUtils.addLinePlotToCombinedPlotChart(chart, "Minor GC Time [ms]", minorGcCollection);

        JFreeChartUtils.saveChart(chart, "MemoryUsage", outputDir, getChartWidth(), (int) (getChartHeight() * 2.3));

        // System.out.printf("OK (%,d ms)\n", TimerUtils.getTime() - start);
    }

    /**
     * Creates a threads chart from the given timer list and stores it to the passed directory.
     * 
     * @param name
     *            the chart title
     * @param outputDir
     *            the target directory
     */
    protected void createThreadsChart(final String name, final File outputDir)
    {
        // System.out.println("Creating threads chart for agent '" + name + "'... ");

        // final long start = TimerUtils.getTime();

        final TimeSeries totalThreadsTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(totalThreadsValueSet, "Total Threads");
        final TimeSeries runnableThreadsTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(runnableThreadsValueSet, "Runnable Threads");
        final TimeSeries blockedThreadsTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(blockedThreadsValueSet, "Blocked Threads");
        final TimeSeries waitingThreadsTimeSeries = JFreeChartUtils.toMinMaxTimeSeries(waitingThreadsValueSet, "Waiting Threads");

        final MinMaxTimeSeriesCollection collection = new MinMaxTimeSeriesCollection();

        collection.addSeries(totalThreadsTimeSeries);
        collection.addSeries(runnableThreadsTimeSeries);
        collection.addSeries(blockedThreadsTimeSeries);
        collection.addSeries(waitingThreadsTimeSeries);

        final JFreeChart chart = JFreeChartUtils.createLineChart(name + " -- Threads", "Threads", collection, getStartTime(), getEndTime());
        JFreeChartUtils.saveChart(chart, "Threads", outputDir, getChartWidth(), getChartHeight());

        // System.out.printf("OK (%,d ms)\n", TimerUtils.getTime() - start);
    }
}
