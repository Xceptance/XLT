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
package com.xceptance.xlt.engine.htmlunit.apache;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import com.xceptance.xlt.api.engine.Session;

/**
 * A specialized {@link StandardHttpRequestRetryHandler} that rather checks the root cause (if any) of the offending
 * exception when it comes to decide whether a request is retriable under these circumstances.
 */
public class XltHttpRequestRetryHandler extends StandardHttpRequestRetryHandler
{
    /**
     * Constructor.
     *
     * @param retryCount
     *            the maximum number of retries
     * @param requestSentRetryEnabled
     *            whether non-idempotent requests that have already been sent are to be retried nevertheless
     */
    public XltHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled)
    {
        super(retryCount, requestSentRetryEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context)
    {
        // if the original exception has a root cause, check this one instead of the original one
        final Throwable rootCause = ExceptionUtils.getRootCause(exception);
        final IOException exceptionToCheck = (rootCause instanceof IOException) ? (IOException) rootCause : exception;

        // now check for retry
        final boolean retry = super.retryRequest(exceptionToCheck, executionCount, context);

        // log an event in case of retry
        if (retry)
        {
            final String eventName = "Request retried (attempt #" + executionCount + ")";
            final String eventMessage = "Host: " + getHostName(context) + ", Reason: " + exceptionToCheck.toString();

            Session.logEvent(eventName, eventMessage);
        }

        return retry;
    }

    /**
     * Extracts the name of the target host from the info stored at the current request execution context.
     * 
     * @param context
     *            the request execution context
     * @return the host name
     */
    private String getHostName(final HttpContext context)
    {
        String hostName = null;

        HttpRequest request = HttpClientContext.adapt(context).getRequest();
        if (request instanceof HttpRequestWrapper)
        {
            request = ((HttpRequestWrapper) request).getOriginal();
        }

        if (request instanceof HttpUriRequest)
        {
            final URI uri = ((HttpUriRequest) request).getURI();
            if (uri != null)
            {
                hostName = uri.getHost();
            }
        }

        return StringUtils.defaultIfBlank(hostName, "<unknown>");
    }
}
