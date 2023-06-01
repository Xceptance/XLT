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

import org.htmlunit.corejs.javascript.ScriptableObject;
import org.junit.Test;

/**
 * Tests the correct handling of trying to set parent scope of a window object to itself. In this case,
 * {@link ScriptableObject#getTopLevelScope(net.sourceforge.htmlunit.corejs.javascript.Scriptable)} would hang in an
 * infinite loop.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class CallObjectWithWindowTest
{

    @Test(timeout = 5000)
    public void testCallObjectWithWindow() throws Throwable
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);

            final String html = "<html><head><title>Test Page</title></head><body>" + "<script type=\"application/javascript\">"
                                + "Object(window)" + "</script>" + "</body></html>";

            final MockWebConnection webConnection = new MockWebConnection();
            webClient.setWebConnection(webConnection);
            webConnection.setDefaultResponse(html);

            webClient.getPage("http://www.example.org");
        }
    }
}
