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

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.Deflater;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.StorageIoUtils;
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
 * of rows per second. High-cardinality forensic attributes ({@code url}, {@code requestId},
 * {@code responseId}, {@code formData}, {@code rawName}) are isolated in a Deflate-compressed
 * Cold Forensic Tail Block at the end of the chunk payload.
 */
public class RequestChunkHandler implements ChunkTypeHandler
{
    /** Maximum number of slowest requests per chunk retained in the Cold Forensic Tail Block in sparse mode. */
    public static final int MAX_SLOWEST_FORENSIC_ROWS = 256;

    /** Process-wide default request details mode ("sparse" or "full"). */
    private static volatile String defaultRequestDetailsMode = "sparse";

    /** Optional per-instance override for request details mode. */
    private final String instanceDetailsMode;

    /**
     * Constructs a {@link RequestChunkHandler} using the configured global request details mode.
     */
    public RequestChunkHandler()
    {
        this(null);
    }

    /**
     * Constructs a {@link RequestChunkHandler} with an explicit request details mode ("sparse" or "full").
     *
     * @param instanceDetailsMode
     *            "sparse", "full", or {@code null} to use global configuration
     */
    public RequestChunkHandler(final String instanceDetailsMode)
    {
        this.instanceDetailsMode = instanceDetailsMode;
    }

    /**
     * Sets the process-wide default request details mode ("sparse" or "full").
     *
     * @param mode
     *            "sparse" or "full"
     */
    public static void setRequestDetailsMode(final String mode)
    {
        defaultRequestDetailsMode = "full".equalsIgnoreCase(mode) ? "full" : "sparse";
    }

    /**
     * Returns the process-wide default request details mode ("sparse" or "full").
     *
     * @return "sparse" or "full"
     */
    public static String getRequestDetailsMode()
    {
        return defaultRequestDetailsMode;
    }

    /**
     * Returns whether full forensic details mode is active for this handler.
     *
     * @return {@code true} if "full" mode is active, {@code false} for "sparse" mode
     */
    public boolean isFullDetailsMode()
    {
        if (instanceDetailsMode != null)
        {
            return "full".equalsIgnoreCase(instanceDetailsMode);
        }
        if ("full".equalsIgnoreCase(defaultRequestDetailsMode))
        {
            return true;
        }
        return "full".equalsIgnoreCase(System.getProperty("com.xceptance.xlt.report.storage.requestDetailsMode"));
    }

    @Override
    public char getTypeCode()
    {
        return 'R';
    }

    @Override
    public ChunkBuilder newChunkBuilder(final GlobalDictionaries dicts)
    {
        return new RequestChunkBuilder(dicts, isFullDetailsMode());
    }

    /**
     * Serializes a sealed {@link RequestChunk} into V3 binary format.
     * <p>
     * <b>Binary Layout:</b>
     * <ol>
     *   <li>Header (29 bytes): row count (int), min timestamp (long), max timestamp (long),
     *       maxRunTime (int), hasFailures (boolean), vectorPayloadLength (int).</li>
     *   <li>Bitmaps: timer name IDs, agent/testCase IDs, failed row indices.</li>
     *   <li>Columnar integer blocks (20 arrays): time offsets, timer IDs, agent IDs, runtimes, status codes,
     *       bytes sent/received, DNS, connect, send, busy, receive, TTFB, TTLB, HTTP methods, MIME content types,
     *       used IPs, IP addresses, user numbers, form data encodings.</li>
     *   <li>Cold Forensic Tail Block (at offset {@code vectorPayloadLength}): forensicRows bitmap,
     *       uncompressed byte length (int), compressed byte length (int), Deflate-compressed forensic bytes.</li>
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

        // 1. Chunk header metadata (29 bytes)
        out.writeInt(c.getRowCount());
        out.writeLong(c.getMinTime());
        out.writeLong(c.getMaxTime());
        out.writeInt(c.getMaxRunTime());
        out.writeBoolean(c.hasFailures());
        out.writeInt(c.getVectorPayloadLength());

        // 2. RoaringBitmap inverted index filters
        c.getTimerNameIds().serialize(out);
        c.getAgentTestCaseIds().serialize(out);
        c.getFailedRows().serialize(out);

        // 3. Primary request performance columns (7 arrays)
        writeIntArray(c.compTimeOffsets, out);
        writeIntArray(c.compTimerNameIds, out);
        writeIntArray(c.compAgentTestCaseIds, out);
        writeIntArray(c.compRunTimes, out);
        writeIntArray(c.compResponseCodes, out);
        writeIntArray(c.compBytesSent, out);
        writeIntArray(c.compBytesReceived, out);

        // 4. Detailed network timing columns (7 arrays)
        writeIntArray(c.compDnsTimes, out);
        writeIntArray(c.compConnectTimes, out);
        writeIntArray(c.compSendTimes, out);
        writeIntArray(c.compServerBusyTimes, out);
        writeIntArray(c.compReceiveTimes, out);
        writeIntArray(c.compTimeToFirstBytes, out);
        writeIntArray(c.compTimeToLastBytes, out);

        // 5. Interned string attribute dictionary IDs and per-row URL hashes (7 arrays)
        writeIntArray(c.compHttpMethods, out);
        writeIntArray(c.compContentTypes, out);
        writeIntArray(c.compUsedIps, out);
        writeIntArray(c.compIpAddresses, out);
        writeIntArray(c.compUserNumbers, out);
        writeIntArray(c.compFormDataEncodings, out);
        writeIntArray(c.compUrlHashes, out);

        // 6. Cold Forensic Tail Block (at offset vectorPayloadLength)
        c.getForensicRows().serialize(out);
        out.writeInt(c.getUncompressedForensicLength());
        final byte[] tail = c.getCompressedForensicTail();
        out.writeInt(tail.length);
        if (tail.length > 0)
        {
            out.write(tail);
        }
    }

    /**
     * Deserializes a binary stream into a sealed {@link RequestChunk}, including its Cold Forensic Tail Block
     * if present in the input stream.
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
        return readChunkInternal(in, true);
    }

    /**
     * Deserializes only the hot columnar vector payload of a {@link RequestChunk}, skipping the
     * Cold Forensic Tail Block.
     *
     * @param in
     *            binary input stream containing at least {@code vectorPayloadLength} bytes
     * @param dicts
     *            global dictionaries for string lookups
     * @return reconstituted {@link RequestChunk} with hot vector columns only
     * @throws IOException
     *             if an I/O error occurs
     */
    public RequestChunk readVectorsOnly(final DataInput in, final GlobalDictionaries dicts) throws IOException
    {
        return readChunkInternal(in, false);
    }

