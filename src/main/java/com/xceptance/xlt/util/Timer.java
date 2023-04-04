package com.xceptance.xlt.util;

public class Timer
{
    private final long ms;
    
    private long stoppedMs; 
                    
    private boolean stopped = false;
    
    private Timer()
    {
        ms = System.currentTimeMillis();
    }
    
    public static Timer start()
    {
        return new Timer();
    }
    
    public Timer stop()
    {
        stoppedMs = System.currentTimeMillis();
        
        if (stopped)
        {
            throw new IllegalArgumentException("Stopped called twice");
        }
        
        stopped = true;
        
        return this;
    }
    
    public long runtimeMillis()
    {
        return stoppedMs - ms;
    }
    
    public String get(final String msg)
    {
        return String.format(msg + " - %,d ms", runtimeMillis());
    }
}
