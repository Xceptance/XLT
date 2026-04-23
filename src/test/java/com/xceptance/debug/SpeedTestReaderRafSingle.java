package com.xceptance.debug;

import java.io.File;
import java.io.RandomAccessFile;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Standalone speed test for reading a single large file using pure JDK RandomAccessFile
 * for uncompressed files without any abstract IO wrappers.
 */
public class SpeedTestReaderRafSingle
{
    public static void main(final String[] args) throws Exception
    {
        if (args.length == 0)
        {
            System.err.println("Please provide the file to read.");
            return;
        }

        final File file = new File(args[0]).getAbsoluteFile();
        if (!file.exists() || !file.isFile())
        {
            System.err.println("Input file does not exist or is not a file: " + file);
            return;
        }

        System.out.println("Processing " + file);

        final long startTime = TimerUtils.get().getStartTime();
        
        long lines = 0;
        long bytes = 0;

        try (final RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            final byte[] buffer = new byte[65536]; // 64k buffer
            int length;
            
            while ((length = raf.read(buffer)) != -1)
            {
                bytes += length;
                
                // Natively sweep the byte array for Unix newlines just to mimic processing
                for (int i = 0; i < length; i++)
                {
                    if (buffer[i] == '\n')
                    {
                        lines++;
                    }
                }
            }
        }

        final long duration = TimerUtils.get().getElapsedTime(startTime);
        final long linesPerSecond = Math.round((lines / (double) Math.max(1, duration)) * 1000L);
        final long mbPerSecond = Math.round((bytes / (double) Math.max(1, duration)) * 1000L / (1024 * 1024));

        System.out.println("--------------------------------------------------");
        System.out.printf("Finished in %,d ms.%n", duration);
        System.out.printf("Lines read: %,d%n", lines);
        System.out.printf("Bytes read: %,d%n", bytes);
        System.out.printf("Lines/sec:  %,d%n", linesPerSecond);
        System.out.printf("MB/sec:     %,d%n", mbPerSecond);
        System.out.println("--------------------------------------------------");
    }
}
