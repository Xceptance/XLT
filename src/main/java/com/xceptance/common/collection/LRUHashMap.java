/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * LRUHashMap implementation that behaves exactly like a normal HashMap. The size of the map is always limited to
 * maxSize. This prevents the map from growing. Any new entry will remove the least frequently used one from the map
 * (LRU).
 * </p>
 * <p>
 * An entry is moved to the top of the list at any get or put operation. The get operation does not structurally modify
 * the map and therefore does not throw an exception in case of iterating and doing gets on values.
 * </p>
 * 
 * @param <K>
 *            type of map entry keys
 * @param <V>
 *            type of map entry values
 * @author Rene Schwietzke
 */
public class LRUHashMap<K, V> extends LinkedHashMap<K, V>
{
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -6633745418108562019L;

    /**
     * The initial size of the new map.
     */
    private static final int INITIAL_SIZE = 3;

    /**
     * The rehashing default fill size.
     */
    private static final float INITIAL_REHASHING_SIZE = 0.75f;

    /**
     * Maximum size of hashmap, limits the amount of content.
     */
    private final int maxSize;

    /**
     * Constructs a new, empty map with the specified maxSize.
     * 
     * @param max
     *            the size limit of the map
     */
    public LRUHashMap(final int max)
    {
        super(INITIAL_SIZE, INITIAL_REHASHING_SIZE, true);

        this.maxSize = max;
    }

    /**
     * Constructs a new map with data from the another map.
     * 
     * @param map
     *            another map to copy data from
     * @param max
     *            the size limit of the map
     */
    public LRUHashMap(final Map<K, V> map, final int max)
    {
        super(INITIAL_SIZE, INITIAL_REHASHING_SIZE, true);

        this.maxSize = max;

        putAll(map);
    }

    /**
     * Returns <tt>true</tt> if this map should remove its eldest entry.
     * 
     * @param eldest
     *            The least recently inserted entry in the map, or if this is an access-ordered map, the least recently
     *            accessed entry. This is the entry that will be removed if this method returns <tt>true</tt>. If the
     *            map was empty prior to the <tt>put</tt> or <tt>putAll</tt> invocation resulting in this invocation,
     *            this will be the entry that was just inserted; in other words, if the map contains a single entry, the
     *            eldest entry is also the newest.
     * @return <tt>true</tt> if the eldest entry should be removed from the map; <tt>false</tt> if it should be
     *         retained.
     */
    @Override
    protected final boolean removeEldestEntry(final Map.Entry<K, V> eldest)
    {
        return size() > maxSize;
    }
}
