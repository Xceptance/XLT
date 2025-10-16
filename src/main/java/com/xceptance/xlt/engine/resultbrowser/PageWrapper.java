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

import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Wraps an HtmlPage or LightWeightPage object. Primarily used to enable uniform handling of both HTML and lightweight
 * pages.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
class PageWrapper
{
    /**
     * Flag which indicates if the page cloning process should be delayed.
     */
    private static final boolean DELAY_CLONING;

    /**
     * Property key used to switch on/off fast dumping.
     */
    private static final String ONERROR_DUMP_MODE = RequestHistory.OUTPUT2DISK_PROPERTY + ".onError.dumpMode";

    /**
     * Property value indicating fast dumping.
     */
    private static final String FINAL_PAGES_ONLY = "finalPagesOnly";

    /**
     * Property value indicating accurate dumping.
     */
    private static final String MODIFIED_AND_FINAL_PAGES = "modifiedAndFinalPages";

    // static initializer
    static
    {
        final XltProperties props = XltProperties.getInstance();
        DELAY_CLONING = !props.getProperty(ONERROR_DUMP_MODE, FINAL_PAGES_ONLY).equals(MODIFIED_AND_FINAL_PAGES);
    }

    /**
     * Wrapped HTML page.
     */
    private PageDOMClone page;

    /**
     * Wrapped lightweight page.
     */
    private final LightWeightPage lwPage;

    /**
     * Wrapped HTML page. Only used if cloning process is delayed.
     */
    private final HtmlPage htmlPage;

    /**
     * Creates a new page wrapper for a lightweight page.
     *
     * @param lwPage
     *            lightweight page to wrap
     */
    public PageWrapper(final LightWeightPage lwPage)
    {
        this.lwPage = lwPage;
        page = null;
        htmlPage = null;
    }

    /**
     * Creates a new page wrapper for a HTML page.
     *
     * @param page
     *            HTML page to wrap
     */
    public PageWrapper(final HtmlPage page)
    {
        lwPage = null;

        if (DELAY_CLONING)
        {
            this.page = null;
            htmlPage = page;
        }
        else
        {
            this.page = DomUtils.clonePage(page);
            htmlPage = null;
        }
    }

    /**
     * Returns <tt>true</tt> if the wrapped page is a HTML page, and <tt>false</tt> otherwise.
     *
     * @return whether or not the wrapped page is a HTML page
     */
    public final boolean isHtmlPage()
    {
        return lwPage == null;
    }

    /**
     * Returns the wrapped HTML page.
     *
     * @return wrapped HTML page
     */
    public final PageDOMClone getHtmlPage()
    {
        if (page == null && htmlPage != null)
        {
            page = DomUtils.clonePage(htmlPage);
        }

        return page;
    }

    /**
     * Returns the wrapped lightweight page.
     *
     * @return wrapped lightweight page
     */
    public final LightWeightPage getLightWeightPage()
    {
        return lwPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if (lwPage != null)
        {
            return lwPage.hashCode();
        }
        if (page != null)
        {
            return page.hashCode();
        }
        if (htmlPage != null)
        {
            return htmlPage.hashCode();
        }

        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        return o != null && hashCode() == o.hashCode();
    }
}
