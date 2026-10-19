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
package com.xceptance.xlt.report.query;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.report.query.QueryModels.ForensicRecord;
import com.xceptance.xlt.report.query.QueryModels.GroupedSeries;
import com.xceptance.xlt.report.query.QueryModels.InspectQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.InspectResponse;
import com.xceptance.xlt.report.query.QueryModels.MetadataResponse;
import com.xceptance.xlt.report.query.QueryModels.SessionNode;
import com.xceptance.xlt.report.query.QueryModels.SessionQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.SessionResponse;
import com.xceptance.xlt.report.query.QueryModels.SummaryRow;
import com.xceptance.xlt.report.query.QueryModels.SummaryTableQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.SummaryTableResponse;
import com.xceptance.xlt.report.query.QueryModels.TimeSeriesQuerySpec;
import com.xceptance.xlt.report.query.QueryModels.TimeSeriesResponse;
import com.xceptance.xlt.report.storage.ChunkStorage;
import com.xceptance.xlt.report.storage.catalog.ChunkCatalog;
import com.xceptance.xlt.report.storage.chunk.Chunk;
import com.xceptance.xlt.report.storage.chunk.DiskChunk;
import com.xceptance.xlt.report.storage.chunk.EventChunk;
import com.xceptance.xlt.report.storage.chunk.RequestChunk;
import com.xceptance.xlt.report.storage.chunk.RequestChunkHandler;
import com.xceptance.xlt.report.storage.chunk.TimerChunk;
import com.xceptance.xlt.report.storage.chunk.TransactionChunk;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;
import com.xceptance.xlt.report.util.RuntimeHistogram;

/**
 * Vectorized columnar query engine for interactive exploration and forensic root-cause analysis over ChunkDB.
 * <p>
 * <b>Architectural Design:</b>
 * <ul>
 *   <li><b>In-Memory Column Pointer &amp; Hint Index ({@link RequestChunkIndex}):</b> Instead of decompressing
 *       all chunks into heap RAM, builds a lightweight (~250 bytes/chunk) jump-index recording exact on-disk
 *       column byte offsets, compressed {@code int[]} lengths, single-value dictionary hints ({@code singleTimerId},
 *       {@code singleAgentId}, {@code singleMethodId}, {@code singleContentTypeId}, {@code singleUsedIpId}), and
 *       learned HTTP status code bounds ({@code minStatusCode}, {@code maxStatusCode}).</li>
 *   <li><b>Zero-Allocation Positional Column Streaming:</b> Reads only the required compressed columns directly
 *       from {@code chunks.bin} into a reusable thread-local {@link ByteBuffer} via
 *       {@link ChunkStorage#readChunkBuffer(long, ByteBuffer)}, bulk-copies words via {@code IntBuffer.get(int[])},
 *       and skips reading constant-value columns entirely.</li>
 *   <li><b>Two-Stage Predicate Pushdown:</b>
 *       <ol>
 *         <li><i>Stage 1 (Catalog, Zone Map &amp; Hint Pruning):</i> Rejects chunks outside {@code [fromTime, toTime]},
 *             disjoint from dictionary filter bitmaps ({@code timerNameIds}, {@code agentTestCaseIds}), or disjoint
 *             from learned chunk hints ({@code maxRunTime}, {@code hasFailures}, {@code minStatusCode..maxStatusCode},
 *             {@code singleMethodId}, {@code singleContentTypeId}, {@code singleUsedIpId}) in \(O(1)\) time.</li>
 *         <li><i>Stage 2 (Vectorized Row Filtering):</i> Evaluates fine-grained row predicates over primitive arrays
 *             and aggregates into per-worker-thread {@link RuntimeHistogram} accumulators.</li>
 *       </ol>
 *   </li>
 *   <li><b>On-Demand Forensic Tail Inflation:</b> During outlier inspection ({@link #inspectOutliers}) and
 *       virtual user session reconstruction ({@link #traceUserSession}), only the winning chunks have their
 *       Deflate-compressed Cold Forensic Tail Block inflated via {@link RequestChunk#loadForensicDetails(Chunk)}.</li>
 * </ul>
 */
public class ColumnarQueryEngine
{
    /** Maximum number of distinct series returned when splitting time-series charts by a dimension. */
    private static final int MAX_GROUPED_SERIES = 8;

    /** Maximum number of time buckets allowed in a single time-series query to bound response size. */
    private static final int MAX_BUCKETS = 2000;

    /** Default limit for forensic outlier inspection queries. */
    private static final int DEFAULT_INSPECT_LIMIT = 100;

    /** Hard ceiling on forensic outlier inspection results to bound browser DOM rendering. */
    private static final int MAX_INSPECT_LIMIT = 500;

    /** Default fallback time window padding (30 seconds) when reconstructing a session without an enclosing Transaction. */
    private static final long DEFAULT_SESSION_PADDING_MS = 30_000L;

    /** Maximum number of candidate request chunks for which exact per-row 32-bit URL hash bitmaps are computed. */
    private static final int MAX_CHUNKS_FOR_EXACT_URL_BITMAP = 32;

    // -------------------------------------------------------------------------
    // On-Disk RequestChunk Column Ordinals (0..20)
    // -------------------------------------------------------------------------
    private static final int IDX_TIME_OFFSETS        = 0;
    private static final int IDX_TIMER_IDS           = 1;
    private static final int IDX_AGENT_IDS           = 2;
    private static final int IDX_RUN_TIMES           = 3;
    private static final int IDX_RESPONSE_CODES      = 4;
    private static final int IDX_BYTES_SENT          = 5;
    private static final int IDX_BYTES_RECEIVED      = 6;
    private static final int IDX_DNS_TIMES           = 7;
    private static final int IDX_CONNECT_TIMES       = 8;
    private static final int IDX_SEND_TIMES          = 9;
    private static final int IDX_SERVER_BUSY_TIMES   = 10;
    private static final int IDX_RECEIVE_TIMES       = 11;
    private static final int IDX_TTFB                = 12;
    private static final int IDX_TTLB                = 13;
    private static final int IDX_HTTP_METHODS        = 14;
    private static final int IDX_CONTENT_TYPES       = 15;
    private static final int IDX_USED_IPS            = 16;
    private static final int IDX_IP_ADDRESSES        = 17;
    private static final int IDX_USER_NUMBERS        = 18;
    private static final int IDX_FORM_DATA_ENCODINGS = 19;
    private static final int IDX_URL_HASHES          = 20;
    private static final int REQUEST_COLUMN_COUNT    = 21;

    private static final int[] EMPTY_INTS = new int[0];

    /** Per-worker-thread reusable I/O ByteBuffer for zero-allocation positional column reads. */
    private static final ThreadLocal<ByteBuffer> TL_READ_BUFFER =
        ThreadLocal.withInitial(() -> ByteBuffer.allocate(262_144));

    private static ByteBuffer acquireThreadBuffer(final int requiredBytes)
    {
        ByteBuffer buf = TL_READ_BUFFER.get();
        if (buf.capacity() < requiredBytes)
        {
            buf = ByteBuffer.allocate(Math.max(requiredBytes, buf.capacity() << 1));
            TL_READ_BUFFER.set(buf);
        }
        buf.clear().limit(requiredBytes);
        return buf;
    }

    /** Backing columnar storage container. */
    private final ChunkStorage storage;

    /** Optional root results directory for resolving local Result Browser dump files. */
    private final File resultsDir;

    /** Lightweight in-memory column pointer and hint index per on-disk RequestChunk (~250 bytes/chunk). */
    private final ConcurrentHashMap<Chunk, RequestChunkIndex> requestIndexMap = new ConcurrentHashMap<>();

    /** Whether column pointer indexes were eagerly warmed up at startup. */
    private final boolean indexedAtStartup;

    /** Lazily initialized and cached dataset metadata response. */
    private volatile MetadataResponse cachedMetadata;

    /**
     * Constructs a {@link ColumnarQueryEngine} over the given {@link ChunkStorage}.
     *
     * @param storage
     *            the ChunkDB storage instance
     */
    public ColumnarQueryEngine(final ChunkStorage storage)
    {
        this(storage, null, false, false);
    }

    /**
     * Constructs a {@link ColumnarQueryEngine} over the given {@link ChunkStorage} and raw results directory.
     *
     * @param storage
     *            the ChunkDB storage instance
     * @param resultsDir
     *            optional raw load test results directory containing {@code <agent>/<scenario>/<user>/output/<dump>}
     */
    public ColumnarQueryEngine(final ChunkStorage storage, final File resultsDir)
    {
        this(storage, resultsDir, false, false);
    }

    /**
     * Constructs a {@link ColumnarQueryEngine} with optional eager warmup of the in-memory column pointer
     * and chunk hint index.
     *
     * @param storage
     *            the ChunkDB storage instance
     * @param resultsDir
     *            optional raw load test results directory
     * @param eagerIndexWarmup
     *            {@code true} to eagerly jump-index all on-disk {@code RequestChunk} column pointers in parallel
     * @param warmHints
     *            {@code true} to also warm up facet metadata at startup
     */
    public ColumnarQueryEngine(final ChunkStorage storage,
                               final File resultsDir,
                               final boolean eagerIndexWarmup,
                               final boolean warmHints)
    {
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        this.resultsDir = resultsDir;
        this.indexedAtStartup = eagerIndexWarmup || warmHints;

        if (this.indexedAtStartup)
        {
            preloadColumnPointerIndex();
        }
    }

    /**
     * Builds the lightweight in-memory column pointer and hint index ({@link RequestChunkIndex}) across all
     * on-disk {@code RequestChunk}s in parallel by jumping across the 21 column length headers without reading
     * or decompressing column bodies (~750 ms and ~1.5 MB RAM for 485M rows / 6,299 chunks).
     */
    private void preloadColumnPointerIndex()
    {
        final List<Chunk> reqChunks = storage.getCatalog().getChunksForType('R');
        reqChunks.parallelStream().forEach(this::getOrBuildRequestIndex);
    }

    /**
     * Returns the active memory residency mode ({@code "INDEXED"} or {@code "DISK"}).
     *
     * @return memory mode string
     */
    public String getMemoryMode()
    {
        return (indexedAtStartup || !requestIndexMap.isEmpty()) ? "INDEXED" : "DISK";
    }

    /**
     * Returns the backing {@link ChunkStorage} instance.
     *
     * @return backing storage
     */
    public ChunkStorage getStorage()
    {
        return storage;
    }

    /**
     * Returns the optional raw load test results directory associated with this query engine.
     *
     * @return raw results directory, or {@code null}
     */
    public File getResultsDir()
    {
        return resultsDir;
    }

    // =========================================================================
    // In-Memory Column Pointer & Hint Index (RequestChunkIndex)
    // =========================================================================

    /**
     * Lightweight in-memory descriptor (~250 bytes) storing on-disk byte offsets for all 21 columns of a
     * {@link DiskChunk} along with single-value dictionary hints and learned HTTP status code bounds.
     */
    private static final class RequestChunkIndex
    {
        final DiskChunk dc;
        final int rowCount;
        final long baseTime;
        final RoaringBitmap failedRows;
        final boolean hasFailedRows;
        final int[] colRelOffset;
        final int[] colIntLen;

        // O(1) single-value hints (-1 = multiple values, >= 0 = constant across all rows in chunk)
        final int singleTimerId;
        final int singleAgentId;

        // Learned zone-map / single-value hints populated lazily on first column read
        volatile short minStatusCode = -1;
        volatile short maxStatusCode = -1;
        volatile int singleMethodId = -2;      // -2 = unknown, -1 = multiple, >= 0 = constant
        volatile int singleContentTypeId = -2; // -2 = unknown, -1 = multiple, >= 0 = constant
        volatile int singleUsedIpId = -2;      // -2 = unknown, -1 = multiple, >= 0 = constant

        RequestChunkIndex(final DiskChunk dc,
                          final RoaringBitmap failedRows,
                          final int[] colRelOffset,
                          final int[] colIntLen)
        {
            this.dc = dc;
            this.rowCount = dc.getRowCount();
            this.baseTime = dc.getMinTime();
            this.hasFailedRows = failedRows != null && !failedRows.isEmpty();
            this.failedRows = this.hasFailedRows ? failedRows : null;
            this.colRelOffset = colRelOffset;
            this.colIntLen = colIntLen;

            final RoaringBitmap tIds = dc.getTimerNameIds();
            this.singleTimerId = (tIds != null && tIds.getCardinality() == 1) ? tIds.first() : -1;

            final RoaringBitmap aIds = dc.getAgentTestCaseIds();
            this.singleAgentId = (aIds != null && aIds.getCardinality() == 1) ? aIds.first() : -1;
        }
    }

    private RequestChunkIndex getOrBuildRequestIndex(final Chunk chunk)
    {
        if (!(chunk instanceof DiskChunk dc))
        {
            return null;
        }
        return requestIndexMap.computeIfAbsent(dc, k -> buildRequestChunkIndex(dc));
    }

    private RequestChunkIndex buildRequestChunkIndex(final DiskChunk dc)
    {
        try
        {
            final long baseFileOffset = dc.getFileOffset();
            final int vecLen = dc.getVectorPayloadLength();
            final int initialRead = Math.min(vecLen, 65_536);
            final ByteBuffer buf = acquireThreadBuffer(initialRead);
            storage.readChunkBuffer(baseFileOffset, buf);
            buf.flip();

            // Skip 29-byte header: rowCount(4) + minTime(8) + maxTime(8) + maxRunTime(4) + hasFailures(1) + vectorPayloadLen(4)
            buf.position(29);

            RoaringBitmap failedRows = null;
            for (int b = 0; b < 3; b++)
            {
                final RoaringBitmap bm = new RoaringBitmap();
                bm.deserialize(buf);
                buf.position(buf.position() + bm.serializedSizeInBytes());
                if (b == 2)
                {
                    failedRows = bm;
                }
            }

            final int[] colRelOffset = new int[REQUEST_COLUMN_COUNT];
            final int[] colIntLen = new int[REQUEST_COLUMN_COUNT];
            int relPos = buf.position();
            int validBufLimit = initialRead;

            for (int c = 0; c < REQUEST_COLUMN_COUNT; c++)
            {
                if (relPos >= vecLen)
                {
                    break;
                }
                final int len;
                if (relPos + 4 <= validBufLimit)
                {
                    len = buf.getInt(relPos);
                }
                else
                {
                    buf.clear().limit(4);
                    storage.readChunkBuffer(baseFileOffset + relPos, buf);
                    len = buf.getInt(0);
                    validBufLimit = 0;
                }
                colRelOffset[c] = relPos + 4;
                colIntLen[c] = len;
                relPos += 4 + (len << 2);
            }

            return new RequestChunkIndex(dc, failedRows, colRelOffset, colIntLen);
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to build column pointer index for DiskChunk: " + dc, e);
        }
    }

    /**
     * Reads and decompresses a single column directly from disk using the chunk's in-memory column pointer.
     */
    private int[] readDecompressedColumn(final RequestChunkIndex idx, final int colIdx)
    {
        final int intLen = idx.colIntLen[colIdx];
        if (intLen <= 0)
        {
            return EMPTY_INTS;
        }
        final int byteLen = intLen << 2;
        final ByteBuffer buf = acquireThreadBuffer(byteLen);
        try
        {
            storage.readChunkBuffer(idx.dc.getFileOffset() + idx.colRelOffset[colIdx], buf);
            buf.flip();
            final int[] comp = new int[intLen];
            buf.asIntBuffer().get(comp);
            final int[] decompressed = FastIntegerCodec.decompress(comp);
            learnColumnHints(idx, colIdx, decompressed);
            return decompressed;
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to read column " + colIdx + " from DiskChunk: " + idx.dc, e);
        }
    }

