package com.xceptance.xlt.report.evaluation;

public class ValidationException extends Exception
{

    public ValidationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ValidationException(final String message)
    {
        super(message);
    }

    public ValidationException(final Throwable cause)
    {
        super(cause);
    }

}
