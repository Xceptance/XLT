package com.xceptance.xlt.api.util;

import java.net.URI;
import java.net.URISyntaxException;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.RegExUtils;

/**
 * This class provides some convenient methods for link processing.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class URLUtils
{
    /**
     * Makes an URL absolute based on a base URL.
     * 
     * @param baseUrl
     *            the base URL
     * @param url
     *            the URL to modify
     * @return the result URL
     */
    public static String makeLinkAbsolute(final String baseUrl, final String url)
    {
        try
        {
            return makeLinkAbsolute(new URI(baseUrl), url);
        }
        // base URL is invalid -> return null
        catch (final URISyntaxException e1)
        {
            return null;
        }
    }

    /**
     * Makes a single link absolute by matching it to a base URI.
     * 
     * @param baseURI
     *            the base URI to use
     * @param url
     *            the link to verify and modify
     * @return the modified URL
     */

    public static String makeLinkAbsolute(final URI baseURI, final String url)
    {
        // parameter validation
        ParameterCheckUtils.isNotNull(baseURI, "baseURI");

        if (url == null)
        {
            return null;
        }

        // trim all whitespace and check again
        final String trimmedURL = url.trim();
        if (trimmedURL.length() == 0)
        {
            return url;
        }

        // check for JS and mail protocols
        if (RegExUtils.isMatching(trimmedURL.toLowerCase(), "^(javascript|mailto):"))
        {
            return null;
        }

        // quote URL
        final String quotedURL = trimmedURL.replace(' ', '+');

        // validate quoted URL and resolve it
        try
        {
            // fix an empty path
            final URI fixedBaseURI = fixPath(baseURI);

            // finally create the new absolute URI
            return fixedBaseURI.resolve(quotedURL).normalize().toASCIIString();
        }
        catch (final Exception e)
        {
            // something went wrong -> print error message and return null
            final String errMsg = String.format("Cannot make link '%s' absolute using base URL '%s'.", url, baseURI.toASCIIString());
            XltLogger.runTimeLogger.error(errMsg);

            return null;
        }
    }

    /**
     * Adds a "/" as the default path if no path is present in the given URI.
     * 
     * @param uri
     *            the URI to fix
     * @return the fixed URI
     * @throws URISyntaxException
     *             if the new URI could not be created
     */
    private static URI fixPath(final URI uri) throws URISyntaxException
    {
        final String path = uri.getPath();

        if (path == null || path.length() == 0)
        {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), "/", uri.getQuery(), uri.getFragment());
        }
        else
        {
            return uri;
        }
    }
}
