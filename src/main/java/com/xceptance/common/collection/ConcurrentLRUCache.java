/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The ConcurrentLRUCache is an implementation that should replace all required caches with LRU characteristics, where a
 * map access pattern (get, set, contains) is needed but minimal synchronization necessary.
 * </p>
 * <p>
 * This implementation uses several ConcurrentHashMaps to achieve the goal of a LRU-like behavior while avoiding a lot
 * of synchronization overhead that is normally connected to the use of a LinkedMap for LRU purposes.
 * </p>
 * <p>
 * ConcurrentLRUCache does not guarantee 100% consistency in terms of LRU behavior and its final size. This is also the
 * reason for not implementing the Map interface to avoid any confusion about the purpose of this cache. It is not a map
 * replacement.
 * </p>
 * 
 * @param <K>
 *            type of cache keys
 * @param <V>
 *            type of cache values
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class ConcurrentLRUCache<K, V>
{
    /**
     * The cache.
     */
    private volatile Cache<K, V> cache;

    /**
     * The desired size of the cache
     */
    private final int maxSize;

    /**
     * Minimum size of cache.
     */
    public final static int MIN_SIZE = 10;

    /**
     * Creates a new {@link ConcurrentLRUCache} object with the given maximum size.
     * 
     * @param maxSize
     *            the maximum size of the cache
     */
    public ConcurrentLRUCache(final int maxSize)
    {
        if (maxSize < MIN_SIZE)
        {
            throw new IllegalArgumentException("Cache setting too small. Minimal cache size is " + MIN_SIZE);
        }

        this.maxSize = maxSize;

        // initialize the cache
        cache = new Cache<K, V>(maxSize);
    }

    /**
     * Get data from the cache. This access will touch the data and move it to the top of the cache to make it recently
     * used.
     * 
     * @param key
     *            the key to access the cache entry
     * @return the value, or <code>null</code> if the key was not found in the cache
     */
    public final V get(final K key)
    {
        final V value = getCacheEntry(key);

        if (value != null)
        {
            // put it back on top to make the LRU working
            // this will make the data available in the first map but does
            // not remove it from the other maps, which is not important
            // because we only modify storage1
            put(key, value);
        }

        return value;
    }

    /**
     * Get data from the cache. No LRU operation will be performed.
     * 
     * @param key
     *            the key to access the cache entry
     * @return the value, or <code>null</code> if the key was not found in the cache
     */
    private V getCacheEntry(final K key)
    {
        // get consistent view
        final Cache<K, V> localCache = cache;

        // try to find the data in the different areas
        V value = localCache.storage1.get(key);

        if (value == null)
        {
            value = localCache.storage2.get(key);

            if (value == null)
            {
                value = localCache.storage3.get(key);
            }
        }

        return value;
    }

    /**
     * Stores a key-value pair in the cache, possibly overwriting any previously stored data.
     * 
     * @param key
     *            the key to use
     * @param value
     *            the value to store
     */
    public final void put(final K key, final V value)
    {
        // get consistent view
        final Cache<K, V> localCache = cache;

        // check the size
        if (localCache.storage1.size() >= localCache.storageSize)
        {
            // reorganize and set the global view
            // this will kick storage3 out of sight
            // because cache is volatile and Cache is synchronized using
            // its constructor, we are memory safe
            cache = new Cache<K, V>(localCache);

            // put the data into the fresh image, this will put the data into
            // the map even so, someone else did the reorganization at the
            // same time, creating another fresh map and we do not see our
            // own reorganized image. If we use our own map, we might end up
            // losing the data.
            cache.storage1.put(key, value);
        }
        else
        {
            // put the data, because the cache was OK before. If someone else
            // was reorganizing in between, the data will end up in the new
            // storage2. Only under heavy write traffic, we might end up writing
            // into 3 or even worse into the kicked out map. But we prefer that,
            // because it is a cache without hard guarantees and it saves us
            // a volatile operation.
            localCache.storage1.put(key, value);
        }

        // done
    }

    /**
     * Removes an entry from the cache.
     * 
     * @param key
     *            the key to be removed
     * @return the old value, or <code>null</code> if no entry was found for the key
     */
    public final V remove(final K key)
    {
        // get consistent view
        final Cache<K, V> localCache = cache;

        // remove the entry from *all* storages
        final V value1 = localCache.storage1.remove(key);
        final V value2 = localCache.storage2.remove(key);
        final V value3 = localCache.storage3.remove(key);

        // return the latest/freshest value
        if (value1 != null)
        {
            return value1;
        }

        if (value2 != null)
        {
            return value2;
        }

        return value3;
    }

    /**
     * Checks whether or not there is a cache entry for the given key. No LRU operation will be performed. There is no
     * guarantee that a later {@link #get(Object)} operation with this key will be successful, because the entry could
     * have been removed in the meantime due to the LRU characteristics of the cache.
     * 
     * @param key
     *            the key
     * @return <code>true</code> if an entry was found, <code>false</code> otherwise
     */
    public final boolean contains(final K key)
    {
        return getCacheEntry(key) != null;
    }

    /**
     * Clear the cache
     */
    public void clear()
    {
        cache = new Cache<K, V>(maxSize);
    }

    /**
     * Returns the current active cache size. This is NOT the set cache size limit. Since elements might be stored
     * several times (once per storage), this value is only a rough estimation.
     * 
     * @return the current size of the cache
     */
    public final int size()
    {
        return cache.size();
    }

    /**
     * A cache structure that is memory safe.
     */
    private class Cache<T, S>
    {
        /**
         * Size of storage.
         */
        private final int storageSize;

        /**
         * 1st level cache.
         */
        private final ConcurrentHashMap<T, S> storage1;

        /**
         * 2nd level cache.
         */
        private final ConcurrentHashMap<T, S> storage2;

        /**
         * 3rd level cache.
         */
        private final ConcurrentHashMap<T, S> storage3;

        /**
         * Creates a new cache.
         * 
         * @param maxSize
         *            maximum size
         */
        public Cache(final int maxSize)
        {
            final int blockSize = 3;

            storageSize = maxSize / blockSize;

            // the three maps, avoid rehashing, size them bigger
            storage1 = new ConcurrentHashMap<T, S>(2 * storageSize + 1);

            // we only read from these maps, so we just need empty maps
            storage2 = new ConcurrentHashMap<T, S>(blockSize);
            storage3 = new ConcurrentHashMap<T, S>(blockSize);
        }

        /**
         * Creates a new cache from the given previous cache.
         * 
         * @param previousCache
         *            previous cache
         */
        public Cache(final Cache<T, S> previousCache)
        {
            storageSize = previousCache.storageSize;

            // the three maps, avoid rehashing, size them bigger
            storage1 = new ConcurrentHashMap<T, S>(2 * storageSize + 1);
            storage2 = previousCache.storage1;
            storage3 = previousCache.storage2;
        }

        /**
         * Returns the size of this cache.
         * 
         * @return entire size of cache
         */
        public int size()
        {
            return storage1.size() + storage2.size() + storage3.size();
        }
    }
}
