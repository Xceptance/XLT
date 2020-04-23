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

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests the fix of issue XLT#1981.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class LoadFrameContentByJSUrlTest
{
    @Test
    public void test_ff45() throws Exception
    {
        test(BrowserVersion.FIREFOX);
    }

    @Test
    public void test_ie() throws Exception
    {
        test(BrowserVersion.INTERNET_EXPLORER);
    }

    private void test(final BrowserVersion browserVersion) throws Exception
    {
        try (final WebClient wc = new WebClient(browserVersion))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            final MockWebConnection webConnection = new MockWebConnection();
            wc.setWebConnection(webConnection);

            final String testString = "FOO BAR baZ!";
            final URL u1 = new URL("http://example.org/");
            webConnection.setResponse(u1, "<html><body>  " + "<script>" +
                                          "var frame = document.createElement('iframe'); frame.setAttribute('id', 'theFrame');" +
                                          "document.body.appendChild(frame); frame.contentWindow['theFrame'] = '<html><body>" + testString +
                                          "</body></html>';" + "frame.src='javascript:window[\"theFrame\"];';" + "</script>" +
                                          "</body></html>");

            final HtmlPage page = wc.getPage(u1);
            final HtmlPage framePage = (HtmlPage) page.getFrames().get(0).getEnclosedPage();
            Assert.assertEquals(testString, framePage.getBody().getTextContent().trim());
        }
    }
}
