/*
 * Copyright (c) 2002-2022 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.libraries;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;
import com.gargoylesoftware.htmlunit.junit.BrowserRunner;
import com.gargoylesoftware.htmlunit.junit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.util.MimeType;

/**
 * Tests that depend on one of JavaScript libraries.
 *
 * @author Ahmed Ashour
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class LibraryDependencyTest extends WebDriverTestCase {

    /**
     * Test for http://sourceforge.net/p/htmlunit/bugs/637/.
     * @throws Exception if the test fails
     */
    @Alerts("2")
    @Test
    public void contextFactory_Browser() throws Exception {
        final String firstHtml =
            "<html>\n"
            + "<head>\n"
            + "  <title>1</title>\n"
            + "  <script src='" + URL_THIRD + "' type='text/javascript'></script>\n"
            + "</head>\n"
            + "<body>\n"
            + "<script>\n"
            + "  setTimeout(finishCreateAccount, 2500);\n"
            + "  function finishCreateAccount() {\n"
            + "    completionUrl = '" + URL_SECOND + "';\n"
            + "    document.location.replace(completionUrl);\n"
            + "  }\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>";
        final String secondHtml =
            "<html>\n"
            + "<head>\n"
            + "  <title>2</title>\n"
            + "  <script src='" + URL_THIRD + "' type='text/javascript'></script>\n"
            + "</head>\n"
            + "<body onload='alert(2)'>\n"
            + "<div id='id2'>Page2</div>\n"
            + "</body>\n"
            + "</html>";
        final String prototype = getContent("libraries/prototype/1.6.0/dist/prototype.js");

        final MockWebConnection webConnection = getMockWebConnection();
        webConnection.setResponse(URL_SECOND, secondHtml);
        webConnection.setResponse(URL_THIRD, prototype, MimeType.APPLICATION_JAVASCRIPT);

        loadPageWithAlerts2(firstHtml, 10_000);
    }

    private String getContent(final String resourceName) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return IOUtils.toString(in, ISO_8859_1);
        }
    }
}
