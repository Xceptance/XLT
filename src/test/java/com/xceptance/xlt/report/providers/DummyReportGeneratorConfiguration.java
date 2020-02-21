package com.xceptance.xlt.report.providers;

import java.io.IOException;

import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Convenience implementation for testing purposes. Overrides {@link #getChartWidth()}.
 * 
 * @author Sebastian Oerding
 */
public class DummyReportGeneratorConfiguration extends ReportGeneratorConfiguration
{
    /**
     * @throws IOException
     */
    public DummyReportGeneratorConfiguration() throws IOException
    {
        super();
    }

    /**
     * Convenience method to get a report generator configuration without the need to catch an exception that would
     * never happen.
     */
    public static DummyReportGeneratorConfiguration getDefault()
    {
        try
        {
            return new DummyReportGeneratorConfiguration();
        }
        catch (final IOException e)
        {
            // this can't happen as long as the super class does not change but the compiler does not know.
            throw new RuntimeException(e);
        }
    }

    /**
     * @return 1 to avoid initialization errors
     */
    @Override
    public int getChartWidth()
    {
        return 1;
    }
}
