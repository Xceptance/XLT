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

import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.host.ClientRect;
import org.htmlunit.javascript.host.html.HTMLElement;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.AbstractWebTestCase;

/**
 * Tests that the bounding box of HTML file inputs have non-zero height and width.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2144_FileInputHasNonZeroWidthTest extends AbstractWebTestCase
{
    @Test
    public void testNonZeroWidth() throws Throwable
    {
        final String html = "<html><body><input type='file' value='' name='foo' /></body></html>";
        final HtmlPage page = loadPage(html);
        final HtmlElement fileInput = page.getFirstByXPath(".//input[@type='file']");

        final HTMLElement scritable = (HTMLElement) fileInput.getScriptableObject();
        final ClientRect rectum;
        try
        {
            Context.enter();
            rectum = scritable.getBoundingClientRect();
        }
        finally
        {
            Context.exit();
        }

        // assure that element has non-zero height
        Assert.assertTrue("Height of file input is 0", rectum.getHeight() > 0);
        // assure that element has non-zero width
        Assert.assertTrue("Width of file input is 0", rectum.getWidth() > 0);
    }
}
