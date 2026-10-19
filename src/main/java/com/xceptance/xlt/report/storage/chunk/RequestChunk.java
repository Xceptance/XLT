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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.storage.StorageIoUtils;

/**
 * High-performance compressed columnar chunk specialized for HTTP Request data ('R').
 * <p>
 * <b>Columnar Architecture &amp; Compression:</b>
 * Each {@link RequestChunk} stores up to 64,000 requests. Instead of instantiating 64,000
 * {@link RequestData} heap objects, all attributes are organized into separate primitive integer
 * arrays and compressed using SIMD FastPFOR bit-packing, with high-cardinality forensic strings
 * stored in an on-demand Deflate-compressed Cold Forensic Tail Block:
 * <ul>
 *   <li><b>Timestamps &amp; Offsets:</b> Base timestamp stored as {@code minTime} (long) with individual
 *       row offsets compressed via monotonically integrated delta FastPFOR.</li>
 *   <li><b>Dictionary References:</b> IDs for timer names, unified (agent, testCase) pairs,
 *       HTTP methods, MIME content types, IP addresses, virtual user numbers, and form data encodings.</li>
 *   <li><b>Network Performance Metrics:</b> Runtimes, response codes, bytes sent/received, DNS lookup time,
 *       TCP connect time, send duration, server wait time (busy), receive duration, time-to-first-byte (TTFB),
 *       and time-to-last-byte (TTLB).</li>
 *   <li><b>Cold Forensic Tail Block:</b> Deflate-compressed block storing per-row {@code url}, {@code requestId},
 *       {@code responseId}, {@code formData}, and pre-merge {@code rawName} for failed/outlier rows (sparse mode)
 *       or all rows (full mode).</li>
 * </ul>
 */
public class RequestChunk implements Chunk
{
    /**
     * Per-row forensic diagnostic details stored in the Cold Forensic Tail Block.
     *
     * @param url
     *            the full unmerged request URL
     * @param requestId
     *            the unique request correlation ID
     * @param responseId
     *            the server response correlation ID
     * @param formData
     *            the HTTP POST/PUT form data payload
     * @param rawName
     *            the original pre-merge request timer name (e.g. "OpenStartPage.1.3")
     */
    public record ForensicDetails(String url, String requestId, String responseId, String formData, String rawName)
    {
    }

    /** Number of rows stored in this chunk. */
    private final int rowCount;

    /** Earliest request timestamp (milliseconds since epoch) in this chunk. */
    private final long minTime;

    /** Latest request timestamp (milliseconds since epoch) in this chunk. */
    private final long maxTime;

    /** Maximum request runtime duration (in milliseconds) across all rows in this chunk. */
    private final int maxRunTime;

    /** Whether this chunk contains any failed requests or HTTP 5xx / status 0 responses. */
    private final boolean hasFailuresFlag;

    /** RoaringBitmap containing all distinct 16-bit timer name IDs present in this chunk. */
    private final RoaringBitmap timerNameIds;

    /** RoaringBitmap containing all distinct 16-bit unified (agent, testCase) pair IDs. */
    private final RoaringBitmap agentTestCaseIds;

    /** RoaringBitmap tracking the row indices of requests that resulted in an error (failed requests). */
    private final RoaringBitmap failedRows;

    // --- Compressed Columnar Data Vectors (FastPFOR packed blocks) ---

    /** Compressed row time offsets relative to minTime. */
    final int[] compTimeOffsets;

    /** Compressed 16-bit timer name dictionary IDs. */
    final int[] compTimerNameIds;

    /** Compressed 16-bit unified (agent, testCase) pair dictionary IDs. */
    final int[] compAgentTestCaseIds;

    /** Compressed total request runtimes in milliseconds. */
    final int[] compRunTimes;

    /** Compressed HTTP response status codes (e.g. 200, 404, 500). */
    final int[] compResponseCodes;

    /** Compressed bytes sent (uplink payload size). */
    final int[] compBytesSent;

    /** Compressed bytes received (downlink response payload size). */
    final int[] compBytesReceived;

    /** Compressed DNS lookup duration in milliseconds. */
    final int[] compDnsTimes;

    /** Compressed TCP/SSL connection duration in milliseconds. */
    final int[] compConnectTimes;

