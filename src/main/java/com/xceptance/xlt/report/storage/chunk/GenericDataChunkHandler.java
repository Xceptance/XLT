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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Generic fallback handler that enables ChunkDB to store and scan any current or future {@link Data} implementation.
 * <p>
 * When a custom data record is encountered whose type code has no specialized handler registered,
 * {@link GenericDataChunkHandler} captures common header attributes (timestamp, name, agent name, test case name)
 * alongside tokenized field columns. During scanning, instances are dynamically instantiated via reflection
 * and populated with their interned values.
 */
public class GenericDataChunkHandler implements ChunkTypeHandler
{
    /** The single-character type code handled by this instance. */
    private final char typeCode;

    /** Reflection target class for dynamically creating instances during query scans. */
    private Class<? extends Data> recordClass;

    /**
     * Constructs a generic handler for the given type code.
     *
     * @param typeCode
     *            custom record type code
     */
    public GenericDataChunkHandler(final char typeCode)
    {
        this.typeCode = typeCode;
    }

    /**
     * Constructs a generic handler with a known target class for instantiation.
     *
     * @param typeCode
     *            custom record type code
     * @param recordClass
     *            target {@link Data} class
     */
    public GenericDataChunkHandler(final char typeCode, final Class<? extends Data> recordClass)
    {
        this.typeCode = typeCode;
        this.recordClass = recordClass;
    }

    /**
     * Configures the target {@link Data} class for dynamic reflection-based instantiation during query scans.
     *
     * @param recordClass
     *            target class
     */
    public void setRecordClass(final Class<? extends Data> recordClass)
    {
        this.recordClass = recordClass;
    }

    @Override
    public char getTypeCode()
    {
        return typeCode;
    }

    @Override
    public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
    {
        return new GenericChunkBuilder(typeCode, dicts);
    }