    private RequestChunk readChunkInternal(final DataInput in, final boolean readForensicTail) throws IOException
    {
        // 1. Read header metadata (29 bytes)
        final int rowCount = in.readInt();
        final long minTime = in.readLong();
        final long maxTime = in.readLong();
        final int maxRunTime = in.readInt();
        final boolean hasFailures = in.readBoolean();
        final int vectorPayloadLen = in.readInt();

        // 2. Deserialize RoaringBitmaps
        final RoaringBitmap timerIds = new RoaringBitmap();
        timerIds.deserialize(in);

        final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        agentTestCaseIds.deserialize(in);

        final RoaringBitmap failedRows = new RoaringBitmap();
        failedRows.deserialize(in);

        // 3. Read 21 compressed integer columns
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
        final int[] compUserNumbers = readIntArray(in);
        final int[] compFormDataEncodings = readIntArray(in);
        final int[] compUrlHashes = readIntArray(in);

        // 4. Read Cold Forensic Tail Block if requested and available in stream
        RoaringBitmap forensicRows = new RoaringBitmap();
        int uncompressedForensicLen = 0;
        byte[] compressedForensicTail = new byte[0];

        if (readForensicTail)
        {
            try
            {
                forensicRows.deserialize(in);
                uncompressedForensicLen = in.readInt();
                final int compressedLen = in.readInt();
                if (compressedLen > 0)
                {
                    compressedForensicTail = new byte[compressedLen];
                    in.readFully(compressedForensicTail);
                }
            }
            catch (final EOFException ignored)
            {
                // Stream was truncated at vectorPayloadLength (e.g. vector-only slice); leave forensic tail empty
                forensicRows = new RoaringBitmap();
                uncompressedForensicLen = 0;
                compressedForensicTail = new byte[0];
            }
        }

        return new RequestChunk(rowCount, minTime, maxTime, maxRunTime, hasFailures,
                                timerIds, agentTestCaseIds, failedRows,
                                compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
                                compResponseCodes, compBytesSent, compBytesReceived,
                                compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
                                compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
                                compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
                                compUserNumbers, compFormDataEncodings, compUrlHashes,
                                forensicRows, uncompressedForensicLen, compressedForensicTail);
    }

