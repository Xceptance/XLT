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
package com.xceptance.xlt.report.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import com.xceptance.common.util.StringMatcher;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.storage.cache.CacheFingerprint;
import com.xceptance.xlt.report.storage.cache.CacheManager;
import com.xceptance.xlt.report.storage.catalog.ChunkCatalog;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.ChunkBuilder;
import com.xceptance.xlt.report.storage.chunk.ChunkSpooler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeHandler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeRegistry;
import com.xceptance.xlt.report.storage.chunk.DiskChunk;
import com.xceptance.xlt.report.storage.chunk.RequestChunk;
import com.xceptance.xlt.report.storage.chunk.RequestChunkHandler;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

public class ChunkStorageAndCacheTest
{
    private File tempDir;

    @Before
    public void setUp() throws Exception
    {
        RequestChunkHandler.setRequestDetailsMode("sparse");
        tempDir = Files.createTempDirectory("chunkdb-test").toFile();
    }

    @After
    public void tearDown() throws Exception
    {
        RequestChunkHandler.setRequestDetailsMode("sparse");
        if (tempDir != null && tempDir.exists())
        {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void testIngestionSerializationAndScanRoundTrip() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();

        final long baseTime = 1_700_000_000_000L;

        // Ingest requests
        for (int i = 0; i < 1000; i++)
        {
            final RequestData r = new RequestData("Action-" + (i % 5));
            r.setTime(baseTime + (i * 10L));
            r.setRunTime(50 + (i % 100));
            r.setResponseCode(i % 50 == 0 ? 500 : 200);
            r.setFailed(i % 50 == 0);
            r.setBytesSent(128);
            r.setBytesReceived(2048 + i);
            r.setHttpMethod("GET");
            r.setContentType("text/html");
            r.setUrl("https://example.com/test?id=" + (i % 10));
            r.setAgentName("Agent-" + (i % 2));
            r.setTransactionName("TOrder");

            collector.collect(r);
        }

        // Ingest actions
        for (int i = 0; i < 200; i++)
        {
            final ActionData a = new ActionData("Action-" + (i % 5));
            a.setTime(baseTime + (i * 50L));
            a.setRunTime(120 + i);
            a.setFailed(i % 20 == 0);
            a.setAgentName("Agent-" + (i % 2));
            a.setTransactionName("TOrder");
            collector.collect(a);
        }

        // Ingest transactions
        for (int i = 0; i < 50; i++)
        {
            final TransactionData t = new TransactionData("TOrder");
            t.setTime(baseTime + (i * 200L));
            t.setRunTime(1500 + i);
            t.setFailed(i % 10 == 0);
            if (t.hasFailed())
            {
                t.setFailedActionName("Action-1");
                t.setFailureStackTrace("java.lang.AssertionError: Assertion failed");
                t.setDirectoryName("dump-dir-" + i);
            }
            t.setAgentName("Agent-" + (i % 2));
            t.setTransactionName("TOrder");
            collector.collect(t);
        }

        // Ingest events
        for (int i = 0; i < 20; i++)
        {
            final EventData e = new EventData("CustomEvent");
            e.setTime(baseTime + (i * 500L));
            e.setMessage("Event occurred at " + i);
            e.setAgentName("Agent-0");
            e.setTransactionName("TOrder");
            collector.collect(e);
        }

        // Finish collector
        final ChunkStorage storage = collector.finish();
        Assert.assertNotNull(storage);
        Assert.assertEquals(1270, storage.getCatalog().getTotalRowCount());
        Assert.assertEquals(baseTime, storage.getMinTime());

        // Save to tempDir
        storage.saveToDirectory(tempDir);

        // Verify storage files exist
        Assert.assertTrue(new File(tempDir, ChunkStorage.DICTIONARIES_FILE_NAME).exists());
        Assert.assertTrue(new File(tempDir, ChunkStorage.CHUNKS_FILE_NAME).exists());

        // Load back from disk
        final ChunkStorage loaded = ChunkStorage.loadFromDirectory(tempDir);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(1270, loaded.getCatalog().getTotalRowCount());
        Assert.assertEquals(storage.getMinTime(), loaded.getMinTime());
        Assert.assertEquals(storage.getMaxTime(), loaded.getMaxTime());

        // Query: Scan all requests
        final List<Data> scannedRequests = new ArrayList<>();
        final ScanPredicate allPred = new ScanPredicate(0, Long.MAX_VALUE, null, null);
        loaded.scan('R', allPred, scannedRequests::add);
        Assert.assertEquals(1000, scannedRequests.size());

        final RequestData first = (RequestData) scannedRequests.get(0);
        Assert.assertEquals("Action-0", first.getName());
        Assert.assertEquals(baseTime, first.getTime());
        Assert.assertEquals(50, first.getRunTime());
        Assert.assertEquals(500, first.getResponseCode());
        Assert.assertTrue(first.hasFailed());
        Assert.assertEquals("GET", first.getHttpMethod().toString());
        Assert.assertEquals("text/html", first.getContentType().toString());

        final RequestData second = (RequestData) scannedRequests.get(1);
        Assert.assertEquals("Action-1", second.getName());
        Assert.assertEquals(baseTime + 10L, second.getTime());
        Assert.assertEquals(51, second.getRunTime());
        Assert.assertEquals(200, second.getResponseCode());
        Assert.assertFalse(second.hasFailed());

        // Verify sampled URL preservation
        Assert.assertNotNull(first.getUrl());
        Assert.assertTrue(first.getUrl().toString().startsWith("https://example.com/test?id="));

        // Query: Time slice filtering (middle 2 seconds)
        final List<Data> slicedRequests = new ArrayList<>();
        final long sliceStart = baseTime + 2000L;
        final long sliceEnd = baseTime + 4000L;
        final ScanPredicate slicePred = new ScanPredicate(sliceStart, sliceEnd, null, null);
        loaded.scan('R', slicePred, slicedRequests::add);
        // Expect records with time in [baseTime + 2000, baseTime + 4000]
        Assert.assertFalse(slicedRequests.isEmpty());
        for (final Data d : slicedRequests)
        {
            Assert.assertTrue(d.getTime() >= sliceStart && d.getTime() <= sliceEnd);
        }

        // Query: Agent filtering via RoaringBitmap
        final StringMatcher agentMatcher = new StringMatcher("Agent-1", null, true);
        final RoaringBitmap matchingAgents = loaded.getDictionaries().filterAgentTestCaseIds(agentMatcher, null);
        Assert.assertNotNull(matchingAgents);

        final List<Data> agent1Requests = new ArrayList<>();
        final ScanPredicate agentPred = new ScanPredicate(0, Long.MAX_VALUE, null, matchingAgents);
        loaded.scan('R', agentPred, agent1Requests::add);
        Assert.assertEquals(500, agent1Requests.size());
        for (final Data d : agent1Requests)
        {
            Assert.assertEquals("Agent-1", d.getAgentName());
        }

        // Query: Verify transactions with errors preserved
        final List<Data> scannedTransactions = new ArrayList<>();
        loaded.scan('T', allPred, scannedTransactions::add);
        Assert.assertEquals(50, scannedTransactions.size());

        int failedTxCount = 0;
        for (final Data d : scannedTransactions)
        {
            final TransactionData td = (TransactionData) d;
            if (td.hasFailed())
            {
                failedTxCount++;
                Assert.assertEquals("Action-1", td.getFailedActionName());
                Assert.assertEquals("java.lang.AssertionError: Assertion failed", td.getFailureStackTrace());
                Assert.assertTrue(td.getDirectoryName().startsWith("dump-dir-"));
            }
        }
        Assert.assertEquals(5, failedTxCount);
    }

    @Test
    public void testCacheFingerprintAndPurge() throws Exception
    {
        final File resultsDir = new File(tempDir, "results");
        resultsDir.mkdirs();

        final File log1 = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log1))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        Assert.assertFalse("Cache should not exist yet", cacheManager.isCacheValid());

        // Create dummy ChunkStorage and save
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        r.setAgentName("Agent-1");
        r.setTransactionName("TOrder");
        collector.collect(r);

        final ChunkStorage storage = collector.finish();
        cacheManager.save(storage);

        Assert.assertTrue("Cache should now be valid", cacheManager.isCacheValid());
        final long[] range = cacheManager.peekTimeRange();
        Assert.assertNotNull(range);
        Assert.assertEquals(1000L, range[0]);

        // Modify input log file size/content
        try (final FileOutputStream fos = new FileOutputStream(log1, true))
        {
            fos.write("R,2,150,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        // Cache must now detect invalidity and automatically purge
        Assert.assertFalse("Cache should be invalid after log file modification", cacheManager.isCacheValid());
        Assert.assertNull("Cache directory should have been purged", cacheManager.peekTimeRange());
    }

    /**
     * Verifies that GlobalDictionaries supports more than 65,535 distinct strings (e.g. 100,000 strings),
     * ensuring that large datasets with extensive unique URL parameters or messages do not overflow
     * or throw {@link IllegalStateException}.
     */
    @Test
    public void testLargeStringDictionaryUncapped() throws Exception
    {
        final GlobalDictionaries dict = new GlobalDictionaries();
        final int targetCount = 100_000;
        final int baseCount = dict.getStringCount(); // 15 pre-interned HTTP strings

        // Intern 100,000 distinct strings
        for (int i = 0; i < targetCount; i++)
        {
            final int id = dict.getOrCreateStringId("https://test.example.com/item?id=" + i);
            Assert.assertEquals(baseCount + i, id);
        }

        Assert.assertEquals(baseCount + targetCount, dict.getStringCount());

        // Verify lock-free lookups
        Assert.assertEquals("https://test.example.com/item?id=0", dict.getString(baseCount));
        Assert.assertEquals("https://test.example.com/item?id=65535", dict.getString(baseCount + 65535));
        Assert.assertEquals("https://test.example.com/item?id=99999", dict.getString(baseCount + 99999));

        // Serialize to binary stream
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final DataOutputStream out = new DataOutputStream(baos))
        {
            dict.writeTo(out);
        }

        // Deserialize back from binary stream
        final GlobalDictionaries restored;
        try (final DataInputStream in = new DataInputStream(new ByteArrayInputStream(baos.toByteArray())))
        {
            restored = GlobalDictionaries.readFrom(in);
        }

        Assert.assertEquals(baseCount + targetCount, restored.getStringCount());
        Assert.assertEquals("https://test.example.com/item?id=0", restored.getString(baseCount));
        Assert.assertEquals("https://test.example.com/item?id=65535", restored.getString(baseCount + 65535));
        Assert.assertEquals("https://test.example.com/item?id=99999", restored.getString(baseCount + 99999));
    }

    /**
     * Verifies that ChunkIngestionCollector safely handles concurrent ingestion from multiple
     * worker threads without lock contention, using thread-local builders and parallel finalization.
     */
    @Test
    public void testParallelCollectorIngestion() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final int threadCount = 8;
        final int recordsPerThread = 20_000;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        final List<Callable<Void>> tasks = new ArrayList<>();
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            tasks.add(() -> {
                final List<Data> batch = new ArrayList<>(1000);
                for (int i = 0; i < recordsPerThread; i++)
                {
                    final RequestData r = new RequestData("Action-" + (i % 10));
                    r.setTime(1_700_000_000_000L + (threadId * 1_000_000L) + i);
                    r.setRunTime(50 + (i % 20));
                    r.setResponseCode(200);
                    r.setAgentName("Agent-" + threadId);
                    r.setTransactionName("TOrder");
                    batch.add(r);

                    if (batch.size() == 1000)
                    {
                        collector.collect(batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty())
                {
                    collector.collect(batch);
                }
                return null;
            });
        }

        final List<Future<Void>> futures = executor.invokeAll(tasks);
        for (final Future<Void> f : futures)
        {
            f.get();
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        final ChunkStorage storage = collector.finish();
        Assert.assertNotNull(storage);
        Assert.assertEquals((long) threadCount * recordsPerThread, storage.getTotalRowCount());

        // Scan and verify records
        final List<Data> results = new ArrayList<>();
        final ScanPredicate allPred = new ScanPredicate(0, Long.MAX_VALUE, null, null);
        storage.scan('R', allPred, results::add);
        Assert.assertEquals((long) threadCount * recordsPerThread, results.size());
    }

    /**
     * Verifies generic record chunking with chunk-local string dictionary isolation.
     */
    @Test
    public void testGenericDataChunkWithChunkLocalStrings() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();

        // Ingest 500 custom log records with arbitrary distinct field strings
        for (int i = 0; i < 500; i++)
        {
            final CustomLogRecord rec = new CustomLogRecord("CustomTimer-" + (i % 3),
                                                            "dynamic-payload-value-" + i,
                                                            "meta-info-" + (i % 5));
            rec.setTime(1_700_000_000_000L + i);
            rec.setAgentName("Agent-1");
            rec.setTransactionName("TScenario");
            collector.collect(rec);
        }

        final ChunkStorage storage = collector.finish();
        Assert.assertNotNull(storage);
        Assert.assertEquals(500, storage.getTotalRowCount());

        // Global dictionaries should NOT have been polluted with generic record column values (only 15 pre-interned HTTP strings exist)
        Assert.assertEquals(15, storage.getDictionaries().getStringCount());

        // Save and reload from disk
        final File genericCacheDir = new File(tempDir, "generic-cache");
        storage.saveToDirectory(genericCacheDir);

        final ChunkStorage loaded = ChunkStorage.loadFromDirectory(genericCacheDir);
        Assert.assertEquals(500, loaded.getTotalRowCount());

        final List<Data> scanned = new ArrayList<>();
        final ScanPredicate allPred = new ScanPredicate(0, Long.MAX_VALUE, null, null);
        loaded.scan('Z', allPred, scanned::add);

        Assert.assertEquals(500, scanned.size());
        final CustomLogRecord first = (CustomLogRecord) scanned.get(0);
        Assert.assertEquals("CustomTimer-0", first.getName());
        Assert.assertEquals("dynamic-payload-value-0", first.getExtra1());
        Assert.assertEquals("meta-info-0", first.getExtra2());
    }

    /**
     * Verifies that generic record chunking correctly handles rows whose serialized columns
     * exceed the initial 4KB row buffer, ensuring rowBuffer expansion preserves all preceding columns.
     */
    @Test
    public void testGenericDataChunkWithLargeRowBufferExpansion() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();

        // Create a large 6000-byte payload for extra2, which forces rowBuffer expansion in GenericDataChunkHandler
        final String largePayload = "X".repeat(6000);

        final CustomLogRecord rec = new CustomLogRecord("CustomTimer-Large",
                                                        "extra-one-value",
                                                        largePayload);
        rec.setTime(1_700_000_000_000L);
        rec.setAgentName("Agent-1");
        rec.setTransactionName("TScenario");
        collector.collect(rec);

