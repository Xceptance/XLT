package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link HistogramValueSet} class.
 */
public class HistogramValueSetTest
{
    @Test
    public void testBasics()
    {
        final double minValue = 0.0;
        final double maxValue = 100.0;
        final int numberOfBins = 100;

        final HistogramValueSet valueSet = new HistogramValueSet(minValue, maxValue, numberOfBins);

        valueSet.addValue(-1);
        valueSet.addValue(0);
        valueSet.addValue(1);
        valueSet.addValue(99);
        valueSet.addValue(100);
        valueSet.addValue(101);

        // check
        Assert.assertEquals(minValue, valueSet.getMinValue(), 0.0);
        Assert.assertEquals(maxValue, valueSet.getMaxValue(), 0.0);
        Assert.assertEquals(numberOfBins, valueSet.getNumberOfBins());

        final int[] countPerBin = valueSet.getCountPerBin();
        Assert.assertEquals(numberOfBins, countPerBin.length);

        Assert.assertEquals(countPerBin[0], 3); // -1, 0 and 1
        Assert.assertEquals(countPerBin[98], 1); // 99
        Assert.assertEquals(countPerBin[99], 2); // 100 and 101
    }
}
