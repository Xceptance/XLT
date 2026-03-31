package com.xceptance.xlt.engine.htmlunit.jdk26;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nullable;

import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

import com.xceptance.xlt.engine.htmlunit.AbstractWebConnection;

/**
 * An alternative {@link org.htmlunit.WebConnection} implementation that uses JDK 26 HttpClient.
 */
public class Jdk26WebConnection extends AbstractWebConnection<HttpClient, HttpRequest, HttpResponse<InputStream>>
{
    private final boolean collectTargetIpAddress;
    
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
    public Jdk26WebConnection(final WebClient webClient, final boolean collectTargetIpAddress)
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
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "jdk26-httpclient-" + (++count));
                    t.setDaemon(true);
                    return t;
                }
            });

            this.httpClient = HttpClient.newBuilder()
                    .executor(this.executor)
                    // TODO: timeouts, proxy, SSL, and DNS hook
                    .build();
        }
        return this.httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
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
        // TODO: Implement request creation
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequest createRequestWithStringBody(final URI uri, final WebRequest webRequest, final String body, final String mimeType, final @Nullable Charset charset)
    {
        // TODO: Implement string body request creation
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequest createRequestWithMultiPartBody(final URI uri, final WebRequest webRequest)
    {
        // TODO: Implement multipart body request creation
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpResponse<InputStream> executeRequest(final HttpClient httpClient, final HttpRequest request) throws IOException
    {
        // TODO: Execute request and collect telemetry
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebResponse makeWebResponse(final HttpResponse<InputStream> response, final WebRequest webRequest, final long loadTime) throws IOException
    {
        // TODO: Transform to HtmlUnit WebResponse
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