    private static void learnColumnHints(final RequestChunkIndex idx, final int colIdx, final int[] values)
    {
        if (values == null || values.length == 0)
        {
            return;
        }
        if (colIdx == IDX_RESPONSE_CODES && idx.minStatusCode < 0)
        {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (final int v : values)
            {
                if (v < min) min = v;
                if (v > max) max = v;
            }
            if (min >= 0 && max <= Short.MAX_VALUE)
            {
                idx.minStatusCode = (short) min;
                idx.maxStatusCode = (short) max;
            }
        }
        else if (colIdx == IDX_HTTP_METHODS && idx.singleMethodId == -2)
        {
            idx.singleMethodId = detectSingleNonNegativeValue(values);
        }
        else if (colIdx == IDX_CONTENT_TYPES && idx.singleContentTypeId == -2)
        {
            idx.singleContentTypeId = detectSingleNonNegativeValue(values);
        }
        else if (colIdx == IDX_USED_IPS && idx.singleUsedIpId == -2)
        {
            idx.singleUsedIpId = detectSingleNonNegativeValue(values);
        }
    }

    private static int detectSingleNonNegativeValue(final int[] values)
    {
        final int first = values[0];
        if (first < 0)
        {
            return -1;
        }
        for (int i = 1; i < values.length; i++)
        {
            if (values[i] != first)
            {
                return -1;
            }
        }
        return first;
    }

    private Chunk resolveNonRequestChunk(final char typeCode, final Chunk chunk, final GlobalDictionaries dicts)
    {
        return storage.getRegistry().getHandler(typeCode).resolveChunk(chunk, dicts);
    }

    // =========================================================================
    // 1. Metadata & Facet Discovery (GET /api/v1/metadata)
    // =========================================================================

    /**
     * Returns global dataset bounds, chunk counts, and distinct dimension facets for populating UI filter controls.
     *
     * @return dataset metadata and facet lists
     */
    public MetadataResponse getMetadata()
    {
        MetadataResponse local = this.cachedMetadata;
        if (local != null)
        {
            return local;
        }
        synchronized (this)
        {
            local = this.cachedMetadata;
            if (local != null)
            {
                return local;
            }
            local = buildMetadata();
            this.cachedMetadata = local;
            return local;
        }
    }

    private MetadataResponse buildMetadata()
    {
        final ChunkCatalog catalog = storage.getCatalog();
        final GlobalDictionaries dicts = storage.getDictionaries();
        final String[] timerNames = dicts.getTimerNamesArray();
        final int timerCount = dicts.getTimerNameCount();

        // 1. Count chunks by type and union timer IDs per record type directly from chunk metadata bitmaps
        final Map<String, Integer> chunksByType = new LinkedHashMap<>();
        final Map<Character, RoaringBitmap> timerIdsByType = new LinkedHashMap<>();
        for (final char tc : new char[]{'R', 'A', 'T', 'P', 'C', 'E', 'W'})
        {
            final List<Chunk> list = catalog.getChunksForType(tc);
            if (!list.isEmpty())
            {
                chunksByType.put(String.valueOf(tc), list.size());
                final RoaringBitmap unionTimers = new RoaringBitmap();
                for (final Chunk c : list)
                {
                    if (c.getTimerNameIds() != null)
                    {
                        unionTimers.or(c.getTimerNameIds());
                    }
                }
                timerIdsByType.put(tc, unionTimers);
            }
        }

        final Map<String, List<String>> timers = new LinkedHashMap<>();
        for (final Map.Entry<Character, RoaringBitmap> entry : timerIdsByType.entrySet())
        {
            final List<String> names = new ArrayList<>(entry.getValue().getCardinality());
            for (final int id : entry.getValue())
            {
                if (id >= 0 && id < timerCount && timerNames[id] != null && !timerNames[id].isEmpty())
                {
                    names.add(timerNames[id]);
                }
            }
            Collections.sort(names);
            timers.put(String.valueOf(entry.getKey()), names);
        }

        // 2. Extract distinct agents and scenarios from GlobalDictionaries.agentTestCases
        final Set<String> agentSet = new TreeSet<>();
        final Set<String> scenarioSet = new TreeSet<>();
        final GlobalDictionaries.AgentTestCase[] pairs = dicts.getAgentTestCasesArray();
        final int pairCount = dicts.getAgentTestCaseCount();
        for (int i = 0; i < pairCount; i++)
        {
            final GlobalDictionaries.AgentTestCase pair = pairs[i];
            if (pair != null)
            {
                if (!pair.agentName().isEmpty())
                {
                    agentSet.add(pair.agentName());
                }
                if (!pair.testCaseName().isEmpty())
                {
                    scenarioSet.add(pair.testCaseName());
                }
            }
        }

        // 3. Discover distinct string IDs referenced by compHttpMethods, compContentTypes, and compUsedIps
        // using direct column-pointer reads across up to 64 evenly-spaced request chunks.
        final RoaringBitmap methodIds = new RoaringBitmap();
        final RoaringBitmap contentTypeIds = new RoaringBitmap();
        final RoaringBitmap usedIpIds = new RoaringBitmap();

        final List<Chunk> reqChunks = catalog.getChunksForType('R');
        final List<Chunk> chunksToScan;
        if (reqChunks.size() <= 64)
        {
            chunksToScan = reqChunks;
        }
        else
        {
            chunksToScan = new ArrayList<>(64);
            final double step = (double) reqChunks.size() / 64.0;
            for (int i = 0; i < 64; i++)
            {
                chunksToScan.add(reqChunks.get(Math.min((int) (i * step), reqChunks.size() - 1)));
            }
        }

        final int facetMask = RequestChunkHandler.COL_HTTP_METHODS
                              | RequestChunkHandler.COL_CONTENT_TYPES
                              | RequestChunkHandler.COL_USED_IPS;

        chunksToScan.parallelStream().forEach(chunk -> {
            final int[] m;
            final int[] ct;
            final int[] ip;
            final RequestChunkIndex idx = getOrBuildRequestIndex(chunk);
            if (idx != null)
            {
                m = readDecompressedColumn(idx, IDX_HTTP_METHODS);
                ct = readDecompressedColumn(idx, IDX_CONTENT_TYPES);
                ip = readDecompressedColumn(idx, IDX_USED_IPS);
            }
            else
            {
                final RequestChunk rc = RequestChunkHandler.loadSelectedColumns(chunk, facetMask);
                m = FastIntegerCodec.decompress(rc.getCompHttpMethods());
                ct = FastIntegerCodec.decompress(rc.getCompContentTypes());
                ip = FastIntegerCodec.decompress(rc.getCompUsedIps());
            }

            final RoaringBitmap localM = new RoaringBitmap();
            final RoaringBitmap localCt = new RoaringBitmap();
            final RoaringBitmap localIp = new RoaringBitmap();

            for (final int id : m)
            {
                if (id >= 0) localM.add(id);
            }
            for (final int id : ct)
            {
                if (id >= 0) localCt.add(id);
            }
            for (final int id : ip)
            {
                if (id >= 0) localIp.add(id);
            }

            synchronized (methodIds)
            {
                methodIds.or(localM);
                contentTypeIds.or(localCt);
                usedIpIds.or(localIp);
            }
        });

        final String[] strings = dicts.getStringsArray();
        final int strCount = dicts.getStringCount();

        final Set<String> httpMethods = new TreeSet<>();
        for (final int id : methodIds)
        {
            if (id < strCount && strings[id] != null && !strings[id].isEmpty())
            {
                httpMethods.add(strings[id]);
            }
        }

        final Set<String> contentTypes = new TreeSet<>();
        for (final int id : contentTypeIds)
        {
            if (id < strCount && strings[id] != null && !strings[id].isEmpty())
            {
                contentTypes.add(strings[id]);
            }
        }

        final Set<String> usedIps = new TreeSet<>();
        for (final int id : usedIpIds)
        {
            if (id < strCount && strings[id] != null && !strings[id].isEmpty())
            {
                usedIps.add(strings[id]);
            }
        }

        return new MetadataResponse(
            storage.getMinTime(),
            storage.getMaxTime(),
            storage.getTotalRowCount(),
            catalog.getChunkCount(),
            chunksByType,
            timers,
            new ArrayList<>(agentSet),
            new ArrayList<>(scenarioSet),
            new ArrayList<>(httpMethods),
            new ArrayList<>(contentTypes),
            new ArrayList<>(usedIps),
            RequestChunkHandler.getRequestDetailsMode(),
            getMemoryMode()
        );
    }

    // =========================================================================
    // 2. Time-Series Query Engine (POST /api/v1/timeseries)
    // =========================================================================

    /**
     * Executes a dynamic time-series aggregation query with automatic resolution bucketing, exact
     * {@link RuntimeHistogram} percentiles per bucket, network timing breakdowns, and optional series splitting.
     *
     * @param spec
     *            the time-series query specification
     * @return parallel columnar arrays ready for {@code uPlot}
     */
    public TimeSeriesResponse queryTimeSeries(final TimeSeriesQuerySpec spec)
    {
        Objects.requireNonNull(spec, "spec cannot be null");
        final long queryStart = System.currentTimeMillis();
        final char typeCode = parseRecordType(spec.recordType());
        final ResolvedFilter filter = resolveFilter(
            typeCode,
            spec.fromTime(),
            spec.toTime(),
            spec.timers(),
            spec.timerQuery(),
            spec.agents(),
            spec.scenarios(),
            spec.usedIps(),
            spec.httpMethods(),
            spec.contentTypes(),
            spec.minStatusCode(),
            spec.maxStatusCode(),
            spec.failedOnly(),
            spec.minRunTime()
        );

        final long fromTime = filter.fromTime;
        final long toTime = Math.max(fromTime + 1, filter.toTime);
        final int bucketWidthMs = resolveBucketWidthMs(fromTime, toTime, spec.bucketWidthMs());
        final int bucketCount = Math.max(1, (int) Math.min(MAX_BUCKETS, ((toTime - fromTime) / bucketWidthMs) + 1));
        final String groupBy = normalizeGroupBy(spec.groupBy(), "NONE");
        final boolean hasGroupBy = !"NONE".equals(groupBy);

        final List<Chunk> candidateChunks = pruneCandidateChunks(typeCode, filter);
        final GlobalDictionaries dicts = storage.getDictionaries();
        final AtomicInteger chunksScanned = new AtomicInteger(0);

        // Execute parallel scan using one TimeSeriesAccumulator per ForkJoin worker thread (instead of one per chunk)
        final TimeSeriesAccumulator merged = candidateChunks.parallelStream().collect(
            () -> new TimeSeriesAccumulator(bucketCount, hasGroupBy),
            (acc, chunk) -> {
                chunksScanned.incrementAndGet();
                if (typeCode == 'R')
                {
                    scanRequestChunkForTimeSeries(chunk, filter, fromTime, bucketWidthMs, bucketCount, groupBy, dicts, acc);
                }
                else
                {
                    scanTimerOrTransactionChunkForTimeSeries(typeCode, chunk, filter, fromTime, bucketWidthMs, bucketCount, groupBy, dicts, acc);
                }
            },
            TimeSeriesAccumulator::merge
        );

        // Materialize response arrays
        final long[] timestamps = new long[bucketCount];
        final double[] rps = new double[bucketCount];
        final double[] mean = new double[bucketCount];
        final int[] min = new int[bucketCount];
        final int[] max = new int[bucketCount];
        final double[] p50 = new double[bucketCount];
        final double[] p90 = new double[bucketCount];
        final double[] p95 = new double[bucketCount];
        final double[] p99 = new double[bucketCount];
        final double[] avgDns = new double[bucketCount];
        final double[] avgConnect = new double[bucketCount];
        final double[] avgSend = new double[bucketCount];
        final double[] avgServerBusy = new double[bucketCount];
        final double[] avgReceive = new double[bucketCount];
        final double[] avgTtfb = new double[bucketCount];

        final double bucketSeconds = bucketWidthMs / 1000.0;
        long totalRowsMatched = 0;

        for (int b = 0; b < bucketCount; b++)
        {
            timestamps[b] = fromTime + (long) b * bucketWidthMs;
            final int c = merged.count[b];
            totalRowsMatched += c;
            if (c > 0)
            {
                rps[b] = round2(c / bucketSeconds);
                mean[b] = round2((double) merged.sumRunTime[b] / c);
                min[b] = merged.minRunTime[b];
                max[b] = merged.maxRunTime[b];
                final RuntimeHistogram hist = merged.histograms[b];
                p50[b] = round2(hist.getPercentile(50.0));
                p90[b] = round2(hist.getPercentile(90.0));
                p95[b] = round2(hist.getPercentile(95.0));
                p99[b] = round2(hist.getPercentile(99.0));
                avgDns[b] = round2((double) merged.sumDns[b] / c);
                avgConnect[b] = round2((double) merged.sumConnect[b] / c);
                avgSend[b] = round2((double) merged.sumSend[b] / c);
                avgServerBusy[b] = round2((double) merged.sumServerBusy[b] / c);
                avgReceive[b] = round2((double) merged.sumReceive[b] / c);
                avgTtfb[b] = round2((double) merged.sumTtfb[b] / c);
            }
        }

        // Materialize grouped series if groupBy != NONE
        final List<GroupedSeries> groupedSeriesList;
        if (hasGroupBy && !merged.groups.isEmpty())
        {
            groupedSeriesList = buildTopGroupedSeries(merged.groups, bucketCount, bucketSeconds);
        }
        else
        {
            groupedSeriesList = Collections.emptyList();
        }

        final long queryTimeMs = System.currentTimeMillis() - queryStart;
        return new TimeSeriesResponse(
            String.valueOf(typeCode),
            fromTime,
            toTime,
            bucketWidthMs,
            bucketCount,
            queryTimeMs,
            chunksScanned.get(),
            totalRowsMatched,
            timestamps,
            merged.count,
            rps,
            merged.errors,
            mean,
            min,
            max,
            p50,
            p90,
            p95,
            p99,
            merged.bytesSent,
            merged.bytesReceived,
            avgDns,
            avgConnect,
            avgSend,
            avgServerBusy,
            avgReceive,
            avgTtfb,
            groupedSeriesList
        );
    }

    /**
     * Resolves the optimal time-series bucket width in milliseconds for a given time span.
     *
     * @param fromTime
     *            start timestamp in ms
     * @param toTime
     *            end timestamp in ms
     * @param requestedBucketWidthMs
     *            explicit bucket width requested by caller, or {@code <= 0} for automatic resolution
     * @return bucket width in milliseconds
     */
    public static int resolveBucketWidthMs(final long fromTime, final long toTime, final int requestedBucketWidthMs)
    {
        final long span = Math.max(1L, toTime - fromTime);
        int width;
        if (requestedBucketWidthMs > 0)
        {
            width = requestedBucketWidthMs;
        }
        else if (span > 6L * 3600_000L)
        {
            width = 60_000;
        }
        else if (span > 3600_000L)
        {
            width = 10_000;
        }
        else if (span > 15L * 60_000L)
        {
            width = 5_000;
        }
        else if (span > 2L * 60_000L)
        {
            width = 1_000;
        }
        else
        {
            width = 100;
        }

        // Clamp bucket width so total buckets never exceed MAX_BUCKETS
        if (span / width >= MAX_BUCKETS)
        {
            width = (int) ((span / (MAX_BUCKETS - 1)) + 1);
        }
        return Math.max(10, width);
    }

