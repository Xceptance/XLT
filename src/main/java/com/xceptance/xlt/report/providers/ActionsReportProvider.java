package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;

/**
 */
public class ActionsReportProvider extends BasicTimerReportProvider<ActionDataProcessor>
{
    /**
     * Constructor.
     */
    public ActionsReportProvider()
    {
        super(ActionDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ActionsReport report = new ActionsReport();

        report.actions = createTimerReports(false);

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof ActionData)
        {
            super.processDataRecord(data);
        }
    }
}
