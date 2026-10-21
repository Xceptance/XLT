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

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Objects (DTOs) and query specifications for the interactive ChunkDB Explorer API.
 * <p>
 * All records in this class serialize cleanly to and from JSON via Jackson without custom serializers,
 * and represent the contract between {@link ColumnarQueryEngine}, {@code ChunkApiServlet}, and the
 * browser-based ChunkDB Explorer UI.
 */
public final class QueryModels
{
    private QueryModels()
    {
    }

    /**
     * Dataset metadata and facet dictionary returned by {@code GET /api/v1/metadata}.
     *
     * @param minTime
     *            global earliest record timestamp (ms since Unix epoch)
     * @param maxTime
     *            global latest record timestamp (ms since Unix epoch)
     * @param totalRecords
     *            total number of records across all chunks in ChunkDB
     * @param totalChunks
     *            total number of columnar chunks in the catalog
     * @param chunksByType
     *            chunk counts partitioned by single-character type code ("R", "A", "T", "P", "C", "E", "W")
     * @param timers
     *            distinct timer names partitioned by record type code ("R", "A", "T", "P", "C", "E")
     * @param agents
     *            sorted list of all distinct load agent IDs
     * @param scenarios
     *            sorted list of all distinct test case / scenario names
     * @param httpMethods
     *            sorted list of distinct HTTP request methods observed in {@code compHttpMethods}
     * @param contentTypes
     *            sorted list of distinct MIME content types observed in {@code compContentTypes}
     * @param usedIps
     *            sorted list of distinct remote server IP addresses observed in {@code compUsedIps}
     * @param forensicMode
     *            active forensic details retention mode ("sparse" or "full")
     * @param memoryMode
     *            active chunk memory residency mode ("DISK", "CACHE", or "PRELOAD")
     */
    public record MetadataResponse(
        long minTime,
        long maxTime,
        long totalRecords,
        int totalChunks,
        Map<String, Integer> chunksByType,
        Map<String, List<String>> timers,
        List<String> agents,
        List<String> scenarios,
        List<String> httpMethods,
        List<String> contentTypes,
        List<String> usedIps,
        String forensicMode,
        String memoryMode
    )
    {
        public MetadataResponse(
            final long minTime,
            final long maxTime,
            final long totalRecords,
            final int totalChunks,
            final Map<String, Integer> chunksByType,
            final Map<String, List<String>> timers,
            final List<String> agents,
            final List<String> scenarios,
            final List<String> httpMethods,
            final List<String> contentTypes,
            final List<String> usedIps,
            final String forensicMode
        )
        {
            this(minTime, maxTime, totalRecords, totalChunks, chunksByType, timers,
                 agents, scenarios, httpMethods, contentTypes, usedIps, forensicMode, "DISK");
        }
    }

    /**
     * Request payload for {@code POST /api/v1/timeseries}.
     *
     * @param recordType
     *            target record type code ("R", "A", "T", "P", "C"); defaults to "R"
     * @param fromTime
     *            inclusive start timestamp in epoch ms (0 or negative defaults to dataset minTime)
     * @param toTime
     *            inclusive end timestamp in epoch ms (0 or negative defaults to dataset maxTime)
     * @param bucketWidthMs
     *            requested time bucket width in ms (0 or negative triggers automatic resolution bucketing)
     * @param timers
     *            optional list of exact timer names to include
     * @param timerQuery
     *            optional case-insensitive substring or regex pattern to filter timer names
     * @param agents
     *            optional list of agent names to include
     * @param scenarios
     *            optional list of test scenario names to include
     * @param usedIps
     *            optional list of server IP addresses to include
     * @param httpMethods
     *            optional list of HTTP methods to include
     * @param contentTypes
     *            optional list of MIME content types to include
     * @param minStatusCode
     *            optional minimum HTTP status code (inclusive)
     * @param maxStatusCode
     *            optional maximum HTTP status code (inclusive)
     * @param failedOnly
     *            when {@code true}, only include failed records (or HTTP status &gt;= 400 / == 0 for requests if status range not set)
     * @param minRunTime
     *            optional minimum runtime threshold in milliseconds (inclusive)
     * @param groupBy
     *            optional series split dimension ("NONE", "STATUS_CODE", "USED_IP", "AGENT", "SCENARIO", "TIMER", "HTTP_METHOD", "CONTENT_TYPE")
     */
    public record TimeSeriesQuerySpec(
        String recordType,
        long fromTime,
        long toTime,
        int bucketWidthMs,
        List<String> timers,
        String timerQuery,
        List<String> agents,
        List<String> scenarios,
        List<String> usedIps,
        List<String> httpMethods,
        List<String> contentTypes,
        Integer minStatusCode,
        Integer maxStatusCode,
        Boolean failedOnly,
        Integer minRunTime,
        String groupBy
    )
    {
    }

