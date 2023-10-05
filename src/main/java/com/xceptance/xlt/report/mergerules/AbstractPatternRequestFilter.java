/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import com.xceptance.common.collection.LRUFastHashMap;
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
    private final LRUFastHashMap<CharSequence, Matcher> cache;

    /**
     * Just a place holder for a NULL
     */
    public static final Matcher NULL = Pattern.compile(".*").matcher("null");

    /**
     * The pattern this filter uses.
     */
    private final Pattern pattern;

    /**
     * Whether or not this is an exclusion rule.
     */
    private final boolean isExclude;

    /**
     * Constructor.
     *
     * @param typeCode
     *            the type code of this request filter
     * @param regex
     *            the regular expression to identify matching requests
     */
    public AbstractPatternRequestFilter(final String typeCode, final String regex)
    {
        // default cache
        this(typeCode, regex, false, 100);
    }

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

        if (StringUtils.isBlank(regex))
        {
            pattern = null;
        }
        else
        {
            pattern = RegExUtils.getPattern(regex, 0);
        }
        this.isExclude = exclude;
        this.cache = cacheSize > 0 ? new LRUFastHashMap<>(cacheSize) : null;
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
    public Object appliesTo(final RequestData requestData)
    {
        if (pattern == null)
        {
            // empty is always fine, we just want to get the full text -> return a non-null dummy object
            return Boolean.TRUE;
        }

        // get the data to match against
        final CharSequence text = getText(requestData);

        // only cache if we want that, there are areas where caching does not make sense and wastes
        // a lot of time, such as urls
        if (cache == null)
        {
            final Matcher matcher = pattern.matcher(text);

            return (matcher.find() ^ isExclude) ? matcher : null;
        }
        else
        {
            Matcher result = this.cache.get(text);
            if (result == null)
            {
                // not found, produce and cache
                final Matcher matcher = pattern.matcher(text);

                result = (matcher.find() ^ isExclude) ? matcher : NULL;
                cache.put(text, result);
            }

            // ok, we got one, just see if this is NULL or a match
            if (result == NULL)
            {
                return null;
            }
            else
            {
                // we need to make the result immutable so that we don't change the
                // matcher and keeps its current state otherwise it wouldn't be reusable
                // and because we already ran the expensive find on it... we wan't to
                // keep that piece cached as well
                return result.toMatchResult();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getReplacementText(final RequestData requestData, final int capturingGroupIndex, final Object filterState)
    {
        if (isExclude || pattern == null || capturingGroupIndex == -1)
        {
            return getText(requestData);
        }

        try
        {
            return ((MatchResult) filterState).group(capturingGroupIndex);
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
        return (pattern == null) ? StringUtils.EMPTY : pattern.pattern();
    }

    /**
     * Whether this filter has an empty pattern.
     */
    public boolean isEmpty()
    {
        return pattern == null;
    }

    /**
     * Whether this filter is an exclude filter.
     */
    public boolean isExclude()
    {
        return isExclude;
    }
}
