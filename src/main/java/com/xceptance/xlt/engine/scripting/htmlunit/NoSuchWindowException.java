package com.xceptance.xlt.engine.scripting.htmlunit;

import com.xceptance.xlt.api.util.XltException;

/**
 * Exception indicating a window lookup miss.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class NoSuchWindowException extends XltException
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchWindowException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's description
     */
    public NoSuchWindowException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the exception's cause
     */
    public NoSuchWindowException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's description
     * @param cause
     *            the exception's cause
     */
    public NoSuchWindowException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
