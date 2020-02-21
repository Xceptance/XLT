package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class _689_TagBalancingTest
{
    @Test
    public void testQuirksModeParagraph() throws Exception
    {
        test("quirks-paragraph", "/html/body/p/table");
    }

    @Test
    public void testQuirksModeAnchor() throws Exception
    {
        test("quirks-anchor", "/html/body/a/table");
    }

    @Test
    @Ignore("HtmlUnit v2.21 or higher not integrated yet")
    public void testStandardModeParagraph() throws Exception
    {
        test("std-paragraph", "/html/body/table");
    }

    private void test(String suffix, String xpath) throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "-" + suffix + ".html");
            final HtmlPage page = webClient.getPage(url);
            
            // make the check
            final List<?> result = page.getByXPath(xpath);
            Assert.assertFalse("No element not found for xpath: " + xpath, result.isEmpty());
        }
    }
}
