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
package com.xceptance.xlt.report.benchmark;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.sun.management.ThreadMXBean;
import com.xceptance.common.io.XltBufferedByteLineReader;
import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.util.ByteSlice;
import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.common.util.CsvByteLineDecoder;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.DataRecordFactory;

/**
 * High-precision benchmarking and profiling suite for the zero-allocation byte ingestion pipeline.
 * Measures throughput (lines/s, MB/s, records/s), thread allocation rates (bytes/record),
 * and memory behavior under real load test data.
 *
 * @author Rene Schwietzke
 */
public class BytePipelineBenchmark
{
    private static final ThreadMXBean THREAD_MX = (ThreadMXBean) ManagementFactory.getThreadMXBean();

    // Representative load test records from real e-commerce data
    private static final byte[] SAMPLE_REQUEST_LINE = ("R,Homepage_ABC_CA.1,1636300948347,155,false,931,31031,200," +
        "https://perf.country.anyhost.com/on/mciahaware.store/Sites-thomasCA-Site/en_CA/Home-GetCustomerCartInfo," +
        "text/html,11,33,0,119,35,154,QL6gx2rdl4wWYFa,GET,application/x-www-form-urlencoded,,0,192.168.1.1,6aa7c93f8b322b24-ORD,10.0.0.1")
        .getBytes(StandardCharsets.UTF_8);

    private static final byte[] SAMPLE_QUOTED_REQUEST_LINE = ("R,\"Homepage, Quoted Item.1\",1636300948347,155,false,931,31031,200," +
        "\"https://perf.country.anyhost.com/search?q=shoes%2C+boots&cat=all\"," +
        "\"text/html; charset=utf-8\",11,33,0,119,35,154,QL6gx2rdl4wWYFa,GET,,,\"0\",192.168.1.1,6aa7c93f8b322b24-ORD,10.0.0.1")
        .getBytes(StandardCharsets.UTF_8);

    private static final byte[] SAMPLE_ACTION_LINE = "A,Homepage_ABC_CA,1636300948347,463,false"
        .getBytes(StandardCharsets.UTF_8);

