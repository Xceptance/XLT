package com.xceptance.xlt.report.providers;

import java.io.File;
import java.util.Properties;

import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;

/**
 * This is a dummy implementation of {@link ReportProviderConfiguration} which is required to instantiate
 * {@link ReportProvider}s (which are required to instantiate {@link AbstractDataProcessor}s).
 * <p>
 * Currently it only returns the default values (<code>null</code> for objects, 0 for numbers and <code>false</code> for
 * booleans). Hence this has to be adjusted if required. In difference to the default values it returns
 * <ul>
 * <li>1 for the chart width</li>
 * </ul>
 * to avoid runtime errors.
 * </p>
 * 
 * @author Sebastian Oerding
 */
class DummyReportProviderConfiguration implements ReportProviderConfiguration
{
    DummyReportProviderConfiguration()
    {
    }

    @Override
    public File getChartDirectory()
    {
        return null;
    }

    @Override
    public long getChartEndTime()
    {
        return 0;
    }

    @Override
    public int getChartHeight()
    {
        return 0;
    }

    @Override
    public long getChartStartTime()
    {
        return 0;
    }

    @Override
    public int getChartWidth()
    {
        return 1;
    }

    @Override
    public boolean shouldChartsGenerated()
    {
        return false;
    }

    @Override
    public File getCsvDirectory()
    {
        return null;
    }

    @Override
    public int getMovingAveragePercentage()
    {
        return 0;
    }

    @Override
    public Properties getProperties()
    {
        return null;
    }

    @Override
    public File getReportDirectory()
    {
        return null;
    }
}
