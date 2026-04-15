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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import com.xceptance.common.io.ByteBufferedLineReader;
import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Standalone speed test for reading XLT result directories using standard File IO and GZIP,
 * leaving Apache Commons VFS out of the loop completely.
 */
public class SpeedTestReaderNoVFS
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

        final File inputDir = new File(args[0]).getAbsoluteFile();
        if (!inputDir.exists() || !inputDir.isDirectory())
        {
            System.err.println("Input directory does not exist or is not a folder: " + inputDir);
            return;
        }

        final int threadCount = args.length > 1 ? Integer.parseInt(args[1]) : Runtime.getRuntime().availableProcessors();

        System.out.println("Processing " + inputDir);
        System.out.printf("Using %d threads%n", threadCount);

        startTime = TimerUtils.get().getStartTime();

        final ExecutorService executor = Executors.newFixedThreadPool(threadCount, new DaemonThreadFactory(i -> "DataReader-" + i, Thread.MAX_PRIORITY));

        // Mimic DataProcessor's agent -> testcase -> user directory traversal
        final File[] agentDirs = inputDir.listFiles();
        if (agentDirs != null)
        {
            for (final File agentDir : agentDirs)
            {
                if (agentDir.isDirectory())
                {
                    final File[] testCaseDirs = agentDir.listFiles();
                    if (testCaseDirs != null)
                    {
                        for (final File testCaseDir : testCaseDirs)
                        {
                            if (testCaseDir.isDirectory())
                            {
                                final File[] testUserDirs = testCaseDir.listFiles();
                                if (testUserDirs != null)
                                {
                                    for (final File testUserDir : testUserDirs)
                                    {
                                        if (testUserDir.isDirectory())
                                        {
                                            executor.execute(() -> processUserDir(testUserDir));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    private static void processUserDir(final File testUserDir)
    {
        try
        {
            final File[] files = testUserDir.listFiles();
            if (files != null)
            {
                for (final File file : files)
                {
                    if (file.isFile() && file.canRead())
                    {
                        final String fileName = file.getName();
                        
                        final boolean isTimerFile = XltConstants.TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(fileName));
                        final boolean isCptTimerFile = XltConstants.CPT_TIMER_FILENAME_PATTERNS.stream().anyMatch(r -> r.asPredicate().test(fileName));
                        
                        if (isTimerFile || isCptTimerFile)
                        {
                            processFile(file);
                        }
                    }
                }
            }
        }
        catch (final Exception e)
        {
            System.err.println("Failed to process directory: " + testUserDir);
            e.printStackTrace();
        }
    }

    private static void processFile(final File file)
    {
        try
        {
            final boolean isCompressed = file.getName().toLowerCase().endsWith(".gz");

            long lines = 0;
            long bytes = 0;

            try (final InputStream is = new FileInputStream(file);
                 final ByteBufferedLineReader reader = new ByteBufferedLineReader(
                     isCompressed ? new GZIPInputStream(is, 1024 * 31) : is))
            {
                final byte[] lineBuffer = new byte[16384]; // Pre-allocate a single buffer
                int length;
                while ((length = reader.readInto(lineBuffer, 0)) != -1)
                {
                    lines++;
                    bytes += length;
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
