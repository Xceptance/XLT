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

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Filters requests based on their status code.
 */
public class StatusCodeRequestFilter extends AbstractPatternRequestFilter
{
    /**
     * Constructor.
     * 
     * @param regex
     *            the regular expression to identify matching requests
     */
    public StatusCodeRequestFilter(final String regex)
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
    public StatusCodeRequestFilter(final String regex, final boolean exclude)
    {
        super("s", regex, exclude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getText(final RequestData requestData)
    {
        return Integer.toString(requestData.getResponseCode());
    }
}
