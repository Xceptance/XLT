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
package com.xceptance.xlt.report.storage.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.ChunkBuilder;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeHandler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeRegistry;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;

/**
 * Unit tests for {@link ScanPredicate} verifying time window pruning, RoaringBitmap set intersections,
 * row-level filtering, and whole-chunk match optimization.
 */
public class ScanPredicateTest
{
    private static Chunk createMockChunk(final long minTime, final long maxTime,
                                         final RoaringBitmap timers, final RoaringBitmap agents)
    {
        return new Chunk()
        {
            @Override public char getTypeCode() { return 'R'; }
            @Override public int getRowCount() { return 100; }
            @Override public long getMinTime() { return minTime; }
            @Override public long getMaxTime() { return maxTime; }
            @Override public RoaringBitmap getTimerNameIds() { return timers; }
            @Override public RoaringBitmap getAgentTestCaseIds() { return agents; }
        };
    }

    @Test
    public void testTimeBoundaryPruning()
    {
        // Predicate window: [1000, 2000]
        final ScanPredicate pred = new ScanPredicate(1000L, 2000L, null, null);

        // Strictly before window [500, 999]
        final Chunk before = createMockChunk(500L, 999L, null, null);
        Assert.assertFalse("Chunk strictly before window should be pruned", pred.mayMatchChunk(before));

        // Strictly after window [2001, 3000]
        final Chunk after = createMockChunk(2001L, 3000L, null, null);
        Assert.assertFalse("Chunk strictly after window should be pruned", pred.mayMatchChunk(after));

        // Overlapping start [800, 1200]
        final Chunk overlapStart = createMockChunk(800L, 1200L, null, null);
        Assert.assertTrue("Chunk overlapping start should match", pred.mayMatchChunk(overlapStart));

        // Overlapping end [1800, 2500]
        final Chunk overlapEnd = createMockChunk(1800L, 2500L, null, null);
        Assert.assertTrue("Chunk overlapping end should match", pred.mayMatchChunk(overlapEnd));

        // Completely inside window [1200, 1800]
        final Chunk inside = createMockChunk(1200L, 1800L, null, null);
        Assert.assertTrue("Chunk inside window should match", pred.mayMatchChunk(inside));

        // Spanning entire window [500, 3000]
        final Chunk spanning = createMockChunk(500L, 3000L, null, null);
        Assert.assertTrue("Chunk spanning window should match", pred.mayMatchChunk(spanning));
    }

    @Test
    public void testTimerBitmapPruning()
    {
        final RoaringBitmap allowedTimers = new RoaringBitmap();
        allowedTimers.add(1);
        allowedTimers.add(2);

        final ScanPredicate pred = new ScanPredicate(0L, 10_000L, allowedTimers, null);

        // Chunk with disjoint timers {3, 4}
        final RoaringBitmap disjoint = new RoaringBitmap();
        disjoint.add(3);
        disjoint.add(4);
        final Chunk disjointChunk = createMockChunk(100L, 200L, disjoint, null);
        Assert.assertFalse("Chunk with disjoint timers must be pruned", pred.mayMatchChunk(disjointChunk));

        // Chunk with overlapping timers {2, 3}
        final RoaringBitmap overlap = new RoaringBitmap();
        overlap.add(2);
        overlap.add(3);
        final Chunk overlapChunk = createMockChunk(100L, 200L, overlap, null);
        Assert.assertTrue("Chunk with overlapping timers must match", pred.mayMatchChunk(overlapChunk));

        // Chunk with null timer bitmap (e.g. untyped or legacy) -> should not prune
        final Chunk nullTimerChunk = createMockChunk(100L, 200L, null, null);
        Assert.assertTrue("Chunk with null timers should not be pruned by timer filter", pred.mayMatchChunk(nullTimerChunk));
    }

    @Test
    public void testAgentTestCaseBitmapPruning()
    {
        final RoaringBitmap allowedAgents = new RoaringBitmap();
        allowedAgents.add(10);

        final ScanPredicate pred = new ScanPredicate(0L, 10_000L, null, allowedAgents);

        // Chunk with disjoint agent {20}
        final RoaringBitmap disjoint = new RoaringBitmap();
        disjoint.add(20);
        final Chunk disjointChunk = createMockChunk(100L, 200L, null, disjoint);
        Assert.assertFalse("Chunk with disjoint agent must be pruned", pred.mayMatchChunk(disjointChunk));

        // Chunk with matching agent {10, 20}
        final RoaringBitmap match = new RoaringBitmap();
        match.add(10);
        match.add(20);
        final Chunk matchChunk = createMockChunk(100L, 200L, null, match);
        Assert.assertTrue("Chunk with matching agent must match", pred.mayMatchChunk(matchChunk));
    }

