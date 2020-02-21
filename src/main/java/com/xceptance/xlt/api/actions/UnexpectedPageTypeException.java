package com.xceptance.xlt.api.actions;

/**
 * An exception to indicate that the wrong page type was loaded and the conversion to an XML or HTML page would fail.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class UnexpectedPageTypeException extends Exception
{
    /**
     * To satisfy Java
     */
    private static final long serialVersionUID = -8147092739069610752L;

    /**
     * Constructor.
     * 
     * @param msg
     *            the message to use
     */
    public UnexpectedPageTypeException(final String msg)
    {
        super(msg);
    }

    /**
     * Constructor.
     * 
     * @param msg
     *            the message to use
     * @param e
     *            the previous exception
     */
    public UnexpectedPageTypeException(final String msg, final Throwable e)
    {
        super(msg, e);
    }

    /**
     * Constructor.
     * 
     * @param e
     *            The previous exception
     */
    public UnexpectedPageTypeException(final Throwable e)
    {
        super(e);
    }
}
