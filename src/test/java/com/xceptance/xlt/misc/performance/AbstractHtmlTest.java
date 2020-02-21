package com.xceptance.xlt.misc.performance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Base class for all HTML tests.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractHtmlTest extends AbstractXLTTestCase
{
    /**
     * Prepares the webclient to return the resource identified by the given name as HTML page.
     * 
     * @param instance
     *            an instance of a class that indicates the name to load
     * @return content of resource as HTML page
     * @throws Exception
     *             thrown when accessing the resource or reading from it failed
     */
    protected <T extends AbstractHtmlTest> HtmlPage setUp(final T instance) throws Exception
    {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(instance.getClass()
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
