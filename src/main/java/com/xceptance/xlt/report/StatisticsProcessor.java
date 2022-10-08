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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.report.ReportProvider;

/**
 * Processes parsed data records. Processing means passing a data record to all configured report providers. Since data
 * processing is not thread-safe (yet), there will be only one data processor.
 */
class StatisticsProcessor
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(StatisticsProcessor.class);

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
     * Take the post processed data and put it into the statitics machinery to
     * capture the final data points. Does use the same threads as before, not a different pool
     * hence better cache utilization.
     *
     * @param data a chunk of post processed data for final statitics gathering
     */
    public void process(final PostprocessedDataContainer dataContainer)
    {
        // it might be empty after filtered
        if (dataContainer.data.size() == 0)
        {
            return;
        }

        // get your own list
        final List<ReportProvider> providerList = new ArrayList<>(reportProviders);
        Collections.shuffle(providerList);

        // run as long as we have not all data put into the report providers
        while (providerList.isEmpty() == false)
        {
            ReportProvider provider = null;

            for (int i = 0; i < providerList.size(); i++)
            {
                final boolean wasLocked = providerList.get(i).lock();
                if (wasLocked)
                {
                    provider = providerList.remove(i);
                    break;
                }
            }

            if (provider == null)
            {
                // got none, give up the cpu
                Thread.yield();
                continue;
            }

            // we have one, we can process the data
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
        }

        // get the max and min
        synchronized (this)
        {
            minimumTime = Math.min(minimumTime, dataContainer.getMinimumTime());
            maximumTime = Math.max(maximumTime, dataContainer.getMaximumTime());
        }
    }
}
