package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test shows that the "parseFloat()" function in Rhino does not ignore leading line termination characters when
 * parsing strings into floats. However, the major browsers accept/ignore these characters.
 */
public class ParseFloatTest
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
            Assert.assertEquals("1.23", alerts.get(0));
            Assert.assertEquals("4.56", alerts.get(1));
            Assert.assertEquals("7.89", alerts.get(2));
        }
    }
}
