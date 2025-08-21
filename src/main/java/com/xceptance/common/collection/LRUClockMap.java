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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * This is a simple LRU map implementation based on a clock algorithm.
 * Not thread-safe! Null support was removed. The LRU part is not really 
 * predictable, but it is fast and simple. We don't keep a linked list, 
 * rather a marker that give every entry a second chance. 
 * If the second chance is false, we can evict this entry. This can happen
 * rather early. In general, it is still a good enough LRU implementation.
 * 
 * To save operations and have less state and memory writes, we are using the
 * current insert position in the array as a clock hand. The clock hand is moved
 * to the right untl we find an entry that has no second chance. 
 * 
 * https://www.geeksforgeeks.org/operating-systems/second-chance-or-clock-page-replacement-policy/
 *
 * @since 9.1.0
 */
public class LRUClockMap<K, V>
{
    private final Wrapper<K, V> FREE_KEY = null;

    /**
     * Keys and values       
     */
    private Wrapper<K, V>[] data;

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
    private int mask;

    /** 
     * Our clock hand, the current position in the array
     */
    private int clockHand = -1;
    
    /**
     * Wrapper class for key, value, and secondChance bit.
     * The secondChance is true to skip it when the clock 
     * is checked. If this is false, we can dispose this entry.
     */
    private static class Wrapper<K, V> 
    {
        final K key;
        V value;
        boolean secondChance;

        Wrapper(K key, V value) 
        {
            this.key = key;
            this.value = value;
            // don't give up on a new entry so easily
            this.secondChance = true;
        }

        @Override
        public int hashCode() 
        {
            return key.hashCode();
        }
        
        @Override
        public String toString() 
        {
            return "[" + key.toString() + ", " + value.toString() + ", " + secondChance + "]";
        }
    }

    @SuppressWarnings("unchecked")
    public LRUClockMap(final int maxSize)
    {
        if (maxSize < 4)
        {
            throw new IllegalArgumentException( "MaxSize must be at least 4" );
        }
        this.maxSize = maxSize;

        final int capacity = arraySize(maxSize, 0.3333f);
        this.mask = capacity - 1;

        this.data = (Wrapper<K, V>[]) new Wrapper[capacity];
    }

    /**
     * Read data without touching the clock flag. This is mostly for testing purposes.
     *
     * @param key
     * @return
     */
    public V getNoLRU(final K key)
    {
        int ptr = mixHash(key.hashCode()) & this.mask;
        while (true)
        {
            final Wrapper<K, V> w = this.data[ptr];

            if (w == FREE_KEY)
            {
                return null;
            }
            else if (w.key.equals(key))
            {
                return w.value;
            }
            ptr = (ptr + 1) & this.mask;
        }         
    }
    
    public V get(final K key)
    {
        int ptr = mixHash(key.hashCode()) & this.mask;
        final Wrapper<K, V> w = this.data[ptr];

        if (w == FREE_KEY)
        {
            return null;
        }

        if (w.key.equals(key))
        {
            if (!w.secondChance)
            {
                // we have to give it a second chance
                // but only if it is not already set
                // saves a write operation
                w.secondChance = true;
            }
            return w.value;
        }
        else
        {
            // we have to do linear probing now
            return expensiveGet(key, ptr);
        }
    }

    /**
     * We have this code here to make the normal get as lean as possible
     * for better inlining. This is the slow path.
     * 
     * @param key what key to look for
     * @param ptr the current position in the array
     * @return the value for the key or null if not found
     */
    private V expensiveGet(final K key, int ptr)
    {
        while (true)
        {
            ptr = (ptr + 1) & this.mask;
            final Wrapper<K, V> w = this.data[ptr];

            if (w == FREE_KEY)
            {
                return null;
            }
            else if (w.key.equals(key))
            {
                if (!w.secondChance)
                {
                    // we have to give it a second chance
                    // but only if it is not already set
                    // saves a write operation
                    w.secondChance = true;
                }
                return w.value;
            }
        }    
    }

    private int mixHash(int h) 
    {

//        h = (h * 0x9e3779b9);
        h = h ^ (h >>> 16);
//
//        return h;
//        
//        h += (h << 13);
//        h ^= (h >>> 12);
//        h += (h << 3);
//        h ^= (h >>> 17);
//        h += (h << 5);
//        h ^= (h >>> 10);

        return h;
        
//        return h;
        
//        return h ^ (h >>> 16); // simple hash mixing
        
//        h ^= (h >>> 16);
//        h *= 0x85ebca6b; 
//        h ^= (h >>> 13);
//        h *= 0xc2b2ae35;
//        h ^= (h >>> 16);
//        return h;
    }
    
