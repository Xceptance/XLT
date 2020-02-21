package com.xceptance.xlt.api.actions;

/**
 * Indicates a missing condition when loading a page. For instance a missing element we can click on.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class ElementMissingException extends Exception
{
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Default constructor.
     */
    public ElementMissingException()
    {
        super();
    }

    /**
     * Creates a new exception using the given message.
     * 
     * @param message
     *            exception message to use
     */
    public ElementMissingException(final String message)
    {
        super(message);
    }

    /**
     * Creates a new exception using the given throwable instance.
     * 
     * @param cause
     *            cause of this exception
     */
    public ElementMissingException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception using the given exception message and cause.
     * 
     * @param message
     *            exception message
     * @param cause
     *            cause of this exception
     */
    public ElementMissingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

}
