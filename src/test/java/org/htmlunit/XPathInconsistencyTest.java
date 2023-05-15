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

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test case shows the different handling of error in case an element is not found by getByXPath()
 * 
 * @author Rene Schwietzke
 */
public class XPathInconsistencyTest
{
    @Test
    public void overwrite() throws Exception
    {
        final String p1 = "<html>" + "<head>" + "<title>1</title>" + "</head>" + "<body>" + "<div><a href=\"link.html\" id=\"test\"></div>"
                          + "</body>" + "</html>";

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            wc.setWebConnection(conn);

            conn.setResponse(new URL("http://myserver/1.html"), p1);

            final HtmlPage initialPage = (HtmlPage) wc.getPage("http://myserver/1.html");

            // return empty list
            final List<?> xPathByAttributeCompare = initialPage.getByXPath("//div[@id='doesNotExist']");
            Assert.assertTrue(xPathByAttributeCompare.isEmpty());

            // causes exception
            try
            {
                final List<?> xPathByID = initialPage.getByXPath("id('doesNotExist')");
                Assert.assertTrue(xPathByID.isEmpty());
            }
            catch (final ElementNotFoundException e)
            {
                Assert.fail("Undesired exception. Empty list expected");
            }
        }
    }
}
