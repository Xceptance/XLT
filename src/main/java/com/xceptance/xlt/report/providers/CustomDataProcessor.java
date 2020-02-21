package com.xceptance.xlt.report.providers;

import java.io.File;

import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * The {@link CustomDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link CustomData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * <li>creation of CSV files with all the data</li>
 * </ul>
 */
public class CustomDataProcessor extends BasicTimerDataProcessor
{
    /**
     * Constructor.
     * 
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public <T extends AbstractDataProcessor> CustomDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        setChartDir(new File(getChartDir(), "custom"));
        setCsvDir(new File(getCsvDir(), "custom"));

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
        return new CustomTimerReport();
    }
}
