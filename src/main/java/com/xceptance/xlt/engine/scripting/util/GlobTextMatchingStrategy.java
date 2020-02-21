package com.xceptance.xlt.engine.scripting.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quick workaround for the original Selenium GlobTextMatchingStrategy, which does not work correctly if the search
 * pattern by chance contains regex-like artifacts other than "*" and "?".
 */
class GlobTextMatchingStrategy extends TextMatchingStrategy
{
    @Override
    public boolean isAMatch(final String searchPattern, final String text, final boolean strict)
    {
        // first quote the search string
        String regex = Pattern.quote(searchPattern);

        // now selectively "un-quote" the wild-card characters "*" and "?"
        regex = regex.replace("*", "\\E.*\\Q").replace("?", "\\E.\\Q");

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
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