    /**
     * Per-group time-series breakdown returned when {@code groupBy} is active on {@code POST /api/v1/timeseries}.
     *
     * @param key
     *            dimension value label (e.g. "200", "503", "10.0.1.12", "TCheckout")
     * @param totalCount
     *            total matching observations in this group across the queried window
     * @param totalErrors
     *            total failed observations in this group across the queried window
     * @param count
     *            per-bucket observation counts
     * @param rps
     *            per-bucket throughput (requests/records per second)
     * @param errors
     *            per-bucket failure counts
     * @param mean
     *            per-bucket arithmetic mean runtime in ms
     * @param p50
     *            per-bucket median (p50) runtime in ms
     * @param p95
     *            per-bucket 95th percentile runtime in ms
     */
    public record GroupedSeries(
        String key,
        long totalCount,
        long totalErrors,
        int[] count,
        double[] rps,
        int[] errors,
        double[] mean,
        double[] p50,
        double[] p95
    )
    {
    }

    /**
     * Response payload for {@code POST /api/v1/timeseries}, formatted as parallel columnar arrays
     * for zero-copy ingestion into {@code uPlot}.
     *
     * @param recordType
     *            evaluated record type ("R", "A", "T", "P", "C")
     * @param fromTime
     *            effective start timestamp in epoch ms
     * @param toTime
     *            effective end timestamp in epoch ms
     * @param bucketWidthMs
     *            effective bucket width in ms
     * @param bucketCount
     *            number of time buckets
     * @param queryTimeMs
     *            server-side query execution time in ms
     * @param chunksScanned
     *            number of candidate chunks decompressed after catalog/Zone Map pruning
     * @param rowsMatched
     *            total number of rows matching the filter across all buckets
     * @param timestamps
     *            bucket start timestamps in epoch milliseconds
     * @param count
     *            per-bucket observation counts
     * @param rps
     *            per-bucket throughput (records/sec)
     * @param errors
     *            per-bucket failed record counts
     * @param mean
     *            per-bucket mean runtime in ms
     * @param min
     *            per-bucket minimum runtime in ms
     * @param max
     *            per-bucket maximum runtime in ms
     * @param p50
     *            per-bucket 50th percentile (median) runtime in ms
     * @param p90
     *            per-bucket 90th percentile runtime in ms
     * @param p95
     *            per-bucket 95th percentile runtime in ms
     * @param p99
     *            per-bucket 99th percentile runtime in ms
     * @param bytesSent
     *            per-bucket total bytes sent
     * @param bytesReceived
     *            per-bucket total bytes received
     * @param avgDns
     *            per-bucket average DNS lookup time in ms
     * @param avgConnect
     *            per-bucket average TCP/TLS connect time in ms
     * @param avgSend
     *            per-bucket average request send time in ms
     * @param avgServerBusy
     *            per-bucket average server busy / wait time in ms
     * @param avgReceive
     *            per-bucket average response receive time in ms
     * @param avgTtfb
     *            per-bucket average time-to-first-byte in ms
     * @param series
     *            optional grouped breakdown series when {@code groupBy != NONE}
     */
    public record TimeSeriesResponse(
        String recordType,
        long fromTime,
        long toTime,
        int bucketWidthMs,
        int bucketCount,
        long queryTimeMs,
        int chunksScanned,
        long rowsMatched,
        long[] timestamps,
        int[] count,
        double[] rps,
        int[] errors,
        double[] mean,
        int[] min,
        int[] max,
        double[] p50,
        double[] p90,
        double[] p95,
        double[] p99,
        long[] bytesSent,
        long[] bytesReceived,
        double[] avgDns,
        double[] avgConnect,
        double[] avgSend,
        double[] avgServerBusy,
        double[] avgReceive,
        double[] avgTtfb,
        List<GroupedSeries> series
    )
    {
    }

