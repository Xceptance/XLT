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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.xceptance.xlt.engine.XltWebClient;

public abstract class AbstractHtmlUnitTest
{
    protected String getContent(final String resourceName) throws IOException
    {
        InputStream in = null;

        try
        {
            in = getClass().getClassLoader().getResourceAsStream("org/htmlunit/performance/" + resourceName);

            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        finally
        {
            in.close();
        }
    }

    protected WebClient getWebClient(final String domain, final Map<String, String[]> mapping, final boolean jsEnabled,
                                     final boolean cssEnabled) throws IOException
    {
        // WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_6_0);
        final WebClient webClient = new XltWebClient();

        webClient.getOptions().setJavaScriptEnabled(jsEnabled);
        try
        {
            webClient.getOptions().setCssEnabled(cssEnabled);
        }
        catch (final Error e)
        {
            // happens when run with HtmlUnit 1.14
            // e.printStackTrace();
        }
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        final MockWebConnection mockWebConnection = new MockWebConnection();

        for (final Entry<String, String[]> entry : mapping.entrySet())
        {
            final String key = entry.getKey();
            final String[] params = entry.getValue();

            if (params.length == 2)
            {
                mockWebConnection.setResponse(new URL(key), getContent(params[0]), params[1]);
            }
            else
            {
                mockWebConnection.setResponse(new URL(key), getContent(params[0]));
            }
        }

        webClient.setWebConnection(mockWebConnection);

        return webClient;
    }
}
