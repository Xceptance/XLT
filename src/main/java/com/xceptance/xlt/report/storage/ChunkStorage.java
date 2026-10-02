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
package com.xceptance.xlt.report.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Consumer;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.report.storage.catalog.ChunkCatalog;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.ChunkSpooler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeHandler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeRegistry;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Top-level container and query engine interface for ChunkDB.
 * <p>
 * {@link ChunkStorage} unifies:
 * <ul>
 *   <li>{@link GlobalDictionaries}: Centralized 16-bit interning for high-frequency string attributes.</li>
 *   <li>{@link ChunkCatalog}: Master inventory of columnar chunks, min/max time bounds, and inverted bitmap indexes.</li>
 *   <li>{@link ChunkTypeRegistry}: Registry providing specialized SIMD encoders/decoders for each record type.</li>
 * </ul>
 * <p>
 * <b>On-Disk Binary Layout:</b>
 * When persisted to a cache directory, {@link ChunkStorage} writes two compact binary files:
 * <ol>
 *   <li>{@code dictionaries.bin}: UTF string tables and unified (agent, testCase) pair mappings.</li>
 *   <li>{@code chunks.bin}: Global timestamp headers, total record count, and serialized columnar chunks
 *       with FastPFOR-compressed column vectors and RoaringBitmap index maps.</li>
 * </ol>
 */
public class ChunkStorage implements AutoCloseable
{
    /** Binary filename for the interned dictionary tables. */
    public static final String DICTIONARIES_FILE_NAME = "dictionaries.bin";

    /** Binary filename for the global chunk catalog and compressed columnar data blocks. */
    public static final String CHUNKS_FILE_NAME = "chunks.bin";

    /** Interned dictionary tables for strings, timer names, and agent/test case pairs. */
    private final GlobalDictionaries dictionaries;

    /** Inventory of sealed chunks with time boundaries and inverted index bitmaps. */
    private final ChunkCatalog catalog;

    /** Registry mapping single-character type codes ('R', 'T', 'A', etc.) to codec handlers. */
    private final ChunkTypeRegistry registry;

    /** The underlying chunks.bin file, or null if strictly in-memory. */
    private final File chunksFile;

    /** Dedicated read-only FileChannel for thread-safe concurrent positional reads. */
    private final FileChannel readChannel;

    /** Whether chunks and dictionaries were already persisted directly to disk. */
    private boolean isPersisted;

    /**
     * Constructs a new empty {@link ChunkStorage} initialized with default dictionaries,
     * an empty catalog, and the singleton {@link ChunkTypeRegistry}.
     */
    public ChunkStorage()
    {
        this(new GlobalDictionaries(), new ChunkCatalog(), ChunkTypeRegistry.getInstance(), null, null, false);
    }

    /**
     * Constructs a {@link ChunkStorage} instance with the specified components.
     *
     * @param dictionaries
     *            interned global dictionaries
     * @param catalog
     *            chunk catalog
     * @param registry
     *            chunk type codec registry
     */
    public ChunkStorage(final GlobalDictionaries dictionaries, final ChunkCatalog catalog, final ChunkTypeRegistry registry)
    {
        this(dictionaries, catalog, registry, null, null, false);
    }

    /**
     * Constructs a {@link ChunkStorage} instance with an associated file and channel for lazy chunk reads.
     *
     * @param dictionaries
     *            interned global dictionaries
     * @param catalog
     *            chunk catalog
     * @param registry
     *            chunk type codec registry
     * @param chunksFile
     *            the chunks.bin file on disk (or null)
     * @param readChannel
     *            open FileChannel for positional chunk loading (or null)
     * @param isPersisted
     *            true if chunks and dictionaries are already stored on disk
     */
    public ChunkStorage(final GlobalDictionaries dictionaries, final ChunkCatalog catalog, final ChunkTypeRegistry registry,
                        final File chunksFile, final FileChannel readChannel, final boolean isPersisted)
    {
        this.dictionaries = dictionaries;
        this.catalog = catalog;
        this.registry = registry;
        this.chunksFile = chunksFile;
        this.readChannel = readChannel;
        this.isPersisted = isPersisted;

        // Wire catalog to this storage instance so DiskChunk instances can load payloads
        this.catalog.attachStorage(this);
    }

