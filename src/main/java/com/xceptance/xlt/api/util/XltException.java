package com.xceptance.xlt.api.util;

/**
 * The base class for all XLT exceptions. This class may also serve as a "general" exception.
 */
public class XltException extends RuntimeException
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public XltException()
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
    public XltException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public XltException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public XltException(final Throwable cause)
    {
        super(cause);
    }
}
