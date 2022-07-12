package com.xceptance.common.util;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class SimpleCacheAwareArrayListTest
{
    @Test
    public void create()
    {
        final SimpleCacheAwareArrayList<String> l = new SimpleCacheAwareArrayList<>(5);
        Assert.assertEquals(0, l.size());
    }

    @Test
    public void fill()
    {
        final SimpleCacheAwareArrayList<String> l = new SimpleCacheAwareArrayList<>(5);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        Assert.assertEquals(5, l.size());

        Assert.assertEquals("a", l.get(0));
        Assert.assertEquals("b", l.get(1));
        Assert.assertEquals("c", l.get(2));
        Assert.assertEquals("d", l.get(3));
        Assert.assertEquals("e", l.get(4));
    }
    
    @Test
    public void fillIntoSecondSlot()
    {
        final SimpleCacheAwareArrayList<Integer> l = new SimpleCacheAwareArrayList<>(1000);
        
        // fill
        IntStream.range(0, 2000).forEach(l::add);
        Assert.assertEquals(2000, l.size());

        // verify
        IntStream.range(0, 2000).forEach(i -> 
        {
            Assert.assertEquals(i, l.get(i).intValue());
        });

    }
}
