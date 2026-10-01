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
package com.xceptance.xlt.report.storage.chunk;

import org.roaringbitmap.RoaringBitmap;

/**
 * High-performance compressed columnar chunk for Event records ('E').
 * <p>
 * Load test events record exceptional application conditions, warnings, and notifications during a run.
 * Timestamps, event names, and event messages are stored in dictionary-interned columnar arrays
 * compressed using SIMD FastPFOR codecs.
 */
public class EventChunk implements Chunk
{
    /** Number of rows contained in this chunk. */
    private final int rowCount;

    /** Minimum timestamp in epoch milliseconds across all rows in this chunk. */
    private final long minTime;

    /** Maximum timestamp in epoch milliseconds across all rows in this chunk. */
    private final long maxTime;

    /** Inverted index bitmap containing all distinct 16-bit event name IDs in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** Inverted index bitmap containing all distinct 16-bit unified (agent, testCase) pair IDs in this chunk. */
    private final RoaringBitmap agentTestCaseIds;

    // --- Compressed Columnar Data Arrays ---

    /** Compressed timestamp offset column (relative to minTime). */
    final int[] compTimeOffsets;

    /** Compressed 16-bit event name ID column. */
    final int[] compTimerNameIds;

    /** Compressed 16-bit unified (agent, testCase) ID column. */
    final int[] compAgentTestCaseIds;

    /** Compressed 16-bit event message dictionary ID column. */
    final int[] compMessages;

    /**
     * Constructs a sealed {@link EventChunk}.
     *
     * @param rowCount
     *            number of rows in the chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of unique event name IDs
     * @param agentTestCaseIds
     *            bitmap of unique agent/test case IDs
     * @param compTimeOffsets
     *            compressed timestamp offsets
     * @param compTimerNameIds
     *            compressed event name IDs
     * @param compAgentTestCaseIds
     *            compressed agent/test case IDs
     * @param compMessages
     *            compressed event message string IDs
     */
    public EventChunk(final int rowCount,
                      final long minTime,
                      final long maxTime,
                      final RoaringBitmap timerNameIds,
                      final RoaringBitmap agentTestCaseIds,
                      final int[] compTimeOffsets,
                      final int[] compTimerNameIds,
                      final int[] compAgentTestCaseIds,
                      final int[] compMessages)
    {
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.timerNameIds = timerNameIds;
        this.agentTestCaseIds = agentTestCaseIds;
        this.compTimeOffsets = compTimeOffsets;
        this.compTimerNameIds = compTimerNameIds;
        this.compAgentTestCaseIds = compAgentTestCaseIds;
        this.compMessages = compMessages;
    }

    @Override
    public char getTypeCode()
    {
        return 'E';
    }

    @Override
    public int getRowCount()
    {
        return rowCount;
    }

    @Override
    public long getMinTime()
    {
        return minTime;
    }

    @Override
    public long getMaxTime()
    {
        return maxTime;
    }

    @Override
    public RoaringBitmap getTimerNameIds()
    {
        return timerNameIds;
    }

    @Override
    public RoaringBitmap getAgentTestCaseIds()
    {
        return agentTestCaseIds;
    }
}
