package com.xceptance.xlt.report.labelingrules;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.report.providers.TimerReport;

import java.util.List;

public class LabelingRuleProcessor
{
    private final List<LabelingRule> labelingRules;

    public LabelingRuleProcessor(final List<LabelingRule> labelingRules)
    {
        this.labelingRules = labelingRules;
    }

    /**
     * Process the labeling rules and update the label of the given report on match.
     *
     * @param report the report
     */
    public void process(final TimerReport report)
    {
        // execute all labeling rules one after the other until processing is complete
        for (final LabelingRule rule : labelingRules)
        {
            try
            {
                final LabelingRule.ReturnState state = rule.process(report);
                if (state == LabelingRule.ReturnState.STOP)
                {
                    return;
                }
            }
            catch (final Throwable t)
            {
                throw new XltException(String.format("Failed to apply labeling rule: %s", rule), t);
            }
        }
    }
}
