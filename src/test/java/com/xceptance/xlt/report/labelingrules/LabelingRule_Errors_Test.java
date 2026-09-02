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
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class LabelingRule_Errors_Test extends LabelingRuleTestBase
{
    @Test
    public void noNewLabels()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class,
                                                             () -> new LabelingRule(null, null, null, null, false, null, null));
        assertEquals("The 'newLabels' must be provided when creating a labeling rule.", ex.getMessage());
    }

    @Test
    public void invalidRecordType()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class,
                                                             () -> new LabelingRule("test", "X,A,Y", null, null, false, null, null));
        assertEquals("Report type codes '[X, Y]' are not allowed for labeling rules. Valid types are: '[T, A, R]'.", ex.getMessage());
    }

    @Test
    public void invalidRegEx()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class,
                                                             () -> new LabelingRule("test", null, "([]-]", null, false, null, null));
        assertEquals("Invalid regular expression: ([]-]", ex.getMessage());
    }

    @Test
    public void invalidPlaceholderCapturingGroupIndex()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class,
                                                             () -> new LabelingRule("test {n:x}", null, null, null, false, null, null));
        assertEquals("Failed to parse the matching group index 'x' as integer", ex.getMessage());
    }

    @Test
    public void placeholderCapturingGroupIndexDoesNotMatchRegEx()
    {
        final InvalidLabelingRuleException ex = assertThrows(InvalidLabelingRuleException.class,
                                                             () -> new LabelingRule("test {n:1}", null, "MyName.*", null, false, null,
                                                                                    null));
        assertEquals("Pattern 'MyName.*' has no matching group '1'. Important: You can only capture in include rules.", ex.getMessage());
    }
}
