package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests that 'window.onchange' is a valid property (not <code>undefined</code>).
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TWindowOnChangeTest
{
    @Test
    public void testWindowOnchange() throws Throwable
    {
        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setDefaultResponse("<html><head><title>TEST PAGE</title></head><body></body></html>");

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            wc.setWebConnection(webConnection);

            final HtmlPage page = wc.getPage("http://www.example.org");
            Assert.assertTrue("window.onchange is undefined", ((Boolean) page.executeJavaScript("'onchange' in window")
                                                                             .getJavaScriptResult()).booleanValue());
        }
    }
}
