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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.storage.ChunkStorage;

/**
 * Coordinates transparent, disk-backed caching of preprocessed columnar chunks for the report generator.
 * <p>
 * <b>Transparent CLI Workflow &amp; Operational Contract:</b>
 * <ul>
 *   <li><b>First Run (Cold Cache):</b> Standard CSV log parsing executes across all worker threads.
 *       Concurrently, {@link com.xceptance.xlt.report.storage.ChunkIngestionCollector} packages all parsed,
 *       merged, and post-processed records into SIMD-compressed columnar chunks in memory. At the conclusion
 *       of Phase 1, the dataset is persisted to disk along with a cryptographic {@link CacheFingerprint}.
 *       The CLI command, exit status, and report outputs remain 100% identical and backwards-compatible.</li>
 *   <li><b>Subsequent Runs (Warm Cache):</b> The {@link CacheManager} verifies whether the cache exists,
 *       calculates a fresh {@link CacheFingerprint} from the raw CSV directory and active merge rules,
 *       and compares it to the saved fingerprint. If valid, the expensive multi-GB CSV parsing phase is
 *       completely bypassed, and columnar chunks are read into memory in a fraction of a second.</li>
 *   <li><b>Strict Invalidation &amp; Safe Purging:</b> If raw log files have been added, modified, or removed,
 *       or if the user adjusted request merge/labeling rules in {@code reportgenerator.properties}, the cache
 *       is recognized as invalid and the entire cache folder is completely deleted from disk immediately.</li>
 * </ul>
 */