    /**
     * Decompresses columnar arrays and evaluates rows against the predicate, reconstructing
     * {@link RequestData} instances (including any forensic details retained in the Cold Forensic Tail Block)
     * and feeding them directly to the provided consumer.
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

        // 2. Filter column decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentTestCaseIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);

        final boolean allMatch = predicate.matchesAllRows(chunk);
        if (!allMatch)
        {
            boolean anyMatch = false;
            for (int i = 0; i < rowCount; i++)
            {
                if (predicate.testRow(baseTime + timeOffsets[i], timerIds[i], agentTestCaseIds[i]))
                {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch)
            {
                return;
            }
        }

        // 3. Decompress remaining 18 columns only when matching rows exist
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
        final int[] userNumbers = c.compUserNumbers != null && c.compUserNumbers.length > 0
            ? FastIntegerCodec.decompress(c.compUserNumbers) : null;
        final int[] formDataEncodings = c.compFormDataEncodings != null && c.compFormDataEncodings.length > 0
            ? FastIntegerCodec.decompress(c.compFormDataEncodings) : null;
        final int[] urlHashes = c.compUrlHashes != null && c.compUrlHashes.length > 0
            ? FastIntegerCodec.decompress(c.compUrlHashes) : null;

        final RoaringBitmap failedRows = c.getFailedRows();
        final boolean anyFailed = !failedRows.isEmpty();
        final RoaringBitmap forensicRows = c.getForensicRows();
        final boolean anyForensic = forensicRows != null && !forensicRows.isEmpty();

        // Direct read-only dictionary arrays for high-throughput inner loop lookups
        final String[] timerNames = dicts.getTimerNamesArray();
        final GlobalDictionaries.AgentTestCase[] agentPairs = dicts.getAgentTestCasesArray();
        final String[] stringBuffers = dicts.getStringsArray();

        // 4. Linear row scan and reconstitution
        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int timerId = timerIds[i];
            final int agentId = agentTestCaseIds[i];

            // Fine-grained row-level check
            if (!allMatch && !predicate.testRow(time, timerId, agentId))
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

            if (userNumbers != null && i < userNumbers.length)
            {
                final int userNumId = userNumbers[i];
                req.setTestUserNumber(userNumId >= 0 && userNumId < stringBuffers.length ? stringBuffers[userNumId] : null);
            }

            if (formDataEncodings != null && i < formDataEncodings.length)
            {
                final int encId = formDataEncodings[i];
                req.setFormDataEncoding(encId >= 0 && encId < stringBuffers.length ? stringBuffers[encId] : null);
            }

            // Precomputed sample URL lookup combined with the row's true fragment-free URL hash
            final GlobalDictionaries.CachedSampleUrl[] sampleUrls = dicts.getCachedSampleUrls(timerId);
            if (sampleUrls != null && sampleUrls.length > 0)
            {
                final GlobalDictionaries.CachedSampleUrl sample = (sampleUrls.length == 1) ? sampleUrls[0] : sampleUrls[i % sampleUrls.length];
                final int urlHash = (urlHashes != null && i < urlHashes.length)
                    ? urlHashes[i]
                    : sample.hashCodeOfUrlWithoutFragment();
                req.setUrlFast(sample.url(), sample.host(), urlHash);
            }

            // Restore exact forensic details if retained for this row in the Cold Forensic Tail Block
            if (anyForensic && forensicRows.contains(i))
            {
                final RequestChunk.ForensicDetails fd = c.getForensicDetails(i);
                if (fd != null)
                {
                    if (fd.url() != null)
                    {
                        req.setUrl(fd.url());
                    }
                    req.setRequestId(fd.requestId());
                    req.setResponseId(fd.responseId());
                    req.setFormData(fd.formData());
                    req.setRawName(fd.rawName());
                }
            }

            consumer.accept(req);
        }
    }

    /**
     * Decompresses and streams matching {@link RequestData} records directly into a reusable
     * {@link PostProcessedDataContainer}. Reuses pooled RequestData instances from the container
     * to eliminate all heap allocation churn during query execution, and reads only hot vector
     * columns from {@link DiskChunk} without touching the Cold Forensic Tail Block.
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

        // Load ONLY the Hot Vector Prefix (21 FastPFOR integer columns + bitmaps) when reading from disk.
        // Standard report generation providers aggregate numeric metrics and sample URLs from GlobalDictionaries
        // and never inspect per-row forensic strings; skipping the Cold Forensic Tail Block avoids disk I/O
        // and Deflate decompression overhead completely.
        final RequestChunk c = (chunk instanceof DiskChunk dc)
            ? (RequestChunk) dc.loadVectorsOnly(this, dicts)
            : (RequestChunk) resolveChunk(chunk, dicts);
        final int rowCount = c.getRowCount();
        final long baseTime = c.getMinTime();

        // 2. Filter column decompression
        final int[] timeOffsets = FastIntegerCodec.decompress(c.compTimeOffsets);
        final int[] timerIds = FastIntegerCodec.decompress(c.compTimerNameIds);
        final int[] agentTestCaseIds = FastIntegerCodec.decompress(c.compAgentTestCaseIds);

        final boolean allMatch = predicate.matchesAllRows(chunk);
        if (!allMatch)
        {
            boolean anyMatch = false;
            for (int i = 0; i < rowCount; i++)
            {
                if (predicate.testRow(baseTime + timeOffsets[i], timerIds[i], agentTestCaseIds[i]))
                {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch)
            {
                return;
            }
        }

        // 3. Decompress remaining 18 columns only when matching rows exist
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
        final int[] userNumbers = c.compUserNumbers != null && c.compUserNumbers.length > 0
            ? FastIntegerCodec.decompress(c.compUserNumbers) : null;
        final int[] formDataEncodings = c.compFormDataEncodings != null && c.compFormDataEncodings.length > 0
            ? FastIntegerCodec.decompress(c.compFormDataEncodings) : null;
        final int[] urlHashes = c.compUrlHashes != null && c.compUrlHashes.length > 0
            ? FastIntegerCodec.decompress(c.compUrlHashes) : null;

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

        int lastUserNumId = -1;
        String lastUserNum = null;

        int lastEncodingId = -1;
        String lastEncoding = null;

        int containerIndex = container.dataList.size();

        // 4. Linear row scan and zero-allocation reconstitution
        for (int i = 0; i < rowCount; i++)
        {
            final long time = baseTime + timeOffsets[i];
            final int timerId = timerIds[i];
            final int agentId = agentTestCaseIds[i];

            // Fine-grained row-level check
            if (!allMatch && !predicate.testRow(time, timerId, agentId))
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
            }
            else
            {
                lastTimerId = timerId;
                lastTimerName = (timerId >= 0 && timerId < timerNames.length) ? timerNames[timerId] : null;
                req.setName(lastTimerName);
                lastUrls = dicts.getCachedSampleUrls(timerId);
            }
            if (lastUrls != null && lastUrls.length > 0)
            {
                final GlobalDictionaries.CachedSampleUrl sample = (lastUrls.length == 1) ? lastUrls[0] : lastUrls[i % lastUrls.length];
                final int urlHash = (urlHashes != null && i < urlHashes.length)
                    ? urlHashes[i]
                    : sample.hashCodeOfUrlWithoutFragment();
                req.setUrlFast(sample.url(), sample.host(), urlHash);
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

            // Register-cached testUserNumber lookup
            if (userNumbers != null && i < userNumbers.length)
            {
                final int userNumId = userNumbers[i];
                if (userNumId == lastUserNumId)
                {
                    req.setTestUserNumber(lastUserNum);
                }
                else
                {
                    lastUserNumId = userNumId;
                    lastUserNum = (userNumId >= 0 && userNumId < stringBuffers.length) ? stringBuffers[userNumId] : null;
                    req.setTestUserNumber(lastUserNum);
                }
            }
            else
            {
                req.setTestUserNumber(null);
            }

            // Register-cached formDataEncoding lookup
            if (formDataEncodings != null && i < formDataEncodings.length)
            {
                final int encId = formDataEncodings[i];
                if (encId == lastEncodingId)
                {
                    req.setFormDataEncoding(lastEncoding);
                }
                else
                {
                    lastEncodingId = encId;
                    lastEncoding = (encId >= 0 && encId < stringBuffers.length) ? stringBuffers[encId] : null;
                    req.setFormDataEncoding(lastEncoding);
                }
            }
            else
            {
                req.setFormDataEncoding(null);
            }

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
     *       heap objects in an {@link java.util.ArrayList}, avoiding massive GC pressure.</li>
     *   <li><b>Single Pass Ingestion:</b> All dictionary interning (timer IDs, agent-testcase IDs, string IDs) and
     *       bitmap updates occur strictly on ingestion during {@link #append(Data)}. When the chunk is sealed, no
     *       redundant iteration or dictionary lookups occur.</li>
     *   <li><b>Cold Forensic Tail Block:</b> In {@code sparse} mode (default), full URLs, request/response IDs,
     *       form data, and pre-merge raw names are materialized only for failed/4xx/5xx/0 requests and the top-256
     *       slowest requests in the chunk. In {@code full} mode, they are captured for all rows.</li>
     * </ul>
     */
    private static class RequestChunkBuilder implements ChunkBuilder
    {
        private final GlobalDictionaries dicts;
        private final boolean fullDetailsMode;