    /**
     * Request payload for {@code POST /api/v1/summary-table}.
     *
     * @param recordType
     *            target record type code ("R", "A", "T", "P", "C"); defaults to "R"
     * @param fromTime
     *            inclusive start timestamp in epoch ms
     * @param toTime
     *            inclusive end timestamp in epoch ms
     * @param timers
     *            optional list of exact timer names to include
     * @param timerQuery
     *            optional case-insensitive substring or regex pattern to filter timer names
     * @param agents
     *            optional list of agent names to include
     * @param scenarios
     *            optional list of test scenario names to include
     * @param usedIps
     *            optional list of server IP addresses to include
     * @param httpMethods
     *            optional list of HTTP methods to include
     * @param contentTypes
     *            optional list of MIME content types to include
     * @param minStatusCode
     *            optional minimum HTTP status code (inclusive)
     * @param maxStatusCode
     *            optional maximum HTTP status code (inclusive)
     * @param failedOnly
     *            when {@code true}, only include failed records
     * @param minRunTime
     *            optional minimum runtime threshold in ms
     * @param groupBy
     *            dimension to aggregate by ("TIMER", "USED_IP", "AGENT", "SCENARIO", "STATUS_CODE", "CONTENT_TYPE", "HTTP_METHOD")
     */
    public record SummaryTableQuerySpec(
        String recordType,
        long fromTime,
        long toTime,
        List<String> timers,
        String timerQuery,
        List<String> agents,
        List<String> scenarios,
        List<String> usedIps,
        List<String> httpMethods,
        List<String> contentTypes,
        Integer minStatusCode,
        Integer maxStatusCode,
        Boolean failedOnly,
        Integer minRunTime,
        String groupBy
    )
    {
    }

    /**
     * Single aggregated dimension row in {@link SummaryTableResponse}.
     *
     * @param key
     *            group key (e.g. timer name, IP address, agent name, status code)
     * @param count
     *            total matching observations
     * @param rps
     *            average throughput (records/sec) across the queried window
     * @param errors
     *            total failed observations
     * @param errorPct
     *            percentage of failed observations (0.0 .. 100.0)
     * @param mean
     *            arithmetic mean runtime in ms
     * @param min
     *            minimum runtime in ms
     * @param max
     *            maximum runtime in ms
     * @param p50
     *            50th percentile (median) runtime in ms
     * @param p90
     *            90th percentile runtime in ms
     * @param p95
     *            95th percentile runtime in ms
     * @param p99
     *            99th percentile runtime in ms
     * @param p999
     *            99.9th percentile runtime in ms
     * @param bytesSent
     *            total bytes sent
     * @param bytesReceived
     *            total bytes received
     * @param avgDns
     *            average DNS lookup time in ms
     * @param avgConnect
     *            average TCP/TLS connect time in ms
     * @param avgSend
     *            average request send time in ms
     * @param avgServerBusy
     *            average server busy time in ms
     * @param avgReceive
     *            average response receive time in ms
     * @param avgTtfb
     *            average time-to-first-byte in ms
     * @param avgTtlb
     *            average time-to-last-byte in ms
     * @param distinctUrls
     *            approximate or exact count of distinct URLs observed for this group
     * @param sampleUrl
     *            representative sample URL (when applicable)
     */
    public record SummaryRow(
        String key,
        long count,
        double rps,
        long errors,
        double errorPct,
        double mean,
        int min,
        int max,
        double p50,
        double p90,
        double p95,
        double p99,
        double p999,
        long bytesSent,
        long bytesReceived,
        double avgDns,
        double avgConnect,
        double avgSend,
        double avgServerBusy,
        double avgReceive,
        double avgTtfb,
        double avgTtlb,
        int distinctUrls,
        String sampleUrl
    )
    {
    }

