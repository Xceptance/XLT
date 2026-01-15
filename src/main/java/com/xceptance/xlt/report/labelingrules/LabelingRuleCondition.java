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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.report.providers.TimerReport;

/**
 * Base class for all labeling rule conditions. Even if they don't use any regex, they will inherit from this class.
 */
public abstract class LabelingRuleCondition
{
    /**
     * Just a placeholder for a NULL in the cache because null is ambiguous.
     */
    private static final MatchResult NULL = Pattern.compile(".*").matcher("null").toMatchResult();

    /**
     * Cache for mapping input strings to existing match results.
     */
    private final Map<CharSequence, MatchResult> cache;

    /**
     * The matcher to use.
     */
    private final Matcher matcher;

    /**
     * The last state of the evaluation, so we don't have to look anything up.
     */
    private MatchResult lastFilterState;

    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching reports
     */
    public LabelingRuleCondition(final String regex)
    {
        this.matcher = StringUtils.isBlank(regex) ? null : RegExUtils.getPattern(regex, 0).matcher("any");
        this.cache = new HashMap<>();
    }

    /**
     * Returns the last filter state.
     *
     * @return the lastFilterState or null of it was a miss
     */
    public MatchResult getLastFilterState()
    {
        return lastFilterState;
    }

    /**
     * Returns the text to examine from the passed report object
     *
     * @return the text to check against
     */
    protected abstract CharSequence getText(final TimerReport report);

    protected CharSequence getNullSafeText(final TimerReport report)
    {
        return Optional.ofNullable(getText(report)).orElse("");
    }

    /**
     * Apply this condition to the given report object.
     *
     * @param report
     *            the timer report object to apply the condition to
     */
    protected boolean apply(final TimerReport report)
    {
        /**
         * Because we now have a special Condition that is taking care of empty patterns, we can assume that if we are
         * here, we have a pattern to match against. One less if!
         */

        // get the data to match against
        final CharSequence text = getNullSafeText(report);

        // check the cache if we already have done that
        MatchResult result = this.cache.get(text);
        if (result != null)
        {
            // ok, we got one, just see if this is NULL or a match
            if (result == NULL)
            {
                this.lastFilterState = null;
                return false;
            }
            else
            {
                this.lastFilterState = result;
                return true;
            }
        }

        // not found, produce and cache, recycle the matcher
        // cache only the result, not the matcher itself
        final Matcher m = this.matcher.reset(text);
        if (m.find())
        {
            result = m.toMatchResult();
            cache.put(text, result);

            this.lastFilterState = result;
            return true;
        }
        else
        {
            // remember the miss
            cache.put(text, NULL);

            this.lastFilterState = null;
            return false;
        }
    }

    /**
     * Returns the right text for the replacement of a certain group or if no group given, we return the entire text
     */
    protected CharSequence getReplacementText(final TimerReport report, final int capturingGroupIndex)
    {
        /**
         * If no capturing group is given, we return the entire text. Empty matchers no longer possible.
         */
        try
        {
            return capturingGroupIndex == -1 ? getNullSafeText(report) : this.getLastFilterState().group(capturingGroupIndex);
        }
        catch (final IndexOutOfBoundsException ioobe)
        {
            final String format = "No matching group %d for input string '%s' and pattern '%s'";
            ThrowableUtils.setMessage(ioobe, String.format(format, capturingGroupIndex, getNullSafeText(report), getPattern()));

            throw ioobe;
        }
    }

    /**
     * Returns the filter pattern string.
     */
    public String getPattern()
    {
        return (matcher == null) ? StringUtils.EMPTY : matcher.pattern().pattern();
    }

    /**
     * Whether this filter has an empty pattern.
     */
    protected boolean isEmpty()
    {
        return matcher == null;
    }

    /**
     * Returns the type code of this condition.
     *
     * @return the type code
     */
    public abstract String getTypeCode();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{ type: '");
        sb.append(getTypeCode()).append("', ");
        sb.append("pattern: '").append(getPattern()).append("' }");

        return sb.toString();
    }
}
