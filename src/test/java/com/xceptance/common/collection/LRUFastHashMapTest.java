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

import org.junit.Test;

/**
 * Test our special fast hashmap
 * @author Rene Schwietzke
 *
 * @since 7.0.0
 */
public class LRUFastHashMapTest
{
    /**
     * Happy path
     */
    @Test
    public void happyPath()
    {
        var lru = new LRUFastHashMap<String, String>(6);
        lru.put("k11", "11");
        lru.put("k12", "12");

        lru.put("k21", "21");
        lru.put("k22", "22");

        lru.put("k31", "31");
        lru.put("k32", "32");

        // levels are filled, we now push the three oldest out
        lru.put("k41", "41");
        lru.put("k42", "42");

        assertNull(lru.get("k11"));
        assertNull(lru.get("k12"));

        // rest is still there
        assertEquals("41", lru.getAndNoUpdate("k41"));
        assertEquals("42", lru.getAndNoUpdate("k42"));

        assertEquals("31", lru.getAndNoUpdate("k31"));
        assertEquals("32", lru.getAndNoUpdate("k32"));

        assertEquals("21", lru.getAndNoUpdate("k21"));
        assertEquals("22", lru.getAndNoUpdate("k22"));

        assertEquals(6, lru.size());
    }

    /**
     * Lru the oldest
     */
    @Test
    public void recoverOldest()
    {
        var lru = new LRUFastHashMap<String, String>(6);
        lru.put("k11", "11");
        lru.put("k12", "12");

        lru.put("k21", "21");
        lru.put("k22", "22");

        lru.put("k31", "31");
        lru.put("k32", "32");

        // save oldest
        assertEquals("11", lru.get("k11"));
        assertNull(lru.get("k12")); // we lost this when the segment dropped due to k11 refresh

        // push out
        lru.put("k41", "41"); // will hit slot 1
        lru.put("k42", "42"); // will move 41 to slot 2 and drop k21 and k22

        assertNull(lru.getAndNoUpdate("k21"));
        assertNull(lru.getAndNoUpdate("k22"));

        // rest is still there
        assertEquals("41", lru.getAndNoUpdate("k41"));
        assertEquals("42", lru.getAndNoUpdate("k42"));

        assertEquals("31", lru.getAndNoUpdate("k31"));
        assertEquals("32", lru.getAndNoUpdate("k32"));

        assertEquals("11", lru.getAndNoUpdate("k11"));

        assertEquals(5, lru.size());
    }

    /**
     * Lru the middle age
     */
    @Test
    public void recoverMiddleAge()
    {
        var lru = new LRUFastHashMap<String, String>(6);
        lru.put("k11", "11");
        lru.put("k12", "12");

        lru.put("k21", "21");
        lru.put("k22", "22");

        lru.put("k31", "31");
        lru.put("k32", "32");

        // save
        assertEquals("21", lru.get("k21"));
        assertEquals("22", lru.get("k22"));

        // push out
        lru.put("k41", "41"); // will hit slot 1
        lru.put("k42", "42"); //

        assertNull(lru.getAndNoUpdate("k11"));
        assertNull(lru.getAndNoUpdate("k12"));

        // rest is still there
        assertEquals("41", lru.getAndNoUpdate("k41"));
        assertEquals("42", lru.getAndNoUpdate("k42"));

        assertEquals("31", lru.getAndNoUpdate("k31"));
        assertEquals("32", lru.getAndNoUpdate("k32"));

        assertEquals("21", lru.getAndNoUpdate("k21"));
        assertEquals("22", lru.getAndNoUpdate("k22"));

        assertEquals(6, lru.size());
    }

    /**
     * Lru the youngest
     */
    @Test
    public void recoverYoungest()
    {
        var lru = new LRUFastHashMap<String, String>(6);
        lru.put("k11", "11");
        lru.put("k12", "12");

        lru.put("k21", "21");
        lru.put("k22", "22");

        lru.put("k31", "31");
        lru.put("k32", "32");

        // save
        assertEquals("31", lru.get("k31"));
        assertEquals("32", lru.get("k32"));

        // push out
        lru.put("k41", "41"); // will hit slot 1 and make 11 and 12 drop
        lru.put("k42", "42");

        assertNull(lru.getAndNoUpdate("k11"));
        assertNull(lru.getAndNoUpdate("k12"));

        // rest is still there
        assertEquals("41", lru.getAndNoUpdate("k41"));
        assertEquals("42", lru.getAndNoUpdate("k42"));

        assertEquals("31", lru.getAndNoUpdate("k31"));
        assertEquals("32", lru.getAndNoUpdate("k32"));

        assertEquals("21", lru.getAndNoUpdate("k21"));
        assertEquals("22", lru.getAndNoUpdate("k22"));

        assertEquals(6, lru.size());
    }

    /**
     * Ensure get without LRU operation if desired
     */
    @Test
    public void getWithoutLRU()
    {
        var lru = new LRUFastHashMap<String, String>(9);
        lru.put("k11", "1");
        lru.put("k12", "2");
        lru.put("k13", "3");

        lru.put("k21", "4");
        lru.put("k22", "5");
        lru.put("k23", "6");

        lru.put("k31", "7");
        lru.put("k32", "8");
        lru.put("k33", "9");

        // read oldest but we don't refresh it, hence it still drops next
        assertEquals("1", lru.getAndNoUpdate("k11"));
        assertEquals("2", lru.getAndNoUpdate("k12"));
        assertEquals("3", lru.getAndNoUpdate("k13"));

        // levels are filled, we now push the level 1 out
        lru.put("k41", "10");
        lru.put("k42", "11");
        lru.put("k43", "11");

        assertNull(lru.get("k11"));
        assertNull(lru.get("k12"));
        assertNull(lru.get("k13"));
    }
}
