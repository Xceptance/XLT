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

import java.io.IOException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class _2912_InvalidNumericCharacterReferenceTest
{
    // The offending text. Should actually be "Nimbus&#8482; 3000 is great".
    private static final String text = "Nimbus&#84823000 is great";

    @Test
    public void invalidCharacterReferenceInAttributeValue() throws FailingHttpStatusCodeException, IOException
    {
        String pageContent = "<p data-desc='" + text + "'></p>";

        test(pageContent);
    }

    @Test
    public void invalidCharacterReferenceInElementBody() throws FailingHttpStatusCodeException, IOException
    {
        String pageContent = "<p>" + text + "</p>";

        test(pageContent);
    }

    private void test(String pageContent) throws FailingHttpStatusCodeException, IOException
    {
        MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(pageContent);

        try (WebClient wc = new WebClient())
        {
            wc.setWebConnection(conn);

            HtmlPage page = wc.getPage("http://dummyhost/");

            System.out.println(page.asXml());
        }
    }
}
