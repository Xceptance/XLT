package com.xceptance.xlt.api.data;

import com.xceptance.xlt.api.util.XltException;

/**
 * Thrown by a {@link DataSetProvider} implementation in case an error occurred when reading or processing test data set
 * files.
 */
public class DataSetProviderException extends XltException
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public DataSetProviderException()
    {
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     */
    public DataSetProviderException(final String message)
    {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the cause
     */
    public DataSetProviderException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public DataSetProviderException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
