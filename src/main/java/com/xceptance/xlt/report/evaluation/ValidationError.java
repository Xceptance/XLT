package com.xceptance.xlt.report.evaluation;

public class ValidationError extends Exception
{

    public ValidationError(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ValidationError(final String message)
    {
        super(message);
    }

    public ValidationError(final Throwable cause)
    {
        super(cause);
    }

}
