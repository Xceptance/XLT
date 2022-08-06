package com.xceptance.xlt.api.engine;

import static org.junit.Assert.assertNotNull;

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
    
    /**
     * Get the global clock to use. Enforces that we set it explicitly to avoid
     * mysterious test outcomes.
     * 
     * @return
     */
    public static GlobalClock currentClock()
    {
        if (clock == null)
        {
            // if we have not set it, something is wrong
            assertNotNull("Clock has not been set, something is wrong", clock);
        }
        
        return clock;
    }
}
