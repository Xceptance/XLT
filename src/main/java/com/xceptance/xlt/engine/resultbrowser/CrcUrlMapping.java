package com.xceptance.xlt.engine.resultbrowser;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.util.UrlUtils;

/**
 * URL mapping used for URL rewriting.
 */
final class CrcUrlMapping implements UrlMapping
{
    /**
     * {@inheritDoc}
     */
    @Override
    public String map(URL url)
    {
        // remove port from URL if it is the default port
        final int port = url.getPort();
        if (port != -1 && port == url.getDefaultPort())
        {
            try
            {
                url = UrlUtils.getUrlWithNewPort(url, -1);
            }
            catch (final MalformedURLException e)
            {
                return null;
            }
        }

        return com.xceptance.common.lang.StringUtils.crc32(UrlUtils.encodeUrl(url, true, StandardCharsets.UTF_8).toString()) +
               getFileSuffix(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String map(final String urlString)
    {
        try
        {
            return map(new URL(urlString));
        }
        catch (final MalformedURLException mue)
        {
            return null;
        }
    }

    /**
     * Returns the file suffix of the given URL.
     *
     * @param url
     *            the URL
     * @return file suffix of given URL prefixed by a dot or an empty string of no file suffix exists
     */
    private String getFileSuffix(final URL url)
    {
        final String path = StringUtils.substringBefore(url.getPath(), ";");
        String suffix = StringUtils.substringAfterLast(path, ".");
        if (suffix == null || suffix.indexOf('/') != -1)
        {
            suffix = StringUtils.EMPTY;
        }

        if (suffix.length() > 0)
        {
            suffix = "." + suffix;
        }

        return suffix;
    }
}
