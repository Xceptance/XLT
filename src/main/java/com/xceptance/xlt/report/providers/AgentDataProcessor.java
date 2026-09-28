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
import com.xceptance.xlt.report.util.IntMinMaxValueSet;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.MinMaxTimeSeriesCollection;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.TaskManager;

/**
 * The {@link AgentDataProcessor} is responsible to calculate statistic information for exactly one agent.
 */

public class AgentDataProcessor extends AbstractDataProcessor
{
    private final IntMinMaxValueSet blockedThreadsValueSet;

    private final DoubleMinMaxValueSet cpuUsageValueSet;

    private long fullGcCount;

    private final ArithmeticMean fullGcCpuUsageMean = new ArithmeticMean();

    private long fullGcTime;

    private final DoubleMinMaxValueSet gcCpuUsageValueSet;

    private long minorGcCount;

    private final ArithmeticMean minorGcCpuUsageMean = new ArithmeticMean();

    private long minorGcTime;

    private final IntMinMaxValueSet runnableThreadsValueSet;

    private final IntMinMaxValueSet totalHeapValueSet;

    private final IntMinMaxValueSet totalThreadsValueSet;

    private final IntMinMaxValueSet usedHeapValueSet;

    // private final MinMaxValueSet usedMemValueSet;

    private final IntMinMaxValueSet waitingThreadsValueSet;

    private final IntMinMaxValueSet minorGcTimeValueSet;

    private final IntMinMaxValueSet fullGcTimeValueSet;

    private final DoubleMinMaxValueSet totalCpuUsageValueSet;

    private int transactions;

    private int transactionErrors;

    /**
     * Flag indicating whether an explicit, full agent name (such as
     * 'Agent-ac0001_us-east1_00-35.227.95.149-8500' provided by {@link JvmResourceUsageData})
     * has been assigned to this processor.
     * <p>
     * When created initially during transaction processing or directory traversal, the processor
     * is keyed and named by the short agent ID (e.g., 'ac0001_us-east1_00'). Once JVM monitor data
     * is processed, the processor name is updated via {@link #setName(String)} to the full agent descriptor.
     * In a multi-threaded reporting architecture where transaction chunks and JVM monitor chunks are handled
     * by separate worker threads, this flag allows merge operations to safely propagate the full descriptive
     * name to the merged master processor.
     */
    private boolean hasExplicitName = false;

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

        usedHeapValueSet = new IntMinMaxValueSet(minMaxValueSetSize);
        // usedMemValueSet = new MinMaxValueSet(minMaxValueSetSize);
        totalHeapValueSet = new IntMinMaxValueSet(minMaxValueSetSize);

        runnableThreadsValueSet = new IntMinMaxValueSet(minMaxValueSetSize);
        blockedThreadsValueSet = new IntMinMaxValueSet(minMaxValueSetSize);
        waitingThreadsValueSet = new IntMinMaxValueSet(minMaxValueSetSize);
        totalThreadsValueSet = new IntMinMaxValueSet(minMaxValueSetSize);

        minorGcTimeValueSet = new IntMinMaxValueSet(minMaxValueSetSize);
        fullGcTimeValueSet = new IntMinMaxValueSet(minMaxValueSetSize);
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

