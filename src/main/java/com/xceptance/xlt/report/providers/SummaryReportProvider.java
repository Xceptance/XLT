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

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;

/**
 */
import java.util.Date;

import com.xceptance.xlt.report.util.IntTimeSeries;
import com.xceptance.xlt.report.util.ReportUtils;
import com.xceptance.xlt.report.util.TimeSeriesDownsampler;

public class SummaryReportProvider extends AbstractReportProvider
{
    private TransactionDataProcessor transactionDataProcessor;

    private ActionDataProcessor actionDataProcessor;

    private RequestDataProcessor requestDataProcessor;

    private PageLoadTimingDataProcessor pageLoadDataProcessor;

    private CustomDataProcessor customTimerDataProcessor;

    private AgentDataProcessor agentDataProcessor;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        // HACK: must not create the data processors before the configuration is set
        transactionDataProcessor = new TransactionDataProcessor("All Transactions", this);
        actionDataProcessor = new ActionDataProcessor("All Actions", this);
        requestDataProcessor = new RequestDataProcessor("All Requests", this, false);
        pageLoadDataProcessor = new PageLoadTimingDataProcessor("All Page Load Timings", this);
        customTimerDataProcessor = new CustomDataProcessor("All Custom Timers", this);
        agentDataProcessor = new AgentDataProcessor("All Agents", this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final SummaryReport report = new SummaryReport();

        report.transactions = (TransactionReport) transactionDataProcessor.createTimerReport(false);
        report.actions = (ActionReport) actionDataProcessor.createTimerReport(false);
        report.requests = (RequestReport) requestDataProcessor.createTimerReport(true);
        report.pageLoadTimings = (PageLoadTimingReport) pageLoadDataProcessor.createTimerReport(false);
        report.customTimers = (CustomTimerReport) customTimerDataProcessor.createTimerReport(false);
        report.agents = (AgentReport) agentDataProcessor.createAgentReport();
        report.timeSeries = createTimeSeries();

        return report;
    }

    /**
     * Builds the coarse time series that the AI data export uses.
     * <p>
     * Deliberately independent of chart generation. The chart JSON holds the same shape of data, but its resolution is
     * the chart width in pixels, and it is not written at all when charts are switched off. Neither should decide what
     * an analysis file contains.
     *
     * @return the time series, or <code>null</code> when the run produced no data to build one from
     */
    private TimeSeriesReport createTimeSeries()
    {
        final long startTime = getConfiguration().getChartStartTime();
        final long endTime = getConfiguration().getChartEndTime();
        final long durationSeconds = (endTime - startTime) / 1000;

        if (durationSeconds <= 0)
        {
            return null;
        }

        final IntTimeSeries transactionTimeSeries = transactionDataProcessor.getTimeSeries();
        final IntTimeSeries actionTimeSeries = actionDataProcessor.getTimeSeries();
        final IntTimeSeries requestTimeSeries = requestDataProcessor.getTimeSeries();

        // the values were bucketed as they were collected, and we cannot be finer than that
        final int sourceResolution = Math.max(1, Math.max(transactionTimeSeries.getSlotWidth(),
                                                          Math.max(actionTimeSeries.getSlotWidth(), requestTimeSeries.getSlotWidth())));

        final int interval = TimeSeriesDownsampler.selectInterval(durationSeconds, sourceResolution);
        final int buckets = (int) ((durationSeconds + interval - 1) / interval);
        final long firstSecond = startTime / 1000;

        final int[] transactionMeans = TimeSeriesDownsampler.meanPerBucket(transactionTimeSeries, firstSecond, buckets, interval);
        final double[] transactionRates = TimeSeriesDownsampler.countRatePerBucket(transactionTimeSeries,
                                                                              firstSecond, buckets, interval);
        final double[] transactionErrorRates = TimeSeriesDownsampler.errorRatePerBucket(transactionTimeSeries,
                                                                                   firstSecond, buckets, interval);

        final int[] actionMeans = TimeSeriesDownsampler.meanPerBucket(actionTimeSeries, firstSecond, buckets, interval);
        final double[] actionRates = TimeSeriesDownsampler.countRatePerBucket(actionTimeSeries,
                                                                          firstSecond, buckets, interval);
        final double[] actionErrorRates = TimeSeriesDownsampler.errorRatePerBucket(actionTimeSeries,
                                                                          firstSecond, buckets, interval);
        
        final int[] requestMeans = TimeSeriesDownsampler.meanPerBucket(requestTimeSeries, firstSecond, buckets, interval);
        final double[] requestRates = TimeSeriesDownsampler.countRatePerBucket(requestTimeSeries,
                                                                          firstSecond, buckets, interval);
        final double[] requestErrorRates = TimeSeriesDownsampler.errorRatePerBucket(requestTimeSeries,
                                                                          firstSecond, buckets, interval);

        final TimeSeriesReport series = new TimeSeriesReport();
        series.interval = interval;
        series.buckets = buckets;
        series.sourceResolution = sourceResolution;

        for (int i = 0; i < buckets; i++)
        {
            final TimeSeriesRowReport row = new TimeSeriesRowReport();

            row.elapsed = (long) i * interval;
            row.time = new Date(startTime + row.elapsed * 1000);

            row.transactionMean = transactionMeans[i];
            row.transactionCountPerSecond = ReportUtils.convertToBigDecimal(transactionRates[i]);
            row.transactionErrorsPerSecond = ReportUtils.convertToBigDecimal(transactionErrorRates[i]);

            row.actionMean = actionMeans[i];
            row.actionCountPerSecond = ReportUtils.convertToBigDecimal(actionRates[i]);
            row.actionErrorsPerSecond = ReportUtils.convertToBigDecimal(actionErrorRates[i]);

            row.requestMean = requestMeans[i];
            row.requestCountPerSecond = ReportUtils.convertToBigDecimal(requestRates[i]);
            row.requestErrorsPerSecond = ReportUtils.convertToBigDecimal(requestErrorRates[i]);

            series.rows.add(row);
        }

        return series;
    }