        final ChunkStorage storage = collector.finish();
        Assert.assertNotNull(storage);
        Assert.assertEquals(1, storage.getTotalRowCount());

        final List<Data> scanned = new ArrayList<>();
        final ScanPredicate allPred = new ScanPredicate(0, Long.MAX_VALUE, null, null);
        storage.scan('Z', allPred, scanned::add);

        Assert.assertEquals(1, scanned.size());
        final CustomLogRecord result = (CustomLogRecord) scanned.get(0);
        Assert.assertEquals("CustomTimer-Large", result.getName());
        Assert.assertEquals("extra-one-value", result.getExtra1());
        Assert.assertEquals(largePayload, result.getExtra2());
    }

    /**
     * Verifies direct-to-disk streaming ingestion via {@link com.xceptance.xlt.report.storage.chunk.ChunkSpooler},
     * ensuring that lightweight {@link DiskChunk} descriptors are populated during parsing, the V2 header
     * and metadata index table are written without buffer overflow, and queries accurately load payloads on demand.
     */
    @Test
    public void testDirectToDiskSpoolingAndQuery() throws Exception
    {
        final File spoolDir = new File(tempDir, "spool-test");
        spoolDir.mkdirs();

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(spoolDir);
        final long baseTime = 1_700_000_000_000L;

        // Ingest 5,000 requests across multiple agents
        for (int i = 0; i < 5000; i++)
        {
            final RequestData r = new RequestData("Action-" + (i % 10));
            r.setTime(baseTime + (i * 20L));
            r.setRunTime(50 + (i % 50));
            r.setResponseCode(i % 100 == 0 ? 500 : 200);
            r.setFailed(i % 100 == 0);
            r.setBytesSent(256);
            r.setBytesReceived(1024 + i);
            r.setHttpMethod("GET");
            r.setContentType("text/html");
            r.setUrl("https://example.com/item?id=" + (i % 25));
            r.setAgentName("Agent-" + (i % 4));
            r.setTransactionName("TScenario");

            collector.collect(r);
        }

        // Finish spooling - writes metadata index table and 44-byte V2 header
        try (final ChunkStorage storage = collector.finish())
        {
            Assert.assertNotNull(storage);
            Assert.assertEquals(5000, storage.getCatalog().getTotalRowCount());
            Assert.assertTrue(storage.isPersisted());

            // Chunks file and dictionaries must exist on disk
            final File chunksFile = new File(spoolDir, ChunkStorage.CHUNKS_FILE_NAME);
            final File dictFile = new File(spoolDir, ChunkStorage.DICTIONARIES_FILE_NAME);
            Assert.assertTrue(chunksFile.exists());
            Assert.assertTrue(dictFile.exists());

            // First chunk must be a DiskChunk
            Assert.assertFalse(storage.getCatalog().getAllChunks().isEmpty());
            Assert.assertTrue(storage.getCatalog().getAllChunks().get(0) instanceof com.xceptance.xlt.report.storage.chunk.DiskChunk);

            // Scan all requests from the active storage
            final List<Data> activeScanned = new ArrayList<>();
            final ScanPredicate allPred = new ScanPredicate(0, Long.MAX_VALUE, null, null);
            storage.scan('R', allPred, activeScanned::add);
            Assert.assertEquals(5000, activeScanned.size());

            final RequestData first = (RequestData) activeScanned.get(0);
            Assert.assertEquals("Action-0", first.getName());
            Assert.assertEquals(baseTime, first.getTime());
            Assert.assertEquals(50, first.getRunTime());
            Assert.assertEquals(500, first.getResponseCode());
            Assert.assertTrue(first.hasFailed());
        }

        // Now verify loading back fresh from directory in V2 format
        try (final ChunkStorage loaded = ChunkStorage.loadFromDirectory(spoolDir))
        {
            Assert.assertNotNull(loaded);
            Assert.assertEquals(5000, loaded.getCatalog().getTotalRowCount());
            Assert.assertEquals(baseTime, loaded.getMinTime());

            final List<Data> reloadedScanned = new ArrayList<>();
            final ScanPredicate allPred = new ScanPredicate(0, Long.MAX_VALUE, null, null);
            loaded.scan('R', allPred, reloadedScanned::add);
            Assert.assertEquals(5000, reloadedScanned.size());
        }
    }

    @Test
    public void testSpoolerWithMixedInMemoryAndDiskChunks() throws Exception
    {
        final File spoolDir = new File(tempDir, "mixed-spool-test");
        spoolDir.mkdirs();

        final ChunkSpooler spooler = new ChunkSpooler(spoolDir);
        final ChunkCatalog catalog = new ChunkCatalog();
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeRegistry registry = new ChunkTypeRegistry();
        final ChunkTypeHandler handler = registry.getHandler('R');

        // 1. Spool one chunk to disk
        final RequestData r1 = new RequestData("R1");
        r1.setTime(1000L);
        r1.setRunTime(10);
        final ChunkBuilder builder1 = handler.newChunkBuilder(dicts);
        builder1.append(r1);
        final Chunk chunk1 = builder1.seal();
        final DiskChunk diskChunk = spooler.spoolChunk(chunk1, handler);
        catalog.addChunk(diskChunk);

        // 2. Add an in-memory chunk directly to the catalog without spooling
        final RequestData r2 = new RequestData("R2");
        r2.setTime(2000L);
        r2.setRunTime(20);
        final ChunkBuilder builder2 = handler.newChunkBuilder(dicts);
        builder2.append(r2);
        final Chunk inMemoryChunk = builder2.seal();
        catalog.addChunk(inMemoryChunk);

        // 3. Spooler finish: should handle mixed chunks without throwing EOFException on reload
        spooler.finish(catalog, dicts);

        // 4. Reload catalog from disk
        final ChunkCatalog reloaded = ChunkCatalog.readFrom(new File(spoolDir, ChunkStorage.CHUNKS_FILE_NAME), dicts, registry);
        Assert.assertNotNull(reloaded);
        Assert.assertTrue("Reloaded catalog should contain chunks", reloaded.getChunkCount() > 0);
    }

    @Test
    public void testLargeStringAndStackTraceHandling() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();

        // 70,000 characters stack trace (> 65,535 bytes in UTF-8)
        final String largeStackTrace = "E".repeat(70_000);
        final TransactionData t = new TransactionData("TOrderLarge");
        t.setTime(1000L);
        t.setRunTime(500);
        t.setFailed(true);
        t.setFailedActionName("ActionLarge");
        t.setFailureStackTrace(largeStackTrace);
        t.setAgentName("Agent-1");
        t.setTransactionName("TOrderLarge");
        collector.collect(t);

        // Large custom data payload (> 65,535 bytes)
        final String largeCustomPayload = "C".repeat(70_000);
        final CustomLogRecord custom = new CustomLogRecord("CustomLarge", "extra1", largeCustomPayload);
        custom.setTime(1001L);
        custom.setAgentName("Agent-1");
        custom.setTransactionName("TOrderLarge");
        collector.collect(custom);

        final ChunkStorage storage = collector.finish();
        Assert.assertNotNull(storage);

        final File largeDir = new File(tempDir, "large-string-test");
        storage.saveToDirectory(largeDir);

        final ChunkStorage loaded = ChunkStorage.loadFromDirectory(largeDir);
        Assert.assertNotNull(loaded);

        final List<Data> scannedTx = new ArrayList<>();
        loaded.scan('T', ScanPredicate.ALL, scannedTx::add);
        Assert.assertEquals(1, scannedTx.size());
        final TransactionData loadedTx = (TransactionData) scannedTx.get(0);
        Assert.assertTrue(loadedTx.hasFailed());
        Assert.assertEquals(largeStackTrace, loadedTx.getFailureStackTrace());

        final List<Data> scannedCustom = new ArrayList<>();
        loaded.scan('Z', ScanPredicate.ALL, scannedCustom::add);
        Assert.assertEquals(1, scannedCustom.size());
        final CustomLogRecord loadedCustom = (CustomLogRecord) scannedCustom.get(0);
        Assert.assertEquals(largeCustomPayload, loadedCustom.getExtra2());
    }

    @Test
    public void testHighTimerIdSampleUrlCap() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        // Ingest 4500 distinct timer names, each with multiple requests
        for (int i = 0; i < 4500; i++)
        {
            final String timerName = "Timer-" + i;
            for (int j = 0; j < 3; j++)
            {
                final RequestData r = new RequestData(timerName);
                r.setTime(1000L + i);
                r.setRunTime(10);
                r.setUrl("https://example.com/timer/" + i + "/sample" + j);
                r.setAgentName("Agent-0");
                r.setTransactionName("Tx");
                collector.collect(r);
            }
        }

        final ChunkStorage storage = collector.finish();
        Assert.assertNotNull(storage);
        final GlobalDictionaries dicts = storage.getDictionaries();
        Assert.assertTrue("Should have interned over 4096 timer IDs", dicts.getTimerNameCount() >= 4500);

        // Check that sample URLs were capped properly
        for (int i = 0; i < 4500; i += 500)
        {
            final int timerId = dicts.getOrCreateTimerNameId("Timer-" + i);
            final GlobalDictionaries.CachedSampleUrl[] samples = dicts.getCachedSampleUrls(timerId);
            Assert.assertNotNull(samples);
            Assert.assertTrue(samples.length <= 5);
        }
    }

    @Test
    public void testGlobalDictionariesConstructorIsolationOnRead() throws Exception
    {
        final GlobalDictionaries original = new GlobalDictionaries();
        // Add non-default strings
        final int id1 = original.getOrCreateStringId("custom-app-1");
        final int id2 = original.getOrCreateStringId("custom-app-2");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        original.writeTo(new DataOutputStream(baos));

        final GlobalDictionaries restored = GlobalDictionaries.readFrom(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        Assert.assertEquals("custom-app-1", restored.getString(id1));
        Assert.assertEquals("custom-app-2", restored.getString(id2));

        // Interning a new string in the restored dictionary should allocate id = stringCount
        final int initialCount = restored.getStringCount();
        final int newId = restored.getOrCreateStringId("custom-app-3");
        Assert.assertEquals(initialCount, newId);
        Assert.assertEquals("custom-app-3", restored.getString(newId));
    }

    @Test
    public void testConcurrentPositionalFileChannelReads() throws Exception
    {
        final File spoolDir = new File(tempDir, "concurrent-reads-test");
        spoolDir.mkdirs();

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(spoolDir);
        for (int i = 0; i < 5000; i++)
        {
            final RequestData r = new RequestData("Action-" + (i % 10));
            r.setTime(1000L + i);
            r.setRunTime(20);
            r.setAgentName("Agent-1");
            r.setTransactionName("TOrder");
            collector.collect(r);
        }

        final ChunkStorage storage = collector.finish();
        final int threadCount = 8;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final List<Callable<Void>> tasks = new ArrayList<>();

        for (int t = 0; t < threadCount; t++)
        {
            tasks.add(() -> {
                final List<Data> results = new ArrayList<>();
                storage.scan('R', ScanPredicate.ALL, results::add);
                Assert.assertEquals(5000, results.size());
                return null;
            });
        }

        final List<Future<Void>> futures = executor.invokeAll(tasks);
        for (final Future<Void> f : futures)
        {
            f.get();
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        storage.close();
    }

    @Test
    public void testUnifiedV2CatalogFormat() throws Exception
    {
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        for (int i = 0; i < 100; i++)
        {
            final RequestData r = new RequestData("R" + i);
            r.setTime(1000L + i);
            r.setRunTime(10);
            r.setAgentName("A");
            r.setTransactionName("T");
            collector.collect(r);
        }
        final ChunkStorage inMemoryStorage = collector.finish();
        Assert.assertFalse(inMemoryStorage.isPersisted());

        final File saveDir = new File(tempDir, "v2-unified-test");
        inMemoryStorage.saveToDirectory(saveDir);

        // Check that chunks.bin starts with V2 MAGIC
        final File chunksFile = new File(saveDir, ChunkStorage.CHUNKS_FILE_NAME);
        try (final java.io.DataInputStream in = new java.io.DataInputStream(new java.io.FileInputStream(chunksFile)))
        {
            final int magic = in.readInt();
            Assert.assertEquals("chunks.bin must be saved in V2 format with MAGIC",
                                ChunkCatalog.MAGIC, magic);
        }

        // Check that dictionaries.bin starts with MAGIC and VERSION
        final File dictFile = new File(saveDir, ChunkStorage.DICTIONARIES_FILE_NAME);
        try (final java.io.DataInputStream in = new java.io.DataInputStream(new java.io.FileInputStream(dictFile)))
        {
            final int magic = in.readInt();
            final int version = in.readInt();
            Assert.assertEquals("dictionaries.bin must start with MAGIC", GlobalDictionaries.MAGIC, magic);
            Assert.assertEquals("dictionaries.bin must start with VERSION", GlobalDictionaries.VERSION, version);
        }

        // Ensure loadFromDirectory works cleanly
        try (final ChunkStorage loaded = ChunkStorage.loadFromDirectory(saveDir))
        {
            Assert.assertNotNull(loaded);
            Assert.assertEquals(100, loaded.getTotalRowCount());
        }
    }

    @Test
    public void testFormatVersionMismatchInFingerprintPurgesCache() throws Exception
    {
        final File resultsDir = new File(tempDir, "res-fmt-test");
        resultsDir.mkdirs();
        final File log = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        collector.collect(r);
        cacheManager.save(collector.finish());

        Assert.assertTrue(cacheManager.isCacheValid());

        // Overwrite fingerprint.bin with formatVersion = 999
        final File cacheDir = new File(resultsDir, "xlt-cache");
        final CacheFingerprint current = CacheFingerprint.calculate(resultsDir, config);
        final CacheFingerprint futureFormat = new CacheFingerprint(999, current.fileCount(), current.totalBytes(), current.mergeRulesHash(), current.xltVersion());
        futureFormat.saveToDirectory(cacheDir);

        // Verification: must detect format incompatibility and purge
        Assert.assertFalse("Cache with future formatVersion must be invalid", cacheManager.isCacheValid());
        Assert.assertFalse("Cache directory must be purged on format mismatch", cacheDir.exists());
    }

    @Test
    public void testReleaseVersionMismatchInFingerprintPurgesCache() throws Exception
    {
        final File resultsDir = new File(tempDir, "res-rel-test");
        resultsDir.mkdirs();
        final File log = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        collector.collect(r);
        cacheManager.save(collector.finish());

        Assert.assertTrue(cacheManager.isCacheValid());

        // Overwrite fingerprint with a different XLT release version
        final File cacheDir = new File(resultsDir, "xlt-cache");
        final CacheFingerprint current = CacheFingerprint.calculate(resultsDir, config);
        final CacheFingerprint otherXltRelease = new CacheFingerprint(current.formatVersion(), current.fileCount(), current.totalBytes(), current.mergeRulesHash(), "99.9.9");
        otherXltRelease.saveToDirectory(cacheDir);

        Assert.assertFalse("Cache from different XLT release must be invalid", cacheManager.isCacheValid());
        Assert.assertFalse("Cache directory must be purged on release version mismatch", cacheDir.exists());
    }

    @Test
    public void testLegacyUnversionedFingerprintTriggersPurge() throws Exception
    {
        final File resultsDir = new File(tempDir, "res-legacy-test");
        resultsDir.mkdirs();
        final File log = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        collector.collect(r);
        cacheManager.save(collector.finish());

        Assert.assertTrue(cacheManager.isCacheValid());

        // Write legacy unversioned fingerprint.bin (int fileCount, long totalBytes, String hash, String xltVersion)
        final File cacheDir = new File(resultsDir, "xlt-cache");
        final File fpFile = new File(cacheDir, "fingerprint.bin");
        final CacheFingerprint current = CacheFingerprint.calculate(resultsDir, config);
        try (final java.io.DataOutputStream out = new java.io.DataOutputStream(new FileOutputStream(fpFile)))
        {
            out.writeInt(current.fileCount());
            out.writeLong(current.totalBytes());
            out.writeUTF(current.mergeRulesHash());
            out.writeUTF(current.xltVersion());
        }

        // CacheManager load should interpret legacy format as formatVersion = 0 and fail matches()
        Assert.assertFalse("Legacy unversioned cache must be purged", cacheManager.isCacheValid());
        Assert.assertFalse("Cache directory must be purged", cacheDir.exists());
    }

    @Test
    public void testIncompatibleDictionariesOrChunksVersionPurgesOnLoad() throws Exception
    {
        final File resultsDir = new File(tempDir, "res-dict-err-test");
        resultsDir.mkdirs();
        final File log = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        collector.collect(r);
        cacheManager.save(collector.finish());

        Assert.assertTrue(cacheManager.isCacheValid());

        // 1. Corrupt dictionaries.bin version
        final File cacheDir = new File(resultsDir, "xlt-cache");
        final File dictFile = new File(cacheDir, ChunkStorage.DICTIONARIES_FILE_NAME);
        try (final java.io.RandomAccessFile raf = new java.io.RandomAccessFile(dictFile, "rw"))
        {
            raf.seek(4); // Seek to version
            raf.writeInt(999);
        }

        // cacheManager.load() should catch the version mismatch, purge the cache, and return null
        final ChunkStorage loaded = cacheManager.load();
        Assert.assertNull("Storage must return null when dictionaries version is incompatible", loaded);
        Assert.assertFalse("Cache directory must be purged on load error", cacheDir.exists());
    }

    @Test
    public void testIncompatibleChunksCatalogVersionPeekAndLoad() throws Exception
    {
        final File resultsDir = new File(tempDir, "res-chunks-err-test");
        resultsDir.mkdirs();
        final File log = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        collector.collect(r);
        cacheManager.save(collector.finish());

        Assert.assertTrue(cacheManager.isCacheValid());

        // Corrupt chunks.bin version at offset 4
        final File cacheDir = new File(resultsDir, "xlt-cache");
        final File chunksFile = new File(cacheDir, ChunkStorage.CHUNKS_FILE_NAME);
        try (final java.io.RandomAccessFile raf = new java.io.RandomAccessFile(chunksFile, "rw"))
        {
            raf.seek(4); // Seek to version
            raf.writeInt(999);
        }

        // peekTimeRange should reject the incompatible version and return null
        Assert.assertNull("peekTimeRange must return null on chunks.bin version mismatch", cacheManager.peekTimeRange());

        // load() should fail cleanly and purge cache
        final ChunkStorage loaded = cacheManager.load();
        Assert.assertNull("Storage must return null when chunks version is incompatible", loaded);
        Assert.assertFalse("Cache directory must be purged on load error", cacheDir.exists());
    }

    @Test
    public void testUserNumberRoundTripAcrossAllRecordTypes() throws Exception
    {
        final File spoolDir = new File(tempDir, "user-number-all-types");
        spoolDir.mkdirs();

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(spoolDir);
        final long baseTime = 1_700_000_000_000L;

        // 1. RequestData ('R')
        final RequestData req = new RequestData("Req-1");
        req.setTime(baseTime + 1);
        req.setRunTime(25);
        req.setAgentName("Agent-1");
        req.setTransactionName("TCheckout");
        req.setTestUserNumber("101");
        collector.collect(req);

        // 2. TransactionData ('T')
        final TransactionData tx = new TransactionData("TCheckout");
        tx.setTime(baseTime + 2);
        tx.setRunTime(250);
        tx.setAgentName("Agent-1");
        tx.setTransactionName("TCheckout");
        tx.setTestUserNumber("102");
        collector.collect(tx);

        // 3. ActionData ('A')
        final ActionData act = new ActionData("OpenCart");
        act.setTime(baseTime + 3);
        act.setRunTime(80);
        act.setAgentName("Agent-1");
        act.setTransactionName("TCheckout");
        act.setTestUserNumber("103");
        collector.collect(act);

        // 4. CustomData ('C')
        final CustomData cust = new CustomData("CustomTimer");
        cust.setTime(baseTime + 4);
        cust.setRunTime(15);
        cust.setAgentName("Agent-1");
        cust.setTransactionName("TCheckout");
        cust.setTestUserNumber("104");
        collector.collect(cust);

        // 5. PageLoadTimingData ('P')
        final PageLoadTimingData page = new PageLoadTimingData("PageLoad");
        page.setTime(baseTime + 5);
        page.setRunTime(120);
        page.setAgentName("Agent-1");
        page.setTransactionName("TCheckout");
        page.setTestUserNumber("105");
        collector.collect(page);

        // 6. WebVitalData ('W')
        final WebVitalData vital = new WebVitalData("LCP");
        vital.setTime(baseTime + 6);
        vital.setValue(450.5);
        vital.setAgentName("Agent-1");
        vital.setTransactionName("TCheckout");
        vital.setTestUserNumber("106");
        collector.collect(vital);

        // 7. CustomValue ('V')
        final CustomValue val = new CustomValue("QueueDepth");
        val.setTime(baseTime + 7);
        val.setValue(12.25);
        val.setAgentName("Agent-1");
        val.setTransactionName("TCheckout");
        val.setTestUserNumber("107");
        collector.collect(val);

        // 8. EventData ('E')
        final EventData evt = new EventData("WarnEvent");
        evt.setTime(baseTime + 8);
        evt.setTestCaseName("TCheckout");
        evt.setMessage("Inventory low");
        evt.setAgentName("Agent-1");
        evt.setTransactionName("TCheckout");
        evt.setTestUserNumber("108");
        collector.collect(evt);

        try (final ChunkStorage active = collector.finish())
        {
            Assert.assertNotNull(active);
        }

        try (final ChunkStorage loaded = ChunkStorage.loadFromDirectory(spoolDir))
        {
            final char[] types = {'R', 'T', 'A', 'C', 'P', 'W', 'V', 'E'};
            final String[] expectedUserNumbers = {"101", "102", "103", "104", "105", "106", "107", "108"};

            for (int i = 0; i < types.length; i++)
            {
                final List<Data> rows = new ArrayList<>();
                loaded.scan(types[i], ScanPredicate.ALL, rows::add);
                Assert.assertEquals("Expected 1 row for type " + types[i], 1, rows.size());
                Assert.assertEquals("User number mismatch for type " + types[i],
                                    expectedUserNumbers[i], rows.get(0).getTestUserNumber());
            }
        }
    }

    @Test
    public void testNonFailedTransactionDirectoryNamePreservation() throws Exception
    {
        final File spoolDir = new File(tempDir, "tx-dir-preserve");
        spoolDir.mkdirs();

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(spoolDir);
        final long baseTime = 1_700_000_000_000L;

        // Non-failed transaction with a Result Browser dump directory (e.g. output2disk = always)
        final TransactionData txSuccessWithDump = new TransactionData("TBrowse");
        txSuccessWithDump.setTime(baseTime);
        txSuccessWithDump.setRunTime(150);
        txSuccessWithDump.setFailed(false);
        txSuccessWithDump.setDirectoryName("dump-success-0001");
        txSuccessWithDump.setTestUserNumber("0");
        txSuccessWithDump.setAgentName("Agent-1");
        txSuccessWithDump.setTransactionName("TBrowse");
        collector.collect(txSuccessWithDump);

        // Normal non-failed transaction without dump directory
        final TransactionData txSuccessNoDump = new TransactionData("TBrowse");
        txSuccessNoDump.setTime(baseTime + 100);
        txSuccessNoDump.setRunTime(120);
        txSuccessNoDump.setFailed(false);
        txSuccessNoDump.setTestUserNumber("1");
        txSuccessNoDump.setAgentName("Agent-1");
        txSuccessNoDump.setTransactionName("TBrowse");
        collector.collect(txSuccessNoDump);

        try (final ChunkStorage active = collector.finish())
        {
            Assert.assertNotNull(active);
            final Chunk txChunk = active.getCatalog().getChunksForType('T').get(0);
            Assert.assertFalse("Chunk with only non-failed transactions must have hasFailures() == false",
                               txChunk.hasFailures());
        }

        try (final ChunkStorage loaded = ChunkStorage.loadFromDirectory(spoolDir))
        {
            final List<Data> rows = new ArrayList<>();
            loaded.scan('T', ScanPredicate.ALL, rows::add);
            Assert.assertEquals(2, rows.size());

            final TransactionData first = (TransactionData) rows.get(0);
            Assert.assertFalse("Transaction must remain non-failed", first.hasFailed());
            Assert.assertEquals("dump-success-0001", first.getDirectoryName());

            final TransactionData second = (TransactionData) rows.get(1);
            Assert.assertFalse(second.hasFailed());
            Assert.assertNull(second.getDirectoryName());
        }
    }

    @Test
    public void testRequestFormDataEncodingAndForensicTailSparseAndFullModes() throws Exception
    {
        final long baseTime = 1_700_000_000_000L;

        // 1. Test default SPARSE mode
        RequestChunkHandler.setRequestDetailsMode("sparse");
        final File sparseDir = new File(tempDir, "forensic-sparse");
        sparseDir.mkdirs();

        final ChunkIngestionCollector sparseCollector = new ChunkIngestionCollector(sparseDir);
        populateForensicTestRequests(sparseCollector, baseTime);
        try (final ChunkStorage s = sparseCollector.finish())
        {
            Assert.assertNotNull(s);
        }

        try (final ChunkStorage loadedSparse = ChunkStorage.loadFromDirectory(sparseDir))
        {
            final List<Chunk> rChunks = loadedSparse.getCatalog().getChunksForType('R');
            Assert.assertEquals(1, rChunks.size());
            final DiskChunk dc = (DiskChunk) rChunks.get(0);

            // Vector payload length must be strictly smaller than full payload length (since forensic tail is non-empty)
            Assert.assertTrue("vectorPayloadLength must be > 0", dc.getVectorPayloadLength() > 0);
            Assert.assertTrue("vectorPayloadLength must be < payloadLength", dc.getVectorPayloadLength() < dc.getPayloadLength());

            final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler('R');
            final RequestChunk fullChunk = (RequestChunk) dc.load(handler, loadedSparse.getDictionaries());

            // Row 0: fast 200 OK (outside top-256 slowest) -> no forensic details in sparse mode
            Assert.assertNull("Fast 200 OK row outside top-256 must not be in sparse forensic tail",
                              fullChunk.getForensicDetails(0));

            // Row 1: failed 500 -> must have exact forensic details
            final RequestChunk.ForensicDetails errDetails = fullChunk.getForensicDetails(1);
            Assert.assertNotNull("Failed 500 row must be in sparse forensic tail", errDetails);
            Assert.assertEquals("https://example.com/checkout?step=1", errDetails.url());
            Assert.assertEquals("req-err-1", errDetails.requestId());
            Assert.assertEquals("resp-err-1", errDetails.responseId());
            Assert.assertEquals("sku=999&qty=1", errDetails.formData());
            Assert.assertEquals("Checkout.1.1", errDetails.rawName());

            // Row 299: slowest 200 OK -> must be in top-256 slowest of sparse forensic tail
            final RequestChunk.ForensicDetails slowDetails = fullChunk.getForensicDetails(299);
            Assert.assertNotNull("Slowest 200 OK row must be in sparse forensic tail", slowDetails);
            Assert.assertEquals("https://example.com/item?idx=299", slowDetails.url());
            Assert.assertEquals("req-299", slowDetails.requestId());
            Assert.assertEquals("resp-299", slowDetails.responseId());
            Assert.assertEquals("field=299", slowDetails.formData());
            Assert.assertEquals("Action.1.299", slowDetails.rawName());

            // Verify loadVectorsOnly skips the forensic tail completely while loading hot vectors
            final RequestChunk vectorsOnlyChunk = (RequestChunk) dc.loadVectorsOnly(handler, loadedSparse.getDictionaries());
            Assert.assertNotNull(vectorsOnlyChunk.getCompRunTimes());
            Assert.assertNotNull(vectorsOnlyChunk.getCompFormDataEncodings());
            Assert.assertNotNull(vectorsOnlyChunk.getCompUserNumbers());
            Assert.assertNull("loadVectorsOnly must not load cold forensic tail", vectorsOnlyChunk.getForensicDetails(1));

            // Verify scan restores formDataEncoding on 100% of rows and forensic details on retained rows
            final List<Data> scanned = new ArrayList<>();
            loadedSparse.scan('R', ScanPredicate.ALL, scanned::add);
            Assert.assertEquals(300, scanned.size());

            final RequestData row0 = (RequestData) scanned.get(0);
            Assert.assertEquals("application/x-www-form-urlencoded", row0.getFormDataEncoding());
            Assert.assertEquals("42", row0.getTestUserNumber());

            final RequestData row1 = (RequestData) scanned.get(1);
            Assert.assertEquals("multipart/form-data", row1.getFormDataEncoding());
            Assert.assertEquals("https://example.com/checkout?step=1", row1.getUrl().toString());
            Assert.assertEquals("req-err-1", row1.getRequestId());
            Assert.assertEquals("resp-err-1", row1.getResponseId());
            Assert.assertEquals("sku=999&qty=1", row1.getFormData());
            Assert.assertEquals("Checkout.1.1", row1.getRawName());
        }

        // 2. Test FULL mode (--preserve-all-details / requestDetailsMode=full)
        RequestChunkHandler.setRequestDetailsMode("full");
        final File fullDir = new File(tempDir, "forensic-full");
        fullDir.mkdirs();

        final ChunkIngestionCollector fullCollector = new ChunkIngestionCollector(fullDir);
        populateForensicTestRequests(fullCollector, baseTime);
        try (final ChunkStorage s = fullCollector.finish())
        {
            Assert.assertNotNull(s);
        }

        try (final ChunkStorage loadedFull = ChunkStorage.loadFromDirectory(fullDir))
        {
            final DiskChunk dc = (DiskChunk) loadedFull.getCatalog().getChunksForType('R').get(0);
            final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler('R');
            final RequestChunk fullChunk = (RequestChunk) dc.load(handler, loadedFull.getDictionaries());

            // In full mode, even Row 0 (fast 200 OK) must have exact forensic details
            final RequestChunk.ForensicDetails row0Details = fullChunk.getForensicDetails(0);
            Assert.assertNotNull("Full mode must retain forensic details for 100% of rows", row0Details);
            Assert.assertEquals("https://example.com/fast?idx=0", row0Details.url());
            Assert.assertEquals("req-fast-0", row0Details.requestId());
            Assert.assertEquals("resp-fast-0", row0Details.responseId());
            Assert.assertEquals("fast=true", row0Details.formData());
            Assert.assertEquals("FastAction.1.0", row0Details.rawName());
        }
    }

    private void populateForensicTestRequests(final ChunkIngestionCollector collector, final long baseTime)
    {
        // Row 0: fast 200 OK
        final RequestData r0 = new RequestData("FastAction");
        r0.setRawName("FastAction.1.0");
        r0.setTime(baseTime);
        r0.setRunTime(5);
        r0.setResponseCode(200);
        r0.setFailed(false);
        r0.setUrl("https://example.com/fast?idx=0");
        r0.setRequestId("req-fast-0");
        r0.setResponseId("resp-fast-0");
        r0.setFormDataEncoding("application/x-www-form-urlencoded");
        r0.setFormData("fast=true");
        r0.setAgentName("Agent-1");
        r0.setTransactionName("TShop");
        r0.setTestUserNumber("42");
        collector.collect(r0);

        // Row 1: failed 500
        final RequestData r1 = new RequestData("Checkout");
        r1.setRawName("Checkout.1.1");
        r1.setTime(baseTime + 1);
        r1.setRunTime(10);
        r1.setResponseCode(500);
        r1.setFailed(true);
        r1.setUrl("https://example.com/checkout?step=1");
        r1.setRequestId("req-err-1");
        r1.setResponseId("resp-err-1");
        r1.setFormDataEncoding("multipart/form-data");
        r1.setFormData("sku=999&qty=1");
        r1.setAgentName("Agent-1");
        r1.setTransactionName("TShop");
        r1.setTestUserNumber("42");
        collector.collect(r1);

        // Rows 2..299: 298 requests with ascending runtimes (102..399 ms)
        for (int i = 2; i < 300; i++)
        {
            final RequestData r = new RequestData("Action");
            r.setRawName("Action.1." + i);
            r.setTime(baseTime + i);
            r.setRunTime(100 + i);
            r.setResponseCode(200);
            r.setFailed(false);
            r.setUrl("https://example.com/item?idx=" + i);
            r.setRequestId("req-" + i);
            r.setResponseId("resp-" + i);
            r.setFormDataEncoding("application/json");
            r.setFormData("field=" + i);
            r.setAgentName("Agent-1");
            r.setTransactionName("TShop");
            r.setTestUserNumber("42");
            collector.collect(r);
        }
    }

    @Test
    public void testCatalogV3ZoneMapsAndPredicatePruning() throws Exception
    {
        Assert.assertEquals("Catalog version must be 3", 3, ChunkCatalog.VERSION);

        final File spoolDir = new File(tempDir, "zone-maps-v3");
        spoolDir.mkdirs();

        final ChunkSpooler spooler = new ChunkSpooler(spoolDir);
        final ChunkCatalog catalog = new ChunkCatalog();
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeRegistry registry = new ChunkTypeRegistry();
        final ChunkTypeHandler handler = registry.getHandler('R');

        // Chunk 1: fast, no failures (maxRunTime = 80, hasFailures = false)
        final ChunkBuilder b1 = handler.newChunkBuilder(dicts);
        final RequestData r1 = new RequestData("FastReq");
        r1.setTime(1000L);
        r1.setRunTime(80);
        r1.setResponseCode(200);
        r1.setFailed(false);
        b1.append(r1);
        catalog.addChunk(spooler.spoolChunk(b1.seal(), handler));

        // Chunk 2: slow, has failures (maxRunTime = 4500, hasFailures = true)
        final ChunkBuilder b2 = handler.newChunkBuilder(dicts);
        final RequestData r2 = new RequestData("SlowErrReq");
        r2.setTime(2000L);
        r2.setRunTime(4500);
        r2.setResponseCode(503);
        r2.setFailed(true);
        b2.append(r2);
        catalog.addChunk(spooler.spoolChunk(b2.seal(), handler));

        spooler.finish(catalog, dicts);

        final ChunkCatalog reloaded = ChunkCatalog.readFrom(new File(spoolDir, ChunkStorage.CHUNKS_FILE_NAME), dicts, registry);
        final List<Chunk> rChunks = reloaded.getChunksForType('R');
        Assert.assertEquals(2, rChunks.size());

        final DiskChunk dc1 = (DiskChunk) rChunks.get(0);
        final DiskChunk dc2 = (DiskChunk) rChunks.get(1);

        Assert.assertEquals(80, dc1.getMaxRunTime());
        Assert.assertFalse(dc1.hasFailures());

        Assert.assertEquals(4500, dc2.getMaxRunTime());
        Assert.assertTrue(dc2.hasFailures());

        // Prune by minRunTime = 1000 -> only dc2 matches
        final ScanPredicate slowPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 1000, false);
        final List<Chunk> slowMatched = reloaded.queryChunks('R', slowPred);
        Assert.assertEquals(1, slowMatched.size());
        Assert.assertSame(dc2, slowMatched.get(0));

        // Prune by requireFailures = true -> only dc2 matches
        final ScanPredicate failPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 0, true);
        final List<Chunk> failMatched = reloaded.queryChunks('R', failPred);
        Assert.assertEquals(1, failMatched.size());
        Assert.assertSame(dc2, failMatched.get(0));
    }

    @Test
    public void testCacheFingerprintInvalidatesOnRequestDetailsModeChange() throws Exception
    {
        final File resultsDir = new File(tempDir, "res-mode-test");
        resultsDir.mkdirs();
        final File log = new File(resultsDir, "timers.csv");
        try (final FileOutputStream fos = new FileOutputStream(log))
        {
            fos.write("R,1,100,false,200\n".getBytes(StandardCharsets.UTF_8));
        }

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");
        config.setRequestDetailsMode("sparse");

        final CacheManager cacheManager = new CacheManager(resultsDir, config);
        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final RequestData r = new RequestData("R1");
        r.setTime(1000L);
        r.setRunTime(50);
        collector.collect(r);
        cacheManager.save(collector.finish());

        Assert.assertTrue("Cache should be valid in sparse mode", cacheManager.isCacheValid());

        // Switch mode to full -> fingerprint must differ and invalidate cache
        config.setRequestDetailsMode("full");
        final CacheManager fullModeManager = new CacheManager(resultsDir, config);
        Assert.assertFalse("Cache built with sparse mode must be invalidated when full mode is requested",
                           fullModeManager.isCacheValid());
    }

    @Test
    public void testUrlHashColumnAndDistinctUrlCountRoundTrip() throws Exception
    {
        RequestChunkHandler.setRequestDetailsMode("sparse");
        final File spoolDir = new File(tempDir, "url-hash-roundtrip");
        spoolDir.mkdirs();

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(spoolDir);
        final long baseTime = 1_700_000_000_000L;

        // Ingest 600 requests for the same timer with 600 distinct URLs (plus consecutive duplicates)
        // to exercise:
        // 1. Zero-allocation byte-slice hashing and String URL hashing
        // 2. Register cache hit for consecutive identical (timerId, urlHash)
        // 3. GlobalDictionaries sample URL cap (10 samples)
        // 4. Tier 1 -> Tier 2 HLL promotion (> 512 distinct URLs) when scanning from ChunkDB
        for (int i = 0; i < 600; i++)
        {
            final String url = "https://example.com/catalog/product/" + i + "?lang=en#frag" + i;
            for (int dup = 0; dup < 2; dup++)
            {
                final RequestData req = new RequestData("BrowseProduct");
                req.setTime(baseTime + (i * 2L) + dup);
                req.setRunTime(20 + (i % 50));
                req.setResponseCode(200);
                req.setFailed(false);
                req.setUrl(url);
                req.setAgentName("Agent-1");
                req.setTransactionName("TBrowse");
                collector.collect(req);
            }
        }

        try (final ChunkStorage active = collector.finish())
        {
            Assert.assertNotNull(active);
        }

        try (final ChunkStorage loaded = ChunkStorage.loadFromDirectory(spoolDir))
        {
            final int timerId = loaded.getDictionaries().getOrCreateTimerNameId("BrowseProduct");
            Assert.assertEquals("GlobalDictionaries must retain up to 10 sample URLs",
                                10,
                                loaded.getDictionaries().getCachedSampleUrls(timerId).length);

            final DiskChunk dc = (DiskChunk) loaded.getCatalog().getChunksForType('R').get(0);
            final RequestChunkHandler handler = (RequestChunkHandler) ChunkTypeRegistry.getInstance().getHandler('R');
            final RequestChunk vectorsOnly = (RequestChunk) dc.loadVectorsOnly(handler, loaded.getDictionaries());
            Assert.assertNotNull(vectorsOnly.getCompUrlHashes());
            Assert.assertTrue("compUrlHashes must be non-empty", vectorsOnly.getCompUrlHashes().length > 0);

            // 1. Scan via PostProcessedDataContainer (hot vector path used by ReportGeneratorMain)
            final com.xceptance.xlt.api.report.PostProcessedDataContainer container =
                new com.xceptance.xlt.api.report.PostProcessedDataContainer(1200, 1);
            handler.scan(dc, ScanPredicate.ALL, container, loaded.getDictionaries());
            Assert.assertEquals(1200, container.dataList.size());

            final com.xceptance.xlt.report.providers.DummyReportGeneratorConfiguration config =
                new com.xceptance.xlt.report.providers.DummyReportGeneratorConfiguration();
            config.setChartStartTime(baseTime);
            config.setChartEndTime(baseTime + 10_000L);
            final com.xceptance.xlt.report.providers.RequestsReportProvider provider =
                new com.xceptance.xlt.report.providers.RequestsReportProvider();
            provider.setConfiguration(config);
            final com.xceptance.xlt.report.providers.RequestDataProcessor proc1 =
                new com.xceptance.xlt.report.providers.RequestDataProcessor("BrowseProduct", provider);
            for (int i = 0; i < container.dataList.size(); i++)
            {
                proc1.processDataRecord(container.dataList.get(i));
            }
            final com.xceptance.xlt.report.providers.RequestReport r1 =
                (com.xceptance.xlt.report.providers.RequestReport) proc1.createTimerReport(false);
            Assert.assertEquals(10, r1.urls.list.size());
            Assert.assertEquals("HLL estimate for 600 distinct URLs from container scan must be within 5%",
                                600.0, r1.urls.total, 600.0 * 0.05);

            // 2. Scan via Consumer<Data> overload
            final com.xceptance.xlt.report.providers.RequestDataProcessor proc2 =
                new com.xceptance.xlt.report.providers.RequestDataProcessor("BrowseProduct", provider);
            loaded.scan('R', ScanPredicate.ALL, proc2::processDataRecord);
            final com.xceptance.xlt.report.providers.RequestReport r2 =
                (com.xceptance.xlt.report.providers.RequestReport) proc2.createTimerReport(false);
            Assert.assertEquals(10, r2.urls.list.size());
            Assert.assertEquals("Container scan and Consumer scan must produce identical distinct URL count",
                                r1.urls.total, r2.urls.total);

            // 3. Test legacy RequestChunk constructor without compUrlHashes (fallback to sample URL hash)
            final RequestChunk legacyChunk = new RequestChunk(
                vectorsOnly.getRowCount(), vectorsOnly.getMinTime(), vectorsOnly.getMaxTime(),
                vectorsOnly.getMaxRunTime(), vectorsOnly.hasFailures(),
                vectorsOnly.getTimerNameIds(), vectorsOnly.getAgentTestCaseIds(), vectorsOnly.getFailedRows(),
                vectorsOnly.getCompTimeOffsets(), vectorsOnly.getCompTimerNameIds(), vectorsOnly.getCompAgentTestCaseIds(),
                vectorsOnly.getCompRunTimes(), vectorsOnly.getCompResponseCodes(), vectorsOnly.getCompBytesSent(),
                vectorsOnly.getCompBytesReceived(), vectorsOnly.getCompDnsTimes(), vectorsOnly.getCompConnectTimes(),
                vectorsOnly.getCompSendTimes(), vectorsOnly.getCompServerBusyTimes(), vectorsOnly.getCompReceiveTimes(),
                vectorsOnly.getCompTimeToFirstBytes(), vectorsOnly.getCompTimeToLastBytes(),
                vectorsOnly.getCompHttpMethods(), vectorsOnly.getCompContentTypes(), vectorsOnly.getCompUsedIps(),
                vectorsOnly.getCompIpAddresses(), vectorsOnly.getCompUserNumbers(), vectorsOnly.getCompFormDataEncodings(),
                vectorsOnly.getForensicRows(), vectorsOnly.getUncompressedForensicLength(), vectorsOnly.getCompressedForensicTail());
            Assert.assertEquals(0, legacyChunk.getCompUrlHashes().length);

            final List<Data> legacyConsumerRows = new ArrayList<>();
            handler.scan(legacyChunk, ScanPredicate.ALL, legacyConsumerRows::add, loaded.getDictionaries());
            Assert.assertEquals(1200, legacyConsumerRows.size());

            final com.xceptance.xlt.api.report.PostProcessedDataContainer legacyContainer =
                new com.xceptance.xlt.api.report.PostProcessedDataContainer(1200, 1);
            handler.scan(legacyChunk, ScanPredicate.ALL, legacyContainer, loaded.getDictionaries());
            Assert.assertEquals(1200, legacyContainer.dataList.size());
        }
    }

    /**
     * Verifies {@link com.xceptance.xlt.report.ChunkQueryEngine} progress bar updates across candidate chunk scanning
     * and provider merging (including empty candidate chunk sets), nested directory fingerprint calculation,
     * and {@link CacheManager#loadValidated()} / {@link CacheManager#peekTimeRangeValidated()}.
     */
    @Test
    public void testChunkQueryEngineProgressBarAndNestedFingerprint() throws Exception
    {
        final File resultsDir = new File(tempDir, "nested-results");
        final File userDir = new File(resultsDir, "ac001/TBrowse/0");
        final File skippedOutputDir = new File(resultsDir, "ac001/output");
        final File skippedGitDir = new File(resultsDir, ".git");
        userDir.mkdirs();
        skippedOutputDir.mkdirs();
        skippedGitDir.mkdirs();

        // Write standard, dated, and compact timer files plus non-timer and skipped-directory files
        Files.writeString(new File(userDir, "timers.csv.gz").toPath(), "R,1,100,false,200\n", StandardCharsets.UTF_8);
        Files.writeString(new File(userDir, "timers.csv.2025-07-17.gz").toPath(), "R,2,100,false,200\n", StandardCharsets.UTF_8);
        Files.writeString(new File(userDir, "timer-wd-1.csv.gz").toPath(), "W,3,100\n", StandardCharsets.UTF_8);
        Files.writeString(new File(userDir, "other.txt").toPath(), "ignored", StandardCharsets.UTF_8);
        Files.writeString(new File(userDir, "timer-invalid.txt").toPath(), "ignored", StandardCharsets.UTF_8);
        Files.writeString(new File(skippedOutputDir, "timers.csv").toPath(), "ignored-in-output", StandardCharsets.UTF_8);
        Files.writeString(new File(skippedGitDir, "timers.csv").toPath(), "ignored-in-git", StandardCharsets.UTF_8);

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final CacheFingerprint fp = CacheFingerprint.calculate(resultsDir, config);
        Assert.assertEquals("Only the 3 valid timer files outside skipped directories must be counted", 3, fp.fileCount());

        final ChunkIngestionCollector collector = new ChunkIngestionCollector();
        final long baseTime = 1_700_000_000_000L;
        for (int i = 0; i < 50; i++)
        {
            final RequestData r = new RequestData("Req-" + (i % 5));
            r.setTime(baseTime + i * 10L);
            r.setRunTime(20 + i);
            r.setResponseCode(200);
            r.setAgentName("ac001");
            r.setTransactionName("TBrowse");
            collector.collect(r);
        }

        final ChunkStorage storage = collector.finish();
        final CacheManager cm = new CacheManager(resultsDir, config);
        cm.save(storage);

        Assert.assertTrue(cm.isCacheValid());
        final long[] range = cm.peekTimeRangeValidated();
        Assert.assertNotNull(range);
        Assert.assertEquals(baseTime, range[0]);
        Assert.assertEquals(baseTime + 490L, range[1]);

        try (final ChunkStorage loaded = cm.loadValidated())
        {
            Assert.assertNotNull(loaded);
            final com.xceptance.xlt.report.providers.DummyReportGeneratorConfiguration dummyConfig =
                new com.xceptance.xlt.report.providers.DummyReportGeneratorConfiguration();
            dummyConfig.setChartStartTime(baseTime);
            dummyConfig.setChartEndTime(baseTime + 1000L);

            final com.xceptance.xlt.report.providers.RequestsReportProvider reqProvider =
                new com.xceptance.xlt.report.providers.RequestsReportProvider();
            reqProvider.setConfiguration(dummyConfig);

            final com.xceptance.xlt.report.StatisticsProcessor statsProcessor =
                new com.xceptance.xlt.report.StatisticsProcessor(List.of(reqProvider), dummyConfig);
            final com.xceptance.xlt.report.ChunkQueryEngine engine =
                new com.xceptance.xlt.report.ChunkQueryEngine(loaded, 2);

            try (final me.tongfei.progressbar.ProgressBar pb =
                     new me.tongfei.progressbar.ProgressBarBuilder()
                         .setTaskName("Reading")
                         .setStyle(me.tongfei.progressbar.ProgressBarStyle.ASCII)
                         .build())
            {
                final long count = engine.executeScan(ScanPredicate.ALL, statsProcessor, pb);
                Assert.assertEquals(50L, count);
                final long expectedSteps = loaded.getCatalog().getChunkCount() + statsProcessor.getReportProviderCount();
                Assert.assertEquals(expectedSteps, pb.getMax());
                Assert.assertEquals(expectedSteps, pb.getCurrent());
            }

            // Also verify empty candidate chunk scan updates progressBar to 1/1
            final com.xceptance.xlt.report.StatisticsProcessor emptyStatsProcessor =
                new com.xceptance.xlt.report.StatisticsProcessor(List.of(reqProvider), dummyConfig);
            final com.xceptance.xlt.report.ChunkQueryEngine emptyEngine =
                new com.xceptance.xlt.report.ChunkQueryEngine(loaded, 2);
            try (final me.tongfei.progressbar.ProgressBar pbEmpty =
                     new me.tongfei.progressbar.ProgressBarBuilder()
                         .setTaskName("Reading")
                         .setStyle(me.tongfei.progressbar.ProgressBarStyle.ASCII)
                         .build())
            {
                final ScanPredicate noMatch = new ScanPredicate(baseTime + 100_000L, baseTime + 200_000L, null, null);
                final long count = emptyEngine.executeScan(noMatch, emptyStatsProcessor, pbEmpty);
                Assert.assertEquals(0L, count);
                Assert.assertEquals(1L, pbEmpty.getMax());
                Assert.assertEquals(1L, pbEmpty.getCurrent());
            }
        }
    }

    /**
     * Custom record type representing an unhandled / novel record format
     * to verify generic chunking and chunk-local string dictionary isolation.
     */
    public static class CustomLogRecord extends com.xceptance.xlt.api.engine.AbstractData
    {
        private String extra1;
        private String extra2;

        public CustomLogRecord()
        {
            super('Z');
        }

        public CustomLogRecord(final String name, final String extra1, final String extra2)
        {
            super(name, 'Z');
            this.extra1 = extra1;
            this.extra2 = extra2;
        }

        @Override
        public List<String> toList()
        {
            final List<String> list = super.toList();
            list.add(extra1 != null ? extra1 : "");
            list.add(extra2 != null ? extra2 : "");
            return list;
        }

        @Override
        public void setRemainingValues(final CsvByteColumns values)
        {
            if (values.size() >= 4)
            {
                extra1 = values.getString(3);
            }
            if (values.size() >= 5)
            {
                extra2 = values.getString(4);
            }
        }

        public String getExtra1()
        {
            return extra1;
        }

        public String getExtra2()
        {
            return extra2;
        }
    }

    /**
     * Verifies {@link GlobalDictionaries} read-only compaction ({@link GlobalDictionaries#compactForReadOnly()}),
     * primitive sample URL storage, lazy reverse-map reconstruction on post-compaction writes,
     * {@link GlobalDictionaries#charSequenceEquals(CharSequence, String)}, IP address splitting,
     * and binary serialization/deserialization edge cases.
     */
    @Test
    public void testGlobalDictionariesCompactionAndEdgeCases() throws Exception
    {
        // 1. charSequenceEquals edge cases
        Assert.assertFalse(GlobalDictionaries.charSequenceEquals(null, null));
        Assert.assertFalse(GlobalDictionaries.charSequenceEquals(null, "abc"));
        Assert.assertFalse(GlobalDictionaries.charSequenceEquals("abc", null));
        final String literal = "sameRef";
        Assert.assertTrue(GlobalDictionaries.charSequenceEquals(literal, literal));
        Assert.assertTrue(GlobalDictionaries.charSequenceEquals(new String("abc"), "abc"));
        Assert.assertFalse(GlobalDictionaries.charSequenceEquals(new String("abc"), "abd"));
        Assert.assertFalse(GlobalDictionaries.charSequenceEquals(new StringBuilder("abc"), "ab"));
        Assert.assertFalse(GlobalDictionaries.charSequenceEquals(new StringBuilder("abc"), "abd"));
        Assert.assertTrue(GlobalDictionaries.charSequenceEquals(new StringBuilder("abc"), "abc"));

        // 2. CachedSampleUrl host resolution with valid host and empty host fallback
        final GlobalDictionaries.CachedSampleUrl withHost = new GlobalDictionaries.CachedSampleUrl("https://example.com/path#frag");
        Assert.assertEquals("example.com", withHost.host());
        final GlobalDictionaries.CachedSampleUrl emptyHost = new GlobalDictionaries.CachedSampleUrl("/relative/only");
        Assert.assertEquals(RequestData.UNKNOWN_HOST, emptyHost.host());

        // 3. Fresh dictionary state and null/cache hits
        final GlobalDictionaries dict = new GlobalDictionaries();
        Assert.assertFalse("Fresh dictionary must not report compacted", dict.isCompacted());
        Assert.assertEquals(-1, dict.getOrCreateTimerNameId(null));
        Assert.assertEquals(-1, dict.getOrCreateStringId(null));

        // Exercise getOrCreateAgentTestCaseId(null, null) when cache.lastId == -1
        final int nullPairId = dict.getOrCreateAgentTestCaseId(null, null);
        Assert.assertEquals(0, nullPairId);
        Assert.assertEquals("", dict.getAgentName(nullPairId));
        Assert.assertEquals("", dict.getTestCaseName(nullPairId));
        // Partial reference hit/miss on ThreadLocalAgentTestCaseCache
        final String ag1 = "Agent1";
        final String tc1 = "TC1";
        final int pair1 = dict.getOrCreateAgentTestCaseId(ag1, tc1);
        Assert.assertEquals(1, pair1);
        Assert.assertEquals(pair1, dict.getOrCreateAgentTestCaseId(ag1, tc1));
        final int pair2 = dict.getOrCreateAgentTestCaseId(ag1, "TC2");
        Assert.assertEquals(2, pair2);
        // Immediate CharSequence match on pair2 (lastId == 2 != 0) before map lookup
        Assert.assertEquals(pair2, dict.getOrCreateAgentTestCaseId(new StringBuilder("Agent1"), new StringBuilder("TC2")));
        // Optimistic map.get(pair) hit on pair1
        Assert.assertEquals(pair1, dict.getOrCreateAgentTestCaseId(new StringBuilder("Agent1"), new StringBuilder("TC1")));
        final int pair3 = dict.getOrCreateAgentTestCaseId("Agent2", "TC2");
        Assert.assertNotEquals(pair2, pair3);
        Assert.assertEquals(pair2, dict.getOrCreateAgentTestCaseId(new StringBuilder("Agent1"), new StringBuilder("TC2")));

        // Timer name thread-local reference and CharSequence cache hits (verify on both ID 0 and ID 1 != 0)
        final String timerRef = "TimerA";
        final int tA = dict.getOrCreateTimerNameId(timerRef);
        Assert.assertEquals(0, tA);
        Assert.assertEquals(tA, dict.getOrCreateTimerNameId(timerRef));
        Assert.assertEquals(tA, dict.getOrCreateTimerNameId(new StringBuilder("TimerA")));
        final String timerRefB = "TimerB";
        final int tB = dict.getOrCreateTimerNameId(timerRefB);
        Assert.assertEquals(1, tB);
        Assert.assertEquals(tB, dict.getOrCreateTimerNameId(timerRefB));
        Assert.assertEquals(tB, dict.getOrCreateTimerNameId(new StringBuilder("TimerB")));
        Assert.assertEquals(tA, dict.getOrCreateTimerNameId("TimerA"));
        Assert.assertEquals(tB, dict.getOrCreateTimerNameId(new String("TimerB")));
        // Verify null input when thread-local cache is already populated (lastRef != null, lastId != -1)
        Assert.assertEquals(-1, dict.getOrCreateTimerNameId(null));

        // String thread-local reference and CharSequence cache hits (15 pre-seeded HTTP strings precede custom strings)
        final String strRef = "CustomHeader";
        final int sA = dict.getOrCreateStringId(strRef);
        Assert.assertEquals(15, sA);
        Assert.assertEquals(sA, dict.getOrCreateStringId(strRef));
        Assert.assertEquals(sA, dict.getOrCreateStringId(new StringBuilder("CustomHeader")));
        Assert.assertEquals(-1, dict.getOrCreateStringId(null));

        // Out-of-bounds and zero-boundary lookups
        Assert.assertNull(dict.getTimerName(-1));
        Assert.assertNull(dict.getTimerName(9999));
        Assert.assertEquals("TimerA", dict.getTimerName(tA));

        Assert.assertNull(dict.getAgentTestCase(-1));
        Assert.assertNull(dict.getAgentTestCase(9999));
        Assert.assertNull(dict.getAgentName(-1));
        Assert.assertNull(dict.getAgentName(9999));
        Assert.assertNull(dict.getTestCaseName(-1));
        Assert.assertNull(dict.getTestCaseName(9999));

        Assert.assertNull(dict.getString(-1));
        Assert.assertNull(dict.getString(9999));
        Assert.assertEquals("GET", dict.getString(0));

        // 4. Filter bitmaps
        Assert.assertNull(dict.filterAgentTestCaseIds(null, null));
        final StringMatcher matchAll = new StringMatcher(".*", null, true);
        Assert.assertNull(dict.filterAgentTestCaseIds(matchAll, matchAll));
        final StringMatcher onlyTc1 = new StringMatcher("TC1", null, true);
        final RoaringBitmap tc1Bitmap = dict.filterAgentTestCaseIds(null, onlyTc1);
        Assert.assertNotNull(tc1Bitmap);
        Assert.assertTrue(tc1Bitmap.contains(pair1));
        Assert.assertFalse(tc1Bitmap.contains(pair2));
        final StringMatcher onlyAg1 = new StringMatcher("Agent1", null, true);
        final RoaringBitmap ag1Tc1Bitmap = dict.filterAgentTestCaseIds(onlyAg1, onlyTc1);
        Assert.assertNotNull(ag1Tc1Bitmap);
        Assert.assertEquals(1, ag1Tc1Bitmap.getCardinality());
        Assert.assertTrue(ag1Tc1Bitmap.contains(pair1));

        // 5. Split IP addresses (compute ID 16 first, then ID 0, then verify ID 16 was not truncated)
        Assert.assertNull(dict.getSplitIpAddresses(-1));
        Assert.assertNull(dict.getSplitIpAddresses(9999));
        final String ipRef = "10.0.0.1|10.0.0.2";
        final int ipId = dict.getOrCreateStringId(ipRef);
        Assert.assertEquals(16, ipId);
        Assert.assertEquals(ipId, dict.getOrCreateStringId(ipRef));
        Assert.assertEquals(ipId, dict.getOrCreateStringId(new StringBuilder("10.0.0.1|10.0.0.2")));
        final String[] split1 = dict.getSplitIpAddresses(ipId);
        Assert.assertArrayEquals(new String[] { "10.0.0.1", "10.0.0.2" }, split1);
        final String[] split0 = dict.getSplitIpAddresses(0);
        Assert.assertArrayEquals(new String[] { "GET" }, split0);
        Assert.assertSame("Second lookup at index 0 must return cached split array", split0, dict.getSplitIpAddresses(0));
        Assert.assertSame("Second lookup must return cached split array", split1, dict.getSplitIpAddresses(ipId));

        final java.lang.reflect.Field cachedSampleUrlsField = GlobalDictionaries.class.getDeclaredField("cachedSampleUrls");
        cachedSampleUrlsField.setAccessible(true);
        final java.lang.reflect.Field sampleUrlCountsField = GlobalDictionaries.class.getDeclaredField("sampleUrlCounts");
        sampleUrlCountsField.setAccessible(true);
        final java.lang.reflect.Field splitIpField = GlobalDictionaries.class.getDeclaredField("splitIpAddresses");
        splitIpField.setAccessible(true);

        // 6. Sample URLs: quota, deduplication, pre-compaction caching, and high timerId expansion
        Assert.assertTrue(dict.hasSufficientSamples(-1));
        Assert.assertFalse(dict.hasSufficientSamples(9999));
        dict.addSampleUrl(-1, "https://example.com/ignored");
        dict.addSampleUrl(tA, null);
        dict.addSampleUrl(tA, "");
        Assert.assertNull(dict.getCachedSampleUrls(-1));
        Assert.assertNull(dict.getCachedSampleUrls(9999));
        Assert.assertNull(dict.getCachedSampleUrls(tA));

        for (int i = 0; i < 12; i++)
        {
            dict.addSampleUrl(tB, "https://example.com/b/" + i);
            dict.addSampleUrl(tB, "https://example.com/b/" + i); // duplicate ignored
            if (i < 9)
            {
                Assert.assertFalse(dict.hasSufficientSamples(tB));
            }
            else
            {
                Assert.assertTrue(dict.hasSufficientSamples(tB));
            }
        }
        // Exercise getCachedSampleUrls when rawSampleUrls != null and counts[timerId] == 0 or timerId == counts.length
        Assert.assertNull(dict.getCachedSampleUrls(tA));
        Assert.assertNull(dict.getCachedSampleUrls(((byte[]) sampleUrlCountsField.get(dict)).length));

        // Add sample URL for timerId == 2 (equals timerNameCount == 2 for readFrom boundary) and cache it first
        dict.addSampleUrl(2, "https://example.com/t2/1");
        final GlobalDictionaries.CachedSampleUrl[] cachedT2 = dict.getCachedSampleUrls(2);
        Assert.assertEquals(1, cachedT2.length);

        // Compute cached sample URLs for tB (tB == 1 < cachedSampleUrls.length == 3) BEFORE compaction
        // and verify cachedSampleUrls[2] was not truncated by computeCachedSampleUrls(tB)
        final GlobalDictionaries.CachedSampleUrl[] cachedB = dict.getCachedSampleUrls(tB);
        Assert.assertNotNull(cachedB);
        Assert.assertEquals(10, cachedB.length);
        Assert.assertSame(cachedB, dict.getCachedSampleUrls(tB));
        Assert.assertEquals(256, ((GlobalDictionaries.CachedSampleUrl[][]) cachedSampleUrlsField.get(dict)).length);
        Assert.assertSame(cachedT2, dict.getCachedSampleUrls(2));

        // Add sample URL for high timerId >= INITIAL_CAPACITY (256), compute cached, then add a second URL to invalidate cache
        dict.addSampleUrl(300, "https://example.com/high/1");
        Assert.assertEquals(1, dict.getCachedSampleUrls(300).length);
        dict.addSampleUrl(300, "https://example.com/high/2");
        // Add sample URL for timerId == 301 (equals cachedSampleUrls.length == 301 for writeTo boundary)
        dict.addSampleUrl(301, "https://example.com/high/301");
        // Also add a sample URL for timerId 302 (> cachedSampleUrls.length = 301) without calling getCachedSampleUrls(302)
        dict.addSampleUrl(302, "https://example.com/high/302");

        // Write before compaction (tB uses cachedSampleUrls[tB], timer 2/300/301/302 use rawSampleUrls)
        final ByteArrayOutputStream preCompactBaos = new ByteArrayOutputStream();
        try (final DataOutputStream dos = new DataOutputStream(preCompactBaos))
        {
            dict.writeTo(dos);
        }
        try (final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(preCompactBaos.toByteArray())))
        {
            final GlobalDictionaries fromPreCompact = GlobalDictionaries.readFrom(dis);
            Assert.assertTrue("Deserialized dictionary must start in compacted state", fromPreCompact.isCompacted());
            Assert.assertEquals(10, fromPreCompact.getCachedSampleUrls(tB).length);
            Assert.assertEquals(1, fromPreCompact.getCachedSampleUrls(2).length);
            Assert.assertEquals(2, fromPreCompact.getCachedSampleUrls(300).length);
            Assert.assertEquals(1, fromPreCompact.getCachedSampleUrls(301).length);
            Assert.assertEquals(1, fromPreCompact.getCachedSampleUrls(302).length);
        }

        // Also verify readFrom and compactForReadOnly do not shrink cachedSampleUrls below timerCount when only timer 0 has sample URLs
        final GlobalDictionaries twoTimerDict = new GlobalDictionaries();
        twoTimerDict.getOrCreateTimerNameId("T0");
        twoTimerDict.getOrCreateTimerNameId("T1");
        twoTimerDict.addSampleUrl(0, "https://example.com/t0");
        final ByteArrayOutputStream twoTimerBaos = new ByteArrayOutputStream();
        try (final DataOutputStream dos = new DataOutputStream(twoTimerBaos))
        {
            twoTimerDict.writeTo(dos);
        }
        try (final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(twoTimerBaos.toByteArray())))
        {
            final GlobalDictionaries restoredTwoTimer = GlobalDictionaries.readFrom(dis);
            Assert.assertEquals(2, ((GlobalDictionaries.CachedSampleUrl[][]) cachedSampleUrlsField.get(restoredTwoTimer)).length);
            Assert.assertEquals(2, ((byte[]) sampleUrlCountsField.get(restoredTwoTimer)).length);
        }
        twoTimerDict.compactForReadOnly();
        Assert.assertEquals(2, ((GlobalDictionaries.CachedSampleUrl[][]) cachedSampleUrlsField.get(twoTimerDict)).length);
        Assert.assertEquals(2, ((byte[]) sampleUrlCountsField.get(twoTimerDict)).length);
        Assert.assertEquals(1, twoTimerDict.getCachedSampleUrls(0).length);

        // 7. Compact for read-only and verify array trimming and reverse-map release
        dict.compactForReadOnly();
        Assert.assertTrue(dict.isCompacted());
        final String[] compactedTimers = dict.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] compactedPairs = dict.getAgentTestCasesArray();
        final String[] compactedStrings = dict.getStringsArray();
        final String[][] compactedSplitIps = (String[][]) splitIpField.get(dict);
        final GlobalDictionaries.CachedSampleUrl[][] compactedCachedUrls =
            (GlobalDictionaries.CachedSampleUrl[][]) cachedSampleUrlsField.get(dict);
        final byte[] compactedUrlCounts = (byte[]) sampleUrlCountsField.get(dict);

        Assert.assertEquals(dict.getTimerNameCount(), compactedTimers.length);
        Assert.assertEquals(dict.getAgentTestCaseCount(), compactedPairs.length);
        Assert.assertEquals(dict.getStringCount(), compactedStrings.length);
        Assert.assertEquals(dict.getStringCount(), compactedSplitIps.length);
        Assert.assertEquals(303, compactedCachedUrls.length);
        Assert.assertEquals(303, compactedUrlCounts.length);
        Assert.assertSame("Pre-computed CachedSampleUrl[] must be preserved on compaction", cachedB, dict.getCachedSampleUrls(tB));
        Assert.assertEquals(2, dict.getCachedSampleUrls(300).length);
        Assert.assertEquals(1, dict.getCachedSampleUrls(302).length);
        Assert.assertNull(dict.getCachedSampleUrls(tA));

        // Exact upper-boundary lookups (id == count) after arrays are trimmed to exact length
        Assert.assertNull(dict.getTimerName(dict.getTimerNameCount()));
        Assert.assertNull(dict.getAgentTestCase(dict.getAgentTestCaseCount()));
        Assert.assertNull(dict.getString(dict.getStringCount()));
        Assert.assertNull(dict.getSplitIpAddresses(dict.getStringCount()));
        Assert.assertNull(dict.getCachedSampleUrls(303));

        // Calling compactForReadOnly() again when already compacted is a no-op that preserves trimmed arrays
        dict.compactForReadOnly();
        Assert.assertTrue(dict.isCompacted());
        Assert.assertSame(compactedTimers, dict.getTimerNamesArray());
        Assert.assertSame(compactedPairs, dict.getAgentTestCasesArray());
        Assert.assertSame(compactedStrings, dict.getStringsArray());
        Assert.assertSame(compactedSplitIps, splitIpField.get(dict));
        Assert.assertSame(compactedCachedUrls, cachedSampleUrlsField.get(dict));
        Assert.assertSame(compactedUrlCounts, sampleUrlCountsField.get(dict));

        // 8. Lazy reconstruction of each reverse map and sample URL staging buffer after compaction
        Assert.assertEquals(tA, dict.getOrCreateTimerNameId("TimerA"));
        Assert.assertFalse("Rebuilding timerNameToId makes isCompacted false", dict.isCompacted());
        dict.compactForReadOnly();
        Assert.assertTrue(dict.isCompacted());

        Assert.assertEquals(pair1, dict.getOrCreateAgentTestCaseId("Agent1", "TC1"));
        Assert.assertFalse("Rebuilding pairToId makes isCompacted false", dict.isCompacted());
        dict.compactForReadOnly();
        Assert.assertTrue(dict.isCompacted());

        Assert.assertEquals(ipId, dict.getOrCreateStringId(new String("10.0.0.1|10.0.0.2")));
        Assert.assertFalse("Rebuilding stringToId makes isCompacted false", dict.isCompacted());
        final int newIpId = dict.getOrCreateStringId("192.168.1.1|192.168.1.2");
        // Compute split IP on newly interned ID beyond compacted splitIpAddresses.length
        Assert.assertArrayEquals(new String[] { "192.168.1.1", "192.168.1.2" }, dict.getSplitIpAddresses(newIpId));
        dict.compactForReadOnly();
        Assert.assertTrue(dict.isCompacted());

        // Add sample URL after compaction (rebuilds rawSampleUrls from cachedSampleUrls)
        dict.addSampleUrl(300, "https://example.com/high/3");
        Assert.assertFalse("Rebuilding rawSampleUrls makes isCompacted false", dict.isCompacted());
        // Add sample URL at exact boundary timerId == cachedSampleUrls.length (303)
        dict.addSampleUrl(303, "https://example.com/high/303");
        Assert.assertEquals(1, dict.getCachedSampleUrls(303).length);
        dict.addSampleUrl(305, "https://example.com/high/305");
        Assert.assertEquals(3, dict.getCachedSampleUrls(300).length);
        Assert.assertEquals(1, dict.getCachedSampleUrls(305).length);

        // 9. Geometric expansion beyond INITIAL_CAPACITY (256) for timerNames and agentTestCases
        final GlobalDictionaries largeDict = new GlobalDictionaries();
        for (int i = 0; i < 300; i++)
        {
            Assert.assertEquals(i, largeDict.getOrCreateTimerNameId("T-" + i));
            Assert.assertEquals(i, largeDict.getOrCreateAgentTestCaseId("Ag-" + i, "Tc-" + i));
        }
        Assert.assertEquals(300, largeDict.getTimerNameCount());
        Assert.assertEquals(300, largeDict.getAgentTestCaseCount());

        // 10. Deserialization error paths (invalid magic, invalid version) and legacy EOF without sample URLs
        final ByteArrayOutputStream badMagic = new ByteArrayOutputStream();
        try (final DataOutputStream dos = new DataOutputStream(badMagic))
        {
            dos.writeInt(0x12345678);
            dos.writeInt(GlobalDictionaries.VERSION);
        }
        try (final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(badMagic.toByteArray())))
        {
            GlobalDictionaries.readFrom(dis);
            Assert.fail("Expected IOException for invalid magic");
        }
        catch (final java.io.IOException expected)
        {
            Assert.assertTrue(expected.getMessage().contains("Invalid dictionaries file format"));
        }

        final ByteArrayOutputStream badVersion = new ByteArrayOutputStream();
        try (final DataOutputStream dos = new DataOutputStream(badVersion))
        {
            dos.writeInt(GlobalDictionaries.MAGIC);
            dos.writeInt(999);
        }
        try (final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(badVersion.toByteArray())))
        {
            GlobalDictionaries.readFrom(dis);
            Assert.fail("Expected IOException for unsupported version");
        }
        catch (final java.io.IOException expected)
        {
            Assert.assertTrue(expected.getMessage().contains("Unsupported dictionaries file version"));
        }

        // Legacy stream lacking sample URLs section (triggers EOFException catch block) with 0 entries
        final ByteArrayOutputStream legacyEmpty = new ByteArrayOutputStream();
        try (final DataOutputStream dos = new DataOutputStream(legacyEmpty))
        {
            dos.writeInt(GlobalDictionaries.MAGIC);
            dos.writeInt(GlobalDictionaries.VERSION);
            dos.writeInt(0); // timers
            dos.writeInt(0); // pairs
            dos.writeInt(0); // strings
            // omit sample URLs count to trigger EOFException
        }
        try (final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(legacyEmpty.toByteArray())))
        {
            final GlobalDictionaries emptyRestored = GlobalDictionaries.readFrom(dis);
            Assert.assertTrue(emptyRestored.isCompacted());
            Assert.assertEquals(0, emptyRestored.getTimerNameCount());
            Assert.assertEquals(0, emptyRestored.getAgentTestCaseCount());
            Assert.assertEquals(0, emptyRestored.getStringCount());
            // Expand from 0-length arrays after deserialization
            Assert.assertEquals(0, emptyRestored.getOrCreateTimerNameId("FirstTimer"));
            Assert.assertEquals(0, emptyRestored.getOrCreateAgentTestCaseId("FirstAgent", "FirstTC"));
            Assert.assertEquals(0, emptyRestored.getOrCreateStringId("1.1.1.1|2.2.2.2"));
            final String[] firstSplit = emptyRestored.getSplitIpAddresses(0);
            Assert.assertArrayEquals(new String[] { "1.1.1.1", "2.2.2.2" }, firstSplit);
            Assert.assertSame("Cached split IP at index 0 must be returned on second call",
                              firstSplit, emptyRestored.getSplitIpAddresses(0));
        }
    }

    /**
     * Verifies {@link ChunkIngestionCollector} batch container ingestion, builder rotation and recycling when
     * chunks fill up, and I/O fallback error handling.
     */
    @Test
    public void testChunkIngestionCollectorLifecycleAndEdgeCases() throws Exception
    {
        // 1. Register a small-capacity test handler (full after 2 rows) for 'Q' and 'X' type codes
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkCatalog catalog = new ChunkCatalog();
        final ChunkTypeRegistry registry = new ChunkTypeRegistry();

        final java.util.concurrent.atomic.AtomicInteger qWriteCount = new java.util.concurrent.atomic.AtomicInteger();
        final ChunkTypeHandler smallHandlerAscii = createSmallCapacityHandler('Q', 2, qWriteCount);
        final ChunkTypeHandler smallHandlerSecond = createSmallCapacityHandler('X', 2, new java.util.concurrent.atomic.AtomicInteger());
        registry.registerHandler(smallHandlerAscii);
        registry.registerHandler(smallHandlerSecond);

        final ChunkIngestionCollector collector = new ChunkIngestionCollector(dicts, catalog, registry);
        Assert.assertSame(dicts, collector.getDictionaries());
        Assert.assertSame(catalog, collector.getCatalog());
        Assert.assertSame(registry, collector.getRegistry());

        final java.lang.reflect.Field tlField = ChunkIngestionCollector.class.getDeclaredField("threadLocalBuilders");
        tlField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final ThreadLocal<ChunkBuilder[]> tlBuilders = (ThreadLocal<ChunkBuilder[]>) tlField.get(collector);
        final java.lang.reflect.Field allBuildersField = ChunkIngestionCollector.class.getDeclaredField("allThreadBuilders");
        allBuildersField.setAccessible(true);
        final java.util.Collection<?> allBuilders = (java.util.Collection<?>) allBuildersField.get(collector);

        // 2. Null and empty input guards across all collect() overloads
        collector.collect((Data) null);
        collector.collect((List<Data>) null);
        collector.collect((com.xceptance.xlt.api.report.PostProcessedDataContainer) null);
        final com.xceptance.xlt.api.report.PostProcessedDataContainer emptyHom =
            new com.xceptance.xlt.api.report.PostProcessedDataContainer(4, 1);
        emptyHom.typeCode = 'Q';
        collector.collect(emptyHom);
        Assert.assertTrue("Empty container must not initialize threadLocalBuilders", allBuilders.isEmpty());
        collector.collect(java.util.Collections.emptyList());
        Assert.assertNull("Empty container must not allocate a builder", tlBuilders.get()['Q']);

        // 3. Homogeneous PostProcessedDataContainer sized exactly to 4 elements (size == internalArr.length)
        // Include a record whose own getTypeCode() is 'X' while container.typeCode is 'Q' to verify homogeneous routing
        final com.xceptance.xlt.api.report.PostProcessedDataContainer homContainer =
            new com.xceptance.xlt.api.report.PostProcessedDataContainer(4, 1);
        homContainer.typeCode = 'Q';
        homContainer.dataList.add(createDummyRecord('Q', 1000L));
        homContainer.dataList.add(null);
        homContainer.dataList.add(createDummyRecord('Q', 1001L)); // triggers isFull() -> sealAndSpool + recycleBuilder
        homContainer.dataList.add(createDummyRecord('X', 1002L)); // routed to 'Q' builder via container.typeCode fast-path
        collector.collect(homContainer);
        Assert.assertEquals(1, tlBuilders.get()['Q'].getRowCount());
        Assert.assertNull(tlBuilders.get()['X']);

        // 4. Heterogeneous PostProcessedDataContainer (typeCode = 0) sized exactly to 4 elements
        final com.xceptance.xlt.api.report.PostProcessedDataContainer hetContainer =
            new com.xceptance.xlt.api.report.PostProcessedDataContainer(4, 1);
        hetContainer.typeCode = 0;
        hetContainer.dataList.add(null);
        hetContainer.dataList.add(createDummyRecord('Q', 1003L)); // triggers isFull() for 'Q' inside collectRecord
        hetContainer.dataList.add(createDummyRecord('X', 1004L));
        hetContainer.dataList.add(createDummyRecord('X', 1005L)); // triggers isFull() for 'X'
        collector.collect(hetContainer);

        // Leave 1 uncommitted row in builders['X'] before collecting secondHomContainer (verifies builder != null reuse)
        collector.collect(createDummyRecord('X', 1006L));
        final com.xceptance.xlt.api.report.PostProcessedDataContainer secondHomContainer =
            new com.xceptance.xlt.api.report.PostProcessedDataContainer(1, 1);
        secondHomContainer.typeCode = 'X';
        secondHomContainer.dataList.add(createDummyRecord('X', 1007L)); // triggers isFull() for 'X' (2 rows: 1006L, 1007L)
        collector.collect(secondHomContainer);
        Assert.assertNull("Full homogeneous builder slot must be cleared", tlBuilders.get()['X']);

        // 5. List<Data> with null elements via collect(List<Data>)
        final List<Data> arrayList = new ArrayList<>();
        arrayList.add(null);
        arrayList.add(createDummyRecord('Q', 1008L));
        collector.collect(arrayList);

        final java.lang.reflect.Field permitsField = ChunkIngestionCollector.class.getDeclaredField("compressionPermits");
        permitsField.setAccessible(true);
        final java.util.concurrent.Semaphore permits = (java.util.concurrent.Semaphore) permitsField.get(collector);
        final java.lang.reflect.Field tasksField = ChunkIngestionCollector.class.getDeclaredField("pendingCompressionTasks");
        tasksField.setAccessible(true);
        final java.util.Collection<?> pendingTasks = (java.util.Collection<?>) tasksField.get(collector);
        final java.lang.reflect.Field execField = ChunkIngestionCollector.class.getDeclaredField("compressionExecutor");
        execField.setAccessible(true);
        final java.util.concurrent.ExecutorService exec = (java.util.concurrent.ExecutorService) execField.get(collector);

        // Verify ThreadFactory explicitly sets daemon=true even when invoked from a non-daemon parent thread
        final java.util.concurrent.atomic.AtomicBoolean workerIsDaemon = new java.util.concurrent.atomic.AtomicBoolean(false);
        final Thread nonDaemonParent = new Thread(() -> {
            final Thread worker = ((java.util.concurrent.ThreadPoolExecutor) exec).getThreadFactory().newThread(() -> {});
            workerIsDaemon.set(worker.isDaemon());
        });
        nonDaemonParent.setDaemon(false);
        nonDaemonParent.start();
        nonDaemonParent.join();
        Assert.assertTrue("ThreadFactory must explicitly mark worker threads as daemon", workerIsDaemon.get());

        try (final ChunkStorage storage = collector.finish())
        {
            Assert.assertNotNull(storage);
            Assert.assertTrue("Dictionaries must be compacted on finish()", dicts.isCompacted());
            Assert.assertEquals(9L, storage.getTotalRowCount());
            Assert.assertEquals(5, catalog.getAllChunks().size());
            for (final Chunk c : catalog.getAllChunks())
            {
                Assert.assertTrue(c.getRowCount() > 0);
            }
            Assert.assertEquals(64, permits.availablePermits());
            Assert.assertTrue(pendingTasks.isEmpty());
            Assert.assertTrue(exec.isShutdown());
            Assert.assertTrue(allBuilders.isEmpty());
        }

        // Calling finish() a second time when threadLocalBuilders is already null
        try (final ChunkStorage storageAgain = collector.finish())
        {
            Assert.assertNotNull(storageAgain);
        }

        // 6. Test spooled ChunkIngestionCollector with builder rotation during collect()
        final File spoolDir = new File(tempDir, "small-spool");
        final ChunkCatalog spoolCatalog = new ChunkCatalog();
        final ChunkTypeRegistry spoolRegistry = new ChunkTypeRegistry();
        spoolRegistry.registerHandler(createSmallCapacityHandler('E', 2, qWriteCount));
        final ChunkIngestionCollector spoolCollector =
            new ChunkIngestionCollector(new GlobalDictionaries(), spoolCatalog, spoolRegistry, spoolDir);
        qWriteCount.set(0);
        spoolCollector.collect(createDummyRecord('E', 2000L));
        spoolCollector.collect(createDummyRecord('E', 2001L)); // triggers background spooling in sealAndSpool
        spoolCollector.collect(createDummyRecord('E', 2002L)); // partial builder sealed & spooled in finish()
        try (final ChunkStorage spooledStorage = spoolCollector.finish())
        {
            Assert.assertNotNull(spooledStorage);
            Assert.assertSame(spoolCatalog, spooledStorage.getCatalog());
            Assert.assertEquals(3L, spooledStorage.getTotalRowCount());
            Assert.assertTrue(spooledStorage.getDictionaries().isCompacted());
            Assert.assertEquals(2, spoolCatalog.getAllChunks().size());
            Assert.assertEquals("Both chunks must be spooled via collector's handler", 2, qWriteCount.get());
            for (final Chunk c : spoolCatalog.getAllChunks())
            {
                Assert.assertTrue("Expected DiskChunk when spooler is active",
                                  c instanceof com.xceptance.xlt.report.storage.chunk.DiskChunk);
            }
        }

        // 7. Test ChunkSpooler init failure fallback (when cacheDir is a regular file blocking mkdirs/tmpFile creation)
        final File blockingFile = new File(tempDir, "not-a-dir");
        Files.writeString(blockingFile.toPath(), "block", StandardCharsets.UTF_8);
        final ChunkIngestionCollector fallbackCollector = new ChunkIngestionCollector(blockingFile);
        final EventData fallbackEvent = new EventData("Ev");
        fallbackEvent.setTime(3000L);
        fallbackEvent.setAgentName("Ag");
        fallbackEvent.setTransactionName("Tc");
        fallbackEvent.setMessage("Msg");
        fallbackCollector.collect(fallbackEvent);
        try (final ChunkStorage fallbackStorage = fallbackCollector.finish())
        {
            Assert.assertEquals(1L, fallbackStorage.getTotalRowCount());
        }

        // 8. Test recycleBuilder pool cap at 32, builder.reset() on poll, and 0-row builder skipping in finish()
        final ChunkCatalog poolCatalog = new ChunkCatalog();
        final ChunkIngestionCollector poolCollector = new ChunkIngestionCollector(new GlobalDictionaries(), poolCatalog, registry);
        @SuppressWarnings("unchecked")
        final ThreadLocal<ChunkBuilder[]> poolTlBuilders = (ThreadLocal<ChunkBuilder[]>) tlField.get(poolCollector);
        final java.lang.reflect.Field recycledField = ChunkIngestionCollector.class.getDeclaredField("recycledBuilders");
        recycledField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final java.util.concurrent.ConcurrentLinkedQueue<ChunkBuilder>[] pools =
            (java.util.concurrent.ConcurrentLinkedQueue<ChunkBuilder>[]) recycledField.get(poolCollector);
        final java.util.concurrent.ConcurrentLinkedQueue<ChunkBuilder> qPool = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pools['Q'] = qPool;
        // First collect when qPool is non-null but empty -> acquireBuilder polls null and creates a new builder
        poolCollector.collect(createDummyRecord('Q', 4000L));
        // Offer a dirty builder (with 1 pre-existing row that reset() must clear) + 30 more builders (31 total in qPool)
        final ChunkBuilder dirtyBuilder = smallHandlerAscii.newChunkBuilder(dicts);
        dirtyBuilder.append(createDummyRecord('Q', 3999L));
        Assert.assertEquals(1, dirtyBuilder.getRowCount());
        qPool.offer(dirtyBuilder);
        for (int i = 0; i < 30; i++)
        {
            qPool.offer(smallHandlerAscii.newChunkBuilder(dicts));
        }
        Assert.assertEquals(31, qPool.size());
        // Collect 2nd record -> fills active builder -> background sealAndSpool recycles fullBuilder into qPool (31 < 32 -> size becomes 32)
        poolCollector.collect(createDummyRecord('Q', 4001L));
        Assert.assertNull("Full builder slot must be cleared on sealAndSpool", poolTlBuilders.get()['Q']);
        @SuppressWarnings("unchecked")
        final java.util.Collection<java.util.concurrent.Future<?>> poolTasks =
            (java.util.Collection<java.util.concurrent.Future<?>>) tasksField.get(poolCollector);
        for (final java.util.concurrent.Future<?> f : poolTasks)
        {
            f.get();
        }
        Assert.assertEquals("Recycled builder must be offered when pool size is 31 (< 32)", 32, qPool.size());

        // Also test recycleBuilder when pool size is already 32 (must not exceed 32)
        final java.lang.reflect.Method recycleMethod =
            ChunkIngestionCollector.class.getDeclaredMethod("recycleBuilder", ChunkBuilder.class, char.class);
        recycleMethod.setAccessible(true);
        recycleMethod.invoke(poolCollector, smallHandlerAscii.newChunkBuilder(dicts), 'Q');
        Assert.assertEquals("Pool size must be capped at 32", 32, qPool.size());

        // Collect 3rd record -> acquires dirtyBuilder from head of qPool (size 32 -> 31), resets it to 0 rows, and appends 4002L
        poolCollector.collect(createDummyRecord('Q', 4002L));
        Assert.assertEquals(31, qPool.size());
        Assert.assertSame("acquireBuilder must poll and return recycled builder from pool", dirtyBuilder, poolTlBuilders.get()['Q']);
        Assert.assertEquals(1, dirtyBuilder.getRowCount());

        // Place a 0-row builder in slot 'X' to verify finish() skips non-null builders with 0 rows
        poolTlBuilders.get()['X'] = smallHandlerSecond.newChunkBuilder(dicts);

        try (final ChunkStorage poolStorage = poolCollector.finish())
        {
            Assert.assertEquals(3L, poolStorage.getTotalRowCount());
            Assert.assertEquals("0-row builder must not be sealed as an extra chunk", 2, poolCatalog.getAllChunks().size());
            Assert.assertNull("finish() must clear recycledBuilders entries", pools['Q']);
        }

        // 9. Test IOException fallback in sealAndSpool and finish() when writeChunk fails, plus finish() spooler failure
        final ChunkTypeRegistry failingRegistry = new ChunkTypeRegistry();
        failingRegistry.registerHandler(new ChunkTypeHandler()
        {
            @Override
            public char getTypeCode()
            {
                return 'E';
            }

            @Override
            public ChunkBuilder newChunkBuilder(final GlobalDictionaries d)
            {
                return smallHandlerAscii.newChunkBuilder(d);
            }

            @Override
            public void writeChunk(final Chunk chunk, final java.io.DataOutput out) throws java.io.IOException
            {
                throw new java.io.IOException("Simulated spool write failure");
            }

            @Override
            public Chunk readChunk(final java.io.DataInput in, final GlobalDictionaries d)
            {
                return null;
            }

            @Override
            public void scan(final Chunk chunk, final ScanPredicate pred,
                             final java.util.function.Consumer<Data> cons, final GlobalDictionaries d)
            {
            }
        });
        final File failSpoolDir = new File(tempDir, "fail-spool");
        final ChunkIngestionCollector failSpoolCollector =
            new ChunkIngestionCollector(new GlobalDictionaries(), new ChunkCatalog(), failingRegistry, failSpoolDir);
        failSpoolCollector.collect(createDummyRecord('E', 5000L));
        failSpoolCollector.collect(createDummyRecord('E', 5001L)); // triggers sealAndSpool IOException fallback
        failSpoolCollector.collect(createDummyRecord('E', 5002L)); // triggers finish() spoolChunk IOException fallback
        // Delete chunks.bin.tmp before finish() so activeSpooler.finish() fails when moving tmpFile -> targetFile
        new File(failSpoolDir, ChunkStorage.CHUNKS_FILE_NAME + ".tmp").delete();
        try
        {
            failSpoolCollector.finish();
            Assert.fail("Expected RuntimeException when spooler.finish fails");
        }
        catch (final RuntimeException expected)
        {
            Assert.assertTrue(expected.getMessage().contains("Failed to finalize spooled chunk storage"));
        }

        // 10. Test asynchronous compression task failure in finish()
        final ChunkTypeRegistry sealFailRegistry = new ChunkTypeRegistry();
        sealFailRegistry.registerHandler(new ChunkTypeHandler()
        {
            @Override
            public char getTypeCode()
            {
                return 'Q';
            }

            @Override
            public ChunkBuilder newChunkBuilder(final GlobalDictionaries d)
            {
                return new ChunkBuilder()
                {
                    private int rows;

                    @Override
                    public char getTypeCode()
                    {
                        return 'Q';
                    }

                    @Override
                    public boolean append(final Data record)
                    {
                        return ++rows >= 1;
                    }

                    @Override
                    public boolean isFull()
                    {
                        return rows >= 1;
                    }

                    @Override
                    public int getRowCount()
                    {
                        return rows;
                    }

                    @Override
                    public Chunk seal()
                    {
                        throw new IllegalStateException("Simulated seal failure");
                    }

                    @Override
                    public void reset()
                    {
                        rows = 0;
                    }
                };
            }

            @Override
            public void writeChunk(final Chunk chunk, final java.io.DataOutput out)
            {
            }

            @Override
            public Chunk readChunk(final java.io.DataInput in, final GlobalDictionaries d)
            {
                return null;
            }

            @Override
            public void scan(final Chunk chunk, final ScanPredicate pred,
                             final java.util.function.Consumer<Data> cons, final GlobalDictionaries d)
            {
            }
        });
        final ChunkIngestionCollector sealFailCollector =
            new ChunkIngestionCollector(new GlobalDictionaries(), new ChunkCatalog(), sealFailRegistry);
        sealFailCollector.collect(createDummyRecord('Q', 6000L)); // triggers sealAndSpool which throws in background task
        try
        {
            sealFailCollector.finish();
            Assert.fail("Expected RuntimeException when async compression task fails");
        }
        catch (final RuntimeException expected)
        {
            Assert.assertTrue(expected.getMessage().contains("Asynchronous chunk compression failed"));
        }
    }

    private static Data createDummyRecord(final char typeCode, final long time)
    {
        final com.xceptance.xlt.api.engine.AbstractData d = new com.xceptance.xlt.api.engine.AbstractData("Timer", typeCode)
        {
            @Override
            public void setRemainingValues(final CsvByteColumns values)
            {
            }
        };
        d.setTime(time);
        d.setAgentName("Ag");
        d.setTransactionName("Tc");
        return d;
    }

    private static ChunkTypeHandler createSmallCapacityHandler(final char typeCode, final int maxRows,
                                                               final java.util.concurrent.atomic.AtomicInteger writeCount)
    {
        final com.xceptance.xlt.report.storage.chunk.EventChunkHandler delegate =
            new com.xceptance.xlt.report.storage.chunk.EventChunkHandler();
        return new ChunkTypeHandler()
        {
            @Override
            public char getTypeCode()
            {
                return typeCode;
            }

            @Override
            public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
            {
                final ChunkBuilder inner = delegate.newChunkBuilder(dicts);
                return new ChunkBuilder()
                {
                    @Override
                    public char getTypeCode()
                    {
                        return typeCode;
                    }

                    @Override
                    public boolean append(final Data record)
                    {
                        if (inner.getRowCount() >= maxRows)
                        {
                            throw new IllegalStateException("Builder overflow beyond maxRows=" + maxRows);
                        }
                        final EventData ev = new EventData(record.getName());
                        ev.setTime(record.getTime());
                        ev.setAgentName(record.getAgentName());
                        ev.setTransactionName(record.getTransactionName());
                        ev.setMessage("m");
                        inner.append(ev);
                        return true;
                    }

                    @Override
                    public boolean isFull()
                    {
                        return inner.getRowCount() >= maxRows;
                    }

                    @Override
                    public int getRowCount()
                    {
                        return inner.getRowCount();
                    }

                    @Override
                    public Chunk seal()
                    {
                        if (Thread.currentThread().getName().startsWith("ChunkCompressionWorker-"))
                        {
                            Assert.assertTrue("Compression worker thread must be daemon", Thread.currentThread().isDaemon());
                        }
                        return inner.seal();
                    }

                    @Override
                    public void reset()
                    {
                        inner.reset();
                    }
                };
            }

            @Override
            public void writeChunk(final Chunk chunk, final java.io.DataOutput out) throws java.io.IOException
            {
                writeCount.incrementAndGet();
                delegate.writeChunk(chunk, out);
            }

            @Override
            public Chunk readChunk(final java.io.DataInput in, final GlobalDictionaries dicts) throws java.io.IOException
            {
                return delegate.readChunk(in, dicts);
            }

            @Override
            public void scan(final Chunk chunk, final ScanPredicate predicate,
                             final java.util.function.Consumer<Data> consumer, final GlobalDictionaries dicts)
            {
                delegate.scan(chunk, predicate, consumer, dicts);
            }
        };
    }

    @Test
    public void testDoubleValueChunkBuilderNoAliasingOnSeal()
    {
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler('V');
        final ChunkBuilder builder = handler.newChunkBuilder(dicts);

        for (int i = 0; i < ChunkBuilder.DEFAULT_CHUNK_CAPACITY; i++)
        {
            final CustomValue cv = new CustomValue("Metric1");
            cv.setTime(1000L + i);
            cv.setValue(1.2345 + i);
            builder.append(cv);
        }
        final com.xceptance.xlt.report.storage.chunk.DoubleValueChunk chunk1 =
            (com.xceptance.xlt.report.storage.chunk.DoubleValueChunk) builder.seal();
        final double[] values1 = chunk1.getValues();
        Assert.assertEquals(ChunkBuilder.DEFAULT_CHUNK_CAPACITY, values1.length);
        Assert.assertEquals(1.2345, values1[0], 0.000001);

        builder.reset();
        // Append new records into the recycled builder
        for (int i = 0; i < 10; i++)
        {
            final CustomValue cv = new CustomValue("Metric2");
            cv.setTime(2000L + i);
            cv.setValue(999.0 + i);
            builder.append(cv);
        }

        // Verify chunk1's values array was NOT overwritten by builder mutations
        Assert.assertEquals(1.2345, values1[0], 0.000001);
        Assert.assertEquals(2.2345, values1[1], 0.000001);
    }

    @Test
    public void testRequestSparseForensicRetentionStatusZero()
    {
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler('R');
        final ChunkBuilder builder = handler.newChunkBuilder(dicts);

        final RequestData req = new RequestData("TestReq");
        req.setTime(1000L);
        req.setRunTime(50);
        req.setResponseCode(0);
        req.setFailed(false);
        req.setUrl("http://example.com/failed_conn");
        req.setRequestId("req-123");
        builder.append(req);

        final com.xceptance.xlt.report.storage.chunk.RequestChunk chunk =
            (com.xceptance.xlt.report.storage.chunk.RequestChunk) builder.seal();
        final com.xceptance.xlt.report.storage.chunk.RequestChunk.ForensicDetails fd = chunk.getForensicDetails(0);
        Assert.assertNotNull("Forensic details must be preserved for status code 0 in sparse mode", fd);
        Assert.assertEquals("http://example.com/failed_conn", fd.url());
        Assert.assertEquals("req-123", fd.requestId());
    }

    @Test
    public void testChunkBuilderTimestampDeltaOverflowProtection()
    {
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler('R');
        final ChunkBuilder builder = handler.newChunkBuilder(dicts);

        final RequestData req1 = new RequestData("Req1");
        req1.setTime(1000L);
        req1.setRunTime(50);
        Assert.assertTrue(builder.append(req1));

        // Attempt to append record spanning > Integer.MAX_VALUE milliseconds
        final RequestData req2 = new RequestData("Req2");
        req2.setTime(1000L + (long) Integer.MAX_VALUE + 10_000L);
        req2.setRunTime(50);
        Assert.assertFalse("Builder must reject record when timestamp delta would overflow 32-bit int", builder.append(req2));
    }

    @Test
    public void testGlobalDictionariesGeometricGrowth()
    {
        final GlobalDictionaries dicts = new GlobalDictionaries();
        dicts.addSampleUrl(500, "http://example.com/500");
        dicts.addSampleUrl(1500, "http://example.com/1500");
        final GlobalDictionaries.CachedSampleUrl[] urls = dicts.getCachedSampleUrls(1500);
        Assert.assertNotNull(urls);
        Assert.assertEquals(1, urls.length);
        Assert.assertEquals("http://example.com/1500", urls[0].url());
    }

    @Test
    public void testCacheFingerprintDetectsInPlaceModificationTimestampChange() throws Exception
    {
        final File resultsDir = new File(tempDir, "fp-mtime-results");
        final File cacheDir = new File(resultsDir, "xlt-cache");
        resultsDir.mkdirs();
        cacheDir.mkdirs();

        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
        config.setDataCacheEnabled(true);
        config.setDataCacheDirectoryName("xlt-cache");

        final File log = new File(resultsDir, "timers.csv");
        Files.writeString(log.toPath(), "R,1,100,false,200\n", StandardCharsets.UTF_8);
        final long initialMtime = 1_700_000_000_000L;
        Assert.assertTrue(log.setLastModified(initialMtime));

        final CacheFingerprint fp1 = CacheFingerprint.calculate(resultsDir, config);
        Assert.assertEquals(1, fp1.fileCount());
        Assert.assertEquals(initialMtime, fp1.maxLastModified());
        fp1.saveToDirectory(cacheDir);

        final CacheFingerprint loadedFp = CacheFingerprint.loadFromDirectory(cacheDir);
        Assert.assertNotNull(loadedFp);
        Assert.assertTrue(loadedFp.matches(fp1));

        // Overwrite log in-place with the EXACT same byte length (18 bytes) but newer mtime
        Files.writeString(log.toPath(), "R,1,999,false,200\n", StandardCharsets.UTF_8);
        Assert.assertTrue(log.setLastModified(initialMtime + 5000L));

        final CacheFingerprint fp2 = CacheFingerprint.calculate(resultsDir, config);
        Assert.assertEquals("Byte size is identical", fp1.totalBytes(), fp2.totalBytes());
        Assert.assertFalse("Fingerprint must detect in-place modification via maxLastModified", loadedFp.matches(fp2));

        // Legacy 5-argument constructor (maxLastModified == 0L) remains backward compatible
        final CacheFingerprint legacyFp = new CacheFingerprint(fp1.formatVersion(), fp1.fileCount(), fp1.totalBytes(), fp1.mergeRulesHash(), fp1.xltVersion());
        Assert.assertEquals(0L, legacyFp.maxLastModified());
        Assert.assertTrue(legacyFp.matches(fp1));
    }

    @Test
    public void testGenericDataChunkVariableColumnCountsInLaterRows() throws Exception
    {
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler('Y');
        final ChunkBuilder builder = handler.newChunkBuilder(dicts);

        // First row has 5 columns; second row has 7 columns
        final Data row1 = new VariableColumnRecord("VarRec-1", 1000L, List.of("Y", "VarRec-1", "1000", "col3", "col4"));
        final Data row2 = new VariableColumnRecord("VarRec-2", 1005L, List.of("Y", "VarRec-2", "1005", "col3", "col4", "col5-extra", "col6-extra"));
        Assert.assertTrue(builder.append(row1));
        Assert.assertTrue(builder.append(row2));

        final Chunk chunk = builder.seal();
        final List<List<String>> decodedRows = new ArrayList<>();
        handler.scan(chunk, ScanPredicate.ALL, d -> {
            if (d instanceof VariableColumnRecord vcr)
            {
                decodedRows.add(vcr.parsedColumns);
            }
        }, dicts);

        Assert.assertEquals(2, decodedRows.size());
        Assert.assertEquals("col5-extra", decodedRows.get(1).get(5));
        Assert.assertEquals("col6-extra", decodedRows.get(1).get(6));
    }

    @Test
    public void testUrlHostParserByteSliceZeroAllocationAndCache()
    {
        final byte[] raw1 = "https://shop.example.com:8443/api/order?id=42".getBytes(StandardCharsets.UTF_8);
        final String host1 = com.xceptance.xlt.report.util.UrlHostParser.retrieveHostFromUrl(raw1, 0, raw1.length);
        Assert.assertEquals("shop.example.com:8443", host1);

        // Second call with identical bytes must hit the thread-local cache and return the same String instance
        final String host1Cached = com.xceptance.xlt.report.util.UrlHostParser.retrieveHostFromUrl(raw1, 0, raw1.length);
        Assert.assertSame(host1, host1Cached);

        // Slice inside a larger CSV buffer
        final byte[] csvLine = "R,OpenHome,1000,50,false,100,200,200,http://backend-01.internal/health,text/html".getBytes(StandardCharsets.UTF_8);
        final int urlOffset = 37;
        final int urlLen = "http://backend-01.internal/health".length();
        Assert.assertEquals("backend-01.internal",
                            com.xceptance.xlt.report.util.UrlHostParser.retrieveHostFromUrl(csvLine, urlOffset, urlLen));
        Assert.assertEquals("", com.xceptance.xlt.report.util.UrlHostParser.retrieveHostFromUrl(csvLine, 0, 0));
    }

    public static class VariableColumnRecord extends com.xceptance.xlt.api.engine.AbstractData
    {
        private List<String> serializeColumns;
        public List<String> parsedColumns;

        public VariableColumnRecord()
        {
            super('Y');
        }

        public VariableColumnRecord(final String name, final long time, final List<String> cols)
        {
            super(name, 'Y');
            setTime(time);
            this.serializeColumns = cols;
        }

        @Override
        public List<String> toList()
        {
            return serializeColumns;
        }

        @Override
        public void setRemainingValues(final CsvByteColumns columns)
        {
            parsedColumns = new ArrayList<>(columns.size());
            for (int i = 0; i < columns.size(); i++)
            {
                parsedColumns.add(columns.getString(i));
            }
        }
    }
}
