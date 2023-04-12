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
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for issue <a href="https://lab.xceptance.de/issues/2859">#2859</a>.
 */
public class _2859_MutationObserverTest
{

    /**
     * Executes the test.
     *
     * @throws Throwable
     *             if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);

            final HtmlPage page = wc.getPage(getClass().getResource(getClass().getSimpleName()+".html"));
            page.executeJavaScript("document.getElementById('headline').style = 'color:red'");

            Assert.assertFalse("Mutation observer callback was NOT invoked", page.getByXPath("/html/body/p").isEmpty());
        }
    }
}