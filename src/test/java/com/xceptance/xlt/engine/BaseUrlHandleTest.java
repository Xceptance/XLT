/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.AbstractWebTestCase;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Tests the correct handling of &lt;base&gt; tags when downloading static content such as images.
 */
public class BaseUrlHandleTest extends AbstractWebTestCase
{
    @BeforeClass
    public static void prepare()
    {
        final XltProperties props = XltProperties.getInstance();
        props.setProperty("com.xceptance.xlt.css.download.images", "onDemand");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
        props.setProperty("com.xceptance.xlt.javaScriptEnabled", "true");
    }

    @Test
    public void testDownloadCSSOnDemand() throws Throwable
    {
        final String html = "<html><head><base href=\"img/\"><style type=\"text/css\">body { background-image: url(logo2.png)}</style></head><body><img src=\"logo.png\"></body></html>";
        getMockConnection().setDefaultResponse(new byte[0], 410, "Gone", "none");

        final HtmlPage page = loadPage(html);
        getWebClient().loadNewStaticContent(page);

        final List<String> urls = getMockConnection().getRequestedUrls(getDefaultUrl());
        Assert.assertArrayEquals(new String[]
            {
                "", "img/logo.png", "img/logo2.png"
            }, urls.toArray());
    }
}
