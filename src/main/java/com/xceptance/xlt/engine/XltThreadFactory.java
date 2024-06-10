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

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.SystemUtils;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.api.util.XltException;

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
        final String threadName = threadNamePrefix + threadCounter.getAndIncrement();

        final Thread thread = createThread(r);
        thread.setName(threadName);

        return thread;
    }

    private Thread createThread(final Runnable runnable)
    {
        final Thread thread;

        if (useVirtualThreads && SystemUtils.IS_JAVA_21)
        {
            System.out.println("Creating virtual thread");

            // This is what actually needs to be done here:
            // thread = Thread.ofVirtual().inheritInheritableThreadLocals(false).unstarted(this::run);

            // To make the above Java 21 code compile on Java 11, use reflection.
            try
            {
                // 1. get the virtual thread builder
                // final ThreadBuilder threadBuilder = Thread.ofVirtual();
                final Object threadBuilder = ReflectionUtils.callStaticMethod(Thread.class, "ofVirtual");

                // 2. set whether to inherit inheritable thread locals
                // threadBuilder.inheritInheritableThreadLocals(inheritInheritableThreadLocals);
                // ReflectionUtils.callMethod(threadBuilder, "inheritInheritableThreadLocals",
                // inheritInheritableThreadLocals);
                ReflectionUtils.getMethod(threadBuilder.getClass(), "inheritInheritableThreadLocals", boolean.class)
                               .invoke(threadBuilder, inheritInheritableThreadLocals);

                // 3. create the thread
                // threadBuilder.unstarted(this::run);
                // thread = (Thread) ReflectionUtils.callMethod(threadBuilder, "unstarted", runnable);
                thread = (Thread) ReflectionUtils.getMethod(threadBuilder.getClass(), "unstarted", Runnable.class).invoke(threadBuilder,
                                                                                                                          runnable);
            }
            catch (final SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                throw new XltException("Failed to create virtual thread", e);
            }
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
