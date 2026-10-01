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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.util.SimpleArrayList;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.report.storage.catalog.ChunkCatalog;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.ChunkBuilder;
import com.xceptance.xlt.report.storage.chunk.ChunkSpooler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeHandler;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeRegistry;
import com.xceptance.xlt.report.storage.chunk.DiskChunk;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;

/**
 * High-throughput stream collector that accumulates preprocessed {@link Data} records into columnar chunks
 * during Phase 1 ingestion in the report generator.
 * <p>
 * <b>Lock-Free Thread-Local Concurrency Architecture:</b>
 * In high-scale load tests (e.g. tens or hundreds of millions of records parsed across multiple worker threads),
 * acquiring a global monitor lock per record severely serializes ingestion and degrades parser throughput.
 * To achieve maximum multi-threaded scalability:
 * <ol>
 *   <li><b>Thread-Local Builders:</b> Each parser worker thread maintains its own thread-local map of active
 *       {@link ChunkBuilder} instances keyed by single-character type code ('R', 'T', 'A', etc.).</li>
 *   <li><b>Lock-Free Record Appending:</b> Parser threads append records and intern dictionary entries entirely
 *       independently without blocking one another.</li>
 *   <li><b>Direct-to-Disk Spooling:</b> When a cache directory is supplied, sealed columnar chunks are immediately
 *       streamed to disk via {@link ChunkSpooler} in background worker threads, discarding the large in-memory chunk
 *       and retaining only an 80-byte {@link DiskChunk} descriptor in the catalog. This bounds memory during parsing to
 *       active builders (< 80 MB) and eliminates all Full GCs.</li>
 *   <li><b>Parallel Chunk Sealing:</b> When a thread-local builder reaches capacity (typically 64,000 records),
 *       that thread rotates to a fresh builder while the sealed chunk is compressed and spooled asynchronously.</li>
 *   <li><b>Batch List Ingestion:</b> Supports high-efficiency batch ingestion via {@link #collect(List)}, allowing
 *       callers to look up the thread-local builder map only once per batch instead of per record.</li>
 *   <li><b>Concurrent Finalization:</b> Upon completion of all parser tasks, {@link #finish()} drains and seals
 *       all remaining partial chunks across all thread maps concurrently using parallel streams.</li>
 * </ol>
 */
public class ChunkIngestionCollector
{
    /** Global string, timer, and agent/test case dictionaries. */
    private final GlobalDictionaries dictionaries;

    /** Master chunk catalog tracking chunk boundaries and inverted index bitmaps. */
    private final ChunkCatalog catalog;

    /** Registry providing type codecs and builders for each record type. */
    private final ChunkTypeRegistry registry;

    /** Optional background spooler streaming chunks directly to disk to prevent heap accumulation. */
    private final ChunkSpooler spooler;

    /**
     * Master registry tracking all thread-local builder arrays created across worker threads.
     * Used during {@link #finish()} to seal all remaining partially filled builders.
     */
    private final List<ChunkBuilder[]> allThreadBuilders = new CopyOnWriteArrayList<>();

    /**
     * Dedicated background worker pool for compressing sealed chunks asynchronously without stalling parser threads.
     */
    private final ExecutorService compressionExecutor;

    /**
     * Bounded permit semaphore preventing excessive uncompressed chunk accumulation in memory during high parsing load.
     */
    private final Semaphore compressionPermits;

    /**
     * List of pending chunk compression futures that must finish before {@link #finish()} returns.
     */
    private final java.util.concurrent.ConcurrentLinkedQueue<Future<?>> pendingCompressionTasks = new java.util.concurrent.ConcurrentLinkedQueue<>();

    /**
     * Thread-local array of active, unsealed chunk builders indexed directly by ASCII type code character.
     * Eliminates map lookups and Character autoboxing overhead on the critical parsing path.
     */
    private final ThreadLocal<ChunkBuilder[]> threadLocalBuilders = ThreadLocal.withInitial(() -> {
        final ChunkBuilder[] array = new ChunkBuilder[128];
        allThreadBuilders.add(array);
        return array;
    });

    /**
     * Constructs a new {@link ChunkIngestionCollector} initialized with default dictionaries,
     * an empty catalog, and the singleton {@link ChunkTypeRegistry}.
     */
    public ChunkIngestionCollector()
    {
        this(new GlobalDictionaries(), new ChunkCatalog(), ChunkTypeRegistry.getInstance(), null);
    }