    public static void main(final String[] args) throws Exception
    {
        System.out.println("================================================================================");
        System.out.println("        XLT ZERO-ALLOCATION BYTE PIPELINE BENCHMARK & PROFILER");
        System.out.println("================================================================================");
        System.out.println("JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
        System.out.println("OS:  " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " (" + Runtime.getRuntime().availableProcessors() + " cores)");
        System.out.println("Thread allocated memory tracking supported: " + THREAD_MX.isThreadAllocatedMemorySupported());
        System.out.println();

        // 1. Component Micro-Benchmarks
        benchmarkCsvByteLineDecoder();
        benchmarkParseNumbers();
        benchmarkByteSliceProbeCache();

        // 2. Reader Streaming Benchmark (Repository timers.csv.gz)
        final File repoTimersGz = new File("src/test/resources/timers.csv.gz");
        if (repoTimersGz.isFile())
        {
            benchmarkLineReader(repoTimersGz);
            benchmarkEndToEndPipelineOnSingleFile(repoTimersGz);
        }
        else
        {
            System.err.println("Warning: src/test/resources/timers.csv.gz not found!");
        }

        // 3. Real Large-Scale Test Data (Ariat dataset if present)
        File ariatDir = new File("/home/rschwietzke/projects/loadtest/test-results/xlt-result-ariat-lt-2025-315-20251119-165727");
        if (args.length > 0)
        {
            ariatDir = new File(args[0]);
        }

        if (ariatDir.isDirectory())
        {
            benchmarkAriatRealDataset(ariatDir, 100); // 100 timer files (~175k records)
            benchmarkAriatRealDatasetMultiThreaded(ariatDir, 500, 8); // 500 files across 8 worker threads (~880k records)
        }
        else
        {
            System.out.println("Ariat directory not found at " + ariatDir + ", skipping multi-file load test dataset.");
        }

        System.out.println("================================================================================");
        System.out.println("BENCHMARK COMPLETED SUCCESSFULLY");
        System.out.println("================================================================================");
    }

    // -----------------------------------------------------------------------------------------------------
    // 1. CsvByteLineDecoder Benchmark
    // -----------------------------------------------------------------------------------------------------

    private static void benchmarkCsvByteLineDecoder()
    {
        System.out.println("--- 1. CsvByteLineDecoder & CsvByteColumns Micro-Benchmark ---");

        final CsvByteColumns columns = new CsvByteColumns(50);

        // Warm up JIT
        for (int i = 0; i < 200_000; i++)
        {
            columns.clear();
            CsvByteLineDecoder.parse(columns, SAMPLE_REQUEST_LINE);
            columns.clear();
            CsvByteLineDecoder.parse(columns, SAMPLE_QUOTED_REQUEST_LINE);
            columns.clear();
            CsvByteLineDecoder.parse(columns, SAMPLE_ACTION_LINE);
        }

        final int iterations = 5_000_000;

        // Measure unquoted Request line
        columns.clear();
        long startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            columns.clear();
            CsvByteLineDecoder.parse(columns, SAMPLE_REQUEST_LINE);
        }
        long durationNs = System.nanoTime() - startTime;
        long allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;

        double opsPerSec = (iterations * 1e9) / durationNs;
        double nsPerOp = (double) durationNs / iterations;
        double mbPerSec = ((double) iterations * SAMPLE_REQUEST_LINE.length / (1024 * 1024)) / (durationNs / 1e9);
        double bytesPerRecord = (double) allocatedBytes / iterations;

        System.out.printf("  Unquoted Request Line (24 cols, %d bytes):%n", SAMPLE_REQUEST_LINE.length);
        System.out.printf("    Throughput:         %,12.0f lines/s (%.1f MB/s)%n", opsPerSec, mbPerSec);
        System.out.printf("    Latency:            %8.2f ns/line%n", nsPerOp);
        System.out.printf("    Total Allocated:    %d bytes across %,d parses%n", allocatedBytes, iterations);
        System.out.printf("    Allocation Rate:    %.4f bytes/record %s%n", bytesPerRecord, (allocatedBytes == 0 ? "(ZERO ALLOCATION verified!)" : ""));

        // Measure quoted Request line
        columns.clear();
        startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            columns.clear();
            CsvByteLineDecoder.parse(columns, SAMPLE_QUOTED_REQUEST_LINE);
        }
        durationNs = System.nanoTime() - startTime;
        allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;

        opsPerSec = (iterations * 1e9) / durationNs;
        nsPerOp = (double) durationNs / iterations;
        mbPerSec = ((double) iterations * SAMPLE_QUOTED_REQUEST_LINE.length / (1024 * 1024)) / (durationNs / 1e9);
        bytesPerRecord = (double) allocatedBytes / iterations;

        System.out.printf("  Quoted Request Line (24 cols, %d bytes, RFC 4180 quotes):%n", SAMPLE_QUOTED_REQUEST_LINE.length);
        System.out.printf("    Throughput:         %,12.0f lines/s (%.1f MB/s)%n", opsPerSec, mbPerSec);
        System.out.printf("    Latency:            %8.2f ns/line%n", nsPerOp);
        System.out.printf("    Total Allocated:    %d bytes across %,d parses%n", allocatedBytes, iterations);
        System.out.printf("    Allocation Rate:    %.4f bytes/record %s%n", bytesPerRecord, (allocatedBytes == 0 ? "(ZERO ALLOCATION verified!)" : ""));

