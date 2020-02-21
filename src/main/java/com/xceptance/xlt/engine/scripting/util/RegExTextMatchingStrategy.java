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