    /**
     * Serializes a sealed {@link GenericDataChunk} to binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header: type code (char), row count (int), min timestamp (long), max timestamp (long).</li>
     *   <li>Bitmaps: timer/metric name IDs, agent/testCase IDs.</li>
     *   <li>Compressed core arrays: time offsets, timer IDs, agent IDs.</li>
     *   <li>String field columns: column count (int), followed by compressed int arrays for each column.</li>
     * </ol>
     *
     * @param chunk
     *            sealed {@link GenericDataChunk}
     * @param out
     *            binary output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void writeChunk(final Chunk chunk, final DataOutput out) throws IOException
    {
        final GenericDataChunk c = (GenericDataChunk) chunk;

        // 1. Chunk header metadata
        out.writeChar(c.getTypeCode());
        out.writeUTF(recordClass != null ? recordClass.getName() : "");
        out.writeInt(c.getRowCount());
        out.writeLong(c.getMinTime());
        out.writeLong(c.getMaxTime());

        // 2. RoaringBitmap inverted index filters
        c.getTimerNameIds().serialize(out);
        c.getAgentTestCaseIds().serialize(out);

        // 3. Compressed core columnar performance vectors
        writeCompressedIntArray(c.compressedTimes, out);
        writeCompressedIntArray(c.compressedTimerNameIds, out);
        writeCompressedIntArray(c.compressedAgentTestCaseIds, out);

        // 4. Chunk-local string dictionary
        final String[] chunkStrings = c.getChunkStrings();
        out.writeInt(chunkStrings.length);
        for (final String s : chunkStrings)
        {
            out.writeUTF(s);
        }

        // 5. Arbitrary string attribute columns
        final int colCount = c.compressedFieldStringIds.length;
        out.writeInt(colCount);
        for (int i = 0; i < colCount; i++)
        {
            writeCompressedIntArray(c.compressedFieldStringIds[i], out);
        }
    }

    /**
     * Deserializes a binary stream into a sealed {@link GenericDataChunk}.
     *
     * @param in
     *            binary input stream
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link GenericDataChunk}
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public Chunk readChunk(final DataInput in, final GlobalDictionaries dicts) throws IOException
    {
        // 1. Read header metadata
        final char tc = in.readChar();
        final String className = in.readUTF();
        if (!className.isEmpty() && this.recordClass == null)
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<? extends Data> clazz = (Class<? extends Data>) Class.forName(className);
                this.recordClass = clazz;
            }
            catch (final Exception ignored)
            {
            }
        }
        final int rowCount = in.readInt();
        final long minTime = in.readLong();
        final long maxTime = in.readLong();

        // 2. Deserialize RoaringBitmaps
        final RoaringBitmap timerIds = new RoaringBitmap();
        timerIds.deserialize(in);

        final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        agentTestCaseIds.deserialize(in);

        // 3. Read compressed integer column arrays
        final int[] compTimes = readCompressedIntArray(in);
        final int[] compTimerIds = readCompressedIntArray(in);
        final int[] compAgentTestCaseIds = readCompressedIntArray(in);

        // 4. Read chunk-local string dictionary
        final int stringCount = in.readInt();
        final String[] chunkStrings = new String[stringCount];
        for (int i = 0; i < stringCount; i++)
        {
            chunkStrings[i] = in.readUTF();
        }

        // 5. Read arbitrary string attribute columns
        final int colCount = in.readInt();
        final int[][] compFieldIds = new int[colCount][];
        for (int i = 0; i < colCount; i++)
        {
            compFieldIds[i] = readCompressedIntArray(in);
        }

        return new GenericDataChunk(tc, rowCount, minTime, maxTime, timerIds, agentTestCaseIds,
                                    chunkStrings, compTimes, compTimerIds, compAgentTestCaseIds, compFieldIds);
    }

    /**
     * Decompresses columnar arrays and scans generic data records against the query predicate.
     *
     * @param chunk
     *            the {@link GenericDataChunk} to scan
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

        final GenericDataChunk c = (GenericDataChunk) resolveChunk(chunk, dicts);

        // 2. SIMD columnar decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compressedTimes);
        final int[] timerIds = FastIntegerCodec.decompress(c.compressedTimerNameIds);
        final int[] agentIds = FastIntegerCodec.decompress(c.compressedAgentTestCaseIds);

        final int colCount = c.compressedFieldStringIds.length;
        final int[][] fieldStringIds = new int[colCount][];
        for (int i = 0; i < colCount; i++)
        {
            fieldStringIds[i] = FastIntegerCodec.decompress(c.compressedFieldStringIds[i]);
        }

        final long baseTime = c.getMinTime();
        final int rowCount = c.getRowCount();
        final String[] chunkStrings = c.getChunkStrings();

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();

        // Pre-convert chunk strings to UTF-8 byte arrays to eliminate conversions in the row loop
        final byte[][] chunkBytes = new byte[chunkStrings.length][];
        for (int s = 0; s < chunkStrings.length; s++)
        {
            final String str = chunkStrings[s];
            chunkBytes[s] = (str != null) ? str.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }

        final CsvByteColumns csvColumns = new CsvByteColumns(colCount);
        byte[] rowBuffer = new byte[4096];

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

            final Data record = createRecordInstance();
            if (record != null)
            {
                int rowOffset = 0;
                csvColumns.reset(rowBuffer);
                for (int col = 0; col < colCount; col++)
                {
                    final int strId = fieldStringIds[col][i];
                    if (strId > 0 && strId <= chunkBytes.length)
                    {
                        final byte[] b = chunkBytes[strId - 1];
                        if (rowOffset + b.length > rowBuffer.length)
                        {
                            rowBuffer = Arrays.copyOf(rowBuffer, Math.max(rowBuffer.length * 2, rowOffset + b.length));
                            csvColumns.setBuffer(rowBuffer);
                        }
                        System.arraycopy(b, 0, rowBuffer, rowOffset, b.length);
                        csvColumns.add(rowOffset, b.length);
                        rowOffset += b.length;
                    }
                    else
                    {
                        csvColumns.add(rowOffset, 0);
                    }
                }

                record.setBaseValues(csvColumns);
                record.setRemainingValues(csvColumns);
                record.setTime(time);
                record.setName(timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null);

                final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
                if (pair != null)
                {
                    record.setAgentName(pair.agentName());
                    record.setTransactionName(pair.testCaseName());
                }

                consumer.accept(record);
            }
        }
    }


    /**
     * Instantiates the concrete target {@link Data} class via reflection.
     */
    private Data createRecordInstance()
    {
        if (recordClass == null)
        {
            return null;
        }
        try
        {
            return recordClass.getDeclaredConstructor().newInstance();
        }
        catch (final Exception e)
        {
            return null;
        }
    }

