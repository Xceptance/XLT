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
import java.util.List;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @see https://lab.xceptance.de/issues/1767
 * @see http://sourceforge.net/p/htmlunit/bugs/1498/
 */
public class _1767_JQueryMobileTest
{
    private HtmlPage page;

    private WebClient webClient;

    private CollectingAlertHandler alertHandler;

    @Before
    public void setup() throws Throwable
    {
        webClient = new WebClient(BrowserVersion.CHROME);
        alertHandler = new CollectingAlertHandler();
        webClient.setAlertHandler(alertHandler);

        /*
         * Get the page as it was deployed on localhost
         */
        final URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".html");
        page = webClient.getPage(url);
    }

    @Test
    public void testPageInitEvent() throws Throwable
    {
        final List<String> alerts = alertHandler.getCollectedAlerts();
        Assert.assertFalse("'pageinit' event not triggered", alerts.isEmpty());
    }

    @Test
    public void testGradeA() throws Throwable
    {
        final Boolean isGradeA = (Boolean) page.executeJavaScript("jQuery.mobile.gradeA()").getJavaScriptResult();
        Assert.assertTrue("JQuery mobile grade A classification failed", isGradeA);
    }

    @Test
    public void testMatchMedia() throws Throwable
    {
        final Boolean haveMatchMedia = (Boolean) page.executeJavaScript("'matchMedia' in window").getJavaScriptResult();
        Assert.assertTrue("Browser doesn't support CSS 3 Media Queries", haveMatchMedia);
    }
}
