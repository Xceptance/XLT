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
package com.xceptance.debug;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Standalone speed test for reading XLT result directories using pure Java NIO 
 * (FileChannel and ByteBuffer) for maximum uncompressed file reading throughput.
 */
public class SpeedTestReaderNio
{
    private static final AtomicLong totalLinesCounter = new AtomicLong();
    private static final AtomicLong totalBytesCounter = new AtomicLong();
    private static final AtomicLong totalFilesCounter = new AtomicLong();
    private static long startTime;

    public static void main(final String[] args) throws Exception
    {
        if (args.length == 0)
        {
            System.err.println("Please provide the results directory.");
            return;
        }

        final Path inputDir = Paths.get(args[0]).toAbsolutePath();
        if (!Files.exists(inputDir) || !Files.isDirectory(inputDir))
        {
            System.err.println("Input directory does not exist or is not a folder: " + inputDir);
            return;
        }

        final int threadCount = args.length > 1 ? Integer.parseInt(args[1]) : Runtime.getRuntime().availableProcessors();

        System.out.println("Processing " + inputDir);
        System.out.printf("Using %d threads%n", threadCount);

        startTime = TimerUtils.get().getStartTime();

        final ExecutorService executor = Executors.newFixedThreadPool(threadCount, new DaemonThreadFactory(i -> "DataReader-" + i, Thread.MAX_PRIORITY));

        // Use modern NIO Files.walk to entirely avoid File[] allocations and File directory loading overhead
        try (Stream<Path> paths = Files.walk(inputDir, 4))
        {
            paths.filter(Files::isRegularFile)
                 .filter(p -> {
                     final String lowerName = p.getFileName().toString().toLowerCase();
                     // Only matching uncompressed timers for the NIO raw byte test
                     return (lowerName.startsWith("timers.csv") || lowerName.startsWith("cpt_timers.csv")) 
                            && lowerName.endsWith(".csv"); 
                 })
                 .forEach(p -> executor.execute(() -> processFile(p)));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        final long duration = TimerUtils.get().getElapsedTime(startTime);
        final long linesPerSecond = Math.round((totalLinesCounter.get() / (double) Math.max(1, duration)) * 1000L);
        final long mbPerSecond = Math.round((totalBytesCounter.get() / (double) Math.max(1, duration)) * 1000L / (1024 * 1024));

        System.out.println("--------------------------------------------------");
        System.out.printf("Finished in %,d ms.%n", duration);
        System.out.printf("Files read: %,d%n", totalFilesCounter.get());
        System.out.printf("Lines read: %,d%n", totalLinesCounter.get());
        System.out.printf("Bytes read: %,d%n", totalBytesCounter.get());
        System.out.printf("Lines/sec:  %,d%n", linesPerSecond);
        System.out.printf("MB/sec:     %,d%n", mbPerSecond);
        System.out.println("--------------------------------------------------");
    }

    private static void processFile(final Path file)
    {
        try
        {
            long lines = 0;
            long bytes = 0;

            // Use Java NIO FileChannel with Direct ByteBuffer to interact natively with OS kernel paging
            try (final FileChannel channel = FileChannel.open(file, StandardOpenOption.READ))
            {
                final ByteBuffer buffer = ByteBuffer.allocateDirect(16384);
                int read;
                
                while ((read = channel.read(buffer)) != -1)
                {
                    bytes += read;
                    buffer.flip();
                    
                    while (buffer.hasRemaining())
                    {
                        if (buffer.get() == '\n')
                        {
                            lines++;
                        }
                    }
                    buffer.clear();
                }
            }

            final long totalLines = totalLinesCounter.addAndGet(lines);
            final long totalBytes = totalBytesCounter.addAndGet(bytes);
            final long currentFiles = totalFilesCounter.incrementAndGet();

            if (currentFiles % 500 == 0)
            {
                final long duration = Math.max(1, TimerUtils.get().getElapsedTime(startTime));
                final long currentMbPerSec = Math.round((totalBytes / (double) duration) * 1000L / (1024 * 1024));
                System.out.printf("Progress: %,d files, %,d lines, %,d MB (%,d MB/s)...%n", 
                                  currentFiles, totalLines, totalBytes / (1024 * 1024), currentMbPerSec);
            }
        }
        catch (final Exception e)
        {
            System.err.println("Failed to read file: " + file);
            e.printStackTrace();
        }
    }
}