        /** Total number of records currently written to this builder. */
        private int count = 0;

        /** Earliest timestamp encountered in this chunk. */
        private long minTime = Long.MAX_VALUE;

        /** Latest timestamp encountered in this chunk. */
        private long maxTime = Long.MIN_VALUE;

        /** Maximum runtime duration encountered in this chunk. */
        private int maxRunTime = 0;

        /** Whether any request in this chunk failed or returned HTTP >= 500 or 0. */
        private boolean chunkHasFailures = false;

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
        private final int[] userNumbers = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] formDataEncodings = new int[DEFAULT_CHUNK_CAPACITY];
        private final int[] urlHashes = new int[DEFAULT_CHUNK_CAPACITY];

        // ---------------------------------------------------------------------
        // In-Memory Index Bitmaps
        // ---------------------------------------------------------------------
        private final RoaringBitmap timerIds = new RoaringBitmap();
        private final RoaringBitmap agentTestCaseIds = new RoaringBitmap();
        private final RoaringBitmap failedRows = new RoaringBitmap();

        // ---------------------------------------------------------------------
        // Cold Forensic Tail Block Tracking (Sparse Top-256 + Errors or Full)
        // ---------------------------------------------------------------------
        private final RoaringBitmap failedOrErrorRows = new RoaringBitmap();
        private final int[] slowestHeapRunTimes = new int[MAX_SLOWEST_FORENSIC_ROWS];
        private final int[] slowestHeapRowIndices = new int[MAX_SLOWEST_FORENSIC_ROWS];
        private int slowestHeapSize = 0;
        private final Map<Integer, RequestChunk.ForensicDetails> forensicMap = new HashMap<>(512);

        // ---------------------------------------------------------------------
        // Thread-Local Register Caches for High-Repetition Attributes
        // ---------------------------------------------------------------------
        private CharSequence lastTimerRef;
        private String lastTimerStr;
        private int lastTimerId = -1;

        private CharSequence lastAgentRef;
        private CharSequence lastTxnRef;
        private String lastAgentStr;
        private String lastTxnStr;
        private int lastAgentId = -1;

