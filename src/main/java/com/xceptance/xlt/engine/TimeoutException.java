package com.xceptance.xlt.engine;

/**
 * Thrown in case waiting for a condition to become true has timed out.
 */
public class TimeoutException extends RuntimeException
{
    /**
     * Default constructor.
     */
    public TimeoutException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     */
    public TimeoutException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause of this exception
     */
    public TimeoutException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause of this exception
     */
    public TimeoutException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
