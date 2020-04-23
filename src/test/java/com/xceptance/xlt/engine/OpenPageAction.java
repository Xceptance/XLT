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
package com.xceptance.xlt.engine;

import java.net.URL;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;

/**
 * Opens a new page by loading the given URL.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class OpenPageAction extends AbstractHtmlPageAction
{
    /**
     * URL to load as string.
     */
    private final String urlString;

    /**
     * URL to load as URL object.
     */
    private URL url;

    /**
     * Constructor.
     * 
     * @param urlString
     *            URL to load as string
     */
    public OpenPageAction(final String urlString)
    {
        super(null);
        this.urlString = urlString;
    }

    /*
     * (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#execute()
     */
    @Override
    protected void execute() throws Exception
    {
        loadPage(url);
    }

    /*
     * (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#postValidate()
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();

        Assert.assertNotNull("Failed to load page for URL '" + url.toExternalForm() + "'.", page);
        Assert.assertEquals(200, page.getWebResponse().getStatusCode());
    }

    /*
     * (non-Javadoc)
     * @see com.xceptance.xlt.api.actions.AbstractAction#preValidate()
     */
    @Override
    public void preValidate() throws Exception
    {
        url = new URL(urlString);
    }

}
