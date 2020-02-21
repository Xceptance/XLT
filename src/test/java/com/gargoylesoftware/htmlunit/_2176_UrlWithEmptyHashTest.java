package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class _2176_UrlWithEmptyHashTest
{
    static
    {
        BasicConfigurator.configure();
    }

    private static final String FOO_URL = "http://foo.com/";

    private static final String BAR_URL = "http://bar.com/";

    @Test
    public void test_differentUrls_noHash() throws Throwable
    {
        test(FOO_URL, BAR_URL, false);
    }

    @Test
    public void test_differentUrls_justHash() throws Throwable
    {
        test(FOO_URL, BAR_URL + "#", false);
    }

    @Test
    public void test_differentUrls_properHash() throws Throwable
    {
        test(FOO_URL, BAR_URL + "#aaa", false);
    }

    @Test
    public void testSamePage_noHash() throws Throwable
    {
        test(FOO_URL, FOO_URL, true);
    }

    @Test
    public void testSamePage_justHash() throws Throwable
    {
        test(FOO_URL, FOO_URL + "#", true);
    }

    @Test
    public void testSamePage_properHash() throws Throwable
    {
        test(FOO_URL, FOO_URL + "#aaa", true);
    }

    private void test(String urlString1, String urlString2, boolean hashJump) throws Throwable
    {
        // set up mock web connection
        URL url1 = new URL(urlString1);
        URL url2 = new URL(urlString2);

        final String title1 = "Page 1";
        final String title2 = "Page 2";
        String page1 = "<html><head><title>" + title1 + "</title></head><body><a id='anchor' href='" + url2 + "'>Go</a></body></html>";
        String page2 = "<html><head><title>" + title2 + "</title></head><body>Got there!</body></html>";

        MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url2, page2);
        conn.setResponse(url1, page1);

        // set up web client
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.setWebConnection(conn);

            // test
            HtmlPage htmlPage = webClient.getPage(url1);
            // System.out.println(htmlPage.asXml());

            HtmlElement a = htmlPage.getHtmlElementById("anchor");
            htmlPage = a.click();
            // System.out.println(htmlPage.asXml());

            final String expectedText = hashJump ? title1 : title2;
            Assert.assertEquals(expectedText, htmlPage.getTitleText());
        }
    }
}
