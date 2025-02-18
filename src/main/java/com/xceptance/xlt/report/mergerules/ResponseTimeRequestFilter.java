/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
