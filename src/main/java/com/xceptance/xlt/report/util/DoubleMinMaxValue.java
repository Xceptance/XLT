/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.util;

/**
 * A {@link DoubleMinMaxValue} stores the minimum/maximum/sum/count of all the sample values added, but can also
 * reproduce a rough approximation of the distinct values added.
 * 
 * @see IntMinMaxValue
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DoubleMinMaxValue
{
    private double accumulatedValue;

    private double maximum;

    private double minimum;

    private int valueCount;

    /**
     * Holds an approximation of the distinct values added to this min-max value.
     */
    private final DoubleLowPrecisionValueSet valueSet = new DoubleLowPrecisionValueSet();

    /**
     * Constructor.
     * 
     * @param value
     *            the first value to add
     */
    public DoubleMinMaxValue(final double value)
    {
        accumulatedValue = value;
        maximum = value;
        minimum = value;
        valueCount = 1;

        valueSet.addValue(value);
    }

    /**
     * @return the accumulatedValue
     */
    public double getAccumulatedValue()
    {
        return accumulatedValue;
    }

    /**
     * @return the average value
     */
    public double getAverageValue()
    {
        return accumulatedValue / valueCount;
    }

    /**
     * @return the maximum
     */
    public double getMaximumValue()
    {
        return maximum;
    }

    /**
     * @return the minimum
     */
    public double getMinimumValue()
    {
        return minimum;
    }

    /**
     * @return the average value
     */
    public double getValue()
    {
        return getAverageValue();
    }

    /**
     * Returns an approximation of the distinct values added to this min-max value.
     * 
     * @return the values
     */
    public double[] getValues()
    {
        return valueSet.getValues();
    }

    /**
     * @return the valueCount
     */
    public int getValueCount()
    {
        return valueCount;
    }

    /**
     * @param item
     */
    DoubleMinMaxValue merge(final DoubleMinMaxValue item)
    {
        if (item != null)
        {
            maximum = Math.max(maximum, item.maximum);
            minimum = Math.min(minimum, item.minimum);
            accumulatedValue += item.accumulatedValue;
            valueCount += item.valueCount;

            valueSet.merge(item.valueSet);
        }

        return this;
    }

    /**
     */
    @Override
    public String toString()
    {
        return "" + getValue() + "/" + getAccumulatedValue() + "/" + getMinimumValue() + "/" + getMaximumValue() + "/" + getValueCount();
    }

    /**
     * @param sample
     */
    public void updateValue(final double sample)
    {
        if (sample > maximum)
        {
            maximum = sample;
        }
        else if (sample < minimum)
        {
            minimum = sample;
        }

        accumulatedValue += sample;
        valueCount++;

        valueSet.addValue(sample);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(accumulatedValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maximum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minimum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + valueCount;
        result = prime * result + ((valueSet == null) ? 0 : valueSet.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
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
        DoubleMinMaxValue other = (DoubleMinMaxValue) obj;
        if (Double.doubleToLongBits(accumulatedValue) != Double.doubleToLongBits(other.accumulatedValue))
        {
            return false;
        }
        if (Double.doubleToLongBits(maximum) != Double.doubleToLongBits(other.maximum))
        {
            return false;
        }
        if (Double.doubleToLongBits(minimum) != Double.doubleToLongBits(other.minimum))
        {
            return false;
        }
        if (valueCount != other.valueCount)
        {
            return false;
        }
        if (valueSet == null)
        {
            if (other.valueSet != null)
            {
                return false;
            }
        }
        else if (!valueSet.equals(other.valueSet))
        {
            return false;
        }
        return true;
    }
}
