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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
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

        return new CacheFingerprint(ChunkCatalog.VERSION, accumulator.fileCount, accumulator.totalBytes, rulesHash, version);
    }

    /**
     * Recursively walks the results directory tree, discovering raw timer CSV files and accumulating
     * file counts and total byte sizes.
     *
     * @param dir
     *            current directory being inspected
     * @param acc
     *            file accumulator holding totals
     */
    private static void scanDirectory(final File dir, final FileAccumulator acc)
    {
        if (dir == null || !dir.isDirectory())
        {
            return;
        }

        final File[] files = dir.listFiles();
        if (files == null)
        {
            return;
        }

        for (final File f : files)
        {
            final String name = f.getName();
            if (f.isDirectory())
            {
                // Skip output report directory, the cache directory itself, and version control directories
                if ("output".equalsIgnoreCase(name) ||
                    acc.cacheDirName.equalsIgnoreCase(name) ||
                    ".git".equalsIgnoreCase(name))
                {
                    continue;
                }
                scanDirectory(f, acc);
            }
            else if (f.isFile() && isTimerLogFile(name))
            {
                acc.fileCount++;
                acc.totalBytes += f.length();
            }
        }
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
        // Check standard timer patterns (timers.csv, requests.csv, etc.)
        for (final Pattern p : XltConstants.TIMER_FILENAME_PATTERNS)
        {
            if (p.matcher(name).matches())
            {
                return true;
            }
        }
        // Check compact timer patterns (cpt-timers.csv, etc.)
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

            // 4. Request forensic details retention mode ("sparse" vs "full")
            final String detailsMode = config.getRequestDetailsMode();
            md.update((detailsMode != null ? detailsMode.toLowerCase() : "sparse").getBytes(StandardCharsets.UTF_8));

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
     * Helper accumulator for directory traversal file metrics.
     */
    private static class FileAccumulator
    {
        final String cacheDirName;
        int fileCount = 0;
        long totalBytes = 0;

        FileAccumulator(final String cacheDirName)
        {
            this.cacheDirName = cacheDirName != null ? cacheDirName : "xlt-cache";
        }
    }
}
