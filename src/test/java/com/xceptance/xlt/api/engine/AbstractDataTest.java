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
package com.xceptance.xlt.api.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.util.SimpleArrayList;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Test the implementation of {@link AbstractData}.
 *
 * @author Rene Schwietzke (Xceptance Software Technologies GmbH)
 */
public class AbstractDataTest
{
    /**
     * Type code to use for creating new instances of class AbstractData.
     */
    private static final char TYPECODE = 'X';

    private static class TestData extends AbstractData
    {
        public XltCharBuffer myData;

        public TestData(String name, char typeCode)
        {
            super(name, typeCode);
        }

        public TestData(char typeCode)
        {
            super(typeCode);
        }

        @Override
        protected int getMinNoCSVElements()
        {
            // typeCode, name, time, myData
            return super.getMinNoCSVElements() + 1;
        }

        @Override
        protected void setupRemainingValues(List<XltCharBuffer> values)
        {
            myData = values.get(3);
        }

        @Override
        protected List<String> addValues()
        {
            var l = super.addValues();
            l.add(myData.toString());

            return l;
        }
    }

    private static class MoreTestData extends AbstractData
    {
        public XltCharBuffer myData1;

        public int myData2;

        public MoreTestData(String name, char typeCode)
        {
            super(name, typeCode);
        }

        public MoreTestData(char typeCode)
        {
            super(typeCode);
        }

        @Override
        protected int getMinNoCSVElements()
        {
            // typeCode, name, time, myData
            return super.getMinNoCSVElements() + 2;
        }

        @Override
        protected void setupRemainingValues(List<XltCharBuffer> values)
        {
            myData1 = values.get(3);
            myData2 = ParseNumbers.parseInt(values.get(4));
        }
    }

    // constructor 1
    @Test
    public void ctr1()
    {
        var d = new TestData(TYPECODE);
        assertEquals(TYPECODE, d.getTypeCode());
        assertNull(d.getName());
    }

    // constructor 2
    @Test
    public void ctr2()
    {
        var d = new TestData("Test", TYPECODE);
        assertEquals(TYPECODE, d.getTypeCode());
        assertEquals("Test", d.getName());
    }

    // flexible values can be set
    @Test
    public void dynamicValues()
    {
        var d = new TestData(TYPECODE);
        d.setTransactionName("Transaction");
        d.setName("Name");
        d.setTime(7654345678L);
        d.setAgentName("Agent");

        assertEquals(TYPECODE, d.getTypeCode());
        assertEquals("Name", d.getName());
        assertEquals("Transaction", d.getTransactionName());
        assertEquals("Agent", d.getAgentName());
        assertEquals(7654345678L, d.getTime());
    }

    // additional time set
    @Test
    public void setTime()
    {
        var d = new TestData(TYPECODE);
        d.setTime(123L);
        assertEquals(123L, d.getTime());

        d.setTime(1661700962960L);
        assertTrue(d.getTime() == 1661700962960L);
    }

    // ensure that type code parsed and assume are the same
    @Test
    public void complainTypeCode()
    {
        var l = new SimpleArrayList<XltCharBuffer>(3);
        var data = XltCharBuffer.valueOf("Y,Name,123456789,MyData");

        var d = new TestData(TYPECODE);

        try
        {
            d.baseValuesFromCSV(l, data);
            fail("No exceptioon raised");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("Cannot recreate the object state"));
        }
    }

    // negative time
    @Test
    public void complainNegativeTime()
    {
        var l = new SimpleArrayList<XltCharBuffer>(3);
        var data = XltCharBuffer.valueOf("X,Name,-5,MyData");

        var d = new TestData(TYPECODE);

        try
        {
            d.baseValuesFromCSV(l, data);
            fail("No exceptioon raised");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Invalid time value: -5", e.getMessage());
        }
    }

    // too little fields
    @Test
    public void complainFieldCount()
    {
        var l = new SimpleArrayList<XltCharBuffer>(3);
        var data = XltCharBuffer.valueOf("X,Name,87654345");

        var d = new TestData(TYPECODE);

        try
        {
            d.baseValuesFromCSV(l, data);
            fail("No exceptioon raised");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("Expected at least 4 fields"));
        }
    }

    // initial value parsing
    @Test
    public void base()
    {
        var l = new SimpleArrayList<XltCharBuffer>(3);
        var data = XltCharBuffer.valueOf("X,Name,123456789,MyData");

        var d = new TestData(TYPECODE);
        d.baseValuesFromCSV(l, data);

        assertEquals('X', d.getTypeCode());
        assertEquals("Name", d.getName());
        assertEquals(123456789L, d.getTime());
    }

    // additional data parsing
    @Test
    public void additionalData()
    {
        var l = new SimpleArrayList<XltCharBuffer>(3);
        var data = XltCharBuffer.valueOf("X,Name,123456789,MyData");

        var d = new TestData(TYPECODE);
        d.baseValuesFromCSV(l, data);
        d.remainingValuesFromCSV(l);

        assertEquals('X', d.getTypeCode());
        assertEquals("Name", d.getName());
        assertEquals(123456789L, d.getTime());
        assertEquals("MyData", d.myData.toString());
    }

    // csv record creation
    @Test
    public void toCsv()
    {
        var csv = "X,Name,123456789,MyData";
        var data = XltCharBuffer.valueOf(csv);

        var l = new SimpleArrayList<XltCharBuffer>(4);

        var d = new TestData(TYPECODE);
        d.baseValuesFromCSV(l, data);
        d.remainingValuesFromCSV(l);

        assertEquals(csv, d.toCSV().toString());
    }
}
