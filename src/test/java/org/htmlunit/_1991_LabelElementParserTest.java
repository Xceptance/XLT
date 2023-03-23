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
package org.htmlunit;

import java.util.List;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.AbstractWebTestCase;

/**
 * Tests the correct parsing of HTML label elements. See issue 1991 for details.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _1991_LabelElementParserTest extends AbstractWebTestCase
{
    @Test
    public void parseInline() throws Throwable
    {
        final String html = "<html>\n<body>\n<a href=\"http://foo.com\">\n<label>\nXL\n</label>\n</a>\n</body>\n</html>";
        final HtmlPage page = loadPage(html);
        final String xpathExpr = "/html/body/a/label";
        final List<?> labels = page.getByXPath(xpathExpr);
        Assert.assertFalse("No such element: " + xpathExpr,labels.isEmpty());
    }

    @Test
    public void parseBlock() throws Throwable
    {
        final String html = "<html>\n<body>\n<label><div>foo</div></label></html>";
        final HtmlPage page = loadPage(html);
        final String xpathExpr = "/html/body/label/div";
        final List<?> labels = page.getByXPath(xpathExpr);
        Assert.assertFalse("No such element: " + xpathExpr,labels.isEmpty());
    }

}
