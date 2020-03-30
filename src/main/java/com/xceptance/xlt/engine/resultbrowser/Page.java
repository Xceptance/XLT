/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;

/**
 * Represents an HTML page added to the request history.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
class Page
{
    private ActionInfo actionInfo;

    /**
     * page wrapper to enable uniform handling of lightweight and HTML pages
     */
    private PageWrapper wrapper;

    /**
     * associated timer name
     */
    private final String name;

    /**
     * The page image.
     */
    private byte[] pageImage;

    /**
     * associated requests
     */
    private final List<Request> requests = new ArrayList<Request>();

    /**
     * Constructor.
     *
     * @param name
     *            the page name
     * @param htmlPage
     *            the HTML page
     */
    public Page(final String name, final HtmlPage htmlPage)
    {
        this.name = name;
        wrapper = new PageWrapper(htmlPage);
    }

    /**
     * Constructor.
     *
     * @param name
     *            the page name
     * @param page
     *            the lightweight page
     */
    public Page(final String name, final LightWeightPage page)
    {
        this.name = name;
        wrapper = new PageWrapper(page);
    }

    /**
     * Constructor.
     *
     * @param name
     *            the page name
     * @param pageImage
     *            the page image
     */
    public Page(final ActionInfo actionInfo, final byte[] pageImage)
    {
        name = actionInfo.name;
        this.actionInfo = actionInfo;
        this.pageImage = pageImage;
    }

    /**
     * Constructor.
     *
     * @param name
     *            the page name
     */
    public Page(final String name)
    {
        this.name = name;
    }

    /**
     * Returns whether or not the wrapped page is a HTML page.
     *
     * @return <code>true</code> if the wrapped page is a HTML page, <code>false</code> otherwise
     */
    public final boolean isHtmlPage()
    {
        return wrapper != null && wrapper.isHtmlPage();
    }

    /**
     * Returns whether or not the wrapped page is a page image (screenshot).
     *
     * @return <code>true</code> if the wrapped page is a page image, <code>false</code> otherwise
     */
    public final boolean isScreenshotPage()
    {
        return pageImage != null;
    }

    /**
     * Returns whether or not the page represents an empty page.
     *
     * @return <code>true</code> if the page is empty, <code>false</code> otherwise
     */
    public final boolean isEmptyPage()
    {
        return pageImage == null && wrapper == null;
    }

    /**
     * Returns the wrapped HTML page.
     *
     * @return wrapped HTML page
     */
    public final PageDOMClone getHtmlPage()
    {
        return (wrapper == null) ? null : wrapper.getHtmlPage();
    }

    /**
     * Returns the wrapped lightweight page.
     *
     * @return wrapped lightweight page
     */
    public final LightWeightPage getLightWeightPage()
    {
        return (wrapper == null) ? null : wrapper.getLightWeightPage();
    }

    /**
     * Returns the wrapped screenshot page.
     *
     * @return wrapped screenshot page
     */
    public final byte[] getScreenshotPage()
    {
        return pageImage;
    }

    /**
     * Returns the name.
     *
     * @return name
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Returns the list of requests.
     *
     * @return list of requests
     */
    public final List<Request> getRequests()
    {
        return requests;
    }

    public ActionInfo getActionInfo()
    {
        return actionInfo;
    }
}
