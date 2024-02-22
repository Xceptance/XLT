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
package com.xceptance.xlt.api.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.ByteOrderMark;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;

import com.xceptance.common.net.HttpHeaderConstants;
import com.xceptance.common.util.ParameterCheckUtils;

/**
 * An abstract super class for {@link ResponseProcessor} implementations.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractResponseProcessor implements ResponseProcessor
{
    /**
     * Modified web response.
     */
    private static class ModifiedWebResponseData extends WebResponseData
    {
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 7571849792027379514L;

        /**
         * Body of web response.
         */
        private final byte[] body;

        /**
         * Constructor.
         * 
         * @param originalResponse
         *            the original response
         * @param body
         *            body of web response
         * @throws IOException
         */
        public ModifiedWebResponseData(final WebResponse originalResponse, final byte[] body) throws IOException
        {
            super(originalResponse.getStatusCode(), originalResponse.getStatusMessage(),
                  fixContentLengthHeader(originalResponse.getResponseHeaders(), body.length));
            this.body = body;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStream()
        {
            return new ByteArrayInputStream(body);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStreamWithBomIfApplicable(final ByteOrderMark... bomHeaders) throws IOException
        {
            return getInputStream();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] getBody()
        {
            return body;
        }
    }

    /**
     * Creates a new web response from the given original web response and the new content.
     * 
     * @param originalWebResponse
     *            the original web response
     * @param content
     *            the new content
     * @return a new web response
     */
    protected WebResponse createWebResponse(final WebResponse originalWebResponse, final byte[] content)
    {
        // parameter validation
        ParameterCheckUtils.isNotNull(originalWebResponse, "originalWebResponse");
        ParameterCheckUtils.isNotNull(content, "content");

        // create response and return it
        return makeResponse(originalWebResponse, content);
    }

    /**
     * Creates a new web response from the given original web response and the new content.
     * 
     * @param originalWebResponse
     *            the original web response
     * @param content
     *            the new content
     * @return a new web response
     */
    protected WebResponse createWebResponse(final WebResponse originalWebResponse, final String content)
    {
        // parameter validation
        ParameterCheckUtils.isNotNull(originalWebResponse, "originalWebResponse");
        ParameterCheckUtils.isNotNull(content, "content");

        // get content charset as defined in given response
        Charset charSet = originalWebResponse.getContentCharset();

        return makeResponse(originalWebResponse, content.getBytes(charSet));
    }

    /**
     * Creates a new web response using the given original web response as template. The given response data will be
     * used to set the content of the new response.
     * <p>
     * This method does NOT perform any null checks.
     * </p>
     * 
     * @param originalWebResponse
     *            original web response
     * @param responseData
     *            response data
     * @return new web response containing the given response data
     */
    private WebResponse makeResponse(final WebResponse originalWebResponse, final byte[] responseData)
    {
        try
        {
            final WebResponseData modifiedWebResponseData = new ModifiedWebResponseData(originalWebResponse, responseData);

            return new WebResponse(modifiedWebResponseData, originalWebResponse.getWebRequest(), originalWebResponse.getLoadTime());
        }
        catch (final IOException e)
        { // currently the exception can never happen using the constructor from above. Reported as bug to HtmlUnit
            throw new RuntimeException("Failed to make response", e);
        }
    }

    /**
     * Fixes the content length response header if present.
     * 
     * @param responseHeaders
     *            the response headers
     * @param length
     *            the new content length
     * @return fixed list of response headers
     */
    private static List<NameValuePair> fixContentLengthHeader(final List<NameValuePair> responseHeaders, final int length)
    {
        if (responseHeaders == null || responseHeaders.isEmpty())
        {
            return responseHeaders;
        }

        final ArrayList<NameValuePair> fixedHeaders = new ArrayList<NameValuePair>();
        for (final NameValuePair header : responseHeaders)
        {
            final String headerName = header.getName();
            if (!HttpHeaderConstants.CONTENT_LENGTH.equals(headerName))
            {
                fixedHeaders.add(header);
            }
            else
            {
                fixedHeaders.add(new NameValuePair(headerName, Integer.toString(length)));
            }
        }

        return fixedHeaders;
    }
}
