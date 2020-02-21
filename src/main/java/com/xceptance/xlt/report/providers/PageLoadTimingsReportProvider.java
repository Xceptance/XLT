package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.PageLoadTimingData;

public class PageLoadTimingsReportProvider extends BasicTimerReportProvider<PageLoadTimingDataProcessor>
{
    /**
     * Constructor.
     */
    public PageLoadTimingsReportProvider()
    {
        super(PageLoadTimingDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final PageLoadTimingsReport report = new PageLoadTimingsReport();

        report.pageLoadTimings = createTimerReports(false);

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof PageLoadTimingData)
        {
            super.processDataRecord(data);
        }
    }
}
