package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;

/**
 * 
 */
public class CustomTimersReportProvider extends BasicTimerReportProvider<CustomDataProcessor>
{
    /**
     * Constructor.
     */
    public CustomTimersReportProvider()
    {
        super(CustomDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomTimersReport report = new CustomTimersReport();

        report.customTimers = createTimerReports(false);

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof CustomData)
        {
            super.processDataRecord(data);
        }
    }
}
