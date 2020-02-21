package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Demonstrates that elements are visible or not depending on the order media-dependent styles are embedded into the
 * page.
 */
public class ElementVisibilityTest
{
    @Test
    public void test1() throws Exception
    {
        // 1) screen 2) handheld 3) print -> fails
        test("1");
    }

    @Test
    public void test2() throws Exception
    {
        // 1) handheld 2) print 3) screen -> succeeds
        test("2");
    }

    private void test(final String fileIndex) throws Exception
    {
        BasicConfigurator.configure();

        // setup
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setCssEnabled(true);

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "-" + fileIndex + ".html");
            final HtmlPage page = webClient.getPage(url);

            final HtmlElement div = page.getHtmlElementById("foo");

            Assert.assertTrue("Element not displayed", div.isDisplayed());
        }
    }

    @Test
    public void testHiddenInput() throws Exception
    {
        final String content = "<html><body><form id=\"theForm\" action=\"javascript:void(0)\">"
                               + "<input type=\"hidden\" name=\"myHiddenInput\" value=\"SomeValue\" />" + "</form></body></html>";
        final MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(content);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage("http://localhost");
            final HtmlForm form = page.getHtmlElementById("theForm");
            Assert.assertFalse("Hidden input is visible", form.getInputByName("myHiddenInput").isDisplayed());
        }
    }
}
