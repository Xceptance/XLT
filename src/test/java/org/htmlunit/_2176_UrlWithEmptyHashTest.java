/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package org.htmlunit;

import java.net.URL;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

public class _2176_UrlWithEmptyHashTest
{
    private static final String FOO_URL = "http://foo.com/";

    private static final String BAR_URL = "http://bar.com/";

    @Test
    public void test_differentUrls_noHash() throws Throwable
    {
        test(FOO_URL, BAR_URL, false);
    }

    @Test
    public void test_differentUrls_justHash() throws Throwable
    {
        test(FOO_URL, BAR_URL + "#", false);
    }

    @Test
    public void test_differentUrls_properHash() throws Throwable
    {
        test(FOO_URL, BAR_URL + "#aaa", false);
    }

    @Test
    public void testSamePage_noHash() throws Throwable
    {
        test(FOO_URL, FOO_URL, true);
    }

    @Test
    public void testSamePage_justHash() throws Throwable
    {
        test(FOO_URL, FOO_URL + "#", true);
    }

    @Test
    public void testSamePage_properHash() throws Throwable
    {
        test(FOO_URL, FOO_URL + "#aaa", true);
    }

    private void test(String urlString1, String urlString2, boolean hashJump) throws Throwable
    {
        // set up mock web connection
        URL url1 = new URL(urlString1);
        URL url2 = new URL(urlString2);

        final String title1 = "Page 1";
        final String title2 = "Page 2";
        String page1 = "<html><head><title>" + title1 + "</title></head><body><a id='anchor' href='" + url2 + "'>Go</a></body></html>";
        String page2 = "<html><head><title>" + title2 + "</title></head><body>Got there!</body></html>";

        MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url2, page2);
        conn.setResponse(url1, page1);

        // set up web client
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.setWebConnection(conn);

            // test
            HtmlPage htmlPage = webClient.getPage(url1);
            // System.out.println(htmlPage.asXml());

            HtmlElement a = htmlPage.getHtmlElementById("anchor");
            htmlPage = a.click();
            // System.out.println(htmlPage.asXml());

            final String expectedText = hashJump ? title1 : title2;
            Assert.assertEquals(expectedText, htmlPage.getTitleText());
        }
    }
}
