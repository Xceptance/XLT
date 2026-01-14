package com.xceptance.xlt.report.labelingrules;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LabelingRule_General_Test extends LabelingRuleTestBase
{
    @Test
    public void toString_noConditions() throws InvalidLabelingRuleException
    {
        final LabelingRule rule = new LabelingRule("test", null, null, null, false, null, null);

        assertEquals("Labeling rule: 'test', reportTypes: [T, A, R], includeConditions: [], excludeConditions: []", rule.toString());
    }

    @Test
    public void toString_multipleConditions() throws InvalidLabelingRuleException
    {
        final LabelingRule rule = new LabelingRule("test", "T T", "MyName.*", null, false, null, "Fo+bar");

        final StringBuilder sb = new StringBuilder();
        sb.append("Labeling rule: 'test', ");
        sb.append("reportTypes: [T], ");
        sb.append("includeConditions: [name: { type: 'n', pattern: 'MyName.*' }], ");
        sb.append("excludeConditions: [label: { type: 'l', pattern: 'Fo+bar' }]");

        assertEquals(sb.toString(), rule.toString());
    }

    @Test
    public void toString_allConditions() throws InvalidLabelingRuleException
    {
        final LabelingRule rule = new LabelingRule("test", "A;T", "MyName.*", "Label1|Label2", false, "Any Name", "Fo+bar");

        final StringBuilder sb = new StringBuilder();
        sb.append("Labeling rule: 'test', ");
        sb.append("reportTypes: [A, T], ");
        sb.append("includeConditions: [name: { type: 'n', pattern: 'MyName.*' }, label: { type: 'l', pattern: 'Label1|Label2' }], ");
        sb.append("excludeConditions: [name: { type: 'n', pattern: 'Any Name' }, label: { type: 'l', pattern: 'Fo+bar' }]");

        assertEquals(sb.toString(), rule.toString());
    }
}
