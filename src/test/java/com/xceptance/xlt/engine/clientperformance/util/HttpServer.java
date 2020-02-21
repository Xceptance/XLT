package com.xceptance.xlt.engine.clientperformance.util;

import java.io.IOException;

/**
 * Simple HTTP server for testing purposes which responds with configurable timings.
 */
public class HttpServer implements AutoCloseable
{
    /**
     * The HTTP connector.
     */
    private HttpConnector httpConnector;

    /**
     * The HTTPS connector.
     */
    private HttpConnector httpsConnector;

    /**
     * Creates a new HTTP server with HTTP/HTTPS connectors on port 80/443.
     */
    public HttpServer() throws IOException
    {
        this(80, 443);
    }

    /**
     * Creates a new HTTP server with HTTP/HTTPS connectors on the given ports.
     */
    public HttpServer(int httpPort, int httpsPort) throws IOException
    {
        if (httpPort >= 0)
        {
            httpConnector = new HttpConnector(httpPort, false);
        }

        if (httpsPort >= 0)
        {
            httpsConnector = new HttpConnector(httpsPort, true);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close()
    {
        if (httpConnector != null)
        {
            httpConnector.close();
        }

        if (httpsConnector != null)
        {
            httpsConnector.close();
        }
    }

    public static void main(String[] args) throws IOException
    {
        try (HttpServer httpServer = new HttpServer())
        {
            System.in.read();
        }
    }
}
