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
 * High-performance columnar chunk for double-precision floating-point metrics ('W' - WebVitalData, 'V' - CustomValue).
 * <p>
 * Supports floating-point measurement series such as Core Web Vitals (LCP, FID, CLS, INP) and custom numeric monitors.
 * Categorical attributes and timestamps are compressed into columnar arrays, while metric values are stored
 * in a high-density {@code double[]} array.
 */
public class DoubleValueChunk implements Chunk
{
    /** Single-character record type indicator ('W' or 'V'). */
    private final char typeCode;

    /** Number of rows contained in this chunk. */
    private final int rowCount;

    /** Minimum timestamp in epoch milliseconds across all rows in this chunk. */
    private final long minTime;

    /** Maximum timestamp in epoch milliseconds across all rows in this chunk. */
    private final long maxTime;

    /** Inverted index bitmap containing all distinct 16-bit metric/timer name IDs in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** Inverted index bitmap containing all distinct 16-bit unified (agent, testCase) pair IDs in this chunk. */
    private final RoaringBitmap agentTestCaseIds;

    // --- Columnar Data Arrays ---

    /** Compressed timestamp offset column (relative to minTime). */
    final int[] compTimeOffsets;

    /** Compressed 16-bit metric name ID column. */
    final int[] compTimerNameIds;

    /** Compressed 16-bit unified (agent, testCase) ID column. */
    final int[] compAgentTestCaseIds;

    /** Dense double-precision floating-point metric value array. */
    final double[] values;

    /**
     * Constructs a sealed {@link DoubleValueChunk}.
     *
     * @param typeCode
     *            chunk type code ('W' or 'V')
     * @param rowCount
     *            number of rows in chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of unique metric name IDs
     * @param agentTestCaseIds
     *            bitmap of unique agent/test case IDs
     * @param compTimeOffsets
     *            compressed timestamp offsets
     * @param compTimerNameIds
     *            compressed metric name IDs
     * @param compAgentTestCaseIds
     *            compressed agent/test case IDs
     * @param values
     *            dense primitive double array of metric values
     */
    public DoubleValueChunk(final char typeCode,
                            final int rowCount,
                            final long minTime,
                            final long maxTime,
                            final RoaringBitmap timerNameIds,
                            final RoaringBitmap agentTestCaseIds,
                            final int[] compTimeOffsets,
                            final int[] compTimerNameIds,
                            final int[] compAgentTestCaseIds,
                            final double[] values)
    {
        this.typeCode = typeCode;
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.timerNameIds = timerNameIds;
        this.agentTestCaseIds = agentTestCaseIds;
        this.compTimeOffsets = compTimeOffsets;
        this.compTimerNameIds = compTimerNameIds;
        this.compAgentTestCaseIds = compAgentTestCaseIds;
        this.values = values;
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
     * Returns the array of raw double metric values.
     *
     * @return double values array
     */
    public double[] getValues()
    {
        return values;
    }
}
