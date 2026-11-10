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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.common.io.fs.VirtualFileSystem;
import com.xceptance.common.util.SynchronizingCounter;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;

/**
 * Regression tests for C3: preventing permanent pipeline deadlocks when {@link DataParserThread},
 * {@link Dispatcher}, or {@link DataReaderThread} encounter runtime exceptions or thread interrupts.
 */
public class DispatcherPipelineSafetyTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private static ReportGeneratorConfiguration createConfig(final int queueLength, final int bucketSize) throws IOException
    {
        final Properties props = new Properties();
        props.setProperty("com.xceptance.xlt.reportgenerator.queue.length", Integer.toString(queueLength));
        props.setProperty("com.xceptance.xlt.reportgenerator.queue.bucketsize", Integer.toString(bucketSize));
        return new ReportGeneratorConfiguration(null, null, props);
    }

    /**
     * C3.1: If an unhandled {@link RuntimeException} occurs during {@link Dispatcher#addPostprocessedData}
     * (or post-parse record handling in {@link DataParserThread}), {@code openDataChunkCount} must still be
     * decremented and {@code actionNamesCounter} must be notified so {@link Dispatcher#waitForDataRecordProcessingToComplete()}
     * does not hang forever, and the parser thread must survive to process subsequent chunks.
     */
    @Test
    public void testParserExceptionDuringPostprocessedDeliveryDoesNotDeadlock() throws Exception
    {
        final ReportGeneratorConfiguration config = createConfig(10, 10);

        final AtomicBoolean failFirstChunk = new AtomicBoolean(true);
        final CountDownLatch secondChunkProcessed = new CountDownLatch(1);

        final Dispatcher dispatcher = new Dispatcher(config, null)
        {
            @Override
            public void addPostprocessedData(final PostProcessedDataContainer postprocessedData, final boolean isLastSubchunk)
                throws InterruptedException
            {
                if (failFirstChunk.compareAndSet(true, false))
                {
                    throw new RuntimeException("Simulated downstream collector failure on chunk 1");
                }
                super.addPostprocessedData(postprocessedData, isLastSubchunk);
                if (isLastSubchunk)
                {
                    secondChunkProcessed.countDown();
                }
            }
        };

        final Map<String, Class<? extends Data>> dataClasses = Map.of(
            "R", RequestData.class,
            "A", ActionData.class
        );
        final DataRecordFactory factory = new DataRecordFactory(dataClasses);
        final DataParserThread parser = new DataParserThread(dispatcher, factory, 0L, Long.MAX_VALUE, config);
        final Thread parserThread = new Thread(parser, "Test-DataParser");
        parserThread.setDaemon(true);
        parserThread.start();

        try
        {
            final byte[] validActionLine =
                "A,OpenHomepage,1700000000000,120,false".getBytes(StandardCharsets.UTF_8);
            final byte[] validRequestLine =
                "R,Homepage,1700000000000,120,false,100,500,200,https://example.com/,text/html,10,5,5,20,80,0,req1,GET,,0,0,127.0.0.1,resp1,127.0.0.1"
                    .getBytes(StandardCharsets.UTF_8);

            final SynchronizingCounter actionCounter1 = new SynchronizingCounter(1);
            // Include both non-request ('A') and request ('R') lines in chunk1 so addPostprocessedData
            // fails on the first subchunk (isLastSubchunk == false) before the second subchunk is reached
            final DataChunk chunk1 = new DataChunk(
                List.of(validActionLine, validRequestLine), 1, null, "agent1", "TTest", "0",
                false, false, new ConcurrentSkipListMap<>(), actionCounter1);

            final DataChunk chunk2 = new DataChunk(
                Collections.singletonList(validRequestLine), 3, null, "agent1", "TTest", "0",
                false, false, new ConcurrentSkipListMap<>(), null);

            dispatcher.addReadData(chunk1);
            dispatcher.addReadData(chunk2);

            final AtomicBoolean waiterCompleted = new AtomicBoolean(false);
            final Thread waiterThread = new Thread(() -> {
                try
                {
                    dispatcher.waitForDataRecordProcessingToComplete();
                    waiterCompleted.set(true);
                }
                catch (final InterruptedException ignored)
                {
                }
            }, "Test-DispatcherWaiter");
            waiterThread.setDaemon(true);
            waiterThread.start();

            waiterThread.join(2000);
            if (waiterThread.isAlive())
            {
                waiterThread.interrupt();
                Assert.fail("Dispatcher.waitForDataRecordProcessingToComplete() deadlocked after RuntimeException in chunk processing");
            }

            Assert.assertTrue("Waiter must complete cleanly", waiterCompleted.get());
            Assert.assertEquals("actionNamesCounter must be decremented even on failure", 0, actionCounter1.get());
            Assert.assertTrue("Parser thread must survive and process chunk 2",
                              secondChunkProcessed.await(1, TimeUnit.SECONDS));
        }
        finally
        {
            parserThread.interrupt();
        }
    }

    /**
     * C3.2: If {@link Dispatcher#addReadData(DataChunk)} is interrupted while blocked on a full
     * {@code readDataQueue}, {@code openDataChunkCount} must be rolled back so it does not leak +1.
     */
    @Test
    public void testInterruptedAddReadDataDoesNotLeakOpenDataChunkCount() throws Exception
    {
        final ReportGeneratorConfiguration config = createConfig(1, 10);

        final Dispatcher dispatcher = new Dispatcher(config, null);
        final DataChunk chunk1 = new DataChunk(
            List.of(), 1, null, "agent1", "TTest", "0", false, false, new ConcurrentSkipListMap<>());
        final DataChunk chunk2 = new DataChunk(
            List.of(), 2, null, "agent1", "TTest", "0", false, false, new ConcurrentSkipListMap<>());

        // Fill the 1-slot queue
        dispatcher.addReadData(chunk1);

        // Attempt to enqueue a second chunk with interrupt flag set -> put() throws InterruptedException immediately
        Thread.currentThread().interrupt();
        try
        {
            dispatcher.addReadData(chunk2);
            Assert.fail("Expected InterruptedException when queue is full and thread is interrupted");
        }
        catch (final InterruptedException expected)
        {
            // Clear interrupt flag on test thread
            Thread.interrupted();
        }

        // Consume and finish the first (and only enqueued) chunk
        final DataChunk retrieved = dispatcher.retrieveReadData();
        Assert.assertSame(chunk1, retrieved);
        dispatcher.addPostprocessedData(new PostProcessedDataContainer(1), true);

        // Now waitForDataRecordProcessingToComplete() must return immediately because no chunks remain
        final AtomicBoolean completed = new AtomicBoolean(false);
        final Thread waiter = new Thread(() -> {
            try
            {
                dispatcher.waitForDataRecordProcessingToComplete();
                completed.set(true);
            }
            catch (final InterruptedException ignored)
            {
            }
        }, "Test-QueueWaiter");
        waiter.setDaemon(true);
        waiter.start();
        waiter.join(1500);

        if (waiter.isAlive())
        {
            waiter.interrupt();
            Assert.fail("openDataChunkCount leaked +1 when addReadData() threw InterruptedException");
        }
        Assert.assertTrue(completed.get());
    }

    /**
     * C3.3: If {@link DataReaderThread} is interrupted while blocked in {@link Dispatcher#addReadData(DataChunk)}
     * for a user directory that also contains client-performance timer files ({@code timer-wd-*.csv}),
     * it must not swallow {@link InterruptedException} and deadlock on {@code actionNamesCounter.awaitZero()}.
     */
    @Test
    public void testInterruptedDataReaderWithClientPerformanceFilesDoesNotDeadlock() throws Exception
    {
        final File userDir = tempFolder.newFolder("agent1", "TTest", "0");
        final File regularTimers = new File(userDir, "timers.csv");
        final File wdTimers = new File(userDir, "timer-wd-1.csv");

        // Write 4 lines to timers.csv with chunkSize = 2 so the reader enqueues 2 chunks
        final String line = "A,OpenPage,1700000000000,50,false\n";
        Files.writeString(regularTimers.toPath(), line + line + line + line, StandardCharsets.UTF_8);
        Files.writeString(wdTimers.toPath(), line, StandardCharsets.UTF_8);

        final ReportGeneratorConfiguration config = createConfig(1, 2);

        final Dispatcher dispatcher = new Dispatcher(config, null);
        dispatcher.incremementDirectoryCount();

        try (final VirtualFileSystem vfs = VirtualFileSystem.open(userDir))
        {
            final DataReaderThread reader = new DataReaderThread(
                vfs.getRoot(), "agent1", "TTest", "0", new AtomicLong(), dispatcher);

            final Thread readerThread = new Thread(reader, "Test-DataReader");
            readerThread.setDaemon(true);
            readerThread.start();

            // Wait until the first chunk is in the queue and the reader blocks trying to enqueue the second chunk
            final long deadline = System.currentTimeMillis() + 2000;
            while (readerThread.getState() != Thread.State.WAITING && System.currentTimeMillis() < deadline)
            {
                Thread.sleep(10);
            }

            // Interrupt the blocked reader thread
            readerThread.interrupt();
            readerThread.join(2000);

            if (readerThread.isAlive())
            {
                Assert.fail("DataReaderThread deadlocked on actionNamesCounter.awaitZero() after InterruptedException");
            }

            Assert.assertEquals("DataReaderThread must still call dispatcher.finishedReading() on interrupt",
                                0, dispatcher.getRemainingDirectoryCount());
        }
    }
}
