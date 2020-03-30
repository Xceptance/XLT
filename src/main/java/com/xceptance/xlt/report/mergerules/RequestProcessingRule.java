/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.mergerules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * A {@link RequestProcessingRule} governs the process of "merging" different requests into one request by renaming the
 * requests. It represents a bundle of criteria a request must meet to be renamed and defines how the new name will look
 * like. As a special processing case, a request matching the filter criteria may also be marked as to-be-discarded.
 */
public class RequestProcessingRule
{
    /**
     * The pattern to find placeholders in the new name.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([acnrstu])(?::([0-9]+))?\\}");

    /**
     * The definition of the new name including placeholders.
     */
    private final String newName;

    /**
     * Whether or not to process the next rule if the current rule applied.
     */
    private final boolean stopOnMatch;

    /**
     * Whether or not to return <code>null</code> for any matching request data. Also implies that no further rules will
     * be processed.
     */
    private final boolean dropOnMatch;

    /**
     * The list of configured request filters of this rule.
     */
    private final AbstractRequestFilter[] requestFilters;

    /**
     * The list of placeholders (with their position, etc.) in the new name.
     */
    private final PlaceholderPosition[] newNamePlaceholders;

    /**
     * Constructor.
     *
     * @param newName
     * @param requestNamePattern
     * @param urlPattern
     * @param contentTypePattern
     * @param statusCodePattern
     * @param agentNamePattern
     * @param transactionNamePattern
     * @param responseTimeRanges
     * @param stopOnMatch
     * @param requestNameExcludePattern
     * @param urlExcludePattern
     * @param contentTypeExcludePattern
     * @param statusCodeExcludePattern
     * @param agentNameExcludePattern
     * @param transactionNameExcludePattern
     * @param dropOnMatch
     * @throws InvalidRequestProcessingRuleException
     */
    public RequestProcessingRule(final String newName, final String requestNamePattern, final String urlPattern,
                                 final String contentTypePattern, final String statusCodePattern, final String agentNamePattern,
                                 final String transactionNamePattern, final String responseTimeRanges, final boolean stopOnMatch,
                                 final String requestNameExcludePattern, final String urlExcludePattern,
                                 final String contentTypeExcludePattern, final String statusCodeExcludePattern,
                                 final String agentNameExcludePattern, final String transactionNameExcludePattern,
                                 final boolean dropOnMatch)
        throws InvalidRequestProcessingRuleException
    {
        this.newName = newName;
        this.stopOnMatch = stopOnMatch;
        this.dropOnMatch = dropOnMatch;

        final ArrayList<AbstractRequestFilter> requestFilters = new ArrayList<>(20);

        try
        {
            requestFilters.add(new RequestNameRequestFilter(requestNamePattern));
            requestFilters.add(new UrlRequestFilter(urlPattern));
            requestFilters.add(new ContentTypeRequestFilter(contentTypePattern));
            requestFilters.add(new StatusCodeRequestFilter(statusCodePattern));
            requestFilters.add(new AgentNameRequestFilter(agentNamePattern));
            requestFilters.add(new TransactionNameRequestFilter(transactionNamePattern));
            requestFilters.add(new ResponseTimeRequestFilter(responseTimeRanges));

            // excludes
            if (StringUtils.isNotBlank(requestNameExcludePattern))
            {
                requestFilters.add(new RequestNameRequestFilter(requestNameExcludePattern, true));
            }

            if (StringUtils.isNotBlank(urlExcludePattern))
            {
                requestFilters.add(new UrlRequestFilter(urlExcludePattern, true));
            }

            if (StringUtils.isNotBlank(contentTypeExcludePattern))
            {
                requestFilters.add(new ContentTypeRequestFilter(contentTypeExcludePattern, true));
            }

            if (StringUtils.isNotBlank(statusCodeExcludePattern))
            {
                requestFilters.add(new StatusCodeRequestFilter(statusCodeExcludePattern, true));
            }

            if (StringUtils.isNotBlank(agentNameExcludePattern))
            {
                requestFilters.add(new AgentNameRequestFilter(agentNameExcludePattern, true));
            }

            if (StringUtils.isNotBlank(transactionNameExcludePattern))
            {
                requestFilters.add(new TransactionNameRequestFilter(transactionNameExcludePattern, true));
            }
        }
        catch (final PatternSyntaxException pse)
        {
            throw new InvalidRequestProcessingRuleException("Invalid regular expression: " + pse.getPattern());
        }

        this.requestFilters = requestFilters.toArray(new AbstractRequestFilter[requestFilters.size()]);

        // parse the placeholder positions now (and only once)
        newNamePlaceholders = parsePlaceholderPositions(newName);

        // Validate the entire rule.
        validateRule();
    }

