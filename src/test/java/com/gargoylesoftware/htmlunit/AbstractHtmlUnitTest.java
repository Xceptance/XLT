package com.gargoylesoftware.htmlunit;

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
            in = getClass().getClassLoader().getResourceAsStream("com/gargoylesoftware/htmlunit/performance/" + resourceName);

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