    /**
     * Returns the global dictionaries associated with this storage instance.
     *
     * @return global dictionaries
     */
    public GlobalDictionaries getDictionaries()
    {
        return dictionaries;
    }

    /**
     * Returns the chunk catalog associated with this storage instance.
     *
     * @return chunk catalog
     */
    public ChunkCatalog getCatalog()
    {
        return catalog;
    }

    /**
     * Returns the chunk type registry used for encoding and decoding chunk types.
     *
     * @return chunk type registry
     */
    public ChunkTypeRegistry getRegistry()
    {
        return registry;
    }

    /**
     * Returns the earliest record timestamp across all chunks in this storage.
     *
     * @return minimum timestamp in milliseconds since epoch
     */
    public long getMinTime()
    {
        return catalog.getGlobalMinTime();
    }

    /**
     * Returns the latest record timestamp across all chunks in this storage.
     *
     * @return maximum timestamp in milliseconds since epoch
     */
    public long getMaxTime()
    {
        return catalog.getGlobalMaxTime();
    }

    /**
     * Returns the total number of records across all chunks in this storage.
     *
     * @return total row count
     */
    public long getTotalRowCount()
    {
        return catalog.getTotalRowCount();
    }

    /**
     * Executes a fast, pruned scan of chunks matching the specified record type code and predicate.
     * <p>
     * Only candidate chunks that pass time boundary checks and bitwise index intersections are decompressed
     * and iterated.
     *
     * @param typeCode
     *            single-character record type code (e.g. 'R' for requests, 'T' for transactions)
     * @param predicate
     *            filter predicate specifying time windows and optional bitmap filters
     * @param consumer
     *            callback receiving matching, reconstituted {@link Data} records
     */
    public void scan(final char typeCode, final ScanPredicate predicate, final Consumer<Data> consumer)
    {
        // 1. Prune candidate chunks via catalog index
        final List<Chunk> matchingChunks = catalog.queryChunks(typeCode, predicate);
        final ChunkTypeHandler handler = registry.getHandler(typeCode);

        // 2. Scan and decompress matching chunks
        for (final Chunk chunk : matchingChunks)
        {
            handler.scan(chunk, predicate, consumer, dictionaries);
        }
    }

    /**
     * Scans all records across all types in this storage, passing each matching record to the consumer.
     *
     * @param predicate
     *            filter predicate
     * @param consumer
     *            callback receiving matching records
     */
    public void scanAll(final ScanPredicate predicate, final Consumer<Data> consumer)
    {
        for (final Chunk chunk : catalog.getAllChunks())
        {
            if (predicate.mayMatchChunk(chunk))
            {
                final ChunkTypeHandler handler = registry.getHandler(chunk.getTypeCode());
                handler.scan(chunk, predicate, consumer, dictionaries);
            }
        }
    }

    /**
     * Checks whether this storage instance has already been persisted to disk.
     * When true, callers such as {@link com.xceptance.xlt.report.storage.cache.CacheManager}
     * can bypass redundant full-storage serialization.
     *
     * @return true if already persisted on disk, false otherwise
     */
    public boolean isPersisted()
    {
        return isPersisted;
    }

