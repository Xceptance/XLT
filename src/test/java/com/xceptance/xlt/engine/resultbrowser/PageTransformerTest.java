/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
/*
 * File: PageTransformerTest.java
 * Created on: Apr 15, 2016
 */
package com.xceptance.xlt.engine.resultbrowser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.htmlunit.MockWebConnection;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.NameValuePair;
import org.htmlunit.util.UrlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltWebClient;
import com.xceptance.xlt.engine.util.LWPageUtilities;

/**
 * Tests the implementation of {@link PageTransformer}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class PageTransformerTest
{
    private static final String HTML_WITH_ENTITIES_IN_ATTS = "<html><body><img src='someImage.png?test&nbsp;=bar&amp;a=b&uuml;z' /></body></html>";

    private final URL url;

    public PageTransformerTest()
    {
        try
        {
            url = new URL("http://localhost/");
        }
        catch (final MalformedURLException mue)
        {
            throw new RuntimeException(mue);
        }
    }

    @Test
    public void testTransformHtmlPage_AttributesWithCharEntityRefs() throws Throwable
    {
        try (final WebClient wc = new WebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(HTML_WITH_ENTITIES_IN_ATTS);
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            final HtmlElement img = (HtmlElement) page.getByXPath("/html/body/img").get(0);

            final URL imageUrl = XltWebClient.makeUrlAbsolute(url, img.getAttribute("src"));
            Assert.assertNotNull(imageUrl);
            Assert.assertEquals("test\u00a0=bar&a=büz", imageUrl.getQuery());

            final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
            final PageDOMClone clone = DomUtils.clonePage(page);

            final PageTransformer instance = new PageTransformer(clone, true);

            final Document doc = instance.transform(urlMapping);
            final Element imgEl = (Element) doc.getElementsByTagName("img").item(0);
            Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" + urlMapping.map(UrlUtils.encodeUrl(imageUrl, false, page.getCharset())),
                                imgEl.getAttribute("src"));
        }
    }

    @Test
    public void testTransformLWPage_AttributesWithCharEntityRefs() throws Throwable
    {
        try (final XltWebClient wc = new XltWebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(HTML_WITH_ENTITIES_IN_ATTS);
            wc.setWebConnection(conn);

            final LightWeightPage lwPage = wc.getLightWeightPage(url);

            final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
            final String imageUrlString = LWPageUtilities.getAllImageLinks(lwPage.getContent()).get(0);

            final URL imageURL = XltWebClient.makeUrlAbsolute(url, imageUrlString);
            Assert.assertNotNull(imageURL);
            Assert.assertEquals("test\u00a0=bar&a=büz", imageURL.getQuery());

            final String doc = new PageTransformer(lwPage).transformLW(urlMapping);

            Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" +
                                urlMapping.map(UrlUtils.encodeUrl(imageURL, false, lwPage.getCharset())),
                                LWPageUtilities.getAllImageLinks(doc).get(0));
        }
    }

    /**
     * Tests the fix for issue #2913
     */
    @Test
    public void testTransformHtmlPage_NonTrivialRedirect() throws Throwable
    {
        try (final XltWebClient wc = new XltWebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setResponse(new URL("https://localhost:1234/testpages/img/spinner.gif"), new byte[0], 200, "OK", "image/png",
                             Collections.emptyList());

            final URL u = new URL("http://example.org/");
            conn.setResponse(u, new byte[0], 301, "Moved Permanently", "text/plain",
                             Arrays.asList(new NameValuePair("Location", "https://localhost:1234/testpages/")));

            final URL url = new URL("https://localhost:1234/testpages/");
            conn.setResponse(url, "<html><body><img src=\"img/spinner.gif\" /> Hoho</body></html>");
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(u);
            final HtmlElement img = (HtmlElement) page.getByXPath("/html/body/img").get(0);

            final URL imageUrl = XltWebClient.makeUrlAbsolute(url, img.getAttribute("src"));
            Assert.assertNotNull(imageUrl);
            final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
            final PageDOMClone clone = DomUtils.clonePage(page);

            final PageTransformer instance = new PageTransformer(clone, true);

            final Document doc = instance.transform(urlMapping);
            final Element imgEl = (Element) doc.getElementsByTagName("img").item(0);
            Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" + urlMapping.map(UrlUtils.encodeUrl(imageUrl, false, page.getCharset())),
                                imgEl.getAttribute("src"));
        }
    }

    /**
     * Tests the fix for issue #2913
     */
    @Test
    public void testTransformLWPage_NonTrivialRedirect() throws Throwable
    {
        try (final XltWebClient wc = new XltWebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setResponse(new URL("https://localhost:1234/testpages/img/spinner.gif"), new byte[0], 200, "OK", "image/png",
                             Collections.emptyList());

            final URL u = new URL("http://example.org/");
            conn.setResponse(u, new byte[0], 301, "Moved Permanently", "text/plain",
                             Arrays.asList(new NameValuePair("Location", "https://localhost:1234/testpages/")));

            final URL url = new URL("https://localhost:1234/testpages/");
            conn.setResponse(url, "<html><body><img src=\"img/spinner.gif\" /> Hoho</body></html>");
            wc.setWebConnection(conn);

            final LightWeightPage lwPage = wc.getLightWeightPage(u);

            final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
            final String imageUrlString = LWPageUtilities.getAllImageLinks(lwPage.getContent()).get(0);

            final URL imageURL = XltWebClient.makeUrlAbsolute(url, imageUrlString);
            Assert.assertNotNull(imageURL);

            final String doc = new PageTransformer(lwPage).transformLW(urlMapping);

            Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" +
                                urlMapping.map(UrlUtils.encodeUrl(imageURL, false, lwPage.getCharset())),
                                LWPageUtilities.getAllImageLinks(doc).get(0));
        }
    }

    @Test
    public void testTransformHtmlPage_BaseUrlHandling() throws Throwable
    {
        try (final XltWebClient wc = new XltWebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(new byte[0], 404, "Not Found", "text/plain");
            wc.setWebConnection(conn);
            {
                final URL u = new URL(url, "foo/img/spinner.gif");
                conn.setResponse(u, new byte[0], 200, "OK", "image/png", Collections.emptyList());
                conn.setResponse(url, "<html><head><base href=\"foo/bar\"></head><body><img src=\"img/spinner.gif\" /></body></html>");

                final HtmlPage page = wc.getPage(url);

                final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
                final PageDOMClone clone = DomUtils.clonePage(page);

                final PageTransformer instance = new PageTransformer(clone, true);

                final Document doc = instance.transform(urlMapping);
                final Element imgEl = (Element) doc.getElementsByTagName("img").item(0);
                Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" + urlMapping.map(UrlUtils.encodeUrl(u, false, page.getCharset())),
                                    imgEl.getAttribute("src"));
            }

            {
                conn.setResponse(url,
                                 "<html><head><base href=\"https://example.org/foo/\"></head><body><img src=\"img/spinner.gif\" /></body></html>");

                final URL u = new URL("https://example.org/foo/img/spinner.gif");
                conn.setResponse(u, new byte[0], 200, "OK", "image/png", Collections.emptyList());

                final HtmlPage page = wc.getPage(url);

                final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
                final PageDOMClone clone = DomUtils.clonePage(page);

                final PageTransformer instance = new PageTransformer(clone, true);

                final Document doc = instance.transform(urlMapping);
                final Element imgEl = (Element) doc.getElementsByTagName("img").item(0);
                Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" + urlMapping.map(UrlUtils.encodeUrl(u, false, page.getCharset())),
                                    imgEl.getAttribute("src"));
            }
        }
    }

    @Test
    public void testTransformLWPage_BaseUrlHandling() throws Throwable
    {
        try (final XltWebClient wc = new XltWebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(new byte[0], 404, "Not Found", "text/plain");
            wc.setWebConnection(conn);
            {
                final URL u = new URL(url, "foo/img/spinner.gif");
                conn.setResponse(u, new byte[0], 200, "OK", "image/png", Collections.emptyList());
                conn.setResponse(url, "<html><head><base href=\"foo/bar\"></head><body><img src=\"img/spinner.gif\" /></body></html>");

                final LightWeightPage page = wc.getLightWeightPage(url);

                final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
                final String doc = new PageTransformer(page).transformLW(urlMapping);

                Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" + urlMapping.map(UrlUtils.encodeUrl(u, false, page.getCharset())),
                                    LWPageUtilities.getAllImageLinks(doc).get(0));
            }

            {
                conn.setResponse(url,
                                 "<html><head><base href=\"https://example.org/foo/\"></head><body><img src=\"img/spinner.gif\" /></body></html>");

                final URL u = new URL("https://example.org/foo/img/spinner.gif");
                conn.setResponse(u, new byte[0], 200, "OK", "image/png", Collections.emptyList());

                final LightWeightPage page = wc.getLightWeightPage(url);

                final UrlMapping urlMapping = new DumpMgr().getUrlMapping();
                final String doc = new PageTransformer(page).transformLW(urlMapping);

                Assert.assertEquals(XltConstants.DUMP_CACHE_DIR + "/" + urlMapping.map(UrlUtils.encodeUrl(u, false, page.getCharset())),
                                    LWPageUtilities.getAllImageLinks(doc).get(0));
            }
        }
    }

    @Test
    public void testTransformHtmlPage_MetaRefreshRemoved() throws Throwable
    {
        try (final WebClient wc = new WebClient())
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><meta http-equiv=\"content-security-policy\" content=\"default-src 'self'\"><meta http-equiv=\"refresh\" content=\"10; url=/\"></head></html>");
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            
            final PageDOMClone clone = DomUtils.clonePage(page);
            final PageTransformer instance = new PageTransformer(clone, true);
            final UrlMapping urlMapping = new DumpMgr().getUrlMapping();

            final Document doc = instance.transform(urlMapping);

            // check that the refresh http-equiv meta tag has been removed, but not the other two http-equiv metas
            final NodeList metas = doc.getElementsByTagName("meta");
            Assert.assertEquals(2, metas.getLength());
            Assert.assertEquals("content-type", ((Element) metas.item(0)).getAttribute("http-equiv"));
            Assert.assertEquals("content-security-policy", ((Element) metas.item(1)).getAttribute("http-equiv"));
        }
    }

    /*
     * TODO: Add more tests
     */
}
