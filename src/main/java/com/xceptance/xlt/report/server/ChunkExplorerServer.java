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
package com.xceptance.xlt.report.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.xceptance.xlt.report.query.ColumnarQueryEngine;

/**
 * Lightweight embedded Jetty HTTP server hosting the ChunkDB Explorer UI, REST API ({@code /api/v1/*}),
 * and local Result Browser dumps ({@code /dumps/*}).
 * <p>
 * Binds strictly to {@code 127.0.0.1} by default so diagnostic test data is only accessible locally.
 */
public class ChunkExplorerServer implements AutoCloseable
{
    /** Default HTTP port for the ChunkDB Explorer server. */
    public static final int DEFAULT_PORT = 8484;

    /** Classpath root containing static Explorer UI assets (index.html, css/*, js/*). */
    private static final String CLASSPATH_UI_ROOT = "/com/xceptance/xlt/report/explorer";

    /** Extension-to-MIME-type lookup map for static UI and Result Browser assets. */
    private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
        Map.entry("html", "text/html; charset=UTF-8"),
        Map.entry("htm", "text/html; charset=UTF-8"),
        Map.entry("xhtml", "application/xhtml+xml; charset=UTF-8"),
        Map.entry("css", "text/css; charset=UTF-8"),
        Map.entry("js", "application/javascript; charset=UTF-8"),
        Map.entry("json", "application/json; charset=UTF-8"),
        Map.entry("har", "application/json; charset=UTF-8"),
        Map.entry("xml", "application/xml; charset=UTF-8"),
        Map.entry("txt", "text/plain; charset=UTF-8"),
        Map.entry("log", "text/plain; charset=UTF-8"),
        Map.entry("svg", "image/svg+xml"),
        Map.entry("png", "image/png"),
        Map.entry("jpg", "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("gif", "image/gif"),
        Map.entry("webp", "image/webp"),
        Map.entry("ico", "image/x-icon"),
        Map.entry("woff", "font/woff"),
        Map.entry("woff2", "font/woff2"),
        Map.entry("ttf", "font/ttf")
    );

    /** Backing columnar query engine. */
    private final ColumnarQueryEngine queryEngine;

    /** Embedded Jetty server instance. */
    private final Server server;

    /**
     * Constructs a {@link ChunkExplorerServer} bound to {@code 127.0.0.1} on the specified port.
     *
     * @param queryEngine
     *            the backing columnar query engine
     * @param port
     *            TCP port to listen on ({@code 0} selects an ephemeral port)
     */
    public ChunkExplorerServer(final ColumnarQueryEngine queryEngine, final int port)
    {
        this(queryEngine, "127.0.0.1", port);
    }

    /**
     * Constructs a {@link ChunkExplorerServer} bound to the specified host and port.
     *
     * @param queryEngine
     *            the backing columnar query engine
     * @param host
     *            bind address (e.g. "127.0.0.1")
     * @param port
     *            TCP port to listen on ({@code 0} selects an ephemeral port)
     */
    public ChunkExplorerServer(final ColumnarQueryEngine queryEngine, final String host, final int port)
    {
        this.queryEngine = Objects.requireNonNull(queryEngine, "queryEngine cannot be null");
        this.server = new Server(new InetSocketAddress(Objects.requireNonNull(host, "host cannot be null"), port));

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // 1. JSON Query API (/api/v1/*)
        context.addServlet(new ServletHolder(new ChunkApiServlet(queryEngine)), "/api/v1/*");

        // 2. Local Result Browser Dumps (/dumps/*)
        context.addServlet(new ServletHolder(new DumpResourceServlet(queryEngine.getResultsDir())), "/dumps/*");

        // 3. Classpath Explorer UI Static Assets (/ and /explorer/*)
        context.addServlet(new ServletHolder(new ExplorerStaticServlet()), "/*");

        this.server.setHandler(context);
    }

    /**
     * Starts the embedded Jetty server.
     *
     * @throws Exception
     *             if the server fails to bind or start
     */
    public void start() throws Exception
    {
        server.start();
    }

