package com.xceptance.common.util;

import java.util.Timer;

/**
 * Utility class for timed operations.
 */
public final class TimerUtils
{
    /**
     * The one and only global {@link Timer} instance.
     */
    private static final Timer timer = new Timer("GeneralPurposeTimer", true);

    /**
     * Returns the global/shared {@link Timer} instance.
     * 
     * @return the timer
     */
    public static Timer getTimer()
    {
        return timer;
    }

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private TimerUtils()
    {
    }
}
