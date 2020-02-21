package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ValueSetTest
{
    /**
     * Test empty condition getValues
     */
    @Test
    public final void testGetValues_Empty()
    {
        final ValueSet set = new ValueSet();
        Assert.assertEquals(0, set.getValues().length);
        Assert.assertEquals(0, set.getValueCount());
    }

    /**
     * Test empty condition getMinimumTime
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetMinimumTime_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getMinimumTime();
    }

    /**
     * Test empty condition getMaximumTime
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetMaximumTime_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getMaximumTime();
    }

    /**
     * Test empty condition getFirstSecond
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetFirstSecond_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getFirstSecond();
    }

    /**
     * Test empty condition getLastSecond
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetLastSecond_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getLastSecond();
    }

    /**
     * Test empty condition getLengthInSeconds
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetLengthInSeconds_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getLengthInSeconds();
    }

    @Test
    public final void testAddOrUpdateValue_OnValue()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042000L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042000010L, set.getMaximumTime());
        Assert.assertEquals(1, set.getValueCount());
        Assert.assertEquals(1L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_1secDiff()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);
        set.addOrUpdateValue(1337042001020L, 100);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042001L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042001020L, set.getMaximumTime());
        Assert.assertEquals(2, set.getValueCount());
        Assert.assertEquals(2L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50, 100
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_2secDiff()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);
        set.addOrUpdateValue(1337042002000L, 100);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042002L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042002000L, set.getMaximumTime());
        Assert.assertEquals(2, set.getValueCount());
        Assert.assertEquals(3L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50, 0, 100
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_11secDiff()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);
        set.addOrUpdateValue(1337042002000L, 100);
        set.addOrUpdateValue(1337042011000L, 200);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042011L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042011000L, set.getMaximumTime());
        Assert.assertEquals(3, set.getValueCount());
        Assert.assertEquals(12L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50, 0, 100, 0, 0, 0, 0, 0, 0, 0, 0, 200
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_DifferentCreationSequence()
    {
        final ValueSet set1 = new ValueSet();
        set1.addOrUpdateValue(1337042000000L, 50);
        set1.addOrUpdateValue(1337042002000L, 100);
        set1.addOrUpdateValue(1337042002010L, 200);
        set1.addOrUpdateValue(1337042012000L, 150);

        final ValueSet set2 = new ValueSet();
        set2.addOrUpdateValue(1337042012000L, 150);
        set2.addOrUpdateValue(1337042002010L, 200);
        set2.addOrUpdateValue(1337042002000L, 100);
        set2.addOrUpdateValue(1337042000000L, 50);

        final ValueSet set3 = new ValueSet();
        set3.addOrUpdateValue(1337042012000L, 150);
        set3.addOrUpdateValue(1337042002000L, 100);
        set3.addOrUpdateValue(1337042002010L, 200);
        set3.addOrUpdateValue(1337042000000L, 50);

        final ValueSet set4 = new ValueSet();
        set4.addOrUpdateValue(1337042000000L, 50);
        set4.addOrUpdateValue(1337042002010L, 200);
        set4.addOrUpdateValue(1337042002000L, 100);
        set4.addOrUpdateValue(1337042012000L, 150);

        Assert.assertEquals(set1, set2);
        Assert.assertEquals(set1, set3);
        Assert.assertEquals(set1, set4);
    }

    /**
     * TODO: Ignored until we decide whether MinMaxValueSet.getValues() can return up to size or 2x size values.
     */
    @Test
    @Ignore
    public final void testToMinMaxValueSet()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000000L, 50);
        set.addOrUpdateValue(1337042001000L, 100);
        set.addOrUpdateValue(1337042002000L, 100);
        set.addOrUpdateValue(1337042011000L, 200);
        set.addOrUpdateValue(1337042019000L, 300);

        final MinMaxValue MMV0 = new MinMaxValue(0);

        // 20 seconds
        final MinMaxValue[] expected = new MinMaxValue[]
            {
                new MinMaxValue(50), new MinMaxValue(100), new MinMaxValue(100), MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, MMV0,
                new MinMaxValue(200), MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, new MinMaxValue(300)
            };

        final MinMaxValue[] actual = set.toMinMaxValueSet(20).getValues();
        Assert.assertArrayEquals(expected, actual);

        // 20 seconds
        final MinMaxValue[] expected10 = new MinMaxValue[]
            {
                new MinMaxValue(50).merge(new MinMaxValue(100)), new MinMaxValue(100).merge(MMV0), new MinMaxValue(0).merge(MMV0),
                new MinMaxValue(0).merge(MMV0), new MinMaxValue(0).merge(MMV0), new MinMaxValue(0).merge(new MinMaxValue(200)),
                new MinMaxValue(0).merge(MMV0), new MinMaxValue(0).merge(MMV0), new MinMaxValue(0).merge(MMV0),
                new MinMaxValue(0).merge(new MinMaxValue(300))
            };

        final MinMaxValue[] actual10 = set.toMinMaxValueSet(10).getValues();
        Assert.assertArrayEquals(expected10, actual10);
    }

}
