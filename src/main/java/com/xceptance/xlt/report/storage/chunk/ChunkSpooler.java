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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.report.storage.ChunkStorage;
import com.xceptance.xlt.report.storage.catalog.ChunkCatalog;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;

/**
 * High-performance background chunk spooler streaming sealed columnar chunks directly to disk
 * during CSV parsing to eliminate heap memory bloat on massive load test datasets.
 * <p>
 * <b>Direct-to-Disk Architecture:</b>
 * Rather than accumulating thousands of sealed chunks and gigabytes of columnar arrays in the JVM heap
 * throughout a cold run, this spooler writes each sealed chunk directly to a temporary binary file
 * ({@code chunks.bin.tmp}) as soon as compression completes. Upon completing parser execution, the spooler
 * flushes any remaining partial chunks, appends the metadata index table, writes the 40-byte catalog header,
 * and atomically promotes the file to {@code chunks.bin}.
 * <p>
 * This guarantees that heap consumption during parsing remains constant and bounded to active builders (< 80 MB),
 * completely preventing garbage collection thrashing and {@link OutOfMemoryError} on 100+ million record datasets.
 */
public class ChunkSpooler
{
    /** Target cache directory. */
    private final File cacheDir;

    /** Temporary working file during parsing. */
    private final File tmpFile;

    /** Final binary chunks file. */
    private final File targetFile;

    /** Underlying random access file handle. */
    private final RandomAccessFile raf;

    /** Dedicated NIO channel for sequential block writes. */
    private final FileChannel channel;

    /** Current write position in the storage file. */
    private long currentOffset;

    /** Guard flag preventing double finalization. */
    private boolean closed = false;

    /**
     * Constructs a new {@link ChunkSpooler} targeting the specified cache directory.
     *
     * @param cacheDir
     *            target cache directory on disk
     * @throws IOException
     *             if an I/O error occurs while creating or opening the temporary chunks file
     */
    public ChunkSpooler(final File cacheDir) throws IOException
    {
        this.cacheDir = cacheDir;
        if (!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }

        this.tmpFile = new File(cacheDir, ChunkStorage.CHUNKS_FILE_NAME + ".tmp");
        this.targetFile = new File(cacheDir, ChunkStorage.CHUNKS_FILE_NAME);

        if (tmpFile.exists())
        {
            tmpFile.delete();
        }

        this.raf = new RandomAccessFile(tmpFile, "rw");
        this.channel = raf.getChannel();

        // Reserve 40-byte header space at offset 0
        final ByteBuffer zeroHeader = ByteBuffer.allocate(ChunkCatalog.HEADER_SIZE);
        channel.write(zeroHeader, 0);
        this.currentOffset = ChunkCatalog.HEADER_SIZE;
    }

    /**
     * Appends a serialized chunk binary payload to the disk file and constructs a corresponding
     * lightweight {@link DiskChunk} descriptor.
     *
     * @param typeCode
     *            single-character record type code ('R', 'T', 'A', etc.)
     * @param rowCount
     *            number of records in chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of contained timer name IDs
     * @param agentTestCaseIds
     *            bitmap of contained agent/test case IDs
     * @param payload
     *            serialized binary chunk payload bytes
     * @return lightweight {@link DiskChunk} referencing the written file coordinates
     * @throws IOException
     *             if an I/O error occurs during write
     */
    public synchronized DiskChunk spoolChunk(final char typeCode, final int rowCount,
                                             final long minTime, final long maxTime,
                                             final RoaringBitmap timerNameIds,
                                             final RoaringBitmap agentTestCaseIds,
                                             final byte[] payload) throws IOException
    {
        final long offset = currentOffset;
        final ByteBuffer buf = ByteBuffer.wrap(payload);
        while (buf.hasRemaining())
        {
            final int written = channel.write(buf, currentOffset);
            currentOffset += written;
        }

        return new DiskChunk(typeCode, rowCount, minTime, maxTime,
                             timerNameIds, agentTestCaseIds,
                             offset, payload.length, targetFile);
    }

