/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.AbstractWebTestCase;

/**
 * Tests the implementation of {@link HtmlImage#isComplete()}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2082_ImageCompleteTest extends AbstractWebTestCase
{
    @Test
    public void testSrcNotPresent() throws Throwable
    {
        final String html = "<html><head><title></title></head><body>" + "<img /></body></html>";
        doTest(html, true);
    }

    @Test
    public void testSrcEmpty() throws Throwable
    {
        final String html = "<html><head><title></title></head><body onload='document.body.firstChild.width'>" +
                            "<img src=''/></body></html>";
        
        // Since FIREFOX_60, HtmlUnit doesn't even try to load an image when src='', hence isComplete() will return false.
        doTest(html, false);
    }

    @Test
    public void testSrcDoesNotExist() throws Throwable
    {
        final String notFoundHtml = "<html><head><title>Not Found</title></head><body><h1>We're sorry!</h1>The requested resource could not be found</body></html>";
        getMockConnection().setDefaultResponse(notFoundHtml, 404, "Not Found", "text/html");

        final String html = "<html><head><title></title></head><body>" + "<img src='/fooBar.jpg'/></body></html>";
        doTest(html, true);
    }

    @Test
    public void testImageBroken() throws Throwable
    {
        final Random rd = new Random();

        final byte[] imgContent = new byte[1024];
        rd.nextBytes(imgContent);
        getMockConnection().setDefaultResponse(imgContent, 200, "OK", "image/jpeg");

        final String html = "<html><head><title></title></head><body>" + "<img src='/fooBar.jpg'/></body></html>";
        doTest(html, true);
    }

    @Test
    public void testImageFine() throws Throwable
    {
        try (final InputStream is = getClass().getResourceAsStream("a.gif"))
        {
            final byte[] imgContent = IOUtils.toByteArray(is);

            getMockConnection().setDefaultResponse(imgContent, 200, "OK", "image/jpeg");

            final String html = "<html><head><title></title></head><body>" + "<img src='/fooBar.jpg'/></body></html>";
            doTest(html, true);
        }
    }

    private void doTest(final String html, boolean expectedIsComplete) throws Throwable
    {
        final HtmlPage page = loadPage(html);
        final List<?> list = page.getByXPath("/html/body/img");
        Assert.assertFalse(list.isEmpty());

        final HtmlImage img = (HtmlImage) list.get(0);
        Assert.assertEquals(expectedIsComplete, img.isComplete());
    }
}
