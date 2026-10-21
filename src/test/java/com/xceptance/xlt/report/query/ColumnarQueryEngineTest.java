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
package com.xceptance.xlt.report.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.report.query.QueryModels.ForensicRecord;
import com.xceptance.xlt.report.query.QueryModels.InspectQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.InspectResponse;
import com.xceptance.xlt.report.query.QueryModels.MetadataResponse;
import com.xceptance.xlt.report.query.QueryModels.SessionNode;
import com.xceptance.xlt.report.query.QueryModels.SessionQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.SessionResponse;
import com.xceptance.xlt.report.query.QueryModels.SummaryRow;
import com.xceptance.xlt.report.query.QueryModels.SummaryTableQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.SummaryTableResponse;
import com.xceptance.xlt.report.query.QueryModels.TimeSeriesQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.TimeSeriesResponse;
import com.xceptance.xlt.report.storage.ChunkIngestionCollector;
import com.xceptance.xlt.report.storage.ChunkStorage;

/**
 * Unit and disk-integration tests for {@link ColumnarQueryEngine}.
 */
public class ColumnarQueryEngineTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private static final long BASE_TIME = 1_700_000_000_000L;

    /**
     * Populates a representative multi-type dataset into a disk-backed {@link ChunkStorage}
     * and creates a mock Result Browser dump directory on disk.
     */
    private ChunkStorage buildSampleStorage(final File cacheDir, final File resultsDir) throws Exception
    {
        // Create a mock Result Browser dump index.html for user "0" on "agent-1" / "TCheckout"
        final File dumpDir = new File(resultsDir, "agent-1/TCheckout/0/output/1700000005000-PlaceOrder");
        dumpDir.mkdirs();
        Files.writeString(new File(dumpDir, "index.html").toPath(), "<html><body>Dump</body></html>", StandardCharsets.UTF_8);

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(cacheDir);

        // Iteration 1 for (agent-1, TCheckout, user 0): [BASE_TIME .. BASE_TIME + 2000] (Success)
        final TransactionData tx1 = new TransactionData("TCheckout");
        tx1.setTime(BASE_TIME);
        tx1.setRunTime(2000);
        tx1.setFailed(false);
        tx1.setAgentName("agent-1");
        tx1.setTransactionName("TCheckout");
        tx1.setTestUserNumber("0");
        collector.collect(tx1);

        final ActionData act1 = new ActionData("OpenHome");
        act1.setTime(BASE_TIME + 10);
        act1.setRunTime(800);
        act1.setFailed(false);
        act1.setAgentName("agent-1");
        act1.setTransactionName("TCheckout");
        act1.setTestUserNumber("0");
        collector.collect(act1);

        final RequestData req1 = createRequest(
            "OpenHome.1", "OpenHome.1.raw", BASE_TIME + 20, 120, 200, false,
            "agent-1", "TCheckout", "0", "GET", "text/html", "10.0.0.1",
            "https://shop.example.com/home?v=1", "req-1", "resp-1", ""
        );
        collector.collect(req1);

        final RequestData req2 = createRequest(
            "OpenHome.1", "OpenHome.2.raw", BASE_TIME + 200, 180, 200, false,
            "agent-1", "TCheckout", "0", "GET", "text/html", "10.0.0.1",
            "https://shop.example.com/home?v=2", "req-2", "resp-2", ""
        );
        collector.collect(req2);

        final PageLoadTimingData page1 = new PageLoadTimingData("DomContentLoaded");
        page1.setTime(BASE_TIME + 350);
        page1.setRunTime(95);
        page1.setFailed(false);
        page1.setAgentName("agent-1");
        page1.setTransactionName("TCheckout");
        page1.setTestUserNumber("0");
        collector.collect(page1);

        // Iteration 2 for (agent-1, TCheckout, user 0): [BASE_TIME + 5000 .. BASE_TIME + 9500] (Failed with dump)
        final TransactionData tx2 = new TransactionData("TCheckout");
        tx2.setTime(BASE_TIME + 5000);
        tx2.setRunTime(4500);
        tx2.setFailed(true);
        tx2.setFailedActionName("PlaceOrder");
        tx2.setFailureStackTrace("java.lang.AssertionError: HTTP 503 Gateway Timeout");
        tx2.setDirectoryName("1700000005000-PlaceOrder");
        tx2.setAgentName("agent-1");
        tx2.setTransactionName("TCheckout");
        tx2.setTestUserNumber("0");
        collector.collect(tx2);

        final ActionData act2 = new ActionData("PlaceOrder");
        act2.setTime(BASE_TIME + 5050);
        act2.setRunTime(4400);
        act2.setFailed(true);
        act2.setAgentName("agent-1");
        act2.setTransactionName("TCheckout");
        act2.setTestUserNumber("0");
        collector.collect(act2);

        final RequestData req3 = createRequest(
            "PlaceOrder.1", "PlaceOrder.1.submit", BASE_TIME + 5100, 4200, 503, true,
            "agent-1", "TCheckout", "0", "POST", "application/json", "10.0.0.2",
            "https://shop.example.com/api/order", "req-3", "resp-3", "{\"sku\":\"ABC-99\"}"
        );
        collector.collect(req3);

        final EventData ev1 = new EventData("OrderRetryWarning");
        ev1.setTestCaseName("TCheckout");
        ev1.setMessage("Backend pool exhausted on 10.0.0.2");
        ev1.setTime(BASE_TIME + 5200);
        ev1.setAgentName("agent-1");
        ev1.setTransactionName("TCheckout");
        ev1.setTestUserNumber("0");
        collector.collect(ev1);

        final CustomData cust1 = new CustomData("PaymentGatewaySign");
        cust1.setTime(BASE_TIME + 5150);
        cust1.setRunTime(45);
        cust1.setFailed(false);
        cust1.setAgentName("agent-1");
        cust1.setTransactionName("TCheckout");
        cust1.setTestUserNumber("0");
        collector.collect(cust1);

        // Iteration 3 for (agent-1, TCheckout, user 0): [BASE_TIME + 12000 .. BASE_TIME + 13000]
        final TransactionData tx3 = new TransactionData("TCheckout");
        tx3.setTime(BASE_TIME + 12000);
        tx3.setRunTime(1000);
        tx3.setFailed(false);
        tx3.setAgentName("agent-1");
        tx3.setTransactionName("TCheckout");
        tx3.setTestUserNumber("0");
        collector.collect(tx3);

        // Second agent/scenario: (agent-2, TBrowse, user 1)
        final RequestData req4 = createRequest(
            "SearchProducts", "SearchProducts.1", BASE_TIME + 6000, 310, 200, false,
            "agent-2", "TBrowse", "1", "GET", "application/json", "10.0.0.1",
            "https://shop.example.com/api/search?q=shoes", "req-4", "resp-4", ""
        );
        collector.collect(req4);

        try (final ChunkStorage built = collector.finish())
        {
            built.saveToDirectory(cacheDir);
        }
        return ChunkStorage.loadFromDirectory(cacheDir);
    }

    private static RequestData createRequest(final String name,
                                             final String rawName,
                                             final long time,
                                             final int runTime,
                                             final int status,
                                             final boolean failed,
                                             final String agent,
                                             final String scenario,
                                             final String userNumber,
                                             final String method,
                                             final String contentType,
                                             final String usedIp,
                                             final String url,
                                             final String reqId,
                                             final String respId,
                                             final String formData)
    {
        final RequestData r = new RequestData(name);
        r.setRawName(rawName);
        r.setTime(time);
        r.setRunTime(runTime);
        r.setResponseCode(status);
        r.setFailed(failed);
        r.setAgentName(agent);
        r.setTransactionName(scenario);
        r.setTestUserNumber(userNumber);
        r.setHttpMethod(method);
        r.setContentType(contentType);
        r.setUsedIpAddress(usedIp);
        r.setUrl(url);
        r.setRequestId(reqId);
        r.setResponseId(respId);
        r.setFormData(formData);
        r.setBytesSent(250);
        r.setBytesReceived(4096);
        r.setDnsTime(5);
        r.setConnectTime(10);
        r.setSendTime(2);
        r.setServerBusyTime(runTime - 25);
        r.setReceiveTime(8);
        r.setTimeToFirstBytes(runTime - 8);
        r.setTimeToLastBytes(runTime);
        return r;
    }

    @Test
    public void testMetadataDiscoverySeparatesFacetColumns() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-meta");
        final File resultsDir = tempFolder.newFolder("results-meta");
        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);
            final MetadataResponse meta = engine.getMetadata();

            assertEquals(BASE_TIME, meta.minTime());
            assertTrue(meta.maxTime() >= BASE_TIME + 12000);
            assertEquals(12L, meta.totalRecords());
            assertEquals(List.of("agent-1", "agent-2"), meta.agents());
            assertEquals(List.of("TBrowse", "TCheckout"), meta.scenarios());
            assertEquals(List.of("GET", "POST"), meta.httpMethods());
            assertEquals(List.of("application/json", "text/html"), meta.contentTypes());
            assertEquals(List.of("10.0.0.1", "10.0.0.2"), meta.usedIps());

            // Verify timers are partitioned by record type
            assertEquals(List.of("OpenHome.1", "PlaceOrder.1", "SearchProducts"), meta.timers().get("R"));
            assertEquals(List.of("OpenHome", "PlaceOrder"), meta.timers().get("A"));
            assertEquals(List.of("TCheckout"), meta.timers().get("T"));

            // Verify caching returns the identical instance
            assertTrue(meta == engine.getMetadata());
        }
    }

    @Test
    public void testTimeSeriesQueryAndGrouping() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-ts");
        final File resultsDir = tempFolder.newFolder("results-ts");
        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            final TimeSeriesQuerySpec spec = new TimeSeriesQuerySpec(
                "R", BASE_TIME, BASE_TIME + 10_000L, 5000,
                null, null, null, null, null, null, null,
                null, null, false, null, "USED_IP"
            );
            final TimeSeriesResponse res = engine.queryTimeSeries(spec);

            assertEquals("R", res.recordType());
            assertEquals(5000, res.bucketWidthMs());
            assertEquals(4L, res.rowsMatched());

            // Bucket 0 ([BASE_TIME .. BASE_TIME + 4999]) has 2 requests (120ms, 180ms)
            assertEquals(2, res.count()[0]);
            assertEquals(0, res.errors()[0]);
            assertEquals(150.0, res.mean()[0], 0.01);
            assertEquals(120, res.min()[0]);
            assertEquals(180, res.max()[0]);

            // Bucket 1 ([BASE_TIME + 5000 .. BASE_TIME + 9999]) has 2 requests (4200ms 503 error, 310ms 200 OK)
            assertEquals(2, res.count()[1]);
            assertEquals(1, res.errors()[1]);
            assertEquals(4200, res.max()[1]);

            // Verify grouped series by USED_IP ("10.0.0.1" has 3 requests, "10.0.0.2" has 1 error request)
            assertEquals(2, res.series().size());
            assertEquals("10.0.0.1", res.series().get(0).key());
            assertEquals(3L, res.series().get(0).totalCount());
            assertEquals("10.0.0.2", res.series().get(1).key());
            assertEquals(1L, res.series().get(1).totalCount());
            assertEquals(1L, res.series().get(1).totalErrors());

            // Also verify grouping by STATUS_CODE, AGENT, SCENARIO, TIMER, HTTP_METHOD, CONTENT_TYPE and non-Request types
            for (final String dim : List.of("STATUS_CODE", "AGENT", "SCENARIO", "TIMER", "HTTP_METHOD", "CONTENT_TYPE"))
            {
                final TimeSeriesResponse grouped = engine.queryTimeSeries(new TimeSeriesQuerySpec(
                    "R", 0L, 0L, 0, null, ".*", List.of("agent-1", "agent-2"), List.of("TCheckout", "TBrowse"),
                    List.of("10.0.0.1", "10.0.0.2"), List.of("GET", "POST"), List.of("text/html", "application/json"),
                    200, 599, false, 10, dim
                ));
                assertEquals(4L, grouped.rowsMatched());
            }

            for (final String rType : List.of("T", "A", "P", "C"))
            {
                final TimeSeriesResponse tr = engine.queryTimeSeries(new TimeSeriesQuerySpec(
                    rType, 0L, 0L, 0, null, null, null, null, null, null, null,
                    null, null, false, null, "TIMER"
                ));
                assertTrue(tr.rowsMatched() >= 1L);
            }
        }
    }

    @Test
    public void testSummaryTableDimensionsAndDistinctUrls() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-sum");
        final File resultsDir = tempFolder.newFolder("results-sum");
        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            // 1. Group by TIMER
            final SummaryTableResponse byTimer = engine.querySummaryTable(new SummaryTableQuerySpec(
                "R", 0L, 0L, null, null, null, null, null, null, null,
                null, null, false, null, "TIMER"
            ));
            assertEquals(4L, byTimer.totalRowsMatched());
            assertEquals(3, byTimer.rows().size());

            final SummaryRow openHome = byTimer.rows().get(0);
            assertEquals("OpenHome.1", openHome.key());
            assertEquals(2L, openHome.count());
            assertEquals(2, openHome.distinctUrls());
            assertNotNull(openHome.sampleUrl());

            // 2. Group by USED_IP filtered to failedOnly=true
            final SummaryTableResponse failedByIp = engine.querySummaryTable(new SummaryTableQuerySpec(
                "R", 0L, 0L, null, null, null, null, null, null, null,
                null, null, true, null, "USED_IP"
            ));
            assertEquals(1L, failedByIp.totalRowsMatched());
            assertEquals(1, failedByIp.rows().size());
            assertEquals("10.0.0.2", failedByIp.rows().get(0).key());
            assertEquals(100.0, failedByIp.rows().get(0).errorPct(), 0.01);

            // 3. Other dimensions and non-Request types
            for (final String dim : List.of("AGENT", "SCENARIO", "STATUS_CODE", "CONTENT_TYPE", "HTTP_METHOD"))
            {
                final SummaryTableResponse resp = engine.querySummaryTable(new SummaryTableQuerySpec(
                    "R", 0L, 0L, null, null, null, null, null, null, null,
                    null, null, false, null, dim
                ));
                assertEquals(4L, resp.totalRowsMatched());
            }

            for (final String rType : List.of("T", "A", "P", "C"))
            {
                final SummaryTableResponse resp = engine.querySummaryTable(new SummaryTableQuerySpec(
                    rType, 0L, 0L, null, null, null, null, null, null, null,
                    null, null, false, null, "AGENT"
                ));
                assertTrue(resp.totalRowsMatched() >= 1L);
            }
        }
    }

    @Test
    public void testInspectSlowestAndFailedTransactions() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-insp");
        final File resultsDir = tempFolder.newFolder("results-insp");
        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            // 1. Slowest requests
            final InspectResponse slowest = engine.inspectOutliers(new InspectQuerySpec(
                "SLOWEST_REQUESTS", 2, 0L, 0L, null, null, null, null, null, null, null,
                null, null, false, null
            ));
            assertEquals(4, slowest.totalMatchingCount());
            assertEquals(2, slowest.records().size());

            final ForensicRecord top = slowest.records().get(0);
            assertEquals("PlaceOrder.1", top.timerName());
            assertEquals("PlaceOrder.1.submit", top.rawName());
            assertEquals(4200, top.runTime());
            assertEquals(503, top.statusCode());
            assertTrue(top.forensicExact());
            assertEquals("https://shop.example.com/api/order", top.url());
            assertEquals("req-3", top.requestId());
            assertEquals("{\"sku\":\"ABC-99\"}", top.formData());

            // 2. Failed requests mode
            final InspectResponse failedReqs = engine.inspectOutliers(new InspectQuerySpec(
                "FAILED_REQUESTS", 10, 0L, 0L, null, "PlaceOrder.*", List.of("agent-1"), List.of("TCheckout"),
                List.of("10.0.0.2"), List.of("POST"), List.of("application/json"),
                500, 599, true, 100
            ));
            assertEquals(1, failedReqs.totalMatchingCount());
            assertEquals("PlaceOrder.1", failedReqs.records().get(0).timerName());

            // 3. Failed transactions with Result Browser dump resolution
            final InspectResponse failedTx = engine.inspectOutliers(new InspectQuerySpec(
                "FAILED_TRANSACTIONS", 10, 0L, 0L, null, null, null, null, null, null, null,
                null, null, true, null
            ));
            assertEquals(1, failedTx.totalMatchingCount());
            assertEquals(1, failedTx.records().size());

            final ForensicRecord txRec = failedTx.records().get(0);
            assertEquals("TCheckout", txRec.timerName());
            assertEquals("PlaceOrder", txRec.failedActionName());
            assertTrue(txRec.stackTrace().contains("HTTP 503 Gateway Timeout"));
            assertEquals("/dumps/agent-1/TCheckout/0/output/1700000005000-PlaceOrder/index.html", txRec.dumpUrl());
        }
    }

    @Test
    public void testTraceUserSessionReconstructsHierarchyAndPrevNextNavigation() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-sess");
        final File resultsDir = tempFolder.newFolder("results-sess");
        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            // Anchor on req3 timestamp (BASE_TIME + 5100) inside Iteration 2
            final SessionResponse session = engine.traceUserSession(new SessionQuerySpec(
                "agent-1", "TCheckout", "0", BASE_TIME + 5100L, 30_000L
            ));

            assertNotNull(session.transaction());
            assertEquals("TCheckout", session.transaction().name());
            assertEquals(BASE_TIME + 5000L, session.windowStart());
            assertEquals(BASE_TIME + 9500L, session.windowEnd());
            assertEquals(Long.valueOf(BASE_TIME), session.prevTransactionTime());
            assertEquals(Long.valueOf(BASE_TIME + 12000L), session.nextTransactionTime());
            assertEquals(
                "/dumps/agent-1/TCheckout/0/output/1700000005000-PlaceOrder/index.html",
                session.transaction().dumpUrl()
            );

            // Verify PlaceOrder action contains the nested Request, Custom timer, and Event
            assertEquals(1, session.items().size());
            final SessionNode placeOrderAction = session.items().get(0);
            assertEquals("A", placeOrderAction.typeCode());
            assertEquals("PlaceOrder", placeOrderAction.name());
            assertEquals(3, placeOrderAction.children().size());
            assertEquals("R", placeOrderAction.children().get(0).typeCode());
            assertEquals("C", placeOrderAction.children().get(1).typeCode());
            assertEquals("E", placeOrderAction.children().get(2).typeCode());
            assertEquals("Backend pool exhausted on 10.0.0.2", placeOrderAction.children().get(2).message());

            // Also test Iteration 1 (with PageLoadTimingData)
            final SessionResponse iter1 = engine.traceUserSession(new SessionQuerySpec(
                "agent-1", "TCheckout", "0", BASE_TIME + 50L, 30_000L
            ));
            assertNotNull(iter1.transaction());
            assertEquals(1, iter1.items().size());
            assertEquals(3, iter1.items().get(0).children().size());

            // Also test fallback window when no enclosing Transaction exists (agent-2, TBrowse, user 1)
            final SessionResponse noTxSession = engine.traceUserSession(new SessionQuerySpec(
                "agent-2", "TBrowse", "1", BASE_TIME + 6000L, 5_000L
            ));
            assertEquals(1, noTxSession.items().size());
            assertEquals("SearchProducts", noTxSession.items().get(0).name());
        }
    }

    @Test
    public void testAutoBucketResolutionIntervals()
    {
        assertEquals(60_000, ColumnarQueryEngine.resolveBucketWidthMs(0L, 7L * 3600_000L, 0));
        assertEquals(10_000, ColumnarQueryEngine.resolveBucketWidthMs(0L, 2L * 3600_000L, 0));
        assertEquals(5_000, ColumnarQueryEngine.resolveBucketWidthMs(0L, 30L * 60_000L, 0));
        assertEquals(1_000, ColumnarQueryEngine.resolveBucketWidthMs(0L, 5L * 60_000L, 0));
        assertEquals(100, ColumnarQueryEngine.resolveBucketWidthMs(0L, 60_000L, 0));
        assertTrue(ColumnarQueryEngine.resolveBucketWidthMs(0L, 1_000_000L, 10) >= 500);
    }

    @Test
    public void testMultiChunkMergingAndRowFilterBranches() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-multi");
        final File resultsDir = tempFolder.newFolder("results-multi");
        // Collect from 2 threads so thread-local builders seal 2 separate RequestChunks
        final ChunkIngestionCollector collector = new ChunkIngestionCollector(cacheDir);

        final Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++)
            {
                final RequestData req = createRequest(
                    "Timer." + i, "Timer." + i + ".raw", BASE_TIME + i * 100L, 50 + i * 20,
                    (i % 3 == 0) ? 500 : 200, (i % 3 == 0),
                    (i % 2 == 0) ? "agent-1" : "agent-2",
                    (i % 2 == 0) ? "TCheckout" : "TBrowse",
                    String.valueOf(i % 2),
                    (i % 2 == 0) ? "GET" : "POST",
                    (i % 2 == 0) ? "text/html" : "application/json",
                    "10.0.0." + (i % 3),
                    "https://shop.example.com/item/" + i,
                    "req-" + i, "resp-" + i, ""
                );
                collector.collect(req);
            }
        });

        final Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++)
            {
                final RequestData reqDup = createRequest(
                    "Timer." + i, "Timer." + i + ".dup", BASE_TIME + 50 + i * 100L, 30 + i * 10,
                    200, false,
                    "agent-1", "TCheckout", "0", "GET", "text/html", "10.0.0.1",
                    "https://shop.example.com/item/dup/" + i,
                    "req-dup-" + i, "resp-dup-" + i, ""
                );
                collector.collect(reqDup);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        try (final ChunkStorage built = collector.finish())
        {
            built.saveToDirectory(cacheDir);
        }

        try (final ChunkStorage storage = ChunkStorage.loadFromDirectory(cacheDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);
            assertNotNull(engine.getStorage());
            assertEquals(resultsDir, engine.getResultsDir());

            // 1. TimeSeries with > 8 groups triggers "Other" series overflow and GroupBucketAccumulator.merge
            final TimeSeriesResponse ts = engine.queryTimeSeries(new TimeSeriesQuerySpec(
                "R", BASE_TIME + 50L, BASE_TIME + 850L, 200,
                List.of("Timer.0", "Timer.1", "Timer.2", "Timer.3", "Timer.4", "Timer.5", "Timer.6", "Timer.7", "Timer.8", "Timer.9"),
                null, List.of("agent-1"), List.of("TCheckout"), List.of("10.0.0.1"),
                List.of("GET"), List.of("text/html"), 200, 299, false, 40, "TIMER"
            ));
            assertTrue(ts.rowsMatched() > 0);
            assertTrue(ts.series().size() <= 9);

            // 2. SummaryTable across multiple chunks with row-level filters
            final SummaryTableResponse sum = engine.querySummaryTable(new SummaryTableQuerySpec(
                "R", BASE_TIME + 50L, BASE_TIME + 850L,
                List.of("Timer.1", "Timer.2", "Timer.3", "Timer.4", "Timer.5"),
                null, List.of("agent-1"), List.of("TCheckout"), List.of("10.0.0.1"),
                List.of("GET"), List.of("text/html"), 200, 299, false, 40, "TIMER"
            ));
            assertTrue(sum.totalRowsMatched() > 0);

            // 3. InspectOutliers across multiple chunks with selective filters
            final InspectResponse insp = engine.inspectOutliers(new InspectQuerySpec(
                "SLOWEST_REQUESTS", 3, BASE_TIME + 50L, BASE_TIME + 850L,
                List.of("Timer.1", "Timer.2", "Timer.3", "Timer.4", "Timer.5"),
                null, List.of("agent-1"), List.of("TCheckout"), List.of("10.0.0.1"),
                List.of("GET"), List.of("text/html"), 200, 299, false, 40
            ));
            assertTrue(insp.totalMatchingCount() > 0);
            assertTrue(insp.records().size() <= 3);

            // 4. Test invalid regex fallback to plain substring matching, plus high minRunTime zone-map pruning
            final SummaryTableResponse invalidRegexResp = engine.querySummaryTable(new SummaryTableQuerySpec(
                "R", 0L, 0L, null, "[Timer.1", null, null, null, null, null,
                null, null, false, 999_999, "NONE"
            ));
            assertEquals(0L, invalidRegexResp.totalRowsMatched());

            // 5. Test failedOnly + minStatusCode >= 500 zone-map pruning
            final InspectResponse failed500 = engine.inspectOutliers(new InspectQuerySpec(
                "FAILED_REQUESTS", 5, 0L, 0L, null, null, null, null, null, null, null,
                500, 599, true, null
            ));
            assertTrue(failed500.totalMatchingCount() >= 1);

            // 6. Test single-arg constructor, unknown user session, and closest-transaction session resolution
            final ColumnarQueryEngine noResultsDirEngine = new ColumnarQueryEngine(storage);
            assertNull(noResultsDirEngine.getResultsDir());
            final SessionResponse missingUser = noResultsDirEngine.traceUserSession(new SessionQuerySpec(
                "unknown-agent", "unknown-scenario", "999", BASE_TIME, 10_000L
            ));
            assertTrue(missingUser.items().isEmpty());
        }
    }

    @Test
    public void testTimeSeriesNetworkTimingsAndByteMetricsNotZero() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-ts-net");
        final File resultsDir = tempFolder.newFolder("results-ts-net");
        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            final TimeSeriesQuerySpec spec = new TimeSeriesQuerySpec(
                "R", BASE_TIME, BASE_TIME + 10_000L, 5000,
                null, null, null, null, null, null, null,
                null, null, false, null, "NONE"
            );
            final TimeSeriesResponse res = engine.queryTimeSeries(spec);

            assertEquals("R", res.recordType());
            assertTrue(res.rowsMatched() > 0);

            // Bucket 0 has requests with 250 bytesSent, 4096 bytesReceived, 5ms DNS, 10ms connect, etc.
            assertTrue("bytesSent must be > 0 for requests", res.bytesSent()[0] > 0);
            assertTrue("bytesReceived must be > 0 for requests", res.bytesReceived()[0] > 0);
            assertTrue("avgDns must be > 0 for requests", res.avgDns()[0] > 0.0);
            assertTrue("avgConnect must be > 0 for requests", res.avgConnect()[0] > 0.0);
            assertTrue("avgSend must be > 0 for requests", res.avgSend()[0] > 0.0);
            assertTrue("avgServerBusy must be > 0 for requests", res.avgServerBusy()[0] > 0.0);
            assertTrue("avgReceive must be > 0 for requests", res.avgReceive()[0] > 0.0);
            assertTrue("avgTtfb must be > 0 for requests", res.avgTtfb()[0] > 0.0);
        }
    }

    @Test
    public void testPruneCandidateChunksWithHttpErrors4xx() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-4xx");
        final File resultsDir = tempFolder.newFolder("results-4xx");

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(cacheDir);
        // Chunk 1: HTTP 200 OK only
        final RequestData req200 = createRequest(
            "Req200", "Req200.raw", BASE_TIME, 100, 200, false,
            "agent-1", "TCheckout", "0", "GET", "text/html", "10.0.0.1",
            "https://shop.example.com/ok", "req-ok", "resp-ok", ""
        );
        collector.collect(req200);

        // Chunk 2: HTTP 404 Not Found (failed flag is false, but responseCode is 404)
        final RequestData req404 = createRequest(
            "Req404", "Req404.raw", BASE_TIME + 1000, 150, 404, false,
            "agent-1", "TCheckout", "0", "GET", "text/html", "10.0.0.1",
            "https://shop.example.com/notfound", "req-nf", "resp-nf", ""
        );
        collector.collect(req404);

        try (final ChunkStorage built = collector.finish())
        {
            built.saveToDirectory(cacheDir);
        }

        try (final ChunkStorage storage = ChunkStorage.loadFromDirectory(cacheDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            // Query failedOnly = true without minStatusCode
            final InspectResponse resp = engine.inspectOutliers(new InspectQuerySpec(
                "FAILED_REQUESTS", 10, BASE_TIME, BASE_TIME + 5000,
                null, null, null, null, null, null, null,
                null, null, true, null
            ));

            assertEquals("Must find exactly 1 failed request (the 404)", 1L, resp.totalMatchingCount());
            assertEquals(1, resp.records().size());
            assertEquals("Req404", resp.records().get(0).timerName());
            assertEquals(404, resp.records().get(0).statusCode());
        }
    }

    @Test
    public void testInspectSlowestRequestsTotalMatchingCount() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-slowest-count");
        final File resultsDir = tempFolder.newFolder("results-slowest-count");

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(cacheDir);

        // Create 3 chunks by filling small batches
        for (int i = 0; i < 30; i++)
        {
            final RequestData req = createRequest(
                "Timer." + i, "Timer.raw", BASE_TIME + (i * 100L), 100 + i * 10, 200, false,
                "agent-1", "TCheckout", "0", "GET", "text/html", "10.0.0.1",
                "https://shop.example.com/" + i, "req-" + i, "resp-" + i, ""
            );
            collector.collect(req);
        }

        try (final ChunkStorage built = collector.finish())
        {
            built.saveToDirectory(cacheDir);
        }

        try (final ChunkStorage storage = ChunkStorage.loadFromDirectory(cacheDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            // Request top 5 slowest
            final InspectResponse resp = engine.inspectOutliers(new InspectQuerySpec(
                "SLOWEST_REQUESTS", 5, BASE_TIME, BASE_TIME + 100_000L,
                null, null, null, null, null, null, null,
                null, null, false, null
            ));

            assertEquals(5, resp.records().size());
            assertEquals("totalMatchingCount must equal all 30 matching requests despite top-K pruning", 30L, resp.totalMatchingCount());
        }
    }

    @Test
    public void testInspectSlowestTransactionsAndActions() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-slow-tx");
        final File resultsDir = tempFolder.newFolder("results-slow-tx");

        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);

            // Test SLOWEST_TRANSACTIONS
            final InspectResponse txResp = engine.inspectOutliers(new InspectQuerySpec(
                "SLOWEST_TRANSACTIONS", 2, BASE_TIME, BASE_TIME + 20_000L,
                null, null, null, null, null, null, null,
                null, null, false, null
            ));

            assertEquals("SLOWEST_TRANSACTIONS", txResp.mode());
            assertEquals(2, txResp.records().size());
            assertEquals("T", txResp.records().get(0).recordType());
            assertTrue("Slowest transaction must be sorted descending",
                       txResp.records().get(0).runTime() >= txResp.records().get(1).runTime());

            // Test SLOWEST_ACTIONS
            final InspectResponse actResp = engine.inspectOutliers(new InspectQuerySpec(
                "SLOWEST_ACTIONS", 2, BASE_TIME, BASE_TIME + 20_000L,
                null, null, null, null, null, null, null,
                null, null, false, null
            ));

            assertEquals("SLOWEST_ACTIONS", actResp.mode());
            assertEquals(2, actResp.records().size());
            assertEquals("A", actResp.records().get(0).recordType());
            assertTrue("Slowest action must be sorted descending",
                       actResp.records().get(0).runTime() >= actResp.records().get(1).runTime());
        }
    }

    @Test
    public void testCombinedDashboardSinglePassQuery() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-dash");
        final File resultsDir = tempFolder.newFolder("results-dash");

        try (final ChunkStorage storage = buildSampleStorage(cacheDir, resultsDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);
            assertFalse(engine.hasCachedMetadata());
            engine.getMetadata();
            assertTrue(engine.hasCachedMetadata());

            final QueryModels.DashboardResponse dash = engine.queryDashboard(new QueryModels.DashboardQuerySpec(
                "R", BASE_TIME, BASE_TIME + 10_000L, 5000,
                null, null, null, null, null, null, null,
                null, null, false, null, "USED_IP", "TIMER"
            ));
            assertNotNull(dash);
            assertNotNull(dash.timeSeries());
            assertNotNull(dash.summaryTable());
            assertEquals(4L, dash.timeSeries().rowsMatched());
            assertEquals(4L, dash.summaryTable().totalRowsMatched());
            assertEquals(2, dash.timeSeries().series().size());
            assertEquals(3, dash.summaryTable().rows().size());

            // Also test non-Request recordType ("T" and "A") through queryDashboard
            for (final String type : List.of("T", "A"))
            {
                final QueryModels.DashboardResponse timerDash = engine.queryDashboard(new QueryModels.DashboardQuerySpec(
                    type, 0L, 0L, 0,
                    null, null, null, null, null, null, null,
                    null, null, false, null, "TIMER", "AGENT"
                ));
                assertTrue(timerDash.timeSeries().rowsMatched() >= 2L);
                assertEquals(timerDash.timeSeries().rowsMatched(), timerDash.summaryTable().totalRowsMatched());
            }
        }
    }

    @Test
    public void testHighCardinalityDistinctUrlsHyperLogLogPromotion() throws Exception
    {
        final File cacheDir = tempFolder.newFolder("cache-hll");
        final File resultsDir = tempFolder.newFolder("results-hll");
        final ChunkIngestionCollector collector = new ChunkIngestionCollector(cacheDir);

        final int distinctTarget = 1200;
        for (int i = 0; i < distinctTarget; i++)
        {
            final RequestData req = createRequest(
                "ProductDetail", "ProductDetail.raw", BASE_TIME + i * 10L, 40 + (i % 50), 200, false,
                "agent-1", "TBrowse", "0", "GET", "text/html", "10.0.0.1",
                "https://shop.example.com/p/sku-" + i + "?session=" + (i * 31),
                "req-" + i, "resp-" + i, ""
            );
            collector.collect(req);
        }

        try (final ChunkStorage built = collector.finish())
        {
            built.saveToDirectory(cacheDir);
        }

        try (final ChunkStorage storage = ChunkStorage.loadFromDirectory(cacheDir))
        {
            final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);
            final SummaryTableResponse sum = engine.querySummaryTable(new SummaryTableQuerySpec(
                "R", 0L, 0L, null, null, null, null, null, null, null,
                null, null, false, null, "TIMER"
            ));
            assertEquals(1, sum.rows().size());
            final int estimatedDistinct = sum.rows().get(0).distinctUrls();
            // 12-bit HLL standard error is ~1.6%; allow generous 8% bound for deterministic test stability
            assertTrue("HLL estimate (" + estimatedDistinct + ") should be close to " + distinctTarget,
                       estimatedDistinct >= 1100 && estimatedDistinct <= 1300);
        }
    }
}
