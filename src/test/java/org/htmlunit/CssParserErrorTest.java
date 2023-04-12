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
package org.htmlunit;

import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.host.html.HTMLElement;
import org.junit.Assert;
import org.junit.Test;

/**
 * Demonstrates an error in the CSS parser.
 */
public class CssParserErrorTest
{
    @Test
    public void testInvalidCSS() throws Throwable
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final HtmlPage htmlPage = webClient.getPage(getClass().getResource("CssParserErrorTest.html"));

            // check the style
            final HTMLElement el = (HTMLElement) htmlPage.getElementById("theme1").getScriptableObject();
            final String bgImgUrl = el.getCurrentStyle().getBackgroundImage();
            Assert.assertEquals("Invalid background image URL: " + bgImgUrl, "url(\"1.jpg\")", bgImgUrl);
        }
    }
}
