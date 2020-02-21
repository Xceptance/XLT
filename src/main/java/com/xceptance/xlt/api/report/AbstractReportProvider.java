package com.xceptance.xlt.api.report;

/**
 * The {@link AbstractReportProvider} class provides common functionality of a typical report provider.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractReportProvider implements ReportProvider
{
    /**
     * The report provider's configuration.
     */
    private ReportProviderConfiguration configuration;

    /**
     * Returns the report provider's configuration. Use the configuration object to get access to general as well as
     * provider-specific properties stored in the global configuration file.
     * 
     * @return the report provider configuration
     */
    public ReportProviderConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ReportProviderConfiguration config)
    {
        configuration = config;
    }
}
