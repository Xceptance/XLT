/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.htmlunit.html.HtmlPage;
import org.htmlunit.javascript.regexp.HtmlUnitRegExpProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.lang.ReflectionUtils;

/**
 * Test implementation of {@link HtmlUnitRegExpProxy}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class HtmlUnitRegexProxyTest implements AlertHandler
{
    /**
     * List of collected alerts.
     */
    private final List<String> alerts = new ArrayList<String>();

    /**
     * WebClient instance used for tests.
     */
    private WebClient wc;

    /**
     * Target URL.
     */
    private final URL url;

    /**
     * Constructor.
     */
    public HtmlUnitRegexProxyTest() throws MalformedURLException
    {
        url = new URL("http://example.org/");
    }

    /**
     * Test initialization.
     * 
     * @throws Throwable
     */
    @Before
    public void init() throws Throwable
    {
        wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setJavaScriptEnabled(true);

        final String response = "<html><head><title>Example Page</title></head><body>"
                                + "<script>'exampleString'.match(/.+(String)/);alert(RegExp.$1);</script>" + "</body></html>";
        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, response);

        wc.setWebConnection(conn);
        wc.setAlertHandler(this);
    }

    /**
     * #1183
     */
    @Test
    public void testRegExpUpdate() throws Throwable
    {
        final HtmlPage page = wc.getPage(url);
        Assert.assertNotNull("Failed to load page", page);
        Assert.assertEquals(1, alerts.size());
        Assert.assertEquals("String", alerts.get(0).trim());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAlert(final Page page, final String message)
    {
        alerts.add(message);
    }

    /**
     * #1409
     */
    @Test
    public void bracketsInCharacterClass() throws Throwable
    {
        final String regex = ReflectionUtils.callStaticMethod(HtmlUnitRegExpProxy.class, "jsRegExpToJavaRegExp",
                                                              "([-.*+?^${}()|[\\]\\/\\\\])");
        Pattern.compile(regex);
    }
}