public class CacheManager
{
    /** Logger instance for cache lifecycle diagnostics. */
    private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);

    /** Report generator configuration providing cache toggles and path settings. */
    private final ReportGeneratorConfiguration config;

    /** Root directory containing raw test results and agent CSV directories. */
    private final File resultsDir;

    /** Target directory on disk where cache files and fingerprints are stored. */
    private final File cacheDir;

    /**
     * Constructs a new {@link CacheManager} for the given results directory and configuration.
     *
     * @param resultsDir
     *            root results directory
     * @param config
     *            report generator configuration (may be null, in which case default settings apply)
     */
    public CacheManager(final File resultsDir, final ReportGeneratorConfiguration config)
    {
        this.resultsDir = resultsDir;
        this.config = config;
        this.cacheDir = config != null ? config.getDataCacheDirectory(resultsDir) : new File(resultsDir, "xlt-cache");
    }

    /**
     * Returns the target cache directory on disk.
     *
     * @return directory where cache files are located
     */
    public File getCacheDirectory()
    {
        return cacheDir;
    }

    /**
     * Checks if data caching is enabled in the configuration.
     *
     * @return true if caching is enabled
     */
    public boolean isCacheEnabled()
    {
        return config != null && config.isDataCacheEnabled();
    }

    /**
     * Checks if a valid, uncorrupted cache matching current inputs and transformation rules is available on disk.
     * <p>
     * If the cache is invalid, stale, or corrupted, it is completely purged immediately to prevent
     * partial or inconsistent state.
     *
     * @return {@code true} if a valid cache is ready to load; {@code false} otherwise
     */
    public boolean isCacheValid()
    {
        // Check global property toggle
        if (!isCacheEnabled())
        {
            return false;
        }

        // Verify directory existence
        if (!cacheDir.exists() || !cacheDir.isDirectory())
        {
            return false;
        }

        try
        {
            // 1. Load saved fingerprint from disk
            final CacheFingerprint saved = CacheFingerprint.loadFromDirectory(cacheDir);
            if (saved == null)
            {
                LOG.info("ChunkDB cache missing fingerprint. Purging cache at: {}", cacheDir);
                purgeCache();
                return false;
            }

            // 2. Compute current fingerprint from raw CSV files and active configuration rules
            final CacheFingerprint current = CacheFingerprint.calculate(resultsDir, config);
            if (!current.matches(saved))
            {
                LOG.info("ChunkDB cache is stale (inputs or rules changed). Purging cache at: {}", cacheDir);
                purgeCache();
                return false;
            }

            return true;
        }
        catch (final Throwable e)
        {
            LOG.warn("Error verifying ChunkDB cache at {}. Purging cache.", cacheDir, e);
            purgeCache();
            return false;
        }
    }

    /**
     * Attempts to load {@link ChunkStorage} from disk cache.
     * <p>
     * If loading fails due to file corruption or I/O failure, the cache directory is safely purged
     * and {@code null} is returned so the report generator falls back to standard CSV parsing.
     *
     * @return loaded {@link ChunkStorage} instance, or {@code null} if cache is invalid or cannot be read
     */
    public ChunkStorage load()
    {
        if (!isCacheValid())
        {
            return null;
        }

        try
        {
            LOG.info("Loading preprocessed test data from ChunkDB cache at: {}", cacheDir);
            final long start = System.currentTimeMillis();
            final ChunkStorage storage = ChunkStorage.loadFromDirectory(cacheDir);
            final long duration = System.currentTimeMillis() - start;
            LOG.info("Successfully loaded ChunkDB cache ({} records in {} chunks) in {} ms",
                     storage.getTotalRowCount(), storage.getCatalog().getChunkCount(), duration);
            return storage;
        }
        catch (final Throwable e)
        {
            LOG.warn("Failed to load ChunkDB cache at {}. Purging corrupted cache.", cacheDir, e);
            purgeCache();
            return null;
        }
    }

    /**
     * Quickly reads global minTime and maxTime from the catalog header if the cache is present and valid.
     * <p>
     * Supports both the modern V2 binary format (with magic number 0x584C5443) and legacy V1 format.
     * This allows {@link com.xceptance.xlt.report.ReportGenerator} to resolve relative start/end time offsets
     * (e.g. "from start + 5m to end - 5m") before initializing log reading and statistics processors.
     *
     * @return 2-element array {@code [minTime, maxTime]}, or {@code null} if cache is absent or invalid
     */
    public long[] peekTimeRange()
    {
        if (!isCacheValid())
        {
            return null;
        }

        final File catalogFile = new File(cacheDir, ChunkStorage.CHUNKS_FILE_NAME);
        if (!catalogFile.isFile() || catalogFile.length() < 16)
        {
            return null;
        }

        try (final java.io.DataInputStream in =
                 new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(catalogFile))))
        {
            final int magicOrMinHigh = in.readInt();
            if (magicOrMinHigh == com.xceptance.xlt.report.storage.catalog.ChunkCatalog.MAGIC)
            {
                // V2 binary format: int magic, int version, long minTime, long maxTime
                final int version = in.readInt();
                final long min = in.readLong();
                final long max = in.readLong();
                return new long[]{min, max};
            }
            else
            {
                // V1 legacy format: long minTime (magicOrMinHigh is high 32-bits), long maxTime
                final int minLow = in.readInt();
                final long min = (((long) magicOrMinHigh) << 32) | (minLow & 0xFFFFFFFFL);
                final long max = in.readLong();
                return new long[]{min, max};
            }
        }
        catch (final Throwable e)
        {
            return null;
        }
    }

    /**
     * Persists {@link ChunkStorage} and saves the calculated {@link CacheFingerprint} to the cache directory.
     * <p>
     * If writing fails at any point, the cache directory is purged completely to avoid leaving half-written data.
     *
     * @param storage
     *            the {@link ChunkStorage} instance to persist
     */
    public void save(final ChunkStorage storage)
    {
        if (!isCacheEnabled() || storage == null)
        {
            return;
        }

        try
        {
            LOG.info("Saving preprocessed test data to ChunkDB cache at: {}", cacheDir);
            final long start = System.currentTimeMillis();

            if (!cacheDir.exists())
            {
                cacheDir.mkdirs();
            }

            // 1. Serialize dictionaries and chunks
            storage.saveToDirectory(cacheDir);

            // 2. Serialize fingerprint last to guarantee atomicity of valid cache state
            final CacheFingerprint fingerprint = CacheFingerprint.calculate(resultsDir, config);
            fingerprint.saveToDirectory(cacheDir);

            final long duration = System.currentTimeMillis() - start;
            LOG.info("Successfully saved ChunkDB cache in {} ms", duration);
        }
        catch (final Throwable e)
        {
            LOG.warn("Failed to save ChunkDB cache at {}. Dropping cache directory.", cacheDir, e);
            purgeCache();
        }
    }

    /**
     * Recursively purges and deletes the entire cache directory from the filesystem.
     */
    public void purgeCache()
    {
        if (cacheDir.exists())
        {
            try
            {
                FileUtils.deleteDirectory(cacheDir);
            }
            catch (final IOException e)
            {
                LOG.warn("Failed to completely delete cache directory: {}", cacheDir, e);
            }
        }
    }
}
