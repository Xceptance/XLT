package com.xceptance.xlt.api.engine;

import com.xceptance.xlt.engine.GlobalClockImpl;

/**
 * <p>
 * The GlobalClock provides the current time in the test cluster. Depending on the configuration, the GlobalClock uses
 * either the master controller's time as the reference time or the local system time (the default).
 * </p>
 * <p>
 * Sometimes the local system clocks of the test machines diverge significantly. This may lead to unexpected results in
 * the test report. There are two ways to get around this:
 * <ol>
 * <li>Install an NTP client on all test machines which synchronizes the local time with a time server. This is the
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
     * The one and only instance.
     */
    private static final GlobalClock singleton = new GlobalClockImpl();

    /**
     * Returns the GlobalClock singleton.
     * 
     * @return the global clock
     */
    public static GlobalClock getInstance()
    {
        return singleton;
    }

    /**
     * Returns the current time as a number of milliseconds elapsed since January 1st, 1970 GMT.
     * 
     * @return the time
     */
    public abstract long getTime();
}
