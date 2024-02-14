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
package org.htmlunit.javascript.host.html;

import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.htmlunit.Page;
import org.htmlunit.SimpleWebTestCase;
import org.htmlunit.StatusHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.junit.BrowserRunner;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link HTMLAppletElement}.
 * @author Marc Guillemot
 * @author Daniel Gredler
 */
@RunWith(BrowserRunner.class)
public class HTMLAppletElement2Test extends SimpleWebTestCase {

    private static boolean SKIP_ = false;

    static {
        if (GraphicsEnvironment.isHeadless()) {
            // skip the tests in headless mode
            SKIP_ = true;
        }
    }

    /**
     * Tests calling applet method from JavaScript code.
     * @throws Exception if the test fails
     */
    @Test
    public void callAppletMethodFromJS() throws Exception {
        Assume.assumeFalse(SKIP_);

        if (getBrowserVersion().isChrome()
                || getBrowserVersion().isEdge()
                || getBrowserVersion().isFirefox()) {
            return;
        }

        final URL url = getClass().getResource("/applets/simpleAppletDoIt.html");

        final WebClient webClient = getWebClientWithMockWebConnection();
        final List<String> collectedStatus = new ArrayList<>();
        final StatusHandler statusHandler = new StatusHandler() {
            @Override
            public void statusMessageChanged(final Page page, final String message) {
                collectedStatus.add(message);
            }
        };
        webClient.setStatusHandler(statusHandler);
        webClient.getOptions().setAppletEnabled(true);

        final HtmlPage page = webClient.getPage(url);

        final HtmlButton button1 = page.getHtmlElementById("button1");
        button1.click();

        final HtmlButton button2 = page.getHtmlElementById("button2");
        button2.click();

        final String[] expectedStatus = {"Called: doIt('hello')", "Called: doIt('12345')"};
        assertEquals(expectedStatus, collectedStatus);
    }
}
