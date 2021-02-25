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
package com.xceptance.xlt.report.util;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.xlt.util.ConcurrencyUtils;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

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
     * The current progress bar
     */
    private volatile ProgressBar progressBar;
    
    /**
     * Total task for that progress
     */
    private final AtomicInteger totalTasks = new AtomicInteger(0);
    
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
    public static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * Constructor.
     */
    private TaskManager()
    {
        init();
    }

    /**
     * Start a progress meter
     */
    public void startProgress(final String msg)
    {
        totalTasks.set(0);
        progressBar = new ProgressBarBuilder()
            .setTaskName(msg).setInitialMax(100).setStyle(ProgressBarStyle.ASCII).build();   
    }
    
    /**
     * Start a progress meter
     */
    public void stopProgress()
    {
        progressBar.close();
        totalTasks.set(0);
    }
    
    /**
     * Adds the given task to the to-do list.
     * 
     * @param task
     *            the task to execute
     */
    public void addTask(final Runnable task)
    {
        progressBar.maxHint(totalTasks.incrementAndGet());
        
        // wrap the task to allow for exception logging
        getExecutor().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    task.run();
                    progressBar.step();
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