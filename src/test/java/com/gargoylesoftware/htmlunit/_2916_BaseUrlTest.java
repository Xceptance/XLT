package com.gargoylesoftware.htmlunit;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class _2916_BaseUrlTest
{
    private static final String pageUrl = "http://host/path/to/page.html";

    private static final String pageTemplate = "<html><head><base href='%s'></head><body></body></html>";

    @Test
    public void fullUrl() throws FailingHttpStatusCodeException, IOException
    {
        test("http://otherhost/img/", "http://otherhost/img/");
    }

    @Test
    public void absolutePath() throws FailingHttpStatusCodeException, IOException
    {
        test("/img/", "http://host:80/img/");
    }

    @Test
    public void relativePath_1() throws FailingHttpStatusCodeException, IOException
    {
        test("img/", "http://host/path/to/img/");
    }

    @Test
    public void relativePath_2() throws FailingHttpStatusCodeException, IOException
    {
        test("img", "http://host/path/to/img");
    }

    @Test
    public void relativePath_3() throws FailingHttpStatusCodeException, IOException
    {
        test("../../../../img/", "http://host/img/");
    }

    private void test(String baseElementHref, String expectedBaseUrl) throws FailingHttpStatusCodeException, IOException
    {
        String pageContent = String.format(pageTemplate, baseElementHref);

        MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(pageContent);

        try (WebClient wc = new WebClient())
        {
            wc.setWebConnection(conn);

            HtmlPage page = wc.getPage(pageUrl);

            String actualBaseUrl = page.getBaseURL().toString();
            System.out.println(actualBaseUrl);

            Assert.assertEquals(expectedBaseUrl, actualBaseUrl);
        }
    }
}
