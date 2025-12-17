/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;

/**
 * Processes parsed data records. Processing means passing a data record to all configured report providers. Since data
 * processing is not thread-safe (yet), there will be only one statistics processor.
 */
class StatisticsProcessor2
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor2.class);

    /**
     * The update lock
     */
    private final ReentrantLock updateLock = new ReentrantLock();

    /**
     * Creation time of last data record.
     */
    private long maximumTime = 0;

    /**
     * Creation time of first data record.
     */
    private long minimumTime = Long.MAX_VALUE;

    /**
     * The configured report providers. An array for less overhead.
     */
    private final List<ReportProviderPool> reportProviderExecutors;

    /**
     * Our pool of report providers with their own executors. This isolates the report providers from each other
     * in the sense of one thread one report provider. No synchronization is needed anymore inside the report providers.
     */
    private record ReportProviderPool(ExecutorService executor, ReportProvider provider) {};

    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     */
    public StatisticsProcessor2(final List<ReportProvider> reportProviders)
    {
        // filter the list and take only the provider that really need runtime parsed data
        this.reportProviderExecutors = reportProviders.
            stream().
            filter(p -> p.wantsDataRecords())
            .map(r -> new ReportProviderPool(Executors.newSingleThreadExecutor(), r)).toList();
    }

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public synchronized long getMaximumTime()
    {
        return maximumTime;
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public synchronized long getMinimumTime()
    {
        return (minimumTime == Long.MAX_VALUE) ? 0 : minimumTime;
    }

    /**
     * Takes the post-processed data and puts it into the statistics machinery to capture the final data points.
     *
     * @param data
     *            a chunk of post-processed data for final statistics gathering
     */
    public void process(final PostProcessedDataContainer dataContainer)
    {
        // it might be empty after filtered
        if (dataContainer.data.size() == 0)
        {
            return;
        }

        // each provider is its own pool, so there is no external synchronization needed
        // anymore, submit to all pools at once, the incoming data is read only and will be
        // synchronized when the thread starts
        final var futures = reportProviderExecutors
            .stream()
            .map(r -> r.executor.submit(() -> r.provider.processAll(dataContainer)))
            .toList();
        
        // get the max and min, update only when needed
        updateLock.lock();
        if (dataContainer.getMinimumTime() < minimumTime)
        {
            minimumTime = dataContainer.getMinimumTime();
        }
        if (dataContainer.getMaximumTime() > maximumTime)
        {
            maximumTime = dataContainer.getMaximumTime();
        }
        updateLock.unlock();

        // ok, wait till we finished in all pools
        futures.forEach(f -> {
            try
            {
                f.get();
            }
            catch (InterruptedException e)
            {
                LOG.warn("Request statistics process was interrupted", e);
            }
            catch (ExecutionException e)
            {
                LOG.error("Failed to process data record, discarding full chunk", e.getCause());
            }
        });
    }
}
