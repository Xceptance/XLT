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
package com.xceptance.xlt.engine.htmlunit.apache5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.engine.dns.XltDnsResolver;
import com.xceptance.xlt.engine.htmlunit.AbstractWebConnection;

/**
 * An alternative {@link WebConnection} implementation that uses Apache's {@link CloseableHttpAsyncClient}.
 */
public class Apache5HttpWebConnection extends AbstractWebConnection<CloseableHttpAsyncClient, SimpleHttpRequest, SimpleHttpResponse>
{
    static
    {
        System.out.println("Using " + Apache5HttpWebConnection.class.getSimpleName());
    }

    private boolean http2Enabled;

    private final PoolingAsyncClientConnectionManager connectionManager;

    /**
     * Constructor.
     *
     * @param webClient
     *            the owning web client
     */
    public Apache5HttpWebConnection(final WebClient webClient, final boolean http2Enabled)
    {
        super(webClient);

        this.http2Enabled = http2Enabled;

        try
        {
            connectionManager = createConnectionManagerBuilder(webClient.getOptions()).build();
        }
        catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e)
        {
            throw new XltException("", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CloseableHttpAsyncClient createHttpClient(final WebClient webClient, final WebRequest webRequest) throws Exception
    {
        return createHttpClientBuilder(webClient, webRequest).build();
    }

    /**
     * {@inheritDoc}
     */
    protected HttpAsyncClientBuilder createHttpClientBuilder(final WebClient webClient, final WebRequest webRequest) throws Exception
    {
        final WebClientOptions webClientOptions = webClient.getOptions();

        int timeout = webRequest.getTimeout();
        if (timeout < 0)
        {
            timeout = webClientOptions.getTimeout();
        }

        final HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();

        builder.setConnectionManager(connectionManager);
        builder.setConnectionManagerShared(true);
        builder.setDefaultCookieStore(new CookieStoreImpl(webClient.getCookieManager()));
        builder.setDefaultCredentialsProvider(new CredentialsProviderImpl(webClient.getCredentialsProvider()));
        builder.setDefaultRequestConfig(createRequestConfigBuilder(timeout, webRequest).build());
        builder.setIOReactorConfig(createIoReactorConfigBuilder(webRequest).build());
        builder.setRedirectStrategy(new HtmlUnitRedirectStrategy());
        builder.setUserAgent(webClient.getBrowserVersion().getUserAgent());
        builder.setVersionPolicy(http2Enabled ? HttpVersionPolicy.NEGOTIATE : HttpVersionPolicy.FORCE_HTTP_1);

        // builder.disableContentCompression();
        builder.disableRedirectHandling();

        // interceptors
        builder.addRequestInterceptorLast(new RetrieveFinalRequestHeadersInterceptor(webRequest));

        // {
        // final XltProperties props = XltProperties.getInstance();
        //
        // // whether to check for stale connections if keep-alive is enabled
        // final boolean staleConnections = props.getProperty("com.xceptance.xlt.http.keepAlive.staleConnectionCheck",
        // props.getProperty("com.xceptance.xlt.keepAlive.staleConnectionCheck", true));
        //
        //
        // final RequestConfig newRequestConfig =
        // newRequestConfiguilder.setStaleConnectionCheckEnabled(staleConnections).build();
        //
        // // miscellaneous networking settings
        // SocketConfig.Builder newSocketConfigBuilder;
        // final SocketConfig defaultSocketConfig = ReflectionUtils.readInstanceField(httpClientBuilder,
        // "defaultSocketConfig");
        // if (defaultSocketConfig == null)
        // {
        // newSocketConfigBuilder = SocketConfig.custom();
        // }
        // else
        // {
        // newSocketConfigBuilder = SocketConfig.copy(defaultSocketConfig);
        // }
        //
        // final SocketConfig newSocketConfig = newSocketConfigBuilder.setTcpNoDelay(true).build();
        // httpClientBuilder.setDefaultSocketConfig(newSocketConfig);
        //
        // // connection manager
        // int threadCount = props.getProperty("com.xceptance.xlt.staticContent.downloadThreads", 4);
        // if (threadCount <= 0)
        // {
        // threadCount = 1;
        // }
        // httpClientBuilder.setMaxConnPerRoute(threadCount);
        //
        // // request retry handler
        // final boolean retryEnabled = props.getProperty("com.xceptance.xlt.http.retry.enabled", true);
        // final int retryCount = props.getProperty("com.xceptance.xlt.http.retry.count", 3);
        // final boolean retryNonIdempotentRequests =
        // props.getProperty("com.xceptance.xlt.http.retry.nonIdempotentRequests", true);
        //
        // final int effectiveRetryCount = retryEnabled ? retryCount : 0;
        //
        // final HttpRequestRetryHandler requestRetryHandler = new XltHttpRequestRetryHandler(effectiveRetryCount,
        // retryNonIdempotentRequests);
        // httpClientBuilder.setRetryHandler(requestRetryHandler);
        // }

        return builder;
    }

    protected RequestConfig.Builder createRequestConfigBuilder(final int timeout, WebRequest webRequest)
    {
        final RequestConfig.Builder builder = RequestConfig.custom();

        // timeout
        final Timeout timeoutMS = Timeout.ofMilliseconds(timeout);

        builder.setConnectTimeout(timeoutMS);
        builder.setConnectionRequestTimeout(timeoutMS);
        builder.setResponseTimeout(timeoutMS);
        // builder.setStaleConnection

        // HTTP proxy
        if (webRequest.getProxyHost() != null && !webRequest.isSocksProxy())
        {
            final HttpHost proxy = new HttpHost(webRequest.getProxyScheme(), webRequest.getProxyHost(), webRequest.getProxyPort());
            builder.setProxy(proxy);
        }

        return builder;
    }

    protected IOReactorConfig.Builder createIoReactorConfigBuilder(WebRequest webRequest)
    {
        final IOReactorConfig.Builder builder = IOReactorConfig.custom();

        builder.setTcpNoDelay(true);

        // socks proxy
        if (webRequest.getProxyHost() != null && webRequest.isSocksProxy())
        {
            builder.setSocksProxyAddress(new InetSocketAddress(webRequest.getProxyHost(), webRequest.getProxyPort()));
            // builder.setSocksProxyUsername();
            // builder.setSocksProxyPassword(null)
        }

        return builder;
    }

    /**
     * Creates a preconfigured builder for {@link PoolingAsyncClientConnectionManager} objects. Sub classes may override
     * this method to add further customizations to the builder.
     * 
     * @param options
     *            the WebClient options
     * @return the builder
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    protected PoolingAsyncClientConnectionManagerBuilder createConnectionManagerBuilder(final WebClientOptions options)
        throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        final PoolingAsyncClientConnectionManagerBuilder builder = PoolingAsyncClientConnectionManagerBuilder.create();

        final TlsStrategy tlsStrategy = createTlsStrategyBuilder(options).build();
        builder.setTlsStrategy(tlsStrategy);

        long connectionTimeToLive = options.getConnectionTimeToLive();
        if (connectionTimeToLive >= 0)
        {
            final TimeValue timeToLive = TimeValue.ofMilliseconds(connectionTimeToLive);
            builder.setConnectionTimeToLive(timeToLive);
        }

        builder.setMaxConnPerRoute(6);
        builder.setDnsResolver(new DnsResolverImpl(new XltDnsResolver()));

        return builder;
    }

    /**
     * Creates a preconfigured builder for {@link TlsStrategy} objects. Sub classes may override this method to add
     * further customizations to the builder.
     * 
     * @param options
     *            the WebClient options
     * @return the builder
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     */
    protected ClientTlsStrategyBuilder createTlsStrategyBuilder(final WebClientOptions options)
        throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException
    {
        final String[] sslClientProtocols = options.getSSLClientProtocols();
        final String[] sslClientCipherSuites = options.getSSLClientCipherSuites();
        final boolean useInsecureSSL = options.isUseInsecureSSL();

        final SSLContext sslContext = createSslContextBuilder(options).build();

        final ClientTlsStrategyBuilder tlsStrategyBuilder = ClientTlsStrategyBuilder.create();
        tlsStrategyBuilder.setSslContext(sslContext);
        tlsStrategyBuilder.setCiphers(sslClientCipherSuites);
        tlsStrategyBuilder.setTlsVersions(sslClientProtocols);
        tlsStrategyBuilder.setHostnameVerifier(useInsecureSSL ? NoopHostnameVerifier.INSTANCE : new DefaultHostnameVerifier());

        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_9))
        {
            // From the async HttpClient examples:

            // IMPORTANT uncomment the following method when running Java 9 or older
            // in order for ALPN support to work and avoid the illegal reflective
            // access operation warning
            tlsStrategyBuilder.setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>()
            {
                @Override
                public TlsDetails create(final SSLEngine sslEngine)
                {
                    return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                }
            });
        }

