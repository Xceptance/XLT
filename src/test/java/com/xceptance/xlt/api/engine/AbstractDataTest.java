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
package com.xceptance.xlt.api.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.common.util.CsvLineDecoder;
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
        public void setRemainingValues(List<XltCharBuffer> values)
        {
            myData = values.get(3);
        }

        @Override
        public List<String> toList()
        {
            var l = super.toList();
            l.add(myData.toString());

            return l;
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
        var data = CsvLineDecoder.parse("Y,Name,123456789,MyData");

        var d = new TestData(TYPECODE);

        try
        {
            d.setBaseValues(data);
            fail("No exception raised");
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
        var data = CsvLineDecoder.parse("X,Name,-5,MyData");

        var d = new TestData(TYPECODE);

        try
        {
            d.setBaseValues(data);
            fail("No exception raised");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Invalid time value: -5", e.getMessage());
        }
    }

    // too little fields
    @Ignore("At the moment, SimpleArrayList does not make range checks for performance reasons")
    @Test(expected = IndexOutOfBoundsException.class)
    public void complainFieldCount()
    {
        var data = CsvLineDecoder.parse("X,Name,87654345");

        var d = new TestData(TYPECODE);

        d.setBaseValues(data);
        d.setRemainingValues(data);
    }

    // initial value parsing
    @Test
    public void base()
    {
        var data = CsvLineDecoder.parse("X,Name,123456789,MyData");

        var d = new TestData(TYPECODE);
        d.setBaseValues(data);

        assertEquals('X', d.getTypeCode());
        assertEquals("Name", d.getName());
        assertEquals(123456789L, d.getTime());
        assertNull(d.myData);
    }

    // additional data parsing
    @Test
    public void additionalData()
    {
        var data = CsvLineDecoder.parse("X,Name,123456789,MyData");

        var d = new TestData(TYPECODE);
        d.setBaseValues(data);
        d.setRemainingValues(data);

        assertEquals('X', d.getTypeCode());
        assertEquals("Name", d.getName());
        assertEquals(123456789L, d.getTime());
        assertEquals("MyData", d.myData.toString());
    }
}
