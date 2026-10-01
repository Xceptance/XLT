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

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.api.util.SimpleArrayList;

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
     * Internal container managing worker provider instances for a single processing thread.
     * Provides pre-filtered arrays indexed by character type code (e.g. 'R', 'T', 'A', 'C', 'E')
     * so that incoming columnar data chunks only dispatch to providers that actually consume that record type.
     */
    private static final class WorkerProviderSet
    {
        /** The complete list of worker providers instantiated for this thread. */
        final List<ReportProvider> allProviders;

        /** Lookup table of providers filtered by ASCII type code (e.g. ['R'], ['T'], ['A']). */
        final ReportProvider[][] providersByTypeCode;

        WorkerProviderSet(final List<ReportProvider> allProviders)
        {
            this.allProviders = allProviders;
            this.providersByTypeCode = new ReportProvider[128][];

            // Pre-filter providers for each ASCII character code
            for (char c = 0; c < 128; c++)
            {
                final List<ReportProvider> matching = new ArrayList<>();
                for (final ReportProvider p : allProviders)
                {
                    if (p.acceptsType(c))
                    {
                        matching.add(p);
                    }
                }
                this.providersByTypeCode[c] = matching.toArray(new ReportProvider[0]);
            }
        }
    }

    /**
     * Thread-local worker provider sets maintaining per-thread instances and type routing tables.
     */
    private final ThreadLocal<WorkerProviderSet> threadLocalProviderSet;

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

        this.threadLocalProviderSet = ThreadLocal.withInitial(this::createWorkerProviderSet);
    }

    /**
     * Instantiates isolated worker report provider instances for the current worker thread,
     * pre-configuring them and cataloging them by accepted record type code.
     *
     * @return the initialized {@link WorkerProviderSet}
     */
    private WorkerProviderSet createWorkerProviderSet()
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
        return new WorkerProviderSet(localList);
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
     * Takes the post-processed data and puts it into the statistics machinery to capture the final data points,
     * dispatching to all registered report providers.
     *
     * @param dataContainer
     *            a chunk of post-processed data for final statistics gathering
     */
    public void process(final PostProcessedDataContainer dataContainer)
    {
        process(dataContainer, dataContainer.typeCode);
    }

    /**
     * Takes the post-processed data and puts it into the statistics machinery, routing it specifically
     * to the report providers interested in the given record type code (e.g. 'R' for HTTP Requests,
     * 'T' for Transactions, 'A' for Actions).
     * <p>
     * If {@code typeCode} is non-zero, this lookup executes in O(1) against a precomputed array of providers,
     * completely eliminating redundant iterations and {@code instanceof} checks across uninterested providers.
     *
     * @param dataContainer
     *            a chunk of post-processed data for final statistics gathering
     * @param typeCode
     *            the data record type code ('R', 'T', 'A', 'C', 'E', 'P', 'W', 'J'), or 0 to dispatch to all
     */
    public void process(final PostProcessedDataContainer dataContainer, final char typeCode)
    {
        // it might be empty after filtered
        if (dataContainer.data.size() == 0)
        {
            return;
        }

        final WorkerProviderSet providerSet = threadLocalProviderSet.get();
        final ReportProvider[] providers = (typeCode > 0 && typeCode < 128)
            ? providerSet.providersByTypeCode[typeCode]
            : null;

        if (providers != null)
        {
            for (int i = 0; i < providers.length; i++)
            {
                try
                {
                    providers[i].processAll(dataContainer);
                }
                catch (final Throwable t)
                {
                    LOG.error("Failed to process data record in worker provider, discarding full chunk", t);
                }
            }
        }
        else
        {
            // Fallback for mixed or untyped chunks (such as heterogeneous CSV log chunks).
            // Instead of dispatching the entire container to all providers (which would force every provider
            // to redundantly scan all records), we inspect the type code of each record and dispatch it directly
            // to only the providers that are registered to accept that record's type.
            final SimpleArrayList<Data> list = dataContainer.dataList;
            final Object[] array = list.getInternalArray();
            final int size = list.size();
            final ReportProvider[][] byTypeCode = providerSet.providersByTypeCode;
            final List<ReportProvider> all = providerSet.allProviders;

            for (int p = 0; p < size; p++)
            {
                final Data data = (Data) array[p];
                final char tc = data.getTypeCode();
                final ReportProvider[] relevantProviders = (tc < 128) ? byTypeCode[tc] : null;

                if (relevantProviders != null)
                {
                    for (int i = 0; i < relevantProviders.length; i++)
                    {
                        try
                        {
                            relevantProviders[i].processDataRecord(data);
                        }
                        catch (final Throwable t)
                        {
                            LOG.error("Failed to process data record in worker provider", t);
                        }
                    }
                }
                else
                {
                    for (int i = 0; i < all.size(); i++)
                    {
                        try
                        {
                            all.get(i).processDataRecord(data);
                        }
                        catch (final Throwable t)
                        {
                            LOG.error("Failed to process data record in worker provider", t);
                        }
                    }
                }
            }

            // Compensate for sampling loss if lines were dropped during parsing
            if (dataContainer.droppedLines > 0)
            {
                int droppedLines = dataContainer.droppedLines;
                final int sampleFactor = dataContainer.sampleFactor;
                for (int p = 0; p < size; p++)
                {
                    final Data data = (Data) array[p];
                    if (!(data instanceof TransactionData))
                    {
                        final char tc = data.getTypeCode();
                        final ReportProvider[] relevantProviders = (tc < 128) ? byTypeCode[tc] : null;
                        for (int y = 1; y < sampleFactor; y++)
                        {
                            if (relevantProviders != null)
                            {
                                for (int i = 0; i < relevantProviders.length; i++)
                                {
                                    relevantProviders[i].processDataRecord(data);
                                }
                            }
                            else
                            {
                                for (int i = 0; i < all.size(); i++)
                                {
                                    all.get(i).processDataRecord(data);
                                }
                            }
                        }
                        droppedLines--;
                        if (droppedLines == 0)
                        {
                            break;
                        }
                    }
                }
            }
        }

        // update max and min
        minimumTime.accumulateAndGet(dataContainer.getMinimumTime(), Math::min);
        maximumTime.accumulateAndGet(dataContainer.getMaximumTime(), Math::max);
    }

    /**
     * Updates global minimum and maximum timestamps directly without requiring per-record inspection.
     *
     * @param min
     *            chunk or batch minimum timestamp
     * @param max
     *            chunk or batch maximum timestamp
     */
    public void updateMinMax(final long min, final long max)
    {
        minimumTime.accumulateAndGet(min, Math::min);
        maximumTime.accumulateAndGet(max, Math::max);
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
