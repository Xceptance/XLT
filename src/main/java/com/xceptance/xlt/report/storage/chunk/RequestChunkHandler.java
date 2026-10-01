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
import java.util.Arrays;
import java.util.function.Consumer;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Specialized high-performance {@link ChunkTypeHandler} for HTTP Request data ('R').
 * <p>
 * <b>Data Characteristics &amp; Optimization:</b>
 * HTTP requests account for over 90% of all load test log volume. This handler packs up to 64,000
 * requests into SIMD-compressed columnar arrays. Network timings, payload sizes, response codes,
 * and string IDs are all compressed independently using FastPFOR bit-packing, achieving extreme
 * compression ratios while allowing selective multi-threaded decompression at tens of millions
 * of rows per second.
 */
public class RequestChunkHandler implements ChunkTypeHandler
{
    @Override
    public char getTypeCode()
    {
        return 'R';
    }

    @Override
    public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
    {
        return new RequestChunkBuilder(dicts);
    }

    /**
     * Serializes a sealed {@link RequestChunk} into binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header: row count (int), min timestamp (long), max timestamp (long).</li>
     *   <li>Bitmaps: timer name IDs, agent/testCase IDs, failed row indices.</li>
     *   <li>Columnar integer blocks: time offsets, timer IDs, agent IDs, runtimes, status codes, bytes sent/received.</li>
     *   <li>Network timing blocks: DNS, connect, send, busy, receive, TTFB, TTLB.</li>
     *   <li>Dictionary ID blocks: HTTP methods, MIME content types, used IPs, IP addresses.</li>
     *   <li>Sample URLs map: entry count, followed by timer ID and URL string list.</li>
     *   <li>Top slowest requests: entry count, followed by serialized field values.</li>
     * </ol>
     *
     * @param chunk
     *            sealed {@link RequestChunk} to write
     * @param out
     *            target binary output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void writeChunk(final Chunk chunk, final DataOutput out) throws IOException
    {
        final RequestChunk c = (RequestChunk) chunk;

        // 1. Chunk header metadata
        out.writeInt(c.getRowCount());
        out.writeLong(c.getMinTime());
        out.writeLong(c.getMaxTime());

        // 2. RoaringBitmap inverted index filters
        c.getTimerNameIds().serialize(out);
        c.getAgentTestCaseIds().serialize(out);
        c.getFailedRows().serialize(out);

        // 3. Primary request performance columns
        writeIntArray(c.compTimeOffsets, out);
        writeIntArray(c.compTimerNameIds, out);
        writeIntArray(c.compAgentTestCaseIds, out);
        writeIntArray(c.compRunTimes, out);
        writeIntArray(c.compResponseCodes, out);
        writeIntArray(c.compBytesSent, out);
        writeIntArray(c.compBytesReceived, out);

        // 4. Detailed network timing columns
        writeIntArray(c.compDnsTimes, out);
        writeIntArray(c.compConnectTimes, out);
        writeIntArray(c.compSendTimes, out);
        writeIntArray(c.compServerBusyTimes, out);
        writeIntArray(c.compReceiveTimes, out);
        writeIntArray(c.compTimeToFirstBytes, out);
        writeIntArray(c.compTimeToLastBytes, out);

        // 5. Interned string attribute dictionary IDs
        writeIntArray(c.compHttpMethods, out);
        writeIntArray(c.compContentTypes, out);
        writeIntArray(c.compUsedIps, out);
        writeIntArray(c.compIpAddresses, out);

        // 6. Representative sample URLs mapped by timer ID (moved to GlobalDictionaries)
        out.writeInt(0);

        // 7. Preserved outlier slowest requests (dead code eliminated)
        out.writeInt(0);
    }

    /**
     * Deserializes a binary stream into a sealed {@link RequestChunk}.
     *
     * @param in
     *            binary input stream
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link RequestChunk}
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public Chunk readChunk(final DataInput in, final GlobalDictionaries dicts) throws IOException
    {
        // 1. Read header metadata
        final int rowCount = in.readInt();
        final long minTime = in.readLong();
        final long maxTime = in.readLong();

        // 2. Deserialize RoaringBitmaps
        final RoaringBitmap timerIds = new RoaringBitmap();
        timerIds.deserialize(in);

        final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        agentTestCaseIds.deserialize(in);

        final RoaringBitmap failedRows = new RoaringBitmap();
        failedRows.deserialize(in);

        // 3. Read compressed integer columns
        final int[] compTimeOffsets = readIntArray(in);
        final int[] compTimerNameIds = readIntArray(in);
        final int[] compAgentTestCaseIds = readIntArray(in);
        final int[] compRunTimes = readIntArray(in);
        final int[] compResponseCodes = readIntArray(in);
        final int[] compBytesSent = readIntArray(in);
        final int[] compBytesReceived = readIntArray(in);

        final int[] compDnsTimes = readIntArray(in);
        final int[] compConnectTimes = readIntArray(in);
        final int[] compSendTimes = readIntArray(in);
        final int[] compServerBusyTimes = readIntArray(in);
        final int[] compReceiveTimes = readIntArray(in);
        final int[] compTimeToFirstBytes = readIntArray(in);
        final int[] compTimeToLastBytes = readIntArray(in);

        final int[] compHttpMethods = readIntArray(in);
        final int[] compContentTypes = readIntArray(in);
        final int[] compUsedIps = readIntArray(in);
        final int[] compIpAddresses = readIntArray(in);

        // 4. Read representative sample URLs (register directly into GlobalDictionaries)
        final int sampleCount = in.readInt();
        for (int i = 0; i < sampleCount; i++)
        {
            final int timerId = in.readInt();
            final int count = in.readInt();
            for (int j = 0; j < count; j++)
            {
                final String u = in.readUTF();
                if (dicts != null)
                {
                    dicts.addSampleUrl(timerId, u);
                }
            }
        }

        // 5. Read slowest requests (skip legacy fields)
        final int slowestCount = in.readInt();
        for (int i = 0; i < slowestCount; i++)
        {
            final int fieldCount = in.readInt();
            for (int j = 0; j < fieldCount; j++)
            {
                in.readUTF();
            }
        }

        return new RequestChunk(rowCount, minTime, maxTime, timerIds, agentTestCaseIds, failedRows,
                                compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
                                compResponseCodes, compBytesSent, compBytesReceived,
                                compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
                                compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
                                compHttpMethods, compContentTypes, compUsedIps, compIpAddresses);
    }

    /**
     * Decompresses columnar arrays and evaluates rows against the predicate, reconstructing
     * {@link RequestData} instances and feeding them directly to the provided consumer.
     *
     * @param chunk
     *            the {@link RequestChunk} to scan
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

        final RequestChunk c = (RequestChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD parallel column decompression via FastPFOR
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentTestCaseIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] runTimes = FastIntegerCodec.decompress(c.compRunTimes);
        final int[] responseCodes = FastIntegerCodec.decompress(c.compResponseCodes);
        final int[] bytesSent = FastIntegerCodec.decompress(c.compBytesSent);
        final int[] bytesReceived = FastIntegerCodec.decompress(c.compBytesReceived);

        final int[] dnsTimes = FastIntegerCodec.decompress(c.compDnsTimes);
        final int[] connectTimes = FastIntegerCodec.decompress(c.compConnectTimes);
        final int[] sendTimes = FastIntegerCodec.decompress(c.compSendTimes);
        final int[] serverBusyTimes = FastIntegerCodec.decompress(c.compServerBusyTimes);
        final int[] receiveTimes = FastIntegerCodec.decompress(c.compReceiveTimes);
        final int[] timeToFirstBytes = FastIntegerCodec.decompress(c.compTimeToFirstBytes);
        final int[] timeToLastBytes = FastIntegerCodec.decompress(c.compTimeToLastBytes);

        final int[] httpMethods = FastIntegerCodec.decompress(c.compHttpMethods);
        final int[] contentTypes = FastIntegerCodec.decompress(c.compContentTypes);
        final int[] usedIps = FastIntegerCodec.decompress(c.compUsedIps);
        final int[] ipAddresses = FastIntegerCodec.decompress(c.compIpAddresses);

        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] stringBuffers = dicts.getStringsArray();

        // 3. Linear row scan and reconstitution
        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int timerId = timerIds[i];
            final int agentId = agentTestCaseIds[i];

            // Fine-grained row-level check
            if (!predicate.testRow(time, timerId, agentId))
            {
                continue;
            }

            // Reconstruct RequestData instance
            final RequestData req = new RequestData();
            req.setTime(time);
            req.setName(timerId >= 0 && timerId < timerNames.length ? timerNames[timerId] : null);

            final GlobalDictionaries.AgentTestCase pair = agentId >= 0 && agentId < agentPairs.length ? agentPairs[agentId] : null;
            if (pair != null)
            {
                req.setAgentName(pair.agentName());
                req.setTransactionName(pair.testCaseName());
            }

            req.setRunTime(runTimes[i]);
            req.setFailed(anyFailed && failedRows.contains(i));
            req.setResponseCode(responseCodes[i]);
            req.setBytesSent(bytesSent[i]);
            req.setBytesReceived(bytesReceived[i]);

            req.setDnsTime(dnsTimes[i]);
            req.setConnectTime(connectTimes[i]);
            req.setSendTime(sendTimes[i]);
            req.setServerBusyTime(serverBusyTimes[i]);
            req.setReceiveTime(receiveTimes[i]);
            req.setTimeToFirstBytes(timeToFirstBytes[i]);
            req.setTimeToLastBytes(timeToLastBytes[i]);

            final int methodId = httpMethods[i];
            req.setHttpMethod(methodId >= 0 && methodId < stringBuffers.length ? stringBuffers[methodId] : null);

            final int typeId = contentTypes[i];
            req.setContentType(typeId >= 0 && typeId < stringBuffers.length ? stringBuffers[typeId] : null);

            final int usedIpId = usedIps[i];
            req.setUsedIpAddress(usedIpId >= 0 && usedIpId < stringBuffers.length ? stringBuffers[usedIpId] : null);

            // Cached pre-split IP addresses array lookup (avoids 4.5M regex splits)
            req.setIpAddresses(dicts.getSplitIpAddresses(ipAddresses[i]));

            // Precomputed sample URL lookup (avoids multi-million URL parsing, hash, and String allocations)
            final GlobalDictionaries.CachedSampleUrl[] sampleUrls = dicts.getCachedSampleUrls(timerId);
            if (sampleUrls != null && sampleUrls.length > 0)
            {
                final GlobalDictionaries.CachedSampleUrl sample = (sampleUrls.length == 1) ? sampleUrls[0] : sampleUrls[i % sampleUrls.length];
                req.setUrlFast(sample.url(), sample.host(), sample.hashCodeOfUrlWithoutFragment());
            }

            consumer.accept(req);
        }
    }

    /**
     * Decompresses and streams matching {@link RequestData} records directly into a reusable
     * {@link PostProcessedDataContainer}. Reuses pooled RequestData instances from the container
     * to eliminate all heap allocation churn during query execution.
     *
     * @param chunk
     *            the chunk to scan
     * @param predicate
     *            scan filter predicate
     * @param container
     *            target container with pre-allocated instance pool
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

        final RequestChunk c = (RequestChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. SIMD parallel column decompression via FastPFOR
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentTestCaseIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);
        final int[] runTimes = FastIntegerCodec.decompress(c.compRunTimes);
        final int[] responseCodes = FastIntegerCodec.decompress(c.compResponseCodes);
        final int[] bytesSent = FastIntegerCodec.decompress(c.compBytesSent);
        final int[] bytesReceived = FastIntegerCodec.decompress(c.compBytesReceived);

        final int[] dnsTimes = FastIntegerCodec.decompress(c.compDnsTimes);
        final int[] connectTimes = FastIntegerCodec.decompress(c.compConnectTimes);
        final int[] sendTimes = FastIntegerCodec.decompress(c.compSendTimes);
        final int[] serverBusyTimes = FastIntegerCodec.decompress(c.compServerBusyTimes);
        final int[] receiveTimes = FastIntegerCodec.decompress(c.compReceiveTimes);
        final int[] timeToFirstBytes = FastIntegerCodec.decompress(c.compTimeToFirstBytes);
        final int[] timeToLastBytes = FastIntegerCodec.decompress(c.compTimeToLastBytes);

        final int[] httpMethods = FastIntegerCodec.decompress(c.compHttpMethods);
        final int[] contentTypes = FastIntegerCodec.decompress(c.compContentTypes);
        final int[] usedIps = FastIntegerCodec.decompress(c.compUsedIps);
        final int[] ipAddresses = FastIntegerCodec.decompress(c.compIpAddresses);

        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();

        // Convert failed bitmap to fast boolean lookup table if any rows failed
        boolean[] failedLookup = null;
        if (anyFailed)
        {
            failedLookup = new boolean[rowCount];
            for (final int r : failedRows)
            {
                if (r < rowCount)
                {
                    failedLookup[r] = true;
                }
            }
        }

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] stringBuffers = dicts.getStringsArray();

        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        boolean hasServerError = false;

        // Register cache for sequential runs of identical attributes
        int lastTimerId = -1;
        String lastTimerName = null;
        GlobalDictionaries.CachedSampleUrl[] lastUrls = null;

        int lastAgentId = -1;
        GlobalDictionaries.AgentTestCase lastPair = null;

        int lastMethodId = -1;
        String lastMethod = null;

        int lastTypeId = -1;
        String lastType = null;

        int lastUsedIpId = -1;
        String lastUsedIp = null;

        int containerIndex = container.dataList.size();

        // 3. Linear row scan and zero-allocation reconstitution
        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int timerId = timerIds[i];
            final int agentId = agentTestCaseIds[i];

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

            // Reconstruct RequestData instance from container pool (zero allocation!)
            final RequestData req = container.getOrCreateRequestData(containerIndex++);
            req.setTime(time);

            // Register-cached timer name & sample URL lookup
            if (timerId == lastTimerId)
            {
                req.setName(lastTimerName);
                if (lastUrls != null && lastUrls.length > 0)
                {
                    final GlobalDictionaries.CachedSampleUrl sample = (lastUrls.length == 1) ? lastUrls[0] : lastUrls[i % lastUrls.length];
                    req.setUrlFast(sample.url(), sample.host(), sample.hashCodeOfUrlWithoutFragment());
                }
            }
            else
            {
                lastTimerId = timerId;
                lastTimerName = (timerId >= 0 && timerId < timerNames.length) ? timerNames[timerId] : null;
                req.setName(lastTimerName);

                lastUrls = dicts.getCachedSampleUrls(timerId);
                if (lastUrls != null && lastUrls.length > 0)
                {
                    final GlobalDictionaries.CachedSampleUrl sample = (lastUrls.length == 1) ? lastUrls[0] : lastUrls[i % lastUrls.length];
                    req.setUrlFast(sample.url(), sample.host(), sample.hashCodeOfUrlWithoutFragment());
                }
            }

            // Register-cached agent/testCase pair lookup
            if (agentId == lastAgentId)
            {
                if (lastPair != null)
                {
                    req.setAgentName(lastPair.agentName());
                    req.setTransactionName(lastPair.testCaseName());
                }
                else
                {
                    req.setAgentName(null);
                    req.setTransactionName(null);
                }
            }
            else
            {
                lastAgentId = agentId;
                lastPair = (agentId >= 0 && agentId < agentPairs.length) ? agentPairs[agentId] : null;
                if (lastPair != null)
                {
                    req.setAgentName(lastPair.agentName());
                    req.setTransactionName(lastPair.testCaseName());
                }
                else
                {
                    req.setAgentName(null);
                    req.setTransactionName(null);
                }
            }

            req.setRunTime(runTimes[i]);
            req.setFailed(failedLookup != null && failedLookup[i]);

            final int code = responseCodes[i];
            req.setResponseCode(code);
            if (code == 0 || code >= 500)
            {
                hasServerError = true;
            }

            req.setBytesSent(bytesSent[i]);
            req.setBytesReceived(bytesReceived[i]);

            req.setDnsTime(dnsTimes[i]);
            req.setConnectTime(connectTimes[i]);
            req.setSendTime(sendTimes[i]);
            req.setServerBusyTime(serverBusyTimes[i]);
            req.setReceiveTime(receiveTimes[i]);
            req.setTimeToFirstBytes(timeToFirstBytes[i]);
            req.setTimeToLastBytes(timeToLastBytes[i]);

            // Register-cached HTTP method lookup
            final int methodId = httpMethods[i];
            if (methodId == lastMethodId)
            {
                req.setHttpMethod(lastMethod);
            }
            else
            {
                lastMethodId = methodId;
                lastMethod = (methodId >= 0 && methodId < stringBuffers.length) ? stringBuffers[methodId] : null;
                req.setHttpMethod(lastMethod);
            }

            // Register-cached content type lookup
            final int typeId = contentTypes[i];
            if (typeId == lastTypeId)
            {
                req.setContentType(lastType);
            }
            else
            {
                lastTypeId = typeId;
                lastType = (typeId >= 0 && typeId < stringBuffers.length) ? stringBuffers[typeId] : null;
                req.setContentType(lastType);
            }

            // Register-cached used IP address lookup
            final int usedIpId = usedIps[i];
            if (usedIpId == lastUsedIpId)
            {
                req.setUsedIpAddress(lastUsedIp);
            }
            else
            {
                lastUsedIpId = usedIpId;
                lastUsedIp = (usedIpId >= 0 && usedIpId < stringBuffers.length) ? stringBuffers[usedIpId] : null;
                req.setUsedIpAddress(lastUsedIp);
            }

            // Cached pre-split IP addresses array lookup (avoids 4.5M regex splits)
            req.setIpAddresses(dicts.getSplitIpAddresses(ipAddresses[i]));

            container.addFast(req);
        }

        // Update container time range in bulk (avoids 4.5M per-row Math.min/Math.max calls)
        if (minTime <= maxTime)
        {
            container.updateTimeRange(minTime, maxTime);
        }

        container.hasFailedRecords = anyFailed || hasServerError;
    }


    /**
     * Serializes a primitive int array prefixed with its length.
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
     * Deserializes a primitive int array prefixed with its length.
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
    // RequestChunkBuilder
    // -------------------------------------------------------------------------

    /**
     * High-throughput mutable builder for accumulating and compressing HTTP request records into a {@link RequestChunk}.
     * <p>
     * <b>Zero-Copy Columnar Architecture:</b>
     * <ul>
     *   <li><b>Direct Column Ingestion:</b> Incoming records are written directly into pre-allocated primitive columnar
     *       arrays (e.g. {@code int[]}, {@code long[]}). This completely eliminates holding 65,536 {@link RequestData}
     *       heap objects in an {@link ArrayList}, avoiding massive GC pressure.</li>
     *   <li><b>Single Pass Ingestion:</b> All dictionary interning (timer IDs, agent-testcase IDs, string IDs) and
     *       bitmap updates occur strictly on ingestion during {@link #append(Data)}. When the chunk is sealed, no
     *       redundant iteration or dictionary lookups occur.</li>
     *   <li><b>Selective String Conversion:</b> Sample URLs are capped at {@link #MAX_SAMPLE_URLS_PER_TIMER} per timer.
     *       Once 10 samples have been captured for a timer, {@code getUrl().toString()} is never called again, saving
     *       over 100 million string allocations in large test runs.</li>
     * </ul>
     */
    private static class RequestChunkBuilder implements ChunkBuilder
    {
        private final GlobalDictionaries dicts;

