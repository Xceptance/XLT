package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

/**
 * Demonstrates an error in the CSS parser.
 */
public class CssParserErrorTest
{
    @Test
    public void testInvalidCSS() throws Throwable
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final HtmlPage htmlPage = webClient.getPage(getClass().getResource("CssParserErrorTest.html"));

            // check the style
            final HTMLElement el = (HTMLElement) htmlPage.getElementById("theme1").getScriptableObject();
            final String bgImgUrl = el.getCurrentStyle().getBackgroundImage();
            Assert.assertEquals("Invalid background image URL: " + bgImgUrl, "url(1.jpg)", bgImgUrl);
        }
    }
}
