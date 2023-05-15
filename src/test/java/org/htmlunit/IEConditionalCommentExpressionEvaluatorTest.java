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

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

public class IEConditionalCommentExpressionEvaluatorTest
{
    @Test
    public void test() throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER))
        {
            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");
            final HtmlPage htmlPage = webClient.getPage(url);

            // check whether there are still IE conditional expression artifacts on
            // the page

            System.err.println(htmlPage.asXml());
            Assert.assertFalse("IE conditional expression artifacts found", htmlPage.asXml().contains("[if IE]&gt;"));
        }
    }
}
