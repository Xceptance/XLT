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
import org.junit.Ignore;
import org.junit.Test;

public class _689_TagBalancingTest
{
    @Test
    public void testQuirksModeParagraph() throws Exception
    {
        test("quirks-paragraph", "/html/body/p/table");
    }

    @Test
    public void testQuirksModeAnchor() throws Exception
    {
        test("quirks-anchor", "/html/body/a/table");
    }

    @Test
    @Ignore("HtmlUnit v2.21 or higher not integrated yet")
    public void testStandardModeParagraph() throws Exception
    {
        test("std-paragraph", "/html/body/table");
    }

    private void test(String suffix, String xpath) throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "-" + suffix + ".html");
            final HtmlPage page = webClient.getPage(url);
            
            // make the check
            final List<?> result = page.getByXPath(xpath);
            Assert.assertFalse("No element not found for xpath: " + xpath, result.isEmpty());
        }
    }
}
