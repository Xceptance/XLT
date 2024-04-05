package com.xceptance.xlt.report.evaluation;

enum Status
{
    PASSED,
    FAILED,
    ERROR,
    SKIPPED;

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
}