        return tlsStrategyBuilder;
    }

    /**
     * Creates a preconfigured builder for {@link SSLContext} objects. Sub classes may override this method to add
     * further customizations to the builder.
     * 
     * @param options
     *            the WebClient options
     * @return the builder
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    protected SSLContextBuilder createSslContextBuilder(final WebClientOptions options)
        throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        final SSLContextBuilder sslContextBuilder = SSLContexts.custom();

        // custom key store
        final KeyStore keyStore = options.getSSLClientCertificateStore();
        final char[] keyStorePassword = options.getSSLClientCertificatePassword();

        sslContextBuilder.loadKeyMaterial(keyStore, keyStorePassword);

        // custom trust store
        final boolean useInsecureSSL = options.isUseInsecureSSL();
        if (useInsecureSSL)
        {
            sslContextBuilder.loadTrustMaterial(new TrustAllStrategy());
        }
        else
        {
            final KeyStore trustStore = options.getSSLTrustStore();
            sslContextBuilder.loadTrustMaterial(trustStore, null);
        }

        return sslContextBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        connectionManager.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SimpleHttpRequest createRequestWithoutBody(final URI uri, final WebRequest webRequest)
    {
        return createRequest(uri, webRequest, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SimpleHttpRequest createRequestWithStringBody(final URI uri, final WebRequest webRequest, final String body, final String contentType,
                                                      final Charset charset)
    {
        return createRequest(uri, webRequest, body, charset, contentType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SimpleHttpRequest createRequestWithMultiPartBody(final URI uri, final WebRequest webRequest)
    {
        throw new UnsupportedOperationException("Multi-part requests not supported yet");
    }

    /**
     *
     */
    private SimpleHttpRequest createRequest(final URI uri, final WebRequest webRequest, final String requestBody, final Charset charset,
                                            final String mimeType)
    {
        SimpleHttpRequest request = new SimpleHttpRequest(webRequest.getHttpMethod().name(), uri);

        if (requestBody != null)
        {
            ContentType contentType = ContentType.create(mimeType, charset);
            request.setBody(requestBody, contentType);
        }

        // copy headers from HU request
        webRequest.getAdditionalHeaders().forEach(request::setHeader);

        return request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SimpleHttpResponse executeRequest(final CloseableHttpAsyncClient httpClient, final SimpleHttpRequest httpRequest)
        throws IOException
    {
        // first start the client if not done so far
        httpClient.start();

        try
        {
            final Future<SimpleHttpResponse> future = httpClient.execute(SimpleRequestProducer.create(httpRequest),
                                                                         SimpleResponseConsumer.create(), null);

            return future.get();
        }
        catch (final InterruptedException e)
        {
            throw new IOException(e);
        }
        catch (final ExecutionException e)
        {
            // throw the causing exception
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            else if (cause instanceof IOException)
            {
                throw (IOException) cause;
            }
            else
            {
                // need to wrap the exception so we can throw it
                throw new IOException(cause);
            }
        }
        finally
        {
            httpClient.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebResponse makeWebResponse(final SimpleHttpResponse response, final WebRequest webRequest, final long loadTime)
        throws IOException
    {
        response.getVersion();

        final List<NameValuePair> headers = toNamevaluePairs(response.getHeaders());
        final WebResponseData webResponseData = new WebResponseData(response.getBodyBytes(), response.getCode(), response.getReasonPhrase(),
                                                                    headers);
        final WebResponse webResponse = new WebResponse(webResponseData, webRequest, loadTime);

        webResponse.setProtocolVersion(response.getVersion().toString());

        return webResponse;
    }

    /**
     * Converts a {@link Header} array to a list of HtmlUnit {@link NameValuePair}.
     */
    private static List<NameValuePair> toNamevaluePairs(final Header[] headers)
    {
        final List<NameValuePair> nameValuePairs = new ArrayList<>();

        for (final Header header : headers)
        {
            nameValuePairs.add(new NameValuePair(header.getName(), header.getValue()));
        }

        return nameValuePairs;
    }

    /**
     * Converts an Apache {@link Header} array to the corresponding map.
     */
    private static Map<String, String> toMap(final Header[] headers)
    {
        final Map<String, String> htmlUnitHeaders = new LinkedHashMap<>();

        for (final Header header : headers)
        {
            htmlUnitHeaders.put(header.getName(), header.getValue());
        }

        return htmlUnitHeaders;
    }

    /**
     * Request interceptor that stores the final set of request headers (i.e. including those automatically added by the
     * HTTP client) at the original {@link WebRequest} instance for later display in the result browser.
     */
    private static class RetrieveFinalRequestHeadersInterceptor implements HttpRequestInterceptor
    {
        private WebRequest webRequest;

        private RetrieveFinalRequestHeadersInterceptor(WebRequest webRequest)
        {
            this.webRequest = webRequest;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException
        {
            webRequest.setAdditionalHeaders(toMap(request.getHeaders()));
        }
    }
}
