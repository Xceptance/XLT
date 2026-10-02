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

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Specialized {@link ChunkTypeHandler} for {@link EventData} records ('E').
 * <p>
 * Manages serialization, deserialization, accumulation, and query scans for test execution events.
 * Timestamps, event names, agent/test case pairs, and event messages are compressed into
 * SIMD FastPFOR columnar arrays.
 */
public class EventChunkHandler implements ChunkTypeHandler
{
    @Override
    public char getTypeCode()
    {
        return 'E';
    }

    @Override
    public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
    {
        return new EventChunkBuilder(dicts);
    }

    /**
     * Serializes a sealed {@link EventChunk} to binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header: type code ('E'), row count (int), min timestamp (long), max timestamp (long).</li>
     *   <li>Bitmaps: event name IDs, agent/testCase IDs.</li>
     *   <li>Compressed arrays: time offsets, event name IDs, agent IDs, message string IDs.</li>
     * </ol>
     *
     * @param chunk
     *            sealed {@link EventChunk}
     * @param out
     *            binary output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void writeChunk(final Chunk chunk, final DataOutput out) throws IOException
    {
        final EventChunk c = (EventChunk) chunk;

        // 1. Chunk header metadata
        out.writeChar('E');
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
        writeIntArray(c.compMessages, out);
    }

    /**
     * Deserializes a binary stream into a sealed {@link EventChunk}.
     *
     * @param in
     *            binary input stream
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link EventChunk}
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
        final int[] compMessages = readIntArray(in);

        return new EventChunk(rowCount, minTime, maxTime, timerIds, agentTestCaseIds,
                              compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compMessages);
    }

    /**
     * Decompresses columnar arrays and scans event records against the query predicate.
     *
     * @param chunk
     *            the {@link EventChunk} to scan
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

        final EventChunk c = (EventChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] messages = FastIntegerCodec.decompress(c.compMessages);

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

            final EventData event = new EventData();
            event.setTime(time);
            event.setName(timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null);

            final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
            if (pair != null)
            {
                event.setAgentName(pair.agentName());
                event.setTransactionName(pair.testCaseName());
                event.setTestCaseName(pair.testCaseName());
            }

            final int msgId = messages[i];
            event.setMessage(msgId >= 0 && msgId < strings.length ? strings[msgId] : null);

            consumer.accept(event);
        }
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
    // EventChunkBuilder
    // -------------------------------------------------------------------------

    /**
     * Mutable accumulation buffer for collecting and compressing {@link EventData} records into an {@link EventChunk}.
     */
    private static class EventChunkBuilder implements ChunkBuilder
    {
        private final GlobalDictionaries dicts;

        private int count = 0;
        private long minTime = Long.MAX_VALUE;
        private long maxTime = Long.MIN_VALUE;

        private final long[] rawTimes = new long[DEFAULT_CHUNK_CAPACITY];
        private final int[] timerIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] agentTestCaseIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] msgArr = new int[DEFAULT_CHUNK_CAPACITY];

        private final RoaringBitmap timerIds = new RoaringBitmap();
        private final RoaringBitmap agentTestCaseIds = new RoaringBitmap();

        EventChunkBuilder(final GlobalDictionaries dicts)
        {
            this.dicts = dicts;
        }

        @Override
        public char getTypeCode()
        {
            return 'E';
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

            final EventData r = (EventData) record;
            final int idx = count;

            final long t = r.getTime();
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
            rawTimes[idx] = t;

            final int tid = dicts.getOrCreateTimerNameId(r.getName());
            timerIdArr[idx] = tid;
            timerIds.add(tid);

            final String testCase = r.getTestCaseName() != null ? r.getTestCaseName() : r.getTransactionName();
            final int aid = dicts.getOrCreateAgentTestCaseId(r.getAgentName(), testCase);
            agentTestCaseIdArr[idx] = aid;
            agentTestCaseIds.add(aid);

            msgArr[idx] = dicts.getOrCreateStringId(r.getMessage());

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
            final int[] finalMessages = size == msgArr.length ? msgArr : Arrays.copyOf(msgArr, size);

            final int[] compTimeOffsets = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerNameIds = FastIntegerCodec.compress(finalTimerIds);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(finalAgentIds);
            final int[] compMessages = FastIntegerCodec.compress(finalMessages);

            return new EventChunk(size, minTime, maxTime, timerIds, agentTestCaseIds,
                                  compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compMessages);
        }
    }
}
