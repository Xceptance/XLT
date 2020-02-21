package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

public class ArithmeticMeanTest
{
    @Test
    public final void testConstructor()
    {
        final ArithmeticMean am = new ArithmeticMean();
        Assert.assertEquals(0, am.getCount());
        Assert.assertTrue(Double.isNaN(am.getMean()));
    }

    @Test
    public final void testAddValue()
    {
        final ArithmeticMean am = new ArithmeticMean();

        am.addValue(1);
        Assert.assertEquals(1, am.getCount());
        Assert.assertTrue(am.getMean() == 1);

        am.addValue(2);
        Assert.assertEquals(2, am.getCount());
        Assert.assertTrue(am.getMean() == 1.5);

        am.addValue(3);
        Assert.assertEquals(3, am.getCount());
        Assert.assertTrue(am.getMean() == 2);

        am.addValue(0);
        Assert.assertEquals(4, am.getCount());
        Assert.assertTrue(am.getMean() == (6.0 / 4));
    }

}
