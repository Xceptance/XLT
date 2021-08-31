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
package com.xceptance.xlt.engine.htmlunit.apache;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

import com.gargoylesoftware.htmlunit.DownloadedContent;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.dns.XltDnsResolver;

/**
 * A specialization of HtmlUnit's Apache-HttpClient-based Web connection that performs additional setup and
 * configuration steps as needed by XLT.
 */
public class XltApacheHttpWebConnection extends HttpWebConnection
{
    /**
     * Creates a new HTTP web connection instance.
     *
     * @param webClient
     *            the WebClient that is using this connection
     */
    public XltApacheHttpWebConnection(final WebClient webClient)
    {
        super(webClient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpClientBuilder createHttpClientBuilder()
    {
        final HttpClientBuilder builder = super.createHttpClientBuilder();

        customizeHttpClientBuilderForXlt(builder);

        return builder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureHttpProcessorBuilder(final HttpClientBuilder builder, final WebRequest webRequest)
    {
        super.configureHttpProcessorBuilder(builder, webRequest);

        setXltRequestExecutor(builder, webRequest);
    }

    /**
     * Performs additional builder customization required for XLT.
     *
     * @param httpClientBuilder
     *            the builder to customize
     */
    private static void customizeHttpClientBuilderForXlt(final HttpClientBuilder httpClientBuilder)
    {
        final XltProperties props = XltProperties.getInstance();

        // whether to check for stale connections if keep-alive is enabled
        final boolean staleConnections = props.getProperty("com.xceptance.xlt.http.keepAlive.staleConnectionCheck",
                                                           props.getProperty("com.xceptance.xlt.keepAlive.staleConnectionCheck", true));

        final RequestConfig.Builder newRequestConfiguilder;
        final RequestConfig defaultRequestConfig = ReflectionUtils.readInstanceField(httpClientBuilder, "defaultRequestConfig");
        if (defaultRequestConfig == null)
        {
            newRequestConfiguilder = RequestConfig.custom();
        }
        else
        {
            newRequestConfiguilder = RequestConfig.copy(defaultRequestConfig);
        }

        final RequestConfig newRequestConfig = newRequestConfiguilder.setStaleConnectionCheckEnabled(staleConnections).build();
        httpClientBuilder.setDefaultRequestConfig(newRequestConfig);

        // miscellaneous networking settings
        SocketConfig.Builder newSocketConfigBuilder;
        final SocketConfig defaultSocketConfig = ReflectionUtils.readInstanceField(httpClientBuilder, "defaultSocketConfig");
        if (defaultSocketConfig == null)
        {
            newSocketConfigBuilder = SocketConfig.custom();
        }
        else
        {
            newSocketConfigBuilder = SocketConfig.copy(defaultSocketConfig);
        }

        final SocketConfig newSocketConfig = newSocketConfigBuilder.setTcpNoDelay(true).build();
        httpClientBuilder.setDefaultSocketConfig(newSocketConfig);

        // connection manager
        int threadCount = props.getProperty("com.xceptance.xlt.staticContent.downloadThreads", 4);
        if (threadCount <= 0)
        {
            threadCount = 1;
        }
        httpClientBuilder.setMaxConnPerRoute(threadCount);

        // request retry handler
        final boolean retryEnabled = props.getProperty("com.xceptance.xlt.http.retry.enabled", true);
        final int retryCount = props.getProperty("com.xceptance.xlt.http.retry.count", 3);
        final boolean retryNonIdempotentRequests = props.getProperty("com.xceptance.xlt.http.retry.nonIdempotentRequests", true);

        final int effectiveRetryCount = retryEnabled ? retryCount : 0;

        final HttpRequestRetryHandler requestRetryHandler = new XltHttpRequestRetryHandler(effectiveRetryCount, retryNonIdempotentRequests);
        httpClientBuilder.setRetryHandler(requestRetryHandler);

        // content encoding
        httpClientBuilder.disableContentCompression();

        // DNS resolver
        httpClientBuilder.setDnsResolver(new XltDnsResolverAdapterForApache(new XltDnsResolver()));
    }

    /**
     * Sets a special {@link HttpRequestExecutor} at the builder. The only purpose of this executor is to collect the
     * final set of request headers and set them at the web request for later use.
     *
     * @param httpClientBuilder
     *            the builder to customize
     * @param webRequest
     *            the current Web request
     */
    private static void setXltRequestExecutor(final HttpClientBuilder httpClientBuilder, final WebRequest webRequest)
    {
        // set our own request executor that collects all the headers for us
        httpClientBuilder.setRequestExecutor(new HttpRequestExecutor()
        {
            @Override
            protected HttpResponse doSendRequest(final HttpRequest request, final HttpClientConnection conn, final HttpContext context)
                throws IOException, HttpException
            {
                // remember the complete set of request headers sent to the server
                final Map<String, String> requestHeaders = new LinkedHashMap<>();
                for (final Header header : request.getAllHeaders())
                {
                    requestHeaders.put(header.getName(), header.getValue());
                }

                // replace the additional headers
                webRequest.setAdditionalHeaders(requestHeaders);

                return super.doSendRequest(request, conn, context);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebResponse makeWebResponse(final HttpResponse httpResponse, final WebRequest webRequest,
                                          final DownloadedContent responseBody, final long loadTime)
    {
        final WebResponse webResponse = super.makeWebResponse(httpResponse, webRequest, responseBody, loadTime);

        // just add protocol version information
        webResponse.setProtocolVersion(httpResponse.getProtocolVersion().toString());

        return webResponse;
    }
}
