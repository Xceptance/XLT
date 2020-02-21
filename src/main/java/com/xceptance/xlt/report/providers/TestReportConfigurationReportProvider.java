package com.xceptance.xlt.report.providers;

import java.util.ArrayList;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.ReportUtils;

/**
 *
 */
public class TestReportConfigurationReportProvider extends AbstractReportProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final TestReportConfigurationReport configReport = new TestReportConfigurationReport();

        // set the configured percentiles
        configReport.runtimePercentiles = new ArrayList<>();

        final double[] percentiles = ((ReportGeneratorConfiguration) getConfiguration()).getRuntimePercentiles();
        for (final double percentile : percentiles)
        {
            configReport.runtimePercentiles.add(ReportUtils.formatValue(percentile));
        }

        // add the configured runtime intervals used to segment the value range
        // (preprocessed for easier XSL transformation)
        final int[] boundaries = ((ReportGeneratorConfiguration) getConfiguration()).getRuntimeIntervalBoundaries();
        final ArrayList<RuntimeInterval> intervals = new ArrayList<RuntimeInterval>();

        if (boundaries.length > 0)
        {
            int lowerBoundary = 0;
            for (final int boundary : boundaries)
            {
                intervals.add(new RuntimeInterval("" + lowerBoundary, "" + boundary));
                lowerBoundary = boundary;
            }

            intervals.add(new RuntimeInterval("" + lowerBoundary, ""));
        }
        configReport.runtimeIntervals = intervals;
        
        configReport.requestTableColorization =  ((ReportGeneratorConfiguration) getConfiguration()).getRequestTableColorizations();

        //
        return configReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        // nothing to do here
    }
}
