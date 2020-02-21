package com.xceptance.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * Utility class that provides convenient methods regarding ordinary java objects.
 */
public final class ProcessUtils
{
    /**
     * 
     */
    private static final long startTime;

    /**
     * Class constructor.
     */
    static
    {
        // initialize startTime using the current uptime
        final long now = System.nanoTime() / 1000000;
        startTime = now - ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ProcessUtils()
    {
    }

    /**
     * Returns the uptime of the current process. This method is intended to replace {@link RuntimeMXBean#getUptime()}
     * as that method uses {@link System#currentTimeMillis()}, which is affected by time corrections.
     * 
     * @return the uptime [ms]
     */
    public static long getUptime()
    {
        final long now = System.nanoTime() / 1000000;
        return now - startTime;
    }
}
