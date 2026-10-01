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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.xceptance.common.util.SynchronizingCounter;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * The {@link Dispatcher} is responsible to coordinate the various reader/parser/processor threads involved when
 * processing test results. It does not only pass the results from one thread to another, but makes sure as well that no
 * more than X threads are active at the same time.
 *
 * @see DataReaderThread
 * @see DataParserThread
 * @see StatisticsProcessor
 */
public class Dispatcher
{
    /**
     * The default maximum number of lines in a chunk delivered from reader threads to parser threads.
     */
    public static final int DEFAULT_QUEUE_CHUNK_SIZE = 200;

    /**
     * How many chunks do we deliver until waiting
     */
    public static final int DEFAULT_QUEUE_LENGTH = 100;

    /**
     * The number of directories that still need to be processed.
     */
    private final SynchronizingCounter remainingDirectories = new SynchronizingCounter();

    /**
     * Total number of directories to be or already have been processed
     */
    private final AtomicInteger totalDirectories = new AtomicInteger();

    /**
     * The number of chunks that still need to be processed.
     */
    private final SynchronizingCounter openDataChunkCount = new SynchronizingCounter();

    /**
     * The data chunks waiting to be parsed that came from the readers
     */
    private final BlockingQueue<DataChunk> readDataQueue;

    /**
     * Size of the chunks in the queues
     */
    public final int chunkSize;

    /**
     * Our progress bar
     */
    private final ProgressBar progressBar = new ProgressBarBuilder().setTaskName("Reading").setStyle(ProgressBarStyle.ASCII).build();

    private final StatisticsProcessor statisticsProcessor;

    /**
     * Optional collector for streaming parsed records into ChunkDB storage during ingestion.
     */
    private final com.xceptance.xlt.report.storage.ChunkIngestionCollector chunkCollector;

    /**
     * Creates a new {@link Dispatcher} object with the given configuration and statistics processor.
     *
     * @param config
     *            the report generator configuration
     * @param statisticsProcessor
     *            the statistics aggregation pipeline
     */
    public Dispatcher(final ReportGeneratorConfiguration config, final StatisticsProcessor statisticsProcessor)
    {
        this(config, statisticsProcessor, null);
    }

    /**
     * Creates a new {@link Dispatcher} object with the given configuration, statistics processor, and ChunkDB collector.
     *
     * @param config
     *            the report generator configuration
     * @param statisticsProcessor
     *            the statistics aggregation pipeline
     * @param chunkCollector
     *            optional {@link com.xceptance.xlt.report.storage.ChunkIngestionCollector} for populating ChunkDB
     */
    public Dispatcher(final ReportGeneratorConfiguration config, final StatisticsProcessor statisticsProcessor,
                      final com.xceptance.xlt.report.storage.ChunkIngestionCollector chunkCollector)
    {
        readDataQueue = new LinkedBlockingQueue<>(config.threadQueueLength);

        chunkSize = config.threadQueueBucketSize;

        this.statisticsProcessor = statisticsProcessor;
        this.chunkCollector = chunkCollector;
    }

    public void startProgress()
    {
    }

    /**
     * Count the directories to be processed up by one
     */
    public void incremementDirectoryCount()
    {
        totalDirectories.incrementAndGet();
        remainingDirectories.increment();
    }

    /**
     * Indicates that a reader thread is about to begin reading. Called by a reader thread.
     */
    public void beginReading() throws InterruptedException
    {
        progressBar.maxHint(totalDirectories.get());
    }

    /**
     * Indicates that a reader thread has finished reading. Called by a reader thread.
     */
    public void finishedReading()
    {
        remainingDirectories.decrement();
        progressBar.maxHint(totalDirectories.get());
        progressBar.step();
    }

    /**
     * Adds a new chunk of lines for further processing. Called by a reader thread.
     *
     * @param lineChunk
     *            the line chunk
     */
    public void addReadData(final DataChunk chunkOfLines) throws InterruptedException
    {
        openDataChunkCount.increment();
        readDataQueue.put(chunkOfLines);
    }

    /**
     * Returns a chunk of lines for further processing. Called by a parser thread.
     *
     * @return the line chunk
     */
    public DataChunk retrieveReadData() throws InterruptedException
    {
        return readDataQueue.take();
    }

    /**
     * Delivers a parsed chunk of data and puts it through the statistics processors.
     * Also streams the parsed data records into ChunkDB ingestion collector if active.
     *
     * @param postprocessedData
     *            the post-processed data records container
     * @throws InterruptedException
     *             if interrupted while queuing or processing
     */
    public void addPostprocessedData(final PostProcessedDataContainer postprocessedData) throws InterruptedException
    {
        addPostprocessedData(postprocessedData, true);
    }

    /**
     * Delivers a parsed chunk of data and puts it through the statistics processors.
     * Also streams the parsed data records into ChunkDB ingestion collector if active.
     *
     * @param postprocessedData
     *            the post-processed data records container
     * @param isLastSubchunk
     *            whether this is the final subchunk for the raw read chunk (triggers finishedProcessing)
     * @throws InterruptedException
     *             if interrupted while queuing or processing
     */
    public void addPostprocessedData(final PostProcessedDataContainer postprocessedData, final boolean isLastSubchunk) throws InterruptedException
    {
        if (chunkCollector != null)
        {
            chunkCollector.collect(postprocessedData);
        }
        else
        {
            statisticsProcessor.process(postprocessedData);
        }
        if (isLastSubchunk)
        {
            finishedProcessing();
        }
    }

    /**
     * Indicates that a chunk has finished processing
     */
    private void finishedProcessing()
    {
        openDataChunkCount.decrement();
    }

    /**
     * Waits until data record processing is complete. Called by the main thread.
     *
     * @throws InterruptedException
     */
    public void waitForDataRecordProcessingToComplete() throws InterruptedException
    {
        // wait for the readers to complete
        remainingDirectories.awaitZero();

        // wait for the data processor thread to finish data record chunks
        openDataChunkCount.awaitZero();

        // stop progress
        progressBar.close();
    }

    /**
     * Return the number of remaining directories
     *
     * @return remaining directory to be processed
     */
    public int getRemainingDirectoryCount()
    {
        return remainingDirectories.get();
    }

    /**
     * Return the number of remaining or processed directory
     *
     * @return total number of processed or to be processed directory
     */
    public int getTotalDirectoryCount()
    {
        return totalDirectories.get();
    }
}