    /**
     * Response payload for {@code POST /api/v1/summary-table}.
     *
     * @param groupBy
     *            evaluated grouping dimension
     * @param recordType
     *            evaluated record type ("R", "A", "T", "P", "C")
     * @param fromTime
     *            effective start timestamp in epoch ms
     * @param toTime
     *            effective end timestamp in epoch ms
     * @param queryTimeMs
     *            server-side query execution time in ms
     * @param chunksScanned
     *            number of candidate chunks decompressed
     * @param totalRowsMatched
     *            total matching observations across all groups
     * @param rows
     *            aggregated rows sorted by count descending
     */
    public record SummaryTableResponse(
        String groupBy,
        String recordType,
        long fromTime,
        long toTime,
        long queryTimeMs,
        int chunksScanned,
        long totalRowsMatched,
        List<SummaryRow> rows
    )
    {
    }

    /**
     * Request payload for {@code POST /api/v1/inspect} (Forensic Outlier &amp; Failure Inspector).
     *
     * @param mode
     *            inspection mode ("SLOWEST_REQUESTS", "FAILED_REQUESTS", or "FAILED_TRANSACTIONS")
     * @param limit
     *            maximum number of records to return (default 100, max 500)
     * @param fromTime
     *            inclusive start timestamp in epoch ms
     * @param toTime
     *            inclusive end timestamp in epoch ms
     * @param timers
     *            optional list of exact timer names to include
     * @param timerQuery
     *            optional case-insensitive substring or regex pattern to filter timer names
     * @param agents
     *            optional list of agent names to include
     * @param scenarios
     *            optional list of test scenario names to include
     * @param usedIps
     *            optional list of server IP addresses to include
     * @param httpMethods
     *            optional list of HTTP methods to include
     * @param contentTypes
     *            optional list of MIME content types to include
     * @param minStatusCode
     *            optional minimum HTTP status code (inclusive)
     * @param maxStatusCode
     *            optional maximum HTTP status code (inclusive)
     * @param failedOnly
     *            when {@code true}, only include failed records
     * @param minRunTime
     *            optional minimum runtime threshold in ms
     */
    public record InspectQuerySpec(
        String mode,
        int limit,
        long fromTime,
        long toTime,
        List<String> timers,
        String timerQuery,
        List<String> agents,
        List<String> scenarios,
        List<String> usedIps,
        List<String> httpMethods,
        List<String> contentTypes,
        Integer minStatusCode,
        Integer maxStatusCode,
        Boolean failedOnly,
        Integer minRunTime
    )
    {
    }

    /**
     * Individual request or transaction diagnostic record returned by {@code POST /api/v1/inspect}.
     *
     * @param time
     *            record start timestamp in epoch ms
     * @param recordType
     *            "R" for HTTP Request or "T" for Transaction
     * @param timerName
     *            merged timer name
     * @param rawName
     *            original pre-merge request timer name (e.g. "OpenHomepage.1.4"), if available
     * @param agent
     *            agent name
     * @param scenario
     *            test case / scenario name
     * @param userNumber
     *            virtual user thread sequence number (e.g. "0", "14")
     * @param runTime
     *            total execution duration in ms
     * @param statusCode
     *            HTTP response status code (for requests)
     * @param failed
     *            whether the record was marked as failed
     * @param httpMethod
     *            HTTP request method (for requests)
     * @param contentType
     *            MIME content type (for requests)
     * @param usedIp
     *            remote server IP address (for requests)
     * @param bytesSent
     *            uplink bytes sent (for requests)
     * @param bytesReceived
     *            downlink bytes received (for requests)
     * @param dnsTime
     *            DNS lookup time in ms
     * @param connectTime
     *            TCP/TLS connect time in ms
     * @param sendTime
     *            request send time in ms
     * @param serverBusyTime
     *            server wait / busy time in ms
     * @param receiveTime
     *            response receive time in ms
     * @param ttfb
     *            time-to-first-byte in ms
     * @param ttlb
     *            time-to-last-byte in ms
     * @param url
     *            exact unmerged URL from the Cold Forensic Tail Block, or representative sample URL fallback
     * @param forensicExact
     *            {@code true} if {@code url}/{@code requestId}/{@code formData} came from the Cold Forensic Tail Block
     *            for this exact row; {@code false} if {@code url} is a representative timer sample URL
     * @param requestId
     *            request correlation ID from the Cold Forensic Tail Block
     * @param responseId
     *            response correlation ID from the Cold Forensic Tail Block
     * @param formData
     *            HTTP POST/PUT form data payload from the Cold Forensic Tail Block
     * @param failedActionName
     *            name of the action where a transaction failed (for transactions)
     * @param stackTrace
     *            exception message and stack trace (for failed transactions)
     * @param directoryName
     *            Result Browser dump directory name (for transactions)
     * @param dumpUrl
     *            relative URL to open the local Result Browser dump ({@code /dumps/.../index.html}), or {@code null}
     */
    public record ForensicRecord(
        long time,
        String recordType,
        String timerName,
        String rawName,
        String agent,
        String scenario,
        String userNumber,
        int runTime,
        int statusCode,
        boolean failed,
        String httpMethod,
        String contentType,
        String usedIp,
        int bytesSent,
        int bytesReceived,
        int dnsTime,
        int connectTime,
        int sendTime,
        int serverBusyTime,
        int receiveTime,
        int ttfb,
        int ttlb,
        String url,
        boolean forensicExact,
        String requestId,
        String responseId,
        String formData,
        String failedActionName,
        String stackTrace,
        String directoryName,
        String dumpUrl
    )
    {
    }

