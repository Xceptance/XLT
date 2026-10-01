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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.report.storage.ChunkStorage;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;

/**
 * Lightweight, memory-efficient disk-backed descriptor representing an immutable columnar chunk.
 * <p>
 * <b>Zero-Bloat Memory Footprint:</b>
 * Rather than retaining megabytes of compressed integer arrays and strings in the JVM heap,
 * a {@link DiskChunk} holds only essential query metadata:
 * <ul>
 *   <li>Single-character record type code (e.g. 'R', 'T', 'A').</li>
 *   <li>Total row count within this chunk.</li>
 *   <li>Global bounding-box millisecond timestamps ({@code minTime}, {@code maxTime}) for instantaneous range pruning.</li>
 *   <li>Compressed 16-bit {@link RoaringBitmap} sets for timer name IDs and unified agent/test case IDs.</li>
 *   <li>64-bit byte offset and 32-bit payload length within the storage file ({@code chunks.bin}).</li>
 * </ul>
 * Under this model, each chunk consumes only ~80 bytes of heap memory, bounding 1,660 chunks (over 108 million
 * records) to less than 200 kilobytes of JVM memory.
 * <p>
 * <b>Concurrent Zero-Lock Position-Based Disk Reads:</b>
 * During query execution in {@link com.xceptance.xlt.report.ChunkQueryEngine}, candidate chunk pruning operates
 * entirely against the in-memory metadata bitmaps of {@link DiskChunk} without touching the disk. When a chunk
 * matches the query predicate, worker threads call {@link #load(ChunkTypeHandler, GlobalDictionaries)} to read
 * and decompress the chunk's payload via thread-safe, positional {@link FileChannel#read(ByteBuffer, long)}
 * operations. The decompressed arrays and reconstituted records are reclaimed by garbage collection immediately
 * after the chunk's scan completes, keeping active heap consumption bounded to tens of megabytes even during
 * multi-threaded scans of 100+ million records.
 */
public class DiskChunk implements Chunk
{
    /** Single-character record type identifier (e.g. 'R' for requests, 'T' for transactions). */
    private final char typeCode;

    /** Number of logical data records stored within this chunk. */
    private final int rowCount;

    /** Earliest timestamp (milliseconds since epoch) of any record in this chunk. */
    private final long minTime;

    /** Latest timestamp (milliseconds since epoch) of any record in this chunk. */
    private final long maxTime;

    /** Set of unique timer name dictionary IDs present in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** Set of unique agent + test case dictionary IDs present in this chunk. */
    private final RoaringBitmap agentTestCaseIds;

    /** Starting byte offset of this chunk's serialized payload in the binary storage file. */
    private final long fileOffset;

    /** Exact byte length of this chunk's serialized binary payload. */
    private final int payloadLength;

    /** Target binary storage file on disk. */
    private final File storageFile;

    /** Optional reference to the active storage container providing shared, open file channels. */
    private volatile ChunkStorage storage;

    /**
     * Constructs a new {@link DiskChunk} descriptor with the specified boundary metadata and file coordinates.
     *
     * @param typeCode
     *            single-character record type code
     * @param rowCount
     *            number of records in chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of contained timer name IDs (may be {@code null})
     * @param agentTestCaseIds
     *            bitmap of contained agent/test case IDs (may be {@code null})
     * @param fileOffset
     *            starting byte offset in storage file
     * @param payloadLength
     *            exact byte length of the chunk's payload
     * @param storageFile
     *            underlying storage file on disk
     */
    public DiskChunk(final char typeCode, final int rowCount, final long minTime, final long maxTime,
                     final RoaringBitmap timerNameIds, final RoaringBitmap agentTestCaseIds,
                     final long fileOffset, final int payloadLength, final File storageFile)
    {
        this.typeCode = typeCode;
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.timerNameIds = timerNameIds != null ? timerNameIds.clone() : new RoaringBitmap();
        this.agentTestCaseIds = agentTestCaseIds != null ? agentTestCaseIds.clone() : new RoaringBitmap();
        this.fileOffset = fileOffset;
        this.payloadLength = payloadLength;
        this.storageFile = storageFile;
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
     * Returns the byte offset of this chunk in the binary storage file.
     *
     * @return byte offset
     */
    public long getFileOffset()
    {
        return fileOffset;
    }

    /**
     * Returns the serialized byte length of this chunk.
     *
     * @return byte length
     */
    public int getPayloadLength()
    {
        return payloadLength;
    }

    /**
     * Returns the underlying file containing this chunk.
     *
     * @return storage file
     */
    public File getStorageFile()
    {
        return storageFile;
    }

    /**
     * Attaches an active {@link ChunkStorage} container to allow reading via shared open channels.
     *
     * @param storage
     *            active storage instance
     */
    public void setStorage(final ChunkStorage storage)
    {
        this.storage = storage;
    }

    /**
     * Reads and deserializes this chunk from disk using the specified type codec handler and dictionaries.
     * <p>
     * Employs lock-free, positional {@link FileChannel#read(ByteBuffer, long)} when a shared storage channel
     * is available, or an isolated read channel fallback otherwise.
     *
     * @param handler
     *            type-specific codec handler for this chunk's type code
     * @param dicts
     *            global dictionaries for string resolution
     * @return reconstituted in-memory chunk instance
     */
    public Chunk load(final ChunkTypeHandler handler, final GlobalDictionaries dicts)
    {
        try
        {
            final byte[] bytes;
            final ChunkStorage currentStorage = this.storage;
            if (currentStorage != null)
            {
                bytes = currentStorage.readChunkBytes(fileOffset, payloadLength);
            }
            else
            {
                bytes = readBytesDirectly();
            }

            final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
            return handler.readChunk(dis, dicts);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(String.format("Failed to load chunk [type=%c, offset=%d, length=%d] from disk: %s",
                                                     typeCode, fileOffset, payloadLength, storageFile), e);
        }
    }

    /**
     * Reads this chunk's raw payload bytes directly from disk when no shared storage channel is attached.
     *
     * @return serialized payload bytes
     * @throws IOException
     *             if an I/O error occurs
     */
    private byte[] readBytesDirectly() throws IOException
    {
        final byte[] bytes = new byte[payloadLength];
        try (final RandomAccessFile raf = new RandomAccessFile(storageFile, "r");
             final FileChannel channel = raf.getChannel())
        {
            final ByteBuffer buf = ByteBuffer.wrap(bytes);
            long pos = fileOffset;
            while (buf.hasRemaining())
            {
                final int read = channel.read(buf, pos);
                if (read < 0)
                {
                    throw new IOException(String.format("Premature EOF reading chunk [type=%c, offset=%d, length=%d] from %s",
                                                        typeCode, fileOffset, payloadLength, storageFile));
                }
                pos += read;
            }
        }
        return bytes;
    }

    @Override
    public String toString()
    {
        return String.format("DiskChunk[type=%c, rows=%d, time=[%d..%d], offset=%d, len=%d]",
                             typeCode, rowCount, minTime, maxTime, fileOffset, payloadLength);
    }
}
