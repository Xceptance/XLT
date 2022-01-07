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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test shows that the attribute values of HTML elements, which have been cloned via JavaScript, are always empty
 * on JavaScript level. On DOM level, however, they are available as expected.
 */

public class AttributeValuesLostAfterCloningElementTest
{
    @Test
    public void test() throws Exception
    {
        // setup
        try (final WebClient webClient = new WebClient())
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final List<String> alerts = new ArrayList<String>();
            webClient.setAlertHandler(new CollectingAlertHandler(alerts));

            // clone a div via JS
            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");
            final HtmlPage page = (HtmlPage) webClient.getPage(url);

            // System.out.println(page.asXml());

            // check whether the attribute is available on DOM level
            final HtmlElement element = page.getHtmlElementById("bar");
            final String value = element.getAttribute("id");
            Assert.assertEquals("bar", value);

            // check whether the attribute was available to JavaScript code
            Assert.assertEquals("id = bar", alerts.get(0));
        }
    }
}
