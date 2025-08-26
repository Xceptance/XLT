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
package com.xceptance.xlt.report.mergerules;

import java.util.ArrayList;
import java.util.Collections;
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
     * The return state for special handling, all >= 0 mean that we continue with a 
     * certain rule, -1 means that we drop the request and -2 means that we stop processing.
     */
    public static final int STOP = -2, DROP = -1;

    /**
     * The pattern to find placeholders in the new name.
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([acmnrstu])(?::(.+?))?\\}");

    /**
     * The definition of the new name without any placeholders.
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
     * The list of place where to insert the data to form a new name
     */
    private final PlaceholderPosition[] newNamePlaceholders;

    /**
     * The ID of this rule.
     */
    private final int id;

    /**
     * The ID of the rule to continue processing at if this rule matched.
     */
    private final int continueOnMatchAtId;

    /**
     * The ID of the rule to continue processing at if this rule did not match.
     */
    private final int continueOnNoMatchAtId;

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
     * @param urlPrecheckText
     * @throws InvalidRequestProcessingRuleException
     */
    public RequestProcessingRule(final int id,
                                 final NewName newName, 
                                 final RequestNamePattern requestNamePattern, 
                                 final UrlPattern urlPattern,
                                 final ContentTypePattern contentTypePattern, 
                                 final StatusCodePattern statusCodePattern, 
                                 final AgentNamePattern agentNamePattern,    
                                 final TransactionNamePattern transactionNamePattern, 
                                 final HttpMethodPattern httpMethodPattern,  
                                 final RunTimeRanges responseTimeRanges,
                                 final StopOnMatch stopOnMatch, 
                                 final RequestNameExcludePattern requestNameExcludePattern, 
                                 final UrlExcludePattern urlExcludePattern,
                                 final ContentTypeExcludePattern contentTypeExcludePattern, 
                                 final StatusCodeExcludePattern statusCodeExcludePattern,
                                 final AgentNameExcludePattern agentNameExcludePattern, 
                                 final TransactionNameExcludePattern transactionNameExcludePattern,
                                 final HttpMethodExcludePattern httpMethodExcludePattern, 
                                 final ContinueOnMatchAtId continueOnMatchAtId, 
                                 final ContinueOnNoMatchAtId continueOnNoMatchAtId,
                                 final DropOnMatch dropOnMatch)
                                     throws InvalidRequestProcessingRuleException
    {
        this(id, newName, requestNamePattern, urlPattern, contentTypePattern, statusCodePattern, agentNamePattern,
             transactionNamePattern, httpMethodPattern, responseTimeRanges, stopOnMatch, requestNameExcludePattern,
             urlExcludePattern, contentTypeExcludePattern, statusCodeExcludePattern, agentNameExcludePattern,
             transactionNameExcludePattern, httpMethodExcludePattern, continueOnMatchAtId, continueOnNoMatchAtId,
             dropOnMatch, new UrlPrecheckText(""));
    }
    
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
    public RequestProcessingRule(final int id,
                                 final NewName newName, 
                                 final RequestNamePattern requestNamePattern, 
                                 final UrlPattern urlPattern,
                                 final ContentTypePattern contentTypePattern, 
                                 final StatusCodePattern statusCodePattern, 
                                 final AgentNamePattern agentNamePattern,    
                                 final TransactionNamePattern transactionNamePattern, 
                                 final HttpMethodPattern httpMethodPattern,  
                                 final RunTimeRanges responseTimeRanges,
                                 final StopOnMatch stopOnMatch, 
                                 final RequestNameExcludePattern requestNameExcludePattern, 
                                 final UrlExcludePattern urlExcludePattern,
                                 final ContentTypeExcludePattern contentTypeExcludePattern, 
                                 final StatusCodeExcludePattern statusCodeExcludePattern,
                                 final AgentNameExcludePattern agentNameExcludePattern, 
                                 final TransactionNameExcludePattern transactionNameExcludePattern,
                                 final HttpMethodExcludePattern httpMethodExcludePattern, 
                                 final ContinueOnMatchAtId continueOnMatchAtId, 
                                 final ContinueOnNoMatchAtId continueOnNoMatchAtId,
                                 final DropOnMatch dropOnMatch,
                                 final UrlPrecheckText urlPrecheckText)
                                     throws InvalidRequestProcessingRuleException
    {
        this.id = id;
        this.stopOnMatch = stopOnMatch.value;
        this.dropOnMatch = dropOnMatch.value;
        this.continueOnMatchAtId = continueOnMatchAtId.value;
        this.continueOnNoMatchAtId = continueOnNoMatchAtId.value;

        // this aids against programming mistakes, not user errors
        // the same number means, we are just continue normally. This safes us from
        // any increment at the end
        if (this.continueOnMatchAtId < this.id)
        {
            throw new InvalidRequestProcessingRuleException(String.format("Continue on match rule ID (%s) must be greater or same than the rule ID (%s)", this.continueOnMatchAtId, this.id));
        }
        if (this.continueOnNoMatchAtId < this.id)
        {
            throw new InvalidRequestProcessingRuleException(String.format("Continue on no match rule ID (%s) must be greater or same than the rule ID (%s)", this.continueOnNoMatchAtId, this.id));
        }

        final ArrayList<AbstractRequestFilter> requestFilters = new ArrayList<>(20);

        // parse the placeholder positions now (and only once)
        final var tempPlaceHolderPositions = parsePlaceholderPositions(newName.value);

        try
        {  
            addIfTypeCodeInNewName(requestFilters, 
                                   new RequestNameRequestFilter(requestNamePattern.value), tempPlaceHolderPositions, requestNamePattern.value);
            if (StringUtils.isNotBlank(requestNameExcludePattern.value))
            {
                requestFilters.add(new RequestNameRequestFilter(requestNameExcludePattern.value, true));
            }

            addIfTypeCodeInNewName(requestFilters, new UrlRequestFilter(urlPattern.value, urlPrecheckText), tempPlaceHolderPositions, urlPattern.value);
            if (StringUtils.isNotBlank(urlExcludePattern.value))
            {
                requestFilters.add(new UrlRequestFilter(urlExcludePattern.value, true, urlPrecheckText));
            }

            addIfTypeCodeInNewName(requestFilters, new ContentTypeRequestFilter(contentTypePattern.value), tempPlaceHolderPositions, contentTypePattern.value);
            if (StringUtils.isNotBlank(contentTypeExcludePattern.value))
            {
                requestFilters.add(new ContentTypeRequestFilter(contentTypeExcludePattern.value, true));
            }

            addIfTypeCodeInNewName(requestFilters, new StatusCodeRequestFilter(statusCodePattern.value), tempPlaceHolderPositions, statusCodePattern.value);
            if (StringUtils.isNotBlank(statusCodeExcludePattern.value))
            {
                requestFilters.add(new StatusCodeRequestFilter(statusCodeExcludePattern.value, true));
            }

            addIfTypeCodeInNewName(requestFilters, new AgentNameRequestFilter(agentNamePattern.value), tempPlaceHolderPositions, agentNamePattern.value);
            if (StringUtils.isNotBlank(agentNameExcludePattern.value))
            {
                requestFilters.add(new AgentNameRequestFilter(agentNameExcludePattern.value, true));
            }

            addIfTypeCodeInNewName(requestFilters, new TransactionNameRequestFilter(transactionNamePattern.value), tempPlaceHolderPositions, transactionNamePattern.value);
            if (StringUtils.isNotBlank(transactionNameExcludePattern.value))
            {
                requestFilters.add(new TransactionNameRequestFilter(transactionNameExcludePattern.value, true));
            }

            addIfTypeCodeInNewName(requestFilters, new HttpMethodRequestFilter(httpMethodPattern.value), tempPlaceHolderPositions, httpMethodPattern.value);
            if (StringUtils.isNotBlank(httpMethodExcludePattern.value))
            {
                requestFilters.add(new HttpMethodRequestFilter(httpMethodExcludePattern.value, true));
            }

            addIfTypeCodeInNewName(requestFilters, new ResponseTimeRequestFilter(responseTimeRanges.value), tempPlaceHolderPositions, responseTimeRanges.value);
        }
        catch (final PatternSyntaxException pse)
        {
            throw new InvalidRequestProcessingRuleException("Invalid regular expression: " + pse.getPattern());
        }

        this.requestFilters = requestFilters.toArray(new AbstractRequestFilter[requestFilters.size()]);
        this.newName = adjustNewName(newName.value, tempPlaceHolderPositions);
        this.newNamePlaceholders = condensePlaceHolders(tempPlaceHolderPositions);

        resolveRequestFilter(this.newNamePlaceholders);
        
        // Validate the entire rule.
        validateRule();
    }

    /**
     * To speed things up, we match request filters to placeholder 
     */
    private void resolveRequestFilter(final PlaceholderPosition[] positions)
    {
        // we have to resolve the request filter for each placeholder position
        for (final PlaceholderPosition pos : positions)
        {
            for (final AbstractRequestFilter filter : requestFilters)
            {
                if (filter.getTypeCode().equals(pos.typeCode))
                {
                    // we found the filter, so set it
                    pos.requestFilter = filter;
                    break;
                }
            }
        }
    }
    
    /**
     * We might have unused placeholders, we can remove them
     * 
     * @param positions the list of placeholder positions
     * @return an array of placeholder positions that are truly used
     */
    private PlaceholderPosition[] condensePlaceHolders(final List<PlaceholderPosition> positions)
    {
        return positions.stream()
                        .filter(p -> p.used)
                        .toArray(PlaceholderPosition[]::new);
    }
    
    
    /**
     * Adds this filter to the filter rule list if the pattern is not empty and the results are later needed in the new
     * name, otherwise we just ignore it to save cyles. Important, we must keep it if it is not empty but also not
     * needed in the new name. 
     * 
     * @param filters the list of filters to add to
     * @param filter the filter to add
     * @param placeHolderPositions the list of placeholder positions to check against
     */
    private void addIfTypeCodeInNewName(final List<AbstractRequestFilter> filters, final AbstractRequestFilter filter, 
                                        final List<PlaceholderPosition> placeHolderPositions, final String pattern)
    {
        final String typeCode = filter.getTypeCode();

        // if the pattern is empty, we need to know if we might need the data anyway
        // such as {c:0} but nothing is in the ContentPattern
        // add the filter only if we need it as source of data
        boolean alreadyAdded = false;
        for (final PlaceholderPosition p : placeHolderPositions)
        {
            if (p.typeCode.equals(typeCode))
            {
                // yes, we play a role and we are not yet in the list
                if (!alreadyAdded)
                {
                    filters.add(filter);
                }
                
                // mark position it, so we keep it
                p.used = true;
                alreadyAdded = true;
            }
        }

        // in case we have not found the typecode to be used in the new name,
        // we see if we still want to apply it as a filter, so we have to 
        // keep it, otherwise we can skip it
        if (!alreadyAdded)
        {
            if (pattern != null && !pattern.isBlank())
            {
                // the pattern is not empty, add it, so we want to filter with it
                filters.add(filter);
            }
            
        }
        else
        {
            // well, we don't add it, because we don't need it
        }
    }

    /**
     * Parses the position of the placeholders in the new name field of the rule.
     * Composes a newName without the placeholders for later speed
     * 
     * @throws InvalidRequestProcessingRuleException
     */
    private List<PlaceholderPosition> parsePlaceholderPositions(final String nameWithPlaceholders)
        throws InvalidRequestProcessingRuleException
    {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(nameWithPlaceholders);

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
                    throw new InvalidRequestProcessingRuleException(
                                                                    String.format("Failed to parse the matching group index '%s' as integer", 
                                                                                  matchingGroupIndexString));
                }
            }

            // build and remember the placeholder position
            final PlaceholderPosition position = new PlaceholderPosition(matcher.group(1), matchingGroupIndex, matcher.start(),
                                                                         matcher.end(), matcher.group().length());
            positions.add(position);
        }

        
        // to avoid calculations later, we revert the positions
        Collections.reverse(positions);
        
        // get us an efficient array for later
        return positions;
    }

    /**
     * Get us a new name that has not longer any placeholders in it, but rather the positions where
     * to insert data.
     * 
     * @param newName the current name with placeholders
     * @param positions a set of already parsed placeholder positions 
     * @return the new name clean of placeholders 
     */
    public static String adjustNewName(final String nameWithPlaceholders, final List<PlaceholderPosition> positions)
    {
        // create us a new name without the placeholders, so we can later easily just insert the replacements without
        // removing or calculating the position
        final StringBuilder name = new StringBuilder(nameWithPlaceholders);
        int displacement = 0;
        
        // we already reversed the positions, so do it the other way around
        for (int i = positions.size() - 1; i >= 0; i--)
        {
            final PlaceholderPosition pos = positions.get(i);
            if (!pos.used)
            {
                // we don't use this placeholder, so skip it
                continue;
            }

            // append the part before the placeholder
            name.delete(pos.start - displacement, pos.end - displacement);

            // update the placeholder, it is now different from before because it does not 
            // really reflect room anymore, rather a position
            positions.set(i, new PlaceholderPosition(pos.typeCode, pos.capturingGroupIndex, pos.start - displacement, pos.used));
            
            // update the sum up shift so far
            displacement += pos.length;
        }
        
        return name.toString();
    }
    
    /**
     * Processes this request merge rule. As a consequence, the passed request data object will (or will not) get a new
     * name. This is a multi-threaded routine aka in use by several threads at the same time.
     *
     * @param requestData
     *            the request data object to process, will also be directly modified as result
     * @return true if we want to stop, false otherwise
     */
    public int process(final RequestData requestData)
    {
        // try each filter and remember its state for later processing
        final int requestFiltersSize = requestFilters.length;

        for (int i = 0; i < requestFiltersSize; i++)
        {
            final AbstractRequestFilter filter = requestFilters[i];
            final var state = filter.appliesTo(requestData);

            if (state == null)
            {
                // return early since one of the filters did *not* apply

                // continue request processing with an unmodified result
                return continueOnNoMatchAtId;
            }
        }

        // all filters applied so we can process the request, but check first what to do
        if (dropOnMatch)
        {
            // stop request processing with a null request
            return DROP;
        }

        // anything to do?
        if (newNamePlaceholders.length > 0)
        {
            // rename the request
            final StringBuilder result = new StringBuilder(50);
            result.append(newName);

            // search as long as there are placeholders in the name
            for (final PlaceholderPosition placeholder : newNamePlaceholders)
            {
                final AbstractRequestFilter requestFilter = placeholder.requestFilter;

                // get replacement
                final CharSequence replacement = requestFilter.getReplacementText(requestData, placeholder.capturingGroupIndex);

                // replace the placeholder with the real values
                result.insert(placeholder.start, replacement);
            }

            // set the final name
            requestData.setName(result.toString());
        }
        else
        {
            // nothing to do, keep the newName
            requestData.setName(newName);
        }

        return stopOnMatch ? STOP : continueOnMatchAtId;
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

    /**
     * Return the rule ID
     * 
     * @return the rule ID
     */
    public int getId()
    {
        return this.id;
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

                        if (placeholderPosition.capturingGroupIndex > 0)
                        {
                            // check that the filter pattern has the wanted matching group
                            final String pattern = patternFilter.getPattern();
                            final int nbCaptureGroups = RegExUtils.getCaptureGroupCount(pattern);
                            if (placeholderPosition.capturingGroupIndex > nbCaptureGroups)
                            {
                                throw new InvalidRequestProcessingRuleException(String.format("Pattern '%s' has no matching group '%d'",
                                                                                              pattern, placeholderPosition.capturingGroupIndex));
                            }
                        }
                    }

                    break;
                }
            }
        }
    }

    /*
     * These are just for type-safety and to make testing nicer. It has not runtime effect, because
     * we only once construct a rule. So it is overall still cheapish.
     */
    public static record NewName(String value) {};
    public static record RequestNamePattern(String value) {}; 
    public static record UrlPattern(String value) {};
    public static record ContentTypePattern(String value) {}; 
    public static record StatusCodePattern(String value) {}; 
    public static record AgentNamePattern(String value) {};   
    public static record TransactionNamePattern(String value) {}; 
    public static record HttpMethodPattern(String value) {};  
    public static record RunTimeRanges(String value) {};
    public static record RequestNameExcludePattern(String value) {}; 
    public static record UrlExcludePattern(String value) {};
    public static record ContentTypeExcludePattern(String value) {}; 
    public static record StatusCodeExcludePattern(String value) {};
    public static record AgentNameExcludePattern(String value) {}; 
    public static record TransactionNameExcludePattern(String value) {};
    public static record HttpMethodExcludePattern(String value) {}; 

    public static record StopOnMatch(boolean value) {}; 
    public static record DropOnMatch(boolean value) {}; 
    public static record ContinueOnMatchAtId(int value) {}; 
    public static record ContinueOnNoMatchAtId(int value) {}; 
    
    public static record UrlPrecheckText(String value) {};
}