        /** Total number of records currently written to this builder. */
        private int count = 0;

        /** Earliest timestamp encountered in this chunk. */
        private long minTime = Long.MAX_VALUE;

        /** Latest timestamp encountered in this chunk. */
        private long maxTime = Long.MIN_VALUE;

        // ---------------------------------------------------------------------
        // Columnar Primitive Arrays (Pre-allocated to DEFAULT_CHUNK_CAPACITY)
        // ---------------------------------------------------------------------
        private final long[] rawTimes = new long[DEFAULT_CHUNK_CAPACITY];
        private final int[] timerIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] agentTestCaseIdArr = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] runTimes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] responseCodes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] bytesSent = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] bytesReceived = new int[DEFAULT_CHUNK_CAPACITY];

        private final int[] dnsTimes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] connectTimes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] sendTimes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] serverBusyTimes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] receiveTimes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] timeToFirstBytes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] timeToLastBytes = new int[DEFAULT_CHUNK_CAPACITY];

        private final int[] httpMethods = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] contentTypes = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] usedIps = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] ipAddresses = new int[DEFAULT_CHUNK_CAPACITY];

        // ---------------------------------------------------------------------
        // In-Memory Index Bitmaps
        // ---------------------------------------------------------------------
        private final RoaringBitmap timerIds = new RoaringBitmap();
        private final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        private final RoaringBitmap failedRows = new RoaringBitmap();

        // ---------------------------------------------------------------------
        // Thread-Local Register Caches for High-Repetition Protocol Attributes
        // ---------------------------------------------------------------------
        private String lastMethodStr;
        private int lastMethodId = -1;

        private String lastContentTypeStr;
        private int lastContentTypeId = -1;

        private String lastUsedIpStr;
        private int lastUsedIpId = -1;

        private String lastIpAddressesStr;
        private int lastIpAddressesId = -1;

        // Quota gate for sample URLs (zero locks, zero atomics)
        private final byte[] threadSampleCounts = new byte[4096];

        /**
         * Constructs a new chunk builder associated with the given global dictionaries.
         *
         * @param dicts
         *            the shared global dictionaries
         */
        RequestChunkBuilder(final GlobalDictionaries dicts)
        {
            this.dicts = dicts;
        }

        @Override
        public char getTypeCode()
        {
            return 'R';
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

            final RequestData req = (RequestData) record;
            final int idx = count;

            // 1. Maintain time bounds and store raw millisecond timestamp
            final long t = req.getTime();
            if (t < minTime) minTime = t;
            if (t > maxTime) maxTime = t;
            rawTimes[idx] = t;

            // 2. Intern timer name and update bitmap index
            final int timerId = dicts.getOrCreateTimerNameId(req.getName());
            timerIdArr[idx] = timerId;
            timerIds.add(timerId);

            // 3. Intern unified agent + scenario pair and update bitmap index
            final int agentId = dicts.getOrCreateAgentTestCaseId(req.getAgentName(), req.getTransactionName());
            agentTestCaseIdArr[idx] = agentId;
            agentTestCaseIds.add(agentId);

            // 4. Track failed status
            if (req.hasFailed())
            {
                failedRows.add(idx);
            }

            // 5. Populate core metrics directly into columnar arrays
            runTimes[idx] = req.getRunTime();
            responseCodes[idx] = req.getResponseCode();
            bytesSent[idx] = (int) req.getBytesSent();
            bytesReceived[idx] = (int) req.getBytesReceived();

            // 6. Populate sub-timing metrics
            dnsTimes[idx] = req.getDnsTime();
            connectTimes[idx] = req.getConnectTime();
            sendTimes[idx] = req.getSendTime();
            serverBusyTimes[idx] = req.getServerBusyTime();
            receiveTimes[idx] = req.getReceiveTime();
            timeToFirstBytes[idx] = req.getTimeToFirstBytes();
            timeToLastBytes[idx] = req.getTimeToLastBytes();

            // 7. Intern protocol attributes with thread-local register caches (avoids 400M+ dictionary map lookups)
            final CharSequence method = req.getHttpMethod();
            if (method == null)
            {
                httpMethods[idx] = -1;
            }
            else if (lastMethodStr != null && GlobalDictionaries.charSequenceEquals(method, lastMethodStr))
            {
                httpMethods[idx] = lastMethodId;
            }
            else
            {
                lastMethodId = dicts.getOrCreateStringId(method);
                lastMethodStr = dicts.getString(lastMethodId);
                httpMethods[idx] = lastMethodId;
            }

            final CharSequence ct = req.getContentType();
            if (ct == null)
            {
                contentTypes[idx] = -1;
            }
            else if (lastContentTypeStr != null && GlobalDictionaries.charSequenceEquals(ct, lastContentTypeStr))
            {
                contentTypes[idx] = lastContentTypeId;
            }
            else
            {
                lastContentTypeId = dicts.getOrCreateStringId(ct);
                lastContentTypeStr = dicts.getString(lastContentTypeId);
                contentTypes[idx] = lastContentTypeId;
            }

            final CharSequence usedIp = req.getUsedIpAddress();
            if (usedIp == null)
            {
                usedIps[idx] = -1;
            }
            else if (lastUsedIpStr != null && GlobalDictionaries.charSequenceEquals(usedIp, lastUsedIpStr))
            {
                usedIps[idx] = lastUsedIpId;
            }
            else
            {
                lastUsedIpId = dicts.getOrCreateStringId(usedIp);
                lastUsedIpStr = dicts.getString(lastUsedIpId);
                usedIps[idx] = lastUsedIpId;
            }

            final String ipAddressesStr = req.getIpAddressesAsString();
            if (ipAddressesStr == null)
            {
                ipAddresses[idx] = -1;
            }
            else if (lastIpAddressesStr != null && ipAddressesStr.equals(lastIpAddressesStr))
            {
                ipAddresses[idx] = lastIpAddressesId;
            }
            else
            {
                lastIpAddressesId = dicts.getOrCreateStringId(ipAddressesStr);
                lastIpAddressesStr = dicts.getString(lastIpAddressesId);
                ipAddresses[idx] = lastIpAddressesId;
            }

            // 8. Capture sample URLs directly into global dictionaries (capped per thread to eliminate sync overhead)
            if (timerId >= 0)
            {
                if (timerId < threadSampleCounts.length)
                {
                    if (threadSampleCounts[timerId] < 20)
                    {
                        threadSampleCounts[timerId]++;
                        dicts.addSampleUrl(timerId, req.getUrl());
                    }
                }
                else
                {
                    dicts.addSampleUrl(timerId, req.getUrl());
                }
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

            // Convert absolute epoch timestamps into delta offsets relative to minTime
            final int[] timeOffsets = new int[size];
            for (int i = 0; i < size; i++)
            {
                timeOffsets[i] = (int) (rawTimes[i] - minTime);
            }

            // If chunk was full, borrow arrays directly; otherwise trim to exact row count
            final int[] finalTimerIds = size == DEFAULT_CHUNK_CAPACITY ? timerIdArr : Arrays.copyOf(timerIdArr, size);
            final int[] finalAgentIds = size == DEFAULT_CHUNK_CAPACITY ? agentTestCaseIdArr : Arrays.copyOf(agentTestCaseIdArr, size);
            final int[] finalRunTimes = size == DEFAULT_CHUNK_CAPACITY ? runTimes : Arrays.copyOf(runTimes, size);
            final int[] finalResponseCodes = size == DEFAULT_CHUNK_CAPACITY ? responseCodes : Arrays.copyOf(responseCodes, size);
            final int[] finalBytesSent = size == DEFAULT_CHUNK_CAPACITY ? bytesSent : Arrays.copyOf(bytesSent, size);
            final int[] finalBytesReceived = size == DEFAULT_CHUNK_CAPACITY ? bytesReceived : Arrays.copyOf(bytesReceived, size);

            final int[] finalDnsTimes = size == DEFAULT_CHUNK_CAPACITY ? dnsTimes : Arrays.copyOf(dnsTimes, size);
            final int[] finalConnectTimes = size == DEFAULT_CHUNK_CAPACITY ? connectTimes : Arrays.copyOf(connectTimes, size);
            final int[] finalSendTimes = size == DEFAULT_CHUNK_CAPACITY ? sendTimes : Arrays.copyOf(sendTimes, size);
            final int[] finalServerBusyTimes = size == DEFAULT_CHUNK_CAPACITY ? serverBusyTimes : Arrays.copyOf(serverBusyTimes, size);
            final int[] finalReceiveTimes = size == DEFAULT_CHUNK_CAPACITY ? receiveTimes : Arrays.copyOf(receiveTimes, size);
            final int[] finalTimeToFirstBytes = size == DEFAULT_CHUNK_CAPACITY ? timeToFirstBytes : Arrays.copyOf(timeToFirstBytes, size);
            final int[] finalTimeToLastBytes = size == DEFAULT_CHUNK_CAPACITY ? timeToLastBytes : Arrays.copyOf(timeToLastBytes, size);

            final int[] finalHttpMethods = size == DEFAULT_CHUNK_CAPACITY ? httpMethods : Arrays.copyOf(httpMethods, size);
            final int[] finalContentTypes = size == DEFAULT_CHUNK_CAPACITY ? contentTypes : Arrays.copyOf(contentTypes, size);
            final int[] finalUsedIps = size == DEFAULT_CHUNK_CAPACITY ? usedIps : Arrays.copyOf(usedIps, size);
            final int[] finalIpAddresses = size == DEFAULT_CHUNK_CAPACITY ? ipAddresses : Arrays.copyOf(ipAddresses, size);

            // Compress columnar integer arrays using FastPFOR128
            final int[] compTimeOffsets = FastIntegerCodec.compress(timeOffsets);
            final int[] compTimerNameIds = FastIntegerCodec.compress(finalTimerIds);
            final int[] compAgentTestCaseIds = FastIntegerCodec.compress(finalAgentIds);
            final int[] compRunTimes = FastIntegerCodec.compress(finalRunTimes);
            final int[] compResponseCodes = FastIntegerCodec.compress(finalResponseCodes);
            final int[] compBytesSent = FastIntegerCodec.compress(finalBytesSent);
            final int[] compBytesReceived = FastIntegerCodec.compress(finalBytesReceived);

            final int[] compDnsTimes = FastIntegerCodec.compress(finalDnsTimes);
            final int[] compConnectTimes = FastIntegerCodec.compress(finalConnectTimes);
            final int[] compSendTimes = FastIntegerCodec.compress(finalSendTimes);
            final int[] compServerBusyTimes = FastIntegerCodec.compress(finalServerBusyTimes);
            final int[] compReceiveTimes = FastIntegerCodec.compress(finalReceiveTimes);
            final int[] compTimeToFirstBytes = FastIntegerCodec.compress(finalTimeToFirstBytes);
            final int[] compTimeToLastBytes = FastIntegerCodec.compress(finalTimeToLastBytes);

            final int[] compHttpMethods = FastIntegerCodec.compress(finalHttpMethods);
            final int[] compContentTypes = FastIntegerCodec.compress(finalContentTypes);
            final int[] compUsedIps = FastIntegerCodec.compress(finalUsedIps);
            final int[] compIpAddresses = FastIntegerCodec.compress(finalIpAddresses);

            return new RequestChunk(size, minTime, maxTime, timerIds, agentTestCaseIds, failedRows,
                                    compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
                                    compResponseCodes, compBytesSent, compBytesReceived,
                                    compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
                                    compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
                                    compHttpMethods, compContentTypes, compUsedIps, compIpAddresses);
        }
    }
}
