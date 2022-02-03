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

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Sometimes, frame content is not loaded from the web, but completely generated via JavaScript. If there is a relative
 * URL in the generated content, an exception is thrown when HtmlUnit tries to make this URL absolute (#340).
 */
public class UrlExpansionTest
{
    @Test
    public void test() throws Exception
    {
        // ~~~ setup ~~~
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final URL url = getClass().getResource("UrlExpansionTest_1.html");
            // final URL url = new URL("http://localhost/UrlExpansionTest_1.html");

            // ~~~ test ~~~
            final HtmlPage page = webClient.getPage(url);
            // System.out.println(page.asXml());

            HtmlPage framePage = (HtmlPage) page.getFrameByName("f").getEnclosedPage();
            // System.out.println(framePage.asXml());

            final HtmlForm form = framePage.getFormByName("form");
            final HtmlInput submit = form.getInputByName("submit");
            framePage = submit.click();
            // System.out.println(framePage.asXml());

            Assert.assertEquals("Form submitted successfully.", framePage.asNormalizedText());
        }
    }
}
