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

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltRandom;

/**
 * Tests the implementation of {@link LRUList}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class LRUListTest
{

    protected LRUList<Integer> instance;

    protected final int LIST_SIZE = 5;

    @Before
    public void intro()
    {
        instance = new LRUList<Integer>(LIST_SIZE);
    }

    /**
     * Tests the implementation of {@link LRUList#add(Object)} where the capacity of the list is reached by not
     * exceeded.
     */
    @Test
    public void testAdd_NoOverflow()
    {
        final Integer[] ints = new Integer[LIST_SIZE];
        for (int i = 0; i < ints.length; i++)
        {
            ints[i] = XltRandom.nextInt();
        }

        instance.addAll(Arrays.asList(ints));

        for (final Integer i : ints)
        {
            Assert.assertTrue(instance.contains(i));
        }
    }

    /**
     * Tests the implementation of {@link LRUList#add(Object)} where the capacity of the list is exceeded and the most
     * rarely used elements of the list should be dropped.
     */
    @Test
    public void testAdd_Overflow()
    {
        final Integer[] ints = new Integer[LIST_SIZE + 2];
        for (int i = 0; i < ints.length; i++)
        {
            ints[i] = XltRandom.nextInt();
            instance.add(ints[i]);
        }

        Assert.assertFalse(instance.contains(ints[0]));
        Assert.assertFalse(instance.contains(ints[1]));

        for (int i = 2; i < ints.length; i++)
        {
            Assert.assertTrue(instance.contains(ints[i]));
        }
    }

    /**
     * Tests the implementation of {@link LRUList#get(int)} which should reflect LRU behavior: Whenever the given index
     * denotes a valid element index, the appropriate list element becomes the new least recently used element and will
     * be moved to the end of the list.
     */
    @Test
    public void testGet()
    {
        instance.add(0);
        instance.add(1);
        instance.add(2);

        Assert.assertEquals(0, instance.get(0).intValue());
        Assert.assertEquals(1, instance.get(0).intValue());
        Assert.assertEquals(2, instance.get(0).intValue());

        final Iterator<Integer> it = instance.iterator();
        int i = 0;
        while (it.hasNext())
        {
            Assert.assertEquals(i++, it.next().intValue());
        }
    }
}
