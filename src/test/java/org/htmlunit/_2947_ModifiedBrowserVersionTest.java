/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.io.IOException;

import org.htmlunit.BrowserVersion.BrowserVersionBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Don't know how to rework this")
public class _2947_ModifiedBrowserVersionTest
{
    private static BrowserVersion _defaultBrowser;

    @BeforeClass
    public static void setDefaultBrowserVersion()
    {
        _defaultBrowser = BrowserVersion.getDefault();
        
        // TODO
        // BrowserVersion.setDefault(BrowserVersion.INTERNET_EXPLORER);
    }

    @AfterClass
    public static void restoreDefaultBrowserVersion()
    {
        BrowserVersion.setDefault(_defaultBrowser);
    }

    @Test
    public void original() throws Throwable
    {
        BrowserVersion browserVersion = BrowserVersion.CHROME;

        test(browserVersion);
    }

    @Test
    public void cloned() throws Throwable
    {
        BrowserVersion browserVersion = new BrowserVersionBuilder(BrowserVersion.CHROME).build();

        test(browserVersion);
    }

    @Test
    public void clonedAndModified() throws Throwable
    {
        BrowserVersion browserVersion = new BrowserVersionBuilder(BrowserVersion.CHROME).setUserAgent("foo").build();

        test(browserVersion);
    }

    /**
     * Tests for the correct setup of HtmlUnit's JS engine using the given browser version.
     * <p>
     * Idea: It seems that there is no other way than checking for a certain property that exist for some browser
     * versions but not all. Here, we use the document element's property 'documentURI' which is defined in FF and
     * CHROME but not in IE.
     * </p>
     * 
     * @param browserVersion
     *            the browser version to use
     */
    private void test(BrowserVersion browserVersion) throws IOException
    {
        try (WebClient webClient = new WebClient(browserVersion))
        {
            MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse("<html><body onload='document.documentURI.length'></body></html>");
            webClient.setWebConnection(conn);

            webClient.getPage("http://localhost/");
        }
    }
}
