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

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test shows that cloning an HtmlPage object with (i)frames causes HtmlUnit to load the frame source from the
 * server once again.
 */

public class ClonePageWithFramesTest
{
    @Test
    public void testClonePageWithFramesDoesNotHitServer() throws Exception
    {
        final String outerHtml = "<html><head></head><frameset cols=\"100%\"><frame src=\"inner.html\"/></frameset></html>";
        final String innerHtml = "<html><head></head><body>foo</body></html>";

        testClonePage(outerHtml, innerHtml);
    }

    @Test
    public void testClonePageWithIFramesDoesNotHitServer() throws Exception
    {
        final String outerHtml = "<html><head></head><body><iframe src=\"inner.html\"/></iframe></body></html>";
        final String innerHtml = "<html><head></head><body>foo</body></html>";

        testClonePage(outerHtml, innerHtml);
    }

    private void testClonePage(final String outerHtml, final String innerHtml) throws Exception
    {
        // setup
        BasicConfigurator.configure();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final TestWebConnection connection = new TestWebConnection();
            webClient.setWebConnection(connection);

            connection.setResponse(new URL("http://localhost/outer.html"), outerHtml);
            connection.setResponse(new URL("http://localhost/inner.html"), innerHtml);

            // test
            final HtmlPage page = (HtmlPage) webClient.getPage("http://localhost/outer.html");

            page.cloneNode(true);
        }
    }

    /**
     * A WebConnection implementation that fails when loading a URL more than once.
     */

    static class TestWebConnection extends MockWebConnection
    {
        private final HashSet<String> urls = new HashSet<String>();

        @Override
        public WebResponse getResponse(final WebRequest webRequest) throws IOException
        {
            final String url = webRequest.getUrl().toString();

            if (urls.contains(url))
            {
                Assert.fail("URL loaded more than once: " + url);
            }

            urls.add(url);

            return super.getResponse(webRequest);
        }
    }
}
