/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.DomNodeList;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlHead;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.UrlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.engine.util.URLCleaner;

/**
 * DOM clone of a HTML page.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
class PageDOMClone
{
    /**
     * DOM document.
     */
    private final Document doc;

    /**
     * Embedded frames.
     */
    private final Map<Element, PageDOMClone> frames;

    /**
     * Web response.
     */
    private final WebResponse response;

    /**
     * Base URL.
     */
    private final URL baseURL;

    /**
     * Creates a new DOM clone for the given HTML page and DOM document.
     *
     * @param htmlPage
     *            original HTML page
     * @param document
     *            DOM document
     */
    public PageDOMClone(final HtmlPage htmlPage, final Document document)
    {
        doc = document;
        frames = new HashMap<Element, PageDOMClone>();
        response = htmlPage.getWebResponse();
        baseURL = URLCleaner.removeUserInfoIfNecessaryAsURL(determineBaseURL(htmlPage));
    }

    /**
     * Returns the DOM document.
     *
     * @return DOM document
     */
    public Document getDocument()
    {
        return doc;
    }

    /**
     * Adds the given page DOM clone to the list of embedded frames.
     *
     * @param frame
     *            embedded frame page as DOM clone
     */
    public synchronized void addFrame(final Element element, final PageDOMClone frame)
    {
        frames.put(element, frame);
    }

    /**
     * Returns the list of embedded frames.
     *
     * @return embedded frames
     */
    public Map<Element, PageDOMClone> getFrames()
    {
        return Collections.unmodifiableMap(frames);
    }

    /**
     * Returns the web response.
     *
     * @return web response
     */
    public WebResponse getResponse()
    {
        return response;
    }

    /**
     * Re-implementation of {@link HtmlPage#getFullyQualifiedUrl(String)}.
     *
     * @param relativeUrl
     *            relative URL string
     * @return absolute URL
     */
    public URL getFullyQualifiedUrl(String relativeUrl)
    {
        // to handle http: and http:/ in FF (Bug 1714767)
        while (relativeUrl.startsWith("http:") && !relativeUrl.startsWith("http://"))
        {
            relativeUrl = "http:/" + relativeUrl.substring(5);
        }

        return XltWebClient.makeUrlAbsolute(baseURL, relativeUrl);
    }

    /**
     * Determines the base URL of the given page.
     *
     * @param page
     *            the HTML page
     * @return base URL of given HTML page
     */
    private URL determineBaseURL(final HtmlPage page)
    {
        final HtmlElement docElement = page.getDocumentElement();
        final DomNodeList<HtmlElement> baseElements = (docElement != null) ? docElement.getElementsByTagName("base") : null;

        URL baseUrl = getResponse().getWebRequest().getUrl();
        if (baseElements == null || baseElements.isEmpty())
        {
            final WebWindow window = page.getEnclosingWindow();
            if (window != null && window != window.getTopWindow())
            {
                final boolean frameSrcIsNotSet = (baseUrl == UrlUtils.URL_ABOUT_BLANK);
                final boolean frameSrcIsJs = "javascript".equals(baseUrl.getProtocol());
                if (frameSrcIsNotSet || frameSrcIsJs)
                {
                    baseUrl = ((HtmlPage) page.getEnclosingWindow().getTopWindow().getEnclosedPage()).getWebResponse().getWebRequest()
                                                                                                     .getUrl();
                }
            }
        }
        else
        {
            final HtmlElement base = baseElements.get(0);
            boolean insideHead = false;
            for (DomNode parent = base.getParentNode(); parent != null; parent = parent.getParentNode())
            {
                if (parent instanceof HtmlHead)
                {
                    insideHead = true;
                    break;
                }
            }
            final String href = base.getAttribute("href");
            if (insideHead && StringUtils.isNotBlank(href))
            {
                final URL u = XltWebClient.makeUrlAbsolute(baseUrl, href);
                if (u != null)
                {
                    baseUrl = u;
                }
            }
        }

        return baseUrl;
    }
}
