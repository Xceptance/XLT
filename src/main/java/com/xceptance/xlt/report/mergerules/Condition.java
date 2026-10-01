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

import java.nio.charset.StandardCharsets;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.collection.LRUClockMap;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.ByteSlice;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * Base class for all request filters. Even if they don't use any regex, they will inherit from this class.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * @author Jörg Weber (Xceptance Software Technologies GmbH)
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public abstract class Condition
{
    /**
     * Just a place holder for a NULL in the cache because null is ambiguous.
     */
    private static final MatchResult NULL = Pattern.compile(".*").matcher("null").toMatchResult();

    /**
     * Cache the expensive stuff, we are a per thread instance.
     */
    private final LRUClockMap<Object, MatchResult> cache;

    /**
     * The matcher we use when we don't want to cache anything
     */
    private final Matcher matcher;

    /**
     * The last state of the evaluation, so we don't have look anything up. All filters are already
     * stateful, so we can do that. 
     */
    private MatchResult lastFilterState;

    /**
     * Reusable query slice for zero-allocation cache lookups when byte data is available.
     */
    protected final ByteSlice querySlice = new ByteSlice();

    /**
     * Constructor.
     *
     * @param typeCode
     *            the type code of this request filter
     * @param regex
     *            the regular expression to identify matching requests
     * @param exclude
     *            whether or not this is an exclusion rule
     * @param cacheSize
     *           the size of the cache for match results, must be larger than 0, it might happen
     *           that we configure one but don't use it at all, such as for runtimes
     */
    public Condition(final String regex, final int cacheSize)
    {
        this.matcher = StringUtils.isBlank(regex) ? null : RegExUtils.getPattern(regex, 0).matcher("any");
        
        if (cacheSize <= 0)
        {
            throw new IllegalArgumentException("Cache size must be larger than 0");
        }
        this.cache = new LRUClockMap<>(cacheSize);
    }
    
    /**
     * Returns the last filterstate. This saves time because we are not holding
     * the state externally anymore but it also means that a condition is not
     * thread-safe and has to be used carefully. Speed rulez!
     * 
     * @return the lastFilterState or null of it was a miss
     */
    public MatchResult getLastFilterState()
    {
        return lastFilterState;
    }

    /**
     * Returns whether the passed request data contains raw unmaterialized byte data for this condition.
     */
    protected boolean hasByteData(final RequestData requestData)
    {
        return false;
    }

    /**
     * Returns the raw byte buffer for this condition if present.
     */
    protected byte[] getByteBuffer(final RequestData requestData)
    {
        return null;
    }

    /**
     * Returns the raw byte buffer offset for this condition.
     */
    protected int getByteOffset(final RequestData requestData)
    {
        return 0;
    }

    /**
     * Returns the raw byte buffer length for this condition.
     */
    protected int getByteLength(final RequestData requestData)
    {
        return 0;
    }

    /**
     * Returns the text to examine from the passed request data object
     *
     * @return the text to check against
     */
    protected abstract CharSequence getText(final RequestData requestData);
    
    /**
     * {@inheritDoc}
     */
    protected boolean apply(final RequestData requestData)
    {
        /**
         * Because we now have a special Condition that is taking care of empty patterns,
         * we can assume that if we are here, we have a pattern to match against.
         * One less if!
         */
        
        // Fast path: Check byte cache first if raw bytes are available
        if (hasByteData(requestData))
        {
            final byte[] buf = getByteBuffer(requestData);
            final int off = getByteOffset(requestData);
            final int len = getByteLength(requestData);

            this.querySlice.set(buf, off, len);
            MatchResult result = this.cache.get(this.querySlice);
            if (result != null)
            {
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

            // Cache miss: decode to UTF-8 String only when we need to evaluate the regex
            final String text = new String(buf, off, len, StandardCharsets.UTF_8);
            final Matcher m = this.matcher.reset(text);
            if (m.find())
            {
                result = m.toMatchResult();
                // Store detached copy of bytes in cache to release underlying line buffer
                this.cache.put(ByteSlice.copyOf(buf, off, len), result);
                this.lastFilterState = result;
                return true;
            }
            else
            {
                // Remember the miss
                this.cache.put(ByteSlice.copyOf(buf, off, len), NULL);
                this.lastFilterState = null;
                return false;
            }
        }

        // get the data to match against (String / CharSequence fallback)
        final CharSequence text = getText(requestData);

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
     * Returns the right text for the the replacement of a certain group or 
     * if no group given, we return the entire text
     */
    protected CharSequence getReplacementText(final RequestData requestData, final int capturingGroupIndex)
    {
        /**
         * If no capturing group is given, we return the entire text. Empty matchers no longer possible.
         */
        try
        {
            return capturingGroupIndex == -1 ? getText(requestData) : this.getLastFilterState().group(capturingGroupIndex);
        }
        catch (final IndexOutOfBoundsException ioobe)
        {
            final String format = "No matching group %d for input string '%s' and pattern '%s'";
            ThrowableUtils.setMessage(ioobe, String.format(format, capturingGroupIndex, getText(requestData), getPattern()));

            throw ioobe;
        }
    }

    /**
     * Returns the filter pattern string.
     */
    protected String getPattern()
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
     * Returns the type code of this request filter.
     *
     * @return the type code
     */
    protected abstract String getTypeCode();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{ type: '");
        sb.append(getTypeCode()).append("', ");
        sb.append("pattern: '").append(getPattern()).append("'}");

        return sb.toString();
    }
}
