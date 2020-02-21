package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Shows that a JavaScript is loaded twice in total if it is already loaded on before the page is completely loaded.
 * This was a bug in HtmlUnit that was finally resolved with HtmlUnit2.9 and related to our issue <a href="
 * https://lab.xceptance.de/issues/578">578</a>
 */
public class OnLoadHandlerCalledTwiceTest
{
    @Test
    public void testWithFrame() throws Exception
    {
        test("_frame.html");
    }

    @Test
    public void testWithIFrame() throws Exception
    {
        test("_iframe.html");
    }

    private void test(final String suffix) throws Exception
    {
        BasicConfigurator.configure();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            webClient.getOptions().setJavaScriptEnabled(true);

            final List<String> alerts = new ArrayList<String>();
            webClient.setAlertHandler(new CollectingAlertHandler(alerts));

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + suffix);
            webClient.getPage(url);

            // check whether the expected values are returned
            Assert.assertEquals("Onload handler called twice!", 1, alerts.size());
        }
    }
}
