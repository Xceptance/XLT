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

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test shows that querying an {@link HtmlElement} for a namespace-qualified attribute leads to unexpected results
 * if the element actually does not have such a qualified attribute, but an unqualified one with the same name. In this
 * case, the framework falls back to the unqualified attribute and deals with it instead, which is not what one would
 * expect.
 */

public class AttributeNSTest
{
    private static WebClient webClient;

    @BeforeClass
    public static void setUp() throws Exception
    {
        final String html = "<html><head></head><body><input id=\"foo\" type=\"button\" name=\"foo\" value=\"FOO\"></body></html>";

        // setup
        webClient = new WebClient();

        final MockWebConnection connection = new MockWebConnection();
        webClient.setWebConnection(connection);
        connection.setResponse(new URL("http://localhost/index.html"), html);
    }

    @Test
    public void test_getAttributeNS() throws Exception
    {
        final HtmlPage page = (HtmlPage) webClient.getPage("http://localhost/index.html");
        final DomElement element = page.getElementById("foo");

        // check that the qualified attribute is undefined
        final String typeValue = element.getAttributeNS("bar", "type");
        Assert.assertEquals("Unexpected value for attribute 'bar:type' found: ", DomElement.ATTRIBUTE_NOT_DEFINED, typeValue);
    }

    @Test
    public void test_hasAttributeNS() throws Exception
    {
        final HtmlPage page = (HtmlPage) webClient.getPage("http://localhost/index.html");
        final DomElement element = page.getElementById("foo");

        // check that the qualified attribute is *not* found
        final boolean found = element.hasAttributeNS("bar", "type");
        Assert.assertFalse("Attribute 'bar:type' found?!?", found);
    }

    @Test
    public void test_removeAttributeNS() throws Exception
    {
        final HtmlPage page = (HtmlPage) webClient.getPage("http://localhost/index.html");
        final DomElement element = page.getElementById("foo");

        // remove the (non-existing) qualified attribute
        element.removeAttributeNS("bar", "type");

        // check that the unqualified attribute is still there
        final boolean found = element.hasAttribute("type");
        Assert.assertTrue("Attribute 'type' not found?!?", found);
    }
}
