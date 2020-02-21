package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @see https://sourceforge.net/p/htmlunit/bugs/1509/
 * @see https://lab.xceptance.de/issues/1987
 */
public class _1987_HtmlEncodingWhenSettingInnerHtmlTest
{
    @Test
    public void test() throws Throwable
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final URL url = getClass().getResource(getClass().getSimpleName() + ".html");
            final HtmlPage htmlPage = webClient.getPage(url);

            final String pageAsXml = htmlPage.asXml();
            System.err.println(htmlPage.asXml());

            Assert.assertFalse("Encountered '&' in page XML", pageAsXml.contains("&"));
        }
    }
}
