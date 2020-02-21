package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class _1595_TagBalanceTest
{
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final HtmlPage page = wc.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));

            Assert.assertFalse(page.asXml(), page.getByXPath("//var/li").isEmpty());
        }
    }
}
