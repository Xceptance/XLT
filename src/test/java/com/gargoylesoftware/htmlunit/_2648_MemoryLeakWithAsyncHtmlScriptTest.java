package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @see https://lab.xceptance.de/issues/2648
 */
public class _2648_MemoryLeakWithAsyncHtmlScriptTest
{
    @Test
    @Ignore("To be run manually only")
    public void test() throws IOException, InterruptedException
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection webConnection = new MockWebConnection();
            webClient.setWebConnection(webConnection);

            // main page
            final URL url = new URL("http://localhost/");
            webConnection.setResponse(url, "<script src='http://localhost/dummy.js' async></script>", "text/html");

            // some empty, but valid JS source
            webConnection.setResponse(new URL("http://localhost/dummy.js"), "");

            // test
            for (int i = 0; i < 10000; i++)
            {
                System.err.printf("### %d\n", i);
                webClient.getPage(url);
            }
        }
    }
}
