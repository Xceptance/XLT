package com.xceptance.xlt.engine.scripting;

/**
 * Thrown if page load was not complete within a certain time period.
 */
public class PageLoadTimeoutException extends ScriptException
{
    /**
     * Constructor.
     */
    public PageLoadTimeoutException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public PageLoadTimeoutException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public PageLoadTimeoutException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public PageLoadTimeoutException(final Throwable cause)
    {
        super(cause);
    }
}
