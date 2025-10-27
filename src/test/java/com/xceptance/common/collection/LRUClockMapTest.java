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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.Test;

import it.unimi.dsi.util.FastRandom;

/**
 * Unit tests for the {@link LRUClockMap} class. Created with partially with Vertex AI and 
 * manually reviewed to ensure correctness.
 * 
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 * @since 9.2.0
 */
public class LRUClockMapTest
{
    /**
     * A simple tuple record to hold key-value pairs for testing purposes.
     * @param <K> key type
     * @param <V> value type
     */
    record Tuple<K, V>(K k, V v) {};

    // --- Constructor Tests ---

    /**
     * Tests that the constructor throws an IllegalArgumentException when maxSize is less than 4.
     */
    @Test
    public void testConstructor_throwsExceptionForMaxSizeLessThan4()
    {
        // The LRUClockMap constructor enforces a minimum maxSize of 4.
        try
        {
            new LRUClockMap<>(3);
            fail("Expected IllegalArgumentException for maxSize < 4");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("MaxSize must be at least 4", e.getMessage());
        }
    }

    /**
     * Smallest supported size
     */
    @Test
    public void testConstructor_smallestSize()
    {
        int maxSize = 4;
        LRUClockMap<String, Integer> map = new LRUClockMap<>(maxSize);
        assertEquals(0, map.size());
        assertEquals(0, map.trueSize());
        // next power of two after 4 is 8
        assertEquals(8, map.occupiedSpace());
    }

    // --- Size Tests ---