    /**
     * Response payload for {@code POST /api/v1/inspect}.
     *
     * @param mode
     *            evaluated inspection mode
     * @param queryTimeMs
     *            server-side query execution time in ms
     * @param chunksScanned
     *            number of candidate chunks inspected after Zone Map pruning
     * @param totalMatchingCount
     *            total number of rows matching the inspection criteria before top-K truncation
     * @param records
     *            top-K forensic records
     */
    public record InspectResponse(
        String mode,
        long queryTimeMs,
        int chunksScanned,
        int totalMatchingCount,
        List<ForensicRecord> records
    )
    {
    }

    /**
     * Query specification for {@code GET /api/v1/session} (Virtual User Session Reconstruction).
     *
     * @param agent
     *            load agent name
     * @param scenario
     *            test case / scenario name
     * @param userNumber
     *            virtual user thread number (e.g. "0", "14")
     * @param targetTime
     *            timestamp (epoch ms) of the clicked request/transaction/event to anchor the session iteration
     * @param paddingMs
     *            fallback window padding (ms) on each side of {@code targetTime} if no enclosing transaction exists
     */
    public record SessionQuerySpec(
        String agent,
        String scenario,
        String userNumber,
        long targetTime,
        long paddingMs
    )
    {
    }

    /**
     * Node in the reconstructed Virtual User Session iteration tree ({@link SessionResponse}).
     *
     * @param typeCode
     *            record type ("T" Transaction, "A" Action, "R" Request, "P" PageLoad, "C" Custom, "E" Event)
     * @param time
     *            start timestamp in epoch ms
     * @param runTime
     *            duration in ms (0 for instant events)
     * @param endTime
     *            end timestamp ({@code time + runTime})
     * @param name
     *            timer or event name
     * @param rawName
     *            pre-merge raw request timer name, if available
     * @param failed
     *            whether this operation failed
     * @param statusCode
     *            HTTP status code (for requests)
     * @param httpMethod
     *            HTTP method (for requests)
     * @param contentType
     *            MIME content type (for requests)
     * @param usedIp
     *            remote server IP address (for requests)
     * @param bytesSent
     *            bytes sent (for requests)
     * @param bytesReceived
     *            bytes received (for requests)
     * @param dnsTime
     *            DNS lookup time in ms
     * @param connectTime
     *            TCP/TLS connect time in ms
     * @param sendTime
     *            request send time in ms
     * @param serverBusyTime
     *            server wait / busy time in ms
     * @param receiveTime
     *            response receive time in ms
     * @param ttfb
     *            time-to-first-byte in ms
     * @param ttlb
     *            time-to-last-byte in ms
     * @param url
     *            request URL (exact from forensic tail or representative sample URL)
     * @param forensicExact
     *            {@code true} if forensic details came from the Cold Forensic Tail Block for this exact row
     * @param requestId
     *            request correlation ID
     * @param responseId
     *            response correlation ID
     * @param formData
     *            HTTP form data payload
     * @param message
     *            event message (for "E") or failure stack trace (for "T")
     * @param failedActionName
     *            failed action name (for "T")
     * @param directoryName
     *            Result Browser dump directory name (for "T")
     * @param dumpUrl
     *            relative URL to the Result Browser HTML dump ({@code /dumps/.../index.html}), if present on disk
     * @param children
     *            nested child operations (e.g. requests/page-loads/events executing within an Action)
     */
    public record SessionNode(
        String typeCode,
        long time,
        int runTime,
        long endTime,
        String name,
        String rawName,
        boolean failed,
        int statusCode,
        String httpMethod,
        String contentType,
        String usedIp,
        int bytesSent,
        int bytesReceived,
        int dnsTime,
        int connectTime,
        int sendTime,
        int serverBusyTime,
        int receiveTime,
        int ttfb,
        int ttlb,
        String url,
        boolean forensicExact,
        String requestId,
        String responseId,
        String formData,
        String message,
        String failedActionName,
        String directoryName,
        String dumpUrl,
        List<SessionNode> children
    )
    {
    }

