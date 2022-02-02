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
package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.util.XltLogger;

/**
 * Tests the implementation of image load handlers.
 */
public class ImageOnloadHandlerTest implements AlertHandler
{
    private final ArrayList<String> alerts = new ArrayList<String>();

    @Test
    public void test1379() throws Throwable
    {
        final byte[] htmlFile = IOUtils.toByteArray(getClass().getResourceAsStream(getClass().getSimpleName() + ".html"));
        final byte[] firstImage = IOUtils.toByteArray(getClass().getResourceAsStream("a.gif"));

        final URL startUrl = new URL("http://www.example.org/foobar.html");

        final MockWebConnection mock = new MockWebConnection();
        mock.setResponse(startUrl, htmlFile, 200, "OK", "text/html", Collections.<NameValuePair>emptyList());
        mock.setDefaultResponse(firstImage, 200, "OK", "image/gif");

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            wc.setWebConnection(mock);
            wc.setAlertHandler(this);

            // load page
            final HtmlPage page = wc.getPage(startUrl);

            Assert.assertTrue("List of alerts should be empty", alerts.isEmpty());

            // validate request count
            final int nbPix = Integer.parseInt(RegExUtils.getFirstMatch(page.getHtmlElementById("pixNumDiv").getTextContent(), "\\d+"));
            XltLogger.runTimeLogger.debug(String.format("Number of pictures: %d, Request count: %d", nbPix, mock.getRequestCount()));
            Assert.assertEquals(nbPix + 1, mock.getRequestCount());
        }
    }

    @Test
    public void test1624() throws Throwable
    {
        final URL startUrl = new URL("http://www.example.org/foobar.html");

        final String htmlMarkUp = "<html><body>" + "<img src=\"someImageA.gif\" onclick=\"this.setAttribute('src','anotherImage.gif')\" "
                                  + "     onload=\"alert('Loaded image from: ' + this.src)\" />" + "</body></html>";
        final MockWebConnection mock = new MockWebConnection();
        mock.setResponse(startUrl, htmlMarkUp, 200, "OK", "text/html", Collections.<NameValuePair>emptyList());
        mock.setDefaultResponse(new byte[0], 200, "OK", "image/gif");

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            wc.setWebConnection(mock);
            wc.setAlertHandler(this);

            // load page
            final HtmlPage page = wc.getPage(startUrl);

            // click on image -> invoke onclick handler that sets the src attribute to a different value
            ((HtmlElement) page.getByXPath("//img").get(0)).click();

            Assert.assertEquals("Invalid list of alerts :: " + Arrays.toString(alerts.toArray(new String[alerts.size()])), 2, alerts.size());
        }
    }

    @After
    public void clearAlerts()
    {
        alerts.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAlert(final Page page, final String message)
    {
        alerts.add(message);
    }
}