    /**
     * Reads a slice of chunk payload bytes directly from disk without altering channel position.
     * <p>
     * This method is thread-safe and allows multiple concurrent reader threads to stream chunk
     * data in parallel without synchronization or locking.
     *
     * @param fileOffset
     *            absolute byte offset in the chunks file
     * @param payloadLength
     *            length of the compressed chunk payload in bytes
     * @return freshly allocated byte array containing the chunk payload
     * @throws IOException
     *             if an I/O error occurs or EOF is reached prematurely
     */
    public byte[] readChunkBytes(final long fileOffset, final int payloadLength) throws IOException
    {
        final byte[] bytes = new byte[payloadLength];
        final ByteBuffer buf = ByteBuffer.wrap(bytes);

        if (readChannel != null)
        {
            // Thread-safe positional read via NIO FileChannel
            long currentPos = fileOffset;
            while (buf.hasRemaining())
            {
                final int read = readChannel.read(buf, currentPos);
                if (read < 0)
                {
                    throw new EOFException("Premature EOF encountered while reading chunk payload at offset " + currentPos);
                }
                currentPos += read;
            }
            return bytes;
        }
        else if (chunksFile != null)
        {
            // Fallback to RandomAccessFile if readChannel was not supplied
            try (final RandomAccessFile raf = new RandomAccessFile(chunksFile, "r"))
            {
                raf.seek(fileOffset);
                raf.readFully(bytes);
            }
            return bytes;
        }
        else
        {
            throw new IOException("Cannot read chunk payload from disk: no underlying file or channel is configured");
        }
    }

    /**
     * Closes any underlying open file channel associated with this storage instance.
     *
     * @throws IOException
     *             if an I/O error occurs during close
     */
    @Override
    public void close() throws IOException
    {
        if (readChannel != null && readChannel.isOpen())
        {
            readChannel.close();
        }
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    /**
     * Serializes this storage to the target cache directory, writing {@code dictionaries.bin}
     * and {@code chunks.bin}.
     * <p>
     * If this storage instance was already spooled directly to disk during parsing
     * (i.e. {@link #isPersisted()} is true), this method is a no-op to prevent redundant I/O.
     *
     * @param dir
     *            target directory on disk
     * @throws IOException
     *             if an I/O error occurs during write
     */
    public void saveToDirectory(final File dir) throws IOException
    {
        // Skip redundant write if data was spooled directly to disk
        if (isPersisted)
        {
            return;
        }

        if (!dir.exists())
        {
            dir.mkdirs();
        }

        // Write directly using ChunkSpooler to produce the unified V2 format with tail index table
        final ChunkSpooler spooler = new ChunkSpooler(dir);
        final List<Chunk> allChunks = catalog.getAllChunks();
        for (final Chunk c : allChunks)
        {
            final ChunkTypeHandler handler = registry.getHandler(c.getTypeCode());
            spooler.spoolChunk(c, handler);
        }
        spooler.finish(catalog, dictionaries);
        isPersisted = true;
    }

    /**
     * Loads and deserializes a {@link ChunkStorage} instance from the specified cache directory.
     * <p>
     * Opens an active read-only {@link FileChannel} for zero-copy, lazy positional chunk loading.
     *
     * @param dir
     *            the directory containing {@code dictionaries.bin} and {@code chunks.bin}
     * @return reconstructed and initialized {@link ChunkStorage} instance
     * @throws IOException
     *             if files are missing or an I/O error occurs during read
     */
    public static ChunkStorage loadFromDirectory(final File dir) throws IOException
    {
        final File dictFile = new File(dir, DICTIONARIES_FILE_NAME);
        final File chunksFile = new File(dir, CHUNKS_FILE_NAME);

        // Verify required binary files exist
        if (!dictFile.exists() || !chunksFile.exists())
        {
            throw new IOException("Missing cache data files in " + dir);
        }

        // 1. Read global dictionaries first so string/timer IDs can be resolved during chunk catalog load
        final GlobalDictionaries dicts;
        try (final DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(dictFile))))
        {
            dicts = GlobalDictionaries.readFrom(in);
        }

        // 2. Read chunk catalog (utilizes fast index seeking if V2, or falls back to V1 sequential parse)
        final ChunkTypeRegistry reg = ChunkTypeRegistry.getInstance();
        final ChunkCatalog cat = ChunkCatalog.readFrom(chunksFile, dicts, reg);

        // 3. Open shared read-only FileChannel for non-blocking concurrent reads of chunk payloads
        final FileChannel channel = FileChannel.open(chunksFile.toPath(), StandardOpenOption.READ);
        return new ChunkStorage(dicts, cat, reg, chunksFile, channel, true);
    }
}
