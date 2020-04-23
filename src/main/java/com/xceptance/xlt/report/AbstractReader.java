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

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.xceptance.common.util.SynchronizingCounter;
import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.engine.util.TimerUtils;

public abstract class AbstractReader<T>
{
    /**
     * The maximum size of a data record chunk.
     */
    private static final int MAX_CHUNK_SIZE = 1000;

    /**
     * The thread that calculates the statistics.
     */
    private final Thread processorThread;

    /**
     * Current list of chunks.
     */
    private List<T> workList = new ArrayList<T>(MAX_CHUNK_SIZE);

    /**
     * The queue of parsed data record chunks.
     */
    private final ArrayBlockingQueue<List<T>> parsedDataRecordChunkQueue = new ArrayBlockingQueue<List<T>>(100);

    /**
     * The queue of preprocessed data record chunks.
     */
    private final ArrayBlockingQueue<List<T>> preprocessedDataRecordChunkQueue = new ArrayBlockingQueue<List<T>>(100);

    /**
     * The executor dealing with the preprocessor threads.
     */
    private final ExecutorService preprocessorExecutor;

    /**
     * The number of data record chunks that still needs to be processed.
     */
    private final SynchronizingCounter chunksToBeProcessed = new SynchronizingCounter();

    /**
     * Number of read lines.
     */
    private int lineCount = 0;

    /**
     * Overall start time.
     */
    private long overallStartTime;

    /**
     * Time of the last {@link #read(BufferedReader)} execution.
     */
    private long readTime = 0;

    /**
     * Constructor.
     * 
     * @param processorThreadName
     *            If not <code>null</code> the processor thread will get this name.
     */
    public AbstractReader(final String processorThreadName)
    {
        this(processorThreadName, 1);
    }

    /**
     * Constructor.
     * 
     * @param processorThreadName
     *            If not <code>null</code> the processor thread will get this name.
     * @param preprocessorThreadCount
     *            the number of parallel preprocessor threads
     */
    public AbstractReader(final String processorThreadName, int preprocessorThreadCount)
    {
        // the one and only data processor thread
        processorThread = new Thread(new Processor());

        if (processorThreadName != null)
        {
            processorThread.setName(processorThreadName);
        }

        processorThread.setDaemon(true);
        processorThread.start();

        // the data preprocessor threads
        preprocessorExecutor = Executors.newFixedThreadPool(preprocessorThreadCount, new DaemonThreadFactory("DataRecordPreprocessor-"));
        for (int i = 0; i < preprocessorThreadCount; i++)
        {
            preprocessorExecutor.execute(new Preprocessor());
        }
    }

    /**
     * Read from the given reader line by line, processes the lines and collects the results.
     * 
     * @param bufferedReader
     *            reader
     * @throws Exception
     *             if something bad happens
     */
    public void read(final BufferedReader bufferedReader) throws Exception
    {
        final long startTime = TimerUtils.getTime();

        String line = bufferedReader.readLine();
        while (line  != null)
        {
            lineCount++;

            final T t = processLine(line);

            if (t != null)
            {
                addToChunk(t);
            }

            line = bufferedReader.readLine();
        }

        finishChunk();

        readTime = TimerUtils.getTime() - startTime;
    }

    /**
     * Performs any clean-up tasks.
     */
    public void cleanUp()
    {
        // interrupt the processor thread, hopefully it will leave its loop
        processorThread.interrupt();

        // shut the preprocessor threads down
        preprocessorExecutor.shutdownNow();
    }

    private void printOverallStatistics()
    {
        System.out.printf("Data records read: %,d (%,d ms)\n", getLineCount(), TimerUtils.getTime() - getOverallStartTime());
    }

    /**
     * Get the number of read lines.
     * 
     * @return the number of read lines
     */
    protected int getLineCount()
    {
        return lineCount;
    }

    /**
     * Get the time for the last {@link #read(BufferedReader)} execution.
     * 
     * @return the time for the last {@link #read(BufferedReader)} execution.
     */
    protected long getReadTime()
    {
        return readTime;
    }

