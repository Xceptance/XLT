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
package com.xceptance.xlt.report.storage.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.storage.StorageIoUtils;
import com.xceptance.xlt.report.storage.catalog.ChunkCatalog;

/**
 * Cryptographic and structural fingerprint representing the exact state of input raw timer CSV logs
 * and report transformation configuration rules.
 * <p>
 * <b>Cache Invalidation Invariants:</b>
 * A cached dataset is valid if and only if all five components match:
 * <ol>
 *   <li><b>Format Version:</b> The binary ChunkDB storage format version ({@link ChunkCatalog#VERSION}),
 *       protecting against binary schema revisions (e.g., V2 to V3 upgrades).</li>
 *   <li><b>Input File Count:</b> Total number of raw timer CSV files in the input results directory tree.</li>
 *   <li><b>Input Total Bytes:</b> Total combined byte size of all discovered raw timer CSV files. Any modification,
 *       appended lines, or deleted log files alters this total.</li>
 *   <li><b>Merge, Labeling &amp; Forensic Retention Rules Hash:</b> SHA-256 digest of request merging rules,
 *       labeling rules, index stripping ({@code com.xceptance.xlt.reportgenerator.removeIndexesFromRequestNames}),
 *       and request forensic retention mode ({@code sparse} vs {@code full} via {@code --preserve-all-details}).
 *       Modifying any merge pattern or toggling full forensic retention invalidates the cache because request names
 *       and Cold Forensic Tail Blocks are constructed at ingestion time.</li>
 *   <li><b>XLT Version:</b> The version string of the XLT framework to protect against release upgrades.</li>
 * </ol>
 *
 * @param formatVersion
 *            the binary ChunkDB storage format version
 * @param fileCount
 *            the number of timer CSV files scanned
 * @param totalBytes
 *            the aggregated byte length of all timer CSV files
 * @param mergeRulesHash
 *            SHA-256 hex digest of transformation and forensic retention rules
 * @param xltVersion
 *            the XLT release version string
 */
public record CacheFingerprint(int formatVersion, int fileCount, long totalBytes, String mergeRulesHash, String xltVersion)
{
    /** Magic header identifier (0x584C5446 = 'XLTF' in ASCII hex). */
    public static final int MAGIC = 0x584C5446;

    /** Binary fingerprint metadata format version. */
    public static final int VERSION = 1;

    /** Binary filename storing the serialized cache fingerprint metadata. */
    private static final String FINGERPRINT_FILE_NAME = "fingerprint.bin";

    /**
     * Backward-compatible convenience constructor defaulting formatVersion to {@link ChunkCatalog#VERSION}.
     *
     * @param fileCount
     *            the number of timer CSV files scanned
     * @param totalBytes
     *            the aggregated byte length of all timer CSV files
     * @param mergeRulesHash
     *            SHA-256 hex digest of transformation rules
     * @param xltVersion
     *            the XLT release version string
     */
    public CacheFingerprint(final int fileCount, final long totalBytes, final String mergeRulesHash, final String xltVersion)
    {
        this(ChunkCatalog.VERSION, fileCount, totalBytes, mergeRulesHash, xltVersion);
    }

    /**
     * Calculates a fingerprint from the test results root directory and report generator configuration.
     *
     * @param resultsDir
     *            root results directory containing test runs
     * @param config
     *            report generator configuration containing merge rules and cache settings
     * @return calculated {@link CacheFingerprint}
     */
    public static CacheFingerprint calculate(final File resultsDir, final ReportGeneratorConfiguration config)
    {
        final FileAccumulator accumulator = new FileAccumulator(config != null ? config.getDataCacheDirectoryName() : "xlt-cache");
        scanDirectory(resultsDir, accumulator);

        final String rulesHash = calculateRulesHash(config);
        final String version = ProductInformation.getProductInformation().getVersion();

        return new CacheFingerprint(ChunkCatalog.VERSION, (int) accumulator.fileCount.sum(), accumulator.totalBytes.sum(), rulesHash, version);
    }

