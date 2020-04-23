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
package com.gargoylesoftware.htmlunit;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests the implementation of iframe handling.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class HtmlUnitInlineFrameTest
{

    private final static URL URL_FIRST;

    private final static URL URL_SECOND;

    static
    {
        try
        {
            URL_FIRST = new URL("http://www.example.org/first/");
            URL_SECOND = new URL("http://www.example.org/second/");
        }
        catch (final MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected WebClient getWebClient()
    {
        final WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setJavaScriptEnabled(true);
        wc.getOptions().setCssEnabled(false);

        return wc;
    }

    /**
     * #868: The iframe has no source and is filled from javascript.
     * 
     * @throws Exception
     *             if an error occurs
     */
    @Test
    public void testFrameContentCreationViaJavascript() throws Exception
    {
        final String html = "<html><head><title>frames</title></head>\n" + "<body>\n" + "<iframe name='foo'></iframe>\n"
                            + "<script type='text/javascript'>\n" + "var doc = window.frames['foo'].document;\n" + "doc.open();\n"
                            + "doc.write('<html><body><div id=\"myContent\">Hi Folks!</div></body></html>');\n" + "doc.close();\n"
                            + "</script>\n" + "<body>\n" + "</html>";

        final WebClient webClient = getWebClient();
        final MockWebConnection conn = new MockWebConnection();
        webClient.setWebConnection(conn);

        conn.setDefaultResponse(html);
        final HtmlPage page = webClient.getPage(URL_FIRST);

        final HtmlPage enclosedPage = (HtmlPage) page.getFrames().get(0).getEnclosedPage();
        final String content = enclosedPage.getHtmlElementById("myContent").asText();
        Assert.assertEquals("Hi Folks!", content);
    }

    /**
     * #868: The iframe has a source but is filled from javascript before.
     * 
     * @throws Exception
     *             if an error occurs
     */
    @Test
    public void testFrameContentCreationViaJavascriptBeforeFrameResolved() throws Exception
    {
        final String html = "<html><head><title>frames</title></head>\n" + "<body>\n" + "<iframe name='foo' src='" + URL_SECOND +
                            "'></iframe>\n" + "<script type='text/javascript'>\n" + "var doc = window.frames['foo'].document;\n" +
                            "doc.open();\n" + "doc.write('<html><body><div id=\"myContent\">Hi Folks!</div></body></html>');\n" +
                            "doc.close();\n" + "</script>\n" + "<body>\n" + "</html>";

        final String frameHtml = "<html><head><title>inside frame</title></head>\n" + "<body>\n"
                                 + "<div id=\"myContent\">inside frame</div>\n" + "<body>\n" + "</html>";

        final WebClient webClient = getWebClient();
        final MockWebConnection conn = new MockWebConnection();
        webClient.setWebConnection(conn);

        conn.setResponse(URL_FIRST, html);
        conn.setResponse(URL_SECOND, frameHtml);
        final HtmlPage page = webClient.getPage(URL_FIRST);

        final HtmlPage enclosedPage = (HtmlPage) page.getFrames().get(0).getEnclosedPage();
        final String content = enclosedPage.getHtmlElementById("myContent").asText();
        Assert.assertEquals("Hi Folks!", content);
    }

    /**
     * #650: The iframe is created dynamically via JavaScript.
     * 
     * @throws Exception
     */
    @Test
    public void testFrameCreationViaJavaScript() throws Exception
    {
        final String html = "<html><head><title>IFrame Test</title></head><body>" +
                            "<div id=\"framecontent\" onclick=\"createFrame()\"><p>Dummy content here!</p></div>" + "<script>" +
                            "function createFrame(){" + "var e = document.getElementById('framecontent');" + "var str = \"<iframe src=\'" +
                            URL_SECOND + "\' style=\'width:100%\'>\";" + "str += '<p>Your browser does not support frames</p>';" +
                            "str += '</iframe>';" + "e.innerHTML = str;" + "}" + "</script>" + "</body></html>";

        final String frameHtml = "<html><head><title>My Test Frame</title></head>" + "<body><h1>Hi Folks!</h1></body></html>";

        final WebClient webClient = getWebClient();
        final MockWebConnection conn = new MockWebConnection();
        webClient.setWebConnection(conn);

        conn.setResponse(URL_FIRST, html);
        conn.setResponse(URL_SECOND, frameHtml);

        final HtmlPage page = webClient.getPage(URL_FIRST);
        ((HtmlElement) page.getElementById("framecontent")).click();

        Assert.assertFalse(page.getByXPath("//iframe").isEmpty());
        final HtmlPage p = (HtmlPage) page.getFrames().get(0).getEnclosedPage();
        final HtmlElement headline = (HtmlElement) p.getByXPath("//body/h1").get(0);
        Assert.assertEquals("Hi Folks!", headline.getTextContent().trim());

    }
}