    /** Compressed request transmission duration in milliseconds. */
    final int[] compSendTimes;

    /** Compressed server busy/processing duration in milliseconds. */
    final int[] compServerBusyTimes;

    /** Compressed response payload reading duration in milliseconds. */
    final int[] compReceiveTimes;

    /** Compressed Time To First Byte (TTFB) duration in milliseconds. */
    final int[] compTimeToFirstBytes;

    /** Compressed Time To Last Byte (TTLB) duration in milliseconds. */
    final int[] compTimeToLastBytes;

    /** Compressed HTTP method string dictionary IDs ("GET", "POST", etc.). */
    final int[] compHttpMethods;

    /** Compressed MIME content type string dictionary IDs ("text/html", etc.). */
    final int[] compContentTypes;

    /** Compressed Used IP address string dictionary IDs. */
    final int[] compUsedIps;

    /** Compressed IP address string dictionary IDs. */
    final int[] compIpAddresses;

    /** Compressed virtual user thread sequence number dictionary IDs. */
    final int[] compUserNumbers;

    /** Compressed form data encoding string dictionary IDs. */
    final int[] compFormDataEncodings;

    /** Compressed 32-bit fragment-free URL hash codes for distinct URL counting. */
    final int[] compUrlHashes;

    /** Sample URL strings mapped by timer name ID for report display. */
    final Map<Integer, List<String>> sampleUrlsByTimer;

    /** Sample list of the slowest requests within this chunk for outlier diagnostics. */
    final List<RequestData> slowestRequests;

    // --- Cold Forensic Tail Block ---

    /** Bitmap of row indices (0..rowCount-1) present in the Cold Forensic Tail Block. */
    final RoaringBitmap forensicRows;

    /** Uncompressed byte length of the serialized forensic tail block. */
    final int uncompressedForensicLength;

    /** Deflate-compressed forensic tail payload bytes. */
    final byte[] compressedForensicTail;

    /** Exact byte length of the serialized hot vector payload (header + bitmaps + 21 int[] columns). */
    private final int vectorPayloadLength;

    /** Lazily inflated forensic details map, populated only upon explicit drill-down inspection. */
    private volatile Map<Integer, ForensicDetails> inflatedForensicDetails;

