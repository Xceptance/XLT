package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.Data;

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
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        if (stat instanceof JvmResourceUsageData)
        {
            super.processDataRecord(stat);
        }
    }
}