    /**
     * High-performance batch record processing override for summary metrics.
     * Uses the chunk container's declared typeCode ('R', 'A', 'T', etc.) to dispatch directly
     * into a specialized tight loop without per-record {@code instanceof} checks.
     *
     * @param dataContainer
     *            the container holding post-processed records for this chunk
     */
    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        final com.xceptance.xlt.api.util.SimpleArrayList<Data> list = dataContainer.dataList;
        final Object[] array = list.getInternalArray();
        final int size = list.size();

        // Fast path: homogeneous chunk of HTTP requests ('R')
        if (dataContainer.typeCode == 'R')
        {
            final RequestDataProcessor rdp = this.requestDataProcessor;
            for (int p = 0; p < size; p++)
            {
                rdp.processDataRecord((RequestData) array[p]);
            }
            return;
        }

        // Fast path: homogeneous chunk of Actions ('A')
        if (dataContainer.typeCode == 'A')
        {
            final ActionDataProcessor adp = this.actionDataProcessor;
            for (int p = 0; p < size; p++)
            {
                adp.processDataRecord((Data) array[p]);
            }
            return;
        }

        // Fast path: homogeneous chunk of Transactions ('T')
        if (dataContainer.typeCode == 'T')
        {
            final TransactionDataProcessor tdp = this.transactionDataProcessor;
            final AgentDataProcessor agdp = this.agentDataProcessor;
            for (int p = 0; p < size; p++)
            {
                final TransactionData td = (TransactionData) array[p];
                tdp.processDataRecord(td);
                agdp.incrementTransactionCounters(td.hasFailed());
            }
            return;
        }

        // Fallback for mixed or other data record types
        for (int p = 0; p < size; p++)
        {
            processDataRecord((Data) array[p]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            requestDataProcessor.processDataRecord(data);
        }
        else if (data instanceof ActionData)
        {
            actionDataProcessor.processDataRecord(data);
        }
        else if (data instanceof TransactionData)
        {
            transactionDataProcessor.processDataRecord(data);
            agentDataProcessor.incrementTransactionCounters(((TransactionData) data).hasFailed());
        }
        else if (data instanceof EventData)
        {
            transactionDataProcessor.processDataRecord(data);
        }
        else if (data instanceof PageLoadTimingData)
        {
            pageLoadDataProcessor.processDataRecord(data);
        }
        else if (data instanceof CustomData)
        {
            customTimerDataProcessor.processDataRecord(data);
        }
        else if (data instanceof JvmResourceUsageData)
        {
            agentDataProcessor.processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final ReportProvider other)
    {
        if (other instanceof SummaryReportProvider)
        {
            merge((SummaryReportProvider) other);
        }
    }

    /**
     * Merges another {@link SummaryReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final SummaryReportProvider other)
    {
        if (other == null)
        {
            return;
        }

        if (transactionDataProcessor != null && other.transactionDataProcessor != null)
        {
            transactionDataProcessor.merge(other.transactionDataProcessor);
        }
        if (actionDataProcessor != null && other.actionDataProcessor != null)
        {
            actionDataProcessor.merge(other.actionDataProcessor);
        }
        if (requestDataProcessor != null && other.requestDataProcessor != null)
        {
            requestDataProcessor.merge(other.requestDataProcessor);
        }
        if (pageLoadDataProcessor != null && other.pageLoadDataProcessor != null)
        {
            pageLoadDataProcessor.merge(other.pageLoadDataProcessor);
        }
        if (customTimerDataProcessor != null && other.customTimerDataProcessor != null)
        {
            customTimerDataProcessor.merge(other.customTimerDataProcessor);
        }
        if (agentDataProcessor != null && other.agentDataProcessor != null)
        {
            agentDataProcessor.merge(other.agentDataProcessor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        // Summary report provider aggregates summary timers across R, A, T, E, P, C, and J
        return typeCode == 'R' || typeCode == 'A' || typeCode == 'T' || typeCode == 'E' ||
               typeCode == 'P' || typeCode == 'C' || typeCode == 'J';
    }
}

