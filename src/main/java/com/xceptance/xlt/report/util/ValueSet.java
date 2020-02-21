package com.xceptance.xlt.report.util;

import java.util.Arrays;

/**
 * A {@link ValueSet} maintains the sum of all values generated at a certain second. If the time range exceeds the size
 * of the value set, the set grows automatically to make room for the new values.
 * <p>
 * Potential bug: no value vs. a value of 0
 */
public class ValueSet
{
    /**
     * The default initial value set size.
     */
    public static final int DEFAULT_SIZE = 1024;

    /**
     * The smallest time [s] for which a value exists.
     */
    private long firstSecond;

    /**
     * The biggest time [s] for which a value exists.
     */
    private long lastSecond;

    /**
     * The maximum time [ms] for which a value has been added to this set.
     */
    private long maximumTime;

    /**
     * The minimum time [ms] for which a value has been added to this set.
     */
    private long minimumTime;

    /**
     * The number of different values that can be stored in this value set.
     */
    private int size;

    /**
     * The total number of values added to this value set.
     */
    private long valueCount;

    /**
     * The values maintained by this value set.
     */
    private int[] values;

    /**
     * Creates a {@link ValueSet} instance with a size of {@link #DEFAULT_SIZE}.
     */
    public ValueSet()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a {@link ValueSet} instance with the specified size.
     * 
     * @param size
     *            the size
     */
    private ValueSet(final int size)
    {
        this.size = size;
        values = new int[size];
    }

    /**
     * Adds a value for a certain time-stamp to this value set or updates the value for an already existing time-stamp.
     * The time-stamp is converted to a second (not ms)
     * 
     * @param time
     *            the time-stamp in ms
     * @param value
     *            the value
     */
    public void addOrUpdateValue(final long time, final int value)
    {
        // get the corresponding second
        final long second = time / 1000;

        // check whether this is the first value added
        if (valueCount == 0)
        {
            // yes, that's easy
            firstSecond = lastSecond = second;
            values[0] = value;

            // maintain statistics
            minimumTime = maximumTime = time;
            valueCount = 1;
        }
        else
        {
            // no, there are values in the set already

            // check whether we might have to grow the value set first
            if (second != firstSecond)
            {
                // decide on the way of shrinking
                if (second > firstSecond)
                {
                    // repeat as long as the second falls after the current size
                    while (second - firstSecond >= size)
                    {
                        grow();
                    }

                    // maintain upper boundary
                    if (second > lastSecond)
                    {
                        lastSecond = second;
                    }
                }
                else
                {
                    // repeat as long as the second still falls outside (before) the current size
                    while (lastSecond - second >= size)
                    {
                        grow();
                    }

                    // shift
                    final int indexDiff = (int) (firstSecond - second);

                    shift(indexDiff);

                    // maintain lower boundary
                    firstSecond = second;
                }
            }

            // calculate final index and update value
            final int index = (int) (second - firstSecond);
            values[index] += value;

            // maintain statistics
            valueCount++;

            if (time < minimumTime)
            {
                minimumTime = time;
            }

            if (time > maximumTime)
            {
                maximumTime = time;
            }
        }
    }

    /**
     * Returns the smallest second [s] for which a value exists in this set.
     * 
     * @return the last second (in s)
     */
    public long getFirstSecond()
    {
        checkValueCount("No first second available because no values have been added so far.");
        return firstSecond;
    }

    /**
     * Returns the biggest second [s] for which a value exists in this set.
     * 
     * @return the last second (in s)
     */
    public long getLastSecond()
    {
        checkValueCount("No last second available because no values have been added so far.");
        return lastSecond;
    }

    /**
     * Returns the amount of seconds in this value set or in other words the length of this value set in seconds
     * 
     * @return amount of seconds (in s)
     */
    public long getLengthInSeconds()
    {
        checkValueCount("No length available because no values have been added so far.");
        return lastSecond - firstSecond + 1; // add 1 so that the length won't be 0
        // this makes sense because there can be more than one value in one second
    }

    /**
     * Returns the maximum time [ms] for which a value has been added to this set.
     * 
     * @return the maximum time (in ms)
     */
    public long getMaximumTime()
    {
        checkValueCount("No maximum time available as no values have been added so far.");
        return maximumTime;
    }

    /**
     * Returns the minimum time [ms] for which a value has been added to this set.
     * 
     * @return the minimum time (in ms)
     */
    public long getMinimumTime()
    {
        checkValueCount("No minimum time available as no values have been added so far.");
        return minimumTime;
    }

    /**
     * Checks that the value count is greater than 0.
     * 
     * @throws IllegalArgumentException
     *             if the value count is equal to 0
     */
    private void checkValueCount(final String message)
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Returns the total number of values added to this value set.
     * 
     * @return the number of values
     */
    public long getValueCount()
    {
        return valueCount;
    }

    /**
     * Returns the values maintained by this set.
     * 
     * @return the values
     */
    public int[] getValues()
    {
        final int[] copy;

        if (valueCount == 0)
        {
            copy = new int[0];
        }
        else
        {
            final int length = (int) (lastSecond - firstSecond + 1);
            copy = new int[length];

            System.arraycopy(values, 0, copy, 0, length);
        }

        return copy;
    }

    /**
     * Creates a {@link MinMaxValueSet} of the given size and adds all stored values to this set.
     * 
     * @param size
     *            the min/max value set size
     * @return the populated min/max value set
     */
    public MinMaxValueSet toMinMaxValueSet(final int size)
    {
        final MinMaxValueSet minMaxValueSet = new MinMaxValueSet(size);

        if (valueCount > 0)
        {
            final int actualSize = (int) (lastSecond - firstSecond + 1);
            long time = firstSecond * 1000L;

            for (int i = 0; i < actualSize; i++, time = time + 1000)
            {
                minMaxValueSet.addOrUpdateValue(time, values[i]);
            }
        }

        return minMaxValueSet;
    }

    /**
     * Shifts the content of the value array to the right for the specified number of elements.
     * 
     * @param indexDiff
     *            the destination index
     */
    private void shift(final int indexDiff)
    {
        System.arraycopy(values, 0, values, indexDiff, size - indexDiff);

        for (int i = 0; i < indexDiff; i++)
        {
            values[i] = 0;
        }
    }

    /**
     * Doubles the size of the value array.
     */
    private void grow()
    {
        final int[] newValues = new int[2 * size];

        System.arraycopy(values, 0, newValues, 0, size);
        size = size * 2;

        values = newValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ValueSet other = (ValueSet) obj;
        if (firstSecond != other.firstSecond)
        {
            return false;
        }
        if (lastSecond != other.lastSecond)
        {
            return false;
        }
        if (maximumTime != other.maximumTime)
        {
            return false;
        }
        if (minimumTime != other.minimumTime)
        {
            return false;
        }
        if (size != other.size)
        {
            return false;
        }
        if (valueCount != other.valueCount)
        {
            return false;
        }
        if (!Arrays.equals(values, other.values))
        {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (firstSecond ^ (firstSecond >>> 32));
        result = prime * result + (int) (lastSecond ^ (lastSecond >>> 32));
        result = prime * result + (int) (maximumTime ^ (maximumTime >>> 32));
        result = prime * result + (int) (minimumTime ^ (minimumTime >>> 32));
        result = prime * result + size;
        result = prime * result + (int) (valueCount ^ (valueCount >>> 32));
        result = prime * result + Arrays.hashCode(values);
        return result;
    }
}
