/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.util;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter with knowledge about its last modification and ability for periodical reset.
 */
public class TimedCounter
{
    /**
     * The counter.
     */
    private final AtomicInteger count;

    /**
     * Creates new counter with initial value <code>0</code>. This counter does NOT has a reset functionality.
     */
    public TimedCounter()
    {
        this(0);
    }

    /**
     * Creates new counter with initial value <code>0</code> and periodical reset functionality.
     *
     * @param resetInterval
     *            the period for counter reset in milliseconds (must be greater than 0).
     */
    public TimedCounter(final long resetInterval)
    {
        this.count = new AtomicInteger(0);

        if (resetInterval > 0)
        {
            new CounterResetTask(this, resetInterval);
        }
    }

    /**
     * Gets the current count.
     *
     * @return the current count
     */
    public int get()
    {
        return count.get();
    }

    /**
     * Increments the counter.
     */
    public void increment()
    {
        count.incrementAndGet();
    }

    /**
     * Sets the counter to the given value.
     *
     * @param value
     *            the value to set
     */
    public void set(final int value)
    {
        count.set(value);
    }

    /**
     * Resets the counter.
     */
    public void reset()
    {
        set(0);
    }

    /**
     * Counter reset task.
     * <p>
     * This task maintains a weak reference to the counter to reset as well as the reset interval. In case the reference
     * to the counter has gone, it won't re-schedule itself for execution any longer.
     * </p>
     */
    private static class CounterResetTask extends TimerTask
    {
        /**
         * The global reset timer responsible for execution of all reset tasks.
         */
        private static final Timer RESET_TIMER = new Timer("TimedCounter-ResetTimer");

        /**
         * The counter to reset.
         */
        private final WeakReference<TimedCounter> counterRef;

        /**
         * Creates a new counter reset task and schedules it for execution.
         *
         * @param counter
         *            the counter to reset
         * @param aInterval
         *            the counter's reset interval
         */
        private CounterResetTask(final TimedCounter counter, final long aInterval)
        {
            counterRef = new WeakReference<>(counter);

            RESET_TIMER.schedule(this, aInterval, aInterval);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            final TimedCounter counter = counterRef.get();
            if (counter != null)
            {
                counter.reset();
            }
            else
            {
                cancel();
            }
        }
    }
}
