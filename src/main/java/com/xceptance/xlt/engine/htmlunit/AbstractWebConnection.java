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
package com.xceptance.xlt.engine.htmlunit;

import static org.htmlunit.BrowserVersionFeatures.URL_AUTH_CREDENTIALS;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.utils.URLEncodedUtils;
import org.htmlunit.FormEncodingType;
import org.htmlunit.HttpHeader;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebClient;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebRequest.HttpHint;
import org.htmlunit.WebResponse;
import org.htmlunit.httpclient.HttpClientConverter;
import org.htmlunit.util.MimeType;
import org.htmlunit.util.NameValuePair;
import org.htmlunit.util.UrlUtils;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Super class for alternative {@link WebConnection} implementations. Provides some common functionality that we would
 * otherwise have to implement again and again.
 *
 * @param <T>
 *            the type of implementation-specific HTTP client instances (T == transport)
 * @param <O>
 *            the type of implementation-specific HTTP request instances (O == outbound data)
 * @param <I>
 *            the type of implementation-specific HTTP response instances (I == inbound data)
 */
public abstract class AbstractWebConnection<T, O, I> implements WebConnection
{
    // TODO: request retry handling

    private final WebClient webClient;

    /**
     * Constructor.
     *
     * @param webClient
     *            the owning web client
     */
    public AbstractWebConnection(final WebClient webClient)
    {
        this.webClient = webClient;
    }

