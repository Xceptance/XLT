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
package com.xceptance.xlt.report.storage.catalog;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.report.storage.ChunkStorage;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeHandler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeRegistry;
import com.xceptance.xlt.report.storage.chunk.DiskChunk;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Central catalog maintaining global test run boundaries, chunk inventory, type partitions,
 * and inverted {@link RoaringBitmap} indexes for rapid query pruning in ChunkDB.
 * <p>
 * <b>Indexing &amp; Pruning Architecture:</b>
 * <ul>
 *   <li><b>Type Partitioning:</b> Chunks are partitioned by record type code (e.g. 'R' for requests,
 *       'T' for transactions, 'A' for actions). When a query only targets requests, non-request chunks
 *       are skipped in \(O(1)\) time without inspecting individual chunk metadata.</li>
 *   <li><b>Min/Max Timestamp Bounds:</b> Each chunk records its earliest and latest millisecond
 *       timestamps. Queries with time range filters \([t_{\text{start}}, t_{\text{end}}]\) instantly
 *       reject any chunk where \(t_{\text{chunkMax}} < t_{\text{start}}\) or \(t_{\text{chunkMin}} > t_{\text{end}}\).</li>
 *   <li><b>Inverted Bitmap Indexes:</b> Maps each unique timerNameId and agentTestCaseId to a
 *       {@link RoaringBitmap} containing the indices of chunks containing that ID. This allows
 *       multi-predicate queries to perform bitwise intersections to identify matching chunks
 *       before touching any columnar data.</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b>
 * Ingestion and catalog population are synchronized during chunk addition. Query operations and
 * lookups access thread-safe collections ({@link CopyOnWriteArrayList} and {@link ConcurrentHashMap})
 * allowing concurrent readers to query the catalog without blocking.
 */
public class ChunkCatalog
{
    /** Magic header identifier (0x584C5443 = 'XLTC' in ASCII hex). */
    public static final int MAGIC = 0x584C5443;

    /** Binary catalog format version. */
    public static final int VERSION = 2;

    /**
     * Standard fixed header size in bytes for the V2 catalog format.
     * <p>
     * Exact binary layout (44 bytes total):
     * <ul>
     *   <li>{@link #MAGIC}: 4 bytes (int) at offset 0</li>
     *   <li>{@link #VERSION}: 4 bytes (int) at offset 4</li>
     *   <li>{@code globalMinTime}: 8 bytes (long) at offset 8</li>
     *   <li>{@code globalMaxTime}: 8 bytes (long) at offset 16</li>
     *   <li>{@code totalRowCount}: 8 bytes (long) at offset 24</li>
     *   <li>{@code chunkCount}: 4 bytes (int) at offset 32</li>
     *   <li>{@code metadataIndexOffset}: 8 bytes (long) at offset 36</li>
     * </ul>
     */
    public static final int HEADER_SIZE = 44;

    /** Earliest timestamp (milliseconds since epoch) across all ingested records. */
    private long globalMinTime = Long.MAX_VALUE;

    /** Latest timestamp (milliseconds since epoch) across all ingested records. */
    private long globalMaxTime = Long.MIN_VALUE;

    /** Total count of records across all ingested chunks. */
    private long totalRowCount = 0;

    /** Master list of all chunks in ingestion sequence. */
    private final List<Chunk> chunks = Collections.synchronizedList(new ArrayList<>());

    /** Chunks grouped by record type code ('R', 'T', 'A', etc.) for instant type filtering. */
    private final Map<Character, List<Chunk>> chunksByType = new ConcurrentHashMap<>();

    /** Inverted index: timerNameId -> RoaringBitmap of chunk indices containing this timer. */
    private final Map<Integer, RoaringBitmap> timerToChunksIndex = new ConcurrentHashMap<>();

    /** Inverted index: agentTestCaseId -> RoaringBitmap of chunk indices containing this pair. */
    private final Map<Integer, RoaringBitmap> agentTestCaseToChunksIndex = new ConcurrentHashMap<>();

    /** Optional reference to the enclosing ChunkStorage instance for shared disk read channels. */
    private volatile ChunkStorage storage;

    /**
     * Constructs a new, empty {@link ChunkCatalog}.
     */
    public ChunkCatalog()
    {
    }

