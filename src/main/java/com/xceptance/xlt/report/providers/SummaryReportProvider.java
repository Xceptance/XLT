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

    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        final java.util.ArrayList<RequestData> requests = dataContainer.getRequests();
        int size = requests.size();
        for (int i = 0; i < size; i++) {
            requestDataProcessor.processDataRecord(requests.get(i));
        }

        final java.util.ArrayList<ActionData> actions = dataContainer.getActions();
        size = actions.size();
        for (int i = 0; i < size; i++) {
            actionDataProcessor.processDataRecord(actions.get(i));
        }

        final java.util.ArrayList<TransactionData> transactions = dataContainer.getTransactions();
        size = transactions.size();
        for (int i = 0; i < size; i++) {
            final TransactionData t = transactions.get(i);
            transactionDataProcessor.processDataRecord(t);
            agentDataProcessor.incrementTransactionCounters(t.hasFailed());
        }

        final java.util.ArrayList<EventData> events = dataContainer.getEvents();
        size = events.size();
        for (int i = 0; i < size; i++) {
            transactionDataProcessor.processDataRecord(events.get(i));
        }

        final java.util.ArrayList<PageLoadTimingData> timings = dataContainer.getPageLoadTimings();
        size = timings.size();
        for (int i = 0; i < size; i++) {
            pageLoadDataProcessor.processDataRecord(timings.get(i));
        }

        final java.util.ArrayList<CustomData> customTimers = dataContainer.getCustomTimers();
        size = customTimers.size();
        for (int i = 0; i < size; i++) {
            customTimerDataProcessor.processDataRecord(customTimers.get(i));
        }

        final java.util.ArrayList<com.xceptance.xlt.agent.JvmResourceUsageData> customData = dataContainer.getJvmResourceUsage();
        size = customData.size();
        for (int i = 0; i < size; i++) {
            agentDataProcessor.processDataRecord(customData.get(i));
        }
    }
}
