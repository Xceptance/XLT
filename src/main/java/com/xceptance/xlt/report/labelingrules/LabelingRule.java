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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.report.labelingrules.reportlabel.ReportLabelCondition;
import com.xceptance.xlt.report.labelingrules.reportname.ReportNameCondition;
import com.xceptance.xlt.report.providers.TimerReport;
import org.apache.commons.lang3.StringUtils;

/**
 * A {@link LabelingRule} governs the process of labeling report objects. It represents a bundle of criteria a report
 * object must meet to be labeled and defines the new label to apply.
 * 
 * @see TimerReport#label
 */
public class LabelingRule
{
    /**
     * The possible return states of the rule evaluation to communicate if the next rule should be evaluated or not.
     */
    public enum ReturnState
    {
        CONTINUE,
        STOP
    }

    /**
     * All type codes that can be used in labeling rules.
     */
    private static final List<String> SUPPORTED_TYPE_CODES = List.of(TimerReport.TimerReportType.TRANSACTION.getTypeCode(),
                                                                     TimerReport.TimerReportType.ACTION.getTypeCode(),
                                                                     TimerReport.TimerReportType.REQUEST.getTypeCode());

    /**
     * The pattern to find placeholders in the new label.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([nl])(?::(.+?))?\\}");

    /**
     * The label to apply if the rule matches.
     */
    private final String newLabel;

    /**
     * Whether the next rule should be processed if the current rule applied.
     */
    private final boolean stopOnMatch;

    /**
     * The report type codes for which this rule should be applied.
     */
    private final Set<String> typeCodes;

    /**
     * The configured include conditions of this rule.
     */
    private final LabelingRuleCondition[] includeConditions;

    /**
     * The configured exclude conditions of this rule.
     */
    private final LabelingRuleCondition[] excludeConditions;

    /**
     * The list of placeholders with info where to insert the data to form a new label.
     */
    private final PlaceholderPosition[] newLabelPlaceholders;

    /**
     * Constructor.
     *
     * @param newLabel
     *            the label to add to the report if all conditions match (required)
     * @param typeString
     *            string containing the type codes of all report types this rule should be evaluated for; multiple type
     *            codes must be delimited by comma, semicolon or whitespaces
     * @param namePattern
     *            include pattern for the report name
     * @param labelPattern
     *            include pattern for the report label
     * @param stopOnMatch
     *            indicates whether the next rule should be processed if the current rule applied
     * @param nameExcludePattern
     *            exclude pattern for the report name
     * @param labelExcludePattern
     *            exclude pattern for the report label
     * @throws InvalidLabelingRuleException if any rule parameter is invalid
     */
    public LabelingRule(final String newLabel, final String typeString, final String namePattern, final String labelPattern,
                        final boolean stopOnMatch, final String nameExcludePattern, final String labelExcludePattern)
        throws InvalidLabelingRuleException
    {
        if (StringUtils.isBlank(newLabel))
        {
            throw new InvalidLabelingRuleException("The 'newLabel' must be provided when creating a labeling rule.");
        }

        this.typeCodes = readTypeCodesFromString(typeString);
        this.stopOnMatch = stopOnMatch;

        // parse the placeholder positions now (and only once)
        final var tempPlaceHolderPositions = parsePlaceholderPositions(newLabel);

        List<LabelingRuleCondition> includeConditions = new ArrayList<>();
        List<LabelingRuleCondition> excludeConditions = new ArrayList<>();

        try
        {
            includeConditions.add(ReportNameCondition.build(namePattern));
            excludeConditions.add(ReportNameCondition.build(nameExcludePattern));

            includeConditions.add(ReportLabelCondition.build(labelPattern));
            excludeConditions.add(ReportLabelCondition.build(labelExcludePattern));
        }
        catch (final PatternSyntaxException pse)
        {
            throw new InvalidLabelingRuleException("Invalid regular expression: " + pse.getPattern());
        }

        // remove all conditionss we don't need
        includeConditions = cleanUpIncludeConditions(tempPlaceHolderPositions, includeConditions);
        excludeConditions = cleanUpExcludeConditions(excludeConditions);

        this.includeConditions = includeConditions.toArray(new LabelingRuleCondition[includeConditions.size()]);
        this.excludeConditions = excludeConditions.toArray(new LabelingRuleCondition[excludeConditions.size()]);

        this.newLabel = adjustNewLabel(newLabel, tempPlaceHolderPositions);
        this.newLabelPlaceholders = tempPlaceHolderPositions.toArray(new PlaceholderPosition[tempPlaceHolderPositions.size()]);

        assignConditionsToPlaceholders(this.newLabelPlaceholders);

        // Validate the entire rule set, because we might still have issues
        validateRule();
    }

    /**
     * Cleanup our conditions down to the ones we really need for creating a new label or simply to be a condition only
     *
     * @param positions
     *            the list of placeholder positions
     * @param conditions
     *            the list of conditions to clean up
     */
    private List<LabelingRuleCondition> cleanUpIncludeConditions(final List<PlaceholderPosition> positions,
                                                                 final List<LabelingRuleCondition> conditions)
    {
        return conditions.stream().filter(c -> {
            // we keep empty conditions if they are used as placeholder
            for (final PlaceholderPosition pos : positions)
            {
                if (pos.typeCode.equals(c.getTypeCode()))
                {
                    return true;
                }
            }

            // we keep all non-empty conditions
            return !c.isEmpty();
        }).toList();
    }

