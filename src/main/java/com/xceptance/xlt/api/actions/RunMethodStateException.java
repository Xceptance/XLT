package com.xceptance.xlt.api.actions;

/**
 * This exception indicates, that the run() method of an {@link AbstractAction} has already been called.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class RunMethodStateException extends IllegalStateException
{
    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public RunMethodStateException()
    {
        super("run() method was already called. Can only be executed once.");
    }

    /**
     * Constructor.
     * 
     * @param msg
     *            message to report
     */
    public RunMethodStateException(final String msg)
    {
        super(msg);
    }
}