    /**
     * Serializes a sealed in-memory chunk using its handler, streams the compressed payload
     * directly to disk, and returns a lightweight {@link DiskChunk} descriptor.
     *
     * @param chunk
     *            the in-memory chunk to serialize and spool
     * @param handler
     *            the chunk type handler responsible for binary serialization
     * @return lightweight disk-backed chunk descriptor
     * @throws IOException
     *             if an I/O error occurs during serialization or disk write
     */
    public DiskChunk spoolChunk(final Chunk chunk, final ChunkTypeHandler handler) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(4096, chunk.getRowCount() * 4));
        final DataOutputStream dos = new DataOutputStream(baos);
        handler.writeChunk(chunk, dos);
        dos.flush();
        final byte[] payload = baos.toByteArray();

        return spoolChunk(chunk.getTypeCode(), chunk.getRowCount(),
                          chunk.getMinTime(), chunk.getMaxTime(),
                          chunk.getTimerNameIds(), chunk.getAgentTestCaseIds(),
                          payload);
    }

    /**
     * Finalizes the chunk storage file by appending the metadata index table, updating the 40-byte
     * global header at offset 0, atomically renaming {@code chunks.bin.tmp} to {@code chunks.bin},
     * and writing {@code dictionaries.bin}.
     *
     * @param catalog
     *            the catalog containing all registered chunk descriptors and time bounds
     * @param dicts
     *            the global dictionaries to serialize
     * @throws IOException
     *             if an I/O error occurs
     */
    public synchronized void finish(final ChunkCatalog catalog, final GlobalDictionaries dicts) throws IOException
    {
        if (closed)
        {
            return;
        }

        // 1. Spool any lingering in-memory chunks to disk so all chunks are uniformly persisted
        final List<Chunk> allChunks = catalog.getAllChunks();
        final List<DiskChunk> diskChunks = new ArrayList<>(allChunks.size());
        for (int i = 0; i < allChunks.size(); i++)
        {
            final Chunk c = allChunks.get(i);
            if (c instanceof DiskChunk dc)
            {
                diskChunks.add(dc);
            }
            else
            {
                final ChunkTypeHandler handler = ChunkTypeRegistry.getInstance().getHandler(c.getTypeCode());
                final DiskChunk dc = spoolChunk(c, handler);
                catalog.replaceChunk(i, dc);
                diskChunks.add(dc);
            }
        }

        // 2. Write metadata index table starting at currentOffset
        final long metadataIndexOffset = currentOffset;
        final ByteArrayOutputStream metaBaos = new ByteArrayOutputStream(Math.max(4096, diskChunks.size() * 128));
        final DataOutputStream metaDos = new DataOutputStream(metaBaos);

        metaDos.writeInt(diskChunks.size());
        for (final DiskChunk dc : diskChunks)
        {
            metaDos.writeLong(dc.getFileOffset());
            metaDos.writeInt(dc.getPayloadLength());
            metaDos.writeChar(dc.getTypeCode());
            metaDos.writeInt(dc.getRowCount());
            metaDos.writeLong(dc.getMinTime());
            metaDos.writeLong(dc.getMaxTime());

            final RoaringBitmap timers = dc.getTimerNameIds();
            if (timers != null)
            {
                metaDos.writeBoolean(true);
                timers.serialize(metaDos);
            }
            else
            {
                metaDos.writeBoolean(false);
            }

            final RoaringBitmap agents = dc.getAgentTestCaseIds();
            if (agents != null)
            {
                metaDos.writeBoolean(true);
                agents.serialize(metaDos);
            }
            else
            {
                metaDos.writeBoolean(false);
            }
        }
        metaDos.flush();

        final ByteBuffer metaBuf = ByteBuffer.wrap(metaBaos.toByteArray());
        while (metaBuf.hasRemaining())
        {
            final int written = channel.write(metaBuf, currentOffset);
            currentOffset += written;
        }

        // 3. Write 44-byte header at offset 0
        final ByteBuffer header = ByteBuffer.allocate(ChunkCatalog.HEADER_SIZE);
        header.putInt(ChunkCatalog.MAGIC);
        header.putInt(ChunkCatalog.VERSION);
        header.putLong(catalog.getGlobalMinTime());
        header.putLong(catalog.getGlobalMaxTime());
        header.putLong(catalog.getTotalRowCount());
        header.putInt(diskChunks.size());
        header.putLong(metadataIndexOffset);
        header.flip();

        channel.write(header, 0);
        channel.force(true);

        channel.close();
        raf.close();
        closed = true;

        // 4. Atomically promote tmpFile to targetFile
        try
        {
            Files.move(tmpFile.toPath(), targetFile.toPath(),
                       StandardCopyOption.ATOMIC_MOVE,
                       StandardCopyOption.REPLACE_EXISTING);
        }
        catch (final AtomicMoveNotSupportedException e)
        {
            Files.move(tmpFile.toPath(), targetFile.toPath(),
                       StandardCopyOption.REPLACE_EXISTING);
        }

        // 5. Save dictionaries
        final File dictFile = new File(cacheDir, ChunkStorage.DICTIONARIES_FILE_NAME);
        try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dictFile))))
        {
            dicts.writeTo(out);
        }
    }

    /**
     * Returns the final target chunks file location.
     *
     * @return target file
     */
    public File getTargetFile()
    {
        return targetFile;
    }
}
