/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.util.Arrays;
import java.util.Collection;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @see https://lab.xceptance.de/issues/2430
 * @see http://sourceforge.net/p/htmlunit/bugs/1685/
 */
@RunWith(Parameterized.class)
public class _2430_FindByCssClassTest
{
    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
            {
                    {
                        "foo"
                    },
                    {
                        "foo "
                    },
                    {
                        " foo"
                    },
                    {
                        "\tfoo"
                    },
                    {
                        "foo\t"
                    },
                    {
                        "\nfoo"
                    },
                    {
                        "foo\n"
                    },
                    {
                        "\tfoo\n"
                    },
                    {
                        "\nfoo\t"
                    },
            });
    }

    @Parameter
    public String classAttributeValue;

    @Test
    public void test() throws Throwable
    {
        // set up mock response
        String page = "<div class='" + classAttributeValue + "'></div>";

        MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(page);

        // set up web client
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.setWebConnection(conn);

            // test
            HtmlPage htmlPage = webClient.getPage("http://dummy.net");
            Assert.assertEquals("Unexpected number of matches:", 1, htmlPage.querySelectorAll(".foo").size());
        }
    }
}
