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

import org.junit.Assert;
import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.report.storage.chunk.Chunk;

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
}
