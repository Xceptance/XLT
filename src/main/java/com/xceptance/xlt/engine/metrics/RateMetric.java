/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
 * A metric for rate values. All reported counts are added to the total count and converted to the corresponding rate
 * value.
 * <p>
 * Note: This class is thread-safe.
 */
public class RateMetric implements Metric
{
    /**
     * The total count (the sum of all values added).
     */
    private long sum;

    /**
     * The time the metric was reported the last time.
     */
    private long time;

    /**
     * The interval [ms] the rate is based on (e.g. 1000 for a per-sec rate).
     */
    private final long rateInterval;

    /**
     * The reporting interval [ms]. Used only once, for the very first rate calculation.
     */
    private final long reportingInterval;

    /**
     * Whether this metric has unreported data.
     */
    private boolean hasData;

    /**
     * Constructor.
     *
     * @param rateInterval
     *            the rate interval [ms]
     * @param reportingInterval
     *            the reporting interval [ms]
     */
    public RateMetric(final long rateInterval, final long reportingInterval)
    {
        this.rateInterval = rateInterval;
        this.reportingInterval = reportingInterval;
    }

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
     * Returns the rate based on the total count, the rate interval, and the time passed. Resets the internal state.
     *
     * @return the rate, or <code>null</code> if no values have been added to this metric since last call
     */
    public synchronized Double getRateAndClear()
    {
        final Double rate;

        if (hasData)
        {
            // calculate rate
            final long now = System.nanoTime() / 1000000;
            final double duration = (time == 0) ? reportingInterval : now - time;
            rate = (double) sum * rateInterval / duration;

            // reset internal state
            sum = 0;
            time = now;
            hasData = false;
        }
        else
        {
            rate = null;
        }

        return rate;
    }
}
