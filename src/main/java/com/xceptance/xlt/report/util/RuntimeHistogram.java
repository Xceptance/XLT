package com.xceptance.xlt.report.util;

/**
 * The {@link RuntimeHistogram} class calculates any percentile from the <code>int</code> values added. In contrast to
 * other implementations, this class does not store any value added, but counts the occurrences of each value. This
 * approach saves memory if the values added are in roughly the same range.
 */
public class RuntimeHistogram
{
    /**
     * The default precision.
     */
    private static final int DEFAULT_PRECISION = 1;

    /**
     * The buckets allocated so far.
     */
    private int[] countPerBucket;

    /**
     *
     */
    private int firstIndex;

    /**
     *
     */
    private int lastIndex;

    /**
     *
     */
    private final int precision;

    /**
     * The number of values added so far to this median calculator.
     */
    private int valueCount;

    /**
     * Constructor.
     */
    public RuntimeHistogram()
    {
        this(DEFAULT_PRECISION);
    }

    /**
     * Constructor.
     *
     * @param precision
     *            the precision to use
     */
    public RuntimeHistogram(final int precision)
    {
        this.precision = precision;
    }

    /**
     * Adds a value to this median calculator.
     *
     * @param value
     *            the value to add
     */
    public void addValue(final int value)
    {
        final int index = value / precision;

        if (valueCount == 0)
        {
            countPerBucket = new int[1];

            countPerBucket[0] = 1;

            firstIndex = lastIndex = index;
            valueCount = 1;
        }
        else
        {
            // grow/shift values array if necessary
            if (index < firstIndex)
            {
                final int delta = firstIndex - index;

                grow(delta, true);

                firstIndex = index;
            }
            else if (index > lastIndex)
            {
                final int delta = index - lastIndex;

                grow(delta, false);

                lastIndex = index;
            }

            countPerBucket[index - firstIndex]++;
            valueCount++;
        }
    }

    /**
     * Returns the median of the values added.
     *
     * @return the median value
     */
    public double getMedianValue()
    {
        return getPercentile(50.0);
    }

    /**
     * Returns the p-th percentile of the values added.
     *
     * @param p
     *            the p (0 &lt; p &le; 100)
     * @return the p-th percentile
     */
    public double getPercentile(final double p)
    {
        if (p <= 0.0 || p > 100.0)
        {
            throw new IllegalArgumentException("Value of parameter 'p' must be in range (0, 100], but was " + p);
        }

        // see https://de.wikipedia.org/wiki/Quantil#Berechnung_empirischer_Quantile

        double value;

        if (valueCount == 0)
        {
            value = 0.0;
        }
        else if (p == 100.0)
        {
            value = lastIndex * precision;
        }
        else
        {
            final double np = valueCount * p / 100.0;

            if (np % 1.0 == 0.0)
            {
                // n*p is integral -> prepare two adjacent indexes
                final int i1 = (int) np;
                final int i2 = i1 + 1;

                // get two adjacent values and calculate the mean of both
                final int value1 = getValue(i1);
                final int value2 = getValue(i2);

                value = (value1 + value2) / 2.0;
            }
            else
            {
                // n*p is fractional -> "ceil" the index
                final int i = (int) Math.ceil(np);

                // just get the corresponding value
                value = getValue(i);
            }
        }

        return value;
    }

    /**
     * Returns the value that corresponds to the given 1-based index.
     *
     * @param valueIndex
     * @return the value
     */
    private int getValue(final int valueIndex)
    {
        // find the bucket that holds the value with the given index
        int bucketIndex = -1;
        int count = 0;

        while (count < valueIndex)
        {
            count += countPerBucket[++bucketIndex];
        }

        // reconstruct the value
        return (firstIndex + bucketIndex) * precision;
    }

    /**
     * Returns the number of allocated buckets.
     *
     * @return the number of buckets used
     */
    public int getNumberOfBuckets()
    {
        return countPerBucket.length;
    }

    /**
     * Grows the bucket array by the specified number of buckets.
     *
     * @param delta
     *            the number of buckets to add
     */
    private void grow(final int delta, final boolean shiftToRight)
    {
        final int[] newCountPerBucket = new int[countPerBucket.length + delta];

        System.arraycopy(countPerBucket, 0, newCountPerBucket, shiftToRight ? delta : 0, countPerBucket.length);

        countPerBucket = newCountPerBucket;
    }
}
