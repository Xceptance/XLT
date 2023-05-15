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
package com.xceptance.xlt.api.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class SimpleArrayListTest
{
    @Test
    public void create()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        Assert.assertEquals(0, l.size());
    }

    @Test
    public void fill()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        Assert.assertEquals(5, l.size());

        Assert.assertEquals("a", l.get(0));
        Assert.assertEquals("b", l.get(1));
        Assert.assertEquals("c", l.get(2));
        Assert.assertEquals("d", l.get(3));
        Assert.assertEquals("e", l.get(4));

        // no growth
        try
        {
            l.get(5);
            Assert.fail();
        }
        catch(IndexOutOfBoundsException e)
        {
            // yeah... expected
        }
    }

    @Test
    public void fillAndGrow()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(2);
        l.add("a");
        l.add("b");
        Assert.assertEquals(2, l.size());
        l.add("d");
        l.add("e");
        Assert.assertEquals("d", l.get(2));
        Assert.assertEquals("e", l.get(3));
        Assert.assertEquals(4, l.size());

        // limited growth
        try
        {
            l.get(5);
            Assert.fail();
        }
        catch(IndexOutOfBoundsException e)
        {
            // yeah... expected
        }
    }

    @Test
    public void partitionHappy()
    {
        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);

        List<List<Integer>> result = list.partition(3);
        Assert.assertEquals(3, result.size());

        Assert.assertEquals(2, result.get(0).size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(0).get(1));

        Assert.assertEquals(2, result.get(1).size());
        Assert.assertEquals(Integer.valueOf(3), result.get(1).get(0));
        Assert.assertEquals(Integer.valueOf(4), result.get(1).get(1));

        Assert.assertEquals(2, result.get(2).size());
        Assert.assertEquals(Integer.valueOf(5), result.get(2).get(0));
        Assert.assertEquals(Integer.valueOf(6), result.get(2).get(1));
    }

    @Test
    public void partitionSize1()
    {
        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);

        List<List<Integer>> result = list.partition(6);
        Assert.assertEquals(6, result.size());

        Assert.assertEquals(1, result.get(0).size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).get(0));

        Assert.assertEquals(1, result.get(1).size());
        Assert.assertEquals(Integer.valueOf(2), result.get(1).get(0));

        Assert.assertEquals(1, result.get(2).size());
        Assert.assertEquals(Integer.valueOf(3), result.get(2).get(0));

        Assert.assertEquals(1, result.get(3).size());
        Assert.assertEquals(Integer.valueOf(4), result.get(3).get(0));

        Assert.assertEquals(1, result.get(4).size());
        Assert.assertEquals(Integer.valueOf(5), result.get(4).get(0));

        Assert.assertEquals(1, result.get(5).size());
        Assert.assertEquals(Integer.valueOf(6), result.get(5).get(0));
    }

    @Test
    public void partitionEndBucketNotEven()
    {

        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        List<List<Integer>> result = list.partition(3);
        Assert.assertEquals(3, result.size());

        Assert.assertEquals(2, result.get(0).size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(0).get(1));

        Assert.assertEquals(2, result.get(1).size());
        Assert.assertEquals(Integer.valueOf(3), result.get(1).get(0));
        Assert.assertEquals(Integer.valueOf(4), result.get(1).get(1));

        Assert.assertEquals(1, result.get(2).size());
        Assert.assertEquals(Integer.valueOf(5), result.get(2).get(0));
    }

    @Test
    public void partitionCountTooLarge()
    {
        final SimpleArrayList<Integer> list = new SimpleArrayList<>(1);
        list.add(1);
        list.add(2);
        list.add(3);

        List<List<Integer>> result = list.partition(4);
        Assert.assertEquals(3, result.size());

        Assert.assertEquals(1, result.get(0).size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0).get(0));

        Assert.assertEquals(1, result.get(1).size());
        Assert.assertEquals(Integer.valueOf(2), result.get(1).get(0));

        Assert.assertEquals(1, result.get(2).size());
        Assert.assertEquals(Integer.valueOf(3), result.get(2).get(0));

    }

    @Test
    public void clear()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(5);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        Assert.assertEquals(5, l.size());

        Assert.assertEquals("a", l.get(0));
        Assert.assertEquals("b", l.get(1));
        Assert.assertEquals("c", l.get(2));
        Assert.assertEquals("d", l.get(3));
        Assert.assertEquals("e", l.get(4));

        l.clear();

        Assert.assertEquals(0, l.size());

        // we are not checking ranges or such... hence this now works!!!
        Assert.assertEquals("a", l.get(0));

        l.clear();
        Assert.assertEquals(0, l.size());
        l.add("e1");
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("e1", l.get(0));

    }

    @Test 
    public void toArray()
    {
        final SimpleArrayList<String> l = new SimpleArrayList<>(4);
        l.add("a");
        l.add("b");
        l.add("c");
        l.add("d");
        l.add("e");
        Assert.assertEquals(5, l.size());        

        // we will get
        {
            var a = l.toArray();
            Assert.assertEquals(5, a.length);
            Assert.assertEquals("a", a[0]);
            Assert.assertEquals("b", a[1]);
            Assert.assertEquals("c", a[2]);
            Assert.assertEquals("d", a[3]);
            Assert.assertEquals("e", a[4]);
        }
        // we will get
        {
            var a = l.toArray(new String[0]);
            Assert.assertEquals(5, a.length);
            Assert.assertEquals("a", a[0]);
            Assert.assertEquals("b", a[1]);
            Assert.assertEquals("c", a[2]);
            Assert.assertEquals("d", a[3]);
            Assert.assertEquals("e", a[4]);
        }
    }
}
