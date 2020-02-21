package com.xceptance.common.collection;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for implementation of {@link LRUHashMap}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class LRUHashMapTest
{
    private LRUHashMap<Integer, String> map;

    @Before
    public void init()
    {
        map = new LRUHashMap<Integer, String>(5);
    }

    @Test
    public void testConstructor_MaxArgOnly()
    {
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testConstructor_WithMapArg()
    {
        // create temporary HashMap and fill it using dummy data
        final Map<Integer, String> m = new HashMap<Integer, String>();
        for (int i = 0; i < 16; i++)
        {
            m.put(i, Integer.toHexString(i));
        }

        // create new LRUHashMap of size 32 using temporary HashMap of size 16
        // -> no
        // overflow
        map = new LRUHashMap<Integer, String>(m, 32);

        // validate LRUHashMap -> should contain all 16 elements (the hexadecimal literals)
        Assert.assertEquals(16, map.size());
        for (int i = 0; i < 16; i++)
        {
            Assert.assertEquals(Integer.toHexString(i), map.get(i));
        }
    }

    @Test
    public void testConstructor_WithMapArg_Overflow()
    {
        // create new LRUHashMap of MAX_SIZE = 5!!
        map = new LRUHashMap<Integer, String>(5);
        // fill map using 16!! values
        for (int i = 0; i < 16; i++)
        {
            map.put(i, Integer.toHexString(i));
        }
        Assert.assertEquals(5, map.size());

        // validate content of LRUHashMap -> last five elements were kept, the others were dropped
        for (int i = 0; i < 16; i++)
        {
            if (i > 10)
            {
                Assert.assertTrue(map.containsKey(i));
                Assert.assertTrue(map.containsValue(Integer.toHexString(i)));
            }
            else
            {
                Assert.assertFalse(map.containsKey(i));
                Assert.assertFalse(map.containsValue(Integer.toHexString(i)));
            }
        }

    }

    @Test
    public void testRemoveEldestEntry()
    {
        // fill map using dummy data
        Assert.assertNull(map.put(1, "one"));
        Assert.assertNull(map.put(2, "two"));
        Assert.assertNull(map.put(3, "three"));
        Assert.assertNull(map.put(4, "four"));
        Assert.assertNull(map.put(5, "five"));

        // ensure maximum capacity
        Assert.assertEquals(5, map.size());

        // overwrite the 1st element
        Assert.assertEquals("one", map.put(1, "unos"));
        // size unchanged
        Assert.assertEquals(5, map.size());

        // remove the 5th element
        Assert.assertEquals("five", map.remove(5));
        // size reduced
        Assert.assertEquals(4, map.size());

        // again, override the 1st element
        Assert.assertEquals("unos", map.put(1, "een"));
        // size unchanged
        Assert.assertEquals(4, map.size());

        // add a new element -> new the 5th
        Assert.assertNull(map.put(6, "six"));
        // add a new element -> now the 6th
        Assert.assertNull(map.put(7, "seven"));

        // 2nd element was the eldest -> should have been removed from map
        Assert.assertFalse(map.containsKey(2));
    }

}
