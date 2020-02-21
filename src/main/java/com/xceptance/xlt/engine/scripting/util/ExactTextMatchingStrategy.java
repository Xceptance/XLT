package com.xceptance.xlt.engine.scripting.util;

class ExactTextMatchingStrategy extends TextMatchingStrategy
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAMatch(final String searchPattern, final String text, final boolean strict)
    {
        if (strict)
        {
            return text.equals(searchPattern);
        }
        else
        {
            return text.contains(searchPattern);
        }
    }
}
