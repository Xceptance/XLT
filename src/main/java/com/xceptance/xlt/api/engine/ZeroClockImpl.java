package com.xceptance.xlt.api.engine;

/**
 * This clock is meant to aid report creation be lowering the overhead. It will always
 * tell you 0 as time.
 */
public class ZeroClockImpl extends GlobalClock
{
    /**
     * The one and only instance.
     */
    private static final ZeroClockImpl singleton = new ZeroClockImpl();

    /**
     * Returns the GlobalClock singleton.
     * 
     * @return the global clock
     */
    public static ZeroClockImpl getInstance()
    {
        return singleton;
    }

    /**
     * Returns always 0
     * 
     * @return 0
     */
    public long getTime()
    {
        return 0;
    }
}
