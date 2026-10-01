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
package com.xceptance.xlt.report;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;

import com.xceptance.common.util.StringMatcher;
import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.agent.CustomSamplersRunner;
import com.xceptance.xlt.agent.JvmResourceUsageDataGenerator;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * Processor for the chain file to log line to parsed log line via
 * log line filter and transformation to finally the statistics part
 * where the report provider will collect there data
 *
 * DataProcessor
 * +- file reading pool
 * +- line processing pool
 * -> StatisicsProcessor (single thread)
 *
 * @see DataReaderThread
 * @see DataParserThread
 * @see StatisticsProcessor
 */
public class DataProcessor
{
    /**
     * The executor dealing with the data record parser threads.
     */
    private final ExecutorService dataParserExecutor;

    /**
     * The area where we handle the final data gathering aka collecting
     * of data for later reporting
     */
    private final StatisticsProcessor statisticsProcessor;

    /**
     * The executor dealing with the data record reader threads.
     */
    private final ExecutorService dataReaderExecutor;

    /**
     * The dispatcher that coordinates all the reader/parser/processor threads.
     */
    private final Dispatcher dispatcher;

    /**
     * Input directory.
     */
    private final FileObject inputDir;

    /**
     * The total number of lines/data records read.
     */
    private final AtomicLong totalLinesCounter = new AtomicLong();

    /**
     * The filter to skip the results of certain test cases when reading.
     */
    private final StringMatcher testCaseFilter;

    /**
     * Only include the results of those agents that match this filter.
     */
    private final StringMatcher agentFilter;

    private final long fromTime;
    private final long toTime;

    private final ReportGeneratorConfiguration config;

    /**
     * Constructor.
     *
     * @param inputDir
     *            input directory
     * @param dataRecordFactory
     *            data record factory
     * @param fromTime
     *            time when report starts
     * @param toTime
     *            time when report ends
     * @param reportProviders
     *            report providers
     * @param requestMergeRules
     *            the request merge rules
     * @param maxThreadCount
     *            the number of parallel threads allowed
     * @param testCaseIncludePatternList
     *            a comma-separated list of reg-ex patterns that match the test cases to read
     * @param testCaseExcludePatternList
     *            a comma-separated list of reg-ex patterns that match the test cases not to read
     * @param agentIncludePatternList
     *            a comma-separated list of reg-ex patterns that match the result directories of those agents to read
     * @param agentExcludePatternList
     *            a comma-separated list of reg-ex patterns that match the result directories of those agents to skip
     * @param removeIndexesFromRequestNames
     *            whether to automatically remove any indexes from request names
     */
    public DataProcessor(
                     final ReportGeneratorConfiguration config,
                     final FileObject inputDir, final DataRecordFactory dataRecordFactory, final long fromTime, final long toTime,
                     final List<ReportProvider> reportProviders,
                     final String testCaseIncludePatternList, final String testCaseExcludePatternList,
                     final String agentIncludePatternList, final String agentExcludePatternList)
    {
        this.config = config;
        this.inputDir = inputDir;
        this.fromTime = fromTime;
        this.toTime = toTime;

        testCaseFilter = new StringMatcher(testCaseIncludePatternList, testCaseExcludePatternList, true);
        agentFilter = new StringMatcher(agentIncludePatternList, agentExcludePatternList, true);

        // the one and only data record processor
        statisticsProcessor = new StatisticsProcessor(reportProviders, config);

        // create the reader executor
        dataReaderExecutor = Executors.newFixedThreadPool(config.readerThreadCount, new DaemonThreadFactory(i -> "DataReader-" + i, Thread.MAX_PRIORITY));

        // create the data record parser threads
        dataParserExecutor = Executors.newFixedThreadPool(config.parserThreadCount, new DaemonThreadFactory(i -> "DataParser-" + i));

        // initialize chunk collector if caching is enabled and full run
        // Only full runs (unfiltered by time slice, test cases, or agents) build the persistent cache
        // to ensure that future runs with arbitrary query filters can reuse the cached database.
        final boolean isFullRun = (fromTime == 0 && toTime == Long.MAX_VALUE &&
                                   testCaseIncludePatternList == null && testCaseExcludePatternList == null &&
                                   agentIncludePatternList == null && agentExcludePatternList == null);
        if (config.isDataCacheEnabled() && isFullRun)
        {
            final java.io.File resultsDir = new java.io.File(inputDir.getName().getPath());
            final java.io.File cacheDir = config.getDataCacheDirectory(resultsDir);
            chunkCollector = new com.xceptance.xlt.report.storage.ChunkIngestionCollector(cacheDir);
        }
        else
        {
            chunkCollector = null;
        }

        // create the dispatcher
        dispatcher = new Dispatcher(config, statisticsProcessor, chunkCollector);

        // start the threads
        for (int i = 0; i < config.parserThreadCount; i++)
        {
            dataParserExecutor.execute(
                                       new DataParserThread(dispatcher, dataRecordFactory, fromTime, toTime, config));
        }

        XltLogger.reportLogger.info(String.format("Input directory: %s", inputDir));
    }

