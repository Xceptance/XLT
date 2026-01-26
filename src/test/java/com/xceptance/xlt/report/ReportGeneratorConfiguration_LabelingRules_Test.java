/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.report.labelingrules.LabelingRule;

public class ReportGeneratorConfiguration_LabelingRules_Test extends ReportGeneratorConfigurationTestBase
{
    @Test
    public void getLabelingRules_noRules()
    {
        final List<LabelingRule> labelingRules = readReportGeneratorProperties().getLabelingRules();
        assertEquals(0, labelingRules.size());
    }

    @Test
    public void getLabelingRules_minimalRule()
    {
        // add a rule with the least number of settings, i.e. only the "newLabels" is configured
        appendLabelingRuleToProperties("1", "label1", null, null, null, null, null, null);

        final List<LabelingRule> labelingRules = readReportGeneratorProperties().getLabelingRules();

        assertEquals(1, labelingRules.size());
        assertMinimalLabelingRule(labelingRules.get(0), "label1");
    }

    @Test
    public void getLabelingRules_singleRule()
    {
        // add a rule with all settings
        appendLabelingRuleToProperties("1", "{l} label1", "A,R", "MyName.*", "^(MyLabel|MyOtherLabel)", false, "Fo+bar", "Any Label");

        final List<LabelingRule> labelingRules = readReportGeneratorProperties().getLabelingRules();

        // Validate rule count
        assertEquals(1, labelingRules.size());

        // Validate new label; placeholders are removed from label
        final LabelingRule rule = labelingRules.get(0);
        assertEquals(" label1", rule.getNewLabels());

        // Validate type codes
        assertEquals(Set.of("A", "R"), rule.getTypeCodes());

        // Validate "stopOnMatch" flag
        assertEquals(false, rule.getStopOnMatch());

        // Validate include conditions
        assertEquals(2, rule.getIncludeConditions().length);
        assertEquals("n", rule.getIncludeConditions()[0].getTypeCode());
        assertEquals("l", rule.getIncludeConditions()[1].getTypeCode());
        assertEquals("MyName.*", rule.getIncludeConditions()[0].getPattern());
        assertEquals("^(MyLabel|MyOtherLabel)", rule.getIncludeConditions()[1].getPattern());

        // Validate exclude conditions
        assertEquals(2, rule.getExcludeConditions().length);
        assertEquals("n", rule.getExcludeConditions()[0].getTypeCode());
        assertEquals("l", rule.getExcludeConditions()[1].getTypeCode());
        assertEquals("Fo+bar", rule.getExcludeConditions()[0].getPattern());
        assertEquals("Any Label", rule.getExcludeConditions()[1].getPattern());
    }

    @Test
    public void getLabelingRules_multipleRules()
    {
        appendLabelingRuleToProperties("1", "label1", "T", "MyName.*", null, false, null, "Any Label");
        appendLabelingRuleToProperties("2", "{n} label_{l}_123", "A,R,T", null, "^(MyLabel|MyOtherLabel)", true, null, null);

        final List<LabelingRule> labelingRules = readReportGeneratorProperties().getLabelingRules();

        assertEquals(2, labelingRules.size());

        // Validate rule 1
        final LabelingRule rule1 = labelingRules.get(0);
        assertEquals("label1", rule1.getNewLabels());
        assertEquals(Set.of("T"), rule1.getTypeCodes());
        assertEquals(false, rule1.getStopOnMatch());
        assertEquals(1, rule1.getIncludeConditions().length);
        assertEquals("n", rule1.getIncludeConditions()[0].getTypeCode());
        assertEquals("MyName.*", rule1.getIncludeConditions()[0].getPattern());
        assertEquals(1, rule1.getExcludeConditions().length);
        assertEquals("l", rule1.getExcludeConditions()[0].getTypeCode());
        assertEquals("Any Label", rule1.getExcludeConditions()[0].getPattern());

        // Validate rule 2
        final LabelingRule rule2 = labelingRules.get(1);
        assertEquals(" label__123", rule2.getNewLabels());
        assertEquals(Set.of("T", "A", "R"), rule2.getTypeCodes());
        assertEquals(true, rule2.getStopOnMatch());
        // Empty include conditions are kept for the placeholders
        assertEquals(2, rule2.getIncludeConditions().length);
        assertEquals("n", rule2.getIncludeConditions()[0].getTypeCode());
        assertEquals("l", rule2.getIncludeConditions()[1].getTypeCode());
        assertEquals("", rule2.getIncludeConditions()[0].getPattern());
        assertEquals("^(MyLabel|MyOtherLabel)", rule2.getIncludeConditions()[1].getPattern());
        assertEquals(0, rule2.getExcludeConditions().length);
    }

