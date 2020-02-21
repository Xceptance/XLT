package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class IEConditionalCommentExpressionEvaluatorTest
{
    @Test
    public void test() throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER))
        {
            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");
            final HtmlPage htmlPage = webClient.getPage(url);

            // check whether there are still IE conditional expression artifacts on
            // the page

            System.err.println(htmlPage.asXml());
            Assert.assertFalse("IE conditional expression artifacts found", htmlPage.asXml().contains("[if IE]&gt;"));
        }
    }
}
