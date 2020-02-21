package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;

/**
 * Represents the Apdex for an action.
 */
public class ApdexReport
{
    /**
     * The string representation of the Apdex value that also includes the underlying threshold value.
     */
    public String longValue;

    /**
     * The numeric Apdex value.
     */
    public BigDecimal value;
}
