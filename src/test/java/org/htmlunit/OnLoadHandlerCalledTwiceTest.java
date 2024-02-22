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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Shows that a JavaScript is loaded twice in total if it is already loaded on before the page is completely loaded.
 * This was a bug in HtmlUnit that was finally resolved with HtmlUnit2.9 and related to our issue <a href="
 * https://lab.xceptance.de/issues/578">578</a>
 */
public class OnLoadHandlerCalledTwiceTest
{
    @Test
    public void testWithFrame() throws Exception
    {
        test("_frame.html");
    }

    @Test
    public void testWithIFrame() throws Exception
    {
        test("_iframe.html");
    }

    private void test(final String suffix) throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            webClient.getOptions().setJavaScriptEnabled(true);

            final List<String> alerts = new ArrayList<String>();
            webClient.setAlertHandler(new CollectingAlertHandler(alerts));

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + suffix);
            webClient.getPage(url);

            // check whether the expected values are returned
            Assert.assertEquals("Onload handler called twice!", 1, alerts.size());
        }
    }
}
