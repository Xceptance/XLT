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
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

public class HtmlSelectTest
{
    /**
     * #912
     */
    @Test
    public void checkPageAfterSetSelectedAttribute() throws Exception
    {
        HtmlPage htmlPage = init();

        final HtmlSelect select = ((HtmlSelect) htmlPage.getHtmlElementById("select"));
        htmlPage = (HtmlPage) select.setSelectedAttribute("open", true);
        Assert.assertEquals("Google", htmlPage.getTitleText());
    }

    /**
     * #912
     */
    @Test
    public void checkPageAfterClick() throws Exception
    {
        HtmlPage htmlPage = init();

        final HtmlInput input = ((HtmlInput) htmlPage.getHtmlElementById("input"));
        htmlPage = (HtmlPage) input.click();
        Assert.assertEquals("Google", htmlPage.getTitleText());
    }

    private HtmlPage init() throws IOException
    {
        final String googleContent = "<html><head><title>Google</title></head><body></body></html>";
        final URL url = getClass().getResource(getClass().getSimpleName() + ".html");
        final URL googleURL = new URL("http://www.google.com/");

        // use mocked web connection to assure we get the right page
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, IOUtils.toString(url.openStream(), StandardCharsets.UTF_8));
        conn.setResponse(googleURL, googleContent);

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);
            webClient.setWebConnection(conn);

            final HtmlPage htmlPage = webClient.getPage(url);

            return htmlPage;
        }
    }
}
