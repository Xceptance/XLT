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

import org.htmlunit.html.BaseFrameElement;
import org.htmlunit.html.FrameWindow;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

/**
 * See https://sourceforge.net/tracker/?func=detail&atid=448266&aid=3083847&group_id=47038 and #1034.
 */
public class FrameWindowsFrameElementInconsistentTest
{
    @Test
    public void test() throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + ".html");
            final HtmlPage page = webClient.getPage(url);

            // System.out.println(page.asXml());

            // test
            final BaseFrameElement frameElement = page.getHtmlElementById("iframe");
            final BaseFrameElement frameElement2 = ((FrameWindow) frameElement.getEnclosedWindow()).getFrameElement();

            // System.out.println(frameElement);
            // System.out.println(frameElement.getParentNode());
            //
            // System.out.println(frameElement2);
            // System.out.println(frameElement2.getParentNode());

            Assert.assertEquals(frameElement.hashCode(), frameElement2.hashCode());
        }
    }
}
