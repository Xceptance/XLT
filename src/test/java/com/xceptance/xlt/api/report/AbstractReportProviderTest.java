package com.xceptance.xlt.api.report;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.providers.ErrorsReportProvider;

/**
 * @author Sebastian Oerding
 */
public class AbstractReportProviderTest
{
    @Test
    public void testAbstractReportProvider()
    {
        final AbstractReportProvider arp = new ErrorsReportProvider();
        try
        {
            final ReportProviderConfiguration config = new ReportGeneratorConfiguration();
            arp.setConfiguration(config);
            Assert.assertSame("Configuration does not match", config, arp.getConfiguration());
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}
