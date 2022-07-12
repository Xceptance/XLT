package com.xceptance.xlt.api.engine;

import com.xceptance.xlt.engine.GlobalClockImpl;

/**
 * Takes care of the right clock for the right purpose. When it runs as a load test, we have a real clock here. 
 * If we run this as a report generator, we have a zero clock impl to offer
 * 
 * @author rschwietzke
 */
public class ClockSwitcher
{
    private static volatile GlobalClock clock; 
    
    public static void init(final GlobalClock clock)
    {
        ClockSwitcher.clock = clock;
    }
    
    public static GlobalClock currentClock()
    {
        return clock == null ? new GlobalClockImpl() : clock;
    }
}
