/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Tests the implementation of the utility class {@link CssUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class CssUtilsTest
{

    /**
     * Sample CSS content.
     */
    private static final String SAMPLE_CSS_CONTENT = "someText url(test1)\n@import \"test2\" screen;\n@import url('test3');\n@import 'test4' handheld;";

    /**
     * Sample base URL.
     */
    private static final String SAMPLE_BASE_URL = "http://localhost";

    /**
     * Pattern used to validate computed resource URLs.
     */
    private static final Pattern RESOURCE_URL_PATTERN = Pattern.compile(SAMPLE_BASE_URL + "/test[1-4]");

    /**
     * Pattern used to validate computed relative resource URLs.
     */
    private static final Pattern RELATIVE_RESOURCE_URL_PATTERN = Pattern.compile("test[1-4]");

    /**
     * Mocked response.
     */
    private WebResponse response = null;

    @Before
    public void init() throws Throwable
    {
        response = Mockito.mock(WebResponse.class);
        Mockito.when(response.getWebRequest()).thenReturn(new WebRequest(new URL("http://localhost")));
    }

    @Test
    public void testIsCss() throws Throwable
    {
        Mockito.when(response.getContentType()).thenReturn("text/html");
        Assert.assertFalse(CssUtils.isCssResponse(response));

        Mockito.when(response.getContentType()).thenReturn("text/mcss");
        Assert.assertFalse(CssUtils.isCssResponse(response));

        Mockito.when(response.getContentType()).thenReturn("html/css");
        Assert.assertFalse(CssUtils.isCssResponse(response));

        Mockito.when(response.getContentType()).thenReturn("  text/css ");
        Assert.assertTrue(CssUtils.isCssResponse(response));

        Mockito.when(response.getContentType()).thenReturn((String) null);
        Mockito.when(response.getWebRequest()).thenReturn(new WebRequest(new URL("http://localhost")));
        Assert.assertFalse(CssUtils.isCssResponse(response));

        Mockito.when(response.getWebRequest()).thenReturn(new WebRequest(new URL("http://localhost-css")));
        Assert.assertFalse(CssUtils.isCssResponse(response));

        Mockito.when(response.getWebRequest()).thenReturn(new WebRequest(new URL("http://localhost/examplecss")));
        Assert.assertFalse(CssUtils.isCssResponse(response));

        Mockito.when(response.getWebRequest()).thenReturn(new WebRequest(new URL("http://localhost/example.css")));
        Assert.assertTrue(CssUtils.isCssResponse(response));
    }

    @Test
    public void testGetResourceUrls_NonCssResponse() throws Throwable
    {
        Assert.assertTrue(CssUtils.getResourceUrls(response).isEmpty());
    }

    @Test
    public void testGetResourceUrls_NoResources() throws Throwable
    {
        Mockito.when(response.getContentType()).thenReturn("text/css");
        Mockito.when(response.getContentAsString()).thenReturn("");

        Assert.assertTrue(CssUtils.getResourceUrls(response).isEmpty());
    }

    @Test
    public void testGetResourceUrls_ContainsResources() throws Throwable
    {
        Mockito.when(response.getWebRequest()).thenReturn(new WebRequest(new URL(new URL(SAMPLE_BASE_URL), "example.css")));
        Mockito.when(response.getContentAsString()).thenReturn(SAMPLE_CSS_CONTENT);

        final Collection<URL> resourceUrls = CssUtils.getResourceUrls(response);
        Assert.assertEquals(4, resourceUrls.size());

        for (final URL url : resourceUrls)
        {
            Assert.assertTrue(RESOURCE_URL_PATTERN.matcher(url.toString()).matches());
        }
    }

    @Test
    public void testGetRelativeUrlStrings() throws Throwable
    {
        Assert.assertTrue(CssUtils.getUrlStrings(null).isEmpty());
        Assert.assertTrue(CssUtils.getUrlStrings("").isEmpty());

        final Collection<String> relativeUrls = CssUtils.getUrlStrings(SAMPLE_CSS_CONTENT);
        Assert.assertEquals(4, relativeUrls.size());

        for (final String s : relativeUrls)
        {
            Assert.assertTrue(RELATIVE_RESOURCE_URL_PATTERN.matcher(s).matches());
        }
    }

    @Test
    public void testGetResourceUrls4StringURL() throws Throwable
    {
        Assert.assertTrue(CssUtils.getResourceUrls((String) null, new URL(SAMPLE_BASE_URL)).isEmpty());
        Assert.assertTrue(CssUtils.getResourceUrls(SAMPLE_CSS_CONTENT, (URL) null).isEmpty());

        final Collection<URL> resourceUrls = CssUtils.getResourceUrls(SAMPLE_CSS_CONTENT, new URL(SAMPLE_BASE_URL));
        Assert.assertEquals(4, resourceUrls.size());

        for (final URL url : resourceUrls)
        {
            Assert.assertTrue(RESOURCE_URL_PATTERN.matcher(url.toString()).matches());
        }

    }

    @Test
    public void testGetResourceUrls4StringString() throws Throwable
    {
        Assert.assertTrue(CssUtils.getResourceUrls(null, (String) null).isEmpty());
        Assert.assertTrue(CssUtils.getResourceUrls(null, SAMPLE_BASE_URL).isEmpty());
        Assert.assertTrue(CssUtils.getResourceUrls("", SAMPLE_BASE_URL).isEmpty());

        final Collection<URL> resourceUrls = CssUtils.getResourceUrls(SAMPLE_CSS_CONTENT, SAMPLE_BASE_URL);
        Assert.assertEquals(4, resourceUrls.size());

        for (final URL url : resourceUrls)
        {
            Assert.assertTrue(RESOURCE_URL_PATTERN.matcher(url.toString()).matches());
        }
    }

    @Test
    public void testGetRelativeUrlStrings_Malformed() throws Throwable
    {
        final String cssContent = "url('foobar.jpg);\n@import url(baz.png\") screen;\nurl(\"foo.gif);\nurl 'bla.org';\nurl(test1.svg');\nurl(\"test2.gif');\nurl('test3.jpg\")";
        final Collection<String> relativeUrls = CssUtils.getUrlStrings(cssContent);
        Assert.assertTrue("Incorrect computed relative URL strings: " + relativeUrls, relativeUrls.isEmpty());
    }

    @Test
    public void testGetRelativeUrlStrings_EmptyUrls() throws Throwable
    {
        final String cssContent = "background-color: #000000; background-image: url();background-color: #2A1E1C; background-image: url();background-color: #0E0F11; background-image: url();background-color: #000000; background-image: url();background-color: #2A1E1C; background-image: url();background-color: #0E0F11; background-image: url();";
        final Collection<String> relativeUrls = CssUtils.getUrlStrings(cssContent);
        Assert.assertTrue("Incorrect computed relative URL strings: " + relativeUrls, relativeUrls.isEmpty());
    }

    @Test
    public void testGetUrlStrings_ContainingAbsoluteAndRelativeUrls() throws Throwable
    {
        String absoluteUrl = "http://foo.bar.net/test.jpg";
        String relativeUrl = "bla/test2.jpg";

        final String cssContent = "#id1{ background-image: url('" + absoluteUrl + "'); } div{ background-image: url('" + relativeUrl +
                                  "'); }";
        final Collection<String> urls = CssUtils.getUrlStrings(cssContent);

        // Validate
        Assert.assertEquals("Unexpected number of urls", 2, urls.size());
        Assert.assertTrue("Absolute URL not found", urls.contains(absoluteUrl));
        Assert.assertTrue("Relative URL not found", urls.contains(relativeUrl));
    }

    @Test
    public void testGetResourceUrls_ContainingAbsoluteAndRelativeUrls() throws Throwable
    {
        String absoluteUrl = "http://foo.bar.net/test.jpg";
        String relativeUrl = "bla/test2.jpg";

        final String cssContent = "#id1{ background-image: url('" + absoluteUrl + "'); } div{ background-image: url('" + relativeUrl +
                                  "'); }";
        final Collection<URL> urls = CssUtils.getResourceUrls(cssContent, SAMPLE_BASE_URL);

        // Validate
        List<String> urlStrings = new ArrayList<>();
        for (URL eachUrl : urls)
        {
            urlStrings.add(eachUrl.toString());
        }
        Assert.assertEquals("Unexpected number of urls", 2, urlStrings.size());
        Assert.assertTrue("Absolute URL not found", urlStrings.contains(absoluteUrl));
        Assert.assertTrue("Relative URL not found", urlStrings.contains(SAMPLE_BASE_URL + "/" + relativeUrl));
    }

    @Test
    public void testClearImportRules() throws Throwable
    {
        Assert.assertEquals("someText url(test1)", CssUtils.clearImportRules(SAMPLE_CSS_CONTENT).trim());
    }

    @Test
    public void testRelativeUrlsWithQuotes() throws Throwable
    {
        String cssContent = "#id_2 { background-image: url('foo.jpg') } \n@import url(\"bar.css\");";
        final Collection<String> urls = CssUtils.getUrlStrings(cssContent);
        Assert.assertEquals(2, urls.size());

        Assert.assertTrue(urls.contains("foo.jpg"));
        Assert.assertTrue(urls.contains("bar.css"));
    }
}
