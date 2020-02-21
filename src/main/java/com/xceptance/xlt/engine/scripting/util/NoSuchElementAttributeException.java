package com.xceptance.xlt.engine.scripting.util;

import com.xceptance.xlt.api.util.XltException;

/**
 * Exception indicating an element attribute lookup miss.
 */
public class NoSuchElementAttributeException extends XltException
{

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public NoSuchElementAttributeException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public NoSuchElementAttributeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the detail message
     */
    public NoSuchElementAttributeException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public NoSuchElementAttributeException(final Throwable cause)
    {
        super(cause);
    }

}
