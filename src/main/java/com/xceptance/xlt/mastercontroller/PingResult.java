package com.xceptance.xlt.mastercontroller;

/**
 * Agent controller's ping result.
 */
public class PingResult
{
    /**
     * Agent controller's status.
     */
    private final Long pingTime;

    /**
     * Exception if occurred.
     */
    private final Exception exception;

    /**
     * @param status
     *            agent controller's status
     */
    public PingResult(final long pingTime)
    {
        this(pingTime, null);
    }

    /**
     * @param exception
     *            exception if occurred
     */
    public PingResult(final Exception exception)
    {
        this(null, exception);
    }

    private PingResult(final Long pingTime, final Exception exception)
    {
        this.pingTime = pingTime;
        this.exception = exception;
    }

    /**
     * Get the agent controller's last known status.
     * 
     * @return the agent controller's last known status
     */
    public long getPingTime()
    {
        return pingTime;
    }

    /**
     * Get the exception that occurred.
     * 
     * @return the exception that occurred
     */
    public Exception getException()
    {
        return exception;
    }
}
