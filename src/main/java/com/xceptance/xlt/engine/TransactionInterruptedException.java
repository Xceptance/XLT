package com.xceptance.xlt.engine;

import com.xceptance.xlt.api.util.XltException;

/**
 * Thrown in a virtual user's thread to indicate that the current transaction was interrupted. In such a case, the test
 * code should abort the current transaction as quickly as possible.
 */
public class TransactionInterruptedException extends XltException
{
    /**
     * Constructor.
     */
    public TransactionInterruptedException()
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
    public TransactionInterruptedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public TransactionInterruptedException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public TransactionInterruptedException(final Throwable cause)
    {
        super(cause);
    }
}
