package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Test for issue <a href="https://lab.xceptance.de/issues/2859">#2859</a>.
 */
public class _2859_MutationObserverTest
{

    /**
     * Executes the test.
     *
     * @throws Throwable
     *             if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);

            final HtmlPage page = wc.getPage(getClass().getResource(getClass().getSimpleName()+".html"));
            page.executeJavaScript("document.getElementById('headline').style = 'color:red'");

            Assert.assertFalse("Mutation observer callback was NOT invoked", page.getByXPath("/html/body/p").isEmpty());
        }
    }
}