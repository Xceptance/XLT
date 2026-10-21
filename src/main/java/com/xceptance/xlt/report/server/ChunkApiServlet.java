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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xceptance.xlt.report.query.ColumnarQueryEngine;
import com.xceptance.xlt.report.query.QueryModels.DashboardQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.InspectQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.SessionQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.SummaryTableQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.TimeSeriesQuerySpec;

/**
 * Embedded Jetty HTTP servlet exposing the ChunkDB Explorer REST API under {@code /api/v1/*}.
 * <p>
 * <b>Supported Endpoints:</b>
 * <ul>
 *   <li>{@code GET /api/v1/metadata}: Global time bounds, chunk counts, and distinct dimension facets.</li>
 *   <li>{@code POST /api/v1/dashboard}: Combined single-pass time-series and summary-table query.</li>
 *   <li>{@code POST /api/v1/timeseries}: Auto-bucketed time-series metrics, exact percentiles, and group splits.</li>
 *   <li>{@code POST /api/v1/summary-table}: Multi-dimensional GROUP BY aggregation table.</li>
 *   <li>{@code POST /api/v1/inspect}: Top-K slowest requests, failed requests, and failed transactions with forensic tail details.</li>
 *   <li>{@code GET|POST /api/v1/session}: Single virtual user iteration reconstruction waterfall.</li>
 * </ul>
 */
public class ChunkApiServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    /** Backing columnar query engine. */
    private final transient ColumnarQueryEngine queryEngine;

    /** Thread-safe Jackson mapper for JSON request/response serialization. */
    private final transient ObjectMapper mapper;

    /** Fair execution lock ensuring two parallelStream() chunk scans never compete simultaneously. */
    private final transient ReentrantLock queryLock = new ReentrantLock(true);

    /**
     * Constructs a {@link ChunkApiServlet} backed by the given {@link ColumnarQueryEngine}.
     *
     * @param queryEngine
     *            the columnar query engine
     */
    public ChunkApiServlet(final ColumnarQueryEngine queryEngine)
    {
        this.queryEngine = Objects.requireNonNull(queryEngine, "queryEngine cannot be null");
        this.mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp)
    {
        applyStandardHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        applyStandardHeaders(resp);
        final String path = normalizePath(req.getPathInfo());

        try
        {
            switch (path)
            {
                case "/metadata" -> {
                    // Fast-path: once immutable metadata is cached in volatile memory, serve directly
                    // without acquiring queryLock so metadata requests never queue behind active scans.
                    if (queryEngine.hasCachedMetadata())
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.getMetadata());
                    }
                    else
                    {
                        queryLock.lock();
                        try
                        {
                            writeJson(resp, HttpServletResponse.SC_OK, queryEngine.getMetadata());
                        }
                        finally
                        {
                            queryLock.unlock();
                        }
                    }
                }
                case "/session" -> {
                    final String agent = firstNonEmpty(req.getParameter("agent"), "");
                    final String scenario = firstNonEmpty(req.getParameter("scenario"), req.getParameter("testCase"), "");
                    final String userNumber = firstNonEmpty(req.getParameter("userNumber"), req.getParameter("user"), "0");
                    final long targetTime = parseLongParam(firstNonEmpty(req.getParameter("targetTime"), req.getParameter("time")), 0L);
                    final long paddingMs = parseLongParam(req.getParameter("paddingMs"), 30_000L);
                    final SessionQuerySpec spec = new SessionQuerySpec(agent, scenario, userNumber, targetTime, paddingMs);
                    queryLock.lock();
                    try
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.traceUserSession(spec));
                    }
                    finally
                    {
                        queryLock.unlock();
                    }
                }
                default -> writeJson(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    Map.of("error", "Unknown GET endpoint: " + path)
                );
            }
        }
        catch (final Exception e)
        {
            writeJson(
                resp,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                Map.of("error", Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName()))
            );
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        applyStandardHeaders(resp);
        final String path = normalizePath(req.getPathInfo());

        try
        {
            switch (path)
            {
                case "/dashboard" -> {
                    final DashboardQuerySpec spec = readJsonBody(req, DashboardQuerySpec.class);
                    queryLock.lock();
                    try
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.queryDashboard(spec));
                    }
                    finally
                    {
                        queryLock.unlock();
                    }
                }
                case "/timeseries" -> {
                    final TimeSeriesQuerySpec spec = readJsonBody(req, TimeSeriesQuerySpec.class);
                    queryLock.lock();
                    try
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.queryTimeSeries(spec));
                    }
                    finally
                    {
                        queryLock.unlock();
                    }
                }
                case "/summary-table" -> {
                    final SummaryTableQuerySpec spec = readJsonBody(req, SummaryTableQuerySpec.class);
                    queryLock.lock();
                    try
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.querySummaryTable(spec));
                    }
                    finally
                    {
                        queryLock.unlock();
                    }
                }
                case "/inspect" -> {
                    final InspectQuerySpec spec = readJsonBody(req, InspectQuerySpec.class);
                    queryLock.lock();
                    try
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.inspectOutliers(spec));
                    }
                    finally
                    {
                        queryLock.unlock();
                    }
                }
                case "/session" -> {
                    final SessionQuerySpec spec = readJsonBody(req, SessionQuerySpec.class);
                    queryLock.lock();
                    try
                    {
                        writeJson(resp, HttpServletResponse.SC_OK, queryEngine.traceUserSession(spec));
                    }
                    finally
                    {
                        queryLock.unlock();
                    }
                }
                default -> writeJson(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    Map.of("error", "Unknown POST endpoint: " + path)
                );
            }
        }
        catch (final Exception e)
        {
            writeJson(
                resp,
                HttpServletResponse.SC_BAD_REQUEST,
                Map.of("error", Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName()))
            );
        }
    }

    private <T> T readJsonBody(final HttpServletRequest req, final Class<T> clazz) throws IOException
    {
        try (final InputStream in = req.getInputStream())
        {
            final byte[] bytes = in.readAllBytes();
            if (bytes.length == 0)
            {
                return mapper.readValue("{}", clazz);
            }
            return mapper.readValue(bytes, clazz);
        }
    }

    private void writeJson(final HttpServletResponse resp, final int status, final Object payload) throws IOException
    {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        final byte[] jsonBytes = mapper.writeValueAsBytes(payload);
        resp.setContentLength(jsonBytes.length);
        resp.getOutputStream().write(jsonBytes);
    }

    private static void applyStandardHeaders(final HttpServletResponse resp)
    {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        resp.setHeader("Cache-Control", "no-store");
    }

    private static String normalizePath(final String pathInfo)
    {
        final String p = Objects.requireNonNullElse(pathInfo, "/");
        if (p.length() > 1 && p.endsWith("/"))
        {
            return p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static String firstNonEmpty(final String... candidates)
    {
        for (final String c : candidates)
        {
            if (c != null && !c.isBlank())
            {
                return c;
            }
        }
        return "";
    }

    private static long parseLongParam(final String val, final long fallback)
    {
        try
        {
            return Long.parseLong(Objects.requireNonNullElse(val, "").trim());
        }
        catch (final NumberFormatException e)
        {
            return fallback;
        }
    }
}
