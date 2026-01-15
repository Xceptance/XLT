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
package com.xceptance.xlt.report.labelingrules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xceptance.xlt.report.providers.RequestReport;

public class LabelingRule_Label_Test extends LabelingRuleTestBase
{
    @Test
    public void include()
    {
        final LabelingRule rule = getLabelLabelingRule("test", "MyLabel.*", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report1 = new RequestReport();
        report1.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report1));
        assertEquals("test", report1.label);

        // no match
        final RequestReport report2 = new RequestReport();
        report2.label = "Any Label";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report2));
        assertEquals("Any Label", report2.label);
    }

    @Test
    public void include_noExistingLabel()
    {
        final LabelingRule rule = getLabelLabelingRule("test", "MyLabel.*", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // report has no existing label, so labeling rule cannot match
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report));
        assertEquals(null, report.label);
    }

    @Test
    public void exclude()
    {
        final LabelingRule rule = getLabelLabelingRule("test", null, "MyLabel.*");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // exclude rule matches, label isn't updated
        final RequestReport report1 = new RequestReport();
        report1.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report1));
        assertEquals("MyLabel123", report1.label);

        // exclude rule doesn't match, label is updated
        final RequestReport report2 = new RequestReport();
        report2.label = "Any Label";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report2));
        assertEquals("test", report2.label);
    }

    @Test
    public void exclude_noExistingLabel()
    {
        final LabelingRule rule = getLabelLabelingRule("test", null, "MyLabel.*");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // report has no existing label, so labeling rule always matches
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("test", report.label);
    }

    @Test
    public void include_exclude()
    {
        final LabelingRule rule = getLabelLabelingRule("test", "MyLabel.*", "Fo+bar");
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match, exclude wins
        final RequestReport report1 = new RequestReport();
        report1.label = "MyLabelFoobar";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report1));
        assertEquals("MyLabelFoobar", report1.label);

        // no match
        final RequestReport report2 = new RequestReport();
        report2.label = "Any Label";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report2));
        assertEquals("Any Label", report2.label);

        // only include matches
        final RequestReport report3 = new RequestReport();
        report3.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report3));
        assertEquals("test", report3.label);

        // only exclude matches
        final RequestReport report4 = new RequestReport();
        report4.label = "Foobar";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report4));
        assertEquals("Foobar", report4.label);
    }

    @Test
    public void include_exclude_noExistingLabel()
    {
        final LabelingRule rule = getLabelLabelingRule("test", "MyLabel.*", "Fo+bar");
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // report has no existing label, so labeling rule cannot match
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report));
        assertEquals(null, report.label);
    }

    @Test
    public void placeholderFull()
    {
        final LabelingRule rule = getLabelLabelingRule("Label:{l}", "MyLabel(\\d+)", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("Label:MyLabel123", report.label);
    }

    @Test
    public void placeholderButNoPattern()
    {
        final LabelingRule rule = getLabelLabelingRule("Label:{l}", null, null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("Label:MyLabel123", report.label);
    }

    @Test
    public void placeholderGroup0()
    {
        final LabelingRule rule = getLabelLabelingRule("Label:{l:0}", "MyLabel(\\d+)", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("Label:MyLabel123", report.label);
    }

    @Test
    public void placeholderAnyGroup()
    {
        final LabelingRule rule = getLabelLabelingRule("{l:1}:{l:2}:{l:1}", "(My)Label(\\d+)", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.label = "MyLabel123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("My:123:My", report.label);
    }

    @Test
    public void placeholderButNoExistingLabel()
    {
        final LabelingRule rule = getLabelLabelingRule("Label:{l}", "MyLabel.*", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // report has no existing label, so labeling rule cannot match
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report));
        assertEquals(null, report.label);
    }
}
