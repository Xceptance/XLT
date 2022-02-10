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
package com.xceptance.common.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.xceptance.common.util.Getter;

/**
 * A {@link ThreadFactory} implementation that creates threads with the daemon flag set.
 */
public class DaemonThreadFactory implements ThreadFactory
{
    /**
     * The default thread factory which does the hard work.
     */
    private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

    /**
     * The thread name prefix.
     */
    private final String threadNamePrefix;

    /**
     * The number of threads created by this factory so far.
     */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * The thread name's prefix getter.
     */
    private final Getter<String> prefixGetter;

    /**
     * Constructor.
     */
    public DaemonThreadFactory()
    {
        this((String) null);
    }

    /**
     * Constructor.
     * 
     * @param threadNamePrefix
     *            the string to prefix the thread name with
     */
    public DaemonThreadFactory(final String threadNamePrefix)
    {
        this.threadNamePrefix = threadNamePrefix;
        prefixGetter = null;
    }

    /**
     * Constructor.
     * 
     * @param getter
     *            the thread name's prefix getter
     */
    public DaemonThreadFactory(final Getter<String> getter)
    {
        threadNamePrefix = null;
        prefixGetter = getter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(final Runnable runnable)
    {
        final Thread thread = defaultThreadFactory.newThread(runnable);
        thread.setDaemon(true);

        final String name = (threadNamePrefix == null) ? (prefixGetter != null ? prefixGetter.get() : "") : threadNamePrefix;
        thread.setName(name + count.getAndIncrement());

        return thread;
    }
}
