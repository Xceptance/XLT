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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.report.query.ColumnarQueryEngine;
import com.xceptance.xlt.report.storage.ChunkIngestionCollector;
import com.xceptance.xlt.report.storage.ChunkStorage;

/**
 * Integration tests for {@link ChunkExplorerServer}, {@link ChunkApiServlet}, and {@link ChunkExplorerMain}.
 */
public class ChunkExplorerServerTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private ChunkStorage storage;
    private File resultsDir;
    private File cacheDir;
    private ChunkExplorerServer server;
    private HttpClient httpClient;

    @Before
    public void setUp() throws Exception
    {
        resultsDir = tempFolder.newFolder("results");
        cacheDir = new File(resultsDir, "xlt-cache");

        // Create a dummy dump file inside resultsDir/ac001/TCheckout/0/output/1000/index.html
        final File dumpDir = new File(resultsDir, "ac001/TCheckout/0/output/1000");
        assertTrue(dumpDir.mkdirs());
        Files.writeString(new File(dumpDir, "index.html").toPath(), "<html><body>Result Browser Dump</body></html>", StandardCharsets.UTF_8);

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(cacheDir);

        final TransactionData tx = new TransactionData("TCheckout");
        tx.setTime(1_000_000L);
        tx.setRunTime(500);
        tx.setFailed(true);
        tx.setFailureStackTrace("java.lang.AssertionError: Checkout failed");
        tx.setFailedActionName("PlaceOrder");
        tx.setTestUserNumber("0");
        tx.setDirectoryName("1000");
        tx.setAgentName("ac001");
        tx.setTransactionName("TCheckout");
        collector.collect(tx);

        final ActionData action = new ActionData("PlaceOrder");
        action.setTime(1_000_050L);
        action.setRunTime(400);
        action.setFailed(true);
        action.setAgentName("ac001");
        action.setTransactionName("TCheckout");
        action.setTestUserNumber("0");
        collector.collect(action);

        final RequestData req = new RequestData("PlaceOrder.1");
        req.setTime(1_000_100L);
        req.setRunTime(350);
        req.setFailed(true);
        req.setResponseCode(503);
        req.setBytesSent(250);
        req.setBytesReceived(1200);
        req.setConnectTime(20);
        req.setSendTime(5);
        req.setServerBusyTime(300);
        req.setReceiveTime(25);
        req.setTimeToFirstBytes(325);
        req.setTimeToLastBytes(350);
        req.setUrl("https://shop.example.com/checkout");
        req.setContentType("application/json");
        req.setHttpMethod("POST");
        req.setFormDataEncoding("application/json");
        req.setFormData("{\"cartId\":99}");
        req.setUsedIpAddress("10.0.0.1");
        req.setRequestId("req-checkout-1");
        req.setResponseId("101");
        req.setAgentName("ac001");
        req.setTransactionName("TCheckout");
        req.setTestUserNumber("0");
        collector.collect(req);

        final EventData ev = new EventData("RetryWarning");
        ev.setTime(1_000_200L);
        ev.setTestCaseName("TCheckout");
        ev.setMessage("Retrying checkout backend");
        ev.setAgentName("ac001");
        ev.setTransactionName("TCheckout");
        ev.setTestUserNumber("0");
        collector.collect(ev);

        try (final ChunkStorage built = collector.finish())
        {
            built.saveToDirectory(cacheDir);
        }
        storage = ChunkStorage.loadFromDirectory(cacheDir);

        final ColumnarQueryEngine engine = new ColumnarQueryEngine(storage, resultsDir);
        server = new ChunkExplorerServer(engine, 0);
        server.start();

        httpClient = HttpClient.newHttpClient();
    }

    @After
    public void tearDown() throws Exception
    {
        if (server != null)
        {
            server.close();
        }
        if (storage != null)
        {
            storage.close();
        }
    }

    @Test
    public void testMetadataEndpoint() throws Exception
    {
        final HttpRequest req = HttpRequest.newBuilder()
                                           .uri(URI.create(server.getBaseUrl() + "/api/v1/metadata"))
                                           .GET()
                                           .build();
        final HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"TCheckout\""));
        assertTrue(resp.body().contains("\"ac001\""));
        assertTrue(resp.body().contains("\"PlaceOrder.1\""));
    }

    @Test
    public void testTimeSeriesEndpoint() throws Exception
    {
        final String body = """
            {
              "recordType": "R",
              "fromTime": 999000,
              "toTime": 1002000,
              "bucketWidthMs": 1000,
              "groupBy": "NONE"
            }
            """;
        final HttpRequest req = HttpRequest.newBuilder()
                                           .uri(URI.create(server.getBaseUrl() + "/api/v1/timeseries"))
                                           .header("Content-Type", "application/json")
                                           .POST(HttpRequest.BodyPublishers.ofString(body))
                                           .build();
        final HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"rowsMatched\":1"));
    }

    @Test
    public void testSummaryTableEndpoint() throws Exception
    {
        final String body = """
            {
              "recordType": "R",
              "fromTime": 999000,
              "toTime": 1002000,
              "groupBy": "TIMER"
            }
            """;
        final HttpRequest req = HttpRequest.newBuilder()
                                           .uri(URI.create(server.getBaseUrl() + "/api/v1/summary-table"))
                                           .header("Content-Type", "application/json")
                                           .POST(HttpRequest.BodyPublishers.ofString(body))
                                           .build();
        final HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"PlaceOrder.1\""));
    }

    @Test
    public void testInspectEndpoint() throws Exception
    {
        final String body = """
            {
              "mode": "FAILED_REQUESTS",
              "fromTime": 999000,
              "toTime": 1002000,
              "limit": 25
            }
            """;
        final HttpRequest req = HttpRequest.newBuilder()
                                           .uri(URI.create(server.getBaseUrl() + "/api/v1/inspect"))
                                           .header("Content-Type", "application/json")
                                           .POST(HttpRequest.BodyPublishers.ofString(body))
                                           .build();
        final HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("req-checkout-1"));
        assertTrue(resp.body().contains("https://shop.example.com/checkout"));
    }

    @Test
    public void testSessionEndpoint() throws Exception
    {
        final String body = """
            {
              "agent": "ac001",
              "scenario": "TCheckout",
              "userNumber": "0",
              "targetTime": 1000100,
              "paddingMs": 30000
            }
            """;
        final HttpRequest req = HttpRequest.newBuilder()
                                           .uri(URI.create(server.getBaseUrl() + "/api/v1/session"))
                                           .header("Content-Type", "application/json")
                                           .POST(HttpRequest.BodyPublishers.ofString(body))
                                           .build();
        final HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"PlaceOrder\""));
        assertTrue(resp.body().contains("\"PlaceOrder.1\""));
        assertTrue(resp.body().contains("\"RetryWarning\""));
        final HttpRequest getSessReq = HttpRequest.newBuilder()
                                                  .uri(URI.create(server.getBaseUrl() + "/api/v1/session?agent=ac001&scenario=TCheckout&userNumber=0&targetTime=1000100"))
                                                  .GET()
                                                  .build();
        final HttpResponse<String> getSessResp = httpClient.send(getSessReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getSessResp.statusCode());
        assertTrue(getSessResp.body().contains("\"PlaceOrder\""));

        final HttpRequest optReq = HttpRequest.newBuilder()
                                              .uri(URI.create(server.getBaseUrl() + "/api/v1/metadata"))
                                              .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                                              .build();
        final HttpResponse<String> optResp = httpClient.send(optReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, optResp.statusCode());

        final HttpRequest unkGet = HttpRequest.newBuilder()
                                              .uri(URI.create(server.getBaseUrl() + "/api/v1/unknown"))
                                              .GET()
                                              .build();
        assertEquals(404, httpClient.send(unkGet, HttpResponse.BodyHandlers.ofString()).statusCode());

        final HttpRequest unkPost = HttpRequest.newBuilder()
                                               .uri(URI.create(server.getBaseUrl() + "/api/v1/unknown"))
                                               .POST(HttpRequest.BodyPublishers.ofString("{}"))
                                               .build();
        assertEquals(404, httpClient.send(unkPost, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    public void testStaticUiAndDumpsEndpoints() throws Exception
    {
        final HttpRequest uiReq = HttpRequest.newBuilder()
                                             .uri(URI.create(server.getBaseUrl() + "/"))
                                             .GET()
                                             .build();
        final HttpResponse<String> uiResp = httpClient.send(uiReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, uiResp.statusCode());
        assertTrue(uiResp.body().contains("ChunkDB Explorer"));
        assertTrue(uiResp.body().contains("SLOWEST_TRANSACTIONS"));
        assertTrue(uiResp.body().contains("SLOWEST_ACTIONS"));
        assertTrue(uiResp.body().contains("FAILED_ACTIONS"));
        assertTrue(uiResp.body().contains("exportSummaryCsvBtn"));
        assertTrue(uiResp.body().contains("exportForensicCsvBtn"));

        final HttpRequest jsReq = HttpRequest.newBuilder()
                                             .uri(URI.create(server.getBaseUrl() + "/explorer/js/explorer.js"))
                                             .GET()
                                             .build();
        final HttpResponse<String> jsResp = httpClient.send(jsReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, jsResp.statusCode());
        assertTrue(jsResp.body().contains("exportSummaryCsv"));
        assertTrue(jsResp.body().contains("exportForensicCsv"));
        assertTrue(jsResp.body().contains("scheduleChartRender"));
        assertTrue(jsResp.body().contains("wf-target-row"));

        final HttpRequest cssReq = HttpRequest.newBuilder()
                                              .uri(URI.create(server.getBaseUrl() + "/explorer/css/explorer.css"))
                                              .GET()
                                              .build();
        final HttpResponse<String> cssResp = httpClient.send(cssReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, cssResp.statusCode());
        assertTrue(cssResp.body().contains(".wf-target-row"));
        assertTrue(cssResp.body().contains("@media"));

        final HttpRequest missingAsset = HttpRequest.newBuilder()
                                                    .uri(URI.create(server.getBaseUrl() + "/explorer/missing.js"))
                                                    .GET()
                                                    .build();
        assertEquals(404, httpClient.send(missingAsset, HttpResponse.BodyHandlers.ofString()).statusCode());

        final HttpRequest validReq = HttpRequest.newBuilder()
                                                .uri(URI.create(server.getBaseUrl() + "/dumps/ac001/TCheckout/0/output/1000/index.html"))
                                                .GET()
                                                .build();
        final HttpResponse<String> validResp = httpClient.send(validReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, validResp.statusCode());
        assertTrue(validResp.body().contains("Result Browser Dump"));

        final HttpRequest missingReq = HttpRequest.newBuilder()
                                                  .uri(URI.create(server.getBaseUrl() + "/dumps/ac001/TCheckout/0/output/9999/index.html"))
                                                  .GET()
                                                  .build();
        final HttpResponse<String> missingResp = httpClient.send(missingReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, missingResp.statusCode());

        final HttpRequest forbiddenDumpReq = HttpRequest.newBuilder()
                                                        .uri(URI.create(server.getBaseUrl() + "/dumps/ac001/TCheckout/0/not-output/index.html"))
                                                        .GET()
                                                        .build();
        assertEquals(403, httpClient.send(forbiddenDumpReq, HttpResponse.BodyHandlers.ofString()).statusCode());

        // Path traversal with .. in DumpResourceServlet
        final HttpRequest traversalDumpReq = HttpRequest.newBuilder()
                                                        .uri(URI.create(server.getBaseUrl() + "/dumps/ac001/TCheckout/0/output/../../../../etc/passwd"))
                                                        .GET()
                                                        .build();
        assertTrue("Traversal dump must be rejected with 400 or 403",
                   httpClient.send(traversalDumpReq, HttpResponse.BodyHandlers.ofString()).statusCode() >= 400);

        // Path traversal with URL-encoded %2e%2e in DumpResourceServlet
        final HttpRequest encDumpReq = HttpRequest.newBuilder()
                                                  .uri(URI.create(server.getBaseUrl() + "/dumps/ac001/TCheckout/0/output/%2e%2e/%2e%2e/index.html"))
                                                  .GET()
                                                  .build();
        assertTrue("Encoded traversal dump must be rejected with 400 or 403",
                   httpClient.send(encDumpReq, HttpResponse.BodyHandlers.ofString()).statusCode() >= 400);

        // Path traversal in ExplorerStaticServlet
        final HttpRequest traversalStaticReq = HttpRequest.newBuilder()
                                                          .uri(URI.create(server.getBaseUrl() + "/explorer/../../pom.xml"))
                                                          .GET()
                                                          .build();
        assertTrue("Traversal static asset must be rejected with 400 or 403",
                   httpClient.send(traversalStaticReq, HttpResponse.BodyHandlers.ofString()).statusCode() >= 400);

        final HttpRequest encStaticReq = HttpRequest.newBuilder()
                                                    .uri(URI.create(server.getBaseUrl() + "/explorer/%2e%2e/%2e%2e/pom.xml"))
                                                    .GET()
                                                    .build();
        assertTrue("Encoded traversal static asset must be rejected with 400 or 403",
                   httpClient.send(encStaticReq, HttpResponse.BodyHandlers.ofString()).statusCode() >= 400);
    }

    @Test
    public void testDirectCacheMountAndColdIngestionInMain() throws Exception
    {
        assertNotNull(ChunkExplorerMain.createCommandLineOptions());
        final ChunkExplorerMain.LoadedDataset loaded =
            ChunkExplorerMain.loadOrIngestDataset(cacheDir, null, null, true);
        assertNotNull(loaded);
        assertNotNull(loaded.storage());
        assertEquals(resultsDir.getCanonicalFile(), loaded.resultsDir().getCanonicalFile());
        assertEquals("full", loaded.detailMode());
        loaded.storage().close();

        // Also test Case 2: Cold cache ingestion from a results directory with a timers.csv file
        final File coldResultsDir = tempFolder.newFolder("cold-results");
        final File userDir = new File(coldResultsDir, "ac001/TCheckout/0");
        assertTrue(userDir.mkdirs());
        final String csv = "T,TCheckout,1000000,500,false\n"
                         + "A,PlaceOrder,1000050,400,false\n"
                         + "R,PlaceOrder.1,1000100,350,false,250,1200,200,https://shop.example.com/checkout,application/json,20,5,300,25,325,350,req-1,POST,,,5,10.0.0.1,101,10.0.0.1\n";
        Files.writeString(new File(userDir, "timers.csv").toPath(), csv, StandardCharsets.UTF_8);

        final ChunkExplorerMain.LoadedDataset ingested =
            ChunkExplorerMain.loadOrIngestDataset(coldResultsDir, null, null, false);
        assertNotNull(ingested);
        assertNotNull(ingested.storage());
        assertEquals(3L, ingested.storage().getTotalRowCount());
        ingested.storage().close();

        // Warm cache mount on the same results directory
        final ChunkExplorerMain.LoadedDataset warm =
            ChunkExplorerMain.loadOrIngestDataset(coldResultsDir, null, null, false);
        assertNotNull(warm);
        assertEquals(3L, warm.storage().getTotalRowCount());
        warm.storage().close();

        // Test executeCli: --help, missing args, nonexistent dir, and valid ephemeral start/stop
        assertEquals(0, ChunkExplorerMain.executeCli(new String[]{"--help"}, false));
        assertEquals(1, ChunkExplorerMain.executeCli(new String[]{}, false));
        assertEquals(2, ChunkExplorerMain.executeCli(new String[]{"/nonexistent/path/for/chunkdb"}, false));
        assertEquals(0, ChunkExplorerMain.executeCli(
            new String[]{"--port", "0", "--preserve-all-details", "-Dcom.xceptance.xlt.reportgenerator.threads=2", cacheDir.getAbsolutePath()},
            false
        ));
    }

    @Test
    public void testApiEdgeCasesAndContentTypes() throws Exception
    {
        assertEquals("text/html; charset=UTF-8", ChunkExplorerServer.guessContentType("index.html"));
        assertEquals("text/css; charset=UTF-8", ChunkExplorerServer.guessContentType("style.css"));
        assertEquals("application/javascript; charset=UTF-8", ChunkExplorerServer.guessContentType("app.js"));
        assertEquals("image/png", ChunkExplorerServer.guessContentType("logo.png"));
        assertEquals("application/octet-stream", ChunkExplorerServer.guessContentType("archive.bin"));
        assertEquals("application/octet-stream", ChunkExplorerServer.guessContentType("noext"));

        // Empty body POST on /api/v1/timeseries should deserialize default {} spec and return 200
        final HttpRequest emptyPost = HttpRequest.newBuilder()
                                                 .uri(URI.create(server.getBaseUrl() + "/api/v1/timeseries"))
                                                 .POST(HttpRequest.BodyPublishers.noBody())
                                                 .build();
        assertEquals(200, httpClient.send(emptyPost, HttpResponse.BodyHandlers.ofString()).statusCode());

        // Invalid JSON body POST should return 400
        final HttpRequest badJsonPost = HttpRequest.newBuilder()
                                                   .uri(URI.create(server.getBaseUrl() + "/api/v1/timeseries"))
                                                   .POST(HttpRequest.BodyPublishers.ofString("{invalid-json"))
                                                   .build();
        assertEquals(400, httpClient.send(badJsonPost, HttpResponse.BodyHandlers.ofString()).statusCode());

        // GET /api/v1/session with non-numeric targetTime should fall back cleanly
        final HttpRequest badLongGet = HttpRequest.newBuilder()
                                                  .uri(URI.create(server.getBaseUrl() + "/api/v1/session?agent=ac001&testCase=TCheckout&user=0&time=notanumber"))
                                                  .GET()
                                                  .build();
        assertEquals(200, httpClient.send(badLongGet, HttpResponse.BodyHandlers.ofString()).statusCode());

        // Server with null resultsDir returns 404 on /dumps/*
        final ColumnarQueryEngine noDumpsEngine = new ColumnarQueryEngine(storage, null);
        try (final ChunkExplorerServer noDumpsServer = new ChunkExplorerServer(noDumpsEngine, 0))
        {
            noDumpsServer.start();
            assertEquals(noDumpsEngine, noDumpsServer.getQueryEngine());
            final HttpRequest dumpReq = HttpRequest.newBuilder()
                                                   .uri(URI.create(noDumpsServer.getBaseUrl() + "/dumps/ac001/TCheckout/0/output/1000/index.html"))
                                                   .GET()
                                                   .build();
            assertEquals(404, httpClient.send(dumpReq, HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }
}
