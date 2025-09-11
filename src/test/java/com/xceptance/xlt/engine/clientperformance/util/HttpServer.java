/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
