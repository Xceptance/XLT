package com.xceptance.xlt.report.util.rework;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for IntTimeSeriesEntry.
 */
public class IntTimeSeriesEntryTestCopilot41
{
    // --- Constructor Tests ---

    @Test
    public void testDefaultConstructor()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry();
        assertEquals(0, entry.getTotalValue());
        assertEquals(0, entry.getAverageValue());
        assertEquals(0, entry.getMaximumValue());
        assertEquals(0, entry.getMinimumValue());
        assertEquals(0, entry.getCount());
        assertEquals(0, entry.getErrorCount());
        assertArrayEquals(new double[]{}, entry.getValues(), 0.0);
    }

    @Test
    public void testParameterizedConstructor()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(42, true);
        assertEquals(42, entry.getTotalValue());
        assertEquals(42, entry.getAverageValue());
        assertEquals(42, entry.getMaximumValue());
        assertEquals(42, entry.getMinimumValue());
        assertEquals(1, entry.getCount());
        assertEquals(1, entry.getErrorCount());
        assertArrayEquals(new double[]{42.0}, entry.getValues(), 0.0);
    }

    @Test
    public void testNegativeValueConstructor()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(-10, false);
        assertEquals(0, entry.getTotalValue());
        assertEquals(0, entry.getAverageValue());
        assertEquals(0, entry.getMaximumValue());
        assertEquals(0, entry.getMinimumValue());
        assertEquals(1, entry.getCount());
        assertEquals(0, entry.getErrorCount());
        assertArrayEquals(new double[]{0.0}, entry.getValues(), 0.0);
    }

    // --- Value Update Tests ---

    @Test
    public void testUpdateValue()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(5, false);
        entry.updateValue(10, false);
        entry.updateValue(20, true);
        assertEquals(35, entry.getTotalValue());
        assertEquals(3, entry.getCount());
        assertEquals(1, entry.getErrorCount());
        assertEquals(20, entry.getMaximumValue());
        assertEquals(5, entry.getMinimumValue());
        assertArrayEquals(new double[]{5.0, 10.0, 20.0}, entry.getValues(), 0.0);
    }

    @Test
    public void testUpdateValueWithNegative()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(10, false);
        entry.updateValue(-5, false);
        assertEquals(10, entry.getTotalValue());
        assertEquals(2, entry.getCount());
        assertEquals(10, entry.getMaximumValue());
        assertEquals(0, entry.getMinimumValue());
        assertArrayEquals(new double[]{10.0, 0.0}, entry.getValues(), 0.0);
    }

    @Test
    public void testUpdateValueDistinctLimit()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(0, false);
        for (int i = 1; i <= 130; i++)
        {
            entry.updateValue(i, false);
        }
        // Should compress values after limit
        assertEquals(131, entry.getCount());
        assertEquals(0, entry.getMinimumValue());
        assertEquals(130, entry.getMaximumValue());
        assertEquals(0, entry.getErrorCount());
        assertTrue(entry.getValues().length <= 128);
    }

    // --- Statistical Methods ---

    @Test
    public void testStatistics()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(2, false);
        entry.updateValue(4, false);
        entry.updateValue(6, false);
        assertEquals(12, entry.getTotalValue());
        assertEquals(4, entry.getAverageValue());
        assertEquals(6, entry.getMaximumValue());
        assertEquals(2, entry.getMinimumValue());
        assertEquals(3, entry.getCount());
    }

    // --- Merge Tests ---

    @Test
    public void testMergeSameScale()
    {
        IntTimeSeriesEntry a = new IntTimeSeriesEntry(10, false);
        a.updateValue(20, false);
        IntTimeSeriesEntry b = new IntTimeSeriesEntry(30, false);
        b.updateValue(40, false);

        a.merge(b);

        assertEquals(100, a.getTotalValue());
        assertEquals(4, a.getCount());
        assertEquals(40, a.getMaximumValue());
        assertEquals(10, a.getMinimumValue());
        assertArrayEquals(new double[]{10.0, 20.0, 30.0, 40.0}, a.getValues(), 0.0);
    }

    @Test
    public void testMergeDifferentScale()
    {
        IntTimeSeriesEntry a = new IntTimeSeriesEntry(10, false);
        for (int i = 0; i < 200; i++) a.updateValue(i, false);
        IntTimeSeriesEntry b = new IntTimeSeriesEntry(1000, false);
        for (int i = 0; i < 200; i++) b.updateValue(i + 1000, false);

        a.merge(b);

        assertEquals(10 + 199*200/2 + 1000 + (1000+1199)*200/2, a.getTotalValue());
        assertEquals(402, a.getCount());
        assertEquals(1199, a.getMaximumValue());
        assertEquals(0, a.getMinimumValue());
        assertTrue(a.getValues().length <= 128);
    }

    @Test
    public void testMergeErrorCounts()
    {
        IntTimeSeriesEntry a = new IntTimeSeriesEntry(1, true);
        a.updateValue(2, false);
        IntTimeSeriesEntry b = new IntTimeSeriesEntry(3, true);
        b.updateValue(4, true);

        a.merge(b);

        assertEquals(4, a.getCount());
        assertEquals(3, a.getErrorCount());
    }

    // --- Equality and String Representation ---

    @Test
    public void testEqualsAndHashCode()
    {
        IntTimeSeriesEntry a = new IntTimeSeriesEntry(5, false);
        a.updateValue(10, true);
        IntTimeSeriesEntry b = new IntTimeSeriesEntry(5, false);
        b.updateValue(10, true);

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEquals()
    {
        IntTimeSeriesEntry a = new IntTimeSeriesEntry(5, false);
        IntTimeSeriesEntry b = new IntTimeSeriesEntry(6, false);
        assertFalse(a.equals(b));
    }

    @Test
    public void testToString()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(7, false);
        entry.updateValue(14, false);
        String s = entry.toString();
        assertTrue(s.contains("2"));
        assertTrue(s.contains("7"));
        assertTrue(s.contains("14"));
    }

    // --- Edge Cases ---

    @Test
    public void testEmptySeries()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry();
        assertEquals(0, entry.getCount());
        assertEquals(0, entry.getTotalValue());
        assertEquals(0, entry.getErrorCount());
        assertArrayEquals(new double[]{}, entry.getValues(), 0.0);
    }

    @Test
    public void testLargeNumbers()
    {
        IntTimeSeriesEntry entry = new IntTimeSeriesEntry(Integer.MAX_VALUE, false);
        entry.updateValue(Integer.MAX_VALUE, false);
        assertEquals((long)Integer.MAX_VALUE * 2, entry.getTotalValue());
        assertEquals(Integer.MAX_VALUE, entry.getMaximumValue());
        assertEquals(Integer.MAX_VALUE, entry.getMinimumValue());
    }
}
