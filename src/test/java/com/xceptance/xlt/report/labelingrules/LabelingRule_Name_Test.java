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

public class LabelingRule_Name_Test extends LabelingRuleTestBase
{
    @Test
    public void include()
    {
        final LabelingRule rule = getNameLabelingRule("test", "MyName.*", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report1 = new RequestReport();
        report1.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report1));
        assertEquals("test", report1.label);

        // no match
        final RequestReport report2 = new RequestReport();
        report2.name = "Any Name";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report2));
        assertEquals(null, report2.label);
    }

    @Test
    public void include_noName()
    {
        final LabelingRule rule = getNameLabelingRule("test", "MyName.*", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // report has no name, so labeling rule cannot match
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report));
        assertEquals(null, report.label);
    }

    @Test
    public void exclude()
    {
        final LabelingRule rule = getNameLabelingRule("test", null, "MyName.*");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // exclude matches, label isn't set
        final RequestReport report1 = new RequestReport();
        report1.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report1));
        assertEquals(null, report1.label);

        // exclude doesn't match, label is set
        final RequestReport report2 = new RequestReport();
        report2.name = "Any Name";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report2));
        assertEquals("test", report2.label);
    }

    @Test
    public void exclude_noName()
    {
        final LabelingRule rule = getNameLabelingRule("test", null, "MyName.*");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // report has no name, so labeling rule always matches
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("test", report.label);
    }

    @Test
    public void include_exclude()
    {
        final LabelingRule rule = getNameLabelingRule("test", "MyName.*", "Fo+bar");
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // both match, exclude wins
        final RequestReport report1 = new RequestReport();
        report1.name = "MyNameFoobar";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report1));
        assertEquals(null, report1.label);

        // none match
        final RequestReport report2 = new RequestReport();
        report2.name = "Any Name";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report2));
        assertEquals(null, report2.label);

        // only include matches
        final RequestReport report3 = new RequestReport();
        report3.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report3));
        assertEquals("test", report3.label);

        // only exclude matches
        final RequestReport report4 = new RequestReport();
        report4.name = "Foobar";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report4));
        assertEquals(null, report4.label);
    }

    @Test
    public void include_exclude_noName()
    {
        final LabelingRule rule = getNameLabelingRule("test", "MyName.*", "Fo+bar");
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // report has no name, so labeling rule cannot match
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report));
        assertEquals(null, report.label);
    }

    @Test
    public void placeholderFull()
    {
        final LabelingRule rule = getNameLabelingRule("Name:{n}", "MyName(\\d+)", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("Name:MyName123", report.label);
    }

    @Test
    public void placeholderButNoPattern()
    {
        final LabelingRule rule = getNameLabelingRule("Name:{n}", null, null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("Name:MyName123", report.label);
    }

    @Test
    public void placeholderGroup0()
    {
        final LabelingRule rule = getNameLabelingRule("Name:{n:0}", "MyName(\\d+)", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("Name:MyName123", report.label);
    }

    @Test
    public void placeholderAnyGroup()
    {
        final LabelingRule rule = getNameLabelingRule("{n:1}:{n:2}:{n:1}", "(My)Name(\\d+)", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // match
        final RequestReport report = new RequestReport();
        report.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report));
        assertEquals("My:123:My", report.label);
    }

    @Test
    public void placeholderButNoName()
    {
        final LabelingRule rule = getNameLabelingRule("Name:{n}", "MyName.*", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // report has no name, so labeling rule cannot match
        final RequestReport report = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report));
        assertEquals(null, report.label);
    }
}
