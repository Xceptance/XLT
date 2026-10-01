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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Specialized high-performance {@link ChunkTypeHandler} for {@link TransactionData} records ('T').
 * <p>
 * Handles serialization, deserialization, accumulation, and scanning of transaction execution records.
 * Compresses timestamps, duration metrics, and test user thread IDs into FastPFOR columnar arrays,
 * while maintaining diagnostic error details in a sparse side-channel.
 */
public class TransactionChunkHandler implements ChunkTypeHandler
{
    @Override
    public char getTypeCode()
    {
        return 'T';
    }

    @Override
    public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
    {
        return new TransactionChunkBuilder(dicts);
    }

    /**
     * Serializes a sealed {@link TransactionChunk} into binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header: row count (int), min timestamp (long), max timestamp (long).</li>
     *   <li>Bitmaps: timer name IDs, agent/testCase IDs, failed row indices.</li>
     *   <li>Columnar compressed blocks: time offsets, timer IDs, agent IDs, runtimes, user numbers.</li>
     *   <li>Sparse error map: number of failed rows, followed by (row index, failedActionName, stackTrace, directoryName).</li>
     * </ol>
     *
     * @param chunk
     *            sealed {@link TransactionChunk}
     * @param out
     *            binary output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void writeChunk(final Chunk chunk, final DataOutput out) throws IOException
    {
        final TransactionChunk c = (TransactionChunk) chunk;

        // 1. Chunk header metadata
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
        writeIntArray(c.compUserNumbers, out);

        // 4. Sparse failure diagnostic payloads
        final Map<Integer, TransactionChunk.TransactionError> errors = c.getErrorDetails();
        out.writeInt(errors.size());
        for (final Map.Entry<Integer, TransactionChunk.TransactionError> entry : errors.entrySet())
        {
            out.writeInt(entry.getKey());
            final TransactionChunk.TransactionError err = entry.getValue();
            out.writeUTF(err.failedActionName() != null ? err.failedActionName() : "");
            out.writeUTF(err.stackTrace() != null ? err.stackTrace() : "");
            out.writeUTF(err.directoryName() != null ? err.directoryName() : "");
        }
    }

    /**
     * Deserializes a binary stream into a sealed {@link TransactionChunk}.
     *
     * @param in
     *            binary input stream
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link TransactionChunk}
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public Chunk readChunk(final DataInput in, final GlobalDictionaries dicts) throws IOException
    {
        // 1. Read header metadata
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
        final int[] compUserNumbers = readIntArray(in);

        // 4. Read sparse failure diagnostic map
        final int errCount = in.readInt();
        final Map<Integer, TransactionChunk.TransactionError> errors = new HashMap<>(errCount);
        for (int i = 0; i < errCount; i++)
        {
            final int rowIdx = in.readInt();
            final String action = in.readUTF();
            final String stack = in.readUTF();
            final String dir = in.readUTF();
            errors.put(rowIdx, new TransactionChunk.TransactionError(action, stack, dir));
        }

        return new TransactionChunk(rowCount, minTime, maxTime, timerIds, agentTestCaseIds, failedRows,
                                    compTimeOffsets, compTimerNameIds, compAgentTestCaseIds,
                                    compRunTimes, compUserNumbers, errors);
    }

    /**
     * Decompresses columnar arrays and scans transactions against the query predicate.
     *
     * @param chunk
     *            the {@link TransactionChunk} to scan
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

        final TransactionChunk c = (TransactionChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] runTimes = FastIntegerCodec.decompress(c.compRunTimes);
        final int[] userNumbers = FastIntegerCodec.decompress(c.compUserNumbers);
        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();
        final Map<Integer, TransactionChunk.TransactionError> errors = c.getErrorDetails();

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

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

            final TransactionData data = new TransactionData();
            data.setTime(time);
            data.setName(timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null);

            final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
            if (pair != null)
            {
                data.setAgentName(pair.agentName());
                data.setTransactionName(pair.testCaseName());
            }

            data.setRunTime(runTimes[i]);
            final int userNumId = userNumbers[i];
            data.setTestUserNumber(userNumId >= 0 && userNumId < strings.length ? strings[userNumId] : null);

            final boolean failed = anyFailed && failedRows.contains(i);
            data.setFailed(failed);
            if (failed && errors != null)
            {
                final TransactionChunk.TransactionError err = errors.get(i);
                if (err != null)
                {
                    data.setFailedActionName(err.failedActionName());
                    data.setFailureStackTrace(err.stackTrace());
                    data.setDirectoryName(err.directoryName());
                }
            }

            consumer.accept(data);
        }
    }

    /**
     * High-performance container-based scan for transaction chunks.
     * <p>
     * Reuses pooled {@link TransactionData} instances from {@link PostProcessedDataContainer#getOrCreateTransactionData(int)},
     * accumulates records via {@link PostProcessedDataContainer#addFast(Data)}, computes the time range in bulk,
     * and sets {@link PostProcessedDataContainer#hasFailedRecords} to allow error report providers to skip clean chunks.
     *
     * @param chunk
     *            the transaction chunk to scan
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

        final TransactionChunk c = (TransactionChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] runTimes = FastIntegerCodec.decompress(c.compRunTimes);
        final int[] userNumbers = FastIntegerCodec.decompress(c.compUserNumbers);
        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();
        final Map<Integer, TransactionChunk.TransactionError> errors = c.getErrorDetails();

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
        final String[] strings = dicts.getStringsArray();

        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        int lastTimerId = -1;
        String lastTimerName = null;

        int lastAgentId = -1;
        GlobalDictionaries.AgentTestCase lastPair = null;

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

            final TransactionData data = container.getOrCreateTransactionData(containerIndex++);
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
            final int userNumId = userNumbers[i];
            data.setTestUserNumber(userNumId >= 0 && userNumId < strings.length ? strings[userNumId] : null);

            final boolean failed = failedLookup != null && failedLookup[i];
            data.setFailed(failed);
            if (failed && errors != null)
            {
                final TransactionChunk.TransactionError err = errors.get(i);
                if (err != null)
                {
                    data.setFailedActionName(err.failedActionName());
                    data.setFailureStackTrace(err.stackTrace());
                    data.setDirectoryName(err.directoryName());
                }
            }
            else
            {
                data.setFailedActionName(null);
                data.setFailureStackTrace((String) null);
                data.setDirectoryName(null);
            }

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
    // TransactionChunkBuilder
    // -------------------------------------------------------------------------

    /**
     * High-throughput mutable accumulation buffer for collecting and compressing {@link TransactionData} into a {@link TransactionChunk}.
     * <p>
     * <b>Zero-Copy Direct Columnar Design:</b>
     * Records are written directly into pre-allocated primitive arrays upon {@link #append(Data)}.
     * This avoids buffering 65,536 {@link TransactionData} objects on the heap and eliminates a secondary
     * transformation loop during {@link #seal()}.
     */
    private static class TransactionChunkBuilder implements ChunkBuilder
    {
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
        private final int[] userNumbers = new int[DEFAULT_CHUNK_CAPACITY];

