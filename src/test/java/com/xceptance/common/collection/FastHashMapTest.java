package com.xceptance.common.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;

public class FastHashMapTest
{
    @Test
    public void happyPath()
    {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("a", 1);
        f.put("b", 2);
        f.put("c", 3);
        f.put("d", 4);
        f.put("e", 5);

        assertEquals(5, f.size());
        assertEquals(Integer.valueOf(1), f.get("a"));
        assertEquals(Integer.valueOf(4), f.get("d"));
        assertEquals(Integer.valueOf(3), f.get("c"));
        assertEquals(Integer.valueOf(5), f.get("e"));
        assertEquals(Integer.valueOf(2), f.get("b"));

        f.put("b", 20);
        assertEquals(5, f.size());
        assertEquals(Integer.valueOf(1), f.get("a"));
        assertEquals(Integer.valueOf(4), f.get("d"));
        assertEquals(Integer.valueOf(20), f.get("b"));
        assertEquals(Integer.valueOf(3), f.get("c"));
        assertEquals(Integer.valueOf(5), f.get("e"));
    }

    @Test
    public void keys()
    {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("aa", 1);
        f.put("bb", 2);
        f.put("cc", 3);
        f.put("dd", 4);
        f.put("ee", 5);

        {
            final List<String> k = f.keys();
            assertEquals(5, k.size());
            assertTrue(k.contains("aa"));
            assertTrue(k.contains("bb"));
            assertTrue(k.contains("cc"));
            assertTrue(k.contains("dd"));
            assertTrue(k.contains("ee"));
        }

        assertEquals(Integer.valueOf(3), f.remove("cc"));
        f.remove("c");
        {
            final List<String> k = f.keys();
            assertEquals(4, k.size());
            assertTrue(k.contains("aa"));
            assertTrue(k.contains("bb"));
            assertTrue(k.contains("dd"));
            assertTrue(k.contains("ee"));
        }

        f.put("zz", 10);
        f.remove("c");
        {
            final List<String> k = f.keys();
            assertEquals(5, k.size());
            assertTrue(k.contains("aa"));
            assertTrue(k.contains("bb"));
            assertTrue(k.contains("dd"));
            assertTrue(k.contains("ee"));
            assertTrue(k.contains("zz"));
        }
        
        // ask for something unknown
        assertNull(f.get("unknown"));
    }
    
    @Test
    public void values()
    {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("aa", 1);
        f.put("bb", 2);
        f.put("cc", 3);
        f.put("dd", 4);
        f.put("ee", 5);

        {
            final List<Integer> values = f.values();
            assertEquals(5, values.size());
            assertTrue(values.contains(1));
            assertTrue(values.contains(2));
            assertTrue(values.contains(3));
            assertTrue(values.contains(4));
            assertTrue(values.contains(5));
        }

        assertEquals(Integer.valueOf(3), f.remove("cc"));
        f.remove("c");
        {
            final List<Integer> values = f.values();
            assertEquals(4, values.size());
            assertTrue(values.contains(1));
            assertTrue(values.contains(2));
            assertTrue(values.contains(4));
            assertTrue(values.contains(5));
        }
    }

    @Test
    public void remove()
    {
        final FastHashMap<String, Integer> f = new FastHashMap<>(3, 0.5f);
        f.put("a", 1);
        f.put("b", 2);
        f.put("c", 3);
        f.put("d", 4);
        f.put("e", 5);

        f.remove("b");
        f.remove("d");

        assertEquals(3, f.size());
        assertEquals(Integer.valueOf(1), f.get("a"));
        assertEquals(Integer.valueOf(3), f.get("c"));
        assertEquals(Integer.valueOf(5), f.get("e"));
        assertNull(f.get("d"));
        assertNull(f.get("b"));

        // remove again
        assertNull(f.remove("b"));
        assertNull(f.remove("d"));
        
        f.put("d", 6);
        f.put("b", 7);
        assertEquals(Integer.valueOf(7), f.get("b"));
        assertEquals(Integer.valueOf(6), f.get("d"));
    }

    @Test
    public void collision()
    {
        var f = new FastHashMap<MockKey<String>, String>(13, 0.5f);
        IntStream.range(0, 15).forEach(i -> {
            f.put(new MockKey<String>(12, "k" + i), "v" + i);
        }); 
        
        assertEquals(15, f.size());
        
        IntStream.range(0, 15).forEach(i -> {
            assertEquals("v" + i, f.get(new MockKey<String>(12, "k" + i)));
        });

        // round 2
        IntStream.range(0, 20).forEach(i -> {
            f.put(new MockKey<String>(12, "k" + i), "v" + i);
        }); 
        
        assertEquals(20, f.size());
        
        IntStream.range(0, 20).forEach(i -> {
            assertEquals("v" + i, f.get(new MockKey<String>(12, "k" + i)));
        });
        
        // round 3
        IntStream.range(0, 10).forEach(i -> {
            assertEquals("v" + i, f.remove(new MockKey<String>(12, "k" + i)));
        });
        IntStream.range(10, 20).forEach(i -> {
            assertEquals("v" + i, f.get(new MockKey<String>(12, "k" + i)));
        });
    }
    
    static class MockKey<T>
    {
        public final T key;
        public final int hash; 
        
        public MockKey(int hash, T key)
        {
            this.hash = hash;
            this.key = key;
        }
        
        @Override
        public int hashCode()
        {
            return hash;
        }
        
        @Override 
        public boolean equals(Object o)
        {
            var t = (MockKey<T>) o;
            return hash == o.hashCode() && key.equals(t.key);
        }

        @Override
        public String toString()
        {
            return "MockKey [key=" + key + ", hash=" + hash + "]";
        }
        
    }
}

