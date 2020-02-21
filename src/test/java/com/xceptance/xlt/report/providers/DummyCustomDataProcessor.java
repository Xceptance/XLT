package com.xceptance.xlt.report.providers;

import java.io.File;

/**
 * Overrides {@link #setChartDir(File)} and {@link #setCsvDir(File)} to avoid problems on instantiation. Makes all
 * private fields readable via reflection, uses a proxy for this purpose to avoid code duplication, see
 * {@link #getProxy()}.
 * 
 * @author Sebastian Oerding
 */
public class DummyCustomDataProcessor extends CustomDataProcessor
{
    private Proxy proxy;

    /**
     * @param name
     * @param provider
     */
    public <T extends AbstractDataProcessor> DummyCustomDataProcessor(final String name,
                                                                      final AbstractDataProcessorBasedReportProvider<T> provider)
    {
        super(name, provider);
        setProxy();
    }

    /**
     * Overwrites the default implementation such that only the chartsDir is set but the directory / file is not
     * created.
     */
    @Override
    public void setChartDir(final File chartsDir)
    {
        setProxy();
        proxy.setChartDir(chartsDir);
    }

    /**
     * Overwrites the default implementation such that only the csvDir is set but the directory / file is not created.
     */
    @Override
    public void setCsvDir(final File csvDir)
    {
        setProxy();
        proxy.setChartDir(csvDir);
    }

    /**
     * Gives access to the proxy which is used to avoid code duplication.
     * 
     * @return a proxy from which all values can be read
     */
    public Proxy getProxy()
    {
        return proxy;
    }

    /**
     * Sets the proxy with the current instance as data processor if it is <code>null</code>.
     */
    private void setProxy()
    {
        if (proxy == null)
        {
            proxy = new Proxy(this);
        }
    }
}
