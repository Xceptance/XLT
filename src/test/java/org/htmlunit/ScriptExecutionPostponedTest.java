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

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test that assures postponed execution of &lt;script&gt; elements. See issue #2089 for details.
 */
public class ScriptExecutionPostponedTest
{
    @Test
    public void test() throws Throwable
    {
        final URL u1 = new URL("http://www.example.org/");
        final URL u2 = new URL("http://www.example.org/foo.js");
        final URL u3 = new URL("http://www.example.org/bar.js");
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(u1,
                         "<html><body><h1>Hello World!</h1>"
                             + "<script>"
                             + "window.onload = function(){ var e = document.createElement('script'); e.src = 'foo.js'; document.body.appendChild(e); }"
                             + "</script></body></html>");
        conn.setResponse(u2,
                         "(function(app){"
                             + "function f(){ var e = document.createElement('script');e.src='bar.js'; document.body.appendChild(e); this._created = true }"
                             + "app.TestObj = new f();" + "})(window.app = window.app || {})", "text/javascript");
        conn.setResponse(u3, "(function(){ alert(window.app.TestObj._created) })()", "text/javascript");

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);
            wc.setWebConnection(conn);

            final CollectingAlertHandler alertHandler = new CollectingAlertHandler();
            wc.setAlertHandler(alertHandler);

            wc.getPage(u1);

            final List<String> alerts = alertHandler.getCollectedAlerts();
            Assert.assertEquals(1, alerts.size());
            Assert.assertEquals("true", alerts.get(0));
        }
    }
}
