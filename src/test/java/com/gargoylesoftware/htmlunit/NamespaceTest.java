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
package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Demonstrates the inconsistent handling of namespaces in HtmlUnit when processing pure HTML pages. Tests will fail on
 * un-patched HtmlUnit. See Issue #1087 Support namespaces
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class NamespaceTest
{
    private HtmlPage page;

    private static final String PAGE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                       + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
                                       + "<html xmlns=\"http://www.w3.org/1999/xhtml\" >\n"
                                       + "<body>\n"
                                       + "<div id=\"myDiv\">\n"
                                       + "</div>\n"
                                       + "<script type=\"application/javascript\">\n"
                                       + "function f(){ var e = document.createElementNS('http://www.w3.org/1999/xhtml', 'p'); e.innerHTML = new Date().toString(); document.getElementById('myDiv').appendChild(e);};\n"
                                       + "setTimeout(f, 500);\n" + "</script>\n" + "</body>\n" + "</html>";

    @Before
    public void init() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            final URL url = new URL("http://example.org/testNamespace");
            conn.setResponse(url, PAGE, "text/html");
            wc.setWebConnection(conn);

            page = wc.getPage(url);
            // wait for JS
            Thread.sleep(1000L);
        }
    }

    @Test
    @Ignore
    public void testNamespace() throws Throwable
    {
        Assert.assertNull("Namespace defined", page.getDocumentElement().getNamespaceURI());
        final DomNodeList<HtmlElement> paragraphs = page.getElementById("myDiv").getElementsByTagName("p");
        Assert.assertFalse("No paragraphs found", paragraphs.isEmpty());
        for (final HtmlElement paragraph : paragraphs)
        {
            Assert.assertNull("Namespace defined paragraph element created by JS", paragraph.getNamespaceURI());
        }
    }

    @Test
    public void testXPath() throws Throwable
    {
        Assert.assertFalse("No paragraphs found on page", page.getByXPath("//p").isEmpty());
    }

}