    /**
     * Attaches an enclosing {@link ChunkStorage} instance to this catalog and connects it to
     * all registered {@link DiskChunk} descriptors.
     *
     * @param storage
     *            the storage instance providing shared file channels
     */
    public synchronized void attachStorage(final ChunkStorage storage)
    {
        this.storage = storage;
        for (final Chunk c : chunks)
        {
            if (c instanceof DiskChunk dc)
            {
                dc.setStorage(storage);
            }
        }
    }

    /**
     * Appends a sealed, immutable {@link Chunk} to the catalog, updating global time bounds,
     * total row count, type partitions, and inverted bitmap indexes.
     *
     * @param chunk
     *            the sealed chunk to register
     */
    public synchronized void addChunk(final Chunk chunk)
    {
        final int chunkIndex = chunks.size();
        chunks.add(chunk);

        if (chunk instanceof DiskChunk dc && storage != null)
        {
            dc.setStorage(storage);
        }

        // Group into type partition
        chunksByType.computeIfAbsent(chunk.getTypeCode(), k -> Collections.synchronizedList(new ArrayList<>())).add(chunk);

        // Update global min/max timestamps
        final long cMin = chunk.getMinTime();
        final long cMax = chunk.getMaxTime();
        if (cMin < globalMinTime)
        {
            globalMinTime = cMin;
        }
        if (cMax > globalMaxTime)
        {
            globalMaxTime = cMax;
        }
        totalRowCount += chunk.getRowCount();

        // Index timer IDs present in this chunk into inverted bitmap index
        final RoaringBitmap timers = chunk.getTimerNameIds();
        if (timers != null)
        {
            for (final int timerId : timers)
            {
                timerToChunksIndex.computeIfAbsent(timerId, k -> new RoaringBitmap()).add(chunkIndex);
            }
        }

        // Index agent + test case IDs present in this chunk into inverted bitmap index
        final RoaringBitmap agents = chunk.getAgentTestCaseIds();
        if (agents != null)
        {
            for (final int agentId : agents)
            {
                agentTestCaseToChunksIndex.computeIfAbsent(agentId, k -> new RoaringBitmap()).add(chunkIndex);
            }
        }
    }

    /**
     * Atomically replaces a chunk at the specified catalog position, updating internal type partitions.
     *
     * @param index
     *            catalog index to replace
     * @param newChunk
     *            the replacement disk-backed chunk descriptor
     */
    public synchronized void replaceChunk(final int index, final DiskChunk newChunk)
    {
        final Chunk old = chunks.set(index, newChunk);
        if (storage != null)
        {
            newChunk.setStorage(storage);
        }
        final List<Chunk> typeList = chunksByType.get(old.getTypeCode());
        if (typeList != null)
        {
            final int tIdx = typeList.indexOf(old);
            if (tIdx >= 0)
            {
                typeList.set(tIdx, newChunk);
            }
        }
    }

    /**
     * Returns the earliest record timestamp across the entire dataset.
     *
     * @return global minimum timestamp in milliseconds since epoch, or 0 if catalog is empty
     */
    public long getGlobalMinTime()
    {
        return totalRowCount == 0 ? 0 : globalMinTime;
    }

    /**
     * Returns the latest record timestamp across the entire dataset.
     *
     * @return global maximum timestamp in milliseconds since epoch, or 0 if catalog is empty
     */
    public long getGlobalMaxTime()
    {
        return totalRowCount == 0 ? 0 : globalMaxTime;
    }

    /**
     * Returns the total number of records across all registered chunks.
     *
     * @return total record row count
     */
    public long getTotalRowCount()
    {
        return totalRowCount;
    }

    /**
     * Returns the total number of chunks currently held in the catalog.
     *
     * @return chunk count
     */
    public int getChunkCount()
    {
        return chunks.size();
    }

    /**
     * Returns an unmodifiable view of all chunks across all record types.
     *
     * @return immutable list of all registered chunks
     */
    public List<Chunk> getAllChunks()
    {
        return Collections.unmodifiableList(chunks);
    }

    /**
     * Returns all chunks matching a specific record type code (e.g. 'R', 'T', 'A').
     *
     * @param typeCode
     *            the single-character type code
     * @return list of chunks for that type, or an empty list if none exist
     */
    public List<Chunk> getChunksForType(final char typeCode)
    {
        final List<Chunk> list = chunksByType.get(typeCode);
        return list != null ? list : Collections.emptyList();
    }

