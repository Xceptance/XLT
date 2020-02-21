package com.gargoylesoftware.htmlunit;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @see https://lab.xceptance.de/issues/2053
 * @see https://sourceforge.net/p/htmlunit/bugs/1577/
 */
public class _2053_ExcessiveSocketUsageTest
{
    @Test
    @Ignore("To be run manually only")
    public void testConnectionsAreClosedWhenWebClientIsClosed() throws IOException
    {
        for (int i = 0; i < 10000; i++)
        {
            System.err.printf("### %d\n", i);

            WebClient webClient = new WebClient(BrowserVersion.CHROME);

            webClient.getPage("http://localhost:8080/posters");
            // System.err.printf("### %s\n", ((HtmlPage)webClient.getCurrentWindow().getEnclosedPage()).asXml());

            webClient.close();
        }
    }

    @Test
    @Ignore("To be run manually only")
    public void testConnectionsAreReusedForSubsequentRequests() throws IOException
    {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        for (int i = 0; i < 10000; i++)
        {
            System.err.printf("### %d\n", i);

            webClient.getPage("http://localhost:8080/posters");
            // System.err.printf("### %s\n", ((HtmlPage)webClient.getCurrentWindow().getEnclosedPage()).asXml());
        }

        webClient.close();
    }
}
