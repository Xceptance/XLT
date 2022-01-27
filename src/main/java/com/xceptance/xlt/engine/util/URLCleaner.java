package com.xceptance.xlt.engine.util;

import java.net.MalformedURLException;
import java.net.URL;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.engine.SessionImpl;

public final class URLCleaner
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private URLCleaner()
    {
    }

    public static URL removeUserInfoIfNecessaryAsURL(final URL url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if (SessionImpl.REMOVE_USERINFO_FROM_REQUEST_URL)
        {
            try
            {
                return UrlUtils.getURLWithoutUserInfo(url);
            }
            catch (final MalformedURLException mue)
            {
                XltLogger.runTimeLogger.error("Failed to remove user-info from URL '{}'", url, mue);
            }
        }

        // return URL as is
        return url;
    }

    public static String removeUserInfoIfNecessaryAsString(final URL url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if (SessionImpl.REMOVE_USERINFO_FROM_REQUEST_URL)
        {
            return UrlUtils.removeUserInfo(url);
        }

        // return URL as string
        return url.toExternalForm();
    }

    public static String removeUserInfoIfNecessaryAsString(final String url)
    {
        // remove user-info from request URL if we need to (GH #57)
        if (SessionImpl.REMOVE_USERINFO_FROM_REQUEST_URL)
        {
            return UrlUtils.removeUserInfo(url);
        }

        // return URL string as is
        return url;
    }

}
