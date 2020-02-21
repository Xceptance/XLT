package com.xceptance.xlt.report.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.util.ConcurrencyUtils;

/**
 * A simple manager for asynchronous tasks.
 */
public class TaskManager
{
    /**
     * The {@link TaskManager} singleton holder.
     */
    private static class SingletonHolder
    {
        private static final TaskManager instance = new TaskManager();
    }

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(TaskManager.class);

    /**
     * The interval [ms] between checks whether all tasks are complete.
     */
    private static final long INTERVAL = 500;

    /**
     * Returns the {@link TaskManager} singleton.
     * 
     * @return the singleton instance
     */
    public static TaskManager getInstance()
    {
        return SingletonHolder.instance;
    }

    /**
     * The underlying thread pool.
     */
    private ThreadPoolExecutor executor;

    /**
     * The default maximum count of threads, which is equal to the number of available CPUs on the current machine.
     */
    public static final int DEFAULT_THREAD_COUNT = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();

    /**
     * Constructor.
     */
    private TaskManager()
    {
        init();
    }

    /**
     * Adds the given task to the to-do list.
     * 
     * @param task
     *            the task to execute
     */
    public void addTask(final Runnable task)
    {
        // wrap the task to allow for exception logging
        getExecutor().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    task.run();
                }
                catch (Exception e)
                {
                    log.error("Failed to execute task", e);
                }
            }
        });
    }

    /**
     * Returns the maximum number of threads to use.
     * 
     * @return the maximum number of threads to use
     */
    public int getMaximumThreadCount()
    {
        return getExecutor().getMaximumPoolSize();
    }

    /**
     * Sets the maximum number of threads to use.
     * 
     * @param threads
     *            the maximum number of threads to use
     */
    public synchronized void setMaximumThreadCount(int threads)
    {
        if (threads < 1)
        {
            threads = DEFAULT_THREAD_COUNT;
        }

        final int curCoreSize = executor.getCorePoolSize();

        /*
         * When adjusting any of the pool sizes (core or max) the following conditions must be satisfied: 0 <= core-size
         * AND 0 < max-size >= core-size
         */

        // first, check if we decrease core pool size
        // -> if so, adjust it BEFORE tuning the max-size
        if (threads < curCoreSize)
        {
            executor.setCorePoolSize(threads);
        }
        // now, adjust the maximum pool size
        executor.setMaximumPoolSize(threads);

        // and finally, check if we increase the core pool size
        // -> if so, adjust it AFTER max-size has been set
        if (threads > curCoreSize)
        {
            executor.setCorePoolSize(threads);
        }
    }

    /**
     * Blocks until all previously added tasks are complete.
     * 
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public synchronized void waitForAllTasksToComplete() throws InterruptedException
    {
        executor.shutdown();
        try
        {
            while (!executor.isTerminated())
            {
                Thread.sleep(INTERVAL);
            }
        }
        finally
        {
            init();
        }
    }

    /**
     * (Re)Initializes the thread pool executor.
     */
    private void init()
    {
        executor = ConcurrencyUtils.getNewThreadPoolExecutor("ReportGenerator-pool-", DEFAULT_THREAD_COUNT);
    }

    /**
     * Returns the thread pool executor.
     * 
     * @return thread pool executor
     */
    private synchronized ThreadPoolExecutor getExecutor()
    {
        return executor;
    }
}
