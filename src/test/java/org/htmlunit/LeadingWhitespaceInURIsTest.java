/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import org.junit.Test;

/**
 * Tests whether URIs with leading whitespace are handled correctly.
 * 
 * @see https://sourceforge.net/p/htmlunit/bugs/627/
 * @see https://sourceforge.net/p/htmlunit/bugs/1728/
 * @see https://lab.xceptance.de/issues/2547
 */
public class LeadingWhitespaceInURIsTest
{
    @Test
    public void test() throws Exception
    {
        final String html = "<html><head>" + "<base href='\nhttp://localhost/'>"
                            + "<script type='text/javascript' src='\nhttp://localhost/script.js'></script>"
                            + "<script type='text/javascript' src='\nscript.js'></script>" + "</head><body>foo</body></html>";
        final String js = "";

        // setup
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final MockWebConnection connection = new MockWebConnection();
            webClient.setWebConnection(connection);

            connection.setResponse(new URL("http://localhost/index.html"), html);
            connection.setResponse(new URL("http://localhost/script.js"), js);

            // test
            webClient.getPage("http://localhost/index.html");
        }
    }
}
