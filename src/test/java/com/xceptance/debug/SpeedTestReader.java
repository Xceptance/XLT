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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.io.ByteBufferedLineReader;
import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Standalone speed test for reading XLT result directories using VFS and GZIP,
 * mimicking the Report Generator's file discovery and raw reading stages.
 */
public class SpeedTestReader
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

        final FileObject inputDir = VFS.getManager().resolveFile(new File(args[0]).getAbsolutePath());
        if (!inputDir.exists() || inputDir.getType() != FileType.FOLDER)
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
        for (final FileObject agentDir : inputDir.getChildren())
        {
            if (agentDir.getType() == FileType.FOLDER)
            {
                for (final FileObject testCaseDir : agentDir.getChildren())
                {
                    if (testCaseDir.getType() == FileType.FOLDER)
                    {
                        for (final FileObject testUserDir : testCaseDir.getChildren())
                        {
                            if (testUserDir.getType() == FileType.FOLDER)
                            {
                                executor.execute(() -> processUserDir(testUserDir));
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

    private static void processUserDir(final FileObject testUserDir)
    {
        try
        {
            for (final FileObject file : testUserDir.getChildren())
            {
                if (file.getType() == FileType.FILE && file.isReadable())
                {
                    final String fileName = file.getName().getBaseName().toLowerCase();
                    
                    // Allow our new test extensions explicitly to bypass strict XltConstants limits
                    final boolean isTimerFile = fileName.endsWith(".csv") || fileName.endsWith(".csv.gz");
                    
                    if (isTimerFile)
                    {
                        processFile(file);
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

    private static void processFile(final FileObject file)
    {
        try
        {
            final String fName = file.getName().getBaseName().toLowerCase();
            final boolean isGzip = fName.endsWith(".gz");
            final boolean isZstd = fName.endsWith(".zst");
            final boolean isLz4 = fName.endsWith(".lz4");

            long lines = 0;
            long bytes = 0;

            try (final InputStream fileIs = new BufferedInputStream(file.getContent().getInputStream());
                 final InputStream decodeIs = getDecoderStream(fileIs, isGzip, isZstd, isLz4);
                 final ByteBufferedLineReader reader = new ByteBufferedLineReader(decodeIs))
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

            if (currentFiles % 1000 == 0)
            {
                final long duration = Math.max(1, TimerUtils.get().getElapsedTime(startTime));
                final long currentMbPerSec = Math.round((totalBytes / (double) duration) * 1000L / (1024 * 1024));
                final long currentLinesPerSec = Math.round((totalLines / (double) duration) * 1000L);
                System.out.printf("Progress: %,d files, %,d lines, %,d MB (%,d MB/s, %,d lines/s)...%n", 
                                  currentFiles, totalLines, totalBytes / (1024 * 1024), currentMbPerSec, currentLinesPerSec);
            }
        }
        catch (final Exception e)
        {
            System.err.println("Failed to read file: " + file);
            e.printStackTrace();
        }
    }

    private static InputStream getDecoderStream(final InputStream in, final boolean isGzip, final boolean isZstd, final boolean isLz4) throws Exception
    {
        if (isGzip) return new GZIPInputStream(in, 1024 * 31);
        if (isZstd) return new io.airlift.compress.zstd.ZstdInputStream(in);
        if (isLz4) return new net.jpountz.lz4.LZ4FrameInputStream(in);
        return in;
    }
}
