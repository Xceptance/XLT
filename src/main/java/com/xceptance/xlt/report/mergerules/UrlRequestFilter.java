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

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Filters requests based on their URLs.
 */
public class UrlRequestFilter extends AbstractPatternRequestFilter
{
    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     */
    public UrlRequestFilter(final String regex)
    {
        this(regex, false);
    }

    /**
     * Constructor.
     *
     * @param regex
     *            the regular expression to identify matching requests
     * @param exclude
     *            whether or not this is an exclusion rule
     */
    public UrlRequestFilter(final String regex, final boolean exclude)
    {
        // we don't want to cache here due to the large variance in
        // urls, it is too costly to look things up with a lot of cache misses
        super("u", regex, exclude, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CharSequence getText(final RequestData requestData)
    {
        return requestData.getUrl();
    }
}
