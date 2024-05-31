package com.xceptance.xlt.report.evaluation;

enum Status
{
    SKIPPED,
    PASSED,
    FAILED,
    ERROR;

    public boolean isPassed()
    {
        return Status.PASSED == this;
    }

    public boolean isFailed()
    {
        return Status.FAILED == this;
    }

    public boolean isError()
    {
        return Status.ERROR == this;
    }

    public boolean isSkipped()
    {
        return Status.SKIPPED == this;
    }

    /**
     * Returns the negated status of this status (PASSED &rarr; FAILED; FAILED &rarr; PASSED; SKIPPED and ERROR stay as
     * is),
     * 
     * @return this status negated
     */
    Status negate()
    {
        if (isPassed())
        {
            return FAILED;
        }
        if (isFailed())
        {
            return PASSED;
        }
        return this;
    }
}
