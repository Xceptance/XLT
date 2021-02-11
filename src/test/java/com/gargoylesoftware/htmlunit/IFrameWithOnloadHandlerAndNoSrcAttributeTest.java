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
import org.junit.Test;

/**
 * See https://sourceforge.net/tracker/?func=detail&atid=448266&aid=3406772&group_id=47038 and #1373.
 */
public class IFrameWithOnloadHandlerAndNoSrcAttributeTest
{
    @Test
    public void firefox_jquery() throws Exception
    {
        test(BrowserVersion.FIREFOX, "_jquery.html", true);
    }

    @Test
    public void firefox_plain() throws Exception
    {
        test(BrowserVersion.FIREFOX, "_plain.html", true);
    }

    @Test
    public void ie_jquery() throws Exception
    {
        test(BrowserVersion.INTERNET_EXPLORER, "_jquery.html", true);
    }

    @Test
    public void ie_plain() throws Exception
    {
        test(BrowserVersion.INTERNET_EXPLORER, "_plain.html", true);
    }

    private void test(final BrowserVersion browserVersion, final String ext, final boolean handlerShouldHaveBeenCalled) throws Exception
    {
        try (final WebClient webClient = new WebClient(browserVersion))
        {
            final CollectingAlertHandler alertHandler = new CollectingAlertHandler();
            webClient.setAlertHandler(alertHandler);

            final URL url = getClass().getResource(getClass().getSimpleName() + ext);
            webClient.getPage(url);

            Assert.assertEquals(handlerShouldHaveBeenCalled, alertHandler.getCollectedAlerts().size() == 1);
        }
    }
}
