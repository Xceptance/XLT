/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.common.xml.HtmlDomPrinter;

/**
 * Tests the implementation of {@link DomUtils}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DomUtilsTest
{
    /**
     * Test the implementation of {@link DomUtils#clonePage(HtmlPage)}.
     *
     * @throws Throwable
     *             thrown when creation of clone has failed or clone is malformed
     */
    @Test
    public void testClonePage() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final MockWebConnection conn = new MockWebConnection();
        final String content = "<html><head><title>Foobar</title></head><body>Content<iframe src=\"test\" /></body></html>";
        conn.setResponse(url, content);

        final String frameContent = "<html><head><title>Frame</title></head><body>FRAME</body></html>";
        final URL frameURL = new URL("http://localhost/test");
        conn.setResponse(frameURL, frameContent);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            final PageDOMClone clone = DomUtils.clonePage(page);

            final Map<Element, PageDOMClone> frames = clone.getFrames();
            Assert.assertEquals(1, frames.size());
            Assert.assertEquals(frameContent, new HtmlDomPrinter().printNode(frames.values().iterator().next().getDocument()));
        }
    }

    /**
     * Test the implementation of {@link DomUtils#clonePage(HtmlPage)} cover comments and CDATA.
     *
     * @throws Throwable
     *             thrown when creation of clone has failed or clone is malformed
     */
    @Test
    public void testClonePage_Comment() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final MockWebConnection conn = new MockWebConnection();
        final String content = "<html>" + "<head>" + "<title>Foobar</title>" + "</head>" + "<body>" + "<!-- comment -->" + "</body>" +
                               "</html>";
        conn.setResponse(url, content);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            final PageDOMClone clone = DomUtils.clonePage(page);

            Assert.assertEquals(content, new HtmlDomPrinter().printNode(clone.getDocument()));
        }
    }

    /**
     * Test the implementation of {@link DomUtils#clonePage(HtmlPage)} Covers the header
     *
     * @throws Throwable
     *             thrown when creation of clone has failed or clone is malformed
     */
    @Test
    public void testClonePage_HtmlHeader() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final MockWebConnection conn = new MockWebConnection();
        final String content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
                               "\"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                               "<html><head><title>Title of Page</title></head><body>Text</body></html>";
        conn.setResponse(url, content);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            final PageDOMClone clone = DomUtils.clonePage(page);

            Assert.assertEquals(content, new HtmlDomPrinter().printNode(clone.getDocument()));
        }
    }

    /**
     * Tests the implementation of {@link DomUtils#clonePage(HtmlPage)}. More specifically, tests if attribute values
     * are correctly cloned as fix of issue XLT#1954.
     *
     * @throws Throwable
     */
    @Test
    public void testClonePage_CharEntityInAttValue() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final MockWebConnection conn = new MockWebConnection();
        final String attValue = "x=6, y=&quot;fooBar&quot;";
        final String content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
                               "\"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                               "<html><head><title>Title of Page</title></head><body>Text<div><input name=\"foo\" type=\"text\" value=\"" +
                               attValue + "\"></div></body></html>";
        conn.setResponse(url, content);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            final PageDOMClone clone = DomUtils.clonePage(page);

            Assert.assertEquals(content, new HtmlDomPrinter().printNode(clone.getDocument()));
        }
    }

    @Test
    public void testClonePage_TextNodeWithCharEntity() throws Throwable
    {
        final URL url = new URL("http://localhost/");
        final MockWebConnection conn = new MockWebConnection();
        final String text = "&lt;/body&gt; &apos;close&apos;";
        final String content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
                               "\"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                               "<html><head><title>Title of Page</title></head><body>Text<div>" + text + "</div><noscript>" + text +
                               "</noscript></body></html>";
        conn.setResponse(url, content);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            final PageDOMClone clone = DomUtils.clonePage(page);

            Assert.assertEquals(content, new HtmlDomPrinter().printNode(clone.getDocument()));
        }

    }
}
