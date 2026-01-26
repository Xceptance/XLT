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

public class LabelingRuleTestBase
{
    /**
     * Helper method for creating a labeling rule that matches only based on the report type.
     *
     * @param newLabels
     *            the new label string to apply if the rule matches
     * @param typeString
     *            the type string for the rule
     * @return the resulting labeling rule
     */
    protected LabelingRule getTypeLabelingRule(final String newLabels, final String typeString)
    {
        try
        {
            return new LabelingRule(newLabels, typeString, null, null, true, null, null);
        }
        catch (final InvalidLabelingRuleException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for creating a labeling rule that matches only based on the report name.
     *
     * @param newLabels
     *            the new label string to apply if the rule matches
     * @param namePattern
     *            the name pattern for the rule
     * @param nameExcludePattern
     *            the name exclude pattern for the rule
     * @return the resulting labeling rule
     */
    protected LabelingRule getNameLabelingRule(final String newLabels, final String namePattern, final String nameExcludePattern)
    {
        try
        {
            return new LabelingRule(newLabels, null, namePattern, null, true, nameExcludePattern, null);
        }
        catch (final InvalidLabelingRuleException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for creating a labeling rule that matches only based on the report labels.
     *
     * @param newLabels
     *            the new label string to apply if the rule matches
     * @param labelPattern
     *            the label pattern for the rule
     * @param labelExcludePattern
     *            the label exclude pattern for the rule
     * @return the resulting labeling rule
     */
    protected LabelingRule getLabelLabelingRule(final String newLabels, final String labelPattern, final String labelExcludePattern)
    {
        try
        {
            return new LabelingRule(newLabels, null, null, labelPattern, true, null, labelExcludePattern);
        }
        catch (final InvalidLabelingRuleException e)
        {
            throw new RuntimeException(e);
        }
    }
}
