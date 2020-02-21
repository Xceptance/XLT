package com.xceptance.xlt.engine.metrics;

/**
 * Common interface for all metric classes.
 */
public interface Metric
{
    /**
     * Updates the metric with the given value.
     *
     * @param value
     *            the value
     */
    public void update(int value);
}
