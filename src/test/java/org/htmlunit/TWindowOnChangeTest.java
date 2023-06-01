/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
 * Tests that 'window.onchange' is a valid property (not <code>undefined</code>).
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TWindowOnChangeTest
{
    @Test
    public void testWindowOnchange() throws Throwable
    {
        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setDefaultResponse("<html><head><title>TEST PAGE</title></head><body></body></html>");

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            wc.setWebConnection(webConnection);

            final HtmlPage page = wc.getPage("http://www.example.org");
            Assert.assertTrue("window.onchange is undefined", ((Boolean) page.executeJavaScript("'onchange' in window")
                                                                             .getJavaScriptResult()).booleanValue());
        }
    }
}
