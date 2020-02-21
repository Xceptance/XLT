package com.xceptance.xlt.engine;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Tests the implementation of {@link LightWeightPageImpl}.
 */
public class LightWeightPageImplTest
{
    @Test
    public void testIssue2240() throws Throwable
    {
        // base URL is malformed,but commented out
        final WebResponse webResponse = new StringWebResponse("<!-- <base href=\"/en/\"> -->", StandardCharsets.UTF_8,
                                                              new URL("http://localhost/"));

        // must not throw MalformedURLException
        new LightWeightPageImpl(webResponse, "OpenStartPage", null);
    }
}