    /**
     * Parses the position of the placeholders in the new name field of the rule.
     *
     * @param newNameWithPlaceholders
     *            the new name containing placeholders
     * @return the placeholder positions found
     * @throws InvalidRequestProcessingRuleException
     */
    private static PlaceholderPosition[] parsePlaceholderPositions(final String newNameWithPlaceholders)
        throws InvalidRequestProcessingRuleException
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
                    throw new InvalidRequestProcessingRuleException("Failed to parse the matching group index '" +
                                                                    matchingGroupIndexString + "' as integer");
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

    /**
     * Processes this request merge rule. As a consequence, the passed request data object will (or will not) get a new
     * name. This is a multi-threaded routine aka in use by several threads at the same time.
     *
     * @param requestData
     *            the request data object to process
     * @return <code>true</code> if processing is complete, or <code>false</code> if other merge rules should be applied
     */
    public RequestProcessingRuleResult process(final RequestData requestData)
    {
        // try each filter and remember its state for later processing
        final int requestFiltersSize = requestFilters.length;
        final Object[] filterStates = new Object[requestFiltersSize];

        for (int i = 0; i < requestFiltersSize; i++)
        {
            final AbstractRequestFilter filter = requestFilters[i];
            filterStates[i] = filter.appliesTo(requestData);
            if (filterStates[i] == null)
            {
                // return early since one of the filters did *not* apply

                // continue request processing with an unmodified result
                return new RequestProcessingRuleResult(requestData, false);
            }
        }

        // all filters applied so we can process the request, but check first what to do
        if (dropOnMatch)
        {
            // stop request processing with a null request
            return new RequestProcessingRuleResult(null, true);
        }

        // anything to do?
        if (newNamePlaceholders.length > 0)
        {
            // rename the request
            final StringBuilder result = new StringBuilder(newName);

            // search as long as there are placeholders in the name
            int displacement = 0;
            for (final PlaceholderPosition placeholder : newNamePlaceholders)
            {
                // find the corresponding filter and filter state
                for (int i = 0; i < requestFiltersSize; i++)
                {
                    final AbstractRequestFilter requestFilter = requestFilters[i];

                    // check if this is our type code (compare it efficiently)
                    if (requestFilter.isSameTypeCode(placeholder.typeCode, placeholder.typeCodeHashCode))
                    {
                        final int capturingGroupIndex = placeholder.index;
                        final Object filterState = filterStates[i];

                        // get replacement
                        final String replacement = requestFilter.getReplacementText(requestData, capturingGroupIndex, filterState);

                        // replace the placeholder with the real values
                        result.replace(placeholder.start + displacement, placeholder.end + displacement, replacement);

                        // adjust the displacement for the next replace
                        displacement += replacement.length() - placeholder.length;

                        break;
                    }
                }
            }

            // set the final name
            requestData.setName(result.toString());
        }
        else
        {
            // nothing to do, keep the newName
            requestData.setName(newName);
        }

        return new RequestProcessingRuleResult(requestData, stopOnMatch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Naming rule: '").append(newName).append("', filters:  [");
        boolean appendComma = false;
        for (final AbstractRequestFilter requestFilter : requestFilters)
        {
            final String typeCode = requestFilter.getTypeCode();

            if (appendComma)
            {
                sb.append(", ");
            }

            // acnrstu
            if ("a".equals(typeCode))
            {
                sb.append("agentName: ").append(requestFilter.toString());
            }
            else if ("c".equals(typeCode))
            {
                sb.append("contentType: ").append(requestFilter.toString());
            }
            else if ("n".equals(typeCode))
            {
                sb.append("requestName: ").append(requestFilter.toString());
            }
            else if ("r".equals(typeCode))
            {
                sb.append("responseTime:").append(requestFilter.toString());
            }
            else if ("s".equals(typeCode))
            {
                sb.append("statusCode:").append(requestFilter.toString());
            }
            else if ("t".equals(typeCode))
            {
                sb.append("txnName: ").append(requestFilter.toString());
            }
            else if ("u".equals(typeCode))
            {
                sb.append("requestURL: ").append(requestFilter.toString());
            }
            else
            {
                sb.append("unknown");
            }

            appendComma = true;
        }
        sb.append("]");

        return sb.toString();
    }

    private void validateRule() throws InvalidRequestProcessingRuleException
    {
        for (final PlaceholderPosition placeholderPosition : newNamePlaceholders)
        {
            for (final AbstractRequestFilter filter : requestFilters)
            {
                if (filter.getTypeCode().equals(placeholderPosition.typeCode))
                {
                    if (filter instanceof AbstractPatternRequestFilter)
                    {
                        final AbstractPatternRequestFilter patternFilter = (AbstractPatternRequestFilter) filter;

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
                                throw new InvalidRequestProcessingRuleException(String.format("Pattern '%s' has no matching group '%d'",
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
