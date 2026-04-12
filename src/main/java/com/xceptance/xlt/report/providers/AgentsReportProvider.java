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
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.TransactionData;

/**
 * 
 */
public class AgentsReportProvider extends AbstractDataProcessorBasedReportProvider<AgentDataProcessor>
{
    /**
     * Constructor.
     */
    public AgentsReportProvider()
    {
        super(AgentDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final AgentsReport report = new AgentsReport();

        for (final AgentDataProcessor processor : getProcessors())
        {
            final AgentReport agentReport = processor.createAgentReport();

            report.agents.add(agentReport);
        }

        return report;
    }

    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        final java.util.ArrayList<JvmResourceUsageData> customData = dataContainer.getJvmResourceUsage();
        int size = customData.size();
        for (int i = 0; i < size; i++)
        {
            final JvmResourceUsageData data = customData.get(i);
            
            final AgentDataProcessor processor = getProcessor(data.getAgentName());
            processor.processDataRecord(data);

            processor.setName(data.getName());
        }

        final java.util.ArrayList<TransactionData> transactions = dataContainer.getTransactions();
        size = transactions.size();
        for (int i = 0; i < size; i++)
        {
            final TransactionData transactionData = transactions.get(i);

            final AgentDataProcessor processor = getProcessor(transactionData.getAgentName());
            processor.incrementTransactionCounters(transactionData.hasFailed());
        }
    }
}