        private CharSequence lastMethodRef;
        private String lastMethodStr;
        private int lastMethodId = -1;

        private CharSequence lastContentTypeRef;
        private String lastContentTypeStr;
        private int lastContentTypeId = -1;

        private CharSequence lastUsedIpRef;
        private String lastUsedIpStr;
        private int lastUsedIpId = -1;

        private String lastIpAddressesRef;
        private String lastIpAddressesStr;
        private int lastIpAddressesId = -1;

        private CharSequence lastUserNumRef;
        private String lastUserNumStr;
        private int lastUserNumId = -1;

        private CharSequence lastEncodingRef;
        private String lastEncodingStr;
        private int lastEncodingId = -1;

        private int lastSampleUrlTimerId = -1;
        private int lastSampleUrlHash = 0;

        // Bitmasks to avoid redundant RoaringBitmap additions across 64K chunk records
        private long[] seenTimers = new long[64];
        private long[] seenAgents = new long[64];

        /**
         * Constructs a new chunk builder associated with the given global dictionaries.
         *
         * @param dicts
         *            the shared global dictionaries
         * @param fullDetailsMode
         *            whether to retain forensic details for all rows ("full") or only errors and top-256 slowest ("sparse")
         */
        RequestChunkBuilder(final GlobalDictionaries dicts, final boolean fullDetailsMode)
        {
            this.dicts = dicts;
            this.fullDetailsMode = fullDetailsMode;
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

            // 2. Intern timer name and update bitmap index with register cache
            final CharSequence timerName = req.getName();
            final int timerId;
            if (timerName == lastTimerRef)
            {
                timerId = lastTimerId;
            }
            else if (lastTimerStr != null && GlobalDictionaries.charSequenceEquals(timerName, lastTimerStr))
            {
                timerId = lastTimerId;
                lastTimerRef = timerName;
            }
            else
            {
                timerId = dicts.getOrCreateTimerNameId(timerName);
                lastTimerId = timerId;
                lastTimerRef = timerName;
                lastTimerStr = dicts.getTimerName(timerId);
                final int timerWord = timerId >>> 6;
                if (timerWord >= seenTimers.length)
                {
                    seenTimers = java.util.Arrays.copyOf(seenTimers, Math.max(seenTimers.length * 2, timerWord + 16));
                }
                final long timerMask = 1L << timerId;
                if ((seenTimers[timerWord] & timerMask) == 0L)
                {
                    seenTimers[timerWord] |= timerMask;
                    timerIds.add(timerId);
                }
            }
            timerIdArr[idx] = timerId;

            // 3. Intern unified agent + scenario pair and update bitmap index with register cache
            final CharSequence agentName = req.getAgentName();
            final CharSequence txnName = req.getTransactionName();
            final int agentId;
            if (agentName == lastAgentRef && txnName == lastTxnRef)
            {
                agentId = lastAgentId;
            }
            else if (lastAgentStr != null && lastTxnStr != null
                     && GlobalDictionaries.charSequenceEquals(agentName, lastAgentStr)
                     && GlobalDictionaries.charSequenceEquals(txnName, lastTxnStr))
            {
                agentId = lastAgentId;
                lastAgentRef = agentName;
                lastTxnRef = txnName;
            }
            else
            {
                agentId = dicts.getOrCreateAgentTestCaseId(agentName, txnName);
                lastAgentId = agentId;
                lastAgentRef = agentName;
                lastTxnRef = txnName;
                final GlobalDictionaries.AgentTestCase pair = dicts.getAgentTestCase(agentId);
                lastAgentStr = pair != null ? pair.agentName() : null;
                lastTxnStr = pair != null ? pair.testCaseName() : null;
                final int agentWord = agentId >>> 6;
                if (agentWord >= seenAgents.length)
                {
                    seenAgents = java.util.Arrays.copyOf(seenAgents, Math.max(seenAgents.length * 2, agentWord + 16));
                }
                final long agentMask = 1L << agentId;
                if ((seenAgents[agentWord] & agentMask) == 0L)
                {
                    seenAgents[agentWord] |= agentMask;
                    agentTestCaseIds.add(agentId);
                }
            }
            agentTestCaseIdArr[idx] = agentId;

            // 4. Track failed status and Zone Map boundaries
            final boolean failed = req.hasFailed();
            final int respCode = req.getResponseCode();
            final int rt = req.getRunTime();
            if (rt > maxRunTime)
            {
                maxRunTime = rt;
            }
            if (failed)
            {
                failedRows.add(idx);
                chunkHasFailures = true;
            }
            else if (respCode >= 500 || respCode == 0)
            {
                chunkHasFailures = true;
            }

            // 5. Populate core metrics directly into columnar arrays
            runTimes[idx] = rt;
            responseCodes[idx] = respCode;
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

            // 7. Intern protocol attributes with register caches (avoids 400M+ dictionary map lookups)
            final CharSequence method = req.getHttpMethod();
            if (method == null)
            {
                httpMethods[idx] = -1;
            }
            else if (method == lastMethodRef)
            {
                httpMethods[idx] = lastMethodId;
            }
            else if (lastMethodStr != null && GlobalDictionaries.charSequenceEquals(method, lastMethodStr))
            {
                httpMethods[idx] = lastMethodId;
                lastMethodRef = method;
            }
            else
            {
                lastMethodId = dicts.getOrCreateStringId(method);
                lastMethodStr = dicts.getString(lastMethodId);
                lastMethodRef = method;
                httpMethods[idx] = lastMethodId;
            }

            final CharSequence ct = req.getContentType();
            if (ct == null)
            {
                contentTypes[idx] = -1;
            }
            else if (ct == lastContentTypeRef)
            {
                contentTypes[idx] = lastContentTypeId;
            }
            else if (lastContentTypeStr != null && GlobalDictionaries.charSequenceEquals(ct, lastContentTypeStr))
            {
                contentTypes[idx] = lastContentTypeId;
                lastContentTypeRef = ct;
            }
            else
            {
                lastContentTypeId = dicts.getOrCreateStringId(ct);
                lastContentTypeStr = dicts.getString(lastContentTypeId);
                lastContentTypeRef = ct;
                contentTypes[idx] = lastContentTypeId;
            }

            final CharSequence usedIp = req.getUsedIpAddress();
            if (usedIp == null)
            {
                usedIps[idx] = -1;
            }
            else if (usedIp == lastUsedIpRef)
            {
                usedIps[idx] = lastUsedIpId;
            }
            else if (lastUsedIpStr != null && GlobalDictionaries.charSequenceEquals(usedIp, lastUsedIpStr))
            {
                usedIps[idx] = lastUsedIpId;
                lastUsedIpRef = usedIp;
            }
            else
            {
                lastUsedIpId = dicts.getOrCreateStringId(usedIp);
                lastUsedIpStr = dicts.getString(lastUsedIpId);
                lastUsedIpRef = usedIp;
                usedIps[idx] = lastUsedIpId;
            }

            final String ipAddressesStr = req.getIpAddressesAsString();
            if (ipAddressesStr == null)
            {
                ipAddresses[idx] = -1;
            }
            else if (ipAddressesStr == lastIpAddressesRef)
            {
                ipAddresses[idx] = lastIpAddressesId;
            }
            else if (lastIpAddressesStr != null && ipAddressesStr.equals(lastIpAddressesStr))
            {
                ipAddresses[idx] = lastIpAddressesId;
                lastIpAddressesRef = ipAddressesStr;
            }
            else
            {
                lastIpAddressesId = dicts.getOrCreateStringId(ipAddressesStr);
                lastIpAddressesStr = dicts.getString(lastIpAddressesId);
                lastIpAddressesRef = ipAddressesStr;
                ipAddresses[idx] = lastIpAddressesId;
            }

            final CharSequence userNum = req.getTestUserNumber();
            if (userNum == null)
            {
                userNumbers[idx] = -1;
            }
            else if (userNum == lastUserNumRef)
            {
                userNumbers[idx] = lastUserNumId;
            }
            else if (lastUserNumStr != null && GlobalDictionaries.charSequenceEquals(userNum, lastUserNumStr))
            {
                userNumbers[idx] = lastUserNumId;
                lastUserNumRef = userNum;
            }
            else
            {
                lastUserNumId = dicts.getOrCreateStringId(userNum);
                lastUserNumStr = dicts.getString(lastUserNumId);
                lastUserNumRef = userNum;
                userNumbers[idx] = lastUserNumId;
            }

            final CharSequence encoding = req.getFormDataEncoding();
            if (encoding == null)
            {
                formDataEncodings[idx] = -1;
            }
            else if (encoding == lastEncodingRef)
            {
                formDataEncodings[idx] = lastEncodingId;
            }
            else if (lastEncodingStr != null && GlobalDictionaries.charSequenceEquals(encoding, lastEncodingStr))
            {
                formDataEncodings[idx] = lastEncodingId;
                lastEncodingRef = encoding;
            }
            else
            {
                lastEncodingId = dicts.getOrCreateStringId(encoding);
                lastEncodingStr = dicts.getString(lastEncodingId);
                lastEncodingRef = encoding;
                formDataEncodings[idx] = lastEncodingId;
            }

            // 8. Capture fragment-free URL hash and sample URLs directly into global dictionaries (quota-checked lock-free)
            final int urlHash = req.hashCodeOfUrlWithoutFragment();
            urlHashes[idx] = urlHash;
            if (timerId >= 0 && (timerId != lastSampleUrlTimerId || urlHash != lastSampleUrlHash) && !dicts.hasSufficientSamples(timerId))
            {
                lastSampleUrlTimerId = timerId;
                lastSampleUrlHash = urlHash;
                dicts.addSampleUrl(timerId, req.getUrl());
            }

            // 9. Capture Cold Forensic Tail Block details (full mode vs sparse mode):
            //    Per-request forensic strings (exact dynamic URL, requestId, responseId, POST formData, pre-merge rawName)
            //    have massive cardinality and would bloat the global dictionary if interned for every row.
            //    - In "full" mode (--preserve-all-details), every row's forensic tuple is retained.
            //    - In "sparse" mode (default), we retain 100% of failed/error requests (failed == true, HTTP >= 400,
            //      or responseCode == 0) PLUS the top-256 slowest requests in the chunk via a primitive min-heap.
            if (fullDetailsMode)
            {
                forensicMap.put(idx, extractForensicDetails(req));
            }
            else
            {
                final boolean isErrorRow = failed || respCode >= 400 || respCode == 0;
                if (isErrorRow)
                {
                    // Unconditionally retain all error/failure rows and mark them in failedOrErrorRows so they
                    // are protected from removal even if they also enter and are later evicted from the slowest heap.
                    failedOrErrorRows.add(idx);
                    forensicMap.put(idx, extractForensicDetails(req));
                }
                if (slowestHeapSize < MAX_SLOWEST_FORENSIC_ROWS)
                {
                    // Heap not yet full: insert row into min-heap and store its forensic tuple
                    if (!isErrorRow)
                    {
                        forensicMap.put(idx, extractForensicDetails(req));
                    }
                    heapInsert(rt, idx);
                }
                else if (rt > slowestHeapRunTimes[0])
                {
                    // Current request is slower than the fastest member of our top-256 slowest set (the min-heap root).
                    // Evict the displaced root's forensic payload ONLY if that row wasn't also an error/failure row.
                    final int evictedIdx = slowestHeapRowIndices[0];
                    if (!failedOrErrorRows.contains(evictedIdx))
                    {
                        forensicMap.remove(evictedIdx);
                    }
                    if (!isErrorRow)
                    {
                        forensicMap.put(idx, extractForensicDetails(req));
                    }
                    // Replace min-heap root and restore heap invariant in O(log 256) = <= 8 comparisons
                    slowestHeapRunTimes[0] = rt;
                    slowestHeapRowIndices[0] = idx;
                    heapSiftDown(0);
                }
            }

            count++;
            return true;
        }

