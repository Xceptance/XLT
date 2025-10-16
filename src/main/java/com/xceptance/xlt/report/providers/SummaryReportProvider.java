/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