    /**
     * Walks the results directory tree in parallel across top-level entries using NIO {@link Files#walkFileTree},
     * discovering raw timer CSV files and accumulating file counts and total byte sizes without redundant
     * per-file {@code stat()} system calls.
     *
     * @param dir
     *            root directory being inspected
     * @param acc
     *            thread-safe file accumulator holding totals
     */
    private static void scanDirectory(final File dir, final FileAccumulator acc)
    {
        if (dir == null || !dir.isDirectory())
        {
            return;
        }

        final File[] topFiles = dir.listFiles();
        if (topFiles == null || topFiles.length == 0)
        {
            return;
        }

        Arrays.stream(topFiles).parallel().forEach(f -> {
            final String name = f.getName();
            if (f.isDirectory())
            {
                if (isSkippedDirectory(name, acc.cacheDirName))
                {
                    return;
                }
                scanSubtree(f.toPath(), acc);
            }
            else if (f.isFile() && isTimerLogFile(name))
            {
                acc.fileCount.increment();
                acc.totalBytes.add(f.length());
            }
        });
    }

    /**
     * Walks a single directory subtree using {@link Files#walkFileTree} so that file type and byte size
     * are obtained directly from {@link BasicFileAttributes} in a single pass.
     */
    private static void scanSubtree(final Path root, final FileAccumulator acc)
    {
        final long[] localCounts = new long[2]; // [0] = fileCount, [1] = totalBytes
        try
        {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                {
                    if (!dir.equals(root))
                    {
                        final Path fileName = dir.getFileName();
                        if (fileName != null && isSkippedDirectory(fileName.toString(), acc.cacheDirName))
                        {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                {
                    if (attrs.isRegularFile())
                    {
                        final Path fileName = file.getFileName();
                        if (fileName != null && isTimerLogFile(fileName.toString()))
                        {
                            localCounts[0]++;
                            localCounts[1] += attrs.size();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc)
                {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (final IOException ignored)
        {
            // Ignore unreadable subtree
        }

        if (localCounts[0] > 0)
        {
            acc.fileCount.add(localCounts[0]);
            acc.totalBytes.add(localCounts[1]);
        }
    }

    /**
     * Checks whether a directory name should be excluded from fingerprint scanning
     * (report output directories, the ChunkDB cache directory itself, and git metadata).
     */
    private static boolean isSkippedDirectory(final String name, final String cacheDirName)
    {
        return "output".equalsIgnoreCase(name) ||
               cacheDirName.equalsIgnoreCase(name) ||
               ".git".equalsIgnoreCase(name);
    }

    /**
     * Tests whether a given filename matches any recognized XLT timer CSV pattern.
     *
     * @param name
     *            the filename to test
     * @return true if the file is a timer log CSV file
     */
    private static boolean isTimerLogFile(final String name)
    {
        // Fast-path for standard timer filenames (99.9%+ of all load test files)
        if ("timers.csv.gz".equals(name) || "timers.csv".equals(name))
        {
            return true;
        }
        if (!name.startsWith("timer"))
        {
            return false;
        }

        // Check standard timer patterns (timers.csv, dated timers.csv, etc.)
        for (final Pattern p : XltConstants.TIMER_FILENAME_PATTERNS)
        {
            if (p.matcher(name).matches())
            {
                return true;
            }
        }
        // Check compact timer patterns (timer-wd-*.csv, etc.)
        for (final Pattern p : XltConstants.CPT_TIMER_FILENAME_PATTERNS)
        {
            if (p.matcher(name).matches())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates a deterministic SHA-256 hex digest representing all configured name transformation,
     * merging, and labeling rules.
     *
     * @param config
     *            report generator configuration
     * @return SHA-256 hex digest string
     */
    private static String calculateRulesHash(final ReportGeneratorConfiguration config)
    {
        if (config == null)
        {
            return "default";
        }
        try
        {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 1. Index removal flag
            md.update(Boolean.toString(config.getRemoveIndexesFromRequestNames()).getBytes(StandardCharsets.UTF_8));

            // 2. Request merging rules
            if (config.getMergeRules() != null)
            {
                for (final Object rule : config.getMergeRules())
                {
                    md.update(rule.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            // 3. Request labeling rules
            if (config.getLabelingRules() != null)
            {
                for (final Object rule : config.getLabelingRules())
                {
                    md.update(rule.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            // 4. Request forensic details retention mode ("sparse" vs "full") and URL hash schema version
            final String detailsMode = config.getRequestDetailsMode();
            md.update((detailsMode != null ? detailsMode.toLowerCase() : "sparse").getBytes(StandardCharsets.UTF_8));
            md.update("url-hashes-v1".getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(md.digest());
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies whether this fingerprint matches another fingerprint exactly across all dimensions.
     *
     * @param other
     *            the stored fingerprint to compare against
     * @return true if all file counts, byte sizes, rules hashes, and versions are identical
     */
    public boolean matches(final CacheFingerprint other)
    {
        if (other == null)
        {
            return false;
        }
        return formatVersion == other.formatVersion &&
               fileCount == other.fileCount &&
               totalBytes == other.totalBytes &&
               Objects.equals(mergeRulesHash, other.mergeRulesHash) &&
               Objects.equals(xltVersion, other.xltVersion);
    }

    /**
     * Serializes this fingerprint to {@code fingerprint.bin} inside the specified cache directory.
     *
     * @param cacheDir
     *            the target cache directory
     * @throws IOException
     *             if an I/O error occurs during write
     */
    public void saveToDirectory(final File cacheDir) throws IOException
    {
        final File file = new File(cacheDir, FINGERPRINT_FILE_NAME);
        try (final DataOutputStream out = new DataOutputStream(new FileOutputStream(file)))
        {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(formatVersion);
            out.writeInt(fileCount);
            out.writeLong(totalBytes);
            StorageIoUtils.writeUtfString(out, mergeRulesHash != null ? mergeRulesHash : "");
            StorageIoUtils.writeUtfString(out, xltVersion != null ? xltVersion : "");
        }
    }

    /**
     * Reads a persisted {@link CacheFingerprint} from {@code fingerprint.bin} in the specified cache directory.
     * <p>
     * Supports both the versioned fingerprint format (with {@link #MAGIC}) and legacy unversioned format.
     * If an older unversioned file is encountered, {@code formatVersion} is mapped to 0, safely triggering
     * cache invalidation and a clean rebuild.
     *
     * @param cacheDir
     *            the directory containing {@code fingerprint.bin}
     * @return loaded {@link CacheFingerprint}, or {@code null} if file does not exist or version is unsupported
     * @throws IOException
     *             if an I/O error occurs while reading
     */
    public static CacheFingerprint loadFromDirectory(final File cacheDir) throws IOException
    {
        final File file = new File(cacheDir, FINGERPRINT_FILE_NAME);
        if (!file.exists() || !file.isFile())
        {
            return null;
        }
        try (final DataInputStream in = new DataInputStream(new FileInputStream(file)))
        {
            final int magicOrCount = in.readInt();
            if (magicOrCount == MAGIC)
            {
                final int fpVersion = in.readInt();
                if (fpVersion != VERSION)
                {
                    return null;
                }
                final int fmtVersion = in.readInt();
                final int count = in.readInt();
                final long bytes = in.readLong();
                final String hash = StorageIoUtils.readUtfString(in);
                final String version = StorageIoUtils.readUtfString(in);
                return new CacheFingerprint(fmtVersion, count, bytes, hash, version);
            }
            else
            {
                // Legacy unversioned format: first int is fileCount
                final int count = magicOrCount;
                final long bytes = in.readLong();
                final String hash = in.readUTF();
                final String version = in.readUTF();
                // formatVersion 0 will fail matches() against ChunkCatalog.VERSION and trigger safe purge
                return new CacheFingerprint(0, count, bytes, hash, version);
            }
        }
    }

    /**
     * Thread-safe accumulator for parallel directory traversal file metrics.
     */
    private static class FileAccumulator
    {
        final String cacheDirName;
        final LongAdder fileCount = new LongAdder();
        final LongAdder totalBytes = new LongAdder();

        FileAccumulator(final String cacheDirName)
        {
            this.cacheDirName = cacheDirName != null ? cacheDirName : "xlt-cache";
        }
    }
}
