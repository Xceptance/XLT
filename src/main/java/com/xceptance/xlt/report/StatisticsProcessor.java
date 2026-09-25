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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;

/**
 * Processes parsed data records by delegating to worker copies of configured report providers per thread,
 * avoiding lock contention completely during processing, and merging them into the master providers when finished.
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
    private final AtomicLong maximumTime = new AtomicLong(0);

    /**
     * Creation time of first data record.
     */
    private final AtomicLong minimumTime = new AtomicLong(Long.MAX_VALUE);

    /**
     * The master configured report providers.
     */
    private final List<ReportProvider> reportProviders;

    /**
     * The report provider configuration used to configure worker providers.
     */
    private final ReportProviderConfiguration configuration;

    /**
     * All worker report provider lists instantiated for worker threads.
     */
    private final ConcurrentLinkedQueue<List<ReportProvider>> allWorkerProviders = new ConcurrentLinkedQueue<>();

    /**
     * Thread-local list of worker report providers.
     */
    private final ThreadLocal<List<ReportProvider>> threadLocalProviders;

    /**
     * Flag indicating whether merge completion has already been performed.
     */
    private final AtomicBoolean completed = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     */
    public StatisticsProcessor(final List<ReportProvider> reportProviders)
    {
        this(reportProviders, null);
    }

    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     * @param configuration
     *            the report provider configuration
     */
    public StatisticsProcessor(final List<ReportProvider> reportProviders, final ReportProviderConfiguration configuration)
    {
        // filter the list and take only the provider that really need runtime parsed data
        this.reportProviders = reportProviders.stream().filter(p -> p.wantsDataRecords()).collect(Collectors.toList());

        if (configuration != null)
        {
            this.configuration = configuration;
        }
        else if (!this.reportProviders.isEmpty() && this.reportProviders.get(0) instanceof AbstractReportProvider)
        {
            this.configuration = ((AbstractReportProvider) this.reportProviders.get(0)).getConfiguration();
        }
        else
        {
            this.configuration = null;
        }

        this.threadLocalProviders = ThreadLocal.withInitial(this::createWorkerProviders);
    }

    private List<ReportProvider> createWorkerProviders()
    {
        final List<ReportProvider> localList = new ArrayList<>(reportProviders.size());
        for (final ReportProvider master : reportProviders)
        {
            try
            {
                final ReportProvider worker = master.getClass().getDeclaredConstructor().newInstance();
                if (configuration != null)
                {
                    worker.setConfiguration(configuration);
                }
                else if (master instanceof AbstractReportProvider)
                {
                    worker.setConfiguration(((AbstractReportProvider) master).getConfiguration());
                }
                localList.add(worker);
            }
            catch (final Exception e)
            {
                LOG.error("Failed to instantiate worker report provider: " + master.getClass().getName(), e);
                throw new RuntimeException(e);
            }
        }
        allWorkerProviders.add(localList);
        return localList;
    }

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public long getMaximumTime()
    {
        return maximumTime.get();
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public long getMinimumTime()
    {
        final long min = minimumTime.get();
        return (min == Long.MAX_VALUE) ? 0 : min;
    }

    /**
     * Takes the post-processed data and puts it into the statistics machinery to capture the final data points.
     *
     * @param dataContainer
     *            a chunk of post-processed data for final statistics gathering
     */
    public void process(final PostProcessedDataContainer dataContainer)
    {
        // it might be empty after filtered
        if (dataContainer.data.size() == 0)
        {
            return;
        }

        final List<ReportProvider> localProviders = threadLocalProviders.get();

        for (int i = 0; i < localProviders.size(); i++)
        {
            try
            {
                localProviders.get(i).processAll(dataContainer);
            }
            catch (final Throwable t)
            {
                LOG.error("Failed to process data record in worker provider, discarding full chunk", t);
            }
        }

        // update max and min
        minimumTime.accumulateAndGet(dataContainer.getMinimumTime(), Math::min);
        maximumTime.accumulateAndGet(dataContainer.getMaximumTime(), Math::max);
    }

    /**
     * Merges all worker provider statistics into the master report providers.
     */
    public void complete()
    {
        if (completed.compareAndSet(false, true))
        {
            final List<List<ReportProvider>> workers = new ArrayList<>(allWorkerProviders);
            allWorkerProviders.clear();

            if (!workers.isEmpty())
            {
                IntStream.range(0, reportProviders.size()).parallel().forEach(i -> {
                    final ReportProvider master = reportProviders.get(i);
                    for (final List<ReportProvider> workerList : workers)
                    {
                        final ReportProvider worker = workerList.get(i);
                        master.merge(worker);
                    }
                });
            }
        }
    }

    /**
     * Returns the master report providers after ensuring all worker statistics have been merged.
     *
     * @return the report providers
     */
    public List<ReportProvider> getReportProviders()
    {
        complete();
        return reportProviders;
    }
}