    /**
     * Our excludes are not providing any data, so we can remove all that are empty
     *
     * @param conditions
     *            the list of conditions to clean up
     */
    private List<LabelingRuleCondition> cleanUpExcludeConditions(final List<LabelingRuleCondition> conditions)
    {
        return conditions.stream().filter(c -> !c.isEmpty()).toList();
    }

    /**
     * To speed things up, we match include conditions to placeholders
     *
     * @param positions
     *            the list of placeholder positions
     */
    private void assignConditionsToPlaceholders(final PlaceholderPosition[] positions)
    {
        // we have to assign the condition to each matching placeholder position
        for (final PlaceholderPosition pos : positions)
        {
            for (final LabelingRuleCondition condition : includeConditions)
            {
                if (condition.getTypeCode().equals(pos.typeCode))
                {
                    // we found the condition, so set it
                    pos.condition = condition;
                    break;
                }
            }
        }
    }

    /**
     * Parses the position of the placeholders in the new label field of the rule. Composes a newLabel without the
     * placeholders for later speed
     *
     * @param labelWithPlaceholders
     *            the label with placeholders
     * @throws InvalidLabelingRuleException if a placeholder contains an invalid matching group index
     */
    private List<PlaceholderPosition> parsePlaceholderPositions(final String labelWithPlaceholders) throws InvalidLabelingRuleException
    {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(labelWithPlaceholders);

        // use a list now and make it an array later
        final List<PlaceholderPosition> positions = new ArrayList<>();

        while (matcher.find())
        {
            // determine the matching group index if any was present at all
            int matchingGroupIndex = -1;

            final String matchingGroupIndexString = matcher.group(2);
            if (matchingGroupIndexString != null)
            {
                try
                {
                    matchingGroupIndex = Integer.valueOf(matchingGroupIndexString);
                }
                catch (final NumberFormatException e)
                {
                    throw new InvalidLabelingRuleException(String.format("Failed to parse the matching group index '%s' as integer",
                                                                         matchingGroupIndexString));
                }
            }

            // build and remember the placeholder position
            final PlaceholderPosition position = new PlaceholderPosition(matcher.group(1), matchingGroupIndex, matcher.start(),
                                                                         matcher.end(), matcher.group().length());
            positions.add(position);
        }

        // to avoid calculations later, we revert the positions and can basically just insert without
        // caring about the position changes due to inserts
        Collections.reverse(positions);

        return positions;
    }

    /**
     * Get us a new label that has no longer any placeholders in it. Instead, the placeholder positions get updated with
     * information where to insert data in the new label.
     *
     * @param labelWithPlaceholders
     *            the current label with placeholders
     * @param positions
     *            a set of already parsed placeholder positions
     * @return the new label clean of placeholders
     */
    public static String adjustNewLabel(final String labelWithPlaceholders, final List<PlaceholderPosition> positions)
    {
        final StringBuilder label = new StringBuilder(labelWithPlaceholders);
        int displacement = 0;

        // we already reversed the positions, so do it the other way around
        for (int i = positions.size() - 1; i >= 0; i--)
        {
            final PlaceholderPosition pos = positions.get(i);

            // remove the placeholder from the newLabel
            label.delete(pos.start - displacement, pos.end - displacement);

            // update the placeholder, it is now different from before because it does not
            // really reflect room anymore, rather a position
            positions.set(i, new PlaceholderPosition(pos.typeCode, pos.capturingGroupIndex, pos.start - displacement));

            // update the sum up shift so far
            displacement += pos.length;
        }

        return label.toString();
    }

    /**
     * Processes this report labeling rule. As a consequence, the passed report object will (or will not) get a new
     * label assigned.
     *
     * @param report
     *            the timer report object to process, will also be directly modified as a result
     * @return the return state
     */
    public ReturnState process(final TimerReport report)
    {
        // if the report type doesn't match the rule's configured type codes, return immediately
        if (!typeCodes.contains(report.getTypeCode()))
        {
            return ReturnState.CONTINUE;
        }

        // return if the report doesn't match all include conditions
        for (final LabelingRuleCondition includeCondition : includeConditions)
        {
            if (!includeCondition.apply(report))
            {
                return ReturnState.CONTINUE;
            }
        }

        // return if the report matches any of the exclude conditions
        for (final LabelingRuleCondition excludeCondition : excludeConditions)
        {
            if (excludeCondition.apply(report))
            {
                return ReturnState.CONTINUE;
            }
        }

        // anything to do?
        if (newLabelPlaceholders.length > 0)
        {
            final StringBuilder result = new StringBuilder(50);
            result.append(newLabel);

            // search as long as there are placeholders in the label
            for (final PlaceholderPosition placeholder : newLabelPlaceholders)
            {
                final LabelingRuleCondition condition = placeholder.condition;

                // get replacement
                final CharSequence replacement = condition.getReplacementText(report, placeholder.capturingGroupIndex);

                // replace the placeholder with the real values
                result.insert(placeholder.start, replacement);
            }

            // set the final label
            report.label = result.toString();
        }
        else
        {
            // nothing to do, keep the newLabel
            report.label = newLabel;
        }

        return stopOnMatch ? ReturnState.STOP : ReturnState.CONTINUE;
    }

