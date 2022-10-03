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
package com.xceptance.xlt.engine.util;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * Provides functionality to measure <em>elapsed</em> time. This class uses either a high-precision timer (aka
 * {@link System#nanoTime()}) or the standard-precision timer (aka {@link System#currentTimeMillis()}). The
 * high-precision timer should be preferred as it is not affected by system time corrections (which might cause
 * inaccurate/negative elapsed time values). However, it might be slightly more expensive on certain operating systems.
 *
 * Don't use this class as source for the system time!
 */
public abstract class TimerUtils
{
    private static final TimerUtils HPT = new HighPrecisionTimerUtils();
    private static final TimerUtils LPT = new LowPrecisionTimerUtils();

    /**
     * Indicates whether or not to use the high-precision timer.
     */
    static final class LocalTimerUtils extends TimerUtils
    {
        static final TimerUtils DEFAULT = new LocalTimerUtils();

        /**
         * Our default time instance running based on required precision
         */
        private final TimerUtils instance;

        /**
         * Creates an instance for safe multi-threaded use
         */
        private LocalTimerUtils()
        {
            instance = XltProperties.getInstance().getProperty("com.xceptance.xlt.useHighPrecisionTimer", true) ? HPT : LPT;
        }

        @Override
        public boolean isHighPrecision()
        {
            return instance.isHighPrecision();
        }

        @Override
        public long getStartTime()
        {
            return instance.getStartTime();
        }

        @Override
        public long getElapsedTime(final long startTime)
        {
            return instance.getElapsedTime(startTime);
        }

    }

    /**
     * The current instance of the timer
     *
     * @return timer according to the property com.xceptance.xlt.useHighPrecisionTimer
     */
    public static TimerUtils get()
    {
        return LocalTimerUtils.DEFAULT;
    }

    /**
     * Returns whether or not the high-precision timer is used.
     *
     * @return the high-precision timer flag
     */
    public abstract boolean isHighPrecision();

    /**
     * Returns the current value. This can be absolute or relative (nanotime).
     * This is not a safe source of system time! Use GlobalClock instead.
     *
     * @return the current value of the system timer
     */
    public abstract long getStartTime();

    /**
     * Returns the difference in msec
     *
     * @param startTime previous time
     * @return the difference between current time and supplied time
     */
    public abstract long getElapsedTime(final long startTime);

    /**
     * Returns a high precision timer. If the VM does not support it, this will
     * be automatically low precision.
     */
    public static TimerUtils getHighPrecisionTimer()
    {
        return HPT;
    }

    /**
     * Returns a low precision timer. This is the ms version of things
     */
    public static TimerUtils getLowPrecisionTimer()
    {
        return LPT;
    }

    /**
     * The high precision version running in nsec
     * if supported by the VM
     *
     * @author rschwietzke
     */
    static final class HighPrecisionTimerUtils extends TimerUtils
    {
        @Override
        public boolean isHighPrecision()
        {
            return true;
        }

        @Override
        public long getStartTime()
        {
            return System.nanoTime();
        }

        @Override
        public long getElapsedTime(final long startTime)
        {
            return (System.nanoTime() - startTime) / 1_000_000L;
        }

    }

    /**
     * The low precision version, running in msec
     *
     * @author rschwietzke
     */
    static final class LowPrecisionTimerUtils extends TimerUtils
    {
        @Override
        public boolean isHighPrecision()
        {
            return false;
        }

        @Override
        public long getStartTime()
        {
            return System.currentTimeMillis();
        }

        @Override
        public long getElapsedTime(final long startTime)
        {
            return System.currentTimeMillis() - startTime;
        }

    }
}
