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

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.report.storage.chunk.Chunk;

/**
 * Two-tier filter predicate used for query acceleration in ChunkDB.
 * <p>
 * <b>Two-Tier Pruning Design:</b>
 * <ol>
 *   <li><b>Tier 1 - Chunk-Level Pruning (Coarse Grain):</b>
 *       Evaluated via {@link #mayMatchChunk(Chunk)} prior to decompression. Checks whether the chunk's
 *       timestamp range \([\text{minTime}, \text{maxTime}]\) intersects the query window \([t_{\text{from}}, t_{\text{to}}]\),
 *       and executes \(O(1)\) bitwise intersections between allowed ID sets and the chunk's internal
 *       {@link RoaringBitmap} sets. If either check fails, the chunk is skipped entirely without decompressing
 *       a single column or allocating objects.</li>
 *   <li><b>Tier 2 - Row-Level Filtering (Fine Grain):</b>
 *       Evaluated via {@link #testRow(long, short, short)} during chunk scanning. Fast primitive numeric
 *       comparisons and bitmap membership checks verify individual row timestamps and interned 16-bit IDs.</li>
 * </ol>
 */
public class ScanPredicate
{
    /** Earliest timestamp in milliseconds (inclusive). */
    private final long fromTime;

    /** Latest timestamp in milliseconds (inclusive). */
    private final long toTime;

    /** Set of allowed 16-bit timer name IDs, or {@code null} if all timer names are accepted. */
    private final RoaringBitmap allowedTimerIds;

    /** Set of allowed 16-bit unified (agent, testCase) pair IDs, or {@code null} if all pairs are accepted. */
    private final RoaringBitmap allowedAgentTestCaseIds;

    /** Unconditional predicate that matches all chunks and records across the entire dataset. */
    public static final ScanPredicate ALL = new ScanPredicate(Long.MIN_VALUE, Long.MAX_VALUE, null, null);

    /**
     * Constructs a new {@link ScanPredicate}.
     *
     * @param fromTime
     *            earliest timestamp (milliseconds since epoch, inclusive)
     * @param toTime
     *            latest timestamp (milliseconds since epoch, inclusive)
     * @param allowedTimerIds
     *            bitmap of allowed timer name IDs (null accepts all)
     * @param allowedAgentTestCaseIds
     *            bitmap of allowed (agent, testCase) pair IDs (null accepts all)
     */
    public ScanPredicate(final long fromTime, final long toTime, final RoaringBitmap allowedTimerIds, final RoaringBitmap allowedAgentTestCaseIds)
    {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.allowedTimerIds = allowedTimerIds;
        this.allowedAgentTestCaseIds = allowedAgentTestCaseIds;
    }

    /**
     * Returns the earliest allowed timestamp in milliseconds.
     *
     * @return start timestamp
     */
    public long getFromTime()
    {
        return fromTime;
    }

    /**
     * Returns the latest allowed timestamp in milliseconds.
     *
     * @return end timestamp
     */
    public long getToTime()
    {
        return toTime;
    }

    /**
     * Evaluates whether the given chunk could contain any records matching this predicate.
     * <p>
     * Performs lightweight bounding-box and bitwise intersection checks without decompressing
     * the chunk's columnar data.
     *
     * @param chunk
     *            the candidate chunk to test
     * @return {@code true} if the chunk may contain matching rows; {@code false} if it can be skipped completely
     */
    public boolean mayMatchChunk(final Chunk chunk)
    {
        // 1. Time boundary pruning: reject if chunk is strictly before or after query window
        if (chunk.getMaxTime() < fromTime || chunk.getMinTime() > toTime)
        {
            return false;
        }

        // 2. Timer name bitmap pruning: reject if chunk's timers are disjoint from allowed set
        if (allowedTimerIds != null)
        {
            final RoaringBitmap chunkTimers = chunk.getTimerNameIds();
            if (chunkTimers != null && !RoaringBitmap.intersects(allowedTimerIds, chunkTimers))
            {
                return false;
            }
        }

        // 3. Agent & TestCase pair bitmap pruning: reject if chunk's pairs are disjoint from allowed set
        if (allowedAgentTestCaseIds != null)
        {
            final RoaringBitmap chunkAgents = chunk.getAgentTestCaseIds();
            if (chunkAgents != null && !RoaringBitmap.intersects(allowedAgentTestCaseIds, chunkAgents))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests whether a specific decompressed row matches the filter conditions.
     *
     * @param time
     *            timestamp of the row in milliseconds
     * @param timerNameId
     *            interned integer timer name ID
     * @param agentTestCaseId
     *            interned integer unified (agent, testCase) pair ID
     * @return {@code true} if row satisfies all criteria; {@code false} otherwise
     */
    public boolean testRow(final long time, final int timerNameId, final int agentTestCaseId)
    {
        // 1. Primitive timestamp bounds check
        if (time < fromTime || time > toTime)
        {
            return false;
        }

        // 2. Timer name ID membership check
        if (allowedTimerIds != null && !allowedTimerIds.contains(timerNameId))
        {
            return false;
        }

        // 3. Agent + TestCase pair ID membership check
        if (allowedAgentTestCaseIds != null && !allowedAgentTestCaseIds.contains(agentTestCaseId))
        {
            return false;
        }

        return true;
    }

    /**
     * Checks if all rows of the specified chunk are guaranteed to match this predicate without
     * needing row-level checks (e.g. if the chunk is completely bounded within [fromTime, toTime]
     * and no ID filtering is active).
     *
     * @param chunk
     *            the candidate chunk to test
     * @return {@code true} if every row in the chunk will match; {@code false} otherwise
     */
    public boolean matchesAllRows(final Chunk chunk)
    {
        return allowedTimerIds == null && allowedAgentTestCaseIds == null
            && chunk.getMinTime() >= fromTime && chunk.getMaxTime() <= toTime;
    }

    /**
     * Returns the allowed timer IDs bitmap, or {@code null} if all timer names are allowed.
     *
     * @return bitmap of allowed timer IDs
     */
    public RoaringBitmap getAllowedTimerIds()
    {
        return allowedTimerIds;
    }

    /**
     * Returns the allowed agent/test case IDs bitmap, or {@code null} if all pairs are allowed.
     *
     * @return bitmap of allowed agent/test case IDs
     */
    public RoaringBitmap getAllowedAgentTestCaseIds()
    {
        return allowedAgentTestCaseIds;
    }
}
