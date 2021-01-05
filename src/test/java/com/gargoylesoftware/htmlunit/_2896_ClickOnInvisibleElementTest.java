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
/*
 * File: _2896_ClickOnInvisibleElementTest.java
 * Created on: Apr 10, 2017
 */
package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Test for issue 2896 (invisible elements honor click event)
 */
@RunWith(Parameterized.class)
public class _2896_ClickOnInvisibleElementTest
{
    @Parameter
    public String html;

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
            {
                {
                    "<div style='display:none'><a href='http://foobar.com'>Click me</a></div>"
                },
                {
                    "<div style='visibility:hidden'><a href='http://foobar.com'>Click me</a></div>"
                },
                {
                    "<style>.inv { display:none }</style><div><p class=inv>Some text here...<a href='http://foobar.com'>Click me</a></p></div>"
                }
            });
    }

    @Test
    public void clickOnInvisibleAnchor() throws Throwable
    {
        final URL urlA = new URL("http://example.org/");
        final URL urlB = new URL("http://foobar.com/");
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(urlA, html);
        conn.setResponse(urlB, "<html><head><title>Foo Bar</title></head><body>Lorem ipsum...</body></html>");

        try (final WebClient wc = new WebClient())
        {
            wc.setWebConnection(conn);

            final HtmlPage pageA = wc.getPage(urlA);
            final HtmlPage pageB = pageA.getAnchors().get(0).click();
            Assert.assertNotNull("Failed to load page", pageB);
            Assert.assertEquals("Foo Bar", pageB.getTitleText());
        }
    }

}
