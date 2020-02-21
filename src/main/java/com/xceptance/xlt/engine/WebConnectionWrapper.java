package com.xceptance.xlt.engine;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * A {@link WebConnection} that wraps another web connection and delegates all the hard work to it.
 */
public class WebConnectionWrapper implements WebConnection
{
    /**
     * The wrapped web connection.
     */
    private final WebConnection wrappedWebConnection;

    /**
     * Creates a new {@link WebConnectionWrapper} instance.
     * 
     * @param webConnection
     *            the web connection to be wrapped
     */
    public WebConnectionWrapper(final WebConnection webConnection)
    {
        this.wrappedWebConnection = webConnection;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
        wrappedWebConnection.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(final WebRequest webRequest) throws IOException
    {
        return wrappedWebConnection.getResponse(webRequest);
    }

    /**
     * Returns the wrapped {@link WebConnection} object.
     * 
     * @return the wrapped web connection
     */
    public WebConnection getWrappedWebConnection()
    {
        return wrappedWebConnection;
    }
}
