/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.util.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Data;

/**
 * Base class for data processing rules. At the moment, this class provides common helpers only.
 */
public class AbstractDataProcessingRule<T extends Data>
{
    /**
     * The pattern to find placeholders in the new label.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([nl])(?::([0-9]+))?\\}");

    /**
     * Adds this filter to the filter rule list if the pattern is not empty and the results is later needed in the new
     * name, otherwise we just ignore it to save cyles
     */
    protected void addIfTypeCodeInNewName(final List<AbstractFilter<T>> filters, final AbstractFilter<T> filter, final String pattern,
                                          final PlaceholderPosition[] newNamePlaceholders)
    {
        final String typeCode = filter.getTypeCode();

        // if the pattern is empty, we need to know if we might need the data anyway
        if (pattern == null || "".equals(pattern))
        {
            // add the filter only if we need it as source of data
            for (final PlaceholderPosition p : newNamePlaceholders)
            {
                if (p.typeCode.equals(typeCode))
                {
                    // yes, we play a role
                    filters.add(filter);
                    return;
                }
            }
        }
        else
        {
            // the pattern is not empty, add it
            filters.add(filter);
        }

        // well, we don't add it, because we don't need it
    }

    /**
     * Parses the position of the placeholders in the new name field of the rule.
     *
     * @param newNameWithPlaceholders
     *            the new name containing placeholders
     * @return the placeholder positions found
     * @throws InvalidLabelingRuleException
     */
    protected PlaceholderPosition[] parsePlaceholderPositions(final String newNameWithPlaceholders)
        throws InvalidDataProcessingRuleException
    {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(newNameWithPlaceholders);

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
                    throw new InvalidDataProcessingRuleException("Failed to parse the matching group index '" + matchingGroupIndexString +
                                                                 "' as integer");
                }
            }

            // build and remember the placeholder position
            final PlaceholderPosition position = new PlaceholderPosition(matcher.group(1), matchingGroupIndex, matcher.start(),
                                                                         matcher.end(), matcher.group().length());
            positions.add(position);
        }

        // get us an efficient array for later, we won't change things anymore
        return positions.toArray(new PlaceholderPosition[positions.size()]);
    }

    protected void validateRule(final AbstractFilter<T>[] filters, final PlaceholderPosition[] newPlaceholderPositions)
        throws InvalidDataProcessingRuleException
    {
        for (final PlaceholderPosition placeholderPosition : newPlaceholderPositions)
        {
            for (final AbstractFilter<T> filter : filters)
            {
                if (filter.getTypeCode().equals(placeholderPosition.typeCode))
                {
                    if (filter instanceof AbstractPatternFilter)
                    {
                        final AbstractPatternFilter<T> patternFilter = (AbstractPatternFilter<T>) filter;

                        // TODO: #3252
                        // if (placeholderPosition.index != -1)
                        // {
                        // // check that we have a pattern at all
                        // if (patternFilter.isEmpty())
                        // {
                        // throw new InvalidRequestProcessingRuleException(String.format("Matching group '%d' specified,
                        // but there is no pattern",
                        // placeholderPosition.index));
                        // }
                        // }

                        if (placeholderPosition.index > 0)
                        {
                            // check that the filter pattern has the wanted matching group
                            final String pattern = patternFilter.getPattern();
                            final int nbCaptureGroups = RegExUtils.getCaptureGroupCount(pattern);
                            if (placeholderPosition.index > nbCaptureGroups)
                            {
                                throw new InvalidDataProcessingRuleException(String.format("Pattern '%s' has no matching group '%d'",
                                                                                           pattern, placeholderPosition.index));
                            }
                        }
                    }

                    break;
                }
            }
        }
    }
}