        /**
         * Snapshots the 5 high-entropy forensic string attributes of a request record.
         * Falls back to {@link RequestData#getName()} if {@code rawName} was not explicitly set prior to merge rules.
         */
        private static RequestChunk.ForensicDetails extractForensicDetails(final RequestData req)
        {
            final String rawName = req.getRawName() != null ? req.getRawName() : req.getName();
            return new RequestChunk.ForensicDetails(
                req.getUrl(),
                req.getRequestId(),
                req.getResponseId(),
                req.getFormData(),
                rawName
            );
        }

        /**
         * Inserts a (runTime, rowIdx) pair into the parallel-array binary min-heap and sifts up
         * so {@code slowestHeapRunTimes[0]} always holds the minimum runtime among the top-K slowest rows.
         */
        private void heapInsert(final int runTime, final int rowIdx)
        {
            int pos = slowestHeapSize++;
            slowestHeapRunTimes[pos] = runTime;
            slowestHeapRowIndices[pos] = rowIdx;
            while (pos > 0)
            {
                final int parent = (pos - 1) >>> 1;
                if (slowestHeapRunTimes[pos] >= slowestHeapRunTimes[parent])
                {
                    break;
                }
                final int tmpRt = slowestHeapRunTimes[parent];
                final int tmpIdx = slowestHeapRowIndices[parent];
                slowestHeapRunTimes[parent] = slowestHeapRunTimes[pos];
                slowestHeapRowIndices[parent] = slowestHeapRowIndices[pos];
                slowestHeapRunTimes[pos] = tmpRt;
                slowestHeapRowIndices[pos] = tmpIdx;
                pos = parent;
            }
        }

