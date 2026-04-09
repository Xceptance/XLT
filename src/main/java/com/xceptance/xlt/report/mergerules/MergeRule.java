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
package com.xceptance.xlt.report.mergerules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.mergerules.agent.AgentNameCondition;
import com.xceptance.xlt.report.mergerules.contenttype.ContentTypeCondition;
import com.xceptance.xlt.report.mergerules.httpmethod.HttpMethodCondition;
import com.xceptance.xlt.report.mergerules.requestname.RequestNameCondition;
import com.xceptance.xlt.report.mergerules.responsetime.ResponseTimeCondition;
import com.xceptance.xlt.report.mergerules.statuscode.StatusCodeCondition;
import com.xceptance.xlt.report.mergerules.transaction.TransactionNameCondition;
import com.xceptance.xlt.report.mergerules.url.UrlCondition;

/**
 * A {@link MergeRule} governs the process of "merging" different requests into one request by renaming the
 * requests. It represents a bundle of conditions a request must meet to be renamed and defines how the new name will look
 * like. As a special processing case, a request matching the filter criteria may also be marked as to-be-discarded.
 * 
 * Conditions can be including or excluding based on conditions. If any exclude condition matches, 
 * the rule is not applied. Only if all include conditions match, the rule is applied.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class MergeRule
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
     * The list of configured include conditions, these also make up the pieces of the new name
     */
    private final Condition[] includeConditions;

    /**
     * The list of configured exclude conditions
     */
    private final Condition[] excludeConditions;

    /**
     * The list of placeholders with info where to insert the data to form a new name.
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
     * @param id
     *          the ID of this rule
     * @param newName
     *          the new name for requests matching this rule, may contain placeholders
     * @param requestNamePattern
     *          the request name pattern to match, may be empty
     * @param urlPattern
     *          the URL pattern to match, may be empty
     * @param contentTypePattern
     *          the content type pattern to match, may be empty
     * @param statusCodePattern
     *          the status code pattern to match, may be empty
     * @param agentNamePattern
     *         the agent name pattern to match, may be empty
     * @param transactionNamePattern
     *         the transaction name pattern to match, may be empty
     * @param httpMethodPattern
     *         the HTTP method pattern to match, may be empty
     * @param responseTimeRanges
     *         the response time ranges to match, may be empty
     * @param stopOnMatch
     *         whether or not to stop processing further rules if this rule matched
     * @param requestNameExcludePattern
     *         the request name exclude pattern to match, may be empty
     * @param urlExcludePattern
     *         the URL exclude pattern to match, may be empty
     * @param contentTypeExcludePattern
     *         the content type exclude pattern to match, may be empty
     * @param statusCodeExcludePattern
     *         the status code exclude pattern to match, may be empty
     * @param agentNameExcludePattern
     *         the agent name exclude pattern to match, may be empty
     * @param transactionNameExcludePattern
     *         the transaction name exclude pattern to match, may be empty
     * @param httpMethodExcludePattern
     *         the HTTP method exclude pattern to match, may be empty
     * @param continueOnMatchAtId
     *         the ID of the rule to continue processing at if this rule matched
     * @param continueOnNoMatchAtId
     *         the ID of the rule to continue processing at if this rule did not match
     * @param dropOnMatch
     *         whether or not to drop requests matching this rule, implies stopping further processing
     * @param urlText
     *         the URL text to match, may be empty
     * @param urlTextExclude
     *         the URL text exclude to match, may be empty
     * 
     * @throws InvalidMergeRuleException
     */
    public MergeRule(final int id,
                     final NewName newName, 
                     final NamePattern requestNamePattern, 
                     final UrlPattern urlPattern,
                     final ContentTypePattern contentTypePattern, 
                     final StatusCodePattern statusCodePattern, 
                     final AgentNamePattern agentNamePattern,    
                     final TransactionNamePattern transactionNamePattern, 
                     final HttpMethodPattern httpMethodPattern,  
                     final RunTimeRanges responseTimeRanges,
                     final StopOnMatch stopOnMatch, 
                     final NameExcludePattern requestNameExcludePattern, 
                     final UrlExcludePattern urlExcludePattern,
                     final ContentTypeExcludePattern contentTypeExcludePattern, 
                     final StatusCodeExcludePattern statusCodeExcludePattern,
                     final AgentNameExcludePattern agentNameExcludePattern, 
                     final TransactionNameExcludePattern transactionNameExcludePattern,
                     final HttpMethodExcludePattern httpMethodExcludePattern, 
                     final ContinueOnMatchAtId continueOnMatchAtId, 
                     final ContinueOnNoMatchAtId continueOnNoMatchAtId,
                     final DropOnMatch dropOnMatch,
                     final UrlText urlText,
                     final UrlTextExclude urlTextExclude)
                         throws InvalidMergeRuleException
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
            throw new InvalidMergeRuleException(
                                                String.format("Continue on match rule ID (%s) must be greater or same than the rule ID (%s)", 
                                                              this.continueOnMatchAtId, this.id));
        }
        if (this.continueOnNoMatchAtId < this.id)
        {
            throw new InvalidMergeRuleException(
                                                String.format("Continue on no match rule ID (%s) must be greater or same than the rule ID (%s)", 
                                                              this.continueOnNoMatchAtId, this.id));
        }

        // parse the placeholder positions now (and only once)
        final var tempPlaceHolderPositions = parsePlaceholderPositions(newName.value);

        List<Condition> includeConditions = new ArrayList<>();
        List<Condition> excludeConditions = new ArrayList<>();

        try
        {  
            // set all up and complain later and also filter the placeholders out
            // that we don't need
            includeConditions.add(RequestNameCondition.build(requestNamePattern.value));
            excludeConditions.add(RequestNameCondition.build(requestNameExcludePattern.value));

            includeConditions.add(UrlCondition.build(urlPattern.value, urlText.value));
            excludeConditions.add(UrlCondition.build(urlExcludePattern.value, urlTextExclude.value));

            includeConditions.add(ContentTypeCondition.build(contentTypePattern.value));
            excludeConditions.add(ContentTypeCondition.build(contentTypeExcludePattern.value));

            includeConditions.add(StatusCodeCondition.build(statusCodePattern.value));
            excludeConditions.add(StatusCodeCondition.build(statusCodeExcludePattern.value));

            includeConditions.add(AgentNameCondition.build(agentNamePattern.value));
            excludeConditions.add(AgentNameCondition.build(agentNameExcludePattern.value));

            includeConditions.add(TransactionNameCondition.build(transactionNamePattern.value));
            excludeConditions.add(TransactionNameCondition.build(transactionNameExcludePattern.value));

            includeConditions.add(HttpMethodCondition.build(httpMethodPattern.value));
            excludeConditions.add(HttpMethodCondition.build(httpMethodExcludePattern.value));

            includeConditions.add(new ResponseTimeCondition(responseTimeRanges.value));
        }
        catch (final PatternSyntaxException pse)
        {
            throw new InvalidMergeRuleException("Invalid regular expression: " + pse.getPattern());
        }

        // remove all filters we don't need
        includeConditions = cleanUpIncludeConditions(tempPlaceHolderPositions, includeConditions);
        excludeConditions = cleanUpExcludeConditions(excludeConditions);

        // boil it down to arrays for later speed
        this.includeConditions = includeConditions.toArray(new Condition[includeConditions.size()]);
        this.excludeConditions = excludeConditions.toArray(new Condition[excludeConditions.size()]);

        this.newName = adjustNewName(newName.value, tempPlaceHolderPositions);
        this.newNamePlaceholders = tempPlaceHolderPositions.toArray(new PlaceholderPosition[tempPlaceHolderPositions.size()]);

        resolveRequestFilter(this.newNamePlaceholders);

        // Validate the entire rule set, because we might still have issues
        validateRule();
    }

    /**
     * Cleanup our conditions down to the ones we really need for creating a new name
     * or simply to be a filter only
     * 
     * @param positions the list of placeholder positions
     * @param conditions the list of conditions to clean up
     */
    private List<Condition> cleanUpIncludeConditions(final List<PlaceholderPosition> positions, final List<Condition> conditions)
    {
        return conditions.stream()
            .filter(c -> 
            {
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
            })
            .toList();
    }

    /**
     * Our excludes are not providing any data, so we can remove all that are not used as filter conditions
     * 
     * @param positions the list of placeholder positions
     * @param conditions the list of conditions to clean up
     */
    private List<Condition> cleanUpExcludeConditions(final List<Condition> conditions)
    {
        return conditions.stream()
            .filter(c -> !c.isEmpty())
            .toList();
    }

    /**
     * To speed things up, we match merge include conditions to placeholders
     * 
     * @param positions the list of placeholder positions
     */
    private void resolveRequestFilter(final PlaceholderPosition[] positions)
    {
        // we have to resolve the request condition for each placeholder position
        for (final PlaceholderPosition pos : positions)
        {
            for (final Condition condition : includeConditions)
            {
                if (condition.getTypeCode().equals(pos.typeCode))
                {
                    // we found the filter, so set it
                    pos.condition = condition;
                    break;
                }
            }
        }
    }

    /**
     * Parses the position of the placeholders in the new name field of the rule.
     * Composes a newName without the placeholders for later speed
     * 
     * @param nameWithPlaceholders the name with placeholders
     * 
     * @throws InvalidMergeRuleException
     */
    private List<PlaceholderPosition> parsePlaceholderPositions(final String nameWithPlaceholders)
        throws InvalidMergeRuleException
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
                    throw new InvalidMergeRuleException(
                                                        String.format("Failed to parse the matching group index '%s' as integer", 
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

        // get us an efficient array for later
        return positions;
    }

    /**
     * Get us a new name that has not longer any placeholders in it. Instead the placeholder positions get updated with
     * information where to insert data in the new name.
     * 
     * @param nameWithPlaceholders
     *            the current name with placeholders
     * @param positions
     *            a set of already parsed placeholder positions
     * @return the new name clean of placeholders
     */
    public static String adjustNewName(final String nameWithPlaceholders, final List<PlaceholderPosition> positions)
    {
        final StringBuilder name = new StringBuilder(nameWithPlaceholders);
        int displacement = 0;

        // we already reversed the positions, so do it the other way around
        for (int i = positions.size() - 1; i >= 0; i--)
        {
            final PlaceholderPosition pos = positions.get(i);

            // append the part before the placeholder
            name.delete(pos.start - displacement, pos.end - displacement);

            // update the placeholder, it is now different from before because it does not
            // really reflect room anymore, rather a position
            positions.set(i, new PlaceholderPosition(pos.typeCode, pos.capturingGroupIndex, pos.start - displacement));

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
     * @return the ID of the next rule to process or {@link #STOP} to stop further processing or {@link #DROP} to drop
     *         the request
     */
    public int process(final RequestData requestData)
    {
        // do the excludes first because they might tell us to skip 
        for (final Condition condition : excludeConditions)
        {
            final var state = condition.apply(requestData);

            if (state == true)
            {
                // return early since one of the excludes applied
                // continue request processing with an unmodified result
                return continueOnNoMatchAtId;
            }
        }

        // try each include filter
        for (final Condition condition : includeConditions)
        {
            final var state = condition.apply(requestData);

            if (state == false)
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
                final Condition requestFilter = placeholder.condition;

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

        final List<Condition> conditions = new ArrayList<>();
        Arrays.stream(includeConditions).forEach(conditions::add);
        Arrays.stream(excludeConditions).forEach(conditions::add);

        for (final Condition condition : conditions)
        {
            if (appendComma)
            {
                sb.append(", ");
            }

            // acnrstu
            switch (condition.getTypeCode())
            {
                case "a" -> sb.append("agentName: ").append(condition.toString());
                case "c" -> sb.append("contentType: ").append(condition.toString());
                case "n" -> sb.append("requestName: ").append(condition.toString());
                case "r" -> sb.append("responseTime: ").append(condition.toString());
                case "s" -> sb.append("statusCode: ").append(condition.toString());
                case "t" -> sb.append("txnName: ").append(condition.toString());
                case "m" -> sb.append("httpMethod: ").append(condition.toString());
                case "u" -> sb.append("url: ").append(condition.toString());
                default -> 
                {
                    sb.append("unknown");
                }
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

    /**
     * Return the include conditions for testing purposes
     * 
     * @return the include conditions
     */
    public Condition[] getIncludeConditions()
    {
        return includeConditions;
    }

    /**
     * Return the exclude conditions for testing purposes
     *
     * @return the exclude conditions
     */
    public Condition[] getExcludeConditions()
    {
        return excludeConditions;
    }

    /**
     * Validates the entire rule for consistency. This includes checking that all placeholders in the new name have a
     * corresponding condition and that any capturing group index specified in a placeholder is valid for the associated
     * condition's pattern.
     * 
     * @throws InvalidMergeRuleException
     *             if the rule is invalid
     */
    private void validateRule() throws InvalidMergeRuleException
    {
        for (final PlaceholderPosition placeholderPosition : newNamePlaceholders)
        {
            for (final Condition c : includeConditions)
            {
                if (c.getTypeCode().equals(placeholderPosition.typeCode))
                {
                    if (placeholderPosition.capturingGroupIndex > 0)
                    {
                        // check that the filter pattern has the wanted matching group
                        final String pattern = c.getPattern();
                        final int nbCaptureGroups = RegExUtils.getCaptureGroupCount(pattern);

                        if (placeholderPosition.capturingGroupIndex > nbCaptureGroups)
                        {
                            throw new InvalidMergeRuleException(
                                                                String.format("Pattern '%s' has no matching group '%d'. Important: You can only capture in include rules.",
                                                                              pattern, placeholderPosition.capturingGroupIndex));
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
    public static record NamePattern(String value) {}; 
    public static record UrlPattern(String value) {};
    public static record ContentTypePattern(String value) {}; 
    public static record StatusCodePattern(String value) {}; 
    public static record AgentNamePattern(String value) {};   
    public static record TransactionNamePattern(String value) {}; 
    public static record HttpMethodPattern(String value) {};  
    public static record RunTimeRanges(String value) {};

    public static record NameExcludePattern(String value) {}; 
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

    public static record UrlText(String value) {};
    public static record UrlTextExclude(String value) {};
}