    /**
     * Serializes a primitive int array prefixed with its element count.
     */
    private static void writeCompressedIntArray(final int[] array, final DataOutput out) throws IOException
    {
        out.writeInt(array.length);
        for (final int val : array)
        {
            out.writeInt(val);
        }
    }

    /**
     * Deserializes a primitive int array prefixed with its element count.
     */
    private static int[] readCompressedIntArray(final DataInput in) throws IOException
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
    // Builder implementation
    // -------------------------------------------------------------------------

    /**
     * Mutable accumulation buffer for collecting and compressing arbitrary {@link Data} records into a {@link GenericDataChunk}.
     */
    private class GenericChunkBuilder implements ChunkBuilder
    {
        private final char typeCode;
        private final GlobalDictionaries dicts;
        private final List<Data> records = new ArrayList<>(DEFAULT_CHUNK_CAPACITY);

        GenericChunkBuilder(final char typeCode, final GlobalDictionaries dicts)
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
            if (recordClass == null && record != null)
            {
                recordClass = record.getClass();
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
            final int[] timerNameIdArr = new int[size];
            final int[] agentTestCaseIdArr = new int[size];

            // Determine columns from first record
            final int colCount = (size > 0 && records.get(0).toList() != null) ? records.get(0).toList().size() : 0;
            final int[][] colStringIds = new int[colCount][size];

            // Chunk-local dictionary: prevents arbitrary or high-cardinality generic record attributes
            // from overflowing or polluting the global dictionaries
            final Map<String, Integer> chunkLocalStringMap = new HashMap<>();
            final List<String> chunkStringsList = new ArrayList<>();

            // 3. Populate columnar arrays and dictionaries
            for (int i = 0; i < size; i++)
            {
                final Data r = records.get(i);
                timeOffsets[i] = (int) (r.getTime() - minTime);

                final int tid = dicts.getOrCreateTimerNameId(r.getName());
                timerNameIdArr[i] = tid;
                timerIds.add(tid);

                final int aid = dicts.getOrCreateAgentTestCaseId(r.getAgentName(), r.getTransactionName());
                agentTestCaseIdArr[i] = aid;
                agentTestCaseIds.add(aid);

                final List<String> fields = r.toList();
                if (fields != null)
                {
                    for (int c = 0; c < Math.min(colCount, fields.size()); c++)
                    {
                        final String val = fields.get(c);
                        if (val != null)
                        {
                            Integer localId = chunkLocalStringMap.get(val);
                            if (localId == null)
                            {
                                localId = chunkStringsList.size() + 1; // 1-based index (0 reserved for null)
                                chunkStringsList.add(val);
                                chunkLocalStringMap.put(val, localId);
                            }
                            colStringIds[c][i] = localId;
                        }
                        else
                        {
                            colStringIds[c][i] = 0;
                        }
                    }
                }
            }

            // 4. Compress columns via FastPFOR
            final int[] compTimes = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerIds = FastIntegerCodec.compress(timerNameIdArr);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(agentTestCaseIdArr);

            final int[][] compCols = new int[colCount][];
            for (int c = 0; c < colCount; c++)
            {
                compCols[c] = FastIntegerCodec.compress(colStringIds[c]);
            }

            final String[] chunkStrings = chunkStringsList.toArray(new String[chunkStringsList.size()]);
            return new GenericDataChunk(typeCode, size, minTime, maxTime, timerIds, agentTestCaseIds,
                                        chunkStrings, compTimes, compTimerIds, compAgentTestCaseIds, compCols);
        }
    }
}
