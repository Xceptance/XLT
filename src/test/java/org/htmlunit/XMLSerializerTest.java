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
 * Testcase which demonstrates issue #1130.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XMLSerializerTest
{
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);

            final String response = "<?xml version=\"1.0\" ?>\n" + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                                    + "<head><title>TEST</title></head>\n" + "<body></body>\n" + "</html>\n";
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(response);

            wc.setWebConnection(conn);

            final String script = "var t = document.createElement('textarea'); new XMLSerializer().serializeToString(t);";
            final HtmlPage page = wc.getPage("http://www.example.org");
            final ScriptResult result = page.executeJavaScript(script);
            Assert.assertEquals("<textarea xmlns=\"http://www.w3.org/1999/xhtml\"></textarea>", result.getJavaScriptResult());
        }
    }
}
