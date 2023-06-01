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

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @see https://lab.xceptance.de/issues/2053
 * @see https://sourceforge.net/p/htmlunit/bugs/1577/
 */
public class _2053_ExcessiveSocketUsageTest
{
    @Test
    @Ignore("To be run manually only")
    public void testConnectionsAreClosedWhenWebClientIsClosed() throws IOException
    {
        for (int i = 0; i < 10000; i++)
        {
            System.err.printf("### %d\n", i);

            WebClient webClient = new WebClient(BrowserVersion.CHROME);

            webClient.getPage("http://localhost:8080/posters");
            // System.err.printf("### %s\n", ((HtmlPage)webClient.getCurrentWindow().getEnclosedPage()).asXml());

            webClient.close();
        }
    }

    @Test
    @Ignore("To be run manually only")
    public void testConnectionsAreReusedForSubsequentRequests() throws IOException
    {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        for (int i = 0; i < 10000; i++)
        {
            System.err.printf("### %d\n", i);

            webClient.getPage("http://localhost:8080/posters");
            // System.err.printf("### %s\n", ((HtmlPage)webClient.getCurrentWindow().getEnclosedPage()).asXml());
        }

        webClient.close();
    }
}
