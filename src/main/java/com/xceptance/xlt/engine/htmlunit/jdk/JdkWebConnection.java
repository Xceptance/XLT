/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MoreBodyPublishers;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.xceptance.xlt.engine.RequestExecutionContext;
import com.xceptance.xlt.engine.socket.SocketMonitor;
import java.net.SocketAddress;
import java.net.ProxySelector;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.io.UncheckedIOException;
import java.net.ProxySelector;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.htmlunit.HttpHeader;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.KeyDataPair;
import org.htmlunit.util.NameValuePair;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;

import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

import com.xceptance.xlt.engine.htmlunit.AbstractWebConnection;

import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import com.xceptance.common.util.ssl.EasyX509TrustManager;
import org.apache.http.impl.EnglishReasonPhraseCatalog;

public class JdkWebConnection extends AbstractWebConnection<HttpClient, HttpRequest, HttpResponse<InputStream>>
{
    private static final SSLContext INSECURE_SSL_CONTEXT;
    private static final SSLParameters INSECURE_SSL_PARAMETERS;

    static {
        try {
            final TrustManager[] trustManagers = new TrustManager[] {
                new EasyX509TrustManager(null)
            };
            
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            
            INSECURE_SSL_CONTEXT = sslContext;
            
            INSECURE_SSL_PARAMETERS = new SSLParameters();
            INSECURE_SSL_PARAMETERS.setEndpointIdentificationAlgorithm("");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to initialize insecure SSL context", e);
        }
    }
    @SuppressWarnings("unused")
    private final boolean collectTargetIpAddress;
    
    public static final ThreadLocal<Boolean> IS_JDK_THREAD = ThreadLocal.withInitial(() -> false);
    
    // HttpClient instance cached for the transaction lifecycle
    private HttpClient httpClient;
    // Executor tied exclusively to this connection
    private ExecutorService executor;

    /**
     * Constructor.
     *
     * @param webClient
     *            the owning web client
     * @param collectTargetIpAddress
     *            whether to collect the target IP address that was used to make the request
     */
    public JdkWebConnection(final WebClient webClient, final boolean collectTargetIpAddress)
    {
        super(webClient);
        this.collectTargetIpAddress = collectTargetIpAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpClient createHttpClient(final WebClient webClient, final WebRequest webRequest) throws Exception
    {
        if (this.httpClient == null)
        {
            this.executor = Executors.newCachedThreadPool(new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Runnable wrapper = () -> {
                        IS_JDK_THREAD.set(true);
                        try {
                            r.run();
                        } finally {
                            IS_JDK_THREAD.remove();
                        }
                    };
                    
                    Thread t = new Thread(wrapper, "jdk-httpclient-" + count.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });

            HttpClient.Builder builder = HttpClient.newBuilder()
                    .executor(this.executor)
                    .followRedirects(HttpClient.Redirect.NEVER);

            // Configure proxy if present
            final String proxyHost = webRequest.getProxyHost();
            if (proxyHost != null)
            {
                final InetSocketAddress proxyHostAddress = new InetSocketAddress(proxyHost, webRequest.getProxyPort());
                final Proxy proxy = new Proxy(webRequest.isSocksProxy() ? Proxy.Type.SOCKS : Proxy.Type.HTTP, proxyHostAddress);
                
                builder.proxy(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) {
                        return Collections.singletonList(proxy);
                    }
                    
                    @Override
                    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                        // no-op
                    }
                });
            }

