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
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.report.providers.RequestReport;
import com.xceptance.xlt.report.providers.TimerReport;

public class LabelingRuleProcessorTest
{
    @Test
    public void continueOnMatch() throws InvalidLabelingRuleException
    {
        // "stopOnMatch" flag is disabled for all rules, so all rules are processed
        final LabelingRule rule1 = new LabelingRule("label1", null, "abc", null, false, null, null);
        final LabelingRule rule2 = new LabelingRule("{l} label2", null, "123", null, false, null, null);
        final LabelingRule rule3 = new LabelingRule("{l} label3", null, "xyz", null, false, null, null);
        final LabelingRuleProcessor processor = new LabelingRuleProcessor(List.of(rule1, rule2, rule3));

        // all rules match
        final RequestReport report1 = new RequestReport();
        report1.name = "xyz-abc-123";
        processor.process(report1);
        assertEquals("label1 label2 label3", report1.label);

        // one rule doesn't match
        final RequestReport report2 = new RequestReport();
        report2.name = "xyz-abc";
        processor.process(report2);
        assertEquals("label1 label3", report2.label);

        // no rule matches
        final RequestReport report3 = new RequestReport();
        report3.name = "foobar";
        processor.process(report3);
        assertEquals(null, report3.label);
    }

    @Test
    public void stopOnMatch() throws InvalidLabelingRuleException
    {
        // "stopOnMatch" flag is enabled for the second rule
        final LabelingRule rule1 = new LabelingRule("label1", null, "abc", null, false, null, null);
        final LabelingRule rule2 = new LabelingRule("{l} label2", null, "123", null, true, null, null);
        final LabelingRule rule3 = new LabelingRule("{l} label3", null, "xyz", null, false, null, null);
        final LabelingRuleProcessor processor = new LabelingRuleProcessor(List.of(rule1, rule2, rule3));

        // all rules match; processing stops after second rule
        final RequestReport report1 = new RequestReport();
        report1.name = "xyz-abc-123";
        processor.process(report1);
        assertEquals("label1 label2", report1.label);

        // second rule doesn't match; third rule is processed
        final RequestReport report2 = new RequestReport();
        report2.name = "xyz-abc";
        processor.process(report2);
        assertEquals("label1 label3", report2.label);

        // no rules match
        final RequestReport report3 = new RequestReport();
        report3.name = "foobar";
        processor.process(report3);
        assertEquals(null, report3.label);
    }

    @Test
    public void ruleProcessingError() throws InvalidLabelingRuleException
    {
        final LabelingRule faultyRule = new LabelingRule("test", null, null, null, false, null, null)
        {
            @Override
            public LabelingRule.ReturnState process(final TimerReport report)
            {
                throw new RuntimeException("Test exception");
            }
        };
        final LabelingRuleProcessor processor = new LabelingRuleProcessor(List.of(faultyRule));

        final XltException ex = assertThrows(XltException.class, () -> processor.process(new RequestReport()));
        assertEquals("Failed to apply labeling rule: " + faultyRule, ex.getMessage());
    }

}
