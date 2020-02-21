package com.xceptance.xlt.report.mergerules;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Filters requests based on their response time.
 */
public class ResponseTimeRequestFilter extends AbstractRequestFilter
{
    /**
     * The response time boundaries.
     */
    private final long[] responseTimeBoundaries;

    /**
     * The response time range string, for example "1001..2000".
     */
    private final String[] responseTimeRanges;

    /**
     * Constructor.
     * 
     * @param responseTimes
     *            the response time range definition string
     */
    public ResponseTimeRequestFilter(final String responseTimes)
    {
        super("r");

        // pre-calculate the replacement strings
        final String[] ranges = StringUtils.split(responseTimes, " ;,");

        responseTimeBoundaries = new long[ranges.length];
        for (int i = 0; i < responseTimeBoundaries.length; i++)
        {
            responseTimeBoundaries[i] = Integer.parseInt(ranges[i]);
        }

        responseTimeRanges = new String[responseTimeBoundaries.length + 1];
        long previousBoundary = 0;
        for (int i = 0; i < responseTimeBoundaries.length; i++)
        {
            final long nextBoundary = responseTimeBoundaries[i];
            responseTimeRanges[i] = previousBoundary + ".." + (nextBoundary - 1);
            previousBoundary = nextBoundary;
        }

        responseTimeRanges[responseTimeRanges.length - 1] = ">=" + previousBoundary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object appliesTo(final RequestData requestData)
    {
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReplacementText(final RequestData requestData, final int capturingGroupIndex, final Object filterState)
    {
        final long responseTime = requestData.getRunTime();

        int i;
        for (i = 0; i < responseTimeBoundaries.length; i++)
        {
            if (responseTime < responseTimeBoundaries[i])
            {
                return responseTimeRanges[i];
            }
        }

        return responseTimeRanges[i];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{ type: '");
        sb.append(getTypeCode()).append("', ");
        sb.append("responseTimeRanges: [");
        for (int i = 0; i < responseTimeRanges.length; i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append("'").append(responseTimeRanges[i]).append("'");
        }
        sb.append("]}");

        return sb.toString();
    }
}