        /**
         * Restores the min-heap property from {@code pos} downward after the root element is replaced
         * by a slower request.
         */
        private void heapSiftDown(int pos)
        {
            final int half = slowestHeapSize >>> 1;
            while (pos < half)
            {
                int child = (pos << 1) + 1;
                final int right = child + 1;
                if (right < slowestHeapSize && slowestHeapRunTimes[right] < slowestHeapRunTimes[child])
                {
                    child = right;
                }
                if (slowestHeapRunTimes[pos] <= slowestHeapRunTimes[child])
                {
                    break;
                }
                final int tmpRt = slowestHeapRunTimes[pos];
                final int tmpIdx = slowestHeapRowIndices[pos];
                slowestHeapRunTimes[pos] = slowestHeapRunTimes[child];
                slowestHeapRowIndices[pos] = slowestHeapRowIndices[child];
                slowestHeapRunTimes[child] = tmpRt;
                slowestHeapRowIndices[child] = tmpIdx;
                pos = child;
            }
        }

        @Override
        public Chunk seal()
        {
            final int size = count;
            if (size == 0)
            {
                minTime = 0;
                maxTime = 0;
                maxRunTime = 0;
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
            final int[] finalUserNumbers = size == DEFAULT_CHUNK_CAPACITY ? userNumbers : Arrays.copyOf(userNumbers, size);
            final int[] finalFormDataEncodings = size == DEFAULT_CHUNK_CAPACITY ? formDataEncodings : Arrays.copyOf(formDataEncodings, size);
            final int[] finalUrlHashes = size == DEFAULT_CHUNK_CAPACITY ? urlHashes : Arrays.copyOf(urlHashes, size);

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
            final int[] compUserNumbers = FastIntegerCodec.compress(finalUserNumbers);
            final int[] compFormDataEncodings = FastIntegerCodec.compress(finalFormDataEncodings);
            final int[] compUrlHashes = FastIntegerCodec.compress(finalUrlHashes);

            // Build and Deflate-compress the Cold Forensic Tail Block:
            // Populate the RoaringBitmap of retained row indices first. Iterating forensicRows below visits
            // row indices in strictly ascending order, which allows RequestChunk.getAllForensicDetails()
            // to pair each 5-string tuple back to its row index by iterating the same bitmap—without storing
            // row index integers inside the Deflate stream.
            final RoaringBitmap forensicRows = new RoaringBitmap();
            for (final Integer rIdx : forensicMap.keySet())
            {
                forensicRows.add(rIdx.intValue());
            }

            final int uncompressedForensicLength;
            final byte[] compressedForensicTail;
            if (forensicRows.isEmpty())
            {
                uncompressedForensicLength = 0;
                compressedForensicTail = new byte[0];
            }
            else
            {
                final ByteArrayOutputStream rawBaos = new ByteArrayOutputStream(Math.max(1024, forensicMap.size() * 96));
                try (final DataOutputStream dos = new DataOutputStream(rawBaos))
                {
                    for (final int rIdx : forensicRows)
                    {
                        final RequestChunk.ForensicDetails fd = forensicMap.get(rIdx);
                        StorageIoUtils.writeUtfString(dos, fd != null ? fd.url() : null);
                        StorageIoUtils.writeUtfString(dos, fd != null ? fd.requestId() : null);
                        StorageIoUtils.writeUtfString(dos, fd != null ? fd.responseId() : null);
                        StorageIoUtils.writeUtfString(dos, fd != null ? fd.formData() : null);
                        StorageIoUtils.writeUtfString(dos, fd != null ? fd.rawName() : null);
                    }
                }
                catch (final IOException e)
                {
                    throw new RuntimeException("Failed to serialize Cold Forensic Tail Block", e);
                }

                final byte[] rawBytes = rawBaos.toByteArray();
                uncompressedForensicLength = rawBytes.length;

                // Compress the UTF-8 forensic tuples with Deflater.BEST_SPEED: URLs and form payloads share
                // high prefix/substring redundancy within a chunk, achieving ~5-8x compression at minimal CPU cost.
                final Deflater deflater = new Deflater(Deflater.BEST_SPEED);
                try
                {
                    deflater.setInput(rawBytes);
                    deflater.finish();
                    final ByteArrayOutputStream compBaos = new ByteArrayOutputStream(Math.max(256, rawBytes.length / 2));
                    final byte[] buf = new byte[4096];
                    while (!deflater.finished())
                    {
                        final int n = deflater.deflate(buf);
                        if (n > 0)
                        {
                            compBaos.write(buf, 0, n);
                        }
                    }
                    compressedForensicTail = compBaos.toByteArray();
                }
                finally
                {
                    deflater.end();
                }
            }

            return new RequestChunk(size, minTime, maxTime, maxRunTime, chunkHasFailures,
                                    timerIds.clone(), agentTestCaseIds.clone(), failedRows.clone(),
                                    compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
                                    compResponseCodes, compBytesSent, compBytesReceived,
                                    compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
                                    compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
                                    compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
                                    compUserNumbers, compFormDataEncodings, compUrlHashes,
                                    forensicRows, uncompressedForensicLength, compressedForensicTail);
        }

        @Override
        public void reset()
        {
            count = 0;
            minTime = Long.MAX_VALUE;
            maxTime = Long.MIN_VALUE;
            maxRunTime = 0;
            chunkHasFailures = false;
            timerIds.clear();
            agentTestCaseIds.clear();
            failedRows.clear();
            failedOrErrorRows.clear();
            slowestHeapSize = 0;
            forensicMap.clear();
            Arrays.fill(seenTimers, 0L);
            Arrays.fill(seenAgents, 0L);
            lastTimerRef = null;
            lastTimerStr = null;
            lastTimerId = -1;
            lastAgentRef = null;
            lastTxnRef = null;
            lastAgentStr = null;
            lastTxnStr = null;
            lastAgentId = -1;
            lastMethodRef = null;
            lastMethodStr = null;
            lastMethodId = -1;
            lastContentTypeRef = null;
            lastContentTypeStr = null;
            lastContentTypeId = -1;
            lastUsedIpRef = null;
            lastUsedIpStr = null;
            lastUsedIpId = -1;
            lastIpAddressesRef = null;
            lastIpAddressesStr = null;
            lastIpAddressesId = -1;
            lastUserNumRef = null;
            lastUserNumStr = null;
            lastUserNumId = -1;
            lastEncodingRef = null;
            lastEncodingStr = null;
            lastEncodingId = -1;
            lastSampleUrlTimerId = -1;
            lastSampleUrlHash = 0;
        }
    }
}
