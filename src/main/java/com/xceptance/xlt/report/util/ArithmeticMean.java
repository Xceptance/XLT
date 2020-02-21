package com.xceptance.xlt.report.util;

/**
 * Computes the arithmetic mean for a stream of data values added using the {@link #addValue(double)} method. The data
 * values are not stored in memory, so this class can be used to compute statistics for very large data streams.
 * <p>
 * Note: This class is not thread-safe.
 */
public class ArithmeticMean
{
    /**
     * The number of values added.
     */
    private int count;

    /**
     * The sum of all values.
     */
    private double sum;

    /**
     * Adds a value.
     * 
     * @param value
     *            the value to add
     */
    public void addValue(final double value)
    {
        count++;
        sum += value;
    }

    /**
     * Returns the number of values added.
     * 
     * @return the number of values
     */
    public int getCount()
    {
        return count;
    }

    /**
     * Returns the mean of the values added.
     * 
     * @return the mean
     */
    public double getMean()
    {
        return sum / count;
    }
}
