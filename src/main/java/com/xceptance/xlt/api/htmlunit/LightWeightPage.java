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
package com.xceptance.xlt.api.htmlunit;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

import com.xceptance.common.util.RegExUtils;

/**
 * A simple page object for light-weight operations.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class LightWeightPage
{
    /**
     * The web response.
     */
    private final WebResponse response;

    /**
     * The timer name.
     */
    private final String name;

    /**
     * Contents character set.
     */
    private final Charset charset;

    /**
     * Constructor.
     * 
     * @param webResponse
     *            the web response
     */
    public LightWeightPage(final WebResponse webResponse, final String timerName)
    {
        response = webResponse;
        name = timerName;
        charset = determineContentCharset();
    }

    /**
     * Returns the page content.
     * 
     * @return the content
     */
    public String getContent()
    {
        return response.getContentAsString(getCharset());
    }

    /**
     * Returns the status code of the web response.
     * 
     * @return status code of response
     */
    public int getHttpResponseCode()
    {
        return response.getStatusCode();
    }

    /**
     * Returns the web response.
     * 
     * @return web response
     */
    public WebResponse getWebResponse()
    {
        return response;
    }

    /**
     * Returns the timer name.
     * 
     * @return timer name
     */
    public String getTimerName()
    {
        return name;
    }

    /**
     * Returns the content character set.
     * 
     * @return content character set
     */
    public String getContentCharset()
    {
        return charset.name();
    }

    /**
     * Returns the content character set.
     * 
     * @return content character set
     */
    public Charset getCharset()
    {
        return charset;
    }

    /**
     * Determines the content character set.
     * 
     * @return content character set
     */
    private Charset determineContentCharset()
    {
        if (response != null)
        {
            /*
             * TODO: I would love to replace all this code with a simple "response.getContentCharset()" as it is much
             * more elaborate and robust. Unfortunately, that method behaves (slightly) different. Maybe we can do this
             * with the next major version.
             */

            // 1st: get value of content-type response header
            String charsetName = getCharsetNameFromContentTypeHeader(response);
            if (StringUtils.isBlank(charsetName))
            {
                final String content = response.getContentAsString(StandardCharsets.ISO_8859_1);
                if (StringUtils.isNotBlank(content))
                {
                    // 2nd: get the encoding attribute from a potential <?xml?> header (in case of XHTML)
                    charsetName = RegExUtils.getFirstMatch(content, "<\\?xml\\s[^>]*?encoding=\"([^\"]+)", 1);
                    if (StringUtils.isBlank(charsetName))
                    {
                        // 3rd: get declared charset from a content-type meta tag
                        charsetName = RegExUtils.getFirstMatch(content, "<meta\\s[^>]*?content=\"[^\"]*?charset=([^\";]+)", 1);
                        if (StringUtils.isBlank(charsetName))
                        {
                            // 4th: get declared charset from a charset meta tag
                            charsetName = RegExUtils.getFirstMatch(content, "<meta\\s+charset=\"([^\"]+)", 1);
                        }
                    }
                }

                if (StringUtils.isBlank(charsetName))
                {
                    // 5th: get content charset of request settings
                    final WebRequest request = response.getWebRequest();
                    charsetName = request != null ? request.getCharset().name() : null;
                }
            }

            // now see what we have got
            if (StringUtils.isNotBlank(charsetName))
            {
                charsetName = charsetName.trim();
                if (Charset.isSupported(charsetName))
                {
                    return Charset.forName(charsetName);
                }
            }
        }

        return StandardCharsets.ISO_8859_1;
    }

    private String getCharsetNameFromContentTypeHeader(final WebResponse response)
    {
        final String contentType = response.getResponseHeaderValue("content-type");

        if (contentType == null)
        {
            return null;
        }
        else
        {
            // Examples:
            // - text/plain
            // - text/plain; charset=utf-8
            // - application/hal+json;charset=utf8;profile="https://my.api.com/";version=1

            return StringUtils.substringBetween(contentType + ";", "charset=", ";");
        }
    }
}
