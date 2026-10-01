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

import java.util.Map;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.TransactionData;

/**
 * High-performance compressed columnar chunk specialized for Transaction records ('T').
 * <p>
 * <b>Columnar Architecture:</b>
 * Stores up to 64,000 transaction scenario executions per chunk. Runtime durations, user thread numbers,
 * and time offsets are compressed into SIMD FastPFOR packed arrays. Because failures in load tests
 * are typically sparse, failure diagnostic payloads (stack traces, failed action names, dump directory names)
 * are stored out-of-band in a sparse map keyed by row index, preserving 0-byte overhead for successful transactions.
 */
public class TransactionChunk implements Chunk
{
    /**
     * Diagnostic payload retained for failed transaction records.
     *
     * @param failedActionName
     *            the name of the action where failure occurred
     * @param stackTrace
     *            the error or exception stack trace message
     * @param directoryName
     *            the agent dump directory where error snapshots are captured
     */
    public record TransactionError(String failedActionName, String stackTrace, String directoryName)
    {
    }

    /** Number of rows in this chunk. */
    private final int rowCount;

    /** Earliest transaction timestamp in milliseconds. */
    private final long minTime;

    /** Latest transaction timestamp in milliseconds. */
    private final long maxTime;

    /** RoaringBitmap containing all distinct 16-bit transaction name IDs present in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** RoaringBitmap containing all distinct 16-bit unified (agent, testCase) pair IDs. */
    private final RoaringBitmap agentTestCaseIds;

    /** RoaringBitmap tracking the row indices of failed transactions. */
    private final RoaringBitmap failedRows;

    // --- Compressed Columnar Data Vectors ---

    /** Compressed timestamp offset column (delta from minTime). */
    final int[] compTimeOffsets;

    /** Compressed transaction name dictionary IDs. */
    final int[] compTimerNameIds;

    /** Compressed unified (agent, testCase) pair dictionary IDs. */
    final int[] compAgentTestCaseIds;

    /** Compressed transaction execution durations in milliseconds. */
    final int[] compRunTimes;

    /** Compressed virtual user thread sequence numbers. */
    final int[] compUserNumbers;

    /** Sparse map of error diagnostics keyed by row index (populated only for failed transactions). */
    final Map<Integer, TransactionError> errorDetails;

    /**
     * Constructs a sealed {@link TransactionChunk}.
     *
     * @param rowCount
     *            total records in chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of contained timer IDs
     * @param agentTestCaseIds
     *            bitmap of contained agent/test case IDs
     * @param failedRows
     *            bitmap of failed row indices
     * @param compTimeOffsets
     *            compressed time offset column
     * @param compTimerNameIds
     *            compressed timer name ID column
     * @param compAgentTestCaseIds
     *            compressed agent/test case ID column
     * @param compRunTimes
     *            compressed run times column
     * @param compUserNumbers
     *            compressed virtual user numbers column
     * @param errorDetails
     *            sparse failure error details map
     */
    public TransactionChunk(final int rowCount,
                            final long minTime,
                            final long maxTime,
                            final RoaringBitmap timerNameIds,
                            final RoaringBitmap agentTestCaseIds,
                            final RoaringBitmap failedRows,
                            final int[] compTimeOffsets,
                            final int[] compTimerNameIds,
                            final int[] compAgentTestCaseIds,
                            final int[] compRunTimes,
                            final int[] compUserNumbers,
                            final Map<Integer, TransactionError> errorDetails)
    {
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
        this.compUserNumbers = compUserNumbers;
        this.errorDetails = errorDetails;
    }

    @Override
    public char getTypeCode()
    {
        return 'T';
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
     * Returns a {@link RoaringBitmap} containing the row indices of all failed transactions in this chunk.
     *
     * @return bitmap of failed rows
     */
    public RoaringBitmap getFailedRows()
    {
        return failedRows;
    }

    /**
     * Returns the sparse map of failure diagnostic payloads keyed by row index.
     *
     * @return error details map
     */
    public Map<Integer, TransactionError> getErrorDetails()
    {
        return errorDetails;
    }
}
