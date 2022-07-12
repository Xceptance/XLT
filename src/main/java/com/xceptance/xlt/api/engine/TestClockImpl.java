package com.xceptance.xlt.api.engine;

/**
 * This is a clock for testing purposes you can control from the outside. Not suitable
 * for anything else.
 */
public class TestClockImpl extends GlobalClock
{
    // current test time
    private volatile long time = 0;
    
    /**
     * The one and only instance.
     */
    private static final TestClockImpl singleton = new TestClockImpl();

    /**
     * Returns the GlobalClock singleton.
     * 
     * @return the global clock
     */
    public static TestClockImpl getInstance()
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
        return time;
    }
    
    /**
     * Set the time for testing purposes
     */
    public void setTime(long time)
    {
        this.time = time;
    }
}
