package com.xceptance.xlt.report.labelingrules;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LabelingRule_Errors_Test extends LabelingRuleTestBase
{
    @Test
    public void noNewLabel()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class, () -> new LabelingRule(null, null, null, null, false, null, null));
        assertEquals("The 'newLabel' must be provided when creating a labeling rule.", ex.getMessage());
    }

    @Test
    public void invalidRecordType()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class, () -> new LabelingRule("test", "X,A,Y", null, null, false, null, null));
        assertEquals("Report type codes '[X, Y]' are not allowed for labeling rules. Valid types are: '[T, A, R]'.", ex.getMessage());
    }

    @Test
    public void invalidRegEx()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class, () -> new LabelingRule("test", null, "([]-]", null, false, null, null));
        assertEquals("Invalid regular expression: ([]-]", ex.getMessage());
    }

    @Test
    public void invalidPlaceholderCapturingGroupIndex()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class, () -> new LabelingRule("test {n:x}", null, null, null, false, null, null));
        assertEquals("Failed to parse the matching group index 'x' as integer", ex.getMessage());
    }

    @Test
    public void placeholderCapturingGroupIndexDoesNotMatchRegEx()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class, () -> new LabelingRule("test {n:1}", null, "MyName.*", null, false, null, null));
        assertEquals("Pattern 'MyName.*' has no matching group '1'. Important: You can only capture in include rules.", ex.getMessage());
    }
}