    @Test
    public void testRowLevelFiltering()
    {
        final RoaringBitmap allowedTimers = new RoaringBitmap();
        allowedTimers.add(5);
        final RoaringBitmap allowedAgents = new RoaringBitmap();
        allowedAgents.add(10);

        final ScanPredicate pred = new ScanPredicate(1000L, 2000L, allowedTimers, allowedAgents);

        // Matching row
        Assert.assertTrue("Matching row should pass", pred.testRow(1500L, 5, 10));

        // Time out of range
        Assert.assertFalse("Row with time before window should fail", pred.testRow(999L, 5, 10));
        Assert.assertFalse("Row with time after window should fail", pred.testRow(2001L, 5, 10));

        // Timer ID mismatch
        Assert.assertFalse("Row with unexpected timer ID should fail", pred.testRow(1500L, 6, 10));

        // Agent ID mismatch
        Assert.assertFalse("Row with unexpected agent ID should fail", pred.testRow(1500L, 5, 11));
    }

    @Test
    public void testMatchesAllRowsOptimization()
    {
        // Unconditional predicate
        final ScanPredicate allPred = ScanPredicate.ALL;
        final Chunk insideChunk = createMockChunk(1000L, 2000L, null, null);
        Assert.assertTrue("Unconditional predicate must match all rows for any chunk",
                          allPred.matchesAllRows(insideChunk));

        // Bounded window [1000, 3000] with no ID filters
        final ScanPredicate windowPred = new ScanPredicate(1000L, 3000L, null, null);

        final Chunk fullyInside = createMockChunk(1200L, 2800L, null, null);
        Assert.assertTrue("Chunk fully inside window should match all rows",
                          windowPred.matchesAllRows(fullyInside));

        final Chunk straddlingMin = createMockChunk(800L, 2500L, null, null);
        Assert.assertFalse("Chunk straddling minTime cannot match all rows without check",
                           windowPred.matchesAllRows(straddlingMin));

        final Chunk straddlingMax = createMockChunk(1500L, 3500L, null, null);
        Assert.assertFalse("Chunk straddling maxTime cannot match all rows without check",
                           windowPred.matchesAllRows(straddlingMax));

        // Predicate with timer filter cannot match all rows blindly
        final RoaringBitmap timers = new RoaringBitmap();
        timers.add(1);
        final ScanPredicate filteredPred = new ScanPredicate(1000L, 3000L, timers, null);
        Assert.assertFalse("Predicate with ID filters cannot match all rows without row checks",
                           filteredPred.matchesAllRows(fullyInside));
    }

    @Test
    public void testRequestChunkRowFilteringByMinRunTimeAndRequireFailures()
    {
        final GlobalDictionaries dicts = new GlobalDictionaries();
        final ChunkTypeHandler handler = new ChunkTypeRegistry().getHandler('R');
        final ChunkBuilder builder = handler.newChunkBuilder(dicts);

        // Mix fast/slow and healthy/failed/HTTP-error rows inside the SAME chunk so chunk-level pruning passes
        builder.append(createRequest("FastOk", 1000L, 50, 200, false));
        builder.append(createRequest("SlowOk", 1010L, 2500, 200, false));
        builder.append(createRequest("FastFailedFlag", 1020L, 80, 200, true));
        builder.append(createRequest("FastHttp503", 1030L, 90, 503, false));
        builder.append(createRequest("SlowHttp0", 1040L, 3200, 0, false));

        final Chunk chunk = builder.seal();
        dicts.compactForReadOnly();

        // 1. minRunTime = 1000 -> must emit ONLY SlowOk (2500) and SlowHttp0 (3200)
        final ScanPredicate slowPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 1000, false);
        assertScanMatchesNames(handler, chunk, slowPred, dicts, List.of("SlowOk", "SlowHttp0"));