    /**
     * Constructs a sealed {@link RequestChunk} without chunk-local sample URLs or slowest requests.
     */
    public RequestChunk(final int rowCount,
                        final long minTime,
                        final long maxTime,
                        final RoaringBitmap timerNameIds,
                        final RoaringBitmap agentTestCaseIds,
                        final RoaringBitmap failedRows,
                        final int[] compTimeOffsets,
                        final int[] compTimerNameIds,
                        final int[] compAgentTestCaseIds,
                        final int[] compRunTimes,
                        final int[] compResponseCodes,
                        final int[] compBytesSent,
                        final int[] compBytesReceived,
                        final int[] compDnsTimes,
                        final int[] compConnectTimes,
                        final int[] compSendTimes,
                        final int[] compServerBusyTimes,
                        final int[] compReceiveTimes,
                        final int[] compTimeToFirstBytes,
                        final int[] compTimeToLastBytes,
                        final int[] compHttpMethods,
                        final int[] compContentTypes,
                        final int[] compUsedIps,
                        final int[] compIpAddresses)
    {
        this(rowCount, minTime, maxTime, timerNameIds, agentTestCaseIds, failedRows,
             compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
             compResponseCodes, compBytesSent, compBytesReceived,
             compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
             compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
             compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
             Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Constructs a sealed {@link RequestChunk} with compressed columnar blocks and metadata.
     */
    public RequestChunk(final int rowCount,
                        final long minTime,
                        final long maxTime,
                        final RoaringBitmap timerNameIds,
                        final RoaringBitmap agentTestCaseIds,
                        final RoaringBitmap failedRows,
                        final int[] compTimeOffsets,
                        final int[] compTimerNameIds,
                        final int[] compAgentTestCaseIds,
                        final int[] compRunTimes,
                        final int[] compResponseCodes,
                        final int[] compBytesSent,
                        final int[] compBytesReceived,
                        final int[] compDnsTimes,
                        final int[] compConnectTimes,
                        final int[] compSendTimes,
                        final int[] compServerBusyTimes,
                        final int[] compReceiveTimes,
                        final int[] compTimeToFirstBytes,
                        final int[] compTimeToLastBytes,
                        final int[] compHttpMethods,
                        final int[] compContentTypes,
                        final int[] compUsedIps,
                        final int[] compIpAddresses,
                        final Map<Integer, List<String>> sampleUrlsByTimer,
                        final List<RequestData> slowestRequests)
    {
        this(rowCount, minTime, maxTime, 0, failedRows != null && !failedRows.isEmpty(),
             timerNameIds, agentTestCaseIds, failedRows,
             compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
             compResponseCodes, compBytesSent, compBytesReceived,
             compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
             compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
             compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
             new int[0], new int[0], new int[0],
             new RoaringBitmap(), 0, new byte[0],
             sampleUrlsByTimer, slowestRequests);
    }

    /**
     * Constructs a sealed V3 {@link RequestChunk} with 20 compressed columnar blocks, Zone Map boundaries,
     * and the Deflate-compressed Cold Forensic Tail Block.
     */
    public RequestChunk(final int rowCount,
                        final long minTime,
                        final long maxTime,
                        final int maxRunTime,
                        final boolean hasFailuresFlag,
                        final RoaringBitmap timerNameIds,
                        final RoaringBitmap agentTestCaseIds,
                        final RoaringBitmap failedRows,
                        final int[] compTimeOffsets,
                        final int[] compTimerNameIds,
                        final int[] compAgentTestCaseIds,
                        final int[] compRunTimes,
                        final int[] compResponseCodes,
                        final int[] compBytesSent,
                        final int[] compBytesReceived,
                        final int[] compDnsTimes,
                        final int[] compConnectTimes,
                        final int[] compSendTimes,
                        final int[] compServerBusyTimes,
                        final int[] compReceiveTimes,
                        final int[] compTimeToFirstBytes,
                        final int[] compTimeToLastBytes,
                        final int[] compHttpMethods,
                        final int[] compContentTypes,
                        final int[] compUsedIps,
                        final int[] compIpAddresses,
                        final int[] compUserNumbers,
                        final int[] compFormDataEncodings,
                        final RoaringBitmap forensicRows,
                        final int uncompressedForensicLength,
                        final byte[] compressedForensicTail)
    {
        this(rowCount, minTime, maxTime, maxRunTime, hasFailuresFlag,
             timerNameIds, agentTestCaseIds, failedRows,
             compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
             compResponseCodes, compBytesSent, compBytesReceived,
             compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
             compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
             compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
             compUserNumbers, compFormDataEncodings, new int[0],
             forensicRows, uncompressedForensicLength, compressedForensicTail,
             Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Constructs a sealed V3 {@link RequestChunk} with all 21 compressed columnar blocks (including per-row URL hashes),
     * Zone Map boundaries, and the Deflate-compressed Cold Forensic Tail Block.
     */
    public RequestChunk(final int rowCount,
                        final long minTime,
                        final long maxTime,
                        final int maxRunTime,
                        final boolean hasFailuresFlag,
                        final RoaringBitmap timerNameIds,
                        final RoaringBitmap agentTestCaseIds,
                        final RoaringBitmap failedRows,
                        final int[] compTimeOffsets,
                        final int[] compTimerNameIds,
                        final int[] compAgentTestCaseIds,
                        final int[] compRunTimes,
                        final int[] compResponseCodes,
                        final int[] compBytesSent,
                        final int[] compBytesReceived,
                        final int[] compDnsTimes,
                        final int[] compConnectTimes,
                        final int[] compSendTimes,
                        final int[] compServerBusyTimes,
                        final int[] compReceiveTimes,
                        final int[] compTimeToFirstBytes,
                        final int[] compTimeToLastBytes,
                        final int[] compHttpMethods,
                        final int[] compContentTypes,
                        final int[] compUsedIps,
                        final int[] compIpAddresses,
                        final int[] compUserNumbers,
                        final int[] compFormDataEncodings,
                        final int[] compUrlHashes,
                        final RoaringBitmap forensicRows,
                        final int uncompressedForensicLength,
                        final byte[] compressedForensicTail)
    {
        this(rowCount, minTime, maxTime, maxRunTime, hasFailuresFlag,
             timerNameIds, agentTestCaseIds, failedRows,
             compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
             compResponseCodes, compBytesSent, compBytesReceived,
             compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
             compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
             compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
             compUserNumbers, compFormDataEncodings, compUrlHashes,
             forensicRows, uncompressedForensicLength, compressedForensicTail,
             Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Full constructor for a sealed V3 {@link RequestChunk} without explicit URL hashes.
     */
    public RequestChunk(final int rowCount,
                        final long minTime,
                        final long maxTime,
                        final int maxRunTime,
                        final boolean hasFailuresFlag,
                        final RoaringBitmap timerNameIds,
                        final RoaringBitmap agentTestCaseIds,
                        final RoaringBitmap failedRows,
                        final int[] compTimeOffsets,
                        final int[] compTimerNameIds,
                        final int[] compAgentTestCaseIds,
                        final int[] compRunTimes,
                        final int[] compResponseCodes,
                        final int[] compBytesSent,
                        final int[] compBytesReceived,
                        final int[] compDnsTimes,
                        final int[] compConnectTimes,
                        final int[] compSendTimes,
                        final int[] compServerBusyTimes,
                        final int[] compReceiveTimes,
                        final int[] compTimeToFirstBytes,
                        final int[] compTimeToLastBytes,
                        final int[] compHttpMethods,
                        final int[] compContentTypes,
                        final int[] compUsedIps,
                        final int[] compIpAddresses,
                        final int[] compUserNumbers,
                        final int[] compFormDataEncodings,
                        final RoaringBitmap forensicRows,
                        final int uncompressedForensicLength,
                        final byte[] compressedForensicTail,
                        final Map<Integer, List<String>> sampleUrlsByTimer,
                        final List<RequestData> slowestRequests)
    {
        this(rowCount, minTime, maxTime, maxRunTime, hasFailuresFlag,
             timerNameIds, agentTestCaseIds, failedRows,
             compTimeOffsets, compTimerNameIds, compAgentTestCaseIds, compRunTimes,
             compResponseCodes, compBytesSent, compBytesReceived,
             compDnsTimes, compConnectTimes, compSendTimes, compServerBusyTimes,
             compReceiveTimes, compTimeToFirstBytes, compTimeToLastBytes,
             compHttpMethods, compContentTypes, compUsedIps, compIpAddresses,
             compUserNumbers, compFormDataEncodings, new int[0],
             forensicRows, uncompressedForensicLength, compressedForensicTail,
             sampleUrlsByTimer, slowestRequests);
    }

    /**
     * Full constructor for a sealed V3 {@link RequestChunk}.
     */
    public RequestChunk(final int rowCount,
                        final long minTime,
                        final long maxTime,
                        final int maxRunTime,
                        final boolean hasFailuresFlag,
                        final RoaringBitmap timerNameIds,
                        final RoaringBitmap agentTestCaseIds,
                        final RoaringBitmap failedRows,
                        final int[] compTimeOffsets,
                        final int[] compTimerNameIds,
                        final int[] compAgentTestCaseIds,
                        final int[] compRunTimes,
                        final int[] compResponseCodes,
                        final int[] compBytesSent,
                        final int[] compBytesReceived,
                        final int[] compDnsTimes,
                        final int[] compConnectTimes,
                        final int[] compSendTimes,
                        final int[] compServerBusyTimes,
                        final int[] compReceiveTimes,
                        final int[] compTimeToFirstBytes,
                        final int[] compTimeToLastBytes,
                        final int[] compHttpMethods,
                        final int[] compContentTypes,
                        final int[] compUsedIps,
                        final int[] compIpAddresses,
                        final int[] compUserNumbers,
                        final int[] compFormDataEncodings,
                        final int[] compUrlHashes,
                        final RoaringBitmap forensicRows,
                        final int uncompressedForensicLength,
                        final byte[] compressedForensicTail,
                        final Map<Integer, List<String>> sampleUrlsByTimer,
                        final List<RequestData> slowestRequests)
    {
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.maxRunTime = maxRunTime;
        this.hasFailuresFlag = hasFailuresFlag || (failedRows != null && !failedRows.isEmpty());
        this.timerNameIds = timerNameIds != null ? timerNameIds : new RoaringBitmap();
        this.agentTestCaseIds = agentTestCaseIds != null ? agentTestCaseIds : new RoaringBitmap();
        this.failedRows = failedRows != null ? failedRows : new RoaringBitmap();
        this.compTimeOffsets = compTimeOffsets;
        this.compTimerNameIds = compTimerNameIds;
        this.compAgentTestCaseIds = compAgentTestCaseIds;
        this.compRunTimes = compRunTimes;
        this.compResponseCodes = compResponseCodes;
        this.compBytesSent = compBytesSent;
        this.compBytesReceived = compBytesReceived;
        this.compDnsTimes = compDnsTimes;
        this.compConnectTimes = compConnectTimes;
        this.compSendTimes = compSendTimes;
        this.compServerBusyTimes = compServerBusyTimes;
        this.compReceiveTimes = compReceiveTimes;
        this.compTimeToFirstBytes = compTimeToFirstBytes;
        this.compTimeToLastBytes = compTimeToLastBytes;
        this.compHttpMethods = compHttpMethods;
        this.compContentTypes = compContentTypes;
        this.compUsedIps = compUsedIps;
        this.compIpAddresses = compIpAddresses;
        this.compUserNumbers = compUserNumbers != null ? compUserNumbers : new int[0];
        this.compFormDataEncodings = compFormDataEncodings != null ? compFormDataEncodings : new int[0];
        this.compUrlHashes = compUrlHashes != null ? compUrlHashes : new int[0];
        this.forensicRows = forensicRows != null ? forensicRows : new RoaringBitmap();
        this.uncompressedForensicLength = uncompressedForensicLength;
        this.compressedForensicTail = compressedForensicTail != null ? compressedForensicTail : new byte[0];
        this.sampleUrlsByTimer = sampleUrlsByTimer != null ? sampleUrlsByTimer : Collections.emptyMap();
        this.slowestRequests = slowestRequests != null ? slowestRequests : Collections.emptyList();
        this.vectorPayloadLength = computeVectorPayloadLength();
    }

    /**
     * Precomputes the exact serialized byte length of the Hot Vector Prefix (everything preceding
     * the Cold Forensic Tail Block in {@link RequestChunkHandler#writeChunk(Chunk, java.io.DataOutput)}).
     * <p>
     * <b>Why this is precomputed:</b>
     * Standard report generation scans require only the 21 compressed integer vectors and bitmaps, never
     * the high-entropy per-row forensic strings (URLs, request/response IDs, POST form data, raw names).
     * By recording this exact byte boundary in the V3 catalog tail index, {@link DiskChunk#loadVectorsOnly}
     * can issue a single bounded positional {@code FileChannel.read} of {@code vectorPayloadLength} bytes,
     * completely avoiding disk I/O transfer and Deflate decompression for the cold forensic tail block.
     */
    private int computeVectorPayloadLength()
    {
        // 1. Fixed 29-byte header:
        //    rowCount (4B) + minTime (8B) + maxTime (8B) + maxRunTime (4B) + hasFailures (1B) + vectorPayloadLength (4B)
        int size = 29;

        // 2. Variable-length serialized RoaringBitmap index sets
        size += timerNameIds.serializedSizeInBytes();
        size += agentTestCaseIds.serializedSizeInBytes();
        size += failedRows.serializedSizeInBytes();

        // 3. All 21 FastPFOR-compressed integer column vectors (each prefixed by a 4-byte int length)
        size += arrayBytes(compTimeOffsets);
        size += arrayBytes(compTimerNameIds);
        size += arrayBytes(compAgentTestCaseIds);
        size += arrayBytes(compRunTimes);
        size += arrayBytes(compResponseCodes);
        size += arrayBytes(compBytesSent);
        size += arrayBytes(compBytesReceived);
        size += arrayBytes(compDnsTimes);
        size += arrayBytes(compConnectTimes);
        size += arrayBytes(compSendTimes);
        size += arrayBytes(compServerBusyTimes);
        size += arrayBytes(compReceiveTimes);
        size += arrayBytes(compTimeToFirstBytes);
        size += arrayBytes(compTimeToLastBytes);
        size += arrayBytes(compHttpMethods);
        size += arrayBytes(compContentTypes);
        size += arrayBytes(compUsedIps);
        size += arrayBytes(compIpAddresses);
        size += arrayBytes(compUserNumbers);
        size += arrayBytes(compFormDataEncodings);
        size += arrayBytes(compUrlHashes);
        return size;
    }

    /**
     * Returns the serialized byte footprint of a length-prefixed {@code int[]} column vector
     * (4 bytes for the array length header + 4 bytes per 32-bit integer element).
     */
    private static int arrayBytes(final int[] arr)
    {
        return 4 + (arr != null ? arr.length * 4 : 0);
    }

    @Override
    public char getTypeCode()
    {
        return 'R';
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
    public int getMaxRunTime()
    {
        return maxRunTime;
    }

    @Override
    public boolean hasFailures()
    {
        return hasFailuresFlag;
    }

    @Override
    public int getVectorPayloadLength()
    {
        return vectorPayloadLength;
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
     * Returns a {@link RoaringBitmap} containing the row indexes of all failed requests in this chunk.
     *
     * @return bitmap of failed rows
     */
    public RoaringBitmap getFailedRows()
    {
        return failedRows;
    }

    public int[] getCompTimeOffsets()
    {
        return compTimeOffsets;
    }

    public int[] getCompTimerNameIds()
    {
        return compTimerNameIds;
    }

    public int[] getCompAgentTestCaseIds()
    {
        return compAgentTestCaseIds;
    }

    public int[] getCompRunTimes()
    {
        return compRunTimes;
    }

    public int[] getCompResponseCodes()
    {
        return compResponseCodes;
    }

    public int[] getCompBytesSent()
    {
        return compBytesSent;
    }

    public int[] getCompBytesReceived()
    {
        return compBytesReceived;
    }

    public int[] getCompDnsTimes()
    {
        return compDnsTimes;
    }

    public int[] getCompConnectTimes()
    {
        return compConnectTimes;
    }

    public int[] getCompSendTimes()
    {
        return compSendTimes;
    }

    public int[] getCompServerBusyTimes()
    {
        return compServerBusyTimes;
    }

    public int[] getCompReceiveTimes()
    {
        return compReceiveTimes;
    }

    public int[] getCompTimeToFirstBytes()
    {
        return compTimeToFirstBytes;
    }

    public int[] getCompTimeToLastBytes()
    {
        return compTimeToLastBytes;
    }

    public int[] getCompHttpMethods()
    {
        return compHttpMethods;
    }

    public int[] getCompContentTypes()
    {
        return compContentTypes;
    }

    public int[] getCompUsedIps()
    {
        return compUsedIps;
    }

    public int[] getCompIpAddresses()
    {
        return compIpAddresses;
    }

    /**
     * Returns the compressed virtual user numbers array.
     *
     * @return compressed user numbers
     */
    public int[] getCompUserNumbers()
    {
        return compUserNumbers;
    }

    /**
     * Returns the compressed form data encodings array.
     *
     * @return compressed form data encodings
     */
    public int[] getCompFormDataEncodings()
    {
        return compFormDataEncodings;
    }

    /**
     * Returns the compressed 32-bit fragment-free URL hashes array.
     *
     * @return compressed URL hashes
     */
    public int[] getCompUrlHashes()
    {
        return compUrlHashes;
    }

    /**
     * Returns the bitmap of row indices stored in the Cold Forensic Tail Block.
     *
     * @return bitmap of rows with forensic details
     */
    public RoaringBitmap getForensicRows()
    {
        return forensicRows;
    }

    /**
     * Returns whether the specified row index has forensic details in the Cold Forensic Tail Block.
     *
     * @param rowIdx
     *            row index within this chunk (0..rowCount-1)
     * @return {@code true} if forensic details were captured for this row
     */
    public boolean hasForensicDetails(final int rowIdx)
    {
        return forensicRows != null && forensicRows.contains(rowIdx);
    }

    /**
     * Returns the compressed byte array of the Cold Forensic Tail Block.
     *
     * @return Deflate-compressed forensic tail bytes
     */
    public byte[] getCompressedForensicTail()
    {
        return compressedForensicTail;
    }

    /**
     * Returns the uncompressed byte length of the Cold Forensic Tail Block.
     *
     * @return uncompressed byte length
     */
    public int getUncompressedForensicLength()
    {
        return uncompressedForensicLength;
    }

    /**
     * Lazily inflates the Cold Forensic Tail Block and returns the {@link ForensicDetails} for the given row index,
     * or {@code null} if no forensic details are stored for that row.
     *
     * @param rowIdx
     *            row index within this chunk
     * @return forensic details for the row, or {@code null}
     */
    public ForensicDetails getForensicDetails(final int rowIdx)
    {
        if (!hasForensicDetails(rowIdx))
        {
            return null;
        }
        return getAllForensicDetails().get(rowIdx);
    }

    /**
     * Lazily inflates and parses all entries in the Cold Forensic Tail Block on demand.
     * <p>
     * <b>Design Rationale:</b>
     * Because standard report scans never inspect per-row forensic strings (or only inspect them on the rare
     * chunk that matches an ad-hoc forensic query), keeping the tail block compressed as a single {@code byte[]}
     * avoids thousands of {@link String} and {@link ForensicDetails} heap allocations unless explicitly requested.
     *
     * @return unmodifiable map of row index to {@link ForensicDetails}
     */
    public Map<Integer, ForensicDetails> getAllForensicDetails()
    {
        // Fast-path: return already-inflated map without synchronization if previously materialized
        Map<Integer, ForensicDetails> local = this.inflatedForensicDetails;
        if (local != null)
        {
            return local;
        }
        synchronized (this)
        {
            // Double-checked locking so concurrent reader threads inflate the tail block at most once
            local = this.inflatedForensicDetails;
            if (local != null)
            {
                return local;
            }

            // If this chunk was loaded via loadVectorsOnly() or had no forensic rows, return empty map
            if (forensicRows == null || forensicRows.isEmpty()
                || compressedForensicTail == null || compressedForensicTail.length == 0
                || uncompressedForensicLength <= 0)
            {
                local = Collections.emptyMap();
                this.inflatedForensicDetails = local;
                return local;
            }

            // 1. Inflate the Deflate-compressed byte stream into an exact-sized uncompressed buffer
            final byte[] raw = new byte[uncompressedForensicLength];
            final Inflater inflater = new Inflater();
            try
            {
                inflater.setInput(compressedForensicTail);
                int offset = 0;
                while (!inflater.finished() && offset < raw.length)
                {
                    final int count = inflater.inflate(raw, offset, raw.length - offset);
                    if (count == 0 && inflater.needsInput())
                    {
                        break;
                    }
                    offset += count;
                }
            }
            catch (final DataFormatException e)
            {
                throw new RuntimeException("Failed to inflate RequestChunk Cold Forensic Tail Block", e);
            }
            finally
            {
                inflater.end();
            }

            // 2. Reconstruct the (rowIdx -> ForensicDetails) map by iterating forensicRows in ascending order.
            //    RoaringBitmap iteration order is strictly ascending and deterministic, matching the exact order
            //    in which RequestChunkBuilder.seal() wrote the 5-string tuples into the Deflate stream. This
            //    eliminates the need to store redundant row index integers inside the compressed byte stream.
            final int cardinality = forensicRows.getCardinality();
            final Map<Integer, ForensicDetails> map = new HashMap<>(cardinality * 4 / 3 + 1);
            try (final DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw)))
            {
                for (final int rowIdx : forensicRows)
                {
                    final String url = StorageIoUtils.readUtfString(in);
                    final String reqId = StorageIoUtils.readUtfString(in);
                    final String respId = StorageIoUtils.readUtfString(in);
                    final String form = StorageIoUtils.readUtfString(in);
                    final String rawName = StorageIoUtils.readUtfString(in);
                    map.put(rowIdx, new ForensicDetails(url, reqId, respId, form, rawName));
                }
            }
            catch (final IOException e)
            {
                throw new RuntimeException("Failed to deserialize RequestChunk Cold Forensic Tail Block", e);
            }

            local = Collections.unmodifiableMap(map);
            this.inflatedForensicDetails = local;
            return local;
        }
    }

    /**
     * Returns sample URLs retained per timer ID for reporting.
     *
     * @return map of timer ID to sample URL list
     */
    public Map<Integer, List<String>> getSampleUrlsByTimer()
    {
        return sampleUrlsByTimer;
    }

    /**
     * Returns the slowest requests observed in this chunk for outlier reporting.
     *
     * @return list of slowest request records
     */
    public List<RequestData> getSlowestRequests()
    {
        return slowestRequests;
    }
}
