package com.xceptance.xlt.report.mergerules;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Filters requests based on their name.
 */
public class RequestNameRequestFilter extends AbstractPatternRequestFilter
{
    /**
     * Constructor.
     * 
     * @param regex
     *            the regular expression to identify matching requests
     */
    public RequestNameRequestFilter(final String regex)
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
    public RequestNameRequestFilter(final String regex, final boolean exclude)
    {
        super("n", regex, exclude);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getText(final RequestData requestData)
    {
        return requestData.getName();
    }
}