        // 2. requireFailures = true -> must emit ONLY FastFailedFlag, FastHttp503, and SlowHttp0
        final ScanPredicate failPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 0, true);
        assertScanMatchesNames(handler, chunk, failPred, dicts, List.of("FastFailedFlag", "FastHttp503", "SlowHttp0"));

        // 3. Combined minRunTime = 1000 AND requireFailures = true -> must emit ONLY SlowHttp0
        final ScanPredicate slowFailPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 1000, true);
        assertScanMatchesNames(handler, chunk, slowFailPred, dicts, List.of("SlowHttp0"));

        // 4. Chunk passes mayMatchChunk (contains SlowOk + Timer "FastOk"), but FastOk itself is < minRunTime -> 0 rows!
        final RoaringBitmap fastOkOnly = RoaringBitmap.bitmapOf(0);
        final ScanPredicate disjointRowPred = new ScanPredicate(0L, Long.MAX_VALUE, fastOkOnly, null, 1000, false);
        Assert.assertTrue("Chunk must pass coarse mayMatchChunk check", disjointRowPred.mayMatchChunk(chunk));
        final PostProcessedDataContainer emptyContainer = assertScanMatchesNames(handler, chunk, disjointRowPred, dicts, List.of());
        Assert.assertFalse("Filtered container with 0 matched failures must not set hasFailedRecords", emptyContainer.hasFailedRecords);
    }

    @Test
    public void testTimerAndTransactionChunkRowFilteringByMinRunTimeAndRequireFailures()
    {
        final ChunkTypeRegistry registry = new ChunkTypeRegistry();

        for (final char typeCode : new char[]{'A', 'C', 'P', 'T'})
        {
            final GlobalDictionaries dicts = new GlobalDictionaries();
            final ChunkTypeHandler handler = registry.getHandler(typeCode);
            final ChunkBuilder builder = handler.newChunkBuilder(dicts);

            builder.append(createTimerRecord(typeCode, "FastSuccess", 2000L, 40, false));
            builder.append(createTimerRecord(typeCode, "SlowSuccess", 2010L, 1500, false));
            builder.append(createTimerRecord(typeCode, "FastFailure", 2020L, 60, true));
            builder.append(createTimerRecord(typeCode, "SlowFailure", 2030L, 2200, true));

            final Chunk chunk = builder.seal();
            dicts.compactForReadOnly();

            // 1. minRunTime = 1000
            final ScanPredicate slowPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 1000, false);
            final PostProcessedDataContainer slowOnlySuccessContainer = assertScanMatchesNames(
                handler, chunk, new ScanPredicate(2000L, 2015L, null, null, 1000, false), dicts, List.of("SlowSuccess")
            );
            Assert.assertFalse("Container with only healthy matched rows must have hasFailedRecords=false for type " + typeCode,
                               slowOnlySuccessContainer.hasFailedRecords);
            assertScanMatchesNames(handler, chunk, slowPred, dicts, List.of("SlowSuccess", "SlowFailure"));

            // 2. requireFailures = true
            final ScanPredicate failPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 0, true);
            final PostProcessedDataContainer failContainer =
                assertScanMatchesNames(handler, chunk, failPred, dicts, List.of("FastFailure", "SlowFailure"));
            Assert.assertTrue("Container with matched failed rows must have hasFailedRecords=true for type " + typeCode,
                              failContainer.hasFailedRecords);

            // 3. minRunTime = 1000 AND requireFailures = true
            final ScanPredicate slowFailPred = new ScanPredicate(0L, Long.MAX_VALUE, null, null, 1000, true);
            assertScanMatchesNames(handler, chunk, slowFailPred, dicts, List.of("SlowFailure"));
        }

        // Direct non-timer testRow(time, timerId, agentId) must also reject when minRunTime > 0 or requireFailures = true
        final ScanPredicate slowPred = new ScanPredicate(0L, 5000L, null, null, 100, false);
        Assert.assertFalse("Non-timer row without runtime cannot satisfy minRunTime > 0", slowPred.testRow(1000L, 1, 1));

        final ScanPredicate failPred = new ScanPredicate(0L, 5000L, null, null, 0, true);
        Assert.assertFalse("Non-timer row without failure state cannot satisfy requireFailures", failPred.testRow(1000L, 1, 1));
    }

    private static PostProcessedDataContainer assertScanMatchesNames(final ChunkTypeHandler handler,
                                                                     final Chunk chunk,
                                                                     final ScanPredicate predicate,
                                                                     final GlobalDictionaries dicts,
                                                                     final List<String> expectedNames)
    {
        // Test Consumer<Data> scan path
        final List<String> consumerNames = new ArrayList<>();
        handler.scan(chunk, predicate, d -> consumerNames.add(d.getName()), dicts);
        Assert.assertEquals("Consumer scan mismatch for type " + chunk.getTypeCode(), expectedNames, consumerNames);

        // Test PostProcessedDataContainer scan path
        final PostProcessedDataContainer container = new PostProcessedDataContainer(16);
        container.typeCode = chunk.getTypeCode();
        handler.scan(chunk, predicate, container, dicts);
        final List<String> containerNames = new ArrayList<>();
        for (int i = 0; i < container.size(); i++)
        {
            containerNames.add(container.dataList.get(i).getName());
        }
        Assert.assertEquals("Container scan mismatch for type " + chunk.getTypeCode(), expectedNames, containerNames);
        return container;
    }

    private static RequestData createRequest(final String name,
                                             final long time,
                                             final int runTime,
                                             final int responseCode,
                                             final boolean failed)
    {
        final RequestData req = new RequestData(name);
        req.setTime(time);
        req.setRunTime(runTime);
        req.setResponseCode(responseCode);
        req.setFailed(failed);
        req.setAgentName("Agent-0");
        req.setTransactionName("TTest");
        req.setHttpMethod("GET");
        req.setContentType("text/html");
        req.setUrl("https://example.com/" + name);
        return req;
    }

    private static TimerData createTimerRecord(final char typeCode,
                                               final String name,
                                               final long time,
                                               final int runTime,
                                               final boolean failed)
    {
        final TimerData data = switch (typeCode)
        {
            case 'A' -> new ActionData(name);
            case 'C' -> new CustomData(name);
            case 'P' -> new PageLoadTimingData(name);
            case 'T' -> new TransactionData(name);
            default -> throw new IllegalArgumentException("Unsupported timer type: " + typeCode);
        };
        data.setTime(time);
        data.setRunTime(runTime);
        data.setFailed(failed);
        data.setAgentName("Agent-0");
        data.setTransactionName("TTest");
        return data;
    }
}
