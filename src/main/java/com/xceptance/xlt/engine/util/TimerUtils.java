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
package com.xceptance.xlt.engine.util;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * Provides functionality to measure <em>elapsed</em> time. This class uses either a high-precision timer (aka
 * {@link System#nanoTime()}) or the standard-precision timer (aka {@link System#currentTimeMillis()}). The
 * high-precision timer should be preferred as it is not affected by system time corrections (which might cause
 * inaccurate/negative elapsed time values). However, it might be slightly more expensive on certain operating systems.
 */
public final class TimerUtils
{
    /**
     * Indicates whether or not to use the high-precision timer.
     */
    private static boolean highPrecisionTimer = XltProperties.getInstance().getProperty("com.xceptance.xlt.useHighPrecisionTimer", true);

    /**
     * Returns whether or not the high-precision timer is used.
     * 
     * @return the high-precision timer flag
     */
    public static boolean isHighPrecisionTimerUsed()
    {
        return highPrecisionTimer;
    }

    /**
     * Sets whether or not the high-precision timer is used.
     * 
     * @param flag
     *            the high-precision timer flag
     */
    public static void setUseHighPrecisionTimer(final boolean flag)
    {
        highPrecisionTimer = flag;
    }

    /**
     * Returns the current value of the system timer, in milliseconds.
     * 
     * @return the current value of the system timer, in milliseconds
     */
    public static long getTime()
    {
        return highPrecisionTimer ? System.nanoTime() / 1000000L : System.currentTimeMillis();
    }
}
