/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit.okhttp3;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.htmlunit.HttpHeader;
import org.htmlunit.WebClient;
import org.htmlunit.WebClientOptions;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.KeyDataPair;
import org.htmlunit.util.MimeType;
import org.htmlunit.util.NameValuePair;

import com.xceptance.common.util.ssl.EasyHostnameVerifier;
import com.xceptance.common.util.ssl.EasyX509TrustManager;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.dns.XltDnsResolver;
import com.xceptance.xlt.engine.htmlunit.AbstractWebConnection;

import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * An alternative {@link WebConnection} implementation that uses OkHttp3 as HTTP client.
 */
public class OkHttp3WebConnection extends AbstractWebConnection<OkHttpClient, Request, Response>
{
    // TODO: stale connection check?
    // TODO: tcp nodelay?

    private static final List<Protocol> HTTP_1_1_ONLY = Arrays.asList(Protocol.HTTP_1_1);

    private static final List<Protocol> HTTP_2_AND_1_1 = Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

    private static final EasyHostnameVerifier INSECURE_HOSTNAME_VERIFIER = new EasyHostnameVerifier();

    private static final EasyX509TrustManager INSECURE_TRUST_MANAGER = new EasyX509TrustManager(null);

    private static final SSLSocketFactory INSECURE_SSL_SOCKET_FACTORY = createInsecureSslSocketFactory();

    private final AuthenticationCache authenticationCache;

    private final ConnectionPool connectionPool;

    private final DnsImpl dns;

    private final List<Protocol> protocols;

    /**
     * Whether to collect the target IP address that was used to make the request.
     */
    private boolean collectTargetIpAddress;