    /**
     * Ingestion collector accumulating records into compressed columnar ChunkDB chunks concurrently
     * with standard report statistics aggregation during cold-cache runs.
     */
    private final com.xceptance.xlt.report.storage.ChunkIngestionCollector chunkCollector;

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public final long getMaximumTime()
    {
        return statisticsProcessor.getMaximumTime();
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public final long getMinimumTime()
    {
        return statisticsProcessor.getMinimumTime();
    }

    /**
     * Reads all the data records from the configured input directory.
     */
    public void readDataRecords()
    {
        try
        {
            dispatcher.startProgress();
            final long start = TimerUtils.get().getStartTime();

            for (final FileObject file : inputDir.getChildren())
            {
                if (file.getType() == FileType.FOLDER)
                {
                    // Check if we need to process the current agent directory
                    final String directoryName = file.getName().getBaseName();
                    if (agentFilter.isAccepted(directoryName))
                    {
                        readDataRecordsFromAgentDir(file);
                    }
                }
            }

            // Wait for all data record parsing and thread-local processing to complete
            dispatcher.waitForDataRecordProcessingToComplete();

            // Calculate parsing and processing duration and throughput strictly for log reading
            final long readDuration = TimerUtils.get().getElapsedTime(start);
            final long linesPerSecond = Math.round((totalLinesCounter.get() / (double) readDuration) * 1000L);

            // Log the parsing throughput strictly representing the record reading and chunk ingestion phase
            XltLogger.reportLogger.info(String.format("%,d records read - %,d ms - %,d lines/s",
                              totalLinesCounter.get(),
                              readDuration,
                              linesPerSecond));

            // If transparent caching is active and this was a full cold run, persist the ChunkStorage database
            // and write a validation fingerprint so subsequent CLI executions can execute via fast SIMD scan.
            // NOTE: When direct-to-disk streaming is active, chunks were already written to chunks.bin.tmp during
            // parsing; finishing writes the index table and header. We safely close storage after caching.
            if (chunkCollector != null)
            {
                final long tCacheStart = TimerUtils.get().getStartTime();
                try (final com.xceptance.xlt.report.storage.ChunkStorage storage = chunkCollector.finish())
                {
                    XltLogger.reportLogger.info("Persisting ChunkDB cache to disk...");
                    final java.io.File resultsDir = new java.io.File(inputDir.getName().getPath());
                    final com.xceptance.xlt.report.storage.cache.CacheManager cacheManager =
                        new com.xceptance.xlt.report.storage.cache.CacheManager(resultsDir, config);
                    cacheManager.save(storage);

                    final long cacheDuration = TimerUtils.get().getElapsedTime(tCacheStart);
                    XltLogger.reportLogger.info(String.format("ChunkDB cache persisted - %,d ms", cacheDuration));

                    // Phase 2: Query the sealed ChunkDB concurrently to aggregate report statistics
                    XltLogger.reportLogger.info("Aggregating report statistics from ChunkDB...");
                    final long scanStart = TimerUtils.get().getStartTime();

                    final org.roaringbitmap.RoaringBitmap matchingAgents =
                        storage.getDictionaries().filterAgentTestCaseIds(agentFilter, testCaseFilter);

                    final com.xceptance.xlt.report.storage.query.ScanPredicate predicate =
                        new com.xceptance.xlt.report.storage.query.ScanPredicate(fromTime, toTime, null, matchingAgents);

                    final com.xceptance.xlt.report.ChunkQueryEngine queryEngine =
                        new com.xceptance.xlt.report.ChunkQueryEngine(storage, config.parserThreadCount);
                    final long recordsProcessed = queryEngine.executeScan(predicate, statisticsProcessor);

                    final long scanDuration = TimerUtils.get().getElapsedTime(scanStart);
                    final long scanRate = scanDuration > 0 ? Math.round((recordsProcessed / (double) scanDuration) * 1000L) : 0;
                    XltLogger.reportLogger.info(String.format("%,d records aggregated from ChunkDB - %,d ms - %,d records/s",
                                                              recordsProcessed, scanDuration, scanRate));
                }
                catch (final Exception e)
                {
                    XltLogger.reportLogger.error("Failed to persist ChunkDB cache or aggregate report: " + e.getMessage(), e);
                    throw new RuntimeException("ChunkDB processing failed", e);
                }
            }
            else
            {
                // Complete processing and merge worker statistics into master providers
                statisticsProcessor.complete();
            }
        }
        catch (final Exception e)
        {
            XltLogger.reportLogger.error("Failed to read data records", e);
        }
        finally
        {
            // ensure all worker statistics are merged into master providers even on partial reads
            statisticsProcessor.complete();

            // stop background threads
            dataParserExecutor.shutdownNow();
            dataReaderExecutor.shutdownNow();
        }
    }

    /**
     * Reads the timer files from the given agent directory.
     *
     * @param agentDir
     *            agent directory
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readDataRecordsFromAgentDir(final FileObject agentDir) throws Exception
    {
        for (final FileObject file : agentDir.getChildren())
        {
            if (file.getType() == FileType.FOLDER)
            {
                // filter out certain directories if so configured
                final String directoryName = file.getName().getBaseName();
                if (isSpecialDirectory(directoryName) || testCaseFilter.isAccepted(directoryName))
                {
                    readDataRecordsFromTestCaseDir(file, agentDir.getName().getBaseName());
                }
            }
        }
    }

    /**
     * Reads the timer files from the given test case directory.
     *
     * @param testCaseDir
     *            test case directory
     * @param agentName
     *            the associated agent
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readDataRecordsFromTestCaseDir(final FileObject testCaseDir, final String agentName) throws Exception
    {
        final String testCaseName = testCaseDir.getName().getBaseName();

        for (final FileObject file : testCaseDir.getChildren())
        {
            if (file.getType() == FileType.FOLDER)
            {
                readDataRecordsFromTestUserDir(file, agentName, testCaseName);
            }
        }
    }

    /**
     * Reads the timer files from the given test user directory.
     *
     * @param testUserDir
     *            test user directory
     * @param agentName
     *            the associated agent
     * @param testCaseName
     *            the associated test case
     * @throws IOException
     *             thrown on I/O-Error
     */
    private void readDataRecordsFromTestUserDir(final FileObject testUserDir, final String agentName, final String testCaseName)
        throws Exception
    {
        dispatcher.incremementDirectoryCount();

        // create a new reader for each user directory and enqueue it for execution
        final String userNumber = testUserDir.getName().getBaseName();
        final DataReaderThread reader = new DataReaderThread(testUserDir, agentName, testCaseName, userNumber,
                                                             totalLinesCounter,
                                                             dispatcher);
        dataReaderExecutor.execute(reader);
    }

    /**
     * Determines whether the given directory name denotes a special directory. Special directories are
     * "Agent-JVM-Monitor" and "CustomSampler".
     *
     * @param directoryName
     *            the directory to check
     * @return whether the directory is special
     */
    private boolean isSpecialDirectory(String directoryName)
    {
        return CustomSamplersRunner.RESULT_DIRECTORY_NAME.equals(directoryName) ||
               JvmResourceUsageDataGenerator.RESULT_DIRECTORY_NAME.equals(directoryName);
    }
}
