package com.xceptance.xlt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.ReportProvider;

/**
 * Processes parsed data records. Processing means passing a data record to all configured report providers. Since data
 * processing is not thread-safe (yet), there will be only one data processor.
 */
class DataRecordProcessor implements Runnable
{
    /**
     * Class logger.
     */
    private static final Log LOG = LogFactory.getLog(LogReader.class);

    /**
     * The dispatcher that coordinates result processing.
     */
    private final Dispatcher dispatcher;

    /**
     * Creation time of last data record.
     */
    private long maximumTime;

    /**
     * Creation time of first data record.
     */
    private long minimumTime;

    /**
     * The configured report providers. An array for less overhead.
     */
    private final ReportProvider[] reportProviders;

    /**
     * Constructor.
     *
     * @param reportProviders
     *            the configured report providers
     * @param dispatcher
     *            the dispatcher that coordinates result processing
     */
    public DataRecordProcessor(final List<ReportProvider> reportProviders, final Dispatcher dispatcher)
    {
        this.reportProviders = reportProviders.toArray(new ReportProvider[0]);
        this.dispatcher = dispatcher;

        maximumTime = 0;
        minimumTime = Long.MAX_VALUE;
    }

    /**
     * Returns the maximum time.
     *
     * @return maximum time
     */
    public final long getMaximumTime()
    {
        return maximumTime;
    }

    /**
     * Returns the minimum time.
     *
     * @return minimum time
     */
    public final long getMinimumTime()
    {
        return (minimumTime == Long.MAX_VALUE) ? 0 : minimumTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        // just a few threads are good enough
        final ForkJoinPool pool = new ForkJoinPool(4);

        while (true)
        {
            try
            {
                // get a chunk of parsed data records
                final List<Data> dataRecords = dispatcher.getNextParsedDataRecordChunk();

                // submit this to all report providers and each provider does its own loop
                // we assume that they are independent of each other and hence this is ok
                final List<ForkJoinTask<?>> tasks = new ArrayList<>(reportProviders.length);
                for (int i = 0; i < reportProviders.length; i++)
                {
                    final ReportProvider reportProvider = reportProviders[i];

                    // give all data to each process threads for one report provider aka SIMD
                    // single instruction multiple data
                    final ForkJoinTask<?> task = pool.submit(() -> {
                        processDataRecords(reportProvider, dataRecords);
                    });
                    tasks.add(task);
                }

                maintainStatistics(dataRecords);

                // wait for completion
                tasks.forEach(t -> t.quietlyJoin());

                // one more chunk is complete
                dispatcher.finishedProcessing();
            }
            catch (final InterruptedException e)
            {
                break;
            }
        }

        // clean up
        pool.shutdown();
        try
        {
            // that should not be necessary, but for the argument of it
            pool.awaitTermination(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e1)
        {
        }
    }

    /**
     * Processes the given data records by passing them to a report provider.
     *
     * @param reportProvider
     *            the report provider
     * @param data
     *            the data records
     */
    private void processDataRecords(final ReportProvider reportProvider, final List<Data> data)
    {
        // process the data
        final int size = data.size();
        for (int i = 0; i < size; i++)
        {
            try
            {
                reportProvider.processDataRecord(data.get(i));
            }
            catch (final Throwable t)
            {
                LOG.warn("Failed to process data record", t);
                System.err.println("\nFailed to process data record: " + t);
            }
        }
    }

    /**
     * Maintain our statistics
     *
     * @param data
     *            the data records
     */
    private void maintainStatistics(final List<Data> data)
    {
        long min = minimumTime;
        long max = maximumTime;

        // process the data
        final int size = data.size();
        for (int i = 0; i < size; i++)
        {
            // maintain statistics
            final long time = data.get(i).getTime();

            min = Math.min(min, time);
            max = Math.max(max, time);
        }

        minimumTime = Math.min(minimumTime, min);
        maximumTime = Math.max(maximumTime, max);
    }
}