    /**
     * Tests that the size of the map correctly increases when new elements are added.
     */
    @Test
    public void testSize_increasesAfterPut()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("key1", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.trueSize());
        map.put("key2", 2);
        assertEquals(2, map.size());
        assertEquals(2, map.trueSize());
    }

    /**
     * Tests that the size of the map does not change when an existing element's value is updated.
     */
    @Test
    public void testSize_doesNotIncreaseOnUpdate()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("key1", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.trueSize());

        map.put("key1", 10); // Update value for existing key
        assertEquals(1, map.size()); // Size should remain the same
        assertEquals(1, map.trueSize());
    }
    
    /**
     * Tests that the size of the map does not change when an existing element's value is updated.
     */
    @Test
    public void testSize_doesNotIncreaseOnUpdateWhenFull()
    {
        final LRUClockMap<String, String> map = new LRUClockMap<>(8);
        IntStream.range(0, 8).forEach(i -> map.put("key" + i, "v" + i));
        assertEquals(8, map.size());
        assertEquals(8, map.trueSize());

        map.put("key1", "v11"); // Update value for existing key
        assertEquals(8, map.size()); // Size should remain the same
        assertEquals(8, map.trueSize());
    }

    // --- Remove Tests ---

    /**
     * Tests removing the only existing key in a map with one element, ensuring the map is empty afterwards
     */
    @Test
    public void testRemove_existingKey_OneElement()
    {
        LRUClockMap<String, String> map = new LRUClockMap<>(32);

        // remove only element
        map.put("k1", "v1");
        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));

        // remove
        assertEquals("v1", map.remove("k1"));
        assertEquals(0, map.size());
        assertEquals(null, map.get("k1"));

        // repeat
        assertEquals(null, map.remove("k1"));
        assertEquals(0, map.size());

        // add same
        map.put("k1", "v2");
        assertEquals(1, map.size());
        assertEquals("v2", map.get("k1"));

        // remove
        assertEquals("v2", map.remove("k1"));
        assertEquals(0, map.size());
        assertEquals(null, map.get("k1"));
    }

    /**
     * Tests removing an existing key in a map with multiple elements, ensuring the map size decreases accordingly
     */
    @Test
    public void testRemove_existingKey_MultipleElement()
    {
        LRUClockMap<String, String> map = new LRUClockMap<>(32);

        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals(3, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));

        // remove
        assertEquals("v1", map.remove("k1"));
        assertEquals(2, map.size());
        assertEquals(null, map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));

        // remove
        assertEquals("v2", map.remove("k2"));
        assertEquals(1, map.size());
        assertEquals(null, map.get("k1"));
        assertEquals(null, map.get("k2"));
        assertEquals("v3", map.get("k3"));

        // remove
        assertEquals("v3", map.remove("k3"));
        assertEquals(0, map.size());
        assertEquals(null, map.get("k1"));
        assertEquals(null, map.get("k2"));
        assertEquals(null, map.get("k3"));
    }

    /**
     * 
     */
    @Test
    public void testRemove_Random()
    {
        // simple int keys
        testRemove_Random_Internal(i -> new Tuple<>(i, i));

        // simple String keys
        testRemove_Random_Internal(i -> new Tuple<>("k" + i, "v" + i));
        testRemove_Random_Internal(i -> new Tuple<>(i + "k", "v" + i));
        testRemove_Random_Internal(i -> new Tuple<>(i + "k" + i, "v" + i));

        final var r = new FastRandom(42L);

        testRemove_Random_Internal(i -> new Tuple<>(
            r.randomString(FastRandom.ALPHANUMERIC_ALL, i), "v" + i));
        testRemove_Random_Internal(i -> new Tuple<>(
            r.randomString(FastRandom.ALPHANUMERIC_ALL, 1, 30) + i, "v" + i));
        testRemove_Random_Internal(i -> new Tuple<>(
            r.randomString(FastRandom.ALPHANUMERIC_ALL, 10), "v" + i));
    }

    private <K, V> void testRemove_Random_Internal(Function<Integer, Tuple<K, V>> generator)
    {
        int SIZE = 100;
        var data = new ArrayList<Tuple<K, V>>();
        for (int i = 0; i < SIZE; i++)
        {
            data.add(generator.apply(i));
        }
        Collections.shuffle(data);

        // fill
        var map = new LRUClockMap<K, V>(SIZE);
        for (var t : data)
        {
            map.put(t.k, t.v);
        }

        // verify
        for (var t : data)
        {   
            assertEquals(t.v, map.get(t.k)); 
        }
        assertEquals(SIZE, map.size());

        // remove 
        for (int i = SIZE - 1; i >= 0; i--)
        {
            var t = data.get(i);
            assertEquals(t.v, map.remove(t.k));
            assertEquals(i, map.size());

            for (int j = 0; j < i; j++)
            {
                var t2 = data.get(j);
                assertEquals(t2.v, map.get(t2.k));
            }
        }
    }

    /**
     * Tests that attempting to remove a key from an empty map does not cause errors
     */
    @Test
    public void testRemove_EmptyMap()
    {
        LRUClockMap<String, String> map = new LRUClockMap<>(32);
        assertEquals(0, map.size());

        // try remove non existing
        assertEquals(null, map.remove("k4"));
        assertEquals(0, map.size());

        // try remove non existing again
        assertEquals(null, map.remove("k4"));
        assertEquals(0, map.size());
    }

    /**
     * Tests that attempting to remove a non-existing key does not change the size of the map
     */
    @Test
    public void testRemove_nonExisting()
    {
        LRUClockMap<String, String> map = new LRUClockMap<>(32);
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        assertEquals(3, map.size());

        // try remove non existing
        assertEquals(null, map.remove("k4"));
        assertEquals(3, map.size());

        // try remove non existing again
        assertEquals(null, map.remove("k4"));
        assertEquals(3, map.size());
    }

    // --- Put and Get Tests ---

    /**
     * Tests the basic functionality of putting key-value pairs and retrieving them.
     */
    @Test
    public void testPutAndGet_basicFunctionality()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        assertEquals(1, map.get("key1").intValue());
        assertEquals(2, map.get("key2").intValue());
        assertEquals(3, map.get("key3").intValue());
    }

    /**
     * Tests that calling get() for a key that has not been put into the map returns null.
     */
    @Test
    public void testGet_returnsNullForNonexistentKey()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        assertNull(map.get("nonexistentKey"));
        map.put("key1", 1);
        assertNull(map.get("nonexistentKey"));
    }

    /**
     * Tests that putting a key-value pair into the map updates the value for an existing key
     * and returns the old value.
     */
    @Test
    public void testPut_updatesExistingValueAndReturnsOldValue()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("key1", 1);
        assertEquals(1, map.get("key1").intValue());

        // Update the value for "key1"
        Integer oldValue = map.put("key1", 10);
        assertEquals(1, oldValue.intValue()); // Verify that the old value is returned
        assertEquals(10, map.get("key1").intValue()); // Verify that the new value is stored
        assertEquals(1, map.size()); // Size should not change on update
    }

    /**
     * Tests that keys with the same hash code are handled correctly via linear probing.
     * AI was wrong here and did not consider the the ability to use a class with
     * hashcode control.
     */
    @Test
    public void testPutAndGet_handlesHashCollisions()
    {
        LRUClockMap<TestString, String> map = new LRUClockMap<>(16);

        map.put(new TestString("K1", 2112), "1"); // Assume hash code 2112
        assertEquals("1", map.get(new TestString("K1", 2112)));
        assertEquals(1, map.size());

        map.put(new TestString("K2", 2112), "2"); // Assume hash code 2112
        assertEquals("1", map.get(new TestString("K1", 2112)));
        assertEquals("2", map.get(new TestString("K2", 2112)));
        assertEquals(2, map.size());

        map.put(new TestString("K3", 2112), "3"); // Assume hash code 2112
        assertEquals("3", map.get(new TestString("K3", 2112)));
        assertEquals("2", map.get(new TestString("K2", 2112)));
        assertEquals("1", map.get(new TestString("K1", 2112)));
        assertEquals(3, map.size());
    }

    /**
     * Check that we update correctly when we have hash collisions.
     */
    @Test
    public void testPutUpdate_handlesHashCollisions()
    {
        LRUClockMap<TestString, String> map = new LRUClockMap<>(8);

        TestString key1 = new TestString("A", 42);
        TestString key2 = new TestString("B", 42);

        map.put(key1, "value1");
        map.put(key2, "value2");

        assertEquals("value1", map.get(key1));
        assertEquals("value2", map.get(key2));
        assertEquals(2, map.size());

        map.put(key1, "value3");
        map.put(key2, "value4");

        assertEquals("value3", map.get(key1));
        assertEquals("value4", map.get(key2));
        assertEquals(2, map.size());

        map.put(key2, "value6");
        map.put(key1, "value5");

        assertEquals("value5", map.get(key1));
        assertEquals("value6", map.get(key2));
        assertEquals(2, map.size());
    }

    /**
     * We have two values with the same hash code. But one is in the map and
     * the other is not. We check a path in expensiveGet with that.
     */
    @Test
    public void testGet_dontFindWithSameHash()
    {
        var ts1 = new TestString("k1", 4);
        var ts2 = new TestString("k2", 4);
        
        LRUClockMap<TestString, String> map = new LRUClockMap<>(8);
        map.put(ts1, "v1");
        assertEquals("v1", map.get(ts1));
        assertEquals(null, map.get(ts2));
    }
    
    /**
     * Test the case that we have a full map and we try to find a key in a chain
     * of hash collisions that is also outdated so it will be given a second chance.
     */
    @Test
    public void testGet_findWithSameHashButTwoKeysAndFullMap()
    {
        var ts1 = new TestString("k1", 1);
        var ts2 = new TestString("k2", 1);
        var ts3 = new TestString("k3", 3);
        var ts4 = new TestString("k4", 3);
        var ts5 = new TestString("k5", 3);
        
        var map = new LRUClockMap<TestString, String>(4);
        map.put(ts1, "v1");
        map.put(ts2, "v2");
        map.put(ts3, "v3");
        map.put(ts4, "v4");
        
        // cause second change flag to be false
        map.put(ts5, "v5");
        
        // get it back to true
        map.get(ts4);
        
        assertEquals("v2", map.get(ts2));
        assertEquals("v3", map.get(ts3));
        assertEquals("v4", map.get(ts4));
        assertEquals("v5", map.get(ts5));
    }
    
    // --- Eviction Tests ---

    /**
     * Smallest map test
     */
    @Test
    public void testEviction_Smallest()
    {
        int maxSize = 42;
        LRUClockMap<String, String> map = new LRUClockMap<>(maxSize);

        var r = new FastRandom(7L);
        for (int i = 0; i < 42000; i++)
        {
            var k = new FastRandom(i).randomString(FastRandom.CHARS, r.nextInt(10, 50));
            var v = "v" + i;

            map.put(k, v);
        }
        assertEquals(maxSize, map.size());
        assertEquals(maxSize, map.trueSize());

        // get all data and hash into a new map
        var keys = map.keys();

        LRUClockMap<String, String> newMap = new LRUClockMap<>(maxSize);
        FastHashMap<String, String> fMap = new FastHashMap<>();
        for (var k : keys)
        {
            newMap.put(k, map.getRaw(k));
            fMap.put(k, map.getRaw(k));
        }

        assertEquals(maxSize, newMap.size());
        assertEquals(maxSize, newMap.trueSize());
    }

    /**
     * Tests that when the map is full, adding a new element triggers eviction,
     * and the map size remains at maxSize. It also verifies the new element is present
     * and one of the old elements is gone. Because of the element type, we know
     * where it lands in the map.
     */
    @Test
    public void testEviction_NoneWhenOnlyUpdate()
    {
        int maxSize = 4;
        LRUClockMap<TestString, String> map = new LRUClockMap<>(maxSize);

        // Fill the map to its maximum logical capacity.
        map.put(new TestString("k1", 1), "v1");
        map.put(new TestString("k2", 2), "v2");
        map.put(new TestString("k3", 3), "v3");
        map.put(new TestString("k4", 4), "v4");

        assertEquals(maxSize, map.size());

        // Update only existing elements. This should NOT trigger eviction.
        map.put(new TestString("k4", 4), "v44");

        // Assertions for eviction:
        // The size should remain capped at maxSize.
        assertEquals(maxSize, map.size());
        assertEquals(maxSize, map.trueSize());

        // one is gone and that depends on the insert pos and the next elements
        assertEquals("v1", map.getRaw(new TestString("k1", 1)));
        assertEquals("v2", map.getRaw(new TestString("k2", 2)));
        assertEquals("v3", map.getRaw(new TestString("k3", 3)));
        assertEquals("v44", map.getRaw(new TestString("k4", 4)));
    }

    /**
     * Eviction with large map and only put misses
     */
    @Test
    public void testEviction_withLargeMapAndOnlyPutMisses()
    {
        int maxSize = 100;
        LRUClockMap<String, String> map = new LRUClockMap<>(maxSize);

        // Fill the map often and provoke cleanup
        var r = new FastRandom(7L);
        for (int i = 0; i < 1_000_000; i++)
        {
            map.put(r.randomString(FastRandom.CHARS, r.nextInt(20)), "v" + i);
        }

        assertEquals(maxSize, map.size());
        assertEquals(maxSize, map.trueSize());

        // Add a new element. This should trigger eviction.
        map.put("1000N1000", "1000vN1000");

        // Assertions for eviction:
        // The size should remain capped at maxSize.
        assertEquals(maxSize, map.size());
        assertEquals(maxSize, map.trueSize());

        // Check that the new element is present.
        assertEquals("1000vN1000", map.getRaw("1000N1000"));

        // Check that one of the old elements is gone.
        assertNull(map.getRaw("k0")); // k0 should be evicted
    }

    /**
     * Tests that when the map is full, adding a new element triggers eviction,
     * and the map size remains at maxSize. It also verifies the new element is present
     * and one of the old elements is gone. Because of the element type, we know
     * where it lands in the map.
     */
    @Test
    public void testEviction_whenMapIsFull()
    {
        int maxSize = 4;
        LRUClockMap<TestString, String> map = new LRUClockMap<>(maxSize);

        // Fill the map to its maximum logical capacity.
        map.put(new TestString("k1", 1), "v1");
        map.put(new TestString("k2", 2), "v2");
        map.put(new TestString("k3", 3), "v3");
        map.put(new TestString("k4", 4), "v4");

        assertEquals(maxSize, map.size());

        // Add a fifth element. This should trigger eviction.
        map.put(new TestString("k5", 5), "v5");

        // Assertions for eviction:
        // The size should remain capped at maxSize.
        assertEquals(maxSize, map.size());
        assertEquals(maxSize, map.trueSize());

        // one is gone and that depends on the insert pos and the next elements
        assertNull(map.getRaw(new TestString("k1", 1))); // k1 should be evicted
        assertEquals("v2", map.getRaw(new TestString("k2", 2)));
        assertEquals("v3", map.getRaw(new TestString("k3", 3)));
        assertEquals("v4", map.getRaw(new TestString("k4", 4)));
        // The newly added element must be present.
        assertEquals("v5", map.getRaw(new TestString("k5", 5)));

        // if we touch k1 know, it should survive the next eviction
        assertEquals("v2", map.get(new TestString("k2", 2)));
        map.put(new TestString("k6", 6), "v6");

        assertNull(map.getRaw(new TestString("k1", 1))); // k1 should be still gone
        assertNull(map.getRaw(new TestString("k3", 3))); // k3 should be evicted
        assertEquals("v4", map.getRaw(new TestString("k4", 4)));
        assertEquals("v5", map.getRaw(new TestString("k5", 5)));
        assertEquals("v6", map.getRaw(new TestString("k6", 6)));
    }

    /**
     * Tests eviction behavior when multiple TestString keys with the same hash code are added.
     * Adds 7 unique TestString keys (same hash, different values), fills the map, then pushes out
     * old entries and verifies which keys remain and which are evicted.
     */
    private void testEviction_withHashCollisionsAndPushOut(final int maxSize, final int collisionHash)
    {
        var map = new LRUClockMap<TestString, String>(maxSize);

        // Add 7 TestString keys with the same hash code but different values
        var collisionKeys = new ArrayList<TestString>();
        for (int i = 0; i < maxSize; i++)
        {
            TestString key = new TestString("K" + i, collisionHash);
            collisionKeys.add(key);
            map.put(key, "V" + key.key);
        }

        // Verify all keys are present
        for (var k : collisionKeys)
        {
            assertEquals("V" + k.key, map.getRaw(k));
        }

        // Add 7 new keys to push out the old ones
        var newKeys = new ArrayList<TestString>();
        for (int i = 0; i < maxSize; i++)
        {
            TestString key = new TestString("N" + i, collisionHash);
            newKeys.add(key);
            map.put(key, "NV" + key.key);
        }

        // Now, only the new keys should remain; the old ones should be evicted
        for (var k : collisionKeys)
        {
            assertNull(map.getRaw(k));
        }
        for (var k : newKeys)
        {
            assertEquals("NV" + k.key, map.getRaw(k));
        }
    }

    @Test
    public void testEviction_withHashCollisionsAndPushOut_StartAt_X()
    {
        testEviction_withHashCollisionsAndPushOut(7, 0);
        testEviction_withHashCollisionsAndPushOut(7, 3);
        testEviction_withHashCollisionsAndPushOut(7, 9);
        testEviction_withHashCollisionsAndPushOut(7, 15);
    }

    /**
     * Tests the clock sweep mechanism: an element accessed via get() has its 'secondChance'
     * flag reset. This makes it survive the current eviction cycle, while unaccessed elements
     * might be evicted. Specifically, an element is evicted on its second consecutive pass
     * by the clock hand if not accessed. `get()` ensures it survives the first pass.
     */
    @Test
    public void testEviction_accessedElementSurvivesFirstEvictionPass()
    {
        int maxSize = 4;
        LRUClockMap<String, Integer> map = new LRUClockMap<>(maxSize);

        map.put("k1", 1); // Accessed item
        map.put("k2", 2); // Unaccessed item 1
        map.put("k3", 3); // Unaccessed item 2
        map.put("k4", 4); // Unaccessed item 2

        assertEquals(maxSize, map.size());

        // Access "k1". This should set its 'secondChance' flag to true.
        map.get("k");

        // Add a fifth element ("k5") to trigger eviction.
        map.put("k", 5); // broken as reminder

        // we cleared one item out...
        assertEquals(maxSize, map.size()); // Size should remain capped.

        // Check that k1 is still present.
        assertEquals(1, map.get("k1").intValue());
        // Check that k4 (the newly added item) is present.
        assertEquals(4, map.get("k4").intValue());

        // Check that exactly one of the unaccessed original items (k2, k3) was evicted.
        Collection<String> keysAfterEviction = map.keys();
        boolean k2Present = keysAfterEviction.contains("k2");
        boolean k3Present = keysAfterEviction.contains("k3");

        // Exactly one of k2 or k3 should be missing.
        assertTrue(k2Present ^ k3Present);
    }

    // --- Clear Tests ---

    /**
     * Tests that the {@code clear()} method effectively empties the map,
     * resetting its size to zero and making previously stored elements inaccessible.
     */
    @Test
    public void testClear_emptiesMapAndResetsState()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("key1", 1);
        map.put("key2", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("key1").intValue());

        map.clear(); // Clear the map

        assertEquals(0, map.size()); // Verify size is zero
        assertEquals(0, map.trueSize()); // nothing to be found
        assertNull(map.get("key1")); // Verify elements are no longer accessible
        assertNull(map.get("key2"));
    }

    /**
     * Tests that calling {@code clear()} on an already empty map does not cause errors
     * and leaves the map in an empty state.
     */
    @Test
    public void testClear_onEmptyMap_doesNothing()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        assertEquals(0, map.size());

        map.clear(); // Call clear on an empty map

        assertEquals(0, map.size()); // Map should remain empty
    }

    // --- Keys Tests ---

    /**
     * Tests that the {@code keys()} method returns an empty list when the map is empty.
     */
    @Test
    public void testKeys_emptyMap()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        List<String> keys = map.keys();
        assertEquals(0, keys.size());
    }

    /**
     * Tests that the {@code keys()} method returns all keys currently present in the map
     * after several elements have been added. The order of keys is not guaranteed so
     * this test might break when the hash codes of the strings change.
     */
    @Test
    public void testKeys_afterPuts()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("keyA", 1);
        map.put("keyB", 2);
        map.put("keyC", 3);
        map.put("keyD", 4);
        map.put("keyE", 5);
        map.put("keyF", 6);

        List<String> keys = map.keys();
        assertEquals(6, keys.size());
        assertEquals("keyA", keys.get(0));
        assertEquals("keyB", keys.get(1));
        assertEquals("keyE", keys.get(2));
        assertEquals("keyF", keys.get(3));
        assertEquals("keyC", keys.get(4));
        assertEquals("keyD", keys.get(5));
    }

    /**
     * Tests that the {@code keys()} method reflects updates to existing keys
     * and does not return duplicate keys for the same key.
     */
    @Test
    public void testKeys_afterUpdate()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("keyA", 1);
        map.put("keyB", 2);
        map.put("keyA", 10); // Update "keyA"

        Collection<String> keys = map.keys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("keyA"));
        assertTrue(keys.contains("keyB"));
    }

    /**
     * Tests that after clearing the map, the {@code keys()} method returns an empty list.
     */
    @Test
    public void testKeys_afterClear()
    {
        LRUClockMap<String, Integer> map = new LRUClockMap<>(32);
        map.put("key1", 1);
        map.put("key2", 2);
        map.clear(); // Clear the map

        var keys = map.keys();
        assertTrue(keys.isEmpty()); // Should be empty after clear
    }

    static class TestString
    {
        public final String key;
        public final int hashCode;

        public TestString(final String key, int hashCode)
        {
            this.key = key;
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode()
        {
            return this.hashCode;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            TestString other = (TestString) obj;
            return obj.hashCode() == this.hashCode && Objects.equals(this.key, other.key);
        }

        @Override
        public String toString()
        {
            return key; // "[value=" + value + ", hashCode=" + hashCode + ", realHash=" + (hashCode ^ (hashCode >>> 16)) + "]";
        }
    }
}
