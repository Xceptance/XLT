/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.net.URL;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test case tries to provoke an overwrite of the page by the previous page when using javascript and the location
 * feature.
 * 
 * @author Rene Schwietzke
 */
public class LocationOverwriteTest
{
    @Test
    public void overwrite() throws Exception
    {
        BasicConfigurator.configure();
        final String p1 = "<html>"
                          + "<head>"
                          + "<title>1</title>"
                          + "<script>"
                          + "function changeLocation()"
                          + "{ "
                          + "window.aMenu = \"location='valid.html'\";"
                          + "eval(window.aMenu);"
                          + "}"
                          + "</script>"
                          + "</head>"
                          + "<body>"
                          + "<script>document.write('<a href=\"http://myserver/wrong.html\" onclick=\"changeLocation(); return false;\" id=\"test\">');</script>"
                          + "</body>" + "</html>";

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            webClient.setWebConnection(conn);

            conn.setResponse(new URL("http://myserver/1.html"), p1);
            conn.setResponseAsGenericHtml(new URL("http://myserver/valid.html"), "valid");
            conn.setResponseAsGenericHtml(new URL("http://myserver/wrong.html"), "wrong");

            final HtmlPage initialPage = (HtmlPage) webClient.getPage("http://myserver/1.html");
            final List<?> testLinks = initialPage.getByXPath("id('test')");

            Assert.assertNotNull(testLinks);
            Assert.assertEquals(1, testLinks.size());

            final HtmlElement link = (HtmlElement) testLinks.get(0);
            final HtmlPage targetPage = (HtmlPage) link.click();

            Assert.assertEquals("valid", targetPage.getTitleText());
        }
    }
}
