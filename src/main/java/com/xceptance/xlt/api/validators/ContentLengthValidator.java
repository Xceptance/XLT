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
package com.xceptance.xlt.api.validators;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.common.net.HttpHeaderConstants;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;

/**
 * Validates the downloaded content length with the announced size from the HTTP header.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class ContentLengthValidator
{
    /**
     * Validates the specified HTML page.
     * 
     * @param page
     *            the page to check
     * @throws AssertionError
     *             if the page fails validation
     */
    public void validate(final HtmlPage page)
    {
        validate(page.getWebResponse());
    }

    /**
     * Validates the specified lightweight HTML page.
     * 
     * @param page
     *            the page to check
     * @throws AssertionError
     *             if the page fails validation
     */
    public void validate(final LightWeightPage page)
    {
        validate(page.getWebResponse());
    }

    /**
     * Checks whether the given web response is complete.
     * 
     * @param response
     *            the web response to validate
     */
    private void validate(final WebResponse response)
    {
        // check that we do not use gzip
        final String encoding = response.getResponseHeaderValue(HttpHeaderConstants.CONTENT_ENCODING);

        if (encoding != null && encoding.contains(HttpHeaderConstants.GZIP))
        {
            // we cannot compare it, because size after decompression does not
            // match the Content-Length
            return;
        }

        final String contentLengthStr = response.getResponseHeaderValue(HttpHeaderConstants.CONTENT_LENGTH);
        if (contentLengthStr == null || contentLengthStr.trim().length() == 0)
        {
            // was a streamed/chunked response, no chance to validate
            return;
        }

        // this is always a valid number, otherwise something is wrong, so we
        // will not catch the exception here
        final int contentLength = Integer.parseInt(contentLengthStr);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final InputStream is = response.getContentAsStream())
        {
            if (is != null)
            {
                IOUtils.copy(is, out);
            }
        }
        catch (final IOException ioe)
        {
            Assert.fail("Failed to read response: " + ioe.getMessage());
        }

        // get the raw response
        final byte[] bytes = out.toByteArray();

        Assert.assertEquals("Content Length of download and announced size from HTTP header do not match.", contentLength, bytes.length);
    }

    /**
     * Returns the singleton instance.
     * 
     * @return the singleton instance
     */
    public static ContentLengthValidator getInstance()
    {
        // return new instance instead of singleton instance because there is no
        // object state at all
        return new ContentLengthValidator();
    }

}
