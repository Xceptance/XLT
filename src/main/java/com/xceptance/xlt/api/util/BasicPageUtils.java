package com.xceptance.xlt.api.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.WebClient;
import com.xceptance.common.util.ParameterCheckUtils;

/**
 * The {@link BasicPageUtils} class provides common helper methods for its child classes. It is not meant to be used
 * directly.
 * 
 * @see HtmlPageUtils
 * @see LightweightHtmlPageUtils
 */
public class BasicPageUtils
{
    /**
     * Returns an absolute URL built from the passed base URL and relative path.
     * 
     * @param baseUrl
     *            the base URL as a string
     * @param relativePath
     *            the relative path
     * @return the new absolute URL
     * @throws MalformedURLException
     *             if the new URL is invalid
     */
    public static String getAbsoluteUrl(final String baseUrl, final String relativePath) throws MalformedURLException
    {
        ParameterCheckUtils.isNotNullOrEmpty(baseUrl, "baseUrl");

        return getAbsoluteUrl(new URL(baseUrl), relativePath).toString();
    }

    /**
     * Returns an absolute URL built from the passed base URL and relative path.
     * 
     * @param baseUrl
     *            the base URL
     * @param relativePath
     *            the relative path
     * @return the new absolute URL
     * @throws MalformedURLException
     *             if the new URL is invalid
     */
    public static String getAbsoluteUrl(final URL baseUrl, final String relativePath) throws MalformedURLException
    {
        ParameterCheckUtils.isNotNull(baseUrl, "baseUrl");
        ParameterCheckUtils.isNotNullOrEmpty(relativePath, "relativePath");

        return WebClient.expandUrl(baseUrl, relativePath).toString();
    }

    /**
     * Returns one entry from the passed list, chosen randomly.
     * 
     * @param <T>
     *            the type of the elements in the list
     * @param elements
     *            the list
     * @return one element from the list
     */
    public static <T> T pickOneRandomly(final List<T> elements)
    {
        return pickOneRandomly(elements, false, false);
    }

    /**
     * Returns one entry from the passed list, chosen randomly. It can be specified whether or not the first entry is to
     * be ignored.
     * 
     * @param <T>
     *            the type of the elements in the list
     * @param elements
     *            the list
     * @param excludeFirst
     *            whether or not the first entry is to be excluded
     * @return one element from the list
     */
    public static <T> T pickOneRandomly(final List<T> elements, final boolean excludeFirst)
    {
        return pickOneRandomly(elements, excludeFirst, false);
    }

    /**
     * Returns one entry from the passed list, chosen randomly. It can be specified whether or not the first and/or the
     * last entries are to be ignored.
     * 
     * @param <T>
     *            the type of the elements in the list
     * @param elements
     *            the list
     * @param excludeFirst
     *            whether or not the first entry is to be excluded
     * @param excludeLast
     *            whether or not the last entry is to be excluded
     * @return one element from the list
     */
    public static <T> T pickOneRandomly(final List<T> elements, final boolean excludeFirst, final boolean excludeLast)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(elements, "elements");

        // check whether there are enough items in the list
        int size = elements.size();

        if (excludeFirst)
        {
            size--;
        }

        if (excludeLast)
        {
            size--;
        }

        Assert.assertTrue("List does not contain enough items.", size > 0);

        // pick one item randomly
        int ran = XltRandom.nextInt(size);

        if (excludeFirst)
        {
            ran++;
        }

        return elements.get(ran);
    }
}
