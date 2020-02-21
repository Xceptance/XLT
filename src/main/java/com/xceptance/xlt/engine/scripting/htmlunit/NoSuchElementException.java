package com.xceptance.xlt.engine.scripting.htmlunit;

import com.xceptance.xlt.api.util.XltException;

/**
 * Exception indicating an element lookup miss.
 */
public class NoSuchElementException extends XltException
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchElementException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's description
     */
    public NoSuchElementException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the exception's cause
     */
    public NoSuchElementException(final Throwable cause)
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
    public NoSuchElementException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
