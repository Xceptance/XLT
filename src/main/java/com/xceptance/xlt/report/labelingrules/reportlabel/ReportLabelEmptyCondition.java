package com.xceptance.xlt.report.labelingrules.reportlabel;

import com.xceptance.xlt.report.providers.TimerReport;

/**
 * Provide data but otherwise match all labels
 */
public class ReportLabelEmptyCondition extends ReportLabelCondition
{
    /**
     * Constructor.
     */
    public ReportLabelEmptyCondition()
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