        agentReport.transactionErrorPercentage = ReportUtils.calculatePercentage(transactionErrors, transactions);

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
     * Sets the display name of this agent processor.
     * <p>
     * In addition to updating the underlying name field, this records that an explicit name has been provided
     * (e.g., from {@link JvmResourceUsageData#getName()}). This distinction is essential for parallel report
     * generation: worker threads processing transaction records initialize their agent processors using only the
     * agent directory/ID (e.g., 'ac0001_us-east1_00'), whereas worker threads processing JVM monitor logs receive
     * the full agent name (e.g., 'Agent-ac0001_us-east1_00-35.227.95.149-8500'). Tracking explicit names ensures
     * that when thread-local processors are merged into the master report provider, the full descriptive name
     * is retained regardless of the merge order of the worker threads.
     *
     * @param name
     *            the agent name to set
     */
    @Override
    public void setName(final String name)
    {
        super.setName(name);
        this.hasExplicitName = true;
    }

    /**
     * Returns whether this processor has been assigned an explicit, full agent name.
     *
     * @return {@code true} if an explicit name was assigned via {@link #setName(String)}, {@code false} otherwise
     */
    public boolean hasExplicitName()
    {
        return hasExplicitName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final AbstractDataProcessor other)
    {
        if (other instanceof AgentDataProcessor)
        {
            merge((AgentDataProcessor) other);
        }
    }

    /**
     * Merges another {@link AgentDataProcessor} into this instance.
     * <p>
     * Merges all resource usage value sets, means, GC statistics, and transaction counters.
     * Furthermore, if the other processor has received an explicit full agent name (or has a canonical
     * 'Agent-' prefix) and this processor does not yet have one, the full name is propagated here.
     *
     * @param other
     *            the other processor to merge
     */
    public void merge(final AgentDataProcessor other)
    {
        if (other == null)
        {
            return;
        }

        /*
         * Propagate the full descriptive agent name across worker threads.
         *
         * In parallel report processing, different worker threads process different chunks of data. A thread
         * processing TransactionData only knows the short agent ID (e.g., 'ac0001_us-east1_00') from the directory
         * structure, whereas a thread processing JvmResourceUsageData receives the full agent name (e.g.,
         * 'Agent-ac0001_us-east1_00-35.227.95.149-8500').
         *
         * When merging processors across threads into the master provider:
         * 1. If 'this' processor only has the default short agent ID but 'other' has received the full explicit name
         *    (or starts with the canonical 'Agent-' prefix), adopt 'other's name.
         * 2. If 'this' already has the full explicit name, retain it.
         * 3. This guarantees that regardless of whether transaction-processing threads or JVM-processing threads
         *    are merged first, the master processor will always hold the complete agent name.
         */
        if (!this.hasExplicitName && other.hasExplicitName)
        {
            super.setName(other.getName());
            this.hasExplicitName = true;
        }
        else if (other.getName() != null && other.getName().startsWith("Agent-") &&
                 (getName() == null || !getName().startsWith("Agent-")))
        {
            super.setName(other.getName());
            this.hasExplicitName = true;
        }

        blockedThreadsValueSet.merge(other.blockedThreadsValueSet);
        cpuUsageValueSet.merge(other.cpuUsageValueSet);
        gcCpuUsageValueSet.merge(other.gcCpuUsageValueSet);
        runnableThreadsValueSet.merge(other.runnableThreadsValueSet);
        totalHeapValueSet.merge(other.totalHeapValueSet);
        totalThreadsValueSet.merge(other.totalThreadsValueSet);
        usedHeapValueSet.merge(other.usedHeapValueSet);
        waitingThreadsValueSet.merge(other.waitingThreadsValueSet);
        minorGcTimeValueSet.merge(other.minorGcTimeValueSet);
        fullGcTimeValueSet.merge(other.fullGcTimeValueSet);
        totalCpuUsageValueSet.merge(other.totalCpuUsageValueSet);

        fullGcCpuUsageMean.merge(other.fullGcCpuUsageMean);
        minorGcCpuUsageMean.merge(other.minorGcCpuUsageMean);

        minorGcCount = Math.max(minorGcCount, other.minorGcCount);
        minorGcTime = Math.max(minorGcTime, other.minorGcTime);
        fullGcCount = Math.max(fullGcCount, other.fullGcCount);
        fullGcTime = Math.max(fullGcTime, other.fullGcTime);

        transactions += other.transactions;
        transactionErrors += other.transactionErrors;
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
        final TimeSeries avgCpuTimeSeries = JFreeChartUtils.createMovingAverageTimeSeries(cpuTimeSeries, getCommonMovingAverageConfig());
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

        final TimeSeries usedHeapAvgSeries = JFreeChartUtils.createMovingAverageTimeSeries(usedHeapSeries, getCommonMovingAverageConfig());

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
