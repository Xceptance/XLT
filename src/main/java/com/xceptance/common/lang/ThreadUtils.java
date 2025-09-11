/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.lang;

/**
 * Utility class that provides convenient methods regarding threads.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public final class ThreadUtils
{
    /**
     * Suspends the current thread forever.
     */
    public static void sleep()
    {
        // sleep "forever"
        sleep(Long.MAX_VALUE);
    }

    /**
     * Suspends the current thread for the specified milliseconds. This method silently ignores any InterruptedException
     * that may happen. Does not sleep if 0 or a negative number is passed.
     * 
     * @param millis
     *            the time to sleep
     */
    public static void sleep(final long millis)
    {
        // do not sleep if not needed
        if (millis > 0)
        {
            try
            {
                Thread.sleep(millis);
            }
            catch (final InterruptedException ex)
            {
                // ignore
            }
        }
    }

    /**
     * Checks if the current thread was interrupted. If so, an {@link InterruptedException} will be thrown.
     * 
     * @throws InterruptedException
     *             thrown if the current thread was interrupted
     */
    public static void checkIfInterrupted() throws InterruptedException
    {
        if (Thread.interrupted())
        {
            throw new InterruptedException();
        }
    }

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ThreadUtils()
    {
    }
}
