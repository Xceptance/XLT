/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript.polyfill;

import java.net.URL;

import org.htmlunit.SimpleWebTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for polyfill support.
 *
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class PolyfillTest extends SimpleWebTestCase {

    @Test
    @Alerts(DEFAULT = "Content fetched",
            IE = {})
    public void fetch() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (typeof fetch == 'function') {\n"
            + "    fetch('fetch.txt')\n"
            + "      .then(response => response.text())\n"
            + "      .then(data => alert(data));\n"
            + "  }\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'></body></html>";

        final URL fetchUrl = new URL(URL_FIRST, "fetch.txt");
        getMockWebConnection().setResponse(fetchUrl, "Content fetched");

        getWebClientWithMockWebConnection().getOptions().setFetchPolyfillEnabled(true);
        loadPageWithAlerts(html, URL_FIRST, (int) DEFAULT_WAIT_TIME);
    }

    @Test
    @Alerts("false")
    public void fetchPolyfillDisabled() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    alert(typeof fetch == 'function');\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'></body></html>";

        final URL fetchUrl = new URL(URL_FIRST, "fetch.txt");
        getMockWebConnection().setResponse(fetchUrl, "Content fetched");

        loadPageWithAlerts(html);
    }
}
