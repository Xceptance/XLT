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
