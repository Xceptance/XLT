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
import java.util.List;
import java.util.function.Consumer;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Specialized {@link ChunkTypeHandler} for double values ('W' - WebVitalData, 'V' - CustomValue).
 * <p>
 * Handles floating-point measurements with categorical metadata. Timestamps and string IDs are
 * bit-packed with SIMD FastPFOR codecs, while metric values are serialized as primitive 64-bit IEEE 754 floats.
 */
public class DoubleValueChunkHandler implements ChunkTypeHandler
{
    /** The single-character type code handled by this instance ('W' or 'V'). */
    private final char typeCode;

    /**
     * Constructs a handler for double value records.
     *
     * @param typeCode
     *            'W' for WebVitalData or 'V' for CustomValue
     */
    public DoubleValueChunkHandler(final char typeCode)
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
        return new DoubleValueChunkBuilder(typeCode, dicts);
    }

    /**
     * Serializes a sealed {@link DoubleValueChunk} to binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header: type code (char), row count (int), min timestamp (long), max timestamp (long).</li>
     *   <li>Bitmaps: metric/timer name IDs, agent/testCase IDs.</li>
     *   <li>Compressed arrays: time offsets, timer IDs, agent IDs.</li>
     *   <li>Floating point array: length (int), followed by raw double values.</li>
     * </ol>
     *
     * @param chunk
     *            sealed {@link DoubleValueChunk}
     * @param out
     *            binary output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void writeChunk(final Chunk chunk, final DataOutput out) throws IOException
    {
        final DoubleValueChunk c = (DoubleValueChunk) chunk;

        // 1. Chunk header metadata
        out.writeChar(c.getTypeCode());
        out.writeInt(c.getRowCount());
        out.writeLong(c.getMinTime());
        out.writeLong(c.getMaxTime());

        // 2. RoaringBitmap inverted index filters
        c.getTimerNameIds().serialize(out);
        c.getAgentTestCaseIds().serialize(out);

        // 3. Compressed columnar performance vectors
        writeIntArray(c.compTimeOffsets, out);
        writeIntArray(c.compTimerNameIds, out);
        writeIntArray(c.compAgentTestCaseIds, out);

        // 4. Double floating-point metric values
        final double[] vals = c.getValues();
        out.writeInt(vals.length);
        for (final double v : vals)
        {
            out.writeDouble(v);
        }
    }

    /**
     * Deserializes a binary stream into a sealed {@link DoubleValueChunk}.
     *
     * @param in
     *            binary input stream
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link DoubleValueChunk}
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

        // 3. Read compressed integer column arrays
        final int[] compTimeOffsets = readIntArray(in);
        final int[] compTimerNameIds = readIntArray(in);
        final int[] compAgentTestCaseIds = readIntArray(in);

        // 4. Read primitive double metric values
        final int valLen = in.readInt();
        final double[] vals = new double[valLen];
        for (int i = 0; i < valLen; i++)
        {
            vals[i] = in.readDouble();
        }

        return new DoubleValueChunk(tc, rowCount, minTime, maxTime, timerIds, agentTestCaseIds,
                                    compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, vals);
    }

    /**
     * Decompresses columnar arrays and scans double value records against the query predicate.
     *
     * @param chunk
     *            the {@link DoubleValueChunk} to scan
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

        final DoubleValueChunk c = (DoubleValueChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final double[] vals = c.getValues();

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

            final String name = timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null;
            final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
            final String agentName = pair != null ? pair.agentName() : null;
            final String testCaseName = pair != null ? pair.testCaseName() : null;

            if (typeCode == 'W')
            {
                final WebVitalData w = new WebVitalData();
                w.setTime(time);
                w.setName(name);
                w.setAgentName(agentName);
                w.setTransactionName(testCaseName);
                w.setValue(vals[i]);
                consumer.accept(w);
            }
            else
            {
                final CustomValue v = new CustomValue();
                v.setTime(time);
                v.setName(name);
                v.setAgentName(agentName);
                v.setTransactionName(testCaseName);
                v.setValue(vals[i]);
                consumer.accept(v);
            }
        }
    }

    /**
     * High-performance container-based scan for double value chunks ('W' - WebVitalData, 'V' - CustomValue).
     * <p>
     * Reuses pooled {@link CustomValue} instances from {@link PostProcessedDataContainer},
     * accumulates records via {@link PostProcessedDataContainer#addFast(Data)}, and computes time range in bulk.
     *
     * @param chunk
     *            the double value chunk to scan
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

        final DoubleValueChunk c = (DoubleValueChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final double[] vals = c.getValues();

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();

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

            // Register-cached name lookup
            final String name;
            if (timerId == lastTimerId)
            {
                name = lastTimerName;
            }
            else
            {
                lastTimerId = timerId;
                lastTimerName = (timerId >= 0 && timerId < timerNames.length) ? timerNames[timerId] : null;
                name = lastTimerName;
            }

            // Register-cached agent/testCase pair lookup
            final String agentName;
            final String testCaseName;
            if (agentId == lastAgentId)
            {
                if (lastPair != null)
                {
                    agentName = lastPair.agentName();
                    testCaseName = lastPair.testCaseName();
                }
                else
                {
                    agentName = null;
                    testCaseName = null;
                }
            }
            else
            {
                lastAgentId = agentId;
                lastPair = (agentId >= 0 && agentId < agentPairs.length) ? agentPairs[agentId] : null;
                if (lastPair != null)
                {
                    agentName = lastPair.agentName();
                    testCaseName = lastPair.testCaseName();
                }
                else
                {
                    agentName = null;
                    testCaseName = null;
                }
            }

            if (typeCode == 'W')
            {
                final WebVitalData w = new WebVitalData();
                w.setTime(time);
                w.setName(name);
                w.setAgentName(agentName);
                w.setTransactionName(testCaseName);
                w.setValue(vals[i]);
                container.addFast(w);
            }
            else
            {
                final CustomValue v = container.getOrCreateCustomValue(containerIndex++);
                v.setTime(time);
                v.setName(name);
                v.setAgentName(agentName);
                v.setTransactionName(testCaseName);
                v.setValue(vals[i]);
                container.addFast(v);
            }
        }

        // Bulk time range update
        if (minTime <= maxTime)
        {
            container.updateTimeRange(minTime, maxTime);
        }

        container.hasFailedRecords = false;
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
    // DoubleValueChunkBuilder
    // -------------------------------------------------------------------------

    /**
     * Mutable accumulation buffer for collecting and compressing {@link WebVitalData} or {@link CustomValue} records.
     */
    private static class DoubleValueChunkBuilder implements ChunkBuilder
    {
        private final char typeCode;
        private final GlobalDictionaries dicts;
        private final List<Data> records = new ArrayList<>(DEFAULT_CHUNK_CAPACITY);

        DoubleValueChunkBuilder(final char typeCode, final GlobalDictionaries dicts)
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
            return records.size();
        }

        @Override
        public boolean isFull()
        {
            return records.size() >= DEFAULT_CHUNK_CAPACITY;
        }

        @Override
        public boolean append(final Data record)
        {
            if (isFull())
            {
                return false;
            }
            records.add(record);
            return true;
        }

        @Override
        public Chunk seal()
        {
            final int size = records.size();
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;

            final RoaringBitmap timerIds = new RoaringBitmap();
            final RoaringBitmap agentTestCaseIds = new RoaringBitmap();

            // 1. Determine bounding timestamps
            for (int i = 0; i < size; i++)
            {
                final Data r = records.get(i);
                final long t = r.getTime();
                if (t < minTime) minTime = t;
                if (t > maxTime) maxTime = t;
            }
            if (size == 0)
            {
                minTime = 0;
                maxTime = 0;
            }

            // 2. Allocate columnar primitive arrays
            final int[] timeOffsets = new int[size];
            final int[] timerIdArr = new int[size];
            final int[] agentTestCaseIdArr = new int[size];
            final double[] values = new double[size];

            // 3. Populate columnar arrays and dictionaries
            for (int i = 0; i < size; i++)
            {
                final Data r = records.get(i);
                timeOffsets[i] = (int) (r.getTime() - minTime);

                final int tid = dicts.getOrCreateTimerNameId(r.getName());
                timerIdArr[i] = tid;
                timerIds.add(tid);

                final int aid = dicts.getOrCreateAgentTestCaseId(r.getAgentName(), r.getTransactionName());
                agentTestCaseIdArr[i] = aid;
                agentTestCaseIds.add(aid);

                if (r instanceof WebVitalData w)
                {
                    values[i] = w.getValue();
                }
                else if (r instanceof CustomValue v)
                {
                    values[i] = v.getValue();
                }
            }

            // 4. Compress integer columns via FastPFOR
            final int[] compTimeOffsets = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerNameIds = FastIntegerCodec.compress(timerIdArr);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(agentTestCaseIdArr);

            return new DoubleValueChunk(typeCode, size, minTime, maxTime, timerIds, agentTestCaseIds,
                                        compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, values);
        }
    }
}
