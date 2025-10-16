/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
    public void clear()
    {
        var m = new FastHashMap<String, Integer>();
        m.put("a", 1);
        assertEquals(1, m.size());

        m.clear();
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());
        assertNull(m.get("a"));

        m.put("b", 2);
        assertEquals(1, m.size());
        m.put("a", 3);
        assertEquals(2, m.size());

        m.clear();
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());

        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.put("c", 3);
        assertEquals(3, m.size());
        assertEquals(3, m.keys().size());
        assertEquals(3, m.values().size());

        assertEquals(Integer.valueOf(1), m.get("a"));
        assertEquals(Integer.valueOf(2), m.get("b"));
        assertEquals(Integer.valueOf(3), m.get("c"));
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

    /**
     * Overflow initial size with collision keys. Some hash code for all keys.
     */
    @Test
    public void overflow()
    {
        final FastHashMap<MockKey<String>, Integer> m = new FastHashMap<>(5, 0.5f);
        var data = IntStream.range(0, 152)
            .mapToObj(Integer::valueOf)
            .collect(
                     Collectors.toMap(i -> new MockKey<String>(1, "k" + i),
                                      i -> i));

        // add all
        data.forEach((k, v) -> m.put(k, v));

        // verify
        data.forEach((k, v) -> assertEquals(v, m.get(k)));
        assertEquals(152, m.size());
        assertEquals(152, m.keys().size());
        assertEquals(152, m.values().size());
    }

    /**
     * Try to hit all slots with bad hashcodes
     */
    @Test
    public void hitEachSlot()
    {
        final FastHashMap<MockKey<String>, Integer> m = new FastHashMap<>(15, 0.9f);

        var data = IntStream.range(0, 150)
            .mapToObj(Integer::valueOf)
            .collect(
                     Collectors.toMap(i -> new MockKey<String>(i, "k1" + i),
                                      i -> i));

        // add the same hash codes again but other keys
        data.putAll(IntStream.range(0, 150)
            .mapToObj(Integer::valueOf)
            .collect(
                     Collectors.toMap(i -> new MockKey<String>(i, "k2" + i),
                                      i -> i)));
        // add all
        data.forEach((k, v) -> m.put(k, v));
        // verify
        data.forEach((k, v) -> assertEquals(v, m.get(k)));
        assertEquals(300, m.size());
        assertEquals(300, m.keys().size());
        assertEquals(300, m.values().size());

        // remove all
        data.forEach((k, v) -> m.remove(k));
        // verify
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());

        // add all
        var keys = data.keySet().stream().collect(Collectors.toList());
        keys.stream().sorted().forEach(k -> m.put(k, data.get(k)));
        // put in different order
        Collections.shuffle(keys);
        keys.forEach(k -> m.put(k, data.get(k) + 42));

        // verify
        data.forEach((k, v) -> assertEquals(Integer.valueOf(v + 42), m.get(k)));
        assertEquals(300, m.size());
        assertEquals(300, m.keys().size());
        assertEquals(300, m.values().size());

        // remove in different order
        Collections.shuffle(keys);
        keys.forEach(k -> m.remove(k));

        // verify
        data.forEach((k, v) -> assertNull(m.get(k)));
        assertEquals(0, m.size());
        assertEquals(0, m.keys().size());
        assertEquals(0, m.values().size());
    }

    static class MockKey<T extends Comparable<T>> implements Comparable<MockKey<T>>
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

        @Override
        public int compareTo(MockKey<T> o)
        {
            return o.key.compareTo(this.key);
        }

    }
}

