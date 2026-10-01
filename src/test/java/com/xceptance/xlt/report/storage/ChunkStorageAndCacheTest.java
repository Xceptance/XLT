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
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.storage.cache.CacheFingerprint;
import com.xceptance.xlt.report.storage.cache.CacheManager;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

public class ChunkStorageAndCacheTest
{
    private File tempDir;

    @Before
    public void setUp() throws Exception
    {
        tempDir = Files.createTempDirectory("chunkdb-test").toFile();
    }

    @After
    public void tearDown() throws Exception
    {
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
            r.setHttpMethod(XltCharBuffer.valueOf("GET"));
            r.setContentType(XltCharBuffer.valueOf("text/html"));
            r.setUrl(XltCharBuffer.valueOf("https://example.com/test?id=" + (i % 10)));
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
            r.setHttpMethod(XltCharBuffer.valueOf("GET"));
            r.setContentType(XltCharBuffer.valueOf("text/html"));
            r.setUrl(XltCharBuffer.valueOf("https://example.com/item?id=" + (i % 25)));
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
        public void setRemainingValues(final List<XltCharBuffer> values)
        {
            if (values.size() >= 4)
            {
                extra1 = values.get(3).toString();
            }
            if (values.size() >= 5)
            {
                extra2 = values.get(4).toString();
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
}
