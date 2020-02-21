package com.xceptance.xlt.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.xceptance.common.util.concurrent.DaemonThreadFactory;

public class ConcurrencyUtils
{
    /**
     * Create a new cached thread pool.
     * 
     * @param namePrefix
     *            thread name prefix
     * @return the newly created thread pool
     * @see {@link Executors#newCachedThreadPool()}
     */
    public static ThreadPoolExecutor getNewThreadPoolExecutor(final String namePrefix)
    {
        return getNewThreadPoolExecutor(namePrefix, -1);
    }
    
    /**
     * Create a new thread pool.
     * 
     * @param namePrefix
     *            thread name prefix
     * @param size
     *            the created thread pool has a fix size if the given size is larger than <code>0</code>. It's a cached
     *            thread pool otherwise.
     * @return the newly created thread pool
     * @see {@link Executors#newFixedThreadPool(int)}
     * @see {@link Executors#newCachedThreadPool()}
     */
    public static ThreadPoolExecutor getNewThreadPoolExecutor(final String namePrefix, final int size)
    {
        final DaemonThreadFactory dtf = namePrefix != null ? new DaemonThreadFactory(namePrefix) : new DaemonThreadFactory();
        return (ThreadPoolExecutor) ((size > 0) ? Executors.newFixedThreadPool(size, dtf) : Executors.newCachedThreadPool(dtf));
    }
}