    /**
     * Returns the owning {@link WebClient} instance.
     * 
     * @return the web client
     */
    protected WebClient getWebClient()
    {
        return webClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(final WebRequest webRequest) throws IOException
    {
        finalizeRequestHeaders(webRequest);

        // create and execute the request
        try
        {
            final T httpClient = createHttpClient(webClient, webRequest);

            final O request = makeRequest(webRequest);

            final long startTime = TimerUtils.getTime();
            final I response = executeRequest(httpClient, request);
            final long loadTime = TimerUtils.getTime() - startTime;

            return makeWebResponse(response, webRequest, loadTime);
        }
        catch (Throwable t)
        {
            // first unwrap any ExecutionException
            if (t instanceof ExecutionException)
            {
                t = t.getCause();
            }

            // now do the right thing
            if (t instanceof Error)
            {
                throw (Error) t;
            }
            else if (t instanceof RuntimeException)
            {
                throw (RuntimeException) t;
            }
            else if (t instanceof IOException)
            {
                throw (IOException) t;
            }
            else
            {
                // wrap any other exception as runtime exception
                throw new XltException("Failed to execute Web request", t);
            }
        }
    }

    /**
     * Creates or returns an implementation-specific HTTP client tailored to match the general settings at the given
     * {@link WebClient} and the specific settings at the given {@link WebRequest}.
     */
    protected abstract T createHttpClient(WebClient webClient, WebRequest webRequest) throws Exception;

    /**
     * Creates an implementation-specific request that resembles the given {@link WebRequest}.
     */
    private O makeRequest(final WebRequest webRequest) throws URISyntaxException
    {
        final HttpMethod method = webRequest.getHttpMethod();
        final Charset charset = webRequest.getCharset();

        // Make sure that the URL is fully encoded. IE actually sends some Unicode chars in request
        // URLs; because of this we allow some Unicode chars in URLs. However, at this point we're
        // handing things over the HttpClient, and HttpClient will blow up if we leave these Unicode
        // chars in the URL.
        final URL url = UrlUtils.encodeUrl(webRequest.getUrl(), false, charset);
        URI uri = url.toURI();

        // build the request
        final O request;

        // set parameters/body
        if (!(method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH || method == HttpMethod.DELETE))
        {
            if (!webRequest.getRequestParameters().isEmpty())
            {
                final List<NameValuePair> pairs = webRequest.getRequestParameters();
                final List<org.apache.http.NameValuePair> httpClientPairs = HttpClientConverter.nameValuePairsToHttpClient(pairs);

                final String query = URLEncodedUtils.format(httpClientPairs, charset);
                uri = UrlUtils.toURI(url, query);
            }

            request = createRequestWithoutBody(uri, webRequest);
        }
        else
        {
            if (webRequest.getEncodingType() == FormEncodingType.URL_ENCODED && method == HttpMethod.POST)
            {
                if (webRequest.getRequestBody() == null)
                {
                    final List<NameValuePair> pairs = webRequest.getRequestParameters();
                    final List<org.apache.http.NameValuePair> httpClientPairs = HttpClientConverter.nameValuePairsToHttpClient(pairs);
                    final String body = URLEncodedUtils.format(httpClientPairs, charset);

                    if (webRequest.hasHint(HttpHint.IncludeCharsetInContentTypeHeader))
                    {
                        request = createRequestWithStringBody(uri, webRequest, body, URLEncodedUtils.CONTENT_TYPE, charset);
                    }
                    else
                    {
                        request = createRequestWithStringBody(uri, webRequest, body, URLEncodedUtils.CONTENT_TYPE, null);
                    }
                }
                else
                {
                    final String body = StringUtils.defaultString(webRequest.getRequestBody());
                    request = createRequestWithStringBody(uri, webRequest, body, URLEncodedUtils.CONTENT_TYPE, charset);
                }
            }
            else if (webRequest.getEncodingType() == FormEncodingType.TEXT_PLAIN && method == HttpMethod.POST)
            {
                String body;
                if (webRequest.getRequestBody() == null)
                {
                    final StringBuilder bodyBuilder = new StringBuilder();
                    for (final NameValuePair pair : webRequest.getRequestParameters())
                    {
                        bodyBuilder.append(StringUtils.remove(StringUtils.remove(pair.getName(), '\r'), '\n')).append("=")
                                   .append(StringUtils.remove(StringUtils.remove(pair.getValue(), '\r'), '\n')).append("\r\n");
                    }

                    body = bodyBuilder.toString();
                }
                else
                {
                    body = StringUtils.defaultString(webRequest.getRequestBody());
                }

                request = createRequestWithStringBody(uri, webRequest, body, MimeType.TEXT_PLAIN, charset);
            }
            else if (FormEncodingType.MULTIPART == webRequest.getEncodingType())
            {
                request = createRequestWithMultiPartBody(uri, webRequest);
            }
            else
            {
                // PUT, PATCH, DELETE

                final String body = webRequest.getRequestBody();
                if (body == null)
                {
                    request = createRequestWithoutBody(uri, webRequest);
                }
                else
                {
                    request = createRequestWithStringBody(uri, webRequest, body, MimeType.TEXT_PLAIN, charset);
                }
            }
        }

        // Tell the client where to get its credentials from
        // (it may have changed on the webClient since last call to getHttpClientFor(...))
        final CredentialsProvider credentialsProvider = webClient.getCredentialsProvider();

        // if the used url contains credentials, we have to add this
        final Credentials requestUrlCredentials = webRequest.getUrlCredentials();
        if (null != requestUrlCredentials && webClient.getBrowserVersion().hasFeature(URL_AUTH_CREDENTIALS))
        {
            final URL requestUrl = webRequest.getUrl();
            final AuthScope authScope = new AuthScope(requestUrl.getHost(), requestUrl.getPort());
            // updating our client to keep the credentials for the next request
            credentialsProvider.setCredentials(authScope, requestUrlCredentials);
        }

        // if someone has set credentials to this request, we have to add this
        final Credentials requestCredentials = webRequest.getCredentials();
        if (null != requestCredentials)
        {
            final URL requestUrl = webRequest.getUrl();
            final AuthScope authScope = new AuthScope(requestUrl.getHost(), requestUrl.getPort());
            // updating our client to keep the credentials for the next request
            credentialsProvider.setCredentials(authScope, requestCredentials);
        }

        return request;
    }

    /**
     * Creates an implementation-specific request that resembles the given {@link WebRequest} and has no body.
     */
    protected abstract O createRequestWithoutBody(final URI uri, final WebRequest webRequest);

    /**
     * Creates an implementation-specific request that resembles the given {@link WebRequest} and populates it with the
     * given string body.
     */
    protected abstract O createRequestWithStringBody(final URI uri, WebRequest webRequest, final String body, final String mimeType,
                                                     final @Nullable Charset charset);

    /**
     * Creates an implementation-specific multi-part request that resembles the given {@link WebRequest}.
     */
    protected abstract O createRequestWithMultiPartBody(final URI uri, final WebRequest webRequest);

    /**
     * Executes the implementation-specific request using the given HTTP client and returns the response.
     */
    protected abstract I executeRequest(T httpClient, final O request) throws IOException;

    /**
     * Creates a {@link WebResponse} instance from the implementation-specific response and links it to the given
     * {@link WebRequest}.
     */
    protected abstract WebResponse makeWebResponse(final I response, final WebRequest webRequest, final long loadTime) throws IOException;

    /**
     * Adds some standard headers to the web request.
     * 
     * @param webRequest
     *            the web request
     */
    protected void finalizeRequestHeaders(final WebRequest webRequest)
    {
        final Map<String, String> requestHeaders = webRequest.getAdditionalHeaders();

        requestHeaders.putIfAbsent(HttpHeader.USER_AGENT, webClient.getBrowserVersion().getUserAgent());

        if (webClient.getOptions().isDoNotTrackEnabled())
        {
            requestHeaders.putIfAbsent(HttpHeader.DNT, "1");
        }
    }
}
