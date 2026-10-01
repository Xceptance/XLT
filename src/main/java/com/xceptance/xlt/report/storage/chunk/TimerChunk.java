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
 * High-performance compressed columnar chunk for Timer records ('A' = Action, 'C' = Custom Timer, 'P' = Page).
 * <p>
 * Actions, custom timers, and page metrics share identical schema properties: timestamp, timer name,
 * agent name, test case name, duration/runtime, and pass/fail status. Storing them in a unified
 * columnar layout with SIMD integer compression allows high throughput while minimizing memory footprint.
 */
public class TimerChunk implements Chunk
{
    /** Single-character record type indicator ('A', 'C', or 'P'). */
    private final char typeCode;

    /** Number of rows contained in this chunk. */
    private final int rowCount;

    /** Minimum timestamp (in epoch milliseconds) across all rows in this chunk. */
    private final long minTime;

    /** Maximum timestamp (in epoch milliseconds) across all rows in this chunk. */
    private final long maxTime;

    /** Inverted index bitmap containing all distinct 16-bit timer name IDs in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** Inverted index bitmap containing all distinct 16-bit unified (agent, testCase) pair IDs in this chunk. */
    private final RoaringBitmap agentTestCaseIds;

    /** Bitmap indexing the row positions of records marked as failed. */
    private final RoaringBitmap failedRows;

    // --- Compressed Columnar Data Arrays ---

    /** Compressed timestamp offset column (relative to minTime). */
    final int[] compTimeOffsets;

    /** Compressed 16-bit timer name ID column. */
    final int[] compTimerNameIds;

    /** Compressed 16-bit unified (agent, testCase) ID column. */
    final int[] compAgentTestCaseIds;

    /** Compressed runtime duration column (in milliseconds). */
    final int[] compRunTimes;

    /**
     * Constructs a sealed {@link TimerChunk}.
     *
     * @param typeCode
     *            the chunk type code ('A', 'C', or 'P')
     * @param rowCount
     *            number of rows in the chunk
     * @param minTime
     *            minimum timestamp in milliseconds
     * @param maxTime
     *            maximum timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of unique timer IDs
     * @param agentTestCaseIds
     *            bitmap of unique agent/test case IDs
     * @param failedRows
     *            bitmap of failed row indices
     * @param compTimeOffsets
     *            compressed delta timestamp offsets
     * @param compTimerNameIds
     *            compressed timer name IDs
     * @param compAgentTestCaseIds
     *            compressed agent/test case IDs
     * @param compRunTimes
     *            compressed runtime durations
     */
    public TimerChunk(final char typeCode,
                      final int rowCount,
                      final long minTime,
                      final long maxTime,
                      final RoaringBitmap timerNameIds,
                      final RoaringBitmap agentTestCaseIds,
                      final RoaringBitmap failedRows,
                      final int[] compTimeOffsets,
                      final int[] compTimerNameIds,
                      final int[] compAgentTestCaseIds,
                      final int[] compRunTimes)
    {
        this.typeCode = typeCode;
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.timerNameIds = timerNameIds;
        this.agentTestCaseIds = agentTestCaseIds;
        this.failedRows = failedRows;
        this.compTimeOffsets = compTimeOffsets;
        this.compTimerNameIds = compTimerNameIds;
        this.compAgentTestCaseIds = compAgentTestCaseIds;
        this.compRunTimes = compRunTimes;
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
     * Returns the bitmap indexing the row positions of records marked as failed.
     *
     * @return bitmap of failed rows
     */
    public RoaringBitmap getFailedRows()
    {
        return failedRows;
    }
}
