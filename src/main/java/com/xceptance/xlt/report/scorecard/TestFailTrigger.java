package com.xceptance.xlt.report.scorecard;

enum TestFailTrigger
{
    PASSED,
    NOTPASSED;

    boolean isTriggeredBy(final Status status)
    {
        if (status != null)
        {
            switch (this)
            {
                case NOTPASSED:
                    return status.isFailed();
                case PASSED:
                    return status.isPassed();
            }
        }

        return false;
    }
}
