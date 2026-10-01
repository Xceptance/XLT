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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.roaringbitmap.RoaringBitmap;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * High-performance compressed columnar chunk specialized for HTTP Request data ('R').
 * <p>
 * <b>Columnar Architecture &amp; Compression:</b>
 * Each {@link RequestChunk} stores up to 64,000 requests. Instead of instantiating 64,000
 * {@link RequestData} heap objects, all 24 attributes are organized into separate primitive integer
 * arrays and compressed using SIMD FastPFOR bit-packing:
 * <ul>
 *   <li><b>Timestamps &amp; Offsets:</b> Base timestamp stored as {@code minTime} (long) with individual
 *       row offsets compressed via monotonically integrated delta FastPFOR.</li>
 *   <li><b>Dictionary References:</b> 16-bit IDs for timer names, unified (agent, testCase) pairs,
 *       HTTP methods, MIME content types, and IP addresses.</li>
 *   <li><b>Network Performance Metrics:</b> Runtimes, response codes, bytes sent/received, DNS lookup time,
 *       TCP connect time, send duration, server wait time (busy), receive duration, time-to-first-byte (TTFB),
 *       and time-to-last-byte (TTLB).</li>
 *   <li><b>Diagnostic URL &amp; Error Data:</b> Representative sample URLs per timer and top slowest requests
 *       retained for report tables and outlier inspection.</li>
 * </ul>
 */
public class RequestChunk implements Chunk
{
    /** Number of rows stored in this chunk. */
    private final int rowCount;

    /** Earliest request timestamp (milliseconds since epoch) in this chunk. */
    private final long minTime;

    /** Latest request timestamp (milliseconds since epoch) in this chunk. */
    private final long maxTime;

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

    /** Sample URL strings mapped by timer name ID for report display. */
    final Map<Integer, List<String>> sampleUrlsByTimer;

    /** Sample list of the slowest requests within this chunk for outlier diagnostics. */
    final List<RequestData> slowestRequests;

    /**
     * Constructs a sealed {@link RequestChunk} with compressed columnar blocks and metadata.
     *
     * @param rowCount
     *            total records in chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of contained timer IDs
     * @param agentTestCaseIds
     *            bitmap of contained agent/test case IDs
     * @param failedRows
     *            bitmap of failed row indices
     * @param compTimeOffsets
     *            compressed time offset column
     * @param compTimerNameIds
     *            compressed timer name ID column
     * @param compAgentTestCaseIds
     *            compressed agent/test case ID column
     * @param compRunTimes
     *            compressed run times column
     * @param compResponseCodes
     *            compressed response codes column
     * @param compBytesSent
     *            compressed bytes sent column
     * @param compBytesReceived
     *            compressed bytes received column
     * @param compDnsTimes
     *            compressed DNS timings column
     * @param compConnectTimes
     *            compressed connection timings column
     * @param compSendTimes
     *            compressed send timings column
     * @param compServerBusyTimes
     *            compressed server busy timings column
     * @param compReceiveTimes
     *            compressed receive timings column
     * @param compTimeToFirstBytes
     *            compressed TTFB column
     * @param compTimeToLastBytes
     *            compressed TTLB column
     * @param compHttpMethods
     *            compressed HTTP methods column
     * @param compContentTypes
     *            compressed content types column
     * @param compUsedIps
     *            compressed used IPs column
     * @param compIpAddresses
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
     *
     * @param rowCount
     *            total records in chunk
     * @param minTime
     *            earliest timestamp in milliseconds
     * @param maxTime
     *            latest timestamp in milliseconds
     * @param timerNameIds
     *            bitmap of contained timer IDs
     * @param agentTestCaseIds
     *            bitmap of contained agent/test case IDs
     * @param failedRows
     *            bitmap of failed row indices
     * @param compTimeOffsets
     *            compressed time offset column
     * @param compTimerNameIds
     *            compressed timer name ID column
     * @param compAgentTestCaseIds
     *            compressed agent/test case ID column
     * @param compRunTimes
     *            compressed run times column
     * @param compResponseCodes
     *            compressed response codes column
     * @param compBytesSent
     *            compressed bytes sent column
     * @param compBytesReceived
     *            compressed bytes received column
     * @param compDnsTimes
     *            compressed DNS timings column
     * @param compConnectTimes
     *            compressed connection timings column
     * @param compSendTimes
     *            compressed send timings column
     * @param compServerBusyTimes
     *            compressed server busy timings column
     * @param compReceiveTimes
     *            compressed receive timings column
     * @param compTimeToFirstBytes
     *            compressed TTFB column
     * @param compTimeToLastBytes
     *            compressed TTLB column
     * @param compHttpMethods
     *            compressed HTTP methods column
     * @param compContentTypes
     *            compressed content types column
     * @param compUsedIps
     *            compressed used IPs column
     * @param compIpAddresses
     *            compressed IP addresses column
     * @param sampleUrlsByTimer
     *            representative URLs per timer ID
     * @param slowestRequests
     *            top slowest requests for reporting
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
        this.rowCount = rowCount;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.timerNameIds = timerNameIds;
        this.agentTestCaseIds = agentTestCaseIds;
        this.failedRows = failedRows;
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
        this.sampleUrlsByTimer = sampleUrlsByTimer;
        this.slowestRequests = slowestRequests;
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
