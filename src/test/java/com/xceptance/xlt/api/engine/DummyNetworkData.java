package com.xceptance.xlt.api.engine;

import java.net.MalformedURLException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * This class provides a dummy implementation of {@link CustomData} but makes {@link #parseValues(String[])} public to
 * allow modifications for testing purposes.
 * <p>
 * The class provides the convenience method {@link #getDefault()} which gives a new instance of this class for each
 * invocation.
 * </p>
 * 
 * @author Sebastian Oerding
 */
public class DummyNetworkData extends NetworkData
{
    /**
     * Instantiates a new instance with a request and no response. The request URL points to
     * &quot;http://localhost&quot;.
     */
    public DummyNetworkData()
    {
        super(new WebRequest(getLocalHostUrl()), null);
    }

    private static URL getLocalHostUrl()
    {
        try
        {
            return new URL("http://localhost");
        }
        catch (final MalformedURLException e)
        {
            // ignore as this won't happen
            throw new RuntimeException(e);
        }
    }
}
