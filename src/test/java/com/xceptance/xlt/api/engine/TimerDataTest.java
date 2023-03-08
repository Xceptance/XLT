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
package com.xceptance.xlt.api.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Test the implementation of {@link TimerData}.
 *
 * @author Rene Schwietzke(Xceptance Software Technologies GmbH)
 */
public class TimerDataTest extends AbstractDataTest
{
    private static final char TYPECODE = 'T';

    private static class TestData extends TimerData
    {
        public TestData(char typeCode)
        {
            super(typeCode);
        }

        public TestData(String name, char typeCode)
        {
            super(name, typeCode);
        }
    }

    // constructor 1
    @Test
    public void ctr1()
    {
        var d = new TestData(TYPECODE);
        assertEquals(TYPECODE, d.getTypeCode());
        assertNull(d.getName());
        assertEquals(0, d.getRunTime());
        assertFalse(d.hasFailed());
    }

    // constructor 2
    @Test
    public void ctr2()
    {
        var d = new TestData("Test", TYPECODE);
        assertEquals(TYPECODE, d.getTypeCode());
        assertEquals("Test", d.getName());
        assertEquals(0, d.getRunTime());
        assertFalse(d.hasFailed());
    }

    // failed
    @Test
    public void failed()
    {
        var d = new TestData("Test", TYPECODE);
        assertFalse(d.hasFailed());

        d.setFailed(true);
        assertTrue(d.hasFailed());

        d.setFailed(false);
        assertFalse(d.hasFailed());
    }

    // runtime
    @Test
    public void testRuntime()
    {
        var d = new TestData("Test", TYPECODE);
        d.setTime(1654632508330L);
        assertEquals(0, d.getRunTime());
        assertEquals(1654632508330L, d.getEndTime());

        d.setTime(1654632508330L);
        var e = 1654632508330L + 1000L;
        d.setRunTime(1000);
        assertEquals(1000, d.getRunTime());
        assertEquals(1654632508330L + 1000, d.getEndTime());

        d.setRunTime(1001);
        assertEquals(1001, d.getRunTime());
        assertEquals(1654632508330L + 1001, d.getEndTime());
    }

    // parse
    @Test
    public void addValues()
    {
        var d = new TestData("Test", TYPECODE);
        d.setTime(1654632508330L);
        d.setRunTime(1002);
        d.setFailed(true);

        var l = d.addValues();
        assertEquals(String.valueOf(TYPECODE), l.get(0));
        assertEquals("Test", l.get(1));
        assertEquals("1654632508330", l.get(2));
        assertEquals("1002", l.get(3));
        assertEquals("true", l.get(4));

        d.setFailed(false);

        l = d.addValues();
        assertEquals(String.valueOf(TYPECODE), l.get(0));
        assertEquals("Test", l.get(1));
        assertEquals("1654632508330", l.get(2));
        assertEquals("1002", l.get(3));
        assertEquals("false", l.get(4));
    }

    // Serialize
    @Test
    public void parseValues()
    {
        var list = List.of(
                           XltCharBuffer.valueOf(String.valueOf(TYPECODE)),
                           XltCharBuffer.valueOf("Name"),
                           XltCharBuffer.valueOf("1654632508330"),
                           XltCharBuffer.valueOf("666"),
                           XltCharBuffer.valueOf("true")
                           );

        var d = new TestData(TYPECODE);
        d.parseBaseValues(list); // inherited
        d.parseRemainingValues(list);

        assertEquals(TYPECODE, d.getTypeCode());
        assertEquals("Name", d.getName());
        assertEquals(1654632508330L, d.getTime());
        assertEquals(666, d.getRunTime());
        assertTrue(d.hasFailed());
    }

    // Serialize
    @Test
    public void parseValues_negativeRuntime()
    {
        var list = List.of(
                           XltCharBuffer.valueOf(String.valueOf(TYPECODE)),
                           XltCharBuffer.valueOf("Name"),
                           XltCharBuffer.valueOf("1654632508330"),
                           XltCharBuffer.valueOf("-22"),
                           XltCharBuffer.valueOf("true")
                           );

        var d = new TestData(TYPECODE);

        try
        {
            d.parseRemainingValues(list);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Invalid value for the 'runtime' attribute.", e.getMessage());
        }

    }

    // count of records
    @Test
    public void getMinNoCSVElements()
    {
        var d = new TestData("Test", TYPECODE);
        assertEquals(5, d.getMinNoCSVElements());
    }
}
