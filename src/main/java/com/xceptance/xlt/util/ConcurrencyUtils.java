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
