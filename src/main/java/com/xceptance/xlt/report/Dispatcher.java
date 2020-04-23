/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import com.xceptance.common.util.SynchronizingCounter;
import com.xceptance.xlt.api.engine.Data;

/**
 * The {@link Dispatcher} is responsible to coordinate the various reader/parser/processor threads involved when
 * processing test results. It does not only pass the results from one thread to another, but makes sure as well that no
 * more than X threads are active at the same time.
 *
 * @see DataRecordReader
 * @see DataRecordParser
 * @see DataRecordProcessor
 */
public class Dispatcher
{
    /**
     * The number of directories that still need to be processed.
     */
    private final SynchronizingCounter directoriesToBeProcessed;

    /**
     * The number of chunks that still need to be processed.
     */
    private final SynchronizingCounter chunksToBeProcessed;

    /**
     * The semaphore to limit the number of active threads.
     */
    private final Semaphore permits;

    /**
     * The line chunks waiting to be parsed.
     */
    private final BlockingQueue<LineChunk> lineChunkQueue;

    /**
     * The data record chunks waiting to be processed.
     */
    private final BlockingQueue<List<Data>> dataRecordChunkQueue;

    /**
     * Creates a new {@link Dispatcher} object with the given thread limit.
     *
     * @param directoriesToBeProcessed
     *            the number of directories that need to be processed
     * @param maxActiveThreads
     *            the maximum number of active threads
     */
    public Dispatcher(final SynchronizingCounter directoriesToBeProcessed, final int maxActiveThreads)
    {
        this.directoriesToBeProcessed = directoriesToBeProcessed;

        permits = new Semaphore(maxActiveThreads);
        chunksToBeProcessed = new SynchronizingCounter();
        lineChunkQueue = new ArrayBlockingQueue<LineChunk>(10);
        dataRecordChunkQueue = new ArrayBlockingQueue<List<Data>>(10);
    }

    /**
     * Indicates that a reader thread is about to begin reading. Called by a reader thread.
     */
    public void beginReading() throws InterruptedException
    {
        permits.acquire();
    }

    /**
     * Adds a new chunk of lines for further processing. Called by a reader thread.
     *
     * @param lineChunk
     *            the line chunk
     */
    public void addNewLineChunk(final LineChunk lineChunk) throws InterruptedException
    {
        chunksToBeProcessed.increment();

        permits.release();
        lineChunkQueue.put(lineChunk);
        permits.acquire();
    }

    /**
     * Indicates that a reader thread has finished reading. Called by a reader thread.
     */
    public void finishedReading()
    {
        directoriesToBeProcessed.decrement();

        permits.release();
    }

    /**
     * Returns a chunk of lines for further processing. Called by a parser thread.
     *
     * @return the line chunk
     */
    public LineChunk getNextLineChunk() throws InterruptedException
    {
        final LineChunk chunk = lineChunkQueue.take();
        permits.acquire();

        return chunk;
    }

    /**
     * Adds a new chunk of parsed data records for further processing. Called by a parser thread.
     *
     * @param dataRecordChunk
     *            the data record chunk
     */
    public void addNewParsedDataRecordChunk(final List<Data> dataRecordChunk) throws InterruptedException
    {
        permits.release();
        dataRecordChunkQueue.put(dataRecordChunk);
    }

    /**
     * Returns a chunk of parsed data records for further processing. Called by a processor thread.
     *
     * @return the data record chunk
     */
    public List<Data> getNextParsedDataRecordChunk() throws InterruptedException
    {
        final List<Data> chunk = dataRecordChunkQueue.take();
        permits.acquire();

        return chunk;
    }

    /**
     * Indicates that a processor thread has finished processing. Called by a processor thread.
     */
    public void finishedProcessing()
    {
        chunksToBeProcessed.decrement();

        permits.release();
    }

    /**
     * Waits until data record processing is complete. Called by the main thread.
     *
     * @throws InterruptedException
     */
    public void waitForDataRecordProcessingToComplete() throws InterruptedException
    {
        // wait for the readers to complete
        directoriesToBeProcessed.awaitZero();

        // wait for the data processor thread to finish data record chunks
        chunksToBeProcessed.awaitZero();
    }
}
