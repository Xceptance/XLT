/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.WebResponse;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.engine.util.LWPageUtilities;

/**
 * A simple page object for light-weight operations.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class LightWeightPageImpl extends LightWeightPage
{
    /**
     * The base URL of this page. Typically the request URL, but may also be the URL specified in a "base" tag.
     */
    private final URL baseUrl;

    /**
     * The frame pages contained in this page mapped by the frame name.
     */
    private final Map<String, LightWeightPage> framePages = new HashMap<String, LightWeightPage>();

    /**
     * The web client.
     */
    private final XltWebClient webClient;

    /**
     * Enclosing page.
     */
    private LightWeightPageImpl enc = null;

    /**
     * Value of 'src' attribute via this frame page is referenced by its enclosing page.
     */
    private String source = null;

    /**
     * Constructor.
     * 
     * @param webResponse
     *            the web response
     * @param timerName
     *            the timer name
     * @param webClient
     *            the web client
     * @throws IOException
     *             if the server could not be contacted
     * @throws FailingHttpStatusCodeException
     *             if the returned HTTP status code indicates an error
     */
    public LightWeightPageImpl(final WebResponse webResponse, final String timerName, final XltWebClient webClient)
        throws FailingHttpStatusCodeException, IOException
    {
        super(webResponse, timerName);

        this.webClient = webClient;

        // remove comments from page content
        String uncommentedContent = LWPageUtilities.removeHtmlComments(getContent());

        baseUrl = getBaseUrl(uncommentedContent);

        // recursively load frame pages
        loadFrames(uncommentedContent);
    }

    /**
     * Returns an absolute URL calculated from this page's base URL and the given relative URL.
     * 
     * @param relativeUrl
     *            the relative URL
     * @return an absolute URL
     */
    public URL expandUrl(final String relativeUrl)
    {
        return XltWebClient.makeUrlAbsolute(baseUrl, relativeUrl);
    }

    /**
     * Returns the page of the (i)frame with the given name.
     * 
     * @param name
     *            the frame name
     * @return the page
     */
    public LightWeightPage getFramePageByFrameName(final String name)
    {
        return framePages.get(name);
    }

    /**
     * Returns the pages of all (i)frames on this page mapped by the frame name.
     * 
     * @return a mapping from frame names to pages
     */
    public Map<String, LightWeightPage> getFramePages()
    {
        return framePages;
    }

    /**
     * Determines the base URL of this page. Typically this is the request URL, but it may also be the URL specified in
     * the page's "base" tag.
     * 
     * @param uncommentedContent
     *            Page content without comments
     * @return the base URL
     * @throws MalformedURLException
     *             if the URL in the base tag is invalid
     */
    private URL getBaseUrl(String uncommentedContent) throws MalformedURLException
    {
        URL url = getWebResponse().getWebRequest().getUrl();

        final List<String> baseUrls = LWPageUtilities.getAllBaseLinks(uncommentedContent);
        if (!baseUrls.isEmpty())
        {
            final String baseUrl = baseUrls.get(0);
            final URL u = XltWebClient.makeUrlAbsolute(url, baseUrl);
            if (u != null)
            {
                url = u;
            }
        }

        return url;
    }

    /**
     * Loads the content of all (i)frames on this page. The frame page of a certain frame may later be accessed via
     * {@link #getFramePageByFrameName(String)}. Actually, this method works recursively, so if a frame page contains
     * frames, they will be loaded as well.
     * 
     * @param uncommentedContent
     *            Page content without comments
     * @throws IOException
     *             if the server could not be contacted
     * @throws FailingHttpStatusCodeException
     *             if the returned HTTP status code indicates an error
     */
    private void loadFrames(String uncommentedContent) throws FailingHttpStatusCodeException, IOException
    {
        // get the attributes list of all (i)frames on this page
        final List<String> frameAttributeLists = LWPageUtilities.getAllFrameAttributes(uncommentedContent);
        for (final String attributeList : frameAttributeLists)
        {
            // get the value of the "src" attribute from the list
            final String src = LWPageUtilities.getAttributeValue(attributeList, "src");
            if (accept(src))
            {
                // make the URL absolute
                final URL url = expandUrl(src);

                // get the value of the "name" attribute from the list
                String name = LWPageUtilities.getAttributeValue(attributeList, "name");
                if (name == null)
                {
                    // none found -> generate an artificial name
                    name = RandomStringUtils.randomAlphabetic(10);
                }

                // retrieve the frame page and store it using the frame's name
                final LightWeightPageImpl framePage = (LightWeightPageImpl) webClient.getLightWeightPage(url);
                framePages.put(name, framePage);
                framePage.setEnclosingPage(this);
                framePage.setSource(src);
            }
        }
    }

    /**
     * Sets the enclosing page.
     * 
     * @param enclosingPage
     *            enclosing page
     */
    private void setEnclosingPage(final LightWeightPageImpl enclosingPage)
    {
        enc = enclosingPage;
    }

    /**
     * Returns the enclosing page.
     * 
     * @return enclosing page
     */
    public LightWeightPage getEnclosingPage()
    {
        return enc;
    }

    /**
     * Sets the source.
     * 
     * @param src
     *            the source to set
     */
    private void setSource(final String src)
    {
        source = src;
    }

    /**
     * Gets the source.
     * 
     * @return source
     */
    public String getSource()
    {
        return source;
    }

    /**
     * Returns whether or not the given URL string will be accepted.
     * 
     * @param urlString
     *            URL string to test
     * @return <code>true</code> if the given URL string will be accepted, <code>false</code> otherwise
     */
    private boolean accept(final String urlString)
    {
        if (urlString == null || urlString.length() == 0)
        {
            return false;
        }

        final int idx = urlString.indexOf(':');
        if (idx == -1)
        {
            return true;
        }

        return RegExUtils.isMatching(urlString, "https?://");
    }
}
