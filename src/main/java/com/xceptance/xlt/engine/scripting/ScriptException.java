package com.xceptance.xlt.engine.scripting;

import com.xceptance.xlt.api.util.XltException;

/**
 * The base class for all exceptions thrown by the script interpreter framework. This class may also serve as a
 * "general" script interpreter exception.
 */
public class ScriptException extends XltException
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3888385537400709411L;

    /**
     * Constructor.
     */
    public ScriptException()
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
    public ScriptException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public ScriptException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public ScriptException(final Throwable cause)
    {
        super(cause);
    }
}