    @Test
    public void getLabelingRules_indexOrderAndGaps()
    {
        // rule indexes are in random order and have gaps
        appendLabelingRuleToProperties("10", "label1", null, null, null, null, null, null);
        appendLabelingRuleToProperties("0", "label2", null, null, null, null, null, null);
        appendLabelingRuleToProperties("3", "label3", null, null, null, null, null, null);

        final List<LabelingRule> labelingRules = readReportGeneratorProperties().getLabelingRules();

        // labeling rules are read in the correct order and gaps are ignored
        assertEquals(3, labelingRules.size());
        assertMinimalLabelingRule(labelingRules.get(0), "label2");
        assertMinimalLabelingRule(labelingRules.get(1), "label3");
        assertMinimalLabelingRule(labelingRules.get(2), "label1");
    }

    @Test
    public void getLabelingRules_error_leadingZeroes()
    {
        appendLabelingRuleToProperties("01", "label1", null, null, null, null, null, null);

        final XltException ex = assertThrows(XltException.class, () -> readReportGeneratorProperties().getLabelingRules());

        final StringBuilder expectedMessageBuilder = new StringBuilder();
        expectedMessageBuilder.append("Leading zeros are not allowed in merge or labeling rule indices.\n");
        expectedMessageBuilder.append("Please check your configuration and fix the following properties:\n\t");
        expectedMessageBuilder.append(ReportGeneratorConfiguration.PROP_LABELING_RULES_PREFIX).append("01.newLabels\n");

        assertEquals(expectedMessageBuilder.toString(), ex.getMessage());
    }

    @Test
    public void getLabelingRules_error_invalidRule()
    {
        // invalid name pattern regex
        appendLabelingRuleToProperties("1", "label1", null, "([]-]", null, null, null, null);

        final XltException ex = assertThrows(XltException.class, () -> readReportGeneratorProperties().getLabelingRules());
        assertEquals("Please check your configuration. At least one labeling rule is invalid and needs to be fixed.", ex.getMessage());
    }

    /**
     * Helper method to assert the configuration of a minimal labeling rule, i.e. a rule with only "newLabels"
     * configured.
     *
     * @param rule
     *            the rule to assert
     * @param expectedNewLabels
     *            the expected "newLabels" of the rule
     */
    private void assertMinimalLabelingRule(final LabelingRule rule, final String expectedNewLabels)
    {
        assertEquals(expectedNewLabels, rule.getNewLabels());
        assertEquals(Set.of("T", "A", "R"), rule.getTypeCodes());
        assertEquals(true, rule.getStopOnMatch());
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
    }

    /**
     * Helper method for appending all properties of a labeling rule to the "reportgenerator.properties" file.
     */
    private void appendLabelingRuleToProperties(final String index, final String newLabels, final String types, final String namePattern,
                                                final String labelPattern, final Boolean stopOnMatch, final String nameExcludePattern,
                                                final String labelExcludePattern)
    {
        final List<String> lines = new ArrayList<>();
        final String prefix = ReportGeneratorConfiguration.PROP_LABELING_RULES_PREFIX + index + ".";

        if (newLabels != null)
        {
            lines.add(prefix + "newLabels = " + newLabels);
        }
        if (types != null)
        {
            lines.add(prefix + "types = " + types);
        }
        if (namePattern != null)
        {
            lines.add(prefix + "namePattern = " + namePattern);
        }
        if (labelPattern != null)
        {
            lines.add(prefix + "labelPattern = " + labelPattern);
        }
        if (stopOnMatch != null)
        {
            lines.add(prefix + "stopOnMatch = " + stopOnMatch);
        }
        if (nameExcludePattern != null)
        {
            lines.add(prefix + "namePattern.exclude = " + nameExcludePattern);
        }
        if (labelExcludePattern != null)
        {
            lines.add(prefix + "labelPattern.exclude = " + labelExcludePattern);
        }

        appendPropertyLinesToFile(lines);
    }
}
