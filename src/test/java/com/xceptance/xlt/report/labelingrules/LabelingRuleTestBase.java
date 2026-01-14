package com.xceptance.xlt.report.labelingrules;

public class LabelingRuleTestBase
{
    /**
     * Helper method for creating a labeling rule that matches only based on the report type.
     *
     * @param newLabel the new label to apply if the rule matches
     * @param typeString the type string for the rule
     * @return the resulting labeling rule
     */
    protected LabelingRule getTypeLabelingRule(final String newLabel, final String typeString)
    {
        try
        {
            return new LabelingRule(newLabel, typeString, null, null, true, null, null);
        }
        catch (final InvalidLabelingRuleException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for creating a labeling rule that matches only based on the report name.
     *
     * @param newLabel the new label to apply if the rule matches
     * @param namePattern the name pattern for the rule
     * @param nameExcludePattern the name exclude pattern for the rule
     * @return the resulting labeling rule
     */
    protected LabelingRule getNameLabelingRule(final String newLabel, final String namePattern, final String nameExcludePattern)
    {
        try
        {
            return new LabelingRule(newLabel, null, namePattern, null, true, nameExcludePattern, null);
        }
        catch (final InvalidLabelingRuleException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for creating a labeling rule that matches only based on the report label.
     *
     * @param newLabel the new label to apply if the rule matches
     * @param labelPattern the label pattern for the rule
     * @param labelExcludePattern the label exclude pattern for the rule
     * @return the resulting labeling rule
     */
    protected LabelingRule getLabelLabelingRule(final String newLabel, final String labelPattern, final String labelExcludePattern)
    {
        try
        {
            return new LabelingRule(newLabel, null, null, labelPattern, true, null, labelExcludePattern);
        }
        catch (final InvalidLabelingRuleException e)
        {
            throw new RuntimeException(e);
        }
    }
}
