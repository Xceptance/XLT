package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomValuesReportProvider extends AbstractDataProcessorBasedReportProvider<CustomValueProcessor>
{
    /**
     * Constructor.
     */
    public CustomValuesReportProvider()
    {
        super(CustomValueProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof CustomValue)
        {
            super.processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomValueReports reports = new CustomValueReports();

        for (final CustomValueProcessor processor : getProcessors())
        {
            final CustomValueReport customValueReport = processor.createReportFragment();

            reports.customValueReports.add(customValueReport);
        }

        return reports;
    }
}