    /**
     * Queries chunks of a specific record type code that satisfy the given {@link ScanPredicate}.
     * Chunks outside the predicate's time window or matching no filter criteria are pruned.
     *
     * @param typeCode
     *            single-character record type code
     * @param predicate
     *            scan filter predicate with time ranges and optional bitmap filters
     * @return filtered list of candidate chunks ready for parallel decompression and scanning
     */
    public List<Chunk> queryChunks(final char typeCode, final ScanPredicate predicate)
    {
        final List<Chunk> list = chunksByType.get(typeCode);
        if (list == null || list.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<Chunk> matched = new ArrayList<>(list.size());
        for (final Chunk c : list)
        {
            // Prune chunk if outside time range or disjoint from ID filter bitmaps
            if (predicate.mayMatchChunk(c))
            {
                matched.add(c);
            }
        }
        return matched;
    }

    // -------------------------------------------------------------------------
    // Deserialization (V2 Format with tail index table)
    // -------------------------------------------------------------------------

    /**
     * Reads a {@link ChunkCatalog} from disk using the V2 binary format.
     * <p>
     * Seeks directly to the metadata index table and instantly instantiates {@link DiskChunk}
     * descriptors, completing in ~1-2 milliseconds without loading or decompressing columnar data.
     *
     * @param chunksFile
     *            the binary chunks file on disk
     * @param dicts
     *            the global dictionaries for resolving interned IDs
     * @param registry
     *            the chunk type registry to locate specialized handlers for each chunk type
     * @return fully reconstructed {@link ChunkCatalog} instance
     * @throws IOException
     *             if an I/O error occurs or the file is not in valid V2 ChunkDB format
     */
    public static ChunkCatalog readFrom(final File chunksFile, final GlobalDictionaries dicts, final ChunkTypeRegistry registry) throws IOException
    {
        try (final RandomAccessFile raf = new RandomAccessFile(chunksFile, "r"))
        {
            if (raf.length() < HEADER_SIZE)
            {
                throw new IOException("Corrupted ChunkDB file " + chunksFile + ": file size less than header size " + HEADER_SIZE);
            }
            final int magic = raf.readInt();
            if (magic != MAGIC)
            {
                throw new IOException(String.format("Invalid ChunkDB file format in %s: magic 0x%08X != 0x%08X", chunksFile, magic, MAGIC));
            }
            final int version = raf.readInt();
            if (version != VERSION)
            {
                throw new IOException(String.format("Unsupported ChunkDB file version in %s: version %d != %d", chunksFile, version, VERSION));
            }
            final ChunkCatalog catalog = new ChunkCatalog();
            catalog.globalMinTime = raf.readLong();
            catalog.globalMaxTime = raf.readLong();
            catalog.totalRowCount = raf.readLong();
            final int chunkCount = raf.readInt();
            final long metadataIndexOffset = raf.readLong();

            // Seek to metadata index table
            raf.seek(metadataIndexOffset);
            final InputStream inStream = Channels.newInputStream(raf.getChannel());
            final DataInput in = new DataInputStream(new BufferedInputStream(inStream));
            final int indexChunkCount = in.readInt();

            for (int i = 0; i < indexChunkCount; i++)
            {
                final long offset = in.readLong();
                final int payloadLength = in.readInt();
                final char typeCode = in.readChar();
                final int rowCount = in.readInt();
                final long minTime = in.readLong();
                final long maxTime = in.readLong();

                final boolean hasTimers = in.readBoolean();
                final RoaringBitmap timers;
                if (hasTimers)
                {
                    timers = new RoaringBitmap();
                    timers.deserialize(in);
                }
                else
                {
                    timers = new RoaringBitmap();
                }

                final boolean hasAgents = in.readBoolean();
                final RoaringBitmap agents;
                if (hasAgents)
                {
                    agents = new RoaringBitmap();
                    agents.deserialize(in);
                }
                else
                {
                    agents = new RoaringBitmap();
                }

                final DiskChunk chunk = new DiskChunk(typeCode, rowCount, minTime, maxTime, timers, agents, offset, payloadLength, chunksFile);
                catalog.chunks.add(chunk);
                catalog.chunksByType.computeIfAbsent(typeCode, k -> Collections.synchronizedList(new ArrayList<>())).add(chunk);

                for (final int timerId : timers)
                {
                    catalog.timerToChunksIndex.computeIfAbsent(timerId, k -> new RoaringBitmap()).add(i);
                }
                for (final int agentId : agents)
                {
                    catalog.agentTestCaseToChunksIndex.computeIfAbsent(agentId, k -> new RoaringBitmap()).add(i);
                }
            }

            return catalog;
        }
    }
}
