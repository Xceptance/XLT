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
 * Generic columnar chunk representation for fallback or dynamically registered {@link Data} record types.
 * <p>
 * Ensures extensibility if new or custom record types are added to the system without requiring specialized
 * chunk codecs. Timestamps, names, and agent/test case pairs are compressed into columnar arrays, and
 * any additional fields parsed via CSV/tokens are interned as string dictionary ID columns.
 */
public class GenericDataChunk implements Chunk
{
    /** Single-character record type indicator. */
    private final char typeCode;

    /** Number of rows contained in this chunk. */
    private final int rowCount;

    /** Minimum timestamp in epoch milliseconds across all rows in this chunk. */
    private final long minTime;

    /** Maximum timestamp in epoch milliseconds across all rows in this chunk. */
    private final long maxTime;

    /** Inverted index bitmap containing all distinct 16-bit timer/metric name IDs in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** Inverted index bitmap containing all distinct 32-bit unified (agent, testCase) pair IDs in this chunk. */
    private final RoaringBitmap agentTestCaseIds;

    /** Chunk-local dictionary strings referenced by index in compressedFieldStringIds. */
    private final String[] chunkStrings;

    // --- Compressed Columnar Data Arrays ---

    /** Compressed timestamp offset column (relative to minTime). */
    final int[] compressedTimes;

    /** Compressed 32-bit timer name ID column. */
    final int[] compressedTimerNameIds;

    /** Compressed 32-bit unified (agent, testCase) ID column. */
    final int[] compressedAgentTestCaseIds;

    /** Compressed dictionary ID columns for arbitrary string attributes. */
    final int[][] compressedFieldStringIds;

    /**
     * Constructs a sealed {@link GenericDataChunk}.
     *
     * @param typeCode
     *            the custom record type code
     * @param rowCount
     *            number of rows in the chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of unique timer IDs
     * @param agentTestCaseIds
     *            bitmap of unique agent/test case IDs
     * @param chunkStrings
     *            chunk-local dictionary strings
     * @param compressedTimes
     *            compressed timestamp offsets
     * @param compressedTimerNameIds
     *            compressed timer name IDs
     * @param compressedAgentTestCaseIds
     *            compressed agent/test case IDs
     * @param compressedFieldStringIds
     *            compressed array of string attribute columns
     */
    public GenericDataChunk(final char typeCode,
                            final int rowCount,
                            final long minTime,
                            final long maxTime,
                            final RoaringBitmap timerNameIds,
                            final RoaringBitmap agentTestCaseIds,
                            final String[] chunkStrings,
                            final int[] compressedTimes,
                            final int[] compressedTimerNameIds,
                            final int[] compressedAgentTestCaseIds,
                            final int[][] compressedFieldStringIds)
    {
        this.typeCode = typeCode;
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.timerNameIds = timerNameIds;
        this.agentTestCaseIds = agentTestCaseIds;
        this.chunkStrings = chunkStrings != null ? chunkStrings : new String[0];
        this.compressedTimes = compressedTimes;
        this.compressedTimerNameIds = compressedTimerNameIds;
        this.compressedAgentTestCaseIds = compressedAgentTestCaseIds;
        this.compressedFieldStringIds = compressedFieldStringIds;
    }

    @Override
    public char getTypeCode()
    {
        return typeCode;
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

    /**
     * Returns the chunk-local string dictionary holding strings interned specifically for this chunk.
     *
     * @return array of chunk-local strings
     */
    public String[] getChunkStrings()
    {
        return chunkStrings;
    }
}
