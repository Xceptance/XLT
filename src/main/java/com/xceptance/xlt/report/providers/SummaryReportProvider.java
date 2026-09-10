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
import com.xceptance.xlt.api.report.ReportProviderConfiguration;

/**
 */
import java.util.Date;

import com.xceptance.xlt.report.util.IntMinMaxValueSet;
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

        final IntMinMaxValueSet transactionRunTimes = transactionDataProcessor.getRunTimeValueSet();
        final IntMinMaxValueSet actionRunTimes = actionDataProcessor.getRunTimeValueSet();
        final IntMinMaxValueSet requestRunTimes = requestDataProcessor.getRunTimeValueSet();

        // the values were bucketed as they were collected, and we cannot be finer than that
        final int sourceResolution = Math.max(1, Math.max(transactionRunTimes.getScale(),
                                                          Math.max(actionRunTimes.getScale(), requestRunTimes.getScale())));

        final int interval = TimeSeriesDownsampler.selectInterval(durationSeconds, sourceResolution);
        final int buckets = (int) ((durationSeconds + interval - 1) / interval);
        final long firstSecond = startTime / 1000;

        final int[] transactionMeans = TimeSeriesDownsampler.meanPerBucket(transactionRunTimes, firstSecond, buckets, interval);
        final int[] actionMeans = TimeSeriesDownsampler.meanPerBucket(actionRunTimes, firstSecond, buckets, interval);
        final int[] requestMeans = TimeSeriesDownsampler.meanPerBucket(requestRunTimes, firstSecond, buckets, interval);

        final double[] transactionRates = TimeSeriesDownsampler.ratePerBucket(transactionDataProcessor.getCountPerSecondValueSet(),
                                                                             firstSecond, buckets, interval);
        final double[] transactionErrorRates = TimeSeriesDownsampler.ratePerBucket(transactionDataProcessor.getErrorsPerSecondValueSet(),
                                                                                   firstSecond, buckets, interval);
        final double[] requestRates = TimeSeriesDownsampler.ratePerBucket(requestDataProcessor.getCountPerSecondValueSet(),
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
            row.requestMean = requestMeans[i];
            row.requestCountPerSecond = ReportUtils.convertToBigDecimal(requestRates[i]);

            series.rows.add(row);
        }

        return series;
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
}
