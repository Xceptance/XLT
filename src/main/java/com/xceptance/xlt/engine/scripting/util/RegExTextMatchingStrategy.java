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
package com.xceptance.xlt.engine.scripting.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegExTextMatchingStrategy extends TextMatchingStrategy
{
    private final boolean caseInsensitive;

    RegExTextMatchingStrategy(final boolean caseInsensitiveMatching)
    {
        caseInsensitive = caseInsensitiveMatching;
    }

    RegExTextMatchingStrategy()
    {
        this(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAMatch(final String searchPattern, final String text, final boolean strict)
    {
        int flags = Pattern.MULTILINE;
        if (caseInsensitive)
        {
            flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        }

        final Pattern pattern = Pattern.compile(searchPattern, flags);
        final Matcher matcher = pattern.matcher(text);

        if (strict)
        {
            return matcher.matches();
        }
        else
        {
            return matcher.find();
        }
    }
}
