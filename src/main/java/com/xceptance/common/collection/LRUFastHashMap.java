/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
 * Simple lru hashmap implementation that does not maintain a linked list
 * for tracking purposes rather uses backup hashmap to retire items and
 * pull them back up if needed. It uses the cheaper open hashing maps underneath.
 * It is not thread-safe!
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

    public LRUFastHashMap(int capacity)
    {
        this.capacity = capacity;
        this.slotSize = this.capacity / 3;

        m1 = new FastHashMap<>(2 * slotSize, 0.5f);
        m2 = new FastHashMap<>(10, 0.5f);
        m3 = new FastHashMap<>(10, 0.5f);
    }

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

    public V put(final K key, final V value)
    {
        final V old = m1.put(key,  value);
        if (m1.size() > slotSize)
        {
            m3 = m2;
            m2 = m1;
            m1 = new FastHashMap<>(2 * slotSize, 0.5f);
        }

        return old;
    }
}
