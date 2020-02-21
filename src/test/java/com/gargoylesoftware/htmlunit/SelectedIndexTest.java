package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test shows that the "selectedIndex" attribute is not available for the "options" collection of an HTML select
 * element if the current browser emulation is Firefox. So "select.options.selectedIndex" returns "undefined". However,
 * the real Firefox browser supports this expression.
 */

public class SelectedIndexTest
{
    @Test
    public void test() throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final List<String> alerts = new ArrayList<String>();
            webClient.setAlertHandler(new CollectingAlertHandler(alerts));

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");
            webClient.getPage(url);

            // check whether the expected values are returned
            Assert.assertEquals("1", alerts.get(0)); // select.selectedIndex
            Assert.assertEquals("1", alerts.get(1)); // select.options.selectedIndex
        }
    }
}