    /**
     * Response payload for {@code GET /api/v1/session}.
     *
     * @param agent
     *            load agent name
     * @param scenario
     *            test case / scenario name
     * @param userNumber
     *            virtual user thread number
     * @param targetTime
     *            requested anchor timestamp in epoch ms
     * @param windowStart
     *            resolved iteration start timestamp in epoch ms
     * @param windowEnd
     *            resolved iteration end timestamp in epoch ms
     * @param prevTransactionTime
     *            start timestamp of the immediately preceding transaction iteration for this virtual user, or {@code null}
     * @param nextTransactionTime
     *            start timestamp of the immediately succeeding transaction iteration for this virtual user, or {@code null}
     * @param queryTimeMs
     *            server-side query execution time in ms
     * @param transaction
     *            enclosing Transaction record for this iteration, or {@code null} if none enclosed the target time
     * @param items
     *            top-level chronological timeline items (Actions containing nested Requests/Events, plus any top-level items)
     */
    public record SessionResponse(
        String agent,
        String scenario,
        String userNumber,
        long targetTime,
        long windowStart,
        long windowEnd,
        Long prevTransactionTime,
        Long nextTransactionTime,
        long queryTimeMs,
        SessionNode transaction,
        List<SessionNode> items
    )
    {
    }

    /**
     * Combined request payload for {@code POST /api/v1/dashboard}, allowing the interactive Explorer UI
     * to compute both the time-series charts and the multi-dimensional summary table in a single HTTP
     * round-trip and a single columnar chunk scan pass.
     */
    public record DashboardQuerySpec(
        String recordType,
        long fromTime,
        long toTime,
        int bucketWidthMs,
        List<String> timers,
        String timerQuery,
        List<String> agents,
        List<String> scenarios,
        List<String> usedIps,
        List<String> httpMethods,
        List<String> contentTypes,
        Integer minStatusCode,
        Integer maxStatusCode,
        Boolean failedOnly,
        Integer minRunTime,
        String chartGroupBy,
        String summaryGroupBy
    )
    {
        /**
         * Returns the requested time-series group-by dimension (defaults to {@code "NONE"} when null).
         */
        public String timeseriesGroupBy()
        {
            return chartGroupBy;
        }
    }

    /**
     * Combined response payload for {@code POST /api/v1/dashboard}.
     *
     * @param queryTimeMs
     *            total wall-clock execution time in milliseconds for the combined single-pass scan
     * @param timeSeries
     *            bucketed time-series response
     * @param summaryTable
     *            multi-dimensional summary table response
     */
    public record DashboardResponse(
        long queryTimeMs,
        TimeSeriesResponse timeSeries,
        SummaryTableResponse summaryTable
    )
    {
    }
}
