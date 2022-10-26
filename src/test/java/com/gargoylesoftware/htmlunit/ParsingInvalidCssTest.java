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

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.css.ComputedCSSStyleDeclaration;
import com.gargoylesoftware.htmlunit.javascript.host.css.StyleAttributes.Definition;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

/**
 * See http://sourceforge.net/tracker/?func=detail&aid=3136642&group_id=47038&atid=448266 and #566.
 */
public class ParsingInvalidCssTest
{
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final HtmlPage page = wc.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));

            final HtmlElement e = (HtmlElement) page.getByXPath("//h1").get(0);
            final ComputedCSSStyleDeclaration styleDec = ((HTMLElement) e.getScriptableObject()).getCurrentStyle();
            Assert.assertEquals("url(\"someFile.jpg\") top right", styleDec.getStyleAttribute(Definition.BACKGROUND));
        }
    }
}
