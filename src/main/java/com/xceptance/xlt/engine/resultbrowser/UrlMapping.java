package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;

/**
 * URL mapping.
 *
 * @author hardy (Xceptance Software Technologies GmbH)
 */
public interface UrlMapping
{
    /**
     * Maps the given URL to a new one and returns it as string.
     *
     * @param url
     *            URL to be mapped
     * @return mapped URL as string
     */
    String map(final URL url);

    /**
     * Maps the given URL string to a new one.
     *
     * @param urlString
     *            URL string to be mapped
     * @return mapped URL string
     */
    String map(final String urlString);

}
