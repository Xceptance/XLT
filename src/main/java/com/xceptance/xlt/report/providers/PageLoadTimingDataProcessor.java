package com.xceptance.xlt.report.providers;

import java.io.File;

import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * The {@link PageLoadTimingDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link PageLoadTimingData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * </ul>
 */
public class PageLoadTimingDataProcessor extends BasicTimerDataProcessor
{
    /**
     * Constructor.
     * 
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public <T extends AbstractDataProcessor> PageLoadTimingDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        setChartDir(new File(getChartDir(), "pageLoadTimings"));
        setCsvDir(new File(getCsvDir(), "pageLoadTimings"));

        // set capping parameters
        final ReportGeneratorConfiguration config = (ReportGeneratorConfiguration) getConfiguration();
        setChartCappingInfo(config.getCustomChartCappingInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerReport createTimerReport()
    {
        return new PageLoadTimingReport();
    }
}
