/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * This is a simple LRU map implementation based on the clock algorithm.
 * Not thread-safe! Null key and value support was removed. 
 * The LRU part is not really predictable, but it is fast and simple. 
 * We don't keep a linked list, rather a marker that give every entry a second chance. 
 * If the second chance is false, we can evict this entry. This can happen
 * rather early. In general, it is still a good enough LRU implementation.
 * 
 * Uses flat parallel arrays instead of per-entry wrapper nodes for zero allocation on
 * insert/eviction and minimal pointer indirection.
 * 
 * https://www.geeksforgeeks.org/operating-systems/second-chance-or-clock-page-replacement-policy/
 *
 * @since 9.1.0
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class LRUClockMap<K, V>
{
    /**
     * Flat key table ({@code null} indicates a free slot).
     */
    private final Object[] keys;

    /**
     * Flat value table aligned with {@link #keys}.
     */
    private final Object[] values;

    /**
     * Cached mixed hash codes aligned with {@link #keys} for fast collision rejection.
     */
    private final int[] hashes;

    /**
     * Clock second-chance flags aligned with {@link #keys}.
     */
    private final boolean[] secondChances;

    /** 
     * Current cache size 
     */
    private int size;
    
    /** 
     * Max cache size
     */
    private final int maxSize;

    /** 
     * Mask to calculate the original position 
     * */
    private final int mask;

    /** 
     * Our clock hand, the next position in the array to check for eviction
     */
    private int clockHand = 0;

    /**
     * Creates a new LRU map with the given maximum size.
     * 
     * @param maxSize the maximum number of entries to hold
     */
    public LRUClockMap(final int maxSize)
    {
        if (maxSize < 4)
        {
            throw new IllegalArgumentException( "MaxSize must be at least 4" );
        }
        this.maxSize = maxSize;

        final int capacity = arraySize(maxSize, 0.50f);
        this.mask = capacity - 1;

        this.keys = new Object[capacity];
        this.values = new Object[capacity];
        this.hashes = new int[capacity];
        this.secondChances = new boolean[capacity];
    }

    /**
     * Mix the hash to have a better distribution. 
     * Inspired by OpenJDK HashMap.
     * 
     * @param h the original hash
     * @return the mixed hash
     */
    private int mixHash(final int h) 
    {
        return h ^ (h >>> 16);
    }
    
    /**
     * Read data without touching the clock flag. This is mostly for testing purposes
     * or in case you really don't want to affect the LRU state.
     *
     * @param key what key to look for
     * @return the value for the key or null if not founds
     */
    @SuppressWarnings("unchecked")
    public V getRaw(final K key)
    {
        final int mixedHash = mixHash(key.hashCode());
        int ptr = mixedHash & this.mask;
        
        while (true)
        {
            final Object k = this.keys[ptr];

            if (k == null)
            {
                return null;
            }
            else if (k == key || (this.hashes[ptr] == mixedHash && k.equals(key)))
            {
                return (V) this.values[ptr];
            }
            
            ptr = (ptr + 1) & this.mask;
        }         
    }
    
    /**
     * Get a value from the map. This will set the second chance flag
     * if the entry is found.
     * 
     * @param key what key to look for
     * @return the value for the key or null if not found
     */
    @SuppressWarnings("unchecked")
    public V get(final K key)
    {
        final int mixedHash = mixHash(key.hashCode());
        final int ptr = mixedHash & this.mask;
        final Object k = this.keys[ptr];

        if (k == null)
        {
            return null;
        }

        if (k == key || (this.hashes[ptr] == mixedHash && k.equals(key)))
        {
            if (!this.secondChances[ptr])
            {
                // we have to give it a second chance
                // but only if it is not already set
                // saves a write operation
                this.secondChances[ptr] = true;
            }
            return (V) this.values[ptr];
        }
        else
        {
            // we have to do linear probing now
            // this is the slow path and hence its own method
            // to keep the main get method as lean as possible
            // for better inlining
            return expensiveGet(key, mixedHash, ptr);
        }
    }

    /**
     * We have this code here to make the normal get as lean as possible
     * for better inlining. This is the slow path.
     * 
     * @param key what key to look for
     * @param mixedHash precomputed mixed hash of {@code key}
     * @param ptr the current position in the array
     * @return the value for the key or null if not found
     */
    @SuppressWarnings("unchecked")
    private V expensiveGet(final K key, final int mixedHash, int ptr)
    {
        // we start with the next position because we already checked ptr
        // and missed there
        while (true)
        {
            ptr = (ptr + 1) & this.mask;
            final Object k = this.keys[ptr];

            if (k == null)
            {
                return null;
            }
            
            if (k == key || (this.hashes[ptr] == mixedHash && k.equals(key)))
            {
                if (!this.secondChances[ptr])
                {
                    // we have to give it a second chance
                    // but only if it is not already set
                    // saves a write operation
                    this.secondChances[ptr] = true;
                }
                return (V) this.values[ptr];
            }
            
            // not found, continue searching
        }    
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced. Starts eviction if the max size is reached.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public V put(final K key, final V value)
    {
        final int mixedHash = mixHash(key.hashCode());
        final int ptr = mixedHash & this.mask;
        
        // evict before inserting a new entry helps to avoid 
        // pushing it out again immediately
        if (this.size == maxSize)
        {
            // if this is going to be an insert, we have to evict
            // otherwise just update
            final V oldValue = update(key, value, mixedHash, ptr);
            if (oldValue != null)
            {
                // we just updated an existing entry, no eviction needed
                return oldValue;
            }
            // ok, we have not updated, so we have to kick one out
            evict();
        }
        
        // put the entry, might replace an existing one or be added, we have enough space 
        // at this point for sure
        return putInternal(key, value, mixedHash, ptr);
    }

    /**
     * Internal put method that does not evict
     * 
     * @param key the key to put
     * @param value the value to put
     * @param mixedHash precomputed mixed hash of {@code key}
     * @param ptr the position to start searching
     *
     * @return the old value or null if there was none
     */
    @SuppressWarnings("unchecked")
    private V putInternal(final K key, final V value, final int mixedHash, int ptr)
    {
        while (true)
        {
            final Object currentKey = this.keys[ptr];

            if (currentKey == null)
            {
                // found a free slot
                this.keys[ptr] = key;
                this.values[ptr] = value;
                this.hashes[ptr] = mixedHash;
                this.secondChances[ptr] = true;
                // we added one entry, so size is increased
                this.size++; 
                
                return null;
            }
            else if (currentKey == key || (this.hashes[ptr] == mixedHash && currentKey.equals(key)))
            {
                // key already exists, replace value
                final V oldValue = (V) this.values[ptr];
                this.values[ptr] = value;
                this.secondChances[ptr] = true;

                return oldValue;
            }
            
            ptr = (ptr + 1) & this.mask; // move to next position
        }
    }
    
    /**
     * Internal method to combine get and put functionality but 
     * without insertion because we might have to evict first.
     * 
     * @param key the key to put
     * @param value the value to put
     * @param mixedHash precomputed mixed hash of {@code key}
     * @param ptr the position to start searching
     * 
     * @return the old value or null if there was none
     */
    @SuppressWarnings("unchecked")
    private V update(final K key, final V value, final int mixedHash, int ptr)
    {
        while (true)
        {
            final Object currentKey = this.keys[ptr];

            if (currentKey == null)
            {
                // no entry found, we don't do anything here
                // just tell
                return null;
            }
            else if (currentKey == key || (this.hashes[ptr] == mixedHash && currentKey.equals(key)))
            {
                // key already exists, replace value
                final V oldValue = (V) this.values[ptr];
                this.values[ptr] = value;
                this.secondChances[ptr] = true;

                return oldValue;
            }
            
            ptr = (ptr + 1) & this.mask; // move to next position
        }
    }
    
    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    @SuppressWarnings("unchecked")
    public V remove(final K key)
    {
        final int mixedHash = mixHash(key.hashCode());
        int ptr = mixedHash & this.mask;

        while (true)
        {
            final Object currentKey = this.keys[ptr];

            if (currentKey == null)
            {
                // Key not found
                return null;
            }

            if (currentKey == key || (this.hashes[ptr] == mixedHash && currentKey.equals(key)))
            {
                final V oldValue = (V) this.values[ptr];
                // free this entry and fix array up
                freePositionAndAdjustArray(ptr);
                
                return oldValue;
            }

            ptr = (ptr + 1) & this.mask;
        }
    }
    
    /**
     * Looks for an entry that has not been touched recently and can be evicted.
     * This is not truly LRU but according to common knowledge, close enough and
     * very cheap.
     */
    private void evict()
    {
        while (true)
        {
            if (this.keys[clockHand] != null)
            {
                if (!this.secondChances[clockHand])
                {
                    // free this entry and fix array up
                    freePositionAndAdjustArray(clockHand);
                    return;
                }
                else
                {
                    // gave it a second chance, now this is gone
                    this.secondChances[clockHand] = false;
                }
            }
            
            clockHand = (clockHand + 1) & this.mask; // move to next position
        }
    }

    /**
     * We remove this position from the map. Because we are a linear map, we have 
     * to shift all entries after this position one step to the left until we
     * hit a free position to ensure we don't break the linear chaining.
     * 
     * @param ptr the position to remove
     */
    private void freePositionAndAdjustArray(final int ptr)
    {
        this.keys[ptr] = null;
        this.values[ptr] = null;
        this.size--; // we removed one entry, so size is reduced
       
        // Shift all entries after this position if needed
        int currentPtr = ptr;
        while (true)
        {
            currentPtr = (currentPtr + 1) & this.mask;

            final Object k = this.keys[currentPtr];
            if (k == null)
            {
                break; // no more entries to shift
            }

            final Object v = this.values[currentPtr];
            final int h = this.hashes[currentPtr];
            final boolean sc = this.secondChances[currentPtr];

            this.keys[currentPtr] = null; // free this entry
            this.values[currentPtr] = null;
            realign(k, v, h, sc); // put it back to the map, it will find a new position eventually
        }
    }
    
    /**
     * Internal reput to account for removed entries. We do not have
     * double keys in the map, so we can safely ignore that fact.
     */
    private void realign(final Object k, final Object v, final int mixedHash, final boolean sc)
    {
        int ptr = mixedHash & this.mask;

        while (true)
        {
            if (this.keys[ptr] == null)
            {
                // found a free slot
                this.keys[ptr] = k;
                this.values[ptr] = v;
                this.hashes[ptr] = mixedHash;
                this.secondChances[ptr] = sc;
                return;
            }
            ptr = (ptr + 1) & this.mask; // move to next position
        }
    }
    
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of entries in the map
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Just tell us how big we are, not how many slots we have occupied.
     * @return the size of the holding array
     */
    public int occupiedSpace()
    {
        return this.keys.length;
    }
    
    /**
     * Returns how many slots are really occupied, should always match
     * size but for testing, we expose that.
     * 
     * @return the occupied slot count
     */
    public int trueSize()
    {
        int count = 0;
        for (final Object k : this.keys)
        {
            if (k != null)
            {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns a list of all keys. This is mainly for testing purposes.
     * This does not cause LRU effects. This is in order of the internal array,
     * but without the free slots.
     *
     * @return a list of keys
     */
    @SuppressWarnings("unchecked")
    public List<K> keys()
    {
        final List<K> result = new ArrayList<>(this.size);

        final int length = this.keys.length;
        for (int i = 0; i < length; i++)
        {
            final Object k = this.keys[i];
            if (k != null)
            {
                result.add((K) k);
            }
        }

        return result;
    }

    /**
     * Clears the map, reuses the data structure by clearing it out.
     */
    public void clear()
    {
        Arrays.fill(this.keys, null);
        Arrays.fill(this.values, null);
        this.size = 0;
    }

    /** 
     * Return the least power of two greater than or equal to the specified value.
     *
     * <p>Note that this function will return 1 when the argument is 0.
     *
     * @param x a long integer smaller than or equal to 2<sup>62</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    private static long nextPowerOfTwo(long x) 
    {
        if ( x == 0 ) 
        {
            return 1;
        }
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        
        return (x | x >> 32) + 1;
    }

    /** Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than or equal to <code>Math.ceil( expected / f )</code>.
     *
     * @param expected the expected number of elements in a hash table.
     * @param f the load factor.
     * @return the minimum possible size for a backing array.
     * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
     */
    private static int arraySize( final int expected, final float f ) 
    {
        final long s = Math.max(2, nextPowerOfTwo((long)Math.ceil(expected / f)));
        if (s > (1 << 30)) 
        {
            throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
        }
        return (int)s;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String toString()
    {
        var sj = new StringJoiner(",\n", "LRUClockMap{\n", "\n}");

        final int length = Math.min(this.keys.length, 1024);
        for (int i = 0; i < length; i++)
        {
            final Object k = this.keys[i];
            if (k == null)
            {
                sj.add(i + " FREE");
            }
            else
            {
                sj.add(i + " " + new DebugWrapper<>((K) k, (V) this.values[i], this.secondChances[i], i).toString());
            }
        }
        sj.add("clockHand: " + clockHand);
        sj.add("size: " + size);
        sj.add("maxSize: " + maxSize);
        
        return sj.toString();
    }
    
    class DebugWrapper<DK, DV> 
    {
        public final DK key;
        public final DV value;
        public final boolean secondChance;
        public final int currentPosition;
        public final int truePosition;

        DebugWrapper(final DK key, final DV value, final boolean secondChance, final int currentPosition) 
        {
            this.key = key;
            this.value = value;
            this.secondChance = secondChance;
            this.currentPosition = currentPosition;
            this.truePosition = mixHash(key.hashCode()) & mask;
        }
        
        @Override
        public String toString() 
        {
            return "[" + key.toString() + ", " + value.toString() + ", " + mixHash(key.hashCode()) + ", " + secondChance + ", " + truePosition + "]";
        }
    }
    
    @SuppressWarnings("unchecked")
    List<DebugWrapper<K, V>> getDebugData()
    {
        final var result = new ArrayList<DebugWrapper<K, V>>(this.keys.length);
        for (int i = 0; i < this.keys.length; i++)
        {
            final Object k = this.keys[i];
            if (k == null)
            {
                result.add(null);
            }
            else
            {
                result.add(new DebugWrapper<>((K) k, (V) this.values[i], this.secondChances[i], i));
            }
        }
        
        return result;
    }
}
