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

import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.StorageIoUtils;
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
     *   <li>Compressed arrays: time offsets, timer IDs, agent IDs, virtual user numbers.</li>
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

        // 3. Compressed columnar performance vectors (including compUserNumbers for virtual user correlation)
        writeIntArray(c.compTimeOffsets, out);
        writeIntArray(c.compTimerNameIds, out);
        writeIntArray(c.compAgentTestCaseIds, out);
        writeIntArray(c.compUserNumbers, out);

        // 4. Double floating-point metric values
        StorageIoUtils.writeDoubleArray(out, c.getValues());
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
        final int[] compUserNumbers = readIntArray(in);

        // 4. Read primitive double metric values via bulk ByteBuffer transfer
        final double[] vals = StorageIoUtils.readDoubleArray(in);

        return new DoubleValueChunk(tc, rowCount, minTime, maxTime, timerIds, agentTestCaseIds,
                                    compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compUserNumbers, vals);
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
        final int[] userNumbers = c.compUserNumbers != null && c.compUserNumbers.length > 0
            ? FastIntegerCodec.decompress(c.compUserNumbers) : null;
        final double[] vals = c.getValues();

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

            final String name = timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null;
            final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
            final String agentName = pair != null ? pair.agentName() : null;
            final String testCaseName = pair != null ? pair.testCaseName() : null;
            final String userNum;
            if (userNumbers != null && i < userNumbers.length)
            {
                final int userNumId = userNumbers[i];
                userNum = (userNumId >= 0 && userNumId < strings.length) ? strings[userNumId] : null;
            }
            else
            {
                userNum = null;
            }

            if (typeCode == 'W')
            {
                final WebVitalData w = new WebVitalData();
                w.setTime(time);
                w.setName(name);
                w.setAgentName(agentName);
                w.setTransactionName(testCaseName);
                w.setTestUserNumber(userNum);
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
                v.setTestUserNumber(userNum);
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
        final int[] userNumbers = c.compUserNumbers != null && c.compUserNumbers.length > 0
            ? FastIntegerCodec.decompress(c.compUserNumbers) : null;
        final double[] vals = c.getValues();

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

            final String userNum;
            if (userNumbers != null && i < userNumbers.length)
            {
                final int userNumId = userNumbers[i];
                userNum = (userNumId >= 0 && userNumId < strings.length) ? strings[userNumId] : null;
            }
            else
            {
                userNum = null;
            }

            if (typeCode == 'W')
            {
                final WebVitalData w = new WebVitalData();
                w.setTime(time);
                w.setName(name);
                w.setAgentName(agentName);
                w.setTransactionName(testCaseName);
                w.setTestUserNumber(userNum);
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
                v.setTestUserNumber(userNum);
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
        StorageIoUtils.writeIntArray(out, array);
    }

    /**
     * Deserializes a primitive int array prefixed with its element count.
     */
    private static int[] readIntArray(final DataInput in) throws IOException
    {
        return StorageIoUtils.readIntArray(in);
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

        private int count = 0;
        private long minTime = Long.MAX_VALUE;
        private long maxTime = Long.MIN_VALUE;

        private final long[] rawTimes = new long[DEFAULT_CHUNK_CAPACITY];
        private final int[] timerIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] agentTestCaseIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] userNumbers = new int[DEFAULT_CHUNK_CAPACITY];
        private final double[] values = new double[DEFAULT_CHUNK_CAPACITY];

        private final RoaringBitmap timerIds = new RoaringBitmap();
        private final RoaringBitmap agentTestCaseIds = new RoaringBitmap();

        // Single-entry register caches and bitmasks to avoid redundant dictionary and RoaringBitmap operations
        private CharSequence lastTimerRef;
        private String lastTimerStr;
        private int lastTimerId = -1;

        private CharSequence lastAgentRef;
        private CharSequence lastTxnRef;
        private String lastAgentStr;
        private String lastTxnStr;
        private int lastAgentId = -1;

        private CharSequence lastUserNumRef;
        private String lastUserNumStr;
        private int lastUserNumId = -1;

        private long[] seenTimers = new long[64];
        private long[] seenAgents = new long[64];

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

            final long t = record.getTime();
            if (count > 0 && (Math.max(maxTime, t) - Math.min(minTime, t)) > Integer.MAX_VALUE)
            {
                return false;
            }

            final int idx = count;
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
            rawTimes[idx] = t;

            // 1. Intern timer name with register cache and bitmask-deduplicated RoaringBitmap update
            final CharSequence timerName = record.getName();
            final int tid;
            if (timerName == lastTimerRef && lastTimerId != -1)
            {
                tid = lastTimerId;
            }
            else if (lastTimerStr != null && GlobalDictionaries.charSequenceEquals(timerName, lastTimerStr))
            {
                tid = lastTimerId;
                lastTimerRef = timerName;
            }
            else
            {
                tid = dicts.getOrCreateTimerNameId(timerName);
                lastTimerId = tid;
                lastTimerRef = timerName;
                lastTimerStr = dicts.getTimerName(tid);
                if (tid >= 0)
                {
                    final int timerWord = tid >>> 6;
                    if (timerWord >= seenTimers.length)
                    {
                        seenTimers = Arrays.copyOf(seenTimers, Math.max(seenTimers.length * 2, timerWord + 16));
                    }
                    final long timerMask = 1L << tid;
                    if ((seenTimers[timerWord] & timerMask) == 0L)
                    {
                        seenTimers[timerWord] |= timerMask;
                        timerIds.add(tid);
                    }
                }
            }
            timerIdArr[idx] = tid;

            // 2. Intern unified (agent, testCase) pair with register cache
            final CharSequence agentName = record.getAgentName();
            final CharSequence txnName = record.getTransactionName();
            final int aid;
            if (agentName == lastAgentRef && txnName == lastTxnRef && lastAgentId != -1)
            {
                aid = lastAgentId;
            }
            else if (lastAgentStr != null && lastTxnStr != null
                     && GlobalDictionaries.charSequenceEquals(agentName, lastAgentStr)
                     && GlobalDictionaries.charSequenceEquals(txnName, lastTxnStr))
            {
                aid = lastAgentId;
                lastAgentRef = agentName;
                lastTxnRef = txnName;
            }
            else
            {
                aid = dicts.getOrCreateAgentTestCaseId(agentName, txnName);
                lastAgentId = aid;
                lastAgentRef = agentName;
                lastTxnRef = txnName;
                final GlobalDictionaries.AgentTestCase pair = dicts.getAgentTestCase(aid);
                lastAgentStr = pair != null ? pair.agentName() : null;
                lastTxnStr = pair != null ? pair.testCaseName() : null;
                if (aid >= 0)
                {
                    final int agentWord = aid >>> 6;
                    if (agentWord >= seenAgents.length)
                    {
                        seenAgents = Arrays.copyOf(seenAgents, Math.max(seenAgents.length * 2, agentWord + 16));
                    }
                    final long agentMask = 1L << aid;
                    if ((seenAgents[agentWord] & agentMask) == 0L)
                    {
                        seenAgents[agentWord] |= agentMask;
                        agentTestCaseIds.add(aid);
                    }
                }
            }
            agentTestCaseIdArr[idx] = aid;

            // 3. Intern virtual user number with register cache
            final CharSequence userNum = record.getTestUserNumber();
            if (userNum == null)
            {
                userNumbers[idx] = -1;
            }
            else if (userNum == lastUserNumRef)
            {
                userNumbers[idx] = lastUserNumId;
            }
            else if (lastUserNumStr != null && GlobalDictionaries.charSequenceEquals(userNum, lastUserNumStr))
            {
                userNumbers[idx] = lastUserNumId;
                lastUserNumRef = userNum;
            }
            else
            {
                lastUserNumId = dicts.getOrCreateStringId(userNum);
                lastUserNumStr = dicts.getString(lastUserNumId);
                lastUserNumRef = userNum;
                userNumbers[idx] = lastUserNumId;
            }

            if (record instanceof WebVitalData w)
            {
                values[idx] = w.getValue();
            }
            else if (record instanceof CustomValue v)
            {
                values[idx] = v.getValue();
            }

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

            final int[] timeOffsets = new int[size];
            for (int i = 0; i < size; i++)
            {
                timeOffsets[i] = (int) (rawTimes[i] - minTime);
            }

            final int[] finalTimerIds = size == timerIdArr.length ? timerIdArr : Arrays.copyOf(timerIdArr, size);
            final int[] finalAgentIds = size == agentTestCaseIdArr.length ? agentTestCaseIdArr : Arrays.copyOf(agentTestCaseIdArr, size);
            final int[] finalUserNumbers = size == userNumbers.length ? userNumbers : Arrays.copyOf(userNumbers, size);
            final double[] finalValues = Arrays.copyOf(values, size);

            final int[] compTimeOffsets = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerNameIds = FastIntegerCodec.compress(finalTimerIds);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(finalAgentIds);
            final int[] compUserNumbers = FastIntegerCodec.compress(finalUserNumbers);

            return new DoubleValueChunk(typeCode, size, minTime, maxTime, timerIds.clone(), agentTestCaseIds.clone(),
                                        compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compUserNumbers, finalValues);
        }

        @Override
        public void reset()
        {
            count = 0;
            minTime = Long.MAX_VALUE;
            maxTime = Long.MIN_VALUE;
            timerIds.clear();
            agentTestCaseIds.clear();
            Arrays.fill(seenTimers, 0L);
            Arrays.fill(seenAgents, 0L);
            lastTimerRef = null;
            lastTimerStr = null;
            lastTimerId = -1;
            lastAgentRef = null;
            lastTxnRef = null;
            lastAgentStr = null;
            lastTxnStr = null;
            lastAgentId = -1;
            lastUserNumRef = null;
            lastUserNumStr = null;
            lastUserNumId = -1;
        }
    }
}
