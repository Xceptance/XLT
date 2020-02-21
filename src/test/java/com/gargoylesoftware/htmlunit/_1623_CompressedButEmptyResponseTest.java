package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * Tests whether an empty response is handled correctly even if it is flagged as compressed. See
 * https://sourceforge.net/tracker/?func=detail&aid=3566999&group_id=47038&atid=448266 or #1623 for more information.
 */
public class _1623_CompressedButEmptyResponseTest
{
    @Test
    public void test() throws Exception
    {
        final URL url = new URL("http://foo.bar/");

        // setup
        final List<NameValuePair> responseHeaders = new ArrayList<NameValuePair>();
        responseHeaders.add(new NameValuePair("Content-Length", "0"));
        responseHeaders.add(new NameValuePair("Content-Encoding", "gzip"));

        final MockWebConnection mockWebConnection = new MockWebConnection();
        mockWebConnection.setResponse(url, "", 200, "OK", "text/html", responseHeaders);

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.setWebConnection(mockWebConnection);

            // test
            final WebResponse webResponse = webClient.loadWebResponse(new WebRequest(url));
            Assert.assertEquals(0, webResponse.getContentAsString().length());
        }
    }
}
