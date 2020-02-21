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
