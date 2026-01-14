package com.xceptance.xlt.report.labelingrules.reportname;

import com.xceptance.xlt.report.providers.TimerReport;

/**
 * Provide data but otherwise match all report names
 */
public class ReportNameEmptyCondition extends ReportNameCondition
{
    /**
     * Constructor.
     */
    public ReportNameEmptyCondition()
    {
        super("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean apply(final TimerReport report)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CharSequence getReplacementText(final TimerReport report, final int capturingGroupIndex)
    {
        // this is our replacement text without conditions
        return getNullSafeText(report);
    }
}