        // Measure short Action line
        columns.clear();
        startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            columns.clear();
            CsvByteLineDecoder.parse(columns, SAMPLE_ACTION_LINE);
        }
        durationNs = System.nanoTime() - startTime;
        allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;

        opsPerSec = (iterations * 1e9) / durationNs;
        nsPerOp = (double) durationNs / iterations;
        mbPerSec = ((double) iterations * SAMPLE_ACTION_LINE.length / (1024 * 1024)) / (durationNs / 1e9);
        bytesPerRecord = (double) allocatedBytes / iterations;

        System.out.printf("  Action Line (5 cols, %d bytes):%n", SAMPLE_ACTION_LINE.length);
        System.out.printf("    Throughput:         %,12.0f lines/s (%.1f MB/s)%n", opsPerSec, mbPerSec);
        System.out.printf("    Latency:            %8.2f ns/line%n", nsPerOp);
        System.out.printf("    Allocation Rate:    %.4f bytes/record %s%n", bytesPerRecord, (allocatedBytes == 0 ? "(ZERO ALLOCATION verified!)" : ""));
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------------------
    // 2. ParseNumbers Micro-Benchmark
    // -----------------------------------------------------------------------------------------------------

    private static void benchmarkParseNumbers()
    {
        System.out.println("--- 2. ParseNumbers Direct Byte Slice Micro-Benchmark ---");

        final byte[] intBytes = "12345678".getBytes(StandardCharsets.UTF_8);
        final byte[] longBytes = "1636300948347".getBytes(StandardCharsets.UTF_8);
        final byte[] doubleBytes = "12345.678".getBytes(StandardCharsets.UTF_8);
        final byte[] boolBytes = "false".getBytes(StandardCharsets.UTF_8);

        final int iterations = 10_000_000;

        // Warm up
        long sink = 0;
        for (int i = 0; i < 200_000; i++)
        {
            sink += ParseNumbers.parseInt(intBytes, 0, intBytes.length);
            sink += ParseNumbers.parseLong(longBytes, 0, longBytes.length);
            sink += (long) ParseNumbers.parseDouble(doubleBytes, 0, doubleBytes.length);
            sink += ParseNumbers.parseBoolean(boolBytes, 0, boolBytes.length) ? 1 : 0;
        }

        // parseInt
        long startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            sink += ParseNumbers.parseInt(intBytes, 0, intBytes.length);
        }
        long durationNs = System.nanoTime() - startTime;
        long allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;
        System.out.printf("  parseInt(\"12345678\"):    %,12.0f ops/s | %6.2f ns/op | %d bytes allocated%n",
                          (iterations * 1e9) / durationNs, (double) durationNs / iterations, allocatedBytes);

        // parseLong
        startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            sink += ParseNumbers.parseLong(longBytes, 0, longBytes.length);
        }
        durationNs = System.nanoTime() - startTime;
        allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;
        System.out.printf("  parseLong(\"1636300948347\"): %,12.0f ops/s | %6.2f ns/op | %d bytes allocated%n",
                          (iterations * 1e9) / durationNs, (double) durationNs / iterations, allocatedBytes);

        // parseDouble
        startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            sink += (long) ParseNumbers.parseDouble(doubleBytes, 0, doubleBytes.length);
        }
        durationNs = System.nanoTime() - startTime;
        allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;
        System.out.printf("  parseDouble(\"12345.678\"): %,12.0f ops/s | %6.2f ns/op | %d bytes allocated%n",
                          (iterations * 1e9) / durationNs, (double) durationNs / iterations, allocatedBytes);

        // parseBoolean
        startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++)
        {
            sink += ParseNumbers.parseBoolean(boolBytes, 0, boolBytes.length) ? 1 : 0;
        }
        durationNs = System.nanoTime() - startTime;
        allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;
        System.out.printf("  parseBoolean(\"false\"):  %,12.0f ops/s | %6.2f ns/op | %d bytes allocated%n",
                          (iterations * 1e9) / durationNs, (double) durationNs / iterations, allocatedBytes);

        if (sink == 42) System.out.println(sink); // prevent dead-code elimination
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------------------
    // 3. ByteSlice Zero-Allocation Cache Probe
    // -----------------------------------------------------------------------------------------------------

    private static void benchmarkByteSliceProbeCache()
    {
        System.out.println("--- 3. ByteSlice Probe Cache Lookup (Condition / MergeRule pattern) ---");

        final Map<ByteSlice, Object> cache = new HashMap<>();
        final String[] urls = {
            "https://perf.country.anyhost.com/on/mciahaware.store/Sites-thomasCA-Site/en_CA/Home-GetCustomerCartInfo",
            "https://perf.country.anyhost.com/en/kids-storage/kids-storage/921619019",
            "https://perf.country.anyhost.com/search?q=shoes",
            "https://perf.country.anyhost.com/cart",
            "https://perf.country.anyhost.com/checkout"
        };

        final byte[][] urlBytes = new byte[urls.length][];
        for (int i = 0; i < urls.length; i++)
        {
            urlBytes[i] = urls[i].getBytes(StandardCharsets.UTF_8);
            cache.put(ByteSlice.copyOf(urlBytes[i], 0, urlBytes[i].length), new Object());
        }

        final ByteSlice probeSlice = new ByteSlice();

        // Warm up
        for (int i = 0; i < 200_000; i++)
        {
            final byte[] b = urlBytes[i % urlBytes.length];
            probeSlice.set(b, 0, b.length);
            cache.get(probeSlice);
        }

        final int iterations = 10_000_000;
        final long startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        final long startTime = System.nanoTime();

        int hits = 0;
        for (int i = 0; i < iterations; i++)
        {
            final byte[] b = urlBytes[i % urlBytes.length];
            probeSlice.set(b, 0, b.length);
            if (cache.get(probeSlice) != null)
            {
                hits++;
            }
        }

        final long durationNs = System.nanoTime() - startTime;
        final long allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;

        final double lookupsPerSec = (iterations * 1e9) / durationNs;
        final double nsPerLookup = (double) durationNs / iterations;
        final double bytesPerLookup = (double) allocatedBytes / iterations;

        System.out.printf("  Probe Lookups:      %,d lookups (100%% hits)%n", iterations);
        System.out.printf("  Throughput:         %,12.0f lookups/s%n", lookupsPerSec);
        System.out.printf("  Latency:            %8.2f ns/lookup%n", nsPerLookup);
        System.out.printf("  Total Allocated:    %d bytes across %,d lookups%n", allocatedBytes, iterations);
        System.out.printf("  Allocation Rate:    %.4f bytes/lookup %s%n", bytesPerLookup, (allocatedBytes == 0 ? "(ZERO ALLOCATION verified!)" : ""));
        System.out.println();
    }

    // -----------------------------------------------------------------------------------------------------
    // 4. XltBufferedByteLineReader Streaming Benchmark
    // -----------------------------------------------------------------------------------------------------

    private static void benchmarkLineReader(final File gzFile) throws IOException
    {
        System.out.println("--- 4. XltBufferedByteLineReader Streaming Throughput (from disk) ---");

        // First, decompress into memory to also measure raw uncompressed reader throughput without gzip decompressor overhead
        final byte[] uncompressedBytes;
        try (final InputStream in = new GZIPInputStream(new FileInputStream(gzFile)))
        {
            uncompressedBytes = in.readAllBytes();
        }

        System.out.printf("  Dataset: %s (%,d compressed bytes, %,d uncompressed bytes)%n",
                          gzFile.getName(), gzFile.length(), uncompressedBytes.length);

        // Benchmark uncompressed memory stream (pure reader + line splitter speed)
        long totalLines = 0;
        long totalBytesRead = 0;
        final int runs = 100;

        // Warm up
        for (int r = 0; r < 20; r++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new ByteArrayInputStream(uncompressedBytes)))
            {
                while (reader.readLine() != null) {}
            }
        }

        final long startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        final long startTime = System.nanoTime();

        for (int r = 0; r < runs; r++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new ByteArrayInputStream(uncompressedBytes)))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    totalLines++;
                    totalBytesRead += line.length;
                }
            }
        }

        final long durationNs = System.nanoTime() - startTime;
        final long allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;

        final double linesPerSec = (totalLines * 1e9) / durationNs;
        final double mbPerSec = ((double) totalBytesRead / (1024 * 1024)) / (durationNs / 1e9);
        final double allocPerLine = (double) allocatedBytes / totalLines;
        final double avgLineLen = (double) totalBytesRead / totalLines;

        System.out.println("  Uncompressed Stream (Reader Line-Splitting):");
        System.out.printf("    Throughput:       %,12.0f lines/s (%.1f MB/s)%n", linesPerSec, mbPerSec);
        System.out.printf("    Avg Line Length:  %8.1f bytes/line%n", avgLineLen);
        System.out.printf("    Alloc Per Line:   %8.1f bytes/line (line byte[] + object overhead)%n", allocPerLine);

        // Benchmark real GZIP file reading
        long gzLines = 0;
        final long gzStart = System.nanoTime();
        final int gzRuns = 20;
        for (int r = 0; r < gzRuns; r++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new GZIPInputStream(new FileInputStream(gzFile), 16384)))
            {
                while (reader.readLine() != null)
                {
                    gzLines++;
                }
            }
        }
        final long gzDurationNs = System.nanoTime() - gzStart;
        final double gzLinesPerSec = (gzLines * 1e9) / gzDurationNs;
        final double gzMbPerSec = ((double) gzLines * avgLineLen / (1024 * 1024)) / (gzDurationNs / 1e9);

        System.out.println("  GZIP Compressed Disk Stream (GZIPInputStream + Reader):");
        System.out.printf("    Throughput:       %,12.0f lines/s (%.1f MB/s decompressed)%n", gzLinesPerSec, gzMbPerSec);
        System.out.println();
    }

    private static DataRecordFactory createFactory()
    {
        final Map<String, Class<? extends Data>> map = new HashMap<>();
        map.put("T", com.xceptance.xlt.api.engine.TransactionData.class);
        map.put("A", com.xceptance.xlt.api.engine.ActionData.class);
        map.put("R", com.xceptance.xlt.api.engine.RequestData.class);
        map.put("C", com.xceptance.xlt.api.engine.CustomData.class);
        map.put("E", com.xceptance.xlt.api.engine.EventData.class);
        map.put("J", com.xceptance.xlt.agent.JvmResourceUsageData.class);
        map.put("V", com.xceptance.xlt.api.engine.CustomValue.class);
        map.put("P", com.xceptance.xlt.api.engine.PageLoadTimingData.class);
        map.put("W", com.xceptance.xlt.api.engine.WebVitalData.class);
        return new DataRecordFactory(map);
    }

    // -----------------------------------------------------------------------------------------------------
    // 5. End-to-End Pipeline Ingestion Benchmark
    // -----------------------------------------------------------------------------------------------------

    private static void benchmarkEndToEndPipelineOnSingleFile(final File gzFile) throws Exception
    {
        System.out.println("--- 5. End-to-End Pipeline Ingestion (Reader -> CsvDecoder -> Data Objects) ---");

        final byte[] uncompressedBytes;
        try (final InputStream in = new GZIPInputStream(new FileInputStream(gzFile)))
        {
            uncompressedBytes = in.readAllBytes();
        }

        final DataRecordFactory factory = createFactory();
        final CsvByteColumns columns = new CsvByteColumns(50);

        // Pre-warm JIT
        for (int w = 0; w < 30; w++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new ByteArrayInputStream(uncompressedBytes)))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.length == 0) continue;
                    columns.clear();
                    CsvByteLineDecoder.parse(columns, line);
                    final Data d = factory.createStatistics(line[0]);
                    d.setBaseValues(columns);
                    d.setRemainingValues(columns);
                }
            }
        }

        final int runs = 100;
        long totalRecords = 0;
        long reqCount = 0;
        long actionCount = 0;
        long customCount = 0;

        final long startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        final long startTime = System.nanoTime();

        for (int r = 0; r < runs; r++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new ByteArrayInputStream(uncompressedBytes)))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.length == 0) continue;
                    columns.clear();
                    CsvByteLineDecoder.parse(columns, line);

                    final Data d = factory.createStatistics(line[0]);
                    d.setBaseValues(columns);
                    d.setRemainingValues(columns);

                    totalRecords++;
                    if (d instanceof RequestData)
                    {
                        reqCount++;
                    }
                    else if (d.getTypeCode() == 'A')
                    {
                        actionCount++;
                    }
                    else
                    {
                        customCount++;
                    }
                }
            }
        }

        final long durationNs = System.nanoTime() - startTime;
        final long allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;

        final double recordsPerSec = (totalRecords * 1e9) / durationNs;
        final double mbPerSec = ((double) uncompressedBytes.length * runs / (1024 * 1024)) / (durationNs / 1e9);
        final double bytesPerRecord = (double) allocatedBytes / totalRecords;

        System.out.println("  [Mode A] Zero-Allocation Byte Pipeline (Lazy URL Slice):");
        System.out.printf("    Records Processed:    %,d (%,d Requests, %,d Actions, %,d Custom)%n",
                          totalRecords, reqCount, actionCount, customCount);
        System.out.printf("    Throughput:           %,12.0f records/s (%.1f MB/s)%n", recordsPerSec, mbPerSec);
        System.out.printf("    Net Allocation Rate:  %,8.1f bytes/record%n", bytesPerRecord);
        System.out.printf("    Total Allocated:      %,d bytes (%.2f MB)%n", allocatedBytes, allocatedBytes / (1024.0 * 1024.0));

        // Mode B: Legacy Eager URL String materialization
        long eagerRecords = 0;
        final long eagerStartBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        final long eagerStartTime = System.nanoTime();
        for (int r = 0; r < runs; r++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new ByteArrayInputStream(uncompressedBytes)))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.length == 0) continue;
                    columns.clear();
                    CsvByteLineDecoder.parse(columns, line);

                    final Data d = factory.createStatistics(line[0]);
                    d.setBaseValues(columns);
                    d.setRemainingValues(columns);

                    if (d instanceof RequestData)
                    {
                        ((RequestData) d).getUrl(); // Force eager String allocation (legacy behavior)
                    }
                    eagerRecords++;
                }
            }
        }
        final long eagerDurationNs = System.nanoTime() - eagerStartTime;
        final long eagerAllocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - eagerStartBytes;
        final double eagerRecordsPerSec = (eagerRecords * 1e9) / eagerDurationNs;
        final double eagerMbPerSec = ((double) uncompressedBytes.length * runs / (1024 * 1024)) / (eagerDurationNs / 1e9);
        final double eagerBytesPerRecord = (double) eagerAllocatedBytes / eagerRecords;

        System.out.println("  [Mode B] Legacy Simulation (Eager URL String Materialization):");
        System.out.printf("    Throughput:           %,12.0f records/s (%.1f MB/s)%n", eagerRecordsPerSec, eagerMbPerSec);
        System.out.printf("    Net Allocation Rate:  %,8.1f bytes/record%n", eagerBytesPerRecord);
        System.out.printf("    Total Allocated:      %,d bytes (%.2f MB)%n", eagerAllocatedBytes, eagerAllocatedBytes / (1024.0 * 1024.0));
        System.out.printf("    Memory Overhead:      +%.1f bytes/record (+%.1f%% extra memory allocated)%n",
                          eagerBytesPerRecord - bytesPerRecord,
                          ((eagerBytesPerRecord - bytesPerRecord) / bytesPerRecord) * 100.0);

        // Mode C: MergeRuleProcessor applied directly on byte slices
        final List<com.xceptance.xlt.report.mergerules.MergeRule> rules = List.of(
            createUrlRule(1, "{n} JS", "\\.js$"),
            createUrlRule(2, "{n} Images", "\\.(gif|png|jpg|ico|webp)$"),
            createUrlRule(3, "{n} CSS", "\\.css$")
        );
        final com.xceptance.xlt.report.mergerules.MergeRuleProcessor mrProcessor =
            new com.xceptance.xlt.report.mergerules.MergeRuleProcessor(rules, true);

        long mrRecords = 0;
        final long mrStartBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        final long mrStartTime = System.nanoTime();
        for (int r = 0; r < runs; r++)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new ByteArrayInputStream(uncompressedBytes)))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.length == 0) continue;
                    columns.clear();
                    CsvByteLineDecoder.parse(columns, line);

                    final Data d = factory.createStatistics(line[0]);
                    d.setBaseValues(columns);
                    d.setRemainingValues(columns);

                    if (d instanceof RequestData)
                    {
                        mrProcessor.postprocess((RequestData) d);
                    }
                    mrRecords++;
                }
            }
        }
        final long mrDurationNs = System.nanoTime() - mrStartTime;
        final long mrAllocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - mrStartBytes;
        final double mrRecordsPerSec = (mrRecords * 1e9) / mrDurationNs;
        final double mrMbPerSec = ((double) uncompressedBytes.length * runs / (1024 * 1024)) / (mrDurationNs / 1e9);
        final double mrBytesPerRecord = (double) mrAllocatedBytes / mrRecords;

        System.out.println("  [Mode C] Byte Pipeline + MergeRuleProcessor (URL Regex Cache Probing via ByteSlice):");
        System.out.printf("    Throughput:           %,12.0f records/s (%.1f MB/s)%n", mrRecordsPerSec, mrMbPerSec);
        System.out.printf("    Net Allocation Rate:  %,8.1f bytes/record%n", mrBytesPerRecord);
        System.out.printf("    Total Allocated:      %,d bytes (%.2f MB)%n", mrAllocatedBytes, mrAllocatedBytes / (1024.0 * 1024.0));
        System.out.println();
    }

    private static com.xceptance.xlt.report.mergerules.MergeRule createUrlRule(final int id, final String newName, final String urlPattern)
    {
        try
        {
            return new com.xceptance.xlt.report.mergerules.MergeRule(
                id,
                new com.xceptance.xlt.report.mergerules.MergeRule.NewName(newName),
                new com.xceptance.xlt.report.mergerules.MergeRule.NamePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.UrlPattern(urlPattern),
                new com.xceptance.xlt.report.mergerules.MergeRule.ContentTypePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.StatusCodePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.AgentNamePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.TransactionNamePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.HttpMethodPattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.RunTimeRanges(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.StopOnMatch(false),
                new com.xceptance.xlt.report.mergerules.MergeRule.NameExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.UrlExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.ContentTypeExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.StatusCodeExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.AgentNameExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.TransactionNameExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.HttpMethodExcludePattern(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnMatchAtId(id),
                new com.xceptance.xlt.report.mergerules.MergeRule.ContinueOnNoMatchAtId(id),
                new com.xceptance.xlt.report.mergerules.MergeRule.DropOnMatch(false),
                new com.xceptance.xlt.report.mergerules.MergeRule.UrlText(""),
                new com.xceptance.xlt.report.mergerules.MergeRule.UrlTextExclude("")
            );
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------------------------------------
    // 6. Large-Scale Real Load Test Ingestion (Ariat Dataset)
    // -----------------------------------------------------------------------------------------------------

    private static void benchmarkAriatRealDataset(final File dir, final int maxFiles) throws Exception
    {
        System.out.println("--- 6. Real Production Dataset Benchmark (Ariat Single-Threaded) ---");

        final List<File> files = collectTimerFiles(dir, maxFiles);
        System.out.printf("  Found %,d timers.csv.gz files in %s%n", files.size(), dir.getName());

        final DataRecordFactory factory = createFactory();
        final CsvByteColumns columns = new CsvByteColumns(50);

        long totalRecords = 0;
        long totalRawBytes = 0;

        final long startGcTime = getGcTime();
        final long startBytes = THREAD_MX.getCurrentThreadAllocatedBytes();
        final long startTime = System.nanoTime();

        for (final File file : files)
        {
            try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new GZIPInputStream(new FileInputStream(file), 16384)))
            {
                byte[] line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.length == 0) continue;
                    totalRawBytes += line.length;

                    columns.clear();
                    CsvByteLineDecoder.parse(columns, line);

                    final Data d = factory.createStatistics(line[0]);
                    d.setBaseValues(columns);
                    d.setRemainingValues(columns);

                    totalRecords++;
                }
            }
        }

        final long durationNs = System.nanoTime() - startTime;
        final long allocatedBytes = THREAD_MX.getCurrentThreadAllocatedBytes() - startBytes;
        final long gcTimeMs = getGcTime() - startGcTime;

        final double recordsPerSec = (totalRecords * 1e9) / durationNs;
        final double mbPerSec = ((double) totalRawBytes / (1024 * 1024)) / (durationNs / 1e9);
        final double bytesPerRecord = (double) allocatedBytes / totalRecords;

        System.out.printf("  Records Ingested:        %,d records from %d files%n", totalRecords, files.size());
        System.out.printf("  Decompressed Volume:     %.2f MB%n", totalRawBytes / (1024.0 * 1024.0));
        System.out.printf("  Wall Clock Time:         %,.2f ms (%.2f s)%n", durationNs / 1e6, durationNs / 1e9);
        System.out.printf("  Ingestion Rate:          %,12.0f records/s (%.1f MB/s)%n", recordsPerSec, mbPerSec);
        System.out.printf("  Memory Allocated:        %,d bytes (%.2f MB)%n", allocatedBytes, allocatedBytes / (1024.0 * 1024.0));
        System.out.printf("  Allocation Per Record:   %,8.1f bytes/record%n", bytesPerRecord);
        System.out.printf("  GC Time Elapsed:         %,d ms%n", gcTimeMs);
        System.out.println();
    }

    private static void benchmarkAriatRealDatasetMultiThreaded(final File dir, final int maxFiles, final int numThreads) throws Exception
    {
        System.out.printf("--- 7. Real Production Dataset Benchmark (Ariat Multi-Threaded: %d Threads) ---%n", numThreads);

        final List<File> files = collectTimerFiles(dir, maxFiles);
        System.out.printf("  Processing %,d files concurrently across %d threads...%n", files.size(), numThreads);

        final ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        final List<Callable<WorkerResult>> tasks = new ArrayList<>();

        // Distribute files among tasks
        final int batchSize = (files.size() + numThreads - 1) / numThreads;
        for (int t = 0; t < numThreads; t++)
        {
            final int start = t * batchSize;
            final int end = Math.min(start + batchSize, files.size());
            if (start < end)
            {
                final List<File> subList = files.subList(start, end);
                tasks.add(() -> {
                    final CsvByteColumns columns = new CsvByteColumns(50);
                    final DataRecordFactory factory = createFactory();
                    long recs = 0;
                    long bytes = 0;
                    final long threadStartBytes = THREAD_MX.getCurrentThreadAllocatedBytes();

                    for (final File file : subList)
                    {
                        try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(new GZIPInputStream(new FileInputStream(file), 16384)))
                        {
                            byte[] line;
                            while ((line = reader.readLine()) != null)
                            {
                                if (line.length == 0) continue;
                                bytes += line.length;

                                columns.clear();
                                CsvByteLineDecoder.parse(columns, line);

                                final Data d = factory.createStatistics(line[0]);
                                d.setBaseValues(columns);
                                d.setRemainingValues(columns);

                                recs++;
                            }
                        }
                    }
                    final long threadAlloc = THREAD_MX.getCurrentThreadAllocatedBytes() - threadStartBytes;
                    return new WorkerResult(recs, bytes, threadAlloc);
                });
            }
        }

        final long startGcTime = getGcTime();
        final long startTime = System.nanoTime();

        final List<Future<WorkerResult>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        long totalRecords = 0;
        long totalRawBytes = 0;
        long totalAllocatedBytes = 0;

        for (final Future<WorkerResult> f : futures)
        {
            final WorkerResult r = f.get();
            totalRecords += r.records;
            totalRawBytes += r.rawBytes;
            totalAllocatedBytes += r.allocatedBytes;
        }

        final long durationNs = System.nanoTime() - startTime;
        final long gcTimeMs = getGcTime() - startGcTime;

        final double recordsPerSec = (totalRecords * 1e9) / durationNs;
        final double mbPerSec = ((double) totalRawBytes / (1024 * 1024)) / (durationNs / 1e9);
        final double bytesPerRecord = (double) totalAllocatedBytes / totalRecords;

        System.out.printf("  Total Records Ingested:  %,d records%n", totalRecords);
        System.out.printf("  Decompressed Volume:     %.2f MB%n", totalRawBytes / (1024.0 * 1024.0));
        System.out.printf("  Wall Clock Time:         %,.2f ms (%.2f s)%n", durationNs / 1e6, durationNs / 1e9);
        System.out.printf("  Aggregate Throughput:    %,12.0f records/s (%.1f MB/s)%n", recordsPerSec, mbPerSec);
        System.out.printf("  Total Memory Allocated:  %,d bytes (%.2f MB)%n", totalAllocatedBytes, totalAllocatedBytes / (1024.0 * 1024.0));
        System.out.printf("  Average Per Record:      %,8.1f bytes/record%n", bytesPerRecord);
        System.out.printf("  GC Time Elapsed:         %,d ms%n", gcTimeMs);
        System.out.println();
    }

    private record WorkerResult(long records, long rawBytes, long allocatedBytes) {}

    private static List<File> collectTimerFiles(final File dir, final int maxFiles)
    {
        final List<File> results = new ArrayList<>();
        collectRecursively(dir, results, maxFiles);
        return results;
    }

    private static void collectRecursively(final File file, final List<File> results, final int maxFiles)
    {
        if (results.size() >= maxFiles) return;
        if (file.isDirectory())
        {
            final File[] children = file.listFiles();
            if (children != null)
            {
                for (final File child : children)
                {
                    collectRecursively(child, results, maxFiles);
                    if (results.size() >= maxFiles) return;
                }
            }
        }
        else if (file.getName().equals("timers.csv.gz"))
        {
            results.add(file);
        }
    }

    private static long getGcTime()
    {
        long sum = 0;
        for (final GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans())
        {
            final long t = gc.getCollectionTime();
            if (t > 0) sum += t;
        }
        return sum;
    }
}
