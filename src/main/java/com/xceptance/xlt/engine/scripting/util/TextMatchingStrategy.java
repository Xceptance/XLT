package com.xceptance.xlt.engine.scripting.util;

/**
 * Text matching strategy which supports the additional parameter <tt>strict</tt>.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
abstract class TextMatchingStrategy
{
    /**
     */
    public boolean isAMatch(final String searchPattern, final String text)
    {
        return isAMatch(searchPattern, text, false);
    }

    /**
     * @param compareThis
     * @param with
     * @param strict
     * @return
     */
    public abstract boolean isAMatch(final String searchPattern, final String text, boolean strict);
}