    private void scanRequestChunkForTimeSeries(final Chunk chunk,
                                               final ResolvedFilter filter,
                                               final long fromTime,
                                               final int bucketWidthMs,
                                               final int bucketCount,
                                               final String groupBy,
                                               final GlobalDictionaries dicts,
                                               final TimeSeriesAccumulator acc)
    {
        final int rowCount = chunk.getRowCount();
        if (rowCount == 0)
        {
            return;
        }

        final RequestChunkIndex idx = getOrBuildRequestIndex(chunk);
        final long baseTime = chunk.getMinTime();
        final RoaringBitmap failedRows;
        final boolean hasFailedRows;

        final int[] timeOffsets;
        final int[] runTimes;
        final int[] statusCodes;
        final int constStatus;
        int[] connectTimes = null;
        int[] sendTimes = null;
        int[] busyTimes = null;
        int[] receiveTimes = null;

        final boolean needTimer = filter.timerIds != null || "TIMER".equals(groupBy);
        final boolean needAgent = filter.agentTestCaseIds != null || "AGENT".equals(groupBy) || "SCENARIO".equals(groupBy);
        final boolean needUsedIp = filter.usedIpIds != null || "USED_IP".equals(groupBy);
        final boolean needMethod = filter.httpMethodIds != null || "HTTP_METHOD".equals(groupBy);
        final boolean needContentType = filter.contentTypeIds != null || "CONTENT_TYPE".equals(groupBy);

        int[] timerIds = null;
        final int constTimerId;
        int[] agentIds = null;
        final int constAgentId;
        int[] usedIpIds = null;
        final int constUsedIpId;
        int[] methodIds = null;
        final int constMethodId;
        int[] contentTypeIds = null;
        final int constContentTypeId;

        if (idx != null)
        {
            failedRows = idx.failedRows;
            hasFailedRows = idx.hasFailedRows;

            if (idx.minStatusCode >= 0 && idx.minStatusCode == idx.maxStatusCode)
            {
                constStatus = idx.minStatusCode;
                statusCodes = null;
            }
            else
            {
                statusCodes = readDecompressedColumn(idx, IDX_RESPONSE_CODES);
                constStatus = -1;
            }

            // Early exit if learned status code bounds prove no row in this chunk can match
            if (idx.minStatusCode >= 0)
            {
                if (filter.minStatusCode != null && idx.maxStatusCode < filter.minStatusCode)
                {
                    return;
                }
                if (filter.maxStatusCode != null && idx.minStatusCode > filter.maxStatusCode)
                {
                    return;
                }
                if (filter.failedOnly && !hasFailedRows && idx.maxStatusCode < 400 && idx.minStatusCode > 0)
                {
                    return;
                }
            }

            timeOffsets = readDecompressedColumn(idx, IDX_TIME_OFFSETS);
            runTimes = readDecompressedColumn(idx, IDX_RUN_TIMES);

            constTimerId = idx.singleTimerId;
            if (filter.timerIds != null && constTimerId < 0)
            {
                timerIds = readDecompressedColumn(idx, IDX_TIMER_IDS);
            }

            constAgentId = idx.singleAgentId;
            if (filter.agentTestCaseIds != null && constAgentId < 0)
            {
                agentIds = readDecompressedColumn(idx, IDX_AGENT_IDS);
            }

            constUsedIpId = idx.singleUsedIpId >= 0 ? idx.singleUsedIpId : -1;
            if (filter.usedIpIds != null && constUsedIpId < 0)
            {
                usedIpIds = readDecompressedColumn(idx, IDX_USED_IPS);
            }

            constMethodId = idx.singleMethodId >= 0 ? idx.singleMethodId : -1;
            if (filter.httpMethodIds != null && constMethodId < 0)
            {
                methodIds = readDecompressedColumn(idx, IDX_HTTP_METHODS);
            }

            constContentTypeId = idx.singleContentTypeId >= 0 ? idx.singleContentTypeId : -1;
            if (filter.contentTypeIds != null && constContentTypeId < 0)
            {
                contentTypeIds = readDecompressedColumn(idx, IDX_CONTENT_TYPES);
            }
        }
        else
        {
            final int colMask = computeRequestColumnMask(filter, groupBy, false);
            final RequestChunk rc = RequestChunkHandler.loadSelectedColumns(chunk, colMask);
            failedRows = rc.getFailedRows();
            hasFailedRows = failedRows != null && !failedRows.isEmpty();
            timeOffsets = FastIntegerCodec.decompress(rc.getCompTimeOffsets());
            runTimes = FastIntegerCodec.decompress(rc.getCompRunTimes());
            statusCodes = FastIntegerCodec.decompress(rc.getCompResponseCodes());
            constStatus = -1;
            connectTimes = FastIntegerCodec.decompress(rc.getCompConnectTimes());
            sendTimes = FastIntegerCodec.decompress(rc.getCompSendTimes());
            busyTimes = FastIntegerCodec.decompress(rc.getCompServerBusyTimes());
            receiveTimes = FastIntegerCodec.decompress(rc.getCompReceiveTimes());
            constTimerId = -1;
            timerIds = needTimer ? FastIntegerCodec.decompress(rc.getCompTimerNameIds()) : null;
            constAgentId = -1;
            agentIds = needAgent ? FastIntegerCodec.decompress(rc.getCompAgentTestCaseIds()) : null;
            constUsedIpId = -1;
            usedIpIds = needUsedIp ? FastIntegerCodec.decompress(rc.getCompUsedIps()) : null;
            constMethodId = -1;
            methodIds = needMethod ? FastIntegerCodec.decompress(rc.getCompHttpMethods()) : null;
            constContentTypeId = -1;
            contentTypeIds = needContentType ? FastIntegerCodec.decompress(rc.getCompContentTypes()) : null;
        }

        final boolean hasGroupBy = !"NONE".equals(groupBy);
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int rt = runTimes[i];
            final int status = statusCodes != null ? statusCodes[i] : constStatus;
            final boolean isFailed = (hasFailedRows && failedRows.contains(i)) || status >= 400 || status == 0;
            int timerId = timerIds != null ? timerIds[i] : constTimerId;
            int agentId = agentIds != null ? agentIds[i] : constAgentId;
            int usedIpId = usedIpIds != null ? usedIpIds[i] : constUsedIpId;
            int methodId = methodIds != null ? methodIds[i] : constMethodId;
            int contentTypeId = contentTypeIds != null ? contentTypeIds[i] : constContentTypeId;

            if (!filter.matchesRequestRow(time, rt, status, isFailed, timerId, agentId, usedIpId, methodId, contentTypeId))
            {
                continue;
            }

            if (connectTimes == null && idx != null)
            {
                connectTimes = readDecompressedColumn(idx, IDX_CONNECT_TIMES);
                sendTimes = readDecompressedColumn(idx, IDX_SEND_TIMES);
                busyTimes = readDecompressedColumn(idx, IDX_SERVER_BUSY_TIMES);
                receiveTimes = readDecompressedColumn(idx, IDX_RECEIVE_TIMES);
                if (needTimer && timerIds == null && constTimerId < 0)
                {
                    timerIds = readDecompressedColumn(idx, IDX_TIMER_IDS);
                    timerId = timerIds[i];
                }
                if (needAgent && agentIds == null && constAgentId < 0)
                {
                    agentIds = readDecompressedColumn(idx, IDX_AGENT_IDS);
                    agentId = agentIds[i];
                }
                if (needUsedIp && usedIpIds == null && constUsedIpId < 0)
                {
                    usedIpIds = readDecompressedColumn(idx, IDX_USED_IPS);
                    usedIpId = usedIpIds[i];
                }
                if (needMethod && methodIds == null && constMethodId < 0)
                {
                    methodIds = readDecompressedColumn(idx, IDX_HTTP_METHODS);
                    methodId = methodIds[i];
                }
                if (needContentType && contentTypeIds == null && constContentTypeId < 0)
                {
                    contentTypeIds = readDecompressedColumn(idx, IDX_CONTENT_TYPES);
                    contentTypeId = contentTypeIds[i];
                }
            }

            final int b = Math.min(bucketCount - 1, Math.max(0, (int) ((time - fromTime) / bucketWidthMs)));
            acc.recordRequest(
                b,
                rt,
                isFailed,
                0,
                0,
                0,
                connectTimes[i],
                sendTimes[i],
                busyTimes[i],
                receiveTimes[i],
                0
            );

            if (hasGroupBy)
            {
                final String groupKey = resolveGroupKey(
                    groupBy, timerId, agentId, status, usedIpId, methodId, contentTypeId,
                    timerNames, agentPairs, strings
                );
                acc.recordGroup(groupKey, b, rt, isFailed);
            }
        }
    }

    private void scanTimerOrTransactionChunkForTimeSeries(final char typeCode,
                                                          final Chunk chunk,
                                                          final ResolvedFilter filter,
                                                          final long fromTime,
                                                          final int bucketWidthMs,
                                                          final int bucketCount,
                                                          final String groupBy,
                                                          final GlobalDictionaries dicts,
                                                          final TimeSeriesAccumulator acc)
    {
        final DecompressedTimerChunk dtc = decompressTimerOrTransactionChunk(typeCode, chunk, dicts);
        if (dtc == null)
        {
            return;
        }

        final boolean hasGroupBy = !"NONE".equals(groupBy);
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

        for (int i = 0; i < dtc.rowCount; i++)
        {
            final long time = dtc.baseTime + dtc.timeOffsets[i];
            final int rt = dtc.runTimes[i];
            final boolean isFailed = dtc.hasFailedRows && dtc.failedRows.contains(i);
            final int timerId = dtc.timerIds[i];
            final int agentId = dtc.agentIds[i];

            if (!filter.matchesTimerRow(time, rt, isFailed, timerId, agentId))
            {
                continue;
            }

            final int b = Math.min(bucketCount - 1, Math.max(0, (int) ((time - fromTime) / bucketWidthMs)));
            acc.recordTimer(b, rt, isFailed);

            if (hasGroupBy)
            {
                final String groupKey = resolveGroupKey(
                    groupBy, timerId, agentId, isFailed ? 500 : 200, -1, -1, -1,
                    timerNames, agentPairs, strings
                );
                acc.recordGroup(groupKey, b, rt, isFailed);
            }
        }
    }

    private record DecompressedTimerChunk(
        int rowCount,
        long baseTime,
        int[] timeOffsets,
        int[] timerIds,
        int[] agentIds,
        int[] runTimes,
        RoaringBitmap failedRows,
        boolean hasFailedRows
    )
    {
    }

    private DecompressedTimerChunk decompressTimerOrTransactionChunk(final char typeCode,
                                                                     final Chunk chunk,
                                                                     final GlobalDictionaries dicts)
    {
        final Chunk resolved = resolveNonRequestChunk(typeCode, chunk, dicts);
        return buildDecompressedTimerChunk(resolved);
    }

    private static DecompressedTimerChunk buildDecompressedTimerChunk(final Chunk resolved)
    {
        final int rowCount = resolved.getRowCount();
        if (rowCount == 0)
        {
            return null;
        }

        final long baseTime = resolved.getMinTime();
        if (resolved instanceof TransactionChunk tc)
        {
            final RoaringBitmap failed = tc.getFailedRows();
            return new DecompressedTimerChunk(
                rowCount,
                baseTime,
                FastIntegerCodec.decompress(tc.getCompTimeOffsets()),
                FastIntegerCodec.decompress(tc.getCompTimerNameIds()),
                FastIntegerCodec.decompress(tc.getCompAgentTestCaseIds()),
                FastIntegerCodec.decompress(tc.getCompRunTimes()),
                failed,
                failed != null && !failed.isEmpty()
            );
        }
        if (resolved instanceof TimerChunk tmc)
        {
            final RoaringBitmap failed = tmc.getFailedRows();
            return new DecompressedTimerChunk(
                rowCount,
                baseTime,
                FastIntegerCodec.decompress(tmc.getCompTimeOffsets()),
                FastIntegerCodec.decompress(tmc.getCompTimerNameIds()),
                FastIntegerCodec.decompress(tmc.getCompAgentTestCaseIds()),
                FastIntegerCodec.decompress(tmc.getCompRunTimes()),
                failed,
                failed != null && !failed.isEmpty()
            );
        }
        return null;
    }

    private List<GroupedSeries> buildTopGroupedSeries(final Map<String, GroupBucketAccumulator> groups,
                                                      final int bucketCount,
                                                      final double bucketSeconds)
    {
        final List<Map.Entry<String, GroupBucketAccumulator>> sorted = new ArrayList<>(groups.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue().totalCount, a.getValue().totalCount));

        final List<GroupedSeries> result = new ArrayList<>(Math.min(sorted.size(), MAX_GROUPED_SERIES + 1));
        GroupBucketAccumulator other = null;

        for (int i = 0; i < sorted.size(); i++)
        {
            final Map.Entry<String, GroupBucketAccumulator> entry = sorted.get(i);
            if (i < MAX_GROUPED_SERIES)
            {
                result.add(entry.getValue().toGroupedSeries(entry.getKey(), bucketSeconds));
            }
            else
            {
                if (other == null)
                {
                    other = new GroupBucketAccumulator(bucketCount);
                }
                other.merge(entry.getValue());
            }
        }

        if (other != null && other.totalCount > 0)
        {
            result.add(other.toGroupedSeries("Other", bucketSeconds));
        }
        return result;
    }

    // =========================================================================
    // 3. Multi-Dimensional Summary Table Engine (POST /api/v1/summary-table)
    // =========================================================================

    /**
     * Executes a multi-dimensional GROUP BY aggregation over the active time window and filter set,
     * computing exact {@link RuntimeHistogram} percentiles ({@code p50}, {@code p90}, {@code p95},
     * {@code p99}, {@code p999}), error rates, throughput, network timing breakdowns, and distinct URL counts.
     *
     * @param spec
     *            the summary table query specification
     * @return aggregated summary table rows sorted by count descending
     */
    public SummaryTableResponse querySummaryTable(final SummaryTableQuerySpec spec)
    {
        Objects.requireNonNull(spec, "spec cannot be null");
        final long queryStart = System.currentTimeMillis();
        final char typeCode = parseRecordType(spec.recordType());
        final String groupBy = normalizeGroupBy(spec.groupBy(), "TIMER");

        final ResolvedFilter filter = resolveFilter(
            typeCode,
            spec.fromTime(),
            spec.toTime(),
            spec.timers(),
            spec.timerQuery(),
            spec.agents(),
            spec.scenarios(),
            spec.usedIps(),
            spec.httpMethods(),
            spec.contentTypes(),
            spec.minStatusCode(),
            spec.maxStatusCode(),
            spec.failedOnly(),
            spec.minRunTime()
        );

        final List<Chunk> candidateChunks = pruneCandidateChunks(typeCode, filter);
        final GlobalDictionaries dicts = storage.getDictionaries();
        final boolean includeUrlHashes = typeCode == 'R'
                                         && "TIMER".equals(groupBy)
                                         && candidateChunks.size() <= MAX_CHUNKS_FOR_EXACT_URL_BITMAP;
        final AtomicInteger chunksScanned = new AtomicInteger(0);

        // Execute parallel scan using one accumulator map per ForkJoin worker thread
        final Map<String, SummaryGroupAccumulator> mergedGroups = candidateChunks.parallelStream().collect(
            HashMap::new,
            (localMap, chunk) -> {
                chunksScanned.incrementAndGet();
                if (typeCode == 'R')
                {
                    scanRequestChunkForSummary(chunk, filter, groupBy, includeUrlHashes, dicts, localMap);
                }
                else
                {
                    scanTimerOrTransactionChunkForSummary(typeCode, chunk, filter, groupBy, dicts, localMap);
                }
            },
            (accMap, chunkMap) -> {
                for (final Map.Entry<String, SummaryGroupAccumulator> e : chunkMap.entrySet())
                {
                    accMap.merge(e.getKey(), e.getValue(), SummaryGroupAccumulator::merge);
                }
            }
        );

        final double windowSeconds = Math.max(0.001, (filter.toTime - filter.fromTime) / 1000.0);
        final List<SummaryRow> rows = new ArrayList<>(mergedGroups.size());
        long totalRowsMatched = 0;

        for (final Map.Entry<String, SummaryGroupAccumulator> entry : mergedGroups.entrySet())
        {
            final SummaryGroupAccumulator g = entry.getValue();
            totalRowsMatched += g.count;

            String sampleUrl = null;
            int fallbackDistinctUrls = 0;
            if (g.representativeTimerId >= 0)
            {
                final GlobalDictionaries.CachedSampleUrl[] samples = dicts.getCachedSampleUrls(g.representativeTimerId);
                if (samples != null && samples.length > 0)
                {
                    sampleUrl = samples[0].url();
                    fallbackDistinctUrls = samples.length;
                }
            }

            final int distinctUrls = g.urlHashes != null ? g.urlHashes.getCardinality() : fallbackDistinctUrls;
            rows.add(g.toSummaryRow(entry.getKey(), windowSeconds, distinctUrls, sampleUrl));
        }

        rows.sort((a, b) -> Long.compare(b.count(), a.count()));

        final long queryTimeMs = System.currentTimeMillis() - queryStart;
        return new SummaryTableResponse(
            groupBy,
            String.valueOf(typeCode),
            filter.fromTime,
            filter.toTime,
            queryTimeMs,
            chunksScanned.get(),
            totalRowsMatched,
            rows
        );
    }

    private void scanRequestChunkForSummary(final Chunk chunk,
                                            final ResolvedFilter filter,
                                            final String groupBy,
                                            final boolean includeUrlHashes,
                                            final GlobalDictionaries dicts,
                                            final Map<String, SummaryGroupAccumulator> localMap)
    {
        final int rowCount = chunk.getRowCount();
        if (rowCount == 0)
        {
            return;
        }

        final RequestChunkIndex idx = getOrBuildRequestIndex(chunk);
        final long baseTime = chunk.getMinTime();
        final RoaringBitmap failedRows;
        final boolean hasFailedRows;

        final int[] timeOffsets;
        final int[] runTimes;
        final int[] statusCodes;
        final int constStatus;
        int[] bytesSent = null;
        int[] bytesReceived = null;
        int[] dnsTimes = null;
        int[] connectTimes = null;
        int[] sendTimes = null;
        int[] busyTimes = null;
        int[] receiveTimes = null;
        int[] ttfbTimes = null;
        int[] ttlbTimes = null;
        int[] urlHashes = null;

        final boolean needTimer = filter.timerIds != null || "TIMER".equals(groupBy);
        final boolean needAgent = filter.agentTestCaseIds != null || "AGENT".equals(groupBy) || "SCENARIO".equals(groupBy);
        final boolean needUsedIp = filter.usedIpIds != null || "USED_IP".equals(groupBy);
        final boolean needMethod = filter.httpMethodIds != null || "HTTP_METHOD".equals(groupBy);
        final boolean needContentType = filter.contentTypeIds != null || "CONTENT_TYPE".equals(groupBy);

        int[] timerIds = null;
        final int constTimerId;
        int[] agentIds = null;
        final int constAgentId;
        int[] usedIpIds = null;
        final int constUsedIpId;
        int[] methodIds = null;
        final int constMethodId;
        int[] contentTypeIds = null;
        final int constContentTypeId;

        if (idx != null)
        {
            failedRows = idx.failedRows;
            hasFailedRows = idx.hasFailedRows;

            if (idx.minStatusCode >= 0 && idx.minStatusCode == idx.maxStatusCode)
            {
                constStatus = idx.minStatusCode;
                statusCodes = null;
            }
            else
            {
                statusCodes = readDecompressedColumn(idx, IDX_RESPONSE_CODES);
                constStatus = -1;
            }

            // Early exit if learned status code bounds prove no row in this chunk can match
            if (idx.minStatusCode >= 0)
            {
                if (filter.minStatusCode != null && idx.maxStatusCode < filter.minStatusCode)
                {
                    return;
                }
                if (filter.maxStatusCode != null && idx.minStatusCode > filter.maxStatusCode)
                {
                    return;
                }
                if (filter.failedOnly && !hasFailedRows && idx.maxStatusCode < 400 && idx.minStatusCode > 0)
                {
                    return;
                }
            }

            timeOffsets = readDecompressedColumn(idx, IDX_TIME_OFFSETS);
            runTimes = readDecompressedColumn(idx, IDX_RUN_TIMES);

            constTimerId = idx.singleTimerId;
            if (filter.timerIds != null && constTimerId < 0)
            {
                timerIds = readDecompressedColumn(idx, IDX_TIMER_IDS);
            }

            constAgentId = idx.singleAgentId;
            if (filter.agentTestCaseIds != null && constAgentId < 0)
            {
                agentIds = readDecompressedColumn(idx, IDX_AGENT_IDS);
            }

            constUsedIpId = idx.singleUsedIpId >= 0 ? idx.singleUsedIpId : -1;
            if (filter.usedIpIds != null && constUsedIpId < 0)
            {
                usedIpIds = readDecompressedColumn(idx, IDX_USED_IPS);
            }

            constMethodId = idx.singleMethodId >= 0 ? idx.singleMethodId : -1;
            if (filter.httpMethodIds != null && constMethodId < 0)
            {
                methodIds = readDecompressedColumn(idx, IDX_HTTP_METHODS);
            }

            constContentTypeId = idx.singleContentTypeId >= 0 ? idx.singleContentTypeId : -1;
            if (filter.contentTypeIds != null && constContentTypeId < 0)
            {
                contentTypeIds = readDecompressedColumn(idx, IDX_CONTENT_TYPES);
            }
        }
        else
        {
            final int colMask = computeRequestColumnMask(filter, groupBy, includeUrlHashes);
            final RequestChunk rc = RequestChunkHandler.loadSelectedColumns(chunk, colMask);
            failedRows = rc.getFailedRows();
            hasFailedRows = failedRows != null && !failedRows.isEmpty();
            timeOffsets = FastIntegerCodec.decompress(rc.getCompTimeOffsets());
            runTimes = FastIntegerCodec.decompress(rc.getCompRunTimes());
            statusCodes = FastIntegerCodec.decompress(rc.getCompResponseCodes());
            constStatus = -1;
            bytesSent = FastIntegerCodec.decompress(rc.getCompBytesSent());
            bytesReceived = FastIntegerCodec.decompress(rc.getCompBytesReceived());
            dnsTimes = FastIntegerCodec.decompress(rc.getCompDnsTimes());
            connectTimes = FastIntegerCodec.decompress(rc.getCompConnectTimes());
            sendTimes = FastIntegerCodec.decompress(rc.getCompSendTimes());
            busyTimes = FastIntegerCodec.decompress(rc.getCompServerBusyTimes());
            receiveTimes = FastIntegerCodec.decompress(rc.getCompReceiveTimes());
            ttfbTimes = FastIntegerCodec.decompress(rc.getCompTimeToFirstBytes());
            ttlbTimes = FastIntegerCodec.decompress(rc.getCompTimeToLastBytes());
            urlHashes = includeUrlHashes ? FastIntegerCodec.decompress(rc.getCompUrlHashes()) : null;
            constTimerId = -1;
            timerIds = needTimer ? FastIntegerCodec.decompress(rc.getCompTimerNameIds()) : null;
            constAgentId = -1;
            agentIds = needAgent ? FastIntegerCodec.decompress(rc.getCompAgentTestCaseIds()) : null;
            constUsedIpId = -1;
            usedIpIds = needUsedIp ? FastIntegerCodec.decompress(rc.getCompUsedIps()) : null;
            constMethodId = -1;
            methodIds = needMethod ? FastIntegerCodec.decompress(rc.getCompHttpMethods()) : null;
            constContentTypeId = -1;
            contentTypeIds = needContentType ? FastIntegerCodec.decompress(rc.getCompContentTypes()) : null;
        }

        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int rt = runTimes[i];
            final int status = statusCodes != null ? statusCodes[i] : constStatus;
            final boolean isFailed = (hasFailedRows && failedRows.contains(i)) || status >= 400 || status == 0;
            int timerId = timerIds != null ? timerIds[i] : constTimerId;
            int agentId = agentIds != null ? agentIds[i] : constAgentId;
            int usedIpId = usedIpIds != null ? usedIpIds[i] : constUsedIpId;
            int methodId = methodIds != null ? methodIds[i] : constMethodId;
            int contentTypeId = contentTypeIds != null ? contentTypeIds[i] : constContentTypeId;

            if (!filter.matchesRequestRow(time, rt, status, isFailed, timerId, agentId, usedIpId, methodId, contentTypeId))
            {
                continue;
            }

            if (bytesSent == null && idx != null)
            {
                bytesSent = readDecompressedColumn(idx, IDX_BYTES_SENT);
                bytesReceived = readDecompressedColumn(idx, IDX_BYTES_RECEIVED);
                dnsTimes = readDecompressedColumn(idx, IDX_DNS_TIMES);
                connectTimes = readDecompressedColumn(idx, IDX_CONNECT_TIMES);
                sendTimes = readDecompressedColumn(idx, IDX_SEND_TIMES);
                busyTimes = readDecompressedColumn(idx, IDX_SERVER_BUSY_TIMES);
                receiveTimes = readDecompressedColumn(idx, IDX_RECEIVE_TIMES);
                ttfbTimes = readDecompressedColumn(idx, IDX_TTFB);
                ttlbTimes = readDecompressedColumn(idx, IDX_TTLB);
                if (includeUrlHashes)
                {
                    urlHashes = readDecompressedColumn(idx, IDX_URL_HASHES);
                }
                if (needTimer && timerIds == null && constTimerId < 0)
                {
                    timerIds = readDecompressedColumn(idx, IDX_TIMER_IDS);
                    timerId = timerIds[i];
                }
                if (needAgent && agentIds == null && constAgentId < 0)
                {
                    agentIds = readDecompressedColumn(idx, IDX_AGENT_IDS);
                    agentId = agentIds[i];
                }
                if (needUsedIp && usedIpIds == null && constUsedIpId < 0)
                {
                    usedIpIds = readDecompressedColumn(idx, IDX_USED_IPS);
                    usedIpId = usedIpIds[i];
                }
                if (needMethod && methodIds == null && constMethodId < 0)
                {
                    methodIds = readDecompressedColumn(idx, IDX_HTTP_METHODS);
                    methodId = methodIds[i];
                }
                if (needContentType && contentTypeIds == null && constContentTypeId < 0)
                {
                    contentTypeIds = readDecompressedColumn(idx, IDX_CONTENT_TYPES);
                    contentTypeId = contentTypeIds[i];
                }
            }

            final String key = resolveGroupKey(
                groupBy, timerId, agentId, status, usedIpId, methodId, contentTypeId,
                timerNames, agentPairs, strings
            );

            SummaryGroupAccumulator groupAcc = localMap.get(key);
            if (groupAcc == null)
            {
                groupAcc = new SummaryGroupAccumulator(includeUrlHashes);
                if ("TIMER".equals(groupBy))
                {
                    groupAcc.representativeTimerId = timerId;
                }
                localMap.put(key, groupAcc);
            }

            final int urlHash = urlHashes != null ? urlHashes[i] : 0;
            groupAcc.recordRequest(
                rt,
                isFailed,
                bytesSent[i],
                bytesReceived[i],
                dnsTimes[i],
                connectTimes[i],
                sendTimes[i],
                busyTimes[i],
                receiveTimes[i],
                ttfbTimes[i],
                ttlbTimes[i],
                urlHash
            );
        }
    }

    private void scanTimerOrTransactionChunkForSummary(final char typeCode,
                                                       final Chunk chunk,
                                                       final ResolvedFilter filter,
                                                       final String groupBy,
                                                       final GlobalDictionaries dicts,
                                                       final Map<String, SummaryGroupAccumulator> localMap)
    {
        final DecompressedTimerChunk dtc = decompressTimerOrTransactionChunk(typeCode, chunk, dicts);
        if (dtc == null)
        {
            return;
        }

        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

        for (int i = 0; i < dtc.rowCount; i++)
        {
            final long time = dtc.baseTime + dtc.timeOffsets[i];
            final int rt = dtc.runTimes[i];
            final boolean isFailed = dtc.hasFailedRows && dtc.failedRows.contains(i);
            final int timerId = dtc.timerIds[i];
            final int agentId = dtc.agentIds[i];

            if (!filter.matchesTimerRow(time, rt, isFailed, timerId, agentId))
            {
                continue;
            }

            final String key = resolveGroupKey(
                groupBy, timerId, agentId, isFailed ? 500 : 200, -1, -1, -1,
                timerNames, agentPairs, strings
            );

            SummaryGroupAccumulator groupAcc = localMap.get(key);
            if (groupAcc == null)
            {
                groupAcc = new SummaryGroupAccumulator(false);
                localMap.put(key, groupAcc);
            }
            groupAcc.recordTimer(rt, isFailed);
        }
    }

    // =========================================================================
    // 4. Forensic Outlier & Failure Inspector (POST /api/v1/inspect)
    // =========================================================================

    /**
     * Inspects top-K slowest requests, failed requests, or failed transactions using Zone Map chunk pruning
     * and on-demand Cold Forensic Tail Block inflation.
     *
     * @param spec
     *            the inspection query specification
     * @return top-K forensic diagnostic records
     */
    public InspectResponse inspectOutliers(final InspectQuerySpec spec)
    {
        Objects.requireNonNull(spec, "spec cannot be null");
        final long queryStart = System.currentTimeMillis();
        final String mode = normalizeInspectMode(spec.mode());
        final int limit = spec.limit() > 0
            ? Math.min(spec.limit(), MAX_INSPECT_LIMIT)
            : DEFAULT_INSPECT_LIMIT;

        final char typeCode = "FAILED_TRANSACTIONS".equals(mode) ? 'T' : 'R';
        final boolean forceFailed = "FAILED_REQUESTS".equals(mode) || "FAILED_TRANSACTIONS".equals(mode)
                                    || Boolean.TRUE.equals(spec.failedOnly());

        final ResolvedFilter filter = resolveFilter(
            typeCode,
            spec.fromTime(),
            spec.toTime(),
            spec.timers(),
            spec.timerQuery(),
            spec.agents(),
            spec.scenarios(),
            spec.usedIps(),
            spec.httpMethods(),
            spec.contentTypes(),
            spec.minStatusCode(),
            spec.maxStatusCode(),
            forceFailed,
            spec.minRunTime()
        );

        if ("FAILED_TRANSACTIONS".equals(mode))
        {
            return inspectFailedTransactions(filter, limit, queryStart);
        }
        return inspectRequests(mode, filter, limit, queryStart);
    }

    private InspectResponse inspectFailedTransactions(final ResolvedFilter filter,
                                                      final int limit,
                                                      final long queryStart)
    {
        final List<Chunk> candidateChunks = pruneCandidateChunks('T', filter);
        final GlobalDictionaries dicts = storage.getDictionaries();

        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

        final List<ForensicRecord> records = new ArrayList<>();
        int totalMatching = 0;
        int chunksScanned = 0;

        for (final Chunk chunk : candidateChunks)
        {
            chunksScanned++;
            final TransactionChunk tc = (TransactionChunk) resolveNonRequestChunk('T', chunk, dicts);
            final RoaringBitmap failedRows = tc.getFailedRows();
            if (failedRows == null || failedRows.isEmpty())
            {
                continue;
            }

            final long baseTime = tc.getMinTime();
            final int[] timeOffsets = FastIntegerCodec.decompress(tc.getCompTimeOffsets());
            final int[] timerIds = FastIntegerCodec.decompress(tc.getCompTimerNameIds());
            final int[] agentIds = FastIntegerCodec.decompress(tc.getCompAgentTestCaseIds());
            final int[] runTimes = FastIntegerCodec.decompress(tc.getCompRunTimes());
            final int[] userNumbers = FastIntegerCodec.decompress(tc.getCompUserNumbers());
            final Map<Integer, TransactionChunk.TransactionError> errorDetails = tc.getErrorDetails();

            for (final int rowIdx : failedRows)
            {
                final long time = baseTime + timeOffsets[rowIdx];
                final int rt = runTimes[rowIdx];
                final int timerId = timerIds[rowIdx];
                final int agentId = agentIds[rowIdx];
                if (!filter.matchesTimerRow(time, rt, true, timerId, agentId))
                {
                    continue;
                }

                totalMatching++;
                final String timerName = safeLookup(timerNames, timerId);
                final GlobalDictionaries.AgentTestCase pair = safeLookup(agentPairs, agentId);
                final String agent = pair != null ? pair.agentName() : "";
                final String scenario = pair != null ? pair.testCaseName() : "";
                final String userNumber = safeLookup(strings, userNumbers[rowIdx]);

                final TransactionChunk.TransactionError err = errorDetails != null ? errorDetails.get(rowIdx) : null;
                final String failedAction = err != null ? err.failedActionName() : null;
                final String stackTrace = err != null ? err.stackTrace() : null;
                final String dirName = err != null ? err.directoryName() : null;
                final String dumpUrl = buildDumpUrl(agent, scenario, userNumber, dirName);

                records.add(new ForensicRecord(
                    time,
                    "T",
                    timerName,
                    timerName,
                    agent,
                    scenario,
                    userNumber,
                    rt,
                    0,
                    true,
                    null,
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    null,
                    true,
                    null,
                    null,
                    null,
                    failedAction,
                    stackTrace,
                    dirName,
                    dumpUrl
                ));
            }
        }

        // Sort failed transactions by runtime descending (or timestamp ascending if equal)
        records.sort(Comparator.comparingInt(ForensicRecord::runTime).reversed()
                               .thenComparingLong(ForensicRecord::time));
        final List<ForensicRecord> limited = records.size() > limit ? new ArrayList<>(records.subList(0, limit)) : records;

        final long queryTimeMs = System.currentTimeMillis() - queryStart;
        return new InspectResponse("FAILED_TRANSACTIONS", queryTimeMs, chunksScanned, totalMatching, limited);
    }

    private InspectResponse inspectRequests(final String mode,
                                            final ResolvedFilter filter,
                                            final int limit,
                                            final long queryStart)
    {
        final List<Chunk> candidateChunks = new ArrayList<>(pruneCandidateChunks('R', filter));
        final GlobalDictionaries dicts = storage.getDictionaries();

        // For SLOWEST_REQUESTS, sort candidate chunks by maxRunTime descending so the bounded min-heap
        // quickly establishes a high k-th runtime threshold and Zone-Map prunes slower chunks without I/O.
        final boolean bySlowest = "SLOWEST_REQUESTS".equals(mode);
        if (bySlowest)
        {
            candidateChunks.sort((a, b) -> Integer.compare(b.getMaxRunTime(), a.getMaxRunTime()));
        }

        final Comparator<CandidateRequestRow> heapComparator =
            Comparator.comparingInt(CandidateRequestRow::runTime).thenComparingLong(r -> -r.time());
        final PriorityQueue<CandidateRequestRow> topKHeap = new PriorityQueue<>(limit + 1, heapComparator);

        final int inspectColMask = RequestChunkHandler.COL_TIME_OFFSETS
                                   | RequestChunkHandler.COL_TIMER_IDS
                                   | RequestChunkHandler.COL_AGENT_IDS
                                   | RequestChunkHandler.COL_RUN_TIMES
                                   | RequestChunkHandler.COL_RESPONSE_CODES
                                   | RequestChunkHandler.COL_BYTES_SENT
                                   | RequestChunkHandler.COL_BYTES_RECEIVED
                                   | RequestChunkHandler.COL_ALL_NETWORK_TIMINGS
                                   | RequestChunkHandler.COL_HTTP_METHODS
                                   | RequestChunkHandler.COL_CONTENT_TYPES
                                   | RequestChunkHandler.COL_USED_IPS
                                   | RequestChunkHandler.COL_USER_NUMBERS;

        int chunksScanned = 0;
        int totalMatching = 0;

        for (final Chunk chunk : candidateChunks)
        {
            // Dynamic Zone Map Top-K Pruning: if we already have `limit` rows and the chunk's maxRunTime
            // is strictly less than the smallest runtime in our top-K heap, skip reading the chunk entirely!
            if (bySlowest && topKHeap.size() >= limit && chunk.getMaxRunTime() > 0
                && chunk.getMaxRunTime() < topKHeap.peek().runTime())
            {
                continue;
            }

            final int rowCount = chunk.getRowCount();
            if (rowCount == 0)
            {
                continue;
            }

            chunksScanned++;
            final RequestChunkIndex idx = getOrBuildRequestIndex(chunk);
            final RequestChunk fallbackRc = (idx == null) ? RequestChunkHandler.loadSelectedColumns(chunk, inspectColMask) : null;

            final long baseTime = chunk.getMinTime();
            final RoaringBitmap failedRows = (idx != null) ? idx.failedRows : fallbackRc.getFailedRows();
            final boolean hasFailedRows = (idx != null) ? idx.hasFailedRows : (failedRows != null && !failedRows.isEmpty());

            final int[] timeOffsets = (idx != null)
                ? readDecompressedColumn(idx, IDX_TIME_OFFSETS)
                : FastIntegerCodec.decompress(fallbackRc.getCompTimeOffsets());
            final int[] runTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_RUN_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompRunTimes());

            final int constStatus;
            final int[] statusCodes;
            if (idx != null && idx.minStatusCode >= 0 && idx.minStatusCode == idx.maxStatusCode)
            {
                constStatus = idx.minStatusCode;
                statusCodes = null;
            }
            else
            {
                constStatus = -1;
                statusCodes = (idx != null)
                    ? readDecompressedColumn(idx, IDX_RESPONSE_CODES)
                    : FastIntegerCodec.decompress(fallbackRc.getCompResponseCodes());
            }

            // Read filter columns upfront only if actively filtered (or resolve via O(1) chunk hints)
            int[] timerIds = null;
            final int constTimerId = (idx != null) ? idx.singleTimerId : -1;
            if (filter.timerIds != null && constTimerId < 0)
            {
                timerIds = (idx != null)
                    ? readDecompressedColumn(idx, IDX_TIMER_IDS)
                    : FastIntegerCodec.decompress(fallbackRc.getCompTimerNameIds());
            }

            int[] agentIds = null;
            final int constAgentId = (idx != null) ? idx.singleAgentId : -1;
            if (filter.agentTestCaseIds != null && constAgentId < 0)
            {
                agentIds = (idx != null)
                    ? readDecompressedColumn(idx, IDX_AGENT_IDS)
                    : FastIntegerCodec.decompress(fallbackRc.getCompAgentTestCaseIds());
            }

            int[] usedIpIds = null;
            final int constUsedIpId = (idx != null && idx.singleUsedIpId >= 0) ? idx.singleUsedIpId : -1;
            if (filter.usedIpIds != null && constUsedIpId < 0)
            {
                usedIpIds = (idx != null)
                    ? readDecompressedColumn(idx, IDX_USED_IPS)
                    : FastIntegerCodec.decompress(fallbackRc.getCompUsedIps());
            }

            int[] methodIds = null;
            final int constMethodId = (idx != null && idx.singleMethodId >= 0) ? idx.singleMethodId : -1;
            if (filter.httpMethodIds != null && constMethodId < 0)
            {
                methodIds = (idx != null)
                    ? readDecompressedColumn(idx, IDX_HTTP_METHODS)
                    : FastIntegerCodec.decompress(fallbackRc.getCompHttpMethods());
            }

            int[] contentTypeIds = null;
            final int constContentTypeId = (idx != null && idx.singleContentTypeId >= 0) ? idx.singleContentTypeId : -1;
            if (filter.contentTypeIds != null && constContentTypeId < 0)
            {
                contentTypeIds = (idx != null)
                    ? readDecompressedColumn(idx, IDX_CONTENT_TYPES)
                    : FastIntegerCodec.decompress(fallbackRc.getCompContentTypes());
            }

            // Lazy decompression of secondary display/network arrays only if at least one row in this chunk enters top-K
            int[] bytesSent = null;
            int[] bytesReceived = null;
            int[] dnsTimes = null;
            int[] connectTimes = null;
            int[] sendTimes = null;
            int[] busyTimes = null;
            int[] receiveTimes = null;
            int[] ttfbTimes = null;
            int[] ttlbTimes = null;
            int[] userNumbers = null;

            for (int i = 0; i < rowCount; i++)
            {
                final int rt = runTimes[i];
                final long time = baseTime + timeOffsets[i];
                final int status = statusCodes != null ? statusCodes[i] : constStatus;
                final boolean isFailed = (hasFailedRows && failedRows.contains(i)) || status >= 400 || status == 0;
                final int timerId = timerIds != null ? timerIds[i] : constTimerId;
                final int agentId = agentIds != null ? agentIds[i] : constAgentId;
                final int usedIpId = usedIpIds != null ? usedIpIds[i] : constUsedIpId;
                final int methodId = methodIds != null ? methodIds[i] : constMethodId;
                final int contentTypeId = contentTypeIds != null ? contentTypeIds[i] : constContentTypeId;

                if (!filter.matchesRequestRow(time, rt, status, isFailed, timerId, agentId, usedIpId, methodId, contentTypeId))
                {
                    continue;
                }

                totalMatching++;

                if (topKHeap.size() >= limit && rt <= topKHeap.peek().runTime())
                {
                    continue;
                }

                if (bytesSent == null)
                {
                    if (idx != null)
                    {
                        if (timerIds == null && constTimerId < 0) timerIds = readDecompressedColumn(idx, IDX_TIMER_IDS);
                        if (agentIds == null && constAgentId < 0) agentIds = readDecompressedColumn(idx, IDX_AGENT_IDS);
                        if (usedIpIds == null && constUsedIpId < 0) usedIpIds = readDecompressedColumn(idx, IDX_USED_IPS);
                        if (methodIds == null && constMethodId < 0) methodIds = readDecompressedColumn(idx, IDX_HTTP_METHODS);
                        if (contentTypeIds == null && constContentTypeId < 0) contentTypeIds = readDecompressedColumn(idx, IDX_CONTENT_TYPES);
                        bytesSent = readDecompressedColumn(idx, IDX_BYTES_SENT);
                        bytesReceived = readDecompressedColumn(idx, IDX_BYTES_RECEIVED);
                        dnsTimes = readDecompressedColumn(idx, IDX_DNS_TIMES);
                        connectTimes = readDecompressedColumn(idx, IDX_CONNECT_TIMES);
                        sendTimes = readDecompressedColumn(idx, IDX_SEND_TIMES);
                        busyTimes = readDecompressedColumn(idx, IDX_SERVER_BUSY_TIMES);
                        receiveTimes = readDecompressedColumn(idx, IDX_RECEIVE_TIMES);
                        ttfbTimes = readDecompressedColumn(idx, IDX_TTFB);
                        ttlbTimes = readDecompressedColumn(idx, IDX_TTLB);
                        userNumbers = readDecompressedColumn(idx, IDX_USER_NUMBERS);
                    }
                    else
                    {
                        if (timerIds == null) timerIds = FastIntegerCodec.decompress(fallbackRc.getCompTimerNameIds());
                        if (agentIds == null) agentIds = FastIntegerCodec.decompress(fallbackRc.getCompAgentTestCaseIds());
                        if (usedIpIds == null) usedIpIds = FastIntegerCodec.decompress(fallbackRc.getCompUsedIps());
                        if (methodIds == null) methodIds = FastIntegerCodec.decompress(fallbackRc.getCompHttpMethods());
                        if (contentTypeIds == null) contentTypeIds = FastIntegerCodec.decompress(fallbackRc.getCompContentTypes());
                        bytesSent = FastIntegerCodec.decompress(fallbackRc.getCompBytesSent());
                        bytesReceived = FastIntegerCodec.decompress(fallbackRc.getCompBytesReceived());
                        dnsTimes = FastIntegerCodec.decompress(fallbackRc.getCompDnsTimes());
                        connectTimes = FastIntegerCodec.decompress(fallbackRc.getCompConnectTimes());
                        sendTimes = FastIntegerCodec.decompress(fallbackRc.getCompSendTimes());
                        busyTimes = FastIntegerCodec.decompress(fallbackRc.getCompServerBusyTimes());
                        receiveTimes = FastIntegerCodec.decompress(fallbackRc.getCompReceiveTimes());
                        ttfbTimes = FastIntegerCodec.decompress(fallbackRc.getCompTimeToFirstBytes());
                        ttlbTimes = FastIntegerCodec.decompress(fallbackRc.getCompTimeToLastBytes());
                        userNumbers = FastIntegerCodec.decompress(fallbackRc.getCompUserNumbers());
                    }
                }

                final CandidateRequestRow candidate = new CandidateRequestRow(
                    chunk,
                    i,
                    time,
                    timerIds != null ? timerIds[i] : constTimerId,
                    agentIds != null ? agentIds[i] : constAgentId,
                    userNumbers[i],
                    rt,
                    status,
                    isFailed,
                    methodIds != null ? methodIds[i] : constMethodId,
                    contentTypeIds != null ? contentTypeIds[i] : constContentTypeId,
                    usedIpIds != null ? usedIpIds[i] : constUsedIpId,
                    bytesSent[i],
                    bytesReceived[i],
                    dnsTimes[i],
                    connectTimes[i],
                    sendTimes[i],
                    busyTimes[i],
                    receiveTimes[i],
                    ttfbTimes[i],
                    ttlbTimes[i]
                );

                topKHeap.offer(candidate);
                if (topKHeap.size() > limit)
                {
                    topKHeap.poll();
                }
            }
        }

        // Extract top-K rows sorted by runTime descending
        final List<CandidateRequestRow> winners = new ArrayList<>(topKHeap);
        winners.sort(Comparator.comparingInt(CandidateRequestRow::runTime).reversed()
                               .thenComparingLong(CandidateRequestRow::time));

        // Load Cold Forensic Tail Blocks only for the distinct chunks represented in `winners`
        final Map<Chunk, Map<Integer, RequestChunk.ForensicDetails>> forensicsByChunk = new HashMap<>();
        for (final CandidateRequestRow w : winners)
        {
            forensicsByChunk.computeIfAbsent(w.chunk(), RequestChunk::loadForensicDetails);
        }

        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] strings = dicts.getStringsArray();

        final List<ForensicRecord> result = new ArrayList<>(winners.size());
        for (final CandidateRequestRow w : winners)
        {
            final String timerName = safeLookup(timerNames, w.timerId());
            final GlobalDictionaries.AgentTestCase pair = safeLookup(agentPairs, w.agentId());
            final String agent = pair != null ? pair.agentName() : "";
            final String scenario = pair != null ? pair.testCaseName() : "";
            final String userNumber = safeLookup(strings, w.userNumId());
            final String method = safeLookup(strings, w.methodId());
            final String contentType = safeLookup(strings, w.contentTypeId());
            final String usedIp = safeLookup(strings, w.usedIpId());

            final Map<Integer, RequestChunk.ForensicDetails> chunkForensics = forensicsByChunk.get(w.chunk());
            final RequestChunk.ForensicDetails fd = chunkForensics != null ? chunkForensics.get(w.rowIdx()) : null;

            final String url;
            final boolean forensicExact;
            final String rawName;
            final String requestId;
            final String responseId;
            final String formData;

            if (fd != null)
            {
                url = fd.url();
                forensicExact = true;
                rawName = (fd.rawName() != null && !fd.rawName().isEmpty()) ? fd.rawName() : timerName;
                requestId = fd.requestId();
                responseId = fd.responseId();
                formData = fd.formData();
            }
            else
            {
                final GlobalDictionaries.CachedSampleUrl[] samples = dicts.getCachedSampleUrls(w.timerId());
                url = (samples != null && samples.length > 0) ? samples[0].url() : "";
                forensicExact = false;
                rawName = timerName;
                requestId = "";
                responseId = "";
                formData = "";
            }

            result.add(new ForensicRecord(
                w.time(),
                "R",
                timerName,
                rawName,
                agent,
                scenario,
                userNumber,
                w.runTime(),
                w.statusCode(),
                w.failed(),
                method,
                contentType,
                usedIp,
                w.bytesSent(),
                w.bytesReceived(),
                w.dnsTime(),
                w.connectTime(),
                w.sendTime(),
                w.serverBusyTime(),
                w.receiveTime(),
                w.ttfb(),
                w.ttlb(),
                url,
                forensicExact,
                requestId,
                responseId,
                formData,
                null,
                null,
                null,
                null
            ));
        }

        final long queryTimeMs = System.currentTimeMillis() - queryStart;
        return new InspectResponse(mode, queryTimeMs, chunksScanned, totalMatching, result);
    }

    private record CandidateRequestRow(
        Chunk chunk,
        int rowIdx,
        long time,
        int timerId,
        int agentId,
        int userNumId,
        int runTime,
        int statusCode,
        boolean failed,
        int methodId,
        int contentTypeId,
        int usedIpId,
        int bytesSent,
        int bytesReceived,
        int dnsTime,
        int connectTime,
        int sendTime,
        int serverBusyTime,
        int receiveTime,
        int ttfb,
        int ttlb
    )
    {
    }

    // =========================================================================
    // 5. Virtual User Session Reconstruction (GET /api/v1/session)
    // =========================================================================

    /**
     * Reconstructs the complete chronological execution waterfall for a single virtual user iteration
     * ({@code (agent, scenario, userNumber)}) anchored around {@code targetTime}.
     *
     * @param spec
     *            the session query specification
     * @return reconstructed virtual user iteration tree and navigation boundaries
     */
    public SessionResponse traceUserSession(final SessionQuerySpec spec)
    {
        Objects.requireNonNull(spec, "spec cannot be null");
        final long queryStart = System.currentTimeMillis();

        final String agent = spec.agent() != null ? spec.agent() : "";
        final String scenario = spec.scenario() != null ? spec.scenario() : "";
        final String userNumber = spec.userNumber() != null ? spec.userNumber() : "";
        final long targetTime = spec.targetTime() > 0 ? spec.targetTime() : storage.getMinTime();
        final long paddingMs = spec.paddingMs() > 0 ? spec.paddingMs() : DEFAULT_SESSION_PADDING_MS;

        final GlobalDictionaries dicts = storage.getDictionaries();
        final int agentTestCaseId = findAgentTestCaseId(dicts, agent, scenario);
        final int userNumId = findStringId(dicts, userNumber);

        if (agentTestCaseId < 0 || userNumId < 0)
        {
            final long queryTimeMs = System.currentTimeMillis() - queryStart;
            return new SessionResponse(
                agent, scenario, userNumber, targetTime,
                targetTime - paddingMs, targetTime + paddingMs,
                null, null, queryTimeMs, null, Collections.emptyList()
            );
        }

        final RoaringBitmap agentFilterBitmap = RoaringBitmap.bitmapOf(agentTestCaseId);
        final String[] timerNames = dicts.getTimerNamesArray();
        final String[] strings = dicts.getStringsArray();

        // Step 1: Scan Transaction ('T') chunks for this (agentTestCaseId, userNumId) to resolve the exact iteration bounds
        final List<SessionNode> userTransactions = new ArrayList<>();
        final List<Chunk> txChunks = storage.getCatalog().queryChunks(
            'T', new ScanPredicate(0L, Long.MAX_VALUE, null, agentFilterBitmap)
        );

        for (final Chunk chunk : txChunks)
        {
            final TransactionChunk tc = (TransactionChunk) resolveNonRequestChunk('T', chunk, dicts);
            final int rowCount = tc.getRowCount();
            final long baseTime = tc.getMinTime();
            final int[] timeOffsets = FastIntegerCodec.decompress(tc.getCompTimeOffsets());
            final int[] timerIds = FastIntegerCodec.decompress(tc.getCompTimerNameIds());
            final int[] agentIds = FastIntegerCodec.decompress(tc.getCompAgentTestCaseIds());
            final int[] runTimes = FastIntegerCodec.decompress(tc.getCompRunTimes());
            final int[] userNums = FastIntegerCodec.decompress(tc.getCompUserNumbers());
            final RoaringBitmap failedRows = tc.getFailedRows();
            final Map<Integer, TransactionChunk.TransactionError> errorDetails = tc.getErrorDetails();

            for (int i = 0; i < rowCount; i++)
            {
                if (agentIds[i] != agentTestCaseId || userNums[i] != userNumId)
                {
                    continue;
                }
                final long time = baseTime + timeOffsets[i];
                final int rt = runTimes[i];
                final String name = safeLookup(timerNames, timerIds[i]);
                final boolean failed = failedRows != null && failedRows.contains(i);
                final TransactionChunk.TransactionError err = errorDetails != null ? errorDetails.get(i) : null;
                final String failedAction = err != null ? err.failedActionName() : null;
                final String stackTrace = err != null ? err.stackTrace() : null;
                final String dirName = err != null ? err.directoryName() : null;
                final String dumpUrl = buildDumpUrl(agent, scenario, userNumber, dirName);

                userTransactions.add(new SessionNode(
                    "T", time, rt, time + rt, name, name, failed, 0,
                    null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    null, true, null, null, null, stackTrace, failedAction, dirName, dumpUrl,
                    new ArrayList<>()
                ));
            }
        }

        userTransactions.sort(Comparator.comparingLong(SessionNode::time));

        // Locate enclosing transaction (or closest transaction within paddingMs)
        int matchedTxIdx = -1;
        for (int i = 0; i < userTransactions.size(); i++)
        {
            final SessionNode tx = userTransactions.get(i);
            if (targetTime >= tx.time() && targetTime <= tx.endTime())
            {
                matchedTxIdx = i;
                break;
            }
        }
        if (matchedTxIdx < 0 && !userTransactions.isEmpty())
        {
            long bestDist = Long.MAX_VALUE;
            for (int i = 0; i < userTransactions.size(); i++)
            {
                final SessionNode tx = userTransactions.get(i);
                final long dist = targetTime < tx.time()
                    ? (tx.time() - targetTime)
                    : (targetTime - tx.endTime());
                if (dist <= paddingMs && dist < bestDist)
                {
                    bestDist = dist;
                    matchedTxIdx = i;
                }
            }
        }

        final SessionNode enclosingTx = matchedTxIdx >= 0 ? userTransactions.get(matchedTxIdx) : null;
        final Long prevTxTime = (matchedTxIdx > 0) ? userTransactions.get(matchedTxIdx - 1).time() : null;
        final Long nextTxTime = (matchedTxIdx >= 0 && matchedTxIdx + 1 < userTransactions.size())
            ? userTransactions.get(matchedTxIdx + 1).time() : null;

        final long windowStart = enclosingTx != null ? enclosingTx.time() : (targetTime - paddingMs);
        final long windowEnd = enclosingTx != null ? enclosingTx.endTime() : (targetTime + paddingMs);
        final ScanPredicate windowPredicate = new ScanPredicate(windowStart, windowEnd, null, agentFilterBitmap);

        // Step 2: Collect Actions ('A'), PageLoads ('P'), Custom Timers ('C'), Events ('E'), and Requests ('R')
        final List<SessionNode> actions = new ArrayList<>();
        final List<SessionNode> childItems = new ArrayList<>();

        for (final char timerType : new char[]{'A', 'P', 'C'})
        {
            for (final Chunk chunk : storage.getCatalog().queryChunks(timerType, windowPredicate))
            {
                final TimerChunk tmc = (TimerChunk) resolveNonRequestChunk(timerType, chunk, dicts);
                final int rowCount = tmc.getRowCount();
                final long baseTime = tmc.getMinTime();
                final int[] timeOffsets = FastIntegerCodec.decompress(tmc.getCompTimeOffsets());
                final int[] timerIds = FastIntegerCodec.decompress(tmc.getCompTimerNameIds());
                final int[] agentIds = FastIntegerCodec.decompress(tmc.getCompAgentTestCaseIds());
                final int[] runTimes = FastIntegerCodec.decompress(tmc.getCompRunTimes());
                final int[] userNums = FastIntegerCodec.decompress(tmc.getCompUserNumbers());
                final RoaringBitmap failedRows = tmc.getFailedRows();

                for (int i = 0; i < rowCount; i++)
                {
                    if (agentIds[i] != agentTestCaseId || userNums[i] != userNumId)
                    {
                        continue;
                    }
                    final long time = baseTime + timeOffsets[i];
                    if (time < windowStart || time > windowEnd)
                    {
                        continue;
                    }
                    final int rt = runTimes[i];
                    final String name = safeLookup(timerNames, timerIds[i]);
                    final boolean failed = failedRows != null && failedRows.contains(i);
                    final SessionNode node = new SessionNode(
                        String.valueOf(timerType), time, rt, time + rt, name, name, failed, 0,
                        null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        null, true, null, null, null, null, null, null, null,
                        new ArrayList<>()
                    );
                    if (timerType == 'A')
                    {
                        actions.add(node);
                    }
                    else
                    {
                        childItems.add(node);
                    }
                }
            }
        }

        // Scan Events ('E')
        for (final Chunk chunk : storage.getCatalog().queryChunks('E', windowPredicate))
        {
            final EventChunk ec = (EventChunk) resolveNonRequestChunk('E', chunk, dicts);
            final int rowCount = ec.getRowCount();
            final long baseTime = ec.getMinTime();
            final int[] timeOffsets = FastIntegerCodec.decompress(ec.getCompTimeOffsets());
            final int[] timerIds = FastIntegerCodec.decompress(ec.getCompTimerNameIds());
            final int[] agentIds = FastIntegerCodec.decompress(ec.getCompAgentTestCaseIds());
            final int[] msgIds = FastIntegerCodec.decompress(ec.getCompMessages());
            final int[] userNums = FastIntegerCodec.decompress(ec.getCompUserNumbers());

            for (int i = 0; i < rowCount; i++)
            {
                if (agentIds[i] != agentTestCaseId || userNums[i] != userNumId)
                {
                    continue;
                }
                final long time = baseTime + timeOffsets[i];
                if (time < windowStart || time > windowEnd)
                {
                    continue;
                }
                final String name = safeLookup(timerNames, timerIds[i]);
                final String msg = safeLookup(strings, msgIds[i]);
                childItems.add(new SessionNode(
                    "E", time, 0, time, name, name, false, 0,
                    null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    null, true, null, null, null, msg, null, null, null,
                    Collections.emptyList()
                ));
            }
        }

        // Scan Requests ('R') using column pointer index to read only timeOffsets + userNumbers first
        final int reqMask = RequestChunkHandler.COL_TIME_OFFSETS
                            | RequestChunkHandler.COL_TIMER_IDS
                            | RequestChunkHandler.COL_AGENT_IDS
                            | RequestChunkHandler.COL_RUN_TIMES
                            | RequestChunkHandler.COL_RESPONSE_CODES
                            | RequestChunkHandler.COL_BYTES_SENT
                            | RequestChunkHandler.COL_BYTES_RECEIVED
                            | RequestChunkHandler.COL_ALL_NETWORK_TIMINGS
                            | RequestChunkHandler.COL_HTTP_METHODS
                            | RequestChunkHandler.COL_CONTENT_TYPES
                            | RequestChunkHandler.COL_USED_IPS
                            | RequestChunkHandler.COL_USER_NUMBERS;

        for (final Chunk chunk : storage.getCatalog().queryChunks('R', windowPredicate))
        {
            final int rowCount = chunk.getRowCount();
            if (rowCount == 0)
            {
                continue;
            }

            final RequestChunkIndex idx = getOrBuildRequestIndex(chunk);
            if (idx != null && idx.singleAgentId >= 0 && idx.singleAgentId != agentTestCaseId)
            {
                continue;
            }

            final RequestChunk fallbackRc = (idx == null) ? RequestChunkHandler.loadSelectedColumns(chunk, reqMask) : null;
            final long baseTime = chunk.getMinTime();
            final int[] timeOffsets = (idx != null)
                ? readDecompressedColumn(idx, IDX_TIME_OFFSETS)
                : FastIntegerCodec.decompress(fallbackRc.getCompTimeOffsets());
            final int[] agentIds = (idx != null)
                ? (idx.singleAgentId >= 0 ? null : readDecompressedColumn(idx, IDX_AGENT_IDS))
                : FastIntegerCodec.decompress(fallbackRc.getCompAgentTestCaseIds());
            final int[] userNums = (idx != null)
                ? readDecompressedColumn(idx, IDX_USER_NUMBERS)
                : FastIntegerCodec.decompress(fallbackRc.getCompUserNumbers());

            // First pass: check if this chunk contains any rows for our target virtual user in the time window
            List<Integer> matchingRowIndices = null;
            for (int i = 0; i < rowCount; i++)
            {
                final int aId = agentIds != null ? agentIds[i] : idx.singleAgentId;
                if (aId != agentTestCaseId || userNums[i] != userNumId)
                {
                    continue;
                }
                final long time = baseTime + timeOffsets[i];
                if (time >= windowStart && time <= windowEnd)
                {
                    if (matchingRowIndices == null)
                    {
                        matchingRowIndices = new ArrayList<>();
                    }
                    matchingRowIndices.add(i);
                }
            }

            if (matchingRowIndices == null)
            {
                continue;
            }

            // Decompress the remaining columns and load forensic tail only for this matching chunk
            final int[] timerIds = (idx != null)
                ? (idx.singleTimerId >= 0 ? null : readDecompressedColumn(idx, IDX_TIMER_IDS))
                : FastIntegerCodec.decompress(fallbackRc.getCompTimerNameIds());
            final int[] runTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_RUN_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompRunTimes());
            final int[] statusCodes = (idx != null)
                ? readDecompressedColumn(idx, IDX_RESPONSE_CODES)
                : FastIntegerCodec.decompress(fallbackRc.getCompResponseCodes());
            final int[] bytesSent = (idx != null)
                ? readDecompressedColumn(idx, IDX_BYTES_SENT)
                : FastIntegerCodec.decompress(fallbackRc.getCompBytesSent());
            final int[] bytesReceived = (idx != null)
                ? readDecompressedColumn(idx, IDX_BYTES_RECEIVED)
                : FastIntegerCodec.decompress(fallbackRc.getCompBytesReceived());
            final int[] dnsTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_DNS_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompDnsTimes());
            final int[] connectTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_CONNECT_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompConnectTimes());
            final int[] sendTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_SEND_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompSendTimes());
            final int[] busyTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_SERVER_BUSY_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompServerBusyTimes());
            final int[] receiveTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_RECEIVE_TIMES)
                : FastIntegerCodec.decompress(fallbackRc.getCompReceiveTimes());
            final int[] ttfbTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_TTFB)
                : FastIntegerCodec.decompress(fallbackRc.getCompTimeToFirstBytes());
            final int[] ttlbTimes = (idx != null)
                ? readDecompressedColumn(idx, IDX_TTLB)
                : FastIntegerCodec.decompress(fallbackRc.getCompTimeToLastBytes());
            final int[] methodIds = (idx != null)
                ? readDecompressedColumn(idx, IDX_HTTP_METHODS)
                : FastIntegerCodec.decompress(fallbackRc.getCompHttpMethods());
            final int[] contentTypeIds = (idx != null)
                ? readDecompressedColumn(idx, IDX_CONTENT_TYPES)
                : FastIntegerCodec.decompress(fallbackRc.getCompContentTypes());
            final int[] usedIpIds = (idx != null)
                ? readDecompressedColumn(idx, IDX_USED_IPS)
                : FastIntegerCodec.decompress(fallbackRc.getCompUsedIps());
            final RoaringBitmap failedRows = (idx != null) ? idx.failedRows : fallbackRc.getFailedRows();
            final Map<Integer, RequestChunk.ForensicDetails> forensics = RequestChunk.loadForensicDetails(chunk);

            for (final int rowIdx : matchingRowIndices)
            {
                final long time = baseTime + timeOffsets[rowIdx];
                final int rt = runTimes[rowIdx];
                final int timerId = timerIds != null ? timerIds[rowIdx] : idx.singleTimerId;
                final String timerName = safeLookup(timerNames, timerId);
                final int status = statusCodes[rowIdx];
                final boolean failed = (failedRows != null && failedRows.contains(rowIdx)) || status >= 400 || status == 0;

                final RequestChunk.ForensicDetails fd = forensics != null ? forensics.get(rowIdx) : null;
                final String url;
                final boolean forensicExact;
                final String rawName;
                final String reqId;
                final String respId;
                final String formData;

                if (fd != null)
                {
                    url = fd.url();
                    forensicExact = true;
                    rawName = (fd.rawName() != null && !fd.rawName().isEmpty()) ? fd.rawName() : timerName;
                    reqId = fd.requestId();
                    respId = fd.responseId();
                    formData = fd.formData();
                }
                else
                {
                    final GlobalDictionaries.CachedSampleUrl[] samples = dicts.getCachedSampleUrls(timerId);
                    url = (samples != null && samples.length > 0) ? samples[0].url() : "";
                    forensicExact = false;
                    rawName = timerName;
                    reqId = "";
                    respId = "";
                    formData = "";
                }

                childItems.add(new SessionNode(
                    "R",
                    time,
                    rt,
                    time + rt,
                    timerName,
                    rawName,
                    failed,
                    status,
                    safeLookup(strings, methodIds[rowIdx]),
                    safeLookup(strings, contentTypeIds[rowIdx]),
                    safeLookup(strings, usedIpIds[rowIdx]),
                    bytesSent[rowIdx],
                    bytesReceived[rowIdx],
                    dnsTimes[rowIdx],
                    connectTimes[rowIdx],
                    sendTimes[rowIdx],
                    busyTimes[rowIdx],
                    receiveTimes[rowIdx],
                    ttfbTimes[rowIdx],
                    ttlbTimes[rowIdx],
                    url,
                    forensicExact,
                    reqId,
                    respId,
                    formData,
                    null,
                    null,
                    null,
                    null,
                    Collections.emptyList()
                ));
            }
        }

        // Step 3: Sort actions and child items chronologically and nest child items under their enclosing Action
        actions.sort(Comparator.comparingLong(SessionNode::time));
        childItems.sort(Comparator.comparingLong(SessionNode::time));

        final List<SessionNode> topLevelItems = new ArrayList<>(actions);
        for (final SessionNode child : childItems)
        {
            SessionNode parentAction = null;
            for (int i = actions.size() - 1; i >= 0; i--)
            {
                final SessionNode act = actions.get(i);
                if (child.time() >= act.time() && child.time() <= act.endTime() + 50L)
                {
                    parentAction = act;
                    break;
                }
            }
            if (parentAction != null)
            {
                parentAction.children().add(child);
            }
            else
            {
                topLevelItems.add(child);
            }
        }

        topLevelItems.sort(Comparator.comparingLong(SessionNode::time));

        final long queryTimeMs = System.currentTimeMillis() - queryStart;
        return new SessionResponse(
            agent,
            scenario,
            userNumber,
            targetTime,
            windowStart,
            windowEnd,
            prevTxTime,
            nextTxTime,
            queryTimeMs,
            enclosingTx,
            topLevelItems
        );
    }

    // =========================================================================
    // Internal Filter & Accumulator Helpers
    // =========================================================================

    private static char parseRecordType(final String recordType)
    {
        if (recordType == null || recordType.isBlank())
        {
            return 'R';
        }
        final char c = Character.toUpperCase(recordType.trim().charAt(0));
        return switch (c)
        {
            case 'R', 'A', 'T', 'P', 'C', 'E' -> c;
            default -> 'R';
        };
    }

    private static String normalizeGroupBy(final String groupBy, final String defaultGroupBy)
    {
        if (groupBy == null || groupBy.isBlank())
        {
            return defaultGroupBy;
        }
        return groupBy.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeInspectMode(final String mode)
    {
        if (mode == null || mode.isBlank())
        {
            return "SLOWEST_REQUESTS";
        }
        final String upper = mode.trim().toUpperCase(Locale.ROOT);
        return switch (upper)
        {
            case "FAILED_REQUESTS", "FAILED_TRANSACTIONS", "SLOWEST_REQUESTS" -> upper;
            default -> "SLOWEST_REQUESTS";
        };
    }

    private static int computeRequestColumnMask(final ResolvedFilter filter,
                                                final String groupBy,
                                                final boolean includeUrlHashes)
    {
        int mask = RequestChunkHandler.COL_TIME_OFFSETS
                   | RequestChunkHandler.COL_RUN_TIMES
                   | RequestChunkHandler.COL_RESPONSE_CODES
                   | RequestChunkHandler.COL_BYTES_SENT
                   | RequestChunkHandler.COL_BYTES_RECEIVED
                   | RequestChunkHandler.COL_ALL_NETWORK_TIMINGS;

        if (filter.timerIds != null || "TIMER".equals(groupBy))
        {
            mask |= RequestChunkHandler.COL_TIMER_IDS;
        }
        if (filter.agentTestCaseIds != null || "AGENT".equals(groupBy) || "SCENARIO".equals(groupBy))
        {
            mask |= RequestChunkHandler.COL_AGENT_IDS;
        }
        if (filter.usedIpIds != null || "USED_IP".equals(groupBy))
        {
            mask |= RequestChunkHandler.COL_USED_IPS;
        }
        if (filter.httpMethodIds != null || "HTTP_METHOD".equals(groupBy))
        {
            mask |= RequestChunkHandler.COL_HTTP_METHODS;
        }
        if (filter.contentTypeIds != null || "CONTENT_TYPE".equals(groupBy))
        {
            mask |= RequestChunkHandler.COL_CONTENT_TYPES;
        }
        if (includeUrlHashes)
        {
            mask |= RequestChunkHandler.COL_URL_HASHES;
        }
        return mask;
    }

    private static String resolveGroupKey(final String groupBy,
                                          final int timerId,
                                          final int agentId,
                                          final int statusCode,
                                          final int usedIpId,
                                          final int methodId,
                                          final int contentTypeId,
                                          final String[] timerNames,
                                          final GlobalDictionaries.AgentTestCase[] agentPairs,
                                          final String[] strings)
    {
        return switch (groupBy)
        {
            case "TIMER" -> safeLookupOrDefault(timerNames, timerId, "(unknown)");
            case "AGENT" -> {
                final GlobalDictionaries.AgentTestCase pair = safeLookup(agentPairs, agentId);
                yield pair != null && !pair.agentName().isEmpty() ? pair.agentName() : "(unknown)";
            }
            case "SCENARIO" -> {
                final GlobalDictionaries.AgentTestCase pair = safeLookup(agentPairs, agentId);
                yield pair != null && !pair.testCaseName().isEmpty() ? pair.testCaseName() : "(unknown)";
            }
            case "STATUS_CODE" -> String.valueOf(statusCode);
            case "USED_IP" -> safeLookupOrDefault(strings, usedIpId, "(none)");
            case "HTTP_METHOD" -> safeLookupOrDefault(strings, methodId, "(unknown)");
            case "CONTENT_TYPE" -> safeLookupOrDefault(strings, contentTypeId, "(none)");
            default -> "All";
        };
    }

    private List<Chunk> pruneCandidateChunks(final char typeCode, final ResolvedFilter filter)
    {
        final ScanPredicate predicate = new ScanPredicate(
            filter.fromTime, filter.toTime, filter.timerIds, filter.agentTestCaseIds
        );
        final List<Chunk> catalogMatched = storage.getCatalog().queryChunks(typeCode, predicate);
        if (catalogMatched.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<Chunk> pruned = new ArrayList<>(catalogMatched.size());
        for (final Chunk c : catalogMatched)
        {
            if (filter.minRunTime > 0 && c.getMaxRunTime() > 0 && c.getMaxRunTime() < filter.minRunTime)
            {
                continue;
            }
            if (filter.canPruneByHasFailures(typeCode) && !c.hasFailures())
            {
                continue;
            }
            if (typeCode == 'R')
            {
                final RequestChunkIndex idx = requestIndexMap.get(c);
                if (idx != null)
                {
                    if (idx.minStatusCode >= 0)
                    {
                        if (filter.minStatusCode != null && idx.maxStatusCode < filter.minStatusCode)
                        {
                            continue;
                        }
                        if (filter.maxStatusCode != null && idx.minStatusCode > filter.maxStatusCode)
                        {
                            continue;
                        }
                        if (filter.failedOnly && !idx.hasFailedRows && idx.maxStatusCode < 400 && idx.minStatusCode > 0)
                        {
                            continue;
                        }
                    }
                    if (filter.httpMethodIds != null && idx.singleMethodId >= 0 && !filter.httpMethodIds.contains(idx.singleMethodId))
                    {
                        continue;
                    }
                    if (filter.contentTypeIds != null && idx.singleContentTypeId >= 0 && !filter.contentTypeIds.contains(idx.singleContentTypeId))
                    {
                        continue;
                    }
                    if (filter.usedIpIds != null && idx.singleUsedIpId >= 0 && !filter.usedIpIds.contains(idx.singleUsedIpId))
                    {
                        continue;
                    }
                }
            }
            pruned.add(c);
        }
        return pruned;
    }

    private ResolvedFilter resolveFilter(final char typeCode,
                                         final long rawFromTime,
                                         final long rawToTime,
                                         final List<String> timers,
                                         final String timerQuery,
                                         final List<String> agents,
                                         final List<String> scenarios,
                                         final List<String> usedIps,
                                         final List<String> httpMethods,
                                         final List<String> contentTypes,
                                         final Integer minStatusCode,
                                         final Integer maxStatusCode,
                                         final Boolean failedOnly,
                                         final Integer minRunTime)
    {
        final long globalMin = storage.getMinTime();
        final long globalMax = storage.getMaxTime();
        final long fromTime = rawFromTime > 0 ? rawFromTime : globalMin;
        final long toTime = rawToTime > 0 ? rawToTime : Math.max(fromTime, globalMax);

        final GlobalDictionaries dicts = storage.getDictionaries();
        final RoaringBitmap timerIds = compileTimerBitmap(dicts, timers, timerQuery);
        final RoaringBitmap agentTestCaseIds = compileAgentScenarioBitmap(dicts, agents, scenarios);
        final RoaringBitmap usedIpIds = (typeCode == 'R') ? compileStringBitmap(dicts, usedIps) : null;
        final RoaringBitmap httpMethodIds = (typeCode == 'R') ? compileStringBitmap(dicts, httpMethods) : null;
        final RoaringBitmap contentTypeIds = (typeCode == 'R') ? compileStringBitmap(dicts, contentTypes) : null;

        return new ResolvedFilter(
            fromTime,
            toTime,
            timerIds,
            agentTestCaseIds,
            usedIpIds,
            httpMethodIds,
            contentTypeIds,
            minStatusCode,
            maxStatusCode,
            Boolean.TRUE.equals(failedOnly),
            minRunTime != null ? Math.max(0, minRunTime) : 0
        );
    }

    private static RoaringBitmap compileTimerBitmap(final GlobalDictionaries dicts,
                                                    final List<String> exactTimers,
                                                    final String timerQuery)
    {
        final boolean hasExact = exactTimers != null && !exactTimers.isEmpty();
        final boolean hasQuery = timerQuery != null && !timerQuery.isBlank();
        if (!hasExact && !hasQuery)
        {
            return null;
        }

        final Set<String> exactSet = hasExact ? new HashSet<>(exactTimers) : null;
        final String queryTrimmed = hasQuery ? timerQuery.trim() : null;
        final String queryLower = hasQuery ? queryTrimmed.toLowerCase(Locale.ROOT) : null;
        Pattern regex = null;
        if (hasQuery)
        {
            try
            {
                regex = Pattern.compile(queryTrimmed, Pattern.CASE_INSENSITIVE);
            }
            catch (final PatternSyntaxException ignored)
            {
                // Fall back to plain case-insensitive substring matching
            }
        }

        final RoaringBitmap bm = new RoaringBitmap();
        final String[] timerNames = dicts.getTimerNamesArray();
        final int count = dicts.getTimerNameCount();
        for (int i = 0; i < count; i++)
        {
            final String name = timerNames[i];
            if (name == null)
            {
                continue;
            }
            if (exactSet != null && !exactSet.contains(name))
            {
                continue;
            }
            if (hasQuery)
            {
                final boolean substringMatch = name.toLowerCase(Locale.ROOT).contains(queryLower);
                final boolean regexMatch = regex != null && regex.matcher(name).find();
                if (!substringMatch && !regexMatch)
                {
                    continue;
                }
            }
            bm.add(i);
        }
        return bm;
    }

    private static RoaringBitmap compileAgentScenarioBitmap(final GlobalDictionaries dicts,
                                                            final List<String> agents,
                                                            final List<String> scenarios)
    {
        final boolean hasAgents = agents != null && !agents.isEmpty();
        final boolean hasScenarios = scenarios != null && !scenarios.isEmpty();
        if (!hasAgents && !hasScenarios)
        {
            return null;
        }

        final Set<String> agentSet = hasAgents ? new HashSet<>(agents) : null;
        final Set<String> scenarioSet = hasScenarios ? new HashSet<>(scenarios) : null;
        final RoaringBitmap bm = new RoaringBitmap();

        final GlobalDictionaries.AgentTestCase[] pairs = dicts.getAgentTestCasesArray();
        final int count = dicts.getAgentTestCaseCount();
        for (int i = 0; i < count; i++)
        {
            final GlobalDictionaries.AgentTestCase pair = pairs[i];
            if (pair == null)
            {
                continue;
            }
            if (agentSet != null && !agentSet.contains(pair.agentName()))
            {
                continue;
            }
            if (scenarioSet != null && !scenarioSet.contains(pair.testCaseName()))
            {
                continue;
            }
            bm.add(i);
        }
        return bm;
    }

    private static RoaringBitmap compileStringBitmap(final GlobalDictionaries dicts, final List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return null;
        }
        final Set<String> targetSet = new HashSet<>(values);
        final RoaringBitmap bm = new RoaringBitmap();
        final String[] strings = dicts.getStringsArray();
        final int count = dicts.getStringCount();
        for (int i = 0; i < count; i++)
        {
            if (strings[i] != null && targetSet.contains(strings[i]))
            {
                bm.add(i);
            }
        }
        return bm;
    }

    private static int findAgentTestCaseId(final GlobalDictionaries dicts, final String agent, final String scenario)
    {
        final GlobalDictionaries.AgentTestCase[] pairs = dicts.getAgentTestCasesArray();
        final int count = dicts.getAgentTestCaseCount();
        for (int i = 0; i < count; i++)
        {
            final GlobalDictionaries.AgentTestCase p = pairs[i];
            if (p != null && p.agentName().equals(agent) && p.testCaseName().equals(scenario))
            {
                return i;
            }
        }
        return -1;
    }

    private static int findStringId(final GlobalDictionaries dicts, final String target)
    {
        final String[] strings = dicts.getStringsArray();
        final int count = dicts.getStringCount();
        for (int i = 0; i < count; i++)
        {
            if (target.equals(strings[i]))
            {
                return i;
            }
        }
        return -1;
    }

    private String buildDumpUrl(final String agent,
                                final String scenario,
                                final String userNumber,
                                final String directoryName)
    {
        if (directoryName == null || directoryName.isEmpty()
            || agent == null || agent.isEmpty()
            || scenario == null || scenario.isEmpty()
            || userNumber == null || userNumber.isEmpty())
        {
            return null;
        }
        if (resultsDir != null)
        {
            final File dumpIndex = new File(
                new File(new File(new File(new File(resultsDir, agent), scenario), userNumber), "output"),
                new File(directoryName, "index.html").getPath()
            );
            if (!dumpIndex.isFile())
            {
                return null;
            }
        }
        return "/dumps/"
               + urlEncodePathSegment(agent) + "/"
               + urlEncodePathSegment(scenario) + "/"
               + urlEncodePathSegment(userNumber) + "/output/"
               + urlEncodePathSegment(directoryName) + "/index.html";
    }

    private static String urlEncodePathSegment(final String segment)
    {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static <T> T safeLookup(final T[] arr, final int id)
    {
        return (arr != null && id >= 0 && id < arr.length) ? arr[id] : null;
    }

    private static String safeLookupOrDefault(final String[] arr, final int id, final String fallback)
    {
        final String val = safeLookup(arr, id);
        return (val != null && !val.isEmpty()) ? val : fallback;
    }

    private static double round2(final double val)
    {
        return Math.round(val * 100.0) / 100.0;
    }

    private static final class ResolvedFilter
    {
        final long fromTime;
        final long toTime;
        final RoaringBitmap timerIds;
        final RoaringBitmap agentTestCaseIds;
        final RoaringBitmap usedIpIds;
        final RoaringBitmap httpMethodIds;
        final RoaringBitmap contentTypeIds;
        final Integer minStatusCode;
        final Integer maxStatusCode;
        final boolean failedOnly;
        final int minRunTime;

        ResolvedFilter(final long fromTime,
                       final long toTime,
                       final RoaringBitmap timerIds,
                       final RoaringBitmap agentTestCaseIds,
                       final RoaringBitmap usedIpIds,
                       final RoaringBitmap httpMethodIds,
                       final RoaringBitmap contentTypeIds,
                       final Integer minStatusCode,
                       final Integer maxStatusCode,
                       final boolean failedOnly,
                       final int minRunTime)
        {
            this.fromTime = fromTime;
            this.toTime = toTime;
            this.timerIds = timerIds;
            this.agentTestCaseIds = agentTestCaseIds;
            this.usedIpIds = usedIpIds;
            this.httpMethodIds = httpMethodIds;
            this.contentTypeIds = contentTypeIds;
            this.minStatusCode = minStatusCode;
            this.maxStatusCode = maxStatusCode;
            this.failedOnly = failedOnly;
            this.minRunTime = minRunTime;
        }

        boolean matchesRequestRow(final long time,
                                  final int rt,
                                  final int status,
                                  final boolean isFailed,
                                  final int timerId,
                                  final int agentId,
                                  final int usedIpId,
                                  final int methodId,
                                  final int contentTypeId)
        {
            if (time < fromTime || time > toTime)
            {
                return false;
            }
            if (minRunTime > 0 && rt < minRunTime)
            {
                return false;
            }
            if (!matchesStatusAndFailure(status, isFailed))
            {
                return false;
            }
            if (timerIds != null && !timerIds.contains(timerId))
            {
                return false;
            }
            if (agentTestCaseIds != null && !agentTestCaseIds.contains(agentId))
            {
                return false;
            }
            if (usedIpIds != null && !usedIpIds.contains(usedIpId))
            {
                return false;
            }
            if (httpMethodIds != null && !httpMethodIds.contains(methodId))
            {
                return false;
            }
            if (contentTypeIds != null && !contentTypeIds.contains(contentTypeId))
            {
                return false;
            }
            return true;
        }

        boolean matchesTimerRow(final long time,
                                final int rt,
                                final boolean isFailed,
                                final int timerId,
                                final int agentId)
        {
            if (time < fromTime || time > toTime)
            {
                return false;
            }
            if (minRunTime > 0 && rt < minRunTime)
            {
                return false;
            }
            if (failedOnly && !isFailed)
            {
                return false;
            }
            if (timerIds != null && !timerIds.contains(timerId))
            {
                return false;
            }
            if (agentTestCaseIds != null && !agentTestCaseIds.contains(agentId))
            {
                return false;
            }
            return true;
        }

        boolean matchesStatusAndFailure(final int statusCode, final boolean isFailed)
        {
            if (failedOnly && !isFailed)
            {
                return false;
            }
            if (minStatusCode != null && statusCode < minStatusCode)
            {
                return false;
            }
            if (maxStatusCode != null && statusCode > maxStatusCode)
            {
                return false;
            }
            return true;
        }

        boolean canPruneByHasFailures(final char typeCode)
        {
            if (!failedOnly)
            {
                return false;
            }
            if (typeCode != 'R')
            {
                return true;
            }
            return minStatusCode != null && minStatusCode >= 500;
        }
    }

    private static final class TimeSeriesAccumulator
    {
        final int bucketCount;
        final int[] count;
        final int[] errors;
        final long[] sumRunTime;
        final int[] minRunTime;
        final int[] maxRunTime;
        final RuntimeHistogram[] histograms;
        final long[] bytesSent;
        final long[] bytesReceived;
        final long[] sumDns;
        final long[] sumConnect;
        final long[] sumSend;
        final long[] sumServerBusy;
        final long[] sumReceive;
        final long[] sumTtfb;
        final Map<String, GroupBucketAccumulator> groups;

        TimeSeriesAccumulator(final int bucketCount, final boolean trackGroups)
        {
            this.bucketCount = bucketCount;
            this.count = new int[bucketCount];
            this.errors = new int[bucketCount];
            this.sumRunTime = new long[bucketCount];
            this.minRunTime = new int[bucketCount];
            Arrays.fill(this.minRunTime, Integer.MAX_VALUE);
            this.maxRunTime = new int[bucketCount];
            this.histograms = new RuntimeHistogram[bucketCount];
            this.bytesSent = new long[bucketCount];
            this.bytesReceived = new long[bucketCount];
            this.sumDns = new long[bucketCount];
            this.sumConnect = new long[bucketCount];
            this.sumSend = new long[bucketCount];
            this.sumServerBusy = new long[bucketCount];
            this.sumReceive = new long[bucketCount];
            this.sumTtfb = new long[bucketCount];
            this.groups = trackGroups ? new HashMap<>() : null;
        }

        void recordRequest(final int b,
                           final int rt,
                           final boolean isFailed,
                           final int sent,
                           final int received,
                           final int dns,
                           final int connect,
                           final int send,
                           final int busy,
                           final int receive,
                           final int ttfb)
        {
            recordTimer(b, rt, isFailed);
            bytesSent[b] += Math.max(0, sent);
            bytesReceived[b] += Math.max(0, received);
            sumDns[b] += Math.max(0, dns);
            sumConnect[b] += Math.max(0, connect);
            sumSend[b] += Math.max(0, send);
            sumServerBusy[b] += Math.max(0, busy);
            sumReceive[b] += Math.max(0, receive);
            sumTtfb[b] += Math.max(0, ttfb);
        }

        void recordTimer(final int b, final int rt, final boolean isFailed)
        {
            count[b]++;
            if (isFailed)
            {
                errors[b]++;
            }
            sumRunTime[b] += rt;
            if (rt < minRunTime[b])
            {
                minRunTime[b] = rt;
            }
            if (rt > maxRunTime[b])
            {
                maxRunTime[b] = rt;
            }
            RuntimeHistogram h = histograms[b];
            if (h == null)
            {
                h = new RuntimeHistogram(1);
                histograms[b] = h;
            }
            h.addValue(Math.max(0, rt));
        }

        void recordGroup(final String key, final int b, final int rt, final boolean isFailed)
        {
            groups.computeIfAbsent(key, k -> new GroupBucketAccumulator(bucketCount))
                  .record(b, rt, isFailed);
        }

        TimeSeriesAccumulator merge(final TimeSeriesAccumulator other)
        {
            for (int b = 0; b < bucketCount; b++)
            {
                if (other.count[b] == 0)
                {
                    continue;
                }
                this.count[b] += other.count[b];
                this.errors[b] += other.errors[b];
                this.sumRunTime[b] += other.sumRunTime[b];
                if (other.minRunTime[b] < this.minRunTime[b])
                {
                    this.minRunTime[b] = other.minRunTime[b];
                }
                if (other.maxRunTime[b] > this.maxRunTime[b])
                {
                    this.maxRunTime[b] = other.maxRunTime[b];
                }
                if (this.histograms[b] == null)
                {
                    this.histograms[b] = new RuntimeHistogram(other.histograms[b]);
                }
                else
                {
                    this.histograms[b].merge(other.histograms[b]);
                }
                this.bytesSent[b] += other.bytesSent[b];
                this.bytesReceived[b] += other.bytesReceived[b];
                this.sumDns[b] += other.sumDns[b];
                this.sumConnect[b] += other.sumConnect[b];
                this.sumSend[b] += other.sumSend[b];
                this.sumServerBusy[b] += other.sumServerBusy[b];
                this.sumReceive[b] += other.sumReceive[b];
                this.sumTtfb[b] += other.sumTtfb[b];
            }

            if (this.groups != null && other.groups != null)
            {
                for (final Map.Entry<String, GroupBucketAccumulator> entry : other.groups.entrySet())
                {
                    this.groups.merge(entry.getKey(), entry.getValue(), (g1, g2) -> {
                        g1.merge(g2);
                        return g1;
                    });
                }
            }
            return this;
        }
    }

    private static final class GroupBucketAccumulator
    {
        final int bucketCount;
        long totalCount;
        long totalErrors;
        final int[] count;
        final int[] errors;
        final long[] sumRunTime;
        final RuntimeHistogram[] histograms;

        GroupBucketAccumulator(final int bucketCount)
        {
            this.bucketCount = bucketCount;
            this.count = new int[bucketCount];
            this.errors = new int[bucketCount];
            this.sumRunTime = new long[bucketCount];
            this.histograms = new RuntimeHistogram[bucketCount];
        }

        void record(final int b, final int rt, final boolean isFailed)
        {
            totalCount++;
            count[b]++;
            if (isFailed)
            {
                totalErrors++;
                errors[b]++;
            }
            sumRunTime[b] += rt;
            RuntimeHistogram h = histograms[b];
            if (h == null)
            {
                h = new RuntimeHistogram(1);
                histograms[b] = h;
            }
            h.addValue(Math.max(0, rt));
        }

        void merge(final GroupBucketAccumulator other)
        {
            this.totalCount += other.totalCount;
            this.totalErrors += other.totalErrors;
            for (int b = 0; b < bucketCount; b++)
            {
                if (other.count[b] == 0)
                {
                    continue;
                }
                this.count[b] += other.count[b];
                this.errors[b] += other.errors[b];
                this.sumRunTime[b] += other.sumRunTime[b];
                if (this.histograms[b] == null)
                {
                    this.histograms[b] = new RuntimeHistogram(other.histograms[b]);
                }
                else
                {
                    this.histograms[b].merge(other.histograms[b]);
                }
            }
        }

        GroupedSeries toGroupedSeries(final String key, final double bucketSeconds)
        {
            final double[] rps = new double[bucketCount];
            final double[] mean = new double[bucketCount];
            final double[] p50 = new double[bucketCount];
            final double[] p95 = new double[bucketCount];

            for (int b = 0; b < bucketCount; b++)
            {
                final int c = count[b];
                if (c > 0)
                {
                    rps[b] = round2(c / bucketSeconds);
                    mean[b] = round2((double) sumRunTime[b] / c);
                    final RuntimeHistogram h = histograms[b];
                    p50[b] = round2(h.getPercentile(50.0));
                    p95[b] = round2(h.getPercentile(95.0));
                }
            }
            return new GroupedSeries(key, totalCount, totalErrors, count, rps, errors, mean, p50, p95);
        }
    }

    private static final class SummaryGroupAccumulator
    {
        int representativeTimerId = -1;
        long count;
        long errors;
        long sumRunTime;
        int minRunTime = Integer.MAX_VALUE;
        int maxRunTime = 0;
        final RuntimeHistogram histogram = new RuntimeHistogram(1);
        long bytesSent;
        long bytesReceived;
        long sumDns;
        long sumConnect;
        long sumSend;
        long sumServerBusy;
        long sumReceive;
        long sumTtfb;
        long sumTtlb;
        final RoaringBitmap urlHashes;

        SummaryGroupAccumulator(final boolean trackUrls)
        {
            this.urlHashes = trackUrls ? new RoaringBitmap() : null;
        }

        void recordRequest(final int rt,
                           final boolean isFailed,
                           final int sent,
                           final int received,
                           final int dns,
                           final int connect,
                           final int send,
                           final int busy,
                           final int receive,
                           final int ttfb,
                           final int ttlb,
                           final int urlHash)
        {
            recordTimer(rt, isFailed);
            bytesSent += Math.max(0, sent);
            bytesReceived += Math.max(0, received);
            sumDns += Math.max(0, dns);
            sumConnect += Math.max(0, connect);
            sumSend += Math.max(0, send);
            sumServerBusy += Math.max(0, busy);
            sumReceive += Math.max(0, receive);
            sumTtfb += Math.max(0, ttfb);
            sumTtlb += Math.max(0, ttlb);
            if (urlHashes != null && urlHash != 0)
            {
                urlHashes.add(urlHash);
            }
        }

        void recordTimer(final int rt, final boolean isFailed)
        {
            count++;
            if (isFailed)
            {
                errors++;
            }
            sumRunTime += rt;
            if (rt < minRunTime)
            {
                minRunTime = rt;
            }
            if (rt > maxRunTime)
            {
                maxRunTime = rt;
            }
            histogram.addValue(Math.max(0, rt));
        }

        SummaryGroupAccumulator merge(final SummaryGroupAccumulator other)
        {
            if (this.representativeTimerId < 0)
            {
                this.representativeTimerId = other.representativeTimerId;
            }
            this.count += other.count;
            this.errors += other.errors;
            this.sumRunTime += other.sumRunTime;
            if (other.minRunTime < this.minRunTime)
            {
                this.minRunTime = other.minRunTime;
            }
            if (other.maxRunTime > this.maxRunTime)
            {
                this.maxRunTime = other.maxRunTime;
            }
            this.histogram.merge(other.histogram);
            this.bytesSent += other.bytesSent;
            this.bytesReceived += other.bytesReceived;
            this.sumDns += other.sumDns;
            this.sumConnect += other.sumConnect;
            this.sumSend += other.sumSend;
            this.sumServerBusy += other.sumServerBusy;
            this.sumReceive += other.sumReceive;
            this.sumTtfb += other.sumTtfb;
            this.sumTtlb += other.sumTtlb;
            if (this.urlHashes != null && other.urlHashes != null)
            {
                this.urlHashes.or(other.urlHashes);
            }
            return this;
        }

        SummaryRow toSummaryRow(final String key,
                                final double windowSeconds,
                                final int distinctUrls,
                                final String sampleUrl)
        {
            final double c = (double) count;
            return new SummaryRow(
                key,
                count,
                round2(c / windowSeconds),
                errors,
                round2((errors * 100.0) / c),
                round2(sumRunTime / c),
                minRunTime,
                maxRunTime,
                round2(histogram.getPercentile(50.0)),
                round2(histogram.getPercentile(90.0)),
                round2(histogram.getPercentile(95.0)),
                round2(histogram.getPercentile(99.0)),
                round2(histogram.getPercentile(99.9)),
                bytesSent,
                bytesReceived,
                round2(sumDns / c),
                round2(sumConnect / c),
                round2(sumSend / c),
                round2(sumServerBusy / c),
                round2(sumReceive / c),
                round2(sumTtfb / c),
                round2(sumTtlb / c),
                distinctUrls,
                sampleUrl
            );
        }
    }
}
