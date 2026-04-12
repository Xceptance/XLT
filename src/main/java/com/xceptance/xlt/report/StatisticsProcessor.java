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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;

/**
 * Processes chunks of parsed data concurrently across all registered report providers.
 * <p>
 * <b>Architecture & Runtime Advantages:</b>
 * <ul>
 *   <li><b>Concurrent Fan-Out:</b> Instead of sequentially calling {@code processAll()} on each provider (which takes 
 *       {@code O(sum(provider_times))}), the chunk is broadcasted to all providers simultaneously, bounding 
 *       overall latency to {@code O(max(provider_times))}.</li>
 *   <li><b>Thread Confined Actor Model (Lock-Free):</b> Each {@link ReportProvider} is wrapped in a dedicated 
 *       {@link ProviderActor} powered by a Virtual Thread. This creates an Actor model where a provider only 
 *       ever mutates its state from its own dedicated thread. As a result, the internal data structures of the 
 *       providers (like HashMaps, counters, and rolling averages) require <b>zero synchronization or locking</b>.</li>
 *   <li><b>Virtual Threads (JDK 21):</b> Using {@code Thread.ofVirtual()} eliminates OS-level thread allocation 
 *       overhead and context-switching penalties. We can effortlessly spawn a virtual thread for each provider 
 *       without consuming high heap memory for thread stacks.</li>
 *   <li><b>Memory Synchronization:</b> The {@link CountDownLatch} guarantees that the producer thread perfectly 
 *       waits for all actors to finish the current chunk before allowing the chunk to be cleared/reused, 
 *       acting as an implicit memory barrier and backpressure mechanism.</li>
 * </ul>
 * <p>
 * <p>
 * <b>Threading contract:</b> The {@link #process(PostProcessedDataContainer)} method is called concurrently
 * by multiple {@link DataParserThread} instances. The `CountDownLatch` provides the happens-before guarantee for
 * each chunk processing, while the `updateLock` synchronizes the updates of the global time boundaries.
 */
class StatisticsProcessor
{
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor.class);

    private final ReentrantLock updateLock = new ReentrantLock();

    /**
     * The maximum time across all processed data chunks.
     */
    private long maximumTime = 0;

    /**
     * The minimum time across all processed data chunks.
     */
    private long minimumTime = Long.MAX_VALUE;

    private final List<ProviderActor> actors;

    public StatisticsProcessor(final List<ReportProvider> reportProviders)
    {
        this.actors = new ArrayList<>(reportProviders.size());
        for (final ReportProvider p : reportProviders)
        {
            this.actors.add(new ProviderActor(p));
        }
    }

    public long getMaximumTime()
    {
        updateLock.lock();
        try
        {
            return maximumTime;
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public long getMinimumTime()
    {
        updateLock.lock();
        try
        {
            return (minimumTime == Long.MAX_VALUE) ? 0 : minimumTime;
        }
        finally
        {
            updateLock.unlock();
        }
    }

    /**
     * Broadcasts the given data container to all provider actors and waits for all of
     * them to complete before updating the global time boundaries. The latch.await()
     * provides the happens-before guarantee between the actor threads and this thread.
     * <p>
     * <b>Chunk Size Note:</b> The size of the incoming {@code dataContainer} is dynamic. Typically, it
     * starts as a chunk of 200 raw text lines (defined by {@code Dispatcher.DEFAULT_QUEUE_CHUNK_SIZE} /
     * {@code config.threadQueueBucketSize}). The parser thread processes these lines and drops any 
     * unparseable or irrelevant lines, so the actual number of data records in the container is 
     * at most equal to the queue bucket size, but often slightly smaller.
     *
     * @param dataContainer
     *            the container of parsed data records to broadcast
     * @throws InterruptedException if the current thread is interrupted while waiting for actors
     */
    public void process(final PostProcessedDataContainer dataContainer) throws InterruptedException
    {
        if (dataContainer.isEmpty())
        {
            return;
        }

        // Create a CountdownLatch designed to wait exactly for the number of ProviderActors we have.
        // This ensures the current parser thread will not pull its next chunk until *all* 
        // report providers have completely finished processing this current chunk.
        final CountDownLatch latch = new CountDownLatch(actors.size());
        
        // Wrap the payload with the synchronization latch
        final ChunkMessage msg = new ChunkMessage(dataContainer, latch);

        // Fan-out the chunk to every active ReportProvider actor.
        // Each actor has its own dedicated Virtual Thread that processes from an ArrayBlockingQueue.
        for (final ProviderActor actor : actors)
        {
            actor.put(msg);
        }

        // Update the global time boundaries in the meantime
        updateLock.lock();
        try
        {
            minimumTime = Math.min(minimumTime, dataContainer.getMinimumTime());
            maximumTime = Math.max(maximumTime, dataContainer.getMaximumTime());
        }
        finally
        {
            updateLock.unlock();
        }

        // Block the calling DataParserThread until every ProviderActor has called countDown()
        // on the latch (which they do specifically in a `finally` block after processing).
        // This acts as absolute backpressure, preventing parser threads from flooding memory 
        // with parsed objects if providers fall behind.
        latch.await();
    }


    private static class ChunkMessage
    {
        final PostProcessedDataContainer dataContainer;
        final CountDownLatch latch;

        ChunkMessage(final PostProcessedDataContainer dataContainer, final CountDownLatch latch)
        {
            this.dataContainer = dataContainer;
            this.latch = latch;
        }
    }

    private static class ProviderActor
    {
        private final ReportProvider provider;
        private final ArrayBlockingQueue<ChunkMessage> queue;

        ProviderActor(final ReportProvider provider)
        {
            this.provider = provider;
            this.queue = new ArrayBlockingQueue<>(10);
            Thread.ofVirtual().name("ReportProviderActor-" + provider.getClass().getSimpleName())
                  .start(this::runLoop);
        }

        private void runLoop()
        {
            try
            {
                while (true)
                {
                    final ChunkMessage msg = queue.take();
                    try
                    {
                        provider.processAll(msg.dataContainer);
                    }
                    catch (final Throwable t)
                    {
                        LOG.error("Failed to process data record in " + provider.getClass().getSimpleName(), t);
                    }
                    finally
                    {
                        msg.latch.countDown();
                    }
                }
            }
            catch (final InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }

        void put(final ChunkMessage msg) throws InterruptedException
        {
            queue.put(msg);
        }
    }
}
