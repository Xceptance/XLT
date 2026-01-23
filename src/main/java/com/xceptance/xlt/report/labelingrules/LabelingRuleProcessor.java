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

import java.util.List;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.report.providers.TimerReport;

public class LabelingRuleProcessor
{
    private final List<LabelingRule> labelingRules;

    public LabelingRuleProcessor(final List<LabelingRule> labelingRules)
    {
        this.labelingRules = labelingRules;
    }

    /**
     * Process the labeling rules and update the labels of the given report on match.
     *
     * @param report
     *            the report
     */
    public void process(final TimerReport report)
    {
        // execute all labeling rules one after the other until processing is complete
        for (final LabelingRule rule : labelingRules)
        {
            try
            {
                final LabelingRule.ReturnState state = rule.process(report);
                if (state == LabelingRule.ReturnState.STOP)
                {
                    return;
                }
            }
            catch (final Throwable t)
            {
                throw new XltException(String.format("Failed to apply labeling rule: %s", rule), t);
            }
        }
    }
}
