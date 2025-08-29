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

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.collection.LRUClockMap;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * Base class for all request filters that use regular expressions to identify matching requests.
 */
public abstract class AbstractPatternRequestFilter extends AbstractRequestFilter
{
    /**
     * Cache the expensive stuff, we are a per thread instance. Can be empty!
     */
    private final LRUClockMap<CharSequence, MatchResult> cache;

    /**
     * Just a place holder for a NULL
     */
    private static final MatchResult NULL = Pattern.compile(".*").matcher("null").toMatchResult();

    /**
     * The matcher we use when we don't want to cache anything
     */
    private final Matcher matcher;

    /**
     * Whether or not this is an exclusion rule.
     */
    final boolean isExclude;

    /**
     * The last state of the evaluation, so we don't have look anything up. All filters are already
     * stateful, so we can do that. 
     */
    protected MatchResult lastFilterState;

    /**
     * Constructor.
     *
     * @param typeCode
     *            the type code of this request filter
     * @param regex
     *            the regular expression to identify matching requests
     * @param exclude
     *            whether or not this is an exclusion rule
     */
    public AbstractPatternRequestFilter(final String typeCode, final String regex, final boolean exclude, final int cacheSize)
    {
        super(typeCode);

        this.matcher = StringUtils.isBlank(regex) ? null : RegExUtils.getPattern(regex, 0).matcher("any");
        this.isExclude = exclude;
        if (cacheSize <= 0)
        {
            throw new IllegalArgumentException("Cache size larger than 0");
        }
        this.cache = new LRUClockMap<>(cacheSize);
    }

    /**
     * Returns the text to examine from the passed request data object.d
     *
     * @return the text
     */
    protected abstract CharSequence getText(final RequestData requestData);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean appliesTo(final RequestData requestData)
    {
        if (this.matcher == null)
        {
            // empty is always fine, we just want to get the full text
            return true;
        }

        // get the data to match against
        final CharSequence text = getText(requestData);

        if (text == null)
        {
            // empty is always fine
            return true;
        }
        
        MatchResult result = this.cache.get(text);
        if (result == null)
        {
            // not found, produce and cache, recycle the matcher
            // cache only the result, not the matcher itself
            final Matcher m = this.matcher.reset(text);

            if (m.find() ^ isExclude)
            {
                // we don't cache the matcher but the result which is immutable
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getReplacementText(final RequestData requestData, final int capturingGroupIndex)
    {
        if (isExclude || matcher == null || capturingGroupIndex == -1)
        {
            return getText(requestData);
        }

        try
        {
            return this.lastFilterState.group(capturingGroupIndex);
        }
        catch (final IndexOutOfBoundsException ioobe)
        {
            final String format = "No matching group %d for input string '%s' and pattern '%s'";
            ThrowableUtils.setMessage(ioobe, String.format(format, capturingGroupIndex, getText(requestData), getPattern()));

            throw ioobe;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{ type: '");
        sb.append(getTypeCode()).append("', ");
        sb.append("pattern: '").append(getPattern()).append("', ");
        sb.append("isExclude: ").append(isExclude).append(" }");

        return sb.toString();
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
    public boolean isEmpty()
    {
        return matcher == null;
    }

    /**
     * Whether this filter is an exclude filter.
     */
    public boolean isExclude()
    {
        return isExclude;
    }
}
