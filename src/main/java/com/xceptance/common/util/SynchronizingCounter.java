/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.util.concurrent.CountDownLatch;

/**
 * A counter object which allows threads to wait until the counter value becomes zero. This class is similar to
 * {@link CountDownLatch}, but adds the possibility to change the counter value after construction time.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class SynchronizingCounter
{
    /**
     * The counter variable.
     */
    private int count;

    /**
     * Creates a new {@link SynchronizingCounter} object and initializes the counter with 0.
     */
    public SynchronizingCounter()
    {
        this(0);
    }

    /**
     * Creates a new {@link SynchronizingCounter} object and initializes the counter with the given value.
     * 
     * @param initialValue
     *            the counter value
     */
    public SynchronizingCounter(final int initialValue)
    {
        count = initialValue;
    }

    /**
     * Adds the given value (may be negative) to the counter value.
     * 
     * @param delta
     *            the value
     * @return incremented value
     */
    public synchronized int add(final int delta)
    {
        count = count + delta;

        check(count);

        return count;
    }

    /**
     * Waits until the counter has reached 0.
     * 
     * @throws InterruptedException
     *             if the thread has been interrupted
     */
    public synchronized void awaitZero() throws InterruptedException
    {
        while (count != 0)
        {
            wait();
        }
    }

    /**
     * Waits until either the counter has reached 0 or the specified timeout has elapsed.
     * 
     * @param timeout
     *            the maximum time to wait (in milliseconds; 0 means no timeout)
     * @throws InterruptedException
     *             if the thread has been interrupted
     */
    public synchronized void awaitZero(final long timeout) throws InterruptedException
    {
        // because we do not know why we have been awoken, we also have to check the timer!!!
        // otherwise we have to go to bed again
        final long wakeUpTime = System.currentTimeMillis() + timeout;

        while (count != 0 && (System.currentTimeMillis() < wakeUpTime))
        {
            wait(timeout);
        }
    }

    /**
     * Checks whether the counter is 0. If so, any thread waiting for the counter to become 0 is awaked.
     * 
     * @param value
     *            the value to be checked
     */
    protected synchronized void check(final int value)
    {
        if (value == 0)
        {
            notifyAll();
        }
    }

    /**
     * Decreases the counter value by 1.
     * 
     * @return decremented counter
     */
    public synchronized int decrement()
    {
        return add(-1);
    }

    /**
     * Returns the current counter value.
     * 
     * @return the counter value
     */
    public synchronized int get()
    {
        return count;
    }

    /**
     * Increases the counter value by 1.
     * 
     * @return incremented counter
     */
    public synchronized int increment()
    {
        return add(1);
    }

    /**
     * Sets the new counter value.
     * 
     * @param value
     *            the counter value
     */
    public synchronized void set(final int value)
    {
        count = value;
        check(value);
    }
}
