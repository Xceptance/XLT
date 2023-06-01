/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.StringWebResponse;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;

/**
 * Return an htmlpage build upon a file name in the package we are in.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class HtmlTestViaFile
{
    /**
     * Prepares the webclient to return the resource identified by the given name as HTML page.
     * 
     * @param instance
     *            an instance to load the respective resource for
     * @return content of resource as HTML page
     * @throws Exception
     *             thrown when accessing the resource or reading from it failed
     */
    public static <T> HtmlPage getHtmlPageByName(final T instance) throws Exception
    {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(instance.getClass()
                                                                                        .getResourceAsStream(instance.getClass()
                                                                                                                     .getSimpleName()
                                                                                                                     .replace('.', '/') +
                                                                                                             ".html"))))
        {
            // create response
            final StringWebResponse webResponse = new StringWebResponse(IOUtils.toString(in), new URL("http://www.goo.com"));

            // create new web client instance
            try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
            {
                // deactivate JS
                webClient.getOptions().setJavaScriptEnabled(false);

                // return page created by the web client's page creator
                return (HtmlPage) webClient.getPageCreator().createPage(webResponse, webClient.getCurrentWindow());
            }
        }
    }
}
