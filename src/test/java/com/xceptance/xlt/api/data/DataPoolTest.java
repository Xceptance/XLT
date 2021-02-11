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
package com.xceptance.xlt.api.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link DataPool}.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class DataPoolTest
{
    /**
     * Dummy test data class.
     */
    public static class TestData
    {
        // this can stay empty
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#DataPool(int, int)} .
     */
    @Test
    public final void testDataPool()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 20);

        Assert.assertEquals(10, dataPool.getMax());
        Assert.assertEquals(20, dataPool.getExpireRate());
    }

    @Test
    public void testDefaultConstructor()
    {
        // test default constructor
        final DataPool<TestData> dataPool = new DataPool<TestData>();
        // test adding and polling elements including adding more than the maximum number of elements
        final TestData td = new TestData();
        dataPool.add(td, 51);
        Assert.assertEquals("Wrong number of elements in pool,", 1, dataPool.getSize());
        final TestData td2 = dataPool.getDataElement();
        Assert.assertSame("Wrong element retrieved from pool,", td, td2);
        Assert.assertNull("Data pool should be empty by now!", dataPool.getDataElement());
    }

    @Test
    public void testAddingAndGettingElements()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>();
        // test adding and polling elements including adding more than the maximum number of elements
        dataPool.add(new TestData(), 99);
        Assert.assertEquals("Wrong number of elements in pool,", 1, dataPool.getSize());
        TestData td = null;
        for (int i = 0; i < 11; i++)
        {
            td = new TestData();
            dataPool.add(td, 99);
        }
        Assert.assertEquals("Wrong number of elements in pool,", 10, dataPool.getSize());
        TestData td2 = null;
        for (int i = 0; i < 10; i++)
        {
            td2 = dataPool.getDataElement();
        }
        Assert.assertSame("Wrong element retrieved from pool,", td, td2);
        Assert.assertNull("Data pool should be empty by now!", dataPool.getDataElement());
    }

    @Test
    public void testClear()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>();
        dataPool.add(new TestData(), 99);
        dataPool.clear();
        Assert.assertNull("Data pool should be empty by now!", dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#getDataElement()}.
     */
    @Test
    public final void testGetDataElement_Empty()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 20);

        Assert.assertNull(dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#getDataElement()}.
     */
    @Test
    public final void testGetDataElement_Normal_Single()
    {
        // none expires
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 0);

        final TestData data = new TestData();

        Assert.assertTrue(dataPool.add(data));
        Assert.assertEquals(data, dataPool.getDataElement());

        // null now
        Assert.assertNull(dataPool.getDataElement());

        // put it back and check
        Assert.assertTrue(dataPool.add(data));
        Assert.assertEquals(data, dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#getDataElement()}.
     */
    @Test
    public final void testGetDataElement_Normal_Double()
    {
        // none expires
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 0);

        final TestData data1 = new TestData();
        final TestData data2 = new TestData();

        Assert.assertTrue(dataPool.add(data1));
        Assert.assertTrue(dataPool.add(data2));
        Assert.assertEquals(data1, dataPool.getDataElement());
        Assert.assertEquals(data2, dataPool.getDataElement());

        // null now
        Assert.assertNull(dataPool.getDataElement());

        Assert.assertTrue(dataPool.add(data1));
        Assert.assertTrue(dataPool.add(data2));
        Assert.assertEquals(data1, dataPool.getDataElement());
        Assert.assertTrue(dataPool.add(data1));

        Assert.assertEquals(data2, dataPool.getDataElement());
        Assert.assertEquals(data1, dataPool.getDataElement());

        // null now
        Assert.assertNull(dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#add(java.lang.Object)}.
     */
    @Test
    public final void testAdd_Expire_0()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 0);

        final TestData data1 = new TestData();

        Assert.assertTrue(dataPool.add(data1));
        Assert.assertEquals(data1, dataPool.getDataElement());
        Assert.assertNull(dataPool.getDataElement());

        Assert.assertTrue(dataPool.add(data1));
        Assert.assertEquals(data1, dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#add(java.lang.Object)}.
     */
    @Test
    public final void testAdd_Expire_100()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 100);

        final TestData data1 = new TestData();

        Assert.assertFalse(dataPool.add(data1));
        Assert.assertNull(dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#add(java.lang.Object)}.
     */
    @Test
    public final void testAdd_Expire_90()
    {
        // expire most of the items
        final DataPool<TestData> dataPool = new DataPool<TestData>(3, 90);

        final TestData data1 = new TestData();
        final TestData data2 = new TestData();
        final TestData data3 = new TestData();

        Assert.assertTrue(dataPool.add(data1, 91));
        Assert.assertEquals(data1, dataPool.getDataElement());

        Assert.assertTrue(dataPool.add(data1, 91));
        Assert.assertFalse(dataPool.add(data2, 89));
        Assert.assertFalse(dataPool.add(data3, 90));

        Assert.assertEquals(data1, dataPool.getDataElement());
        Assert.assertNull(dataPool.getDataElement()); // data1 is gone
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#getMax()}.
     */
    @Test
    public final void testAdd_NeverAdd()
    {
        // expire most of the items
        final DataPool<TestData> dataPool = new DataPool<TestData>(3, 90);

        final TestData data1 = new TestData();
        final TestData data2 = new TestData();
        final TestData data3 = new TestData();
        final TestData data4 = new TestData();

        Assert.assertFalse(dataPool.add(data1, 0));
        Assert.assertFalse(dataPool.add(data2, 0));
        Assert.assertFalse(dataPool.add(data3, 0));
        Assert.assertFalse(dataPool.add(data4, 0));

        Assert.assertNull(dataPool.getDataElement());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#getMax()}.
     */
    @Test
    public final void testExpireRate()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>(10, 90);

        Assert.assertEquals(90, dataPool.getExpireRate());

        dataPool.setExpireRate(33);

        Assert.assertEquals(33, dataPool.getExpireRate());
    }

    /**
     * Test method for {@link com.xceptance.xlt.api.data.DataPool#getMax()}.
     */
    @Test
    public final void testConstructorWithNegativeMax()
    {
        final DataPool<TestData> dataPool = new DataPool<TestData>(-10, 90);

        Assert.assertEquals(90, dataPool.getExpireRate());

        dataPool.setExpireRate(33);

        Assert.assertEquals(33, dataPool.getExpireRate());

        dataPool.add(new TestData(), 34);
    }

    /**
     * Test shrinking of data
     */
    @Test
    public void testSetMax()
    {
        // expire most of the items
        final DataPool<String> dataPool = new DataPool<String>(5, 0);
        dataPool.add("1");
        dataPool.add("2");
        dataPool.add("3");
        dataPool.add("4");
        dataPool.add("5");

        dataPool.setMax(3);

        Assert.assertEquals("3", dataPool.getDataElement());
        Assert.assertEquals("4", dataPool.getDataElement());
        Assert.assertEquals("5", dataPool.getDataElement());
        Assert.assertNull(dataPool.getDataElement());

        dataPool.setMax(Integer.MIN_VALUE);
        Assert.assertEquals("Wrong max", 1, dataPool.getMax());
    }
}
