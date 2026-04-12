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

class StatisticsProcessor
{
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor.class);

    private final ReentrantLock updateLock = new ReentrantLock();

    private long maximumTime = 0;
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

    public void process(final PostProcessedDataContainer dataContainer)
    {
        if (dataContainer.isEmpty())
        {
            return;
        }

        final CountDownLatch latch = new CountDownLatch(actors.size());
        final ChunkMessage msg = new ChunkMessage(dataContainer, latch);

        for (final ProviderActor actor : actors)
        {
            try
            {
                actor.put(msg);
            }
            catch (final InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return;
        }

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
