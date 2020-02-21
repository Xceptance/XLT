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
        super("u", regex, exclude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getText(final RequestData requestData)
    {
        return requestData.getUrl();
    }
}