    /**
     * Blocks the calling thread until the Jetty server shuts down.
     *
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public void join() throws InterruptedException
    {
        server.join();
    }

    /**
     * Stops the embedded Jetty server.
     *
     * @throws Exception
     *             if an error occurs during shutdown
     */
    public void stop() throws Exception
    {
        server.stop();
    }

    @Override
    public void close() throws Exception
    {
        stop();
    }

    /**
     * Returns the actual local TCP port the server is listening on.
     *
     * @return local port number
     */
    public int getPort()
    {
        final ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        return connector.getLocalPort();
    }

    /**
     * Returns the base HTTP URL of the running server (e.g. {@code http://127.0.0.1:8484}).
     *
     * @return base HTTP URL
     */
    public String getBaseUrl()
    {
        return "http://127.0.0.1:" + getPort();
    }

    /**
     * Returns the backing {@link ColumnarQueryEngine}.
     *
     * @return query engine
     */
    public ColumnarQueryEngine getQueryEngine()
    {
        return queryEngine;
    }

    /**
     * Serves static UI assets (HTML, CSS, JS) from the classpath package {@code com/xceptance/xlt/report/explorer}.
     */
    private static final class ExplorerStaticServlet extends HttpServlet
    {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
        {
            String path = Objects.requireNonNullElse(req.getRequestURI(), "/");
            if (path.startsWith("/explorer"))
            {
                path = path.substring("/explorer".length());
            }
            if (path.isEmpty() || "/".equals(path))
            {
                path = "/index.html";
            }

            final String decodedPath;
            try
            {
                decodedPath = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
            }
            catch (final Exception e)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            final Path normalized = Path.of(decodedPath).normalize();
            final String normStr = normalized.toString().replace('\\', '/');
            if (normStr.startsWith("..") || normStr.contains("/../") || !normStr.startsWith("/"))
            {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            final String resourcePath = CLASSPATH_UI_ROOT + normStr;
            try (final InputStream in = ChunkExplorerServer.class.getResourceAsStream(resourcePath))
            {
                if (in == null)
                {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + path);
                    return;
                }
                final byte[] bytes = in.readAllBytes();
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType(guessContentType(path));
                resp.setHeader("Cache-Control", "no-cache");
                resp.setContentLength(bytes.length);
                resp.getOutputStream().write(bytes);
            }
        }
    }

    /**
     * Securely serves local Result Browser dump files under {@code <resultsDir>/<agent>/<scenario>/<user>/output/<dump>/*}.
     */
    private static final class DumpResourceServlet extends HttpServlet
    {
        private static final long serialVersionUID = 1L;
        private final File resultsDir;

        DumpResourceServlet(final File resultsDir)
        {
            this.resultsDir = resultsDir;
        }

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
        {
            if (resultsDir == null)
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No raw results directory mounted for Result Browser dumps");
                return;
            }

            final String pathInfo = Objects.requireNonNullElse(req.getPathInfo(), "");
            final String decodedPathInfo;
            try
            {
                decodedPathInfo = java.net.URLDecoder.decode(pathInfo, StandardCharsets.UTF_8);
            }
            catch (final Exception e)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (decodedPathInfo.contains("..") || !decodedPathInfo.contains("/output/"))
            {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Result Browser dump path");
                return;
            }

            final Path base = resultsDir.toPath().toAbsolutePath().normalize();
            final String relPath = decodedPathInfo.startsWith("/") ? decodedPathInfo.substring(1) : decodedPathInfo;
            final Path target = base.resolve(relPath).normalize();
            if (!target.startsWith(base))
            {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Path traversal forbidden");
                return;
            }
            if (!Files.isRegularFile(target))
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(guessContentType(target.getFileName().toString()));
            resp.setContentLengthLong(Files.size(target));
            Files.copy(target, resp.getOutputStream());
        }
    }

    static String guessContentType(final String path)
    {
        final int dot = path.lastIndexOf('.');
        final String ext = dot >= 0 ? path.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        return CONTENT_TYPES.getOrDefault(ext, "application/octet-stream");
    }
}