        // ---------------------------------------------------------------------
        // In-Memory Index Bitmaps
        // ---------------------------------------------------------------------
        private final RoaringBitmap timerIds = new RoaringBitmap();
        private final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        private final RoaringBitmap failedRows = new RoaringBitmap();

        // ---------------------------------------------------------------------
        // Diagnostics
        // ---------------------------------------------------------------------
        private final Map<Integer, TransactionChunk.TransactionError> errorMap = new HashMap<>();

        /**
         * Constructs a new transaction chunk builder associated with global dictionaries.
         *
         * @param dicts
         *            global dictionaries for interning string metadata
         */
        TransactionChunkBuilder(final GlobalDictionaries dicts)
        {
            this.dicts = dicts;
        }

        @Override
        public char getTypeCode()
        {
            return 'T';
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

            final TransactionData td = (TransactionData) record;
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

            // 4. Record failure status and diagnostics out-of-band
            if (td.hasFailed())
            {
                failedRows.add(idx);
                errorMap.put(idx, new TransactionChunk.TransactionError(
                    td.getFailedActionName(),
                    td.getFailureStackTrace(),
                    td.getDirectoryName()
                ));
            }

            // 5. Store run time and intern test user number
            runTimes[idx] = td.getRunTime();
            userNumbers[idx] = dicts.getOrCreateStringId(td.getTestUserNumber());

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
            final int[] finalUserNumbers = size == DEFAULT_CHUNK_CAPACITY ? userNumbers : Arrays.copyOf(userNumbers, size);

            // Compress columns via FastPFOR
            final int[] compTimeOffsets = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerNameIds = FastIntegerCodec.compress(finalTimerIds);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(finalAgentIds);
            final int[] compRunTimes = FastIntegerCodec.compress(finalRunTimes);
            final int[] compUserNumbers = FastIntegerCodec.compress(finalUserNumbers);

            return new TransactionChunk(size, minTime, maxTime, timerIds, agentTestCaseIds, failedRows,
                                        compTimeOffsets, compTimerNameIds, compAgentTestCaseIds,
                                        compRunTimes, compUserNumbers, errorMap);
        }
    }
}
