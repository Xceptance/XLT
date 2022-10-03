/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.xceptance.xlt.engine.util.TimerUtils;

/**
 * The {@link PageStatistics} class holds some statistics generated while loading the current page. Currently, this
 * includes:
 * <ul>
 * <li>the number of bytes really loaded - this includes HTML, CSS, JS, and images</li>
 * <li>the total number of bytes, either really loaded or returned from the cache</li>
 * </ul>
 * There is exactly one {@link PageStatistics} instances for each test user thread (and its helper threads).
 */
public final class PageStatistics
{
    /**
     * Maintains the page statistics instances keyed by the thread group.
     */
    private static final Map<ThreadGroup, PageStatistics> pageStatistics = new ConcurrentHashMap<ThreadGroup, PageStatistics>();

    /**
     * Returns the {@link PageStatistics} instance responsible for the current test user.
     *
     * @return the page statistics
     */
    public static PageStatistics getPageStatistics()
    {
        final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

        PageStatistics stats = null;
        synchronized (threadGroup)
        {
            stats = pageStatistics.get(threadGroup);
            if (stats == null)
            {
                stats = new PageStatistics();
                pageStatistics.put(threadGroup, stats);
            }
        }

        return stats;
    }

    /**
     * The really loaded number of bytes.
     */
    private final AtomicLong bytes = new AtomicLong();

    /**
     * The total number of bytes, including cached resources.
     */
    private final AtomicLong totalBytes = new AtomicLong();

    /**
     * The total number of milliseconds needed to load the page including static content (maybe cached).
     */
    private final AtomicLong loadTime = new AtomicLong();

    /**
     * Flag which indicates whether or not loading a new page has finished.
     */
    private final AtomicBoolean pageLoaded = new AtomicBoolean(Boolean.FALSE);

    /**
     * Constructor.
     */
    private PageStatistics()
    {
    }

    /**
     * Adds the given number of bytes to the loaded bytes.
     *
     * @param bytes
     *            the number of bytes
     */
    public void addToBytes(final long bytes)
    {
        this.bytes.addAndGet(bytes);
    }

    /**
     * Adds the given number of bytes to the total bytes.
     *
     * @param bytes
     *            the number of bytes
     */
    public void addToTotalBytes(final long bytes)
    {
        totalBytes.addAndGet(bytes);
    }

    /**
     * Returns the number of bytes loaded.
     *
     * @return the number of bytes
     */
    public long getBytes()
    {
        return bytes.get();
    }

    /**
     * Returns the total number of bytes.
     *
     * @return the number of bytes
     */
    public long getTotalBytes()
    {
        return totalBytes.get();
    }

    /**
     * Clears any values.
     */
    protected void reset()
    {
        bytes.set(0);
        totalBytes.set(0);
        loadTime.set(0);
        pageLoaded.set(false);
    }

    /**
     * Called when web client has started to load a new page.
     */
    public void pageLoadStarted()
    {
        reset();
        loadTime.set(TimerUtils.get().getStartTime());
    }

    /**
     * Called when web client has finished to load a new page.
     */
    public void pageLoadFinished()
    {
        loadTime.set(TimerUtils.get().getElapsedTime(loadTime.get()));
        pageLoaded.set(true);
    }

    /**
     * Returns the load time.
     *
     * @return load time in milliseconds
     */
    public long getLoadTime()
    {
        if (pageLoaded.get())
        {
            return loadTime.get();
        }
        return 0L;
    }
}
