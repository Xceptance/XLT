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
import java.util.concurrent.atomic.AtomicInteger;

public class XltThreadFactory implements ThreadFactory
{
    private final boolean useVirtualThreads;

    private final boolean inheritInheritableThreadLocals;

    private final String threadNamePrefix;

    private final AtomicInteger threadCounter = new AtomicInteger();

    public XltThreadFactory(final boolean inheritInheritableThreadLocals, final String threadNamePrefix)
    {
        useVirtualThreads = false;
        this.inheritInheritableThreadLocals = inheritInheritableThreadLocals;
        this.threadNamePrefix = Objects.toString(threadNamePrefix, "XltThread-");
    }

    @Override
    public Thread newThread(final Runnable r)
    {
        final Thread thread = createThread(r);

        final String threadName = threadNamePrefix + threadCounter.getAndIncrement();
        thread.setName(threadName);

        return thread;
    }

    private Thread createThread(final Runnable runnable)
    {
        final Thread thread;

        if (useVirtualThreads)
        {
            System.out.println("Creating virtual thread");

            thread = Thread.ofVirtual().inheritInheritableThreadLocals(false).unstarted(runnable);
        }
        else
        {
            System.out.println("Creating platform thread");

            thread = new Thread(null, runnable, "", 0, inheritInheritableThreadLocals);
            thread.setDaemon(true);
        }

        return thread;
    }
}
