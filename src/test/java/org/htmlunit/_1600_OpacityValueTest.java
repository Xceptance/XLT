/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
import java.util.ArrayList;
import java.util.List;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.host.html.HTMLElement;
import org.junit.Assert;
import org.junit.Test;

/**
 * See http://sourceforge.net/tracker/index.php?func=detail&aid=3534330&group_id=47038&atid=448266 and #1600.
 */
public class _1600_OpacityValueTest
{
    @Test
    public void ff45() throws Exception
    {
        test(new WebClient(BrowserVersion.FIREFOX), "0.55");
    }

    @Test
    public void ie() throws Exception
    {
        test(new WebClient(BrowserVersion.INTERNET_EXPLORER), "0.55");
    }

    @Test
    public void chrome() throws Exception
    {
        test(new WebClient(BrowserVersion.CHROME), "0.55");
    }

    private void test(final WebClient webClient, final String expectedOpacityValue) throws Exception
    {
        // setup
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(true);

        final List<String> alerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(alerts));

        // get the page
        final URL url = getClass().getResource(getClass().getSimpleName() + ".html");
        final HtmlPage htmlPage = webClient.getPage(url);

        // test
        final HtmlElement e = htmlPage.getHtmlElementById("div");
        final String opacity = ((HTMLElement) e.getScriptableObject()).getStyle().getOpacity();

        Assert.assertEquals(expectedOpacityValue, opacity);
    }
}
