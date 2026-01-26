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