    public V put(final K key, final V value)
    {
        int ptr = mixHash(key.hashCode()) & this.mask;

        while (true)
        {
            ptr = ptr & this.mask;
            final Wrapper<K, V> w = this.data[ptr];

            if (w == FREE_KEY)
            {
                this.size++;
                this.data[ptr] = new Wrapper<>(key, value);

                if (this.size > maxSize)
                {
                    // we are full, so we need to evict
                    // we start at the current position
                    // so we don't need a clockhand 
                    // no prove that this shortcut works as 
                    // good as a real clock hand, but it seems to
                    // make sense and we have less memory to write to
                    evict();
                }

                return null;
            }

            if (w.key.equals(key))
            {
                final V oldValue = w.value;
                w.value = value;
                w.secondChance = true; // give it a second chance
                return oldValue;
            }

            ptr++;
        }
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public V remove(final K key)
    {
        int ptr = mixHash(key.hashCode()) & this.mask;

        while (true)
        {
            final Wrapper<K, V> w = this.data[ptr];

            if (w == FREE_KEY)
            {
                // Key not found
                return null;
            }

            if (w.key.equals(key))
            {
                final V oldValue = w.value;
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
            // ok, next pos
            clockHand = (clockHand + 1) & this.mask;

            final Wrapper<K, V> w = this.data[clockHand];
            if (w == FREE_KEY)
            {
                continue; // skip this entry, no real data
            }

            if (w.secondChance == false)
            {
                // free this entry and fix array up
                freePositionAndAdjustArray(clockHand);
                return;
            }

            // lost the second chance, might be evicted next time
            w.secondChance = false;
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
        this.data[ptr] = FREE_KEY;
        this.size--; // we removed one entry, so size is reduced
       
        // Shift all entries after this position if needed
        int currentPtr = ptr;
        while (true)
        {
            currentPtr = (currentPtr + 1) & this.mask;

            final Wrapper<K, V> w = this.data[currentPtr];
            if (w == FREE_KEY)
            {
                break; // no more entries to shift
            }
            
            this.data[currentPtr] = FREE_KEY; // free this entry
            realign(w); // put it back to the map, it will find a new position eventually
        }
    }
    
    /**
     * Internal put to account for removed entries. We do not have
     * double keys in the map, so we can safely ignore that fact.
     * 
     * @param entry the wrapper to put
     */
    private void realign(final Wrapper<K, V> entry)
    {
        int ptr = mixHash(entry.key.hashCode()) & this.mask;

        while (true)
        {
            final Wrapper<K, V> current = this.data[ptr];

            if (current == FREE_KEY)
            {
                // found a free slot
                this.data[ptr] = entry;
                return;
            }
            ptr = (ptr + 1) & this.mask; // move to next position
        }
    }
    
    public int size()
    {
        return this.size;
    }

    /**
     * Returns how many slots are really occupied, should always match
     * size but for testing, we expose that.
     * 
     * @return the occupied slots
     */
    public int trueSize()
    {
        int count = 0;
        for (var w : this.data)
        {
            if (w != null)
            {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns a list of all keys. This is mainly for testing purposes.
     *
     * @return a list of keys
     */
    public List<K> keys()
    {
        final List<K> result = new ArrayList<>();

        final int length = this.data.length;
        for (int i = 0; i < length; i++)
        {
            final Wrapper<K, V> w = this.data[i];
            if (w != FREE_KEY)
            {
                result.add(w.key);
            }
        }

        return result;
    }

    /**
     * Clears the map, reuses the data structure by clearing it out.
     */
    public void clear()
    {
        Arrays.fill(data, FREE_KEY);
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
    public static long nextPowerOfTwo(long x) 
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
    public static int arraySize( final int expected, final float f ) 
    {
        final long s = Math.max(2, nextPowerOfTwo((long)Math.ceil(expected / f)));
        if (s > (1 << 30)) 
        {
            throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
        }
        return (int)s;
    }
    
    @Override
    public String toString()
    {
        var sj = new StringJoiner(",\n", "LRUClockMap{\n", "\n}");

        final int length = Math.min(this.data.length, 1024);
        for (int i = 0; i < length; i++)
        {
            final Wrapper<K, V> w = this.data[i];
            if (w == FREE_KEY)
            {
                sj.add(i + " FREE");
            }
            else
            {
                sj.add(i + " " + new DebugWrapper<K, V>(w, i).toString());
            }
        }
        
        return sj.toString();
    }
    
    public class DebugWrapper<DK, DV> 
    {
        public final DK key;
        public final DV value;
        public final boolean secondChance;
        public final int currentPosition;
        public final int truePosition;

        DebugWrapper(Wrapper<DK, DV> wrapper, int currentPosition) 
        {
            this.key = wrapper.key;
            this.value = wrapper.value;
            this.secondChance = wrapper.secondChance;
            this.currentPosition = currentPosition; // will be set later
            this.truePosition = mixHash(wrapper.key.hashCode()) & mask; // calculate the true position
        }
        
        @Override
        public String toString() 
        {
            return "[" + key.toString() + ", " + value.toString() + ", " + mixHash(key.hashCode()) + ", " + secondChance + ", " + truePosition + "]";
        }
    }
    
    public List<DebugWrapper<K, V>> getDebugData()
    {
        final var result = new ArrayList<DebugWrapper<K, V>>(this.data.length);
        for (int i = 0; i < this.data.length; i++)
        {
            final Wrapper<K, V> w = this.data[i];
            if (w == FREE_KEY)
            {
                result.add(null);
            }
            else
            {
                result.add(new DebugWrapper<>(w, i));
            }
        }
        
        return result;
    }
}
