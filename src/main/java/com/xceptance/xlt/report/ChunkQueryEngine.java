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
package com.xceptance.xlt.report;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.report.PostProcessedDataContainer;

import com.xceptance.xlt.report.storage.ChunkStorage;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.ChunkTypeHandler;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Parallel query execution engine for scanning ChunkDB chunks directly into {@link StatisticsProcessor}.
 * <p>
 * <b>Query Execution Lifecycle:</b>
 * <ol>
 *   <li><b>Step 1 - Metadata &amp; Bitmap Index Pruning:</b> Before decompressing any columnar data,
 *       all chunks in the catalog are tested against the query {@link ScanPredicate}. Chunks whose
 *       time bounds do not overlap the query window, or whose inverted {@link org.roaringbitmap.RoaringBitmap}
 *       sets are disjoint from the agent/test case filter, are pruned in microseconds without disk or CPU overhead.</li>
 *   <li><b>Step 2 - Parallel SIMD Decompression &amp; Iteration:</b> Candidate chunks are distributed across
 *       a dedicated {@link ForkJoinPool}. Each worker thread decompresses columns using {@link com.xceptance.xlt.report.storage.compression.FastIntegerCodec},
 *       materializes {@link Data} records, packages them in a {@link PostProcessedDataContainer}, and feeds them
 *       directly into thread-safe worker processors in {@link StatisticsProcessor}.</li>
 *   <li><b>Step 3 - Finalization &amp; Aggregation:</b> Upon completion, {@link StatisticsProcessor#complete()}
 *       merges all thread-local statistics into master report providers.</li>
 * </ol>
 */
public class ChunkQueryEngine
{
    /** Logger instance for query performance diagnostics. */
    private static final Logger LOG = LoggerFactory.getLogger(ChunkQueryEngine.class);

    /** The underlying ChunkDB storage containing chunks and dictionaries. */
    private final ChunkStorage storage;

    /** Number of parallel worker threads allocated for chunk scanning. */
    private final int threadCount;

    /**
     * Reusable thread-local data containers sized for full chunk capacity (up to 65,536 records).
     * <p>
     * Reusing pre-allocated data containers across chunks avoids allocating millions of container
     * and array objects on the JVM heap, completely removing GC pressure during warm query scans.
     */
    private static final ThreadLocal<PostProcessedDataContainer> THREAD_LOCAL_CONTAINER =
        ThreadLocal.withInitial(() -> new PostProcessedDataContainer(65536, 1));

    /**
     * Constructs a new {@link ChunkQueryEngine}.
     *
     * @param storage
     *            the ChunkDB storage containing the chunks to scan
     * @param threadCount
     *            number of worker threads (if &lt;= 0, defaults to available CPU cores)
     */
    public ChunkQueryEngine(final ChunkStorage storage, final int threadCount)
    {
        this.storage = storage;
        this.threadCount = threadCount > 0 ? threadCount : Runtime.getRuntime().availableProcessors();
    }

    /**
     * Executes a parallel scan over all candidate chunks matching the predicate, feeding results
     * directly into the {@link StatisticsProcessor}.
     *
     * @param predicate
     *            filter criteria specifying time boundaries, timer name filters, and agent/test case IDs
     * @param statisticsProcessor
     *            statistics processor that accumulates metrics for report providers
     */
    public long executeScan(final ScanPredicate predicate, final StatisticsProcessor statisticsProcessor)
    {
        final long start = System.currentTimeMillis();

        // Step 1: Candidate chunk pruning via metadata min/max timestamps & RoaringBitmaps
        final List<Chunk> candidateChunks = storage.getCatalog().getAllChunks().stream()
            .filter(predicate::mayMatchChunk)
            .collect(Collectors.toList());

        LOG.info("ChunkDB query matched {} of {} chunks", candidateChunks.size(), storage.getCatalog().getChunkCount());

        // If no chunks match the filter, finalize processor and exit early
        if (candidateChunks.isEmpty())
        {
            statisticsProcessor.complete();
            return 0L;
        }

        final java.util.concurrent.atomic.LongAdder totalRecords = new java.util.concurrent.atomic.LongAdder();
        final java.util.concurrent.atomic.LongAdder totalScanNanos = new java.util.concurrent.atomic.LongAdder();
        final java.util.concurrent.atomic.LongAdder totalProcessNanos = new java.util.concurrent.atomic.LongAdder();

        // Step 2: Parallel decompression & streaming into statistics processor
        final ForkJoinPool pool = new ForkJoinPool(threadCount);
        try
        {
            pool.submit(() -> candidateChunks.parallelStream().forEach(chunk -> {
                // Find codec handler for this chunk type
                final ChunkTypeHandler handler = storage.getRegistry().getHandler(chunk.getTypeCode());
                final PostProcessedDataContainer container = THREAD_LOCAL_CONTAINER.get();
                container.reset();
                container.typeCode = chunk.getTypeCode();

                // Decompress columns and stream matching records into reusable container
                final long s0 = System.nanoTime();
                handler.scan(chunk, predicate, container, storage.getDictionaries());
                final long s1 = System.nanoTime();
                totalScanNanos.add(s1 - s0);

                // Feed container into thread-safe statistics processor with type-filtered routing
                final int count = container.data.size();
                if (count > 0)
                {
                    totalRecords.add(count);
                    final long p0 = System.nanoTime();
                    statisticsProcessor.process(container, chunk.getTypeCode());
                    final long p1 = System.nanoTime();
                    totalProcessNanos.add(p1 - p0);
                }
                container.reset();
            })).get();
        }

        catch (final Exception e)
        {
            LOG.error("Error executing parallel chunk scan", e);
            pool.shutdownNow();
            throw new RuntimeException("ChunkDB query execution failed", e);
        }
        finally
        {
            // Clean up thread pool
            pool.shutdown();
        }

        // Finalize statistics merging across thread-local provider sets
        statisticsProcessor.complete();

        final long duration = System.currentTimeMillis() - start;
        com.xceptance.xlt.api.util.XltLogger.reportLogger.info(String.format(
            "ChunkDB timing breakdown: handler.scan (decomp+reconstitute) CPU time = %,d ms, statisticsProcessor.process CPU time = %,d ms across %d threads",
            totalScanNanos.sum() / 1_000_000L, totalProcessNanos.sum() / 1_000_000L, threadCount));

        LOG.info("ChunkDB query completed in {} ms (processed {} chunks, {} records)",
                 duration, candidateChunks.size(), totalRecords.sum());
        return totalRecords.sum();
    }
}
