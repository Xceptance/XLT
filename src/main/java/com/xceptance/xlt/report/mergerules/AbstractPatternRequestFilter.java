/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * Base class for all request filters that use regular expressions to identify matching requests.
 */
public abstract class AbstractPatternRequestFilter extends AbstractRequestFilter
{
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
        this(typeCode, regex, false);
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
    public AbstractPatternRequestFilter(final String typeCode, final String regex, final boolean exclude)
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
        isExclude = exclude;
    }

    /**
     * Returns the text to examine from the passed request data object.
     *
     * @return the text
     */
    protected abstract String getText(RequestData requestData);

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

        final Matcher matcher = pattern.matcher(getText(requestData));

        return (matcher.find() ^ isExclude) ? matcher : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReplacementText(final RequestData requestData, final int capturingGroupIndex, final Object filterState)
    {
        if (isExclude || pattern == null || capturingGroupIndex == -1)
        {
            return getText(requestData);
        }

        try
        {
            return ((Matcher) filterState).group(capturingGroupIndex);
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
