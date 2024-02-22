/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
import java.util.function.IntFunction;

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
     * The number of threads created by this factory so far.
     */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * Default name generator if none is set
     */
    private static final IntFunction<String> DEFAULT_NAME_GENERATOR = i -> "Thread-" + i;

    /**
     * The thread name's prefix getter.
     */
    private final IntFunction<String> nameGenerator;

    /**
     * Priority to set
     */
    private final int priority;

    /**
     * Constructor.
     */
    public DaemonThreadFactory()
    {
        this(DEFAULT_NAME_GENERATOR, Thread.NORM_PRIORITY);
    }

    /**
     * Constructor.
     * 
     * @param nameGenerator
     *            a name generator that is given an int and it shall return a name
     * @param priority
     *            the priority for the threads to create
     */
    public DaemonThreadFactory(final IntFunction<String> nameGenerator, final int priority)
    {
        this.nameGenerator = nameGenerator == null ? DEFAULT_NAME_GENERATOR : nameGenerator;
        this.priority = priority;
    }

    /**
     * Constructor.
     * 
     * @param nameGenerator
     *            a name generator that is given an int and it shall return a name
     */
    public DaemonThreadFactory(final IntFunction<String> nameGenerator)
    {
        this(nameGenerator, Thread.NORM_PRIORITY);
    }

    /**
     * Constructor.
     * 
     * @param threadNamePrefix
     *            the string to prefix the thread name with
     */
    public DaemonThreadFactory(final String threadNamePrefix)
    {
        this(i -> threadNamePrefix + i, Thread.NORM_PRIORITY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(final Runnable runnable)
    {
        final Thread thread = defaultThreadFactory.newThread(runnable);
        thread.setDaemon(true);
        thread.setPriority(priority);

        thread.setName(nameGenerator.apply(count.getAndIncrement()));

        return thread;
    }
}
