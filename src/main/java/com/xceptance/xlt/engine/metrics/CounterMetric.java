/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.metrics;

/**
 * A metric for count values. All reported counts are added to the total count.
 * <p>
 * Note: This class is thread-safe.
 */
public class CounterMetric implements Metric
{
    /**
     * The total count (the sum of all values added).
     */
    private long sum;

    /**
     * Whether this metric has unreported data.
     */
    private boolean hasData;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void update(final int value)
    {
        sum += value;
        hasData = true;
    }

    /**
     * Returns the total count and resets the internal counter.
     *
     * @return the total count, or <code>null</code> if no values have been added to this metric since last call
     */
    public synchronized Long getCountAndClear()
    {
        final Long count;

        if (hasData)
        {
            count = sum;

            // reset internal state
            sum = 0;
            hasData = false;
        }
        else
        {
            count = null;
        }

        return count;
    }
}
