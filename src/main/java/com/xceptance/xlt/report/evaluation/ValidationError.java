package com.xceptance.xlt.report.evaluation;

public class ValidationError extends Exception
{

    public ValidationError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ValidationError(String message)
    {
        super(message);
    }

    public ValidationError(Throwable cause)
    {
        super(cause);
    }

}