            // Route JVM-level authentication requests back to XLT's credentials provider
            final CredentialsProvider credentialsProvider = webClient.getCredentialsProvider();
            if (credentialsProvider != null)
            {
                builder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // Default to ANY scope if precise matching fails, 
                        // matching XLT's legacy behavior where precise scheme mapping isn't always possible here.
                        AuthScope expectedScope = new AuthScope(
                                getRequestingHost(), getRequestingPort(), getRequestingPrompt(), getRequestingScheme()
                        );
                        Credentials creds = credentialsProvider.getCredentials(expectedScope);
                        if (creds == null) {
                            creds = credentialsProvider.getCredentials(AuthScope.ANY);
                        }

                        if (creds != null && creds.getUserPrincipal() != null) {
                            return new PasswordAuthentication(
                                    creds.getUserPrincipal().getName(), 
                                    creds.getPassword() != null ? creds.getPassword().toCharArray() : new char[0]
                            );
                        }
                        return null;
                    }
                });
            }

            // Ensure WebClient insecure SSL setting is respected
            if (webClient.getOptions().isUseInsecureSSL()) {
                builder.sslContext(INSECURE_SSL_CONTEXT);
                builder.sslParameters(INSECURE_SSL_PARAMETERS);
            }

            // TODO: timeouts
            this.httpClient = builder.build();
        }
        return this.httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        if (this.httpClient != null)
        {
            this.httpClient.close();
            this.httpClient = null;
        }
        if (this.executor != null)
        {
            this.executor.shutdownNow();
            this.executor = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequest createRequestWithoutBody(final URI uri, final WebRequest webRequest)
    {
        return createRequest(uri, webRequest, HttpRequest.BodyPublishers.noBody());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequest createRequestWithStringBody(final URI uri, final WebRequest webRequest, final String body, final String mimeType, final @Nullable Charset charset)
    {
        String contentType = webRequest.getAdditionalHeader(HttpHeader.CONTENT_TYPE);
        if (contentType == null)
        {
            contentType = (charset == null) ? mimeType : mimeType + ";charset=" + charset;
        }

        final byte[] bytes;
        if (charset != null) {
            bytes = body.getBytes(charset);
        } else {
            bytes = body.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        }

        webRequest.setAdditionalHeader(HttpHeader.CONTENT_TYPE, contentType);

        return createRequest(uri, webRequest, HttpRequest.BodyPublishers.ofByteArray(bytes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequest createRequestWithMultiPartBody(final URI uri, final WebRequest webRequest)
    {
        final MultipartBodyPublisher.Builder builder = 
            MultipartBodyPublisher.newBuilder();

        for (final NameValuePair pair : webRequest.getRequestParameters())
        {
            if (pair instanceof KeyDataPair)
            {
                final KeyDataPair filePair = (KeyDataPair) pair;
                
                String mimeType = filePair.getMimeType();
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                
                MediaType mediaType = 
                    MediaType.parse(mimeType);
                
                if (filePair.getFile() != null) 
                {
                    try {
                        String filename = filePair.getFileName();
                        if (filename == null) {
                            filename = filePair.getFile().getName();
                        }
                        HttpRequest.BodyPublisher filePublisher = HttpRequest.BodyPublishers.ofInputStream(() -> {
                            try {
                                return Files.newInputStream(filePair.getFile().toPath());
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                        builder.formPart(
                            filePair.getName(), 
                            filename, 
                            MoreBodyPublishers.ofMediaType(filePublisher, mediaType)
                        );
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot find upload file: " + filePair.getFile(), e);
                    }
                } 
                else if (filePair.getData() != null) 
                {
                    HttpRequest.BodyPublisher dataPublisher = HttpRequest.BodyPublishers.ofByteArray(filePair.getData());
                    builder.formPart(
                        filePair.getName(), 
                        filePair.getValue(), 
                        MoreBodyPublishers.ofMediaType(dataPublisher, mediaType)
                    );
                }
            }
            else
            {
                builder.textPart(pair.getName(), pair.getValue());
            }
        }

        final MultipartBodyPublisher publisher = builder.build();

        webRequest.removeAdditionalHeader(HttpHeader.CONTENT_TYPE);
        webRequest.setAdditionalHeader(HttpHeader.CONTENT_TYPE, 
            "multipart/form-data; boundary=" + publisher.boundary());

        return createRequest(uri, webRequest, publisher);
    }

    private HttpRequest createRequest(final URI uri, final WebRequest webRequest, final HttpRequest.BodyPublisher bodyPublisher)
    {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);

        builder.method(webRequest.getHttpMethod().name(), bodyPublisher);

        webRequest.getAdditionalHeaders().forEach((name, value) -> {
            // The JDK HttpClient restricts injecting structural headers manually
            if (!"Host".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name) && !"Connection".equalsIgnoreCase(name)) {
                builder.header(name, value);
            }
        });

        // timeout
        int timeout = webRequest.getTimeout();
        if (timeout < 0) {
            timeout = getWebClient().getOptions().getTimeout();
        }
        if (timeout > 0) {
            builder.timeout(Duration.ofMillis(timeout));
        }

        return builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpResponse<InputStream> executeRequest(final HttpClient httpClient, final HttpRequest request) throws IOException
    {
        final SocketMonitor socketMonitor = 
            RequestExecutionContext.getCurrent().getSocketMonitor();

        // 1. Fake Connect Time bounds
        // WARNING: JDK 11+ java.net.http.HttpClient permanently obscures native socket metrics
        // inside its opaque connection pool. It does not provide any public EventListener or SPI
        // to hook the true TCP `connect()` latency, unlike OkHttp's EventListener.
        // Therefore, connect time will always be logged as a strict 0ms heuristic.
        socketMonitor.connectingStarted();
        socketMonitor.connected(); 

        // 2. Estimate Request payload and hook Sent Bytes
        // Note: Payload upload metrics are heuristic approximations rather than literal socket
        // bytes written, as JDK HttpClient obscures native TLS socket frames.
        long approxRequestBytes = request.uri().toString().length() + 200L; // Headers + URI
        if (request.bodyPublisher().isPresent()) {
            long contentLength = request.bodyPublisher().get().contentLength();
            if (contentLength > 0) {
                approxRequestBytes += contentLength;
            }
        }
        
        long remaining = approxRequestBytes;
        while (remaining > 0) {
            int chunk = (int) Math.min(remaining, Integer.MAX_VALUE);
            socketMonitor.wrote(chunk);
            remaining -= chunk;
        }

        // 3. Hook TTFB and Response parsing via a BodySubscriber wrapper
        HttpResponse.BodyHandler<InputStream> baseHandler = HttpResponse.BodyHandlers.ofInputStream();
        HttpResponse.BodyHandler<InputStream> wrappedHandler = new HttpResponse.BodyHandler<InputStream>() {
            @Override
            public HttpResponse.BodySubscriber<InputStream> apply(HttpResponse.ResponseInfo responseInfo) {
                // When apply is called, response headers have just arrived. This is our TTFB!
                int approxHeaderBytes = 256; // Base protocol overhead
                for (Map.Entry<String, List<String>> entry : responseInfo.headers().map().entrySet()) {
                    approxHeaderBytes += entry.getKey().length();
                    for (String val : entry.getValue()) approxHeaderBytes += val.length();
                }
                
                // Record TTFB and header bytes
                socketMonitor.read(approxHeaderBytes);

                final HttpResponse.BodySubscriber<InputStream> baseSubscriber = baseHandler.apply(responseInfo);
                
                return new HttpResponse.BodySubscriber<InputStream>() {
                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        baseSubscriber.onSubscribe(subscription);
                    }
                    
                    @Override
                    public void onNext(List<ByteBuffer> item) {
                        int chunk = 0;
                        for (ByteBuffer b : item) {
                            chunk += b.remaining();
                        }
                        if (chunk > 0) {
                            socketMonitor.read(chunk);
                        }
                        baseSubscriber.onNext(item);
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {
                        baseSubscriber.onError(throwable);
                    }
                    
                    @Override
                    public void onComplete() {
                        baseSubscriber.onComplete();
                    }
                    
                    @Override
                    public CompletionStage<InputStream> getBody() {
                        return baseSubscriber.getBody();
                    }
                };
            }
        };

        try {
            IS_JDK_THREAD.set(true);
            return httpClient.send(request, wrappedHandler);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } finally {
            IS_JDK_THREAD.remove();
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    protected WebResponse makeWebResponse(final HttpResponse<InputStream> response, final WebRequest webRequest, final long loadTime) throws IOException
    {
        try (InputStream responseBody = response.body())
        {
            byte[] bytes = responseBody.readAllBytes();
            List<NameValuePair> headers = new ArrayList<>();
            response.headers().map().forEach((name, values) -> {
                for (String val : values) {
                    headers.add(new NameValuePair(name, val));
                }
            });

            final WebResponseData webResponseData = new WebResponseData(bytes, response.statusCode(), getReasonPhrase(response.statusCode()), headers);
            final WebResponse webResponse = new WebResponse(webResponseData, webRequest, loadTime);

            String protocolVersion;
            switch (response.version()) {
                case HTTP_1_1:
                    protocolVersion = "http/1.1";
                    break;
                case HTTP_2:
                    protocolVersion = "h2";
                    break;
                default:
                    protocolVersion = response.version().name();
                    break;
            }
            webResponse.setProtocolVersion(protocolVersion);

            return webResponse;
        }
    }

    /**
     * Attempts to map a standard HTTP status code back to a default reason phrase.
     * JDK 11's HttpClient does not natively return reason phrases.
     */
    private static String getReasonPhrase(int statusCode) {
        try {
            String reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
            return reason != null ? reason : "";
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