    /**
     * Determine the report types for which this labeling rule applies based on the given string and return their type
     * codes. If the given string is blank, all supported type codes are returned (i.e. the rule will match all
     * supported report types).
     *
     * @param typeCodeString
     *            the string containing the type codes (delimited by comma, semicolon or whitespaces)
     * @return a set containing the resulting type codes
     * @throws InvalidLabelingRuleException
     *             if the string contains an unsupported type code
     */
    private Set<String> readTypeCodesFromString(final String typeCodeString) throws InvalidLabelingRuleException
    {
        final List<String> typeCodes = List.of(ParseUtils.parseDelimitedString(typeCodeString));

        // if no type codes are configured, the rule applies to all supported report types
        if (typeCodes.isEmpty())
        {
            return new LinkedHashSet<>(SUPPORTED_TYPE_CODES);
        }

        // fail if any of the given type codes aren't supported
        final List<String> invalidTypeCodes = typeCodes.stream().filter(t -> !SUPPORTED_TYPE_CODES.contains(t)).toList();
        if (!invalidTypeCodes.isEmpty())
        {
            throw new InvalidLabelingRuleException("Report type codes '" + invalidTypeCodes +
                                                   "' are not allowed for labeling rules. Valid types are: '" + SUPPORTED_TYPE_CODES +
                                                   "'.");
        }

        return new LinkedHashSet<>(typeCodes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Labeling rule: '");
        sb.append(newLabel);
        sb.append("', reportTypes: ");
        sb.append(typeCodes);
        sb.append(", includeConditions: [");
        sb.append(conditionsToString(includeConditions));
        sb.append("], excludeConditions: [");
        sb.append(conditionsToString(excludeConditions));
        sb.append("]");

        return sb.toString();
    }

    /**
     * Return a string representation of an array of conditions.
     *
     * @param conditions the conditions to stringify
     * @return the resulting string representation
     */
    private String conditionsToString(final LabelingRuleCondition[] conditions)
    {
        final StringBuilder sb = new StringBuilder();
        boolean appendComma = false;
        for (final LabelingRuleCondition condition : conditions)
        {
            if (appendComma)
            {
                sb.append(", ");
            }

            // nl
            switch (condition.getTypeCode())
            {
                case "n" -> sb.append("name: ").append(condition);
                case "l" -> sb.append("label: ").append(condition);
                case null, default -> sb.append("unknown");
            }

            appendComma = true;
        }

        return sb.toString();
    }

    /**
     * Return the new label (with all placeholders removed) for testing purposes
     *
     * @return the new label
     */
    public String getNewLabel()
    {
        return newLabel;
    }

    /**
     * Return the type codes for testing purposes
     *
     * @return the type codes
     */
    public Set<String> getTypeCodes()
    {
        return typeCodes;
    }

    /**
     * Return the "stopOnMatch" flag for testing purposes
     *
     * @return the "stopOnMatch" flag
     */
    public boolean getStopOnMatch()
    {
        return stopOnMatch;
    }

    /**
     * Return the include conditions for testing purposes
     *
     * @return the include conditions
     */
    public LabelingRuleCondition[] getIncludeConditions()
    {
        return includeConditions;
    }

    /**
     * Return the exclude conditions for testing purposes
     *
     * @return the exclude conditions
     */
    public LabelingRuleCondition[] getExcludeConditions()
    {
        return excludeConditions;
    }

    /**
     * Validates the entire rule for consistency. This includes checking that all placeholders in the new label have a
     * corresponding condition and that any capturing group index specified in a placeholder is valid for the associated
     * condition's pattern.
     *
     * @throws InvalidLabelingRuleException
     *             if the rule is invalid
     */
    private void validateRule() throws InvalidLabelingRuleException
    {
        for (final PlaceholderPosition placeholderPosition : newLabelPlaceholders)
        {
            for (final LabelingRuleCondition c : includeConditions)
            {
                if (c.getTypeCode().equals(placeholderPosition.typeCode))
                {
                    if (placeholderPosition.capturingGroupIndex > 0)
                    {
                        // check that the condition pattern has the expected matching group
                        final String pattern = c.getPattern();
                        final int nbCaptureGroups = RegExUtils.getCaptureGroupCount(pattern);

                        if (placeholderPosition.capturingGroupIndex > nbCaptureGroups)
                        {
                            throw new InvalidLabelingRuleException(String.format("Pattern '%s' has no matching group '%d'. Important: You can only capture in include rules.",
                                                                                 pattern, placeholderPosition.capturingGroupIndex));
                        }
                    }

                    break;
                }
            }
        }
    }
}
