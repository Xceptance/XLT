package com.xceptance.common.lang;

/**
 * Utility class that provides convenient methods regarding threads.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public final class ThreadUtils
{
    /**
     * Suspends the current thread forever.
     */
    public static void sleep()
    {
        // sleep "forever"
        sleep(Long.MAX_VALUE);
    }

    /**
     * Suspends the current thread for the specified milliseconds. This method silently ignores any InterruptedException
     * that may happen. Does not sleep if 0 or a negative number is passed.
     * 
     * @param millis
     *            the time to sleep
     */
    public static void sleep(final long millis)
    {
        // do not sleep if not needed
        if (millis > 0)
        {
            try
            {
                Thread.sleep(millis);
            }
            catch (final InterruptedException ex)
            {
                // ignore
            }
        }
    }

    /**
     * Checks if the current thread was interrupted. If so, an {@link InterruptedException} will be thrown.
     * 
     * @throws InterruptedException
     *             thrown if the current thread was interrupted
     */
    public static void checkIfInterrupted() throws InterruptedException
    {
        if (Thread.interrupted())
        {
            throw new InterruptedException();
        }
    }

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ThreadUtils()
    {
    }
}
