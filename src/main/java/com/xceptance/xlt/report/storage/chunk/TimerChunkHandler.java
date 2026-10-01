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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * High-performance {@link ChunkTypeHandler} for standard timer records ('A' - Action, 'C' - Custom, 'P' - PageLoad).
 * <p>
 * Manages serialization, deserialization, accumulation, and fast scanning for duration-based metrics.
 * Timestamps, durations, timer names, and agent/test case IDs are compressed into SIMD FastPFOR arrays.
 */
public class TimerChunkHandler implements ChunkTypeHandler
{
    /** The single-character type code handled by this instance ('A', 'C', or 'P'). */
    private final char typeCode;

    /**
     * Constructs a handler for the given timer record type.
     *
     * @param typeCode
     *            'A' for ActionData, 'C' for CustomData, or 'P' for PageLoadTimingData
     */
    public TimerChunkHandler(final char typeCode)
    {
        this.typeCode = typeCode;
    }

    @Override
    public char getTypeCode()
    {
        return typeCode;
    }

    @Override
    public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
    {
        return new TimerChunkBuilder(typeCode, dicts);
    }

    /**
     * Serializes a sealed {@link TimerChunk} to binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header: type code (char), row count (int), min timestamp (long), max timestamp (long).</li>
     *   <li>Bitmaps: timer name IDs, agent/testCase IDs, failed row indices.</li>
     *   <li>Compressed arrays: time offsets, timer IDs, agent IDs, runtimes.</li>
     * </ol>
     *
     * @param chunk
     *            sealed {@link TimerChunk}
     * @param out
     *            binary output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void writeChunk(final Chunk chunk, final DataOutput out) throws IOException
    {
        final TimerChunk c = (TimerChunk) chunk;

        // 1. Chunk header metadata
        out.writeChar(c.getTypeCode());
        out.writeInt(c.getRowCount());
        out.writeLong(c.getMinTime());
        out.writeLong(c.getMaxTime());

        // 2. RoaringBitmap inverted index filters
        c.getTimerNameIds().serialize(out);
        c.getAgentTestCaseIds().serialize(out);
        c.getFailedRows().serialize(out);

        // 3. Compressed columnar performance vectors
        writeIntArray(c.compTimeOffsets, out);
        writeIntArray(c.compTimerNameIds, out);
        writeIntArray(c.compAgentTestCaseIds, out);
        writeIntArray(c.compRunTimes, out);
    }

    /**
     * Deserializes a binary stream into a sealed {@link TimerChunk}.
     *
     * @param in
     *            binary input stream
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link TimerChunk}
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public Chunk readChunk(final DataInput in, final GlobalDictionaries dicts) throws IOException
    {
        // 1. Read header metadata
        final char tc = in.readChar();
        final int rowCount = in.readInt();
        final long minTime = in.readLong();
        final long maxTime = in.readLong();

        // 2. Deserialize RoaringBitmaps
        final RoaringBitmap timerIds = new RoaringBitmap();
        timerIds.deserialize(in);

        final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        agentTestCaseIds.deserialize(in);

        final RoaringBitmap failedRows = new RoaringBitmap();
        failedRows.deserialize(in);

        // 3. Read compressed integer column arrays
        final int[] compTimeOffsets = readIntArray(in);
        final int[] compTimerNameIds = readIntArray(in);
        final int[] compAgentTestCaseIds = readIntArray(in);
        final int[] compRunTimes = readIntArray(in);

        return new TimerChunk(tc, rowCount, minTime, maxTime, timerIds, agentTestCaseIds, failedRows,
                              compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes);
    }

    /**
     * Decompresses columnar arrays and scans timer records against the query predicate.
     *
     * @param chunk
     *            the {@link TimerChunk} to scan
     * @param predicate
     *            scan filter predicate
     * @param consumer
     *            record consumer callback
     * @param dicts
     *            global dictionaries for string lookups
     */
    @Override
    public void scan(final Chunk chunk, final ScanPredicate predicate, final Consumer<Data> consumer, final GlobalDictionaries dicts)
    {
        // 1. Chunk-level boundary and bitmap intersection pruning
        if (!predicate.mayMatchChunk(chunk))
        {
            return;
        }

        final TimerChunk c = (TimerChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] runTimes = FastIntegerCodec.decompress(c.compRunTimes);
        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();

        // 3. Linear row scan and object reconstruction
        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int timerId = timerIds[i];
            final int agentId = agentIds[i];

            // Fine-grained row-level check
            if (!predicate.testRow(time, timerId, agentId))
            {
                continue;
            }

            final TimerData data = createRecord(typeCode);
            data.setTime(time);
            data.setName(timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null);

            final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
            if (pair != null)
            {
                data.setAgentName(pair.agentName());
                data.setTransactionName(pair.testCaseName());
            }

            data.setRunTime(runTimes[i]);
            data.setFailed(anyFailed && failedRows.contains(i));

            consumer.accept(data);
        }
    }

    /**
     * High-performance container-based scan for timer chunks ('A' - Actions, 'C' - Custom, 'P' - PageLoad).
     * <p>
     * Reuses pooled {@link TimerData} instances from {@link PostProcessedDataContainer},
     * accumulates records via {@link PostProcessedDataContainer#addFast(Data)}, computes the time range in bulk,
     * and sets {@link PostProcessedDataContainer#hasFailedRecords} to allow error report providers to skip clean chunks.
     *
     * @param chunk
     *            the timer chunk to scan
     * @param predicate
     *            the scan filter predicate
     * @param container
     *            the destination post-processed data container
     * @param dicts
     *            global intern dictionaries
     */
    @Override
    public void scan(final Chunk chunk, final ScanPredicate predicate, final PostProcessedDataContainer container, final GlobalDictionaries dicts)
    {
        // 1. Chunk-level boundary and bitmap intersection pruning
        if (!predicate.mayMatchChunk(chunk))
        {
            return;
        }

        final TimerChunk c = (TimerChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] runTimes = FastIntegerCodec.decompress(c.compRunTimes);
        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();

        // Convert failed bitmap to fast boolean lookup table if any rows failed
        boolean[] failedLookup = null;
        if (anyFailed)
        {
            failedLookup = new boolean[rowCount];
            for (final int r : failedRows)
            {
                if (r < rowCount)
                {
                    failedLookup[r] = true;
                }
            }
        }

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();

        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        int lastTimerId = -1;
        String lastTimerName = null;

        int lastAgentId = -1;
        GlobalDictionaries.AgentTestCase lastPair = null;

        final char tc = c.getTypeCode();
        int containerIndex = container.dataList.size();

        // 3. Linear row scan and zero-allocation object reconstitution
        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int timerId = timerIds[i];
            final int agentId = agentIds[i];

            // Fine-grained row-level check
            if (!predicate.testRow(time, timerId, agentId))
            {
                continue;
            }

            if (time < minTime)
            {
                minTime = time;
            }
            if (time > maxTime)
            {
                maxTime = time;
            }

            // Pool lookup based on concrete type code
            final TimerData data;
            if (tc == 'A')
            {
                data = container.getOrCreateActionData(containerIndex++);
            }
            else if (tc == 'C')
            {
                data = container.getOrCreateCustomData(containerIndex++);
            }
            else
            {
                data = createRecord(tc);
            }

            data.setTime(time);

            // Register-cached timer name lookup
            if (timerId == lastTimerId)
            {
                data.setName(lastTimerName);
            }
            else
            {
                lastTimerId = timerId;
                lastTimerName = (timerId >= 0 && timerId < timerNames.length) ? timerNames[timerId] : null;
                data.setName(lastTimerName);
            }

            // Register-cached agent/testCase pair lookup
            if (agentId == lastAgentId)
            {
                if (lastPair != null)
                {
                    data.setAgentName(lastPair.agentName());
                    data.setTransactionName(lastPair.testCaseName());
                }
                else
                {
                    data.setAgentName(null);
                    data.setTransactionName(null);
                }
            }
            else
            {
                lastAgentId = agentId;
                lastPair = (agentId >= 0 && agentId < agentPairs.length) ? agentPairs[agentId] : null;
                if (lastPair != null)
                {
                    data.setAgentName(lastPair.agentName());
                    data.setTransactionName(lastPair.testCaseName());
                }
                else
                {
                    data.setAgentName(null);
                    data.setTransactionName(null);
                }
            }

            data.setRunTime(runTimes[i]);
            data.setFailed(failedLookup != null && failedLookup[i]);

            container.addFast(data);
        }

        // Bulk time range update
        if (minTime <= maxTime)
        {
            container.updateTimeRange(minTime, maxTime);
        }

        container.hasFailedRecords = anyFailed;
    }


    /**
     * Factory method creating a concrete instance of {@link TimerData} based on the type code.
     */
    private TimerData createRecord(final char tc)
    {
        return switch (tc)
        {
            case 'A' -> new ActionData();
            case 'C' -> new CustomData();
            case 'P' -> new PageLoadTimingData();
            default -> new ActionData();
        };
    }

    /**
     * Serializes a primitive int array prefixed with its element count.
     */
    private static void writeIntArray(final int[] array, final DataOutput out) throws IOException
    {
        out.writeInt(array.length);
        for (final int v : array)
        {
            out.writeInt(v);
        }
    }

    /**
     * Deserializes a primitive int array prefixed with its element count.
     */
    private static int[] readIntArray(final DataInput in) throws IOException
    {
        final int len = in.readInt();
        final int[] array = new int[len];
        for (int i = 0; i < len; i++)
        {
            array[i] = in.readInt();
        }
        return array;
    }

    // -------------------------------------------------------------------------
    // TimerChunkBuilder
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // TimerChunkBuilder
    // -------------------------------------------------------------------------

    /**
     * High-throughput mutable accumulation buffer for collecting and compressing {@link TimerData} into a {@link TimerChunk}.
     * <p>
     * <b>Zero-Copy Direct Columnar Architecture:</b>
     * Incoming records are written directly into pre-allocated primitive arrays upon {@link #append(Data)}.
     * This completely avoids accumulating 65,536 {@link TimerData} objects on the heap and eliminates a secondary
     * loop on {@link #seal()}.
     */
    private static class TimerChunkBuilder implements ChunkBuilder
    {
        private final char typeCode;
        private final GlobalDictionaries dicts;

        /** Total number of records currently written to this builder. */
        private int count = 0;

        /** Earliest timestamp encountered in this chunk. */
        private long minTime = Long.MAX_VALUE;

        /** Latest timestamp encountered in this chunk. */
        private long maxTime = Long.MIN_VALUE;

        // ---------------------------------------------------------------------
        // Columnar Primitive Arrays (Pre-allocated to DEFAULT_CHUNK_CAPACITY)
        // ---------------------------------------------------------------------
        private final long[] rawTimes = new long[DEFAULT_CHUNK_CAPACITY];
        private final int[] timerIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] agentTestCaseIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] runTimes = new int[DEFAULT_CHUNK_CAPACITY];

        // ---------------------------------------------------------------------
        // In-Memory Index Bitmaps
        // ---------------------------------------------------------------------
        private final RoaringBitmap timerIds = new RoaringBitmap();
        private final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        private final RoaringBitmap failedRows = new RoaringBitmap();

        /**
         * Constructs a new timer chunk builder for the specified type code.
         *
         * @param typeCode
         *            record type code ('A', 'C', or 'P')
         * @param dicts
         *            global dictionaries for interning string metadata
         */
        TimerChunkBuilder(final char typeCode, final GlobalDictionaries dicts)
        {
            this.typeCode = typeCode;
            this.dicts = dicts;
        }

        @Override
        public char getTypeCode()
        {
            return typeCode;
        }

        @Override
        public int getRowCount()
        {
            return count;
        }

        @Override
        public boolean isFull()
        {
            return count >= DEFAULT_CHUNK_CAPACITY;
        }

        @Override
        public boolean append(final Data record)
        {
            if (count >= DEFAULT_CHUNK_CAPACITY)
            {
                return false;
            }

            final TimerData td = (TimerData) record;
            final int idx = count;

            // 1. Maintain bounding timestamps and store raw timestamp
            final long t = td.getTime();
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
            rawTimes[idx] = t;

            // 2. Intern timer name and update bitmap index
            final int timerId = dicts.getOrCreateTimerNameId(td.getName());
            timerIdArr[idx] = timerId;
            timerIds.add(timerId);

            // 3. Intern unified agent + test case pair and update bitmap index
            final int aid = dicts.getOrCreateAgentTestCaseId(td.getAgentName(), td.getTransactionName());
            agentTestCaseIdArr[idx] = aid;
            agentTestCaseIds.add(aid);

            // 4. Record failure status
            if (td.hasFailed())
            {
                failedRows.add(idx);
            }

            // 5. Store run time
            runTimes[idx] = td.getRunTime();

            count++;
            return true;
        }

        @Override
        public Chunk seal()
        {
            final int size = count;
            if (size == 0)
            {
                minTime = 0;
                maxTime = 0;
            }

            // Convert absolute epoch timestamps into delta offsets relative to minTime
            final int[] timeOffsets = new int[size];
            for (int i = 0; i < size; i++)
            {
                timeOffsets[i] = (int) (rawTimes[i] - minTime);
            }

            // If chunk was full, borrow arrays directly; otherwise trim to exact row count
            final int[] finalTimerIds = size == DEFAULT_CHUNK_CAPACITY ? timerIdArr : Arrays.copyOf(timerIdArr, size);
            final int[] finalAgentIds = size == DEFAULT_CHUNK_CAPACITY ? agentTestCaseIdArr : Arrays.copyOf(agentTestCaseIdArr, size);
            final int[] finalRunTimes = size == DEFAULT_CHUNK_CAPACITY ? runTimes : Arrays.copyOf(runTimes, size);

            // Compress columns via FastPFOR
            final int[] compTimeOffsets = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerNameIds = FastIntegerCodec.compress(finalTimerIds);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(finalAgentIds);
            final int[] compRunTimes = FastIntegerCodec.compress(finalRunTimes);

            return new TimerChunk(typeCode, size, minTime, maxTime, timerIds, agentTestCaseIds, failedRows,
                                  compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes);
        }
    }
}
