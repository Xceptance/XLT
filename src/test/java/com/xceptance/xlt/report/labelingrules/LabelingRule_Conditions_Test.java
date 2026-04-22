/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import com.xceptance.xlt.report.providers.PageLoadTimingReport;
import com.xceptance.xlt.report.providers.RequestReport;
import com.xceptance.xlt.report.providers.TransactionReport;

public class LabelingRule_Conditions_Test extends LabelingRuleTestBase
{
    @Test
    public void ruleWithoutConditions() throws InvalidLabelingRuleException
    {
        // create labeling rules without conditions, i.e. rules that always match
        final LabelingRule rule1 = new LabelingRule("test1", null, null, null, true, null, null);
        final LabelingRule rule2 = new LabelingRule("{l} test2", null, null, null, true, null, null);
        assertEquals(0, rule1.getIncludeConditions().length);
        assertEquals(0, rule1.getExcludeConditions().length);

        // apply first rule
        final RequestReport report1 = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule1.process(report1));
        assertEquals("test1", report1.labels);

        // apply second rule
        assertEquals(LabelingRule.ReturnState.STOP, rule2.process(report1));
        assertEquals("test1 test2", report1.labels);

        // rule doesn't match for unsupported report types
        final PageLoadTimingReport report2 = new PageLoadTimingReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule1.process(report2));
        assertEquals(null, report2.labels);
    }

    @Test
    public void placeHoldersButNoNameOrLabel() throws InvalidLabelingRuleException
    {
        // new label contains placeholders
        final LabelingRule rule = new LabelingRule("test:{n}:{l}", null, null, null, true, null, null);
        assertEquals(2, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);

        // name and label are null
        final RequestReport report1 = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report1));
        assertEquals("test::", report1.labels);

        // name and label are empty
        final RequestReport report2 = new RequestReport();
        report2.name = "";
        report2.labels = "";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report2));
        assertEquals("test::", report2.labels);

        // name and label are whitespace only
        final RequestReport report3 = new RequestReport();
        report3.name = "  ";
        report3.labels = "  ";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report3));
        assertEquals("test:  :  ", report3.labels);
    }

    @Test
    public void ruleWithAllConditions() throws InvalidLabelingRuleException
    {
        // rule with all include and exclude conditions, as well as placeholders
        final LabelingRule rule = new LabelingRule("test:{n:1}:{l:1}", "R", "NameInclude(.*)", "LabelInclude(.*)", true, "NameExclude",
                                                   "LabelExclude");
        assertEquals(2, rule.getIncludeConditions().length);
        assertEquals(2, rule.getExcludeConditions().length);

        // only name include matches
        final RequestReport report1 = new RequestReport();
        report1.name = "NameInclude123";
        report1.labels = "Any Label";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report1));
        assertEquals("Any Label", report1.labels);

        // only label include matches
        final RequestReport report2 = new RequestReport();
        report2.name = "Any Name";
        report2.labels = "LabelInclude456";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report2));
        assertEquals("LabelInclude456", report2.labels);

        // both includes match
        final RequestReport report3 = new RequestReport();
        report3.name = "NameInclude123";
        report3.labels = "LabelInclude456";
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(report3));
        assertEquals("test:123:456", report3.labels);

        // only one exclude matches
        final RequestReport report4 = new RequestReport();
        report4.name = "Any Name";
        report4.labels = "LabelExclude";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report4));
        assertEquals("LabelExclude", report4.labels);

        // both includes match, but one exclude matches
        final RequestReport report5 = new RequestReport();
        report5.name = "NameInclude123 NameExclude";
        report5.labels = "LabelInclude456";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report5));
        assertEquals("LabelInclude456", report5.labels);

        // both includes match, but both excludes match
        final RequestReport report6 = new RequestReport();
        report6.name = "NameInclude123 NameExclude";
        report6.labels = "LabelInclude456 LabelExclude";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report6));
        assertEquals("LabelInclude456 LabelExclude", report6.labels);

        // both includes match, but type doesn't
        final TransactionReport report7 = new TransactionReport();
        report7.name = "NameInclude123";
        report7.labels = "LabelInclude456";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report7));
        assertEquals("LabelInclude456", report7.labels);

        // all includes and excludes don't match
        final RequestReport report8 = new RequestReport();
        report8.name = "Any Name";
        report8.labels = "Any Label";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report8));
        assertEquals("Any Label", report8.labels);
    }

    @Test
    public void continueOnMatch() throws InvalidLabelingRuleException
    {
        // set "stopOnMatch" flag to "false"
        final LabelingRule rule = new LabelingRule("test", null, "MyName.*", null, false, "Fo+bar", null);
        assertEquals(1, rule.getIncludeConditions().length);
        assertEquals(1, rule.getExcludeConditions().length);

        // includes and excludes match
        final RequestReport report1 = new RequestReport();
        report1.name = "MyNameFoobar";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report1));
        assertEquals(null, report1.labels);

        // includes and excludes don't match
        final RequestReport report2 = new RequestReport();
        report2.name = "Any Name";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report2));
        assertEquals(null, report2.labels);

        // only includes match
        final RequestReport report3 = new RequestReport();
        report3.name = "MyName123";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report3));
        assertEquals("test", report3.labels);

        // only excludes match
        final RequestReport report4 = new RequestReport();
        report4.name = "Foobar";
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(report4));
        assertEquals(null, report4.labels);
    }
}
