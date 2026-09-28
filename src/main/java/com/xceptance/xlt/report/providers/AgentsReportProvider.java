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

    /**
     * High-performance batch record processing override for agent metrics.
     * Accurately routes {@link JvmResourceUsageData} and {@link TransactionData}
     * using agent ID lookups and appropriate processor methods.
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

        if (dataContainer.typeCode == 'T')
        {
            /*
             * Specialized tight-loop dispatch for TransactionData ('T') records.
             * Directly extracts agent name, retrieves or creates the thread-local
             * AgentDataProcessor, and increments transaction/failure counters without
             * polymorphic virtual method overhead.
             */
            for (int p = 0; p < size; p++)
            {
                final TransactionData transactionData = (TransactionData) array[p];
                final AgentDataProcessor processor = getProcessor(transactionData.getAgentName());
                processor.incrementTransactionCounters(transactionData.hasFailed());
            }
            return;
        }

        if (dataContainer.typeCode == 'J')
        {
            /*
             * Specialized tight-loop dispatch for JvmResourceUsageData ('J') records.
             * Processes JVM resource metrics and ensures the processor's display name
             * is updated to the full agent descriptor (e.g. 'Agent-ac0001_us-east1_00-...').
             */
            for (int p = 0; p < size; p++)
            {
                final JvmResourceUsageData jvmData = (JvmResourceUsageData) array[p];
                final AgentDataProcessor processor = getProcessor(jvmData.getAgentName());
                processor.processDataRecord(jvmData);
                processor.setName(jvmData.getName());
            }
            return;
        }

        /*
         * Fallback for heterogeneous or unspecified chunk containers: process record by record.
         */
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
        /*
         * An agent processor bundles all data gathered for a certain agent, now also certain transaction data. When
         * looking up the responsible agent processor for a Data object, we can no longer use the full agent name (e.g.
         * 'Agent-ac0001_us-east1_00-34.138.16.104-8500') as this information is available for JvmResourceUsageData
         * objects only. Instead, we now use the agent ID (e.g. 'ac0001_us-east1'), as all types of Data objects carry
         * this information. This approach is different from other report providers that are based on
         * AbstractDataProcessorBasedReportProvider.
         */

        if (data instanceof JvmResourceUsageData)
        {
            final AgentDataProcessor processor = getProcessor(data.getAgentName());
            processor.processDataRecord(data);

            /*
             * If we use the agent ID to reference the agent processor, the agent would be named as such in the report
             * as well. Since we want the full agent name in the report, we have to "fix" the initial name by setting
             * the full name later on.
             */
            processor.setName(data.getName());
        }
        else if (data instanceof TransactionData)
        {
            final TransactionData transactionData = (TransactionData) data;

            final AgentDataProcessor processor = getProcessor(data.getAgentName());
            processor.incrementTransactionCounters(transactionData.hasFailed());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        // Agents report provider aggregates JVM usage metrics ('J') and Transaction counts ('T')
        return typeCode == 'J' || typeCode == 'T';
    }
}

