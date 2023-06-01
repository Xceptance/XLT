/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

/**
 * Simple LRU hashmap implementation that does not maintain a linked list
 * for tracking purposes rather uses backup hashmap to retire items and
 * pull them back up if needed. It uses the cheaper open hashing maps underneath.
 * It is not thread-safe!
 *
 * This is an LRU setup that drops an entire segment (up to 33% of the capacity)
 * when the LRU size is exhausted. THis all is done to avoid all the small updates
 * for hot cache entries, we just read here and don't need any update.
 *
 * @author Rene Schwietzke
 * @since 7.0.0
 */
public class LRUFastHashMap<K, V>
{
    private final int capacity;
    private final int slotSize;

    private FastHashMap<K, V> m1;
    private FastHashMap<K, V> m2;
    private FastHashMap<K, V> m3;

    /**
     * Create a new LRU map with max capacity.
     *
     * @param capacity the max capacity
     */
    public LRUFastHashMap(int capacity)
    {
        this.capacity = capacity;
        this.slotSize = this.capacity / 3;

        m1 = new FastHashMap<>(2 * slotSize, 0.5f);
        m2 = new FastHashMap<>(2 * slotSize, 0.5f);
        m3 = new FastHashMap<>(2 * slotSize, 0.5f);
    }

    /**
     * Fetches a value from the LRU map if it exists, moves the
     * fetched key-value pair in front aka renews it.
     *
     * @param key the key to fetch
     * @return the value of the key or null if it does not exist
     */
    public V get(final K key)
    {
        final V v1 = m1.get(key);
        if (v1 != null)
        {
            return v1;
        }

        final V v2 = m2.get(key);
        if (v2 != null)
        {
            put(key,  v2);
            return v2;
        }

        final V v3 = m3.get(key);
        if (v3 != null)
        {
            put(key,  v3);
            return v3;
        }

        return null;
    }

    /**
     * Fetches a value from the LRU map if it exists but does
     * not update the LRU state. Mainly useful for testing.
     *
     * @param key the key to fetch
     * @return the value of the key or null if it does not exist
     */
    public V getAndNoUpdate(final K key)
    {
        final V v1 = m1.get(key);
        if (v1 != null)
        {
            return v1;
        }

        final V v2 = m2.get(key);
        if (v2 != null)
        {
            return v2;
        }

        return m3.get(key);
    }

    /**
     * Add a key-value pair to the front of the LRU map or moves the existing
     * key to the front and updates the value
     *
     * @param key the key
     * @param value the value
     * @return the old value of the key, if it already existed
     */
    public V put(final K key, final V value)
    {
        // see if we are at capacity first
        if (m1.size() >= slotSize)
        {
            // recycle the old
            final FastHashMap<K, V> oldM3 = m3;
            oldM3.clear();

            m3 = m2;
            m2 = m1;
            m1 = oldM3;
        }

        // update the cache
        final V old = m1.put(key,  value);

        return old;
    }

    /**
     * Returns the size of the cache
     *
     * @return the current size
     */
    public int size()
    {
        return m1.size() + m2.size() + m3.size();
    }
}