    /**
     * Constructs a new {@link ChunkIngestionCollector} configured to stream chunks directly to disk
     * in the specified cache directory.
     *
     * @param cacheDir
     *            target cache directory for direct-to-disk chunk spooling (or null for in-memory buffering)
     */
    public ChunkIngestionCollector(final File cacheDir)
    {
        this(new GlobalDictionaries(), new ChunkCatalog(), ChunkTypeRegistry.getInstance(), cacheDir);
    }

    /**
     * Constructs a {@link ChunkIngestionCollector} with specific dictionaries, catalog, and registry.
     *
     * @param dictionaries
     *            global dictionaries for interning string metadata
     * @param catalog
     *            catalog for storing sealed chunks
     * @param registry
     *            chunk type codec registry
     */
    public ChunkIngestionCollector(final GlobalDictionaries dictionaries, final ChunkCatalog catalog, final ChunkTypeRegistry registry)
    {
        this(dictionaries, catalog, registry, null);
    }

    /**
     * Constructs a {@link ChunkIngestionCollector} with specific dictionaries, catalog, registry, and cache directory.
     *
     * @param dictionaries
     *            global dictionaries for interning string metadata
     * @param catalog
     *            catalog for storing sealed chunks
     * @param registry
     *            chunk type codec registry
     * @param cacheDir
     *            target cache directory for streaming chunk spooling (or null if purely in-memory)
     */
    public ChunkIngestionCollector(final GlobalDictionaries dictionaries, final ChunkCatalog catalog,
                                   final ChunkTypeRegistry registry, final File cacheDir)
    {
        this.dictionaries = dictionaries;
        this.catalog = catalog;
        this.registry = registry;

        ChunkSpooler s = null;
        if (cacheDir != null)
        {
            try
            {
                s = new ChunkSpooler(cacheDir);
            }
            catch (final IOException e)
            {
                XltLogger.runTimeLogger.warn("Failed to initialize direct-to-disk chunk spooler; falling back to in-memory buffering: " + e.getMessage());
                s = null;
            }
        }
        this.spooler = s;

        final int cpus = Runtime.getRuntime().availableProcessors();
        final int workerCount = Math.max(2, Math.min(8, cpus / 2));
        this.compressionExecutor = Executors.newFixedThreadPool(workerCount, new ThreadFactory()
        {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable r)
            {
                final Thread t = new Thread(r, "ChunkCompressionWorker-" + count.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
        this.compressionPermits = new Semaphore(Math.max(8, cpus * 2));
    }

    /**
     * Returns the global dictionaries used during ingestion.
     *
     * @return global dictionaries
     */
    public GlobalDictionaries getDictionaries()
    {
        return dictionaries;
    }

    /**
     * Returns the chunk catalog accumulating sealed chunks.
     *
     * @return chunk catalog
     */
    public ChunkCatalog getCatalog()
    {
        return catalog;
    }

    /**
     * Returns the chunk type registry.
     *
     * @return chunk type registry
     */
    public ChunkTypeRegistry getRegistry()
    {
        return registry;
    }

    /**
     * Ingests a batch of post-processed data records into the calling thread's active chunk builders.
     * <p>
     * Looking up the thread-local builder map once per batch significantly reduces ThreadLocal overhead
     * compared to individual record ingestion.
     *
     * @param records
     *            the list of post-processed {@link Data} records to ingest (null or empty lists are safely ignored)
     */
    public void collect(final PostProcessedDataContainer container)
    {
        if (container == null)
        {
            return;
        }

        final SimpleArrayList<Data> list = container.dataList;
        final int size = list.size();
        if (size == 0)
        {
            return;
        }

        final ChunkBuilder[] builders = threadLocalBuilders.get();
        final Object[] internalArr = list.getInternalArray();
        final char typeCode = container.typeCode;

        if (typeCode > 0 && typeCode < builders.length)
        {
            // Homogeneous chunk fast-path: resolve builder once per chunk
            ChunkBuilder builder = builders[typeCode];
            if (builder == null)
            {
                final ChunkTypeHandler handler = registry.getHandler(typeCode);
                if (handler == null)
                {
                    return;
                }
                builder = handler.newChunkBuilder(dictionaries);
                builders[typeCode] = builder;
            }

            for (int i = 0; i < size; i++)
            {
                final Data record = (Data) internalArr[i];
                if (record != null)
                {
                    builder.append(record);
                    if (builder.isFull())
                    {
                        final ChunkBuilder fullBuilder = builder;
                        final ChunkTypeHandler handler = registry.getHandler(typeCode);
                        builder = handler != null ? handler.newChunkBuilder(dictionaries) : null;
                        builders[typeCode] = builder;
                        sealAndSpool(fullBuilder, typeCode);
                        if (builder == null)
                        {
                            break;
                        }
                    }
                }
            }
        }
        else
        {
            // Heterogeneous chunk: per-record dispatch
            for (int i = 0; i < size; i++)
            {
                final Data record = (Data) internalArr[i];
                if (record != null)
                {
                    collectRecord(record, builders);
                }
            }
        }
    }

    /**
     * Ingests a list of post-processed data records directly into the calling thread's active chunk builders.
     *
     * @param records
     *            the list of post-processed {@link Data} records to ingest (null or empty lists are safely ignored)
     */
    public void collect(final List<Data> records)
    {
        if (records == null || records.isEmpty())
        {
            return;
        }

        // Obtain dedicated builders for the current calling thread
        final ChunkBuilder[] builders = threadLocalBuilders.get();
        final int size = records.size();

        if (records instanceof SimpleArrayList)
        {
            final Object[] array = ((SimpleArrayList<Data>) records).getInternalArray();
            for (int i = 0; i < size; i++)
            {
                final Data record = (Data) array[i];
                if (record != null)
                {
                    collectRecord(record, builders);
                }
            }
        }
        else
        {
            for (int i = 0; i < size; i++)
            {
                final Data record = records.get(i);
                if (record != null)
                {
                    collectRecord(record, builders);
                }
            }
        }
    }

    /**
     * Ingests a single post-processed data record into the calling thread's active chunk builder.
     *
     * @param record
     *            the post-processed {@link Data} record to ingest (null records are safely ignored)
     */
    public void collect(final Data record)
    {
        if (record == null)
        {
            return;
        }

        final ChunkBuilder[] builders = threadLocalBuilders.get();
        collectRecord(record, builders);
    }

    /**
     * Internal helper to append a single record to the appropriate builder in the specified builder array.
     * If the builder reaches maximum capacity, it is sealed and registered immediately.
     *
     * @param record
     *            the record to append
     * @param builders
     *            the caller's thread-confined active builders array indexed by type code
     */
    private void collectRecord(final Data record, final ChunkBuilder[] builders)
    {
        final char typeCode = record.getTypeCode();

        // 1. Locate or lazily initialize the builder for this type code on the calling thread
        ChunkBuilder builder = (typeCode < builders.length) ? builders[typeCode] : null;
        if (builder == null)
        {
            final ChunkTypeHandler handler = registry.getHandler(typeCode);
            if (handler == null)
            {
                return;
            }
            builder = handler.newChunkBuilder(dictionaries);
            if (typeCode < builders.length)
            {
                builders[typeCode] = builder;
            }
        }

        // 2. Append record to builder
        builder.append(record);

        // 3. If builder reached maximum chunk capacity, seal and rotate to a new builder
        if (builder.isFull())
        {
            final ChunkBuilder fullBuilder = builder;

            // Rotate to a fresh chunk builder immediately on the parser thread
            final ChunkTypeHandler handler = registry.getHandler(typeCode);
            final ChunkBuilder newBuilder = handler != null ? handler.newChunkBuilder(dictionaries) : null;
            if (typeCode < builders.length)
            {
                builders[typeCode] = newBuilder;
            }

            sealAndSpool(fullBuilder, typeCode);
        }
    }

    /**
     * Seals a full chunk builder and offloads columnar compression and spooling to background workers.
     *
     * @param fullBuilder
     *            the sealed chunk builder
     * @param typeCode
     *            the data type code
     */
    private void sealAndSpool(final ChunkBuilder fullBuilder, final char typeCode)
    {
        // Offload columnar compression and spooling to background pool if permits are available
        if (compressionPermits.tryAcquire())
        {
            pendingCompressionTasks.add(compressionExecutor.submit(() -> {
                try
                {
                    final Chunk chunk = fullBuilder.seal();
                    if (spooler != null)
                    {
                        try
                        {
                            final ChunkTypeHandler h = registry.getHandler(typeCode);
                            final DiskChunk diskChunk = spooler.spoolChunk(chunk, h);
                            catalog.addChunk(diskChunk);
                            return;
                        }
                        catch (final IOException e)
                        {
                            XltLogger.runTimeLogger.warn("Failed to spool chunk to disk; retaining in-memory chunk: " + e.getMessage());
                        }
                    }
                    catalog.addChunk(chunk);
                }
                finally
                {
                    compressionPermits.release();
                }
            }));
        }
        else
        {
            // Fallback to synchronous compression and spooling under extreme saturation to bound memory
            final Chunk chunk = fullBuilder.seal();
            if (spooler != null)
            {
                try
                {
                    final ChunkTypeHandler h = registry.getHandler(typeCode);
                    final DiskChunk diskChunk = spooler.spoolChunk(chunk, h);
                    catalog.addChunk(diskChunk);
                    return;
                }
                catch (final IOException e)
                {
                    XltLogger.runTimeLogger.warn("Failed to spool chunk to disk; retaining in-memory chunk: " + e.getMessage());
                }
            }
            catalog.addChunk(chunk);
        }
    }

    /**
     * Flushes and seals all remaining partially filled chunk builders across all worker threads,
     * registers them in the catalog, and produces a fully populated, queryable {@link ChunkStorage} instance.
     * <p>
     * Waits for all background compression tasks to complete before draining partial builders.
     * If direct-to-disk spooling was active, the storage file is finalized and indexed, and an open
     * read-only {@link FileChannel} is attached for lazy query execution.
     *
     * @return the complete {@link ChunkStorage} ready for querying and disk caching
     */
    public synchronized ChunkStorage finish()
    {
        // 1. Await completion of all pending background chunk compression tasks
        for (final Future<?> task : pendingCompressionTasks)
        {
            try
            {
                task.get();
            }
            catch (final Exception e)
            {
                throw new RuntimeException("Asynchronous chunk compression failed", e);
            }
        }
        pendingCompressionTasks.clear();

        // 2. Shut down the background compression executor pool
        compressionExecutor.shutdown();

        // 3. Collect all non-empty active builders from all worker threads
        final List<ChunkBuilder> remainingBuilders = new ArrayList<>();
        for (final ChunkBuilder[] threadBuilders : allThreadBuilders)
        {
            for (int i = 0; i < threadBuilders.length; i++)
            {
                final ChunkBuilder builder = threadBuilders[i];
                if (builder != null && builder.getRowCount() > 0)
                {
                    remainingBuilders.add(builder);
                }
                threadBuilders[i] = null;
            }
        }
        allThreadBuilders.clear();

        // 4. Seal and compress all remaining builders in parallel
        final List<Chunk> sealedChunks = remainingBuilders.parallelStream()
            .map(ChunkBuilder::seal)
            .toList();

        // 5. Register sealed chunks into catalog (spooling to disk if spooler is active)
        for (final Chunk chunk : sealedChunks)
        {
            if (spooler != null)
            {
                try
                {
                    final ChunkTypeHandler h = registry.getHandler(chunk.getTypeCode());
                    final DiskChunk diskChunk = spooler.spoolChunk(chunk, h);
                    catalog.addChunk(diskChunk);
                    continue;
                }
                catch (final IOException e)
                {
                    XltLogger.runTimeLogger.warn("Failed to spool remaining chunk to disk; retaining in-memory chunk: " + e.getMessage());
                }
            }
            catalog.addChunk(chunk);
        }

        // 6. Finalize spooled storage files and attach open file channel for lazy loading
        if (spooler != null)
        {
            try
            {
                spooler.finish(catalog, dictionaries);
                final File targetFile = spooler.getTargetFile();
                final FileChannel channel = FileChannel.open(targetFile.toPath(), StandardOpenOption.READ);
                final ChunkStorage storage = new ChunkStorage(dictionaries, catalog, registry, targetFile, channel, true);
                catalog.attachStorage(storage);
                return storage;
            }
            catch (final IOException e)
            {
                XltLogger.runTimeLogger.error("Failed to finalize spooled chunk storage; falling back to in-memory storage: " + e.getMessage());
            }
        }

        return new ChunkStorage(dictionaries, catalog, registry);
    }
}