    /**
     * Processes a read line.
     * 
     * @param line
     *            a line to process
     * @return processed line result
     */
    abstract protected T processLine(final String line);

    /**
     * Preprocesses a single line result. Note that this method is called concurrently by multiple threads.
     * 
     * @param t
     *            element to process
     */
    protected void preprocessLineResult(T t)
    {
    }

    /**
     * Process a single line result. Note that this method is called by a single thread only.
     * 
     * @param t
     *            element to process
     */
    abstract protected void processLineResult(T t);

    /**
     * Time right before the first read.
     * 
     * @param overallStartTime
     *            time right before the first read
     */
    protected void setOverallStartTime(final long overallStartTime)
    {
        this.overallStartTime = overallStartTime;
    }

    /**
     * Time right before the first read.
     * 
     * @return time right before the first read
     */
    protected long getOverallStartTime()
    {
        return overallStartTime;
    }

    /**
     * Waits for the data record processor thread to empty the data record chunk queue.
     * 
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    protected void waitForDataRecordProcessingToComplete() throws InterruptedException
    {
        chunksToBeProcessed.awaitZero();
        printOverallStatistics();
    }

    /**
     * Adds the given object to the current list of chunks.
     *
     * @param t
     *            the object to add
     * @throws Exception
     */
    protected void addToChunk(final T t) throws Exception
    {
        workList.add(t);

        if (workList.size() == MAX_CHUNK_SIZE)
        {
            addChunkToQueue();
        }
    }

    /**
     * Finishes the current list of chunks.
     *
     * @throws Exception
     */
    protected void finishChunk() throws Exception
    {
        if (!workList.isEmpty())
        {
            addChunkToQueue();
        }
    }

    /**
     * Adds the current list of chunks to the chunk queue and creates a new chunk worklist.
     *
     * @throws Exception
     */
    private void addChunkToQueue() throws Exception
    {
        chunksToBeProcessed.increment();

        enqueueItem(workList, parsedDataRecordChunkQueue, "parsedDataRecordChunkQueue");

        workList = new ArrayList<T>(MAX_CHUNK_SIZE);
    }

    /**
     * Preprocesses the data records. This is done concurrently with multiple threads.
     */
    private class Preprocessor implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    // get a chunk of data records
                    List<T> dataRecords = dequeueItem(parsedDataRecordChunkQueue, "parsedDataRecordChunkQueue");

                    // preprocess the chunk
                    final int length = dataRecords.size();
                    for (int i = 0; i < length; i++)
                    {
                        preprocessLineResult(dataRecords.get(i));
                    }

                    // put the chunk onto the out-queue
                    enqueueItem(dataRecords, preprocessedDataRecordChunkQueue, "preprocessedDataRecordChunkQueue");
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
    }

    /**
     * Processes the data records. This is done in a single thread.
     */
    private class Processor implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    // get a chunk of data records
                    List<T> dataRecords = dequeueItem(preprocessedDataRecordChunkQueue, "preprocessedDataRecordChunkQueue");

                    // process the chunk
                    final int length = dataRecords.size();
                    for (int i = 0; i < length; i++)
                    {
                        processLineResult(dataRecords.get(i));
                    }

                    // one more chunk is complete
                    chunksToBeProcessed.decrement();
                }
                catch (final InterruptedException e)
                {
                    break;
                }
            }
        }
    }

    private <E> void enqueueItem(E item, BlockingQueue<E> queue, String queueName) throws InterruptedException
    {
        // alternative debug code
        // if (!queue.offer(item))
        // {
        // System.out.printf("### %s\n", "Queue full - " + queueName);
        // queue.put(item);
        // }

        queue.put(item);
    }

    private <E> E dequeueItem(BlockingQueue<E> queue, String queueName) throws InterruptedException
    {
        // alternative debug code
        // E item;
        // if ((item = queue.poll()) == null)
        // {
        // System.out.printf("### %s\n", "Queue empty - " + queueName);
        // item = queue.take();
        // }
        //
        // return item;

        return queue.take();
    }
}
