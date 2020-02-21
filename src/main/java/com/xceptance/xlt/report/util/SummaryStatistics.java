package com.xceptance.xlt.report.util;

/**
 * Computes summary statistics for a stream of data values added using the {@link #addValue(int)} method. The data
 * values are not stored in memory, so this class can be used to compute statistics for very large data streams. This
 * class is similar to {@link org.apache.commons.math.stat.descriptive.SummaryStatistics}, but optimized for
 * <code>int</code> values (about 10 times faster).
 * <p>
 * Note: This class is not thread-safe.
 */

public class SummaryStatistics
{
    /**
     * The number of values added.
     */
    private long count;

    /**
     * The maximum value.
     */
    private int maximum = Integer.MIN_VALUE;

    /**
     * The minimum value.
     */
    private int minimum = Integer.MAX_VALUE;

    /**
     * The sum of all values.
     */
    private double sum;

    /**
     * The sum of the square of all values.
     */
    private double sumOfSquares;

    /**
     * Adds a value.
     * 
     * @param value
     *            the value to add
     */
    public void addValue(final int value)
    {
        if (value > maximum)
        {
            maximum = value;
        }

        if (value < minimum)
        {
            minimum = value;
        }

        count++;

        sum += value;
        sumOfSquares += (double) value * (double) value;
    }

    /**
     * Returns the number of values added.
     * 
     * @return the number of values
     */
    public long getCount()
    {
        return count;
    }

    /**
     * Returns the maximum of the values added.
     * 
     * @return the maximum
     */
    public int getMaximum()
    {
        return count == 0 ? 0 : maximum;
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

    /**
     * Returns the minimum of the values added.
     * 
     * @return the minimum
     */
    public int getMinimum()
    {
        return count == 0 ? 0 : minimum;
    }

    /**
     * Returns the standard deviation of the values added.
     * 
     * @return the standard deviation
     */
    public double getStandardDeviation()
    {
        final double mean = getMean();

        return Math.sqrt(sumOfSquares / count - mean * mean);
    }

    /**
     * Returns the sum of all values added.
     * 
     * @return the sum
     */
    public double getSum()
    {
        return sum;
    }
}
