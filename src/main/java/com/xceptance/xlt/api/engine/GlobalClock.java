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
package com.xceptance.xlt.api.engine;

/**
 * <p>
 * The GlobalClock provides the current time in the test cluster. Depending on the configuration, the GlobalClock uses
 * either the master controller's time as the reference time or the local system time (the default).
 * </p>
 * <p>
 * Sometimes the local system clocks of the test machines diverge significantly. This may lead to unexpected results in
 * the test report. There are two ways to get around this:
 * <ol>
 * <li>Install a NTP client on all test machines which synchronizes the local time with a time server. This is the
 * preferred solution.</li>
 * <li>Use the time of one machine (in this case the master controller's machine) as the reference time. All timestamps
 * are created relative to this reference time.</li>
 * </ol>
 * If the latter approach is used, one needs to give the system the chance to correct the local time. So, avoid using
 * <code>System.currentTimeMillis()</code> in favor of <code>GlobalClock.getInstance().getTime()</code>.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public abstract class GlobalClock
{
    /**
     * The one and only instance. This instance might be a zero clock impl for the purpose of a quick
     * and efficient report rendering. This should be safe, because the agents are independent from the master
     * and the report generator
     */
    private static class InstanceHolder 
    {
        private static final GlobalClock singleton = ClockSwitcher.currentClock();
    }

    /**
     * Returns the GlobalClock singleton.
     * 
     * @return the global clock
     */
    public static GlobalClock getInstance()
    {
        return InstanceHolder.singleton;
    }

    /**
     * Returns the current time as a number of milliseconds elapsed since January 1st, 1970 GMT.
     * 
     * @return the time
     */
    public abstract long getTime();
}
