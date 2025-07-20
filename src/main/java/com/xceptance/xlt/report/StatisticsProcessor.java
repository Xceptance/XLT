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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.util.XltLogger;

/**
 * Processes parsed data records. Processing means passing a data record to all configured report providers. Since data
 * processing is not thread-safe (yet), there will be only one statistics processor.
 */
class StatisticsProcessor
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor.class);

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
    private final List<ReportProvider> reportProviders;

    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     */
    public StatisticsProcessor(final List<ReportProvider> reportProviders)
    {
        // filter the list and take only the provider that really need runtime parsed data
        this.reportProviders = reportProviders.stream().filter(p -> p.wantsDataRecords()).collect(Collectors.toList());
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
        
        final List<Thread> virtualThreads = new ArrayList<>();
        for (final ReportProvider provider : this.reportProviders)
        {
            final Thread virtualThread = Thread.ofVirtual().start(() -> 
            {
                provider.lock();
                try
                {
                    provider.processAll(dataContainer);
                }
                catch (final Throwable t)
                {
                    LOG.error("Failed to process data record, discarding full chunk", t);
                }
                finally
                {
                    provider.unlock();
                }                
            });
            
            virtualThreads.add(virtualThread);
        }

        // wait for completion of all virtual threads
        for (Thread thread : virtualThreads) 
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                // that should not happen, but we have to check anyway
                XltLogger.reportLogger.error("Interrupted while waiting for report provider thread to finish", e);
            }
        }

        // get the max and min
        updateLock.lock();
        {
            minimumTime = Math.min(minimumTime, dataContainer.getMinimumTime());
            maximumTime = Math.max(maximumTime, dataContainer.getMaximumTime());
        }
        updateLock.unlock();
    }
}
