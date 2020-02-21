package com.xceptance.xlt.engine.scripting.htmlunit;

import com.xceptance.xlt.api.util.XltException;

/**
 * Exception indicating the usage of an invalid element locator.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class IllegalLocatorException extends XltException
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public IllegalLocatorException()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's message
     */
    public IllegalLocatorException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the exception's cause
     */
    public IllegalLocatorException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the exception's message
     * @param cause
     *            the exception's cause
     */
    public IllegalLocatorException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