    /**
     * Constructor.
     *
     * @param webClient
     *            the owning web client
     * @param http2Enabled
     *            whether or not HTTP/2 is enabled at all
     * @param collectTargetIpAddress
     *            whether to collect the target IP address that was used to make the request
     */
    public OkHttp3WebConnection(final WebClient webClient, final boolean http2Enabled, final boolean collectTargetIpAddress)
    {
        super(webClient);

        this.collectTargetIpAddress = collectTargetIpAddress;

        authenticationCache = new AuthenticationCache();
        connectionPool = new ConnectionPool(6, 60, TimeUnit.SECONDS);
        dns = new DnsImpl(new XltDnsResolver());
        protocols = http2Enabled ? HTTP_2_AND_1_1 : HTTP_1_1_ONLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OkHttpClient createHttpClient(final WebClient webClient, final WebRequest webRequest) throws Exception
    {
        final WebClientOptions webClientOptions = webClient.getOptions();
        final Authenticator authenticator = new AuthenticatorImpl(webClient.getCredentialsProvider());

        final Builder httpClientBuilder = new OkHttpClient.Builder();

        // basic settings and components
        httpClientBuilder.authenticator(authenticator);
        httpClientBuilder.connectionPool(connectionPool);
        httpClientBuilder.cookieJar(new CookieJarImpl(webClient.getCookieManager()));
        httpClientBuilder.dns(dns);
        httpClientBuilder.protocols(protocols);
        httpClientBuilder.retryOnConnectionFailure(true);

        // redirects (disable automatic redirects as HtmlUnit handles redirects itself)
        httpClientBuilder.followRedirects(false);
        httpClientBuilder.followSslRedirects(false);

        // proxy
        final String proxyHost = webRequest.getProxyHost();
        if (proxyHost != null)
        {
            final InetSocketAddress proxyHostAddress = new InetSocketAddress(proxyHost, webRequest.getProxyPort());
            final Proxy proxy = new Proxy(webRequest.isSocksProxy() ? Proxy.Type.SOCKS : Proxy.Type.HTTP, proxyHostAddress);
            httpClientBuilder.proxy(proxy);
            httpClientBuilder.proxyAuthenticator(authenticator);
        }

        // timeouts
        int timeout = webRequest.getTimeout();
        if (timeout < 0)
        {
            timeout = webClientOptions.getTimeout();
        }

        final Duration timeoutMS = Duration.ofMillis(timeout);
        httpClientBuilder.connectTimeout(timeoutMS);
        httpClientBuilder.readTimeout(timeoutMS);
        httpClientBuilder.writeTimeout(timeoutMS);

        // dispatcher
        final Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(64);
        dispatcher.setMaxRequestsPerHost(6);
        httpClientBuilder.dispatcher(dispatcher);

        // SSL
        if (webClientOptions.isUseInsecureSSL())
        {
            httpClientBuilder.sslSocketFactory(INSECURE_SSL_SOCKET_FACTORY, INSECURE_TRUST_MANAGER);
            httpClientBuilder.hostnameVerifier(INSECURE_HOSTNAME_VERIFIER);
        }

        // interceptors
        httpClientBuilder.addNetworkInterceptor(new AuthorizationHeaderInterceptor(authenticationCache));
        httpClientBuilder.addNetworkInterceptor(new RetrieveFinalRequestHeadersInterceptor(webRequest));

        if (collectTargetIpAddress)
        {
            httpClientBuilder.addNetworkInterceptor(new RetrieveUsedTargetIpAddressInterceptor());
        }

        // finally create the HTTP client
        return httpClientBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        connectionPool.evictAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Request createRequestWithoutBody(final URI uri, final WebRequest webRequest)
    {
        return createRequest(uri, webRequest, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Request createRequestWithStringBody(final URI uri, final WebRequest webRequest, final String body, final String mimeType,
                                                  final @Nullable Charset charset)
    {
        // ensure that a custom content type header wins over HtmlUnit's sometimes wrong defaults
        String contentType = webRequest.getAdditionalHeader(HttpHeader.CONTENT_TYPE);
        if (contentType == null)
        {
            contentType = (charset == null) ? mimeType : mimeType + ";charset=" + charset;
        }

        // create the request body
        final RequestBody requestBody;

        final MediaType mediaType = MediaType.get(contentType);
        if (mediaType.charset() == null)
        {
            // interpret string body as binary/ISO_8859_1 content and create a binary request body
            final byte[] bytes = body.getBytes(StandardCharsets.ISO_8859_1);
            requestBody = RequestBody.create(mediaType, bytes);
        }
        else
        {
            // create a regular text request body
            requestBody = RequestBody.create(mediaType, body);
        }

        return createRequest(uri, webRequest, requestBody);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Request createRequestWithMultiPartBody(final URI uri, final WebRequest webRequest)
    {
        final MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (final NameValuePair pair : webRequest.getRequestParameters())
        {
            if (pair instanceof KeyDataPair)
            {
                addFilePart((KeyDataPair) pair, builder);
            }
            else
            {
                builder.addFormDataPart(pair.getName(), pair.getValue());
            }
        }

        final RequestBody requestBody = builder.build();

        return createRequest(uri, webRequest, requestBody);
    }

    /**
     * Adds a file part to the given multi-part request body builder.
     *
     * @param pairWithFile
     *            the parameter describing the file part to add
     * @param builder
     *            the multi-part request body builder
     */
    void addFilePart(final KeyDataPair pairWithFile, final MultipartBody.Builder builder)
    {
        final String name = pairWithFile.getName();
        final String value = pairWithFile.getValue();
        final byte[] data = pairWithFile.getData();
        final File file = pairWithFile.getFile();

        // determine media type
        String mimeType = pairWithFile.getMimeType();
        if (mimeType == null)
        {
            mimeType = MimeType.APPLICATION_OCTET_STREAM;
        }

        final MediaType mediaType = MediaType.parse(mimeType);

        // determine file name
        final String filename;
        if (file == null)
        {
            filename = value;
        }
        else if (pairWithFile.getFileName() == null)
        {
            filename = file.getName();
        }
        else
        {
            filename = pairWithFile.getFileName();
        }

        // add form data part in the right way (depending on what was given as input)
        if (data != null)
        {
            builder.addFormDataPart(name, filename, RequestBody.create(mediaType, data));
        }
        else if (file != null)
        {
            builder.addFormDataPart(name, filename, RequestBody.create(mediaType, file));
        }
        else
        {
            builder.addFormDataPart(name, filename, RequestBody.create(mediaType, new byte[0]));
        }
    }

    /**
     * Creates a new {@link Request} according to the web request and populates it with the given request body.
     *
     * @param uri
     *            the target URI
     * @param webRequest
     *            the HtmlUnit web request
     * @param requestBody
     *            the request body
     * @return the ready-to-sent request
     */
    private Request createRequest(final URI uri, final WebRequest webRequest, final @Nullable RequestBody requestBody)
    {
        final Request.Builder requestBuilder = new Request.Builder();

        requestBuilder.url(uri.toString());
        requestBuilder.method(webRequest.getHttpMethod().name(), requestBody);

        // set any custom headers
        webRequest.getAdditionalHeaders().forEach(requestBuilder::header);

        return requestBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response executeRequest(final OkHttpClient httpClient, final Request request) throws IOException
    {
        return httpClient.newCall(request).execute();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    protected WebResponse makeWebResponse(final Response response, final WebRequest webRequest, final long loadTime) throws IOException
    {
        try (ResponseBody responseBody = response.body())
        {
            final List<NameValuePair> headers = toNamevaluePairs(response.headers());
            final WebResponseData webResponseData = new WebResponseData(responseBody.bytes(), response.code(), response.message(), headers);
            final WebResponse webResponse = new WebResponse(webResponseData, webRequest, loadTime);

            webResponse.setProtocolVersion(response.protocol().toString());

            return webResponse;
        }
    }

    /**
     * Converts a {@link Headers} object to a list of HtmlUnit {@link NameValuePair}.
     */
    private static List<NameValuePair> toNamevaluePairs(final Headers headers)
    {
        final List<NameValuePair> nameValuePairs = new ArrayList<>();

        for (int i = 0; i < headers.size(); i++)
        {
            nameValuePairs.add(new NameValuePair(headers.name(i), headers.value(i)));
        }

        return nameValuePairs;
    }

    /**
     * Creates an SSL socket factory that will be used when we are in "easy mode", i.e. when
     * {@link WebClientOptions#isUseInsecureSSL()} was set.
     *
     * @return the socket factory
     */
    private static SSLSocketFactory createInsecureSslSocketFactory()
    {
        try
        {
            final TrustManager[] trustManagers = new TrustManager[]
                {
                    INSECURE_TRUST_MANAGER
                };

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            return sslContext.getSocketFactory();
        }
        catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new XltException("Failed to create insecure SSL socket factory", e);
        }
    }
}
