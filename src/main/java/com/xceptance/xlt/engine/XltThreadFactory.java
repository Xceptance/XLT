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
package com.xceptance.xlt.engine;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nullable;

/**
 * A {@link ThreadFactory} that creates either virtual or platform daemon threads.
 */
public class XltThreadFactory implements ThreadFactory
{
    private static final String DEFAULT_THREAD_NAME_PREFIX = "XltThread-";

    /**
     * The underlying thread factory that does the hard work.
     */
    private final ThreadFactory threadFactory;

    /**
     * Creates a thread factory creating daemon threads that have the default name prefix and don't inherit inheritable
     * thread-locals.
     * 
     * @param createVirtualThreads
     *            whether to create virtual threads instead of platform threads
     */
    public XltThreadFactory(boolean createVirtualThreads)
    {
        this(createVirtualThreads, false);
    }

    /**
     * Creates a thread factory creating daemon threads that have the default name prefix.
     * 
     * @param createVirtualThreads
     *            whether to create virtual threads instead of platform threads
     * @param inheritInheritableThreadLocals
     *            whether or not the threads will inherit inheritable thread-locals
     */
    public XltThreadFactory(boolean createVirtualThreads, final boolean inheritInheritableThreadLocals)
    {
        this(createVirtualThreads, inheritInheritableThreadLocals, DEFAULT_THREAD_NAME_PREFIX);
    }

    /**
     * Creates a thread factory creating daemon threads.
     * 
     * @param createVirtualThreads
     *            whether to create virtual threads instead of platform threads
     * @param inheritInheritableThreadLocals
     *            whether or not the threads will inherit inheritable thread-locals
     * @param threadNamePrefix
     *            an optional thread name prefix (a counter will be appended to it)
     */
    public XltThreadFactory(boolean createVirtualThreads, final boolean inheritInheritableThreadLocals,
                            @Nullable final String threadNamePrefix)
    {
        // set up the thread builder
        final Thread.Builder threadBuilder = createVirtualThreads ? Thread.ofVirtual() : Thread.ofPlatform().daemon();
        threadBuilder.inheritInheritableThreadLocals(inheritInheritableThreadLocals);
        threadBuilder.name(Objects.toString(threadNamePrefix, DEFAULT_THREAD_NAME_PREFIX), 0);

        // create a thread-safe thread factory from the thread builder
        threadFactory = threadBuilder.factory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(final Runnable r)
    {
        return threadFactory.newThread(r);
    }
}
