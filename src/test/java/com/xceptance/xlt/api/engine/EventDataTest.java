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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.xceptance.common.lang.XltCharBuffer;
import com.xceptance.common.util.SimpleArrayList;

/**
 * Test the implementation of {@link EventData}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class EventDataTest extends AbstractDataTest
{
    /**
     * Ctr 1
     */
    @Test
    public void ctr1()
    {
        var e = new EventData();
        assertEquals('E', e.getTypeCode());
        assertNull(e.getName());

        // our own data points
        assertNull(e.getMessage());
        assertNull(e.getTestCaseName());
    }

    /**
     * Ctr 2
     */
    @Test
    public void ctr2()
    {
        var e = new EventData("N1");
        assertEquals('E', e.getTypeCode());
        assertEquals("N1", e.getName());

        // our own data points
        assertNull(e.getMessage());
        assertNull(e.getTestCaseName());
    }

    /**
     * Setter
     */
    @Test
    public void setter()
    {
        final var MSG = "A message";
        final var TC = "MyTestCase";

        var e = new EventData();
        e.setMessage(MSG);
        e.setTestCaseName(TC);

        assertEquals(MSG, e.getMessage());
        assertEquals(TC, e.getTestCaseName());
    }

    /**
     * Base and extended parse
     */
    @Test
    public void incrementalParse()
    {
        var csv = "E,Test42,1602817628282,TCName,A message";
        var e = new EventData();
        var result = new SimpleArrayList<XltCharBuffer>(10);

        e.baseValuesFromCSV(result, XltCharBuffer.valueOf(csv));
        assertEquals('E', e.getTypeCode());
        assertEquals("Test42", e.getName());
        assertEquals(1602817628282L, e.getTime());
        
        // not set yet
        assertNull(e.getMessage());
        assertNull(e.getTestCaseName());        
        
        // ok, now what is really important
        e.remainingFromCSV(result);
        assertEquals("A message", e.getMessage());
        assertEquals("TCName", e.getTestCaseName());
    }
    
    /**
     * Verify valid size
     */
    @Test
    public void size()
    {
        var e = new EventData();
        assertEquals(5, e.getMinNoCSVElements());
    }

    /**
     * CSV roundtrip
     */
    @Test
    public void fromCsv()
    {
        var e = fromCsv("E,Test,1654462372457,TC,Message");
        assertEquals('E', e.getTypeCode());
        assertEquals(1654462372457L, e.getTime());
        assertEquals("Test", e.getName());
        assertEquals("TC", e.getTestCaseName());
        assertEquals("Message", e.getMessage());

        var e2 = fromCsv("E,Test,1654462372458,TC,\"Message with quotes\"");
        assertEquals('E', e2.getTypeCode());
        assertEquals(1654462372458L, e2.getTime());
        assertEquals("Test", e2.getName());
        assertEquals("TC", e2.getTestCaseName());
        assertEquals("Message with quotes", e2.getMessage());
    }

    /**
     * CSV from ctr
     */
    @Test
    public void toCsv()
    {
        var e = new EventData("Test");
        e.setTime(1654462372456L);
        e.setMessage("Message");
        e.setTestCaseName("TC");
        assertEquals("E,Test,1654462372456,TC,Message", e.toCSV());

        var e2 = new EventData("Test");
        e2.setTime(1654462372456L);
        e2.setMessage("Message with ,");
        e2.setTestCaseName("TC");
        assertEquals("E,Test,1654462372456,TC,\"Message with ,\"", e2.toCSV());
    }
    
    /**
     * Just a helper to keep the old test cases alive
     * @param csv
     * @return
     */
    private static EventData fromCsv(final String csv)
    {
        var instance = new EventData();
        var result = new SimpleArrayList<XltCharBuffer>(10);

        instance.baseValuesFromCSV(result, XltCharBuffer.valueOf(csv));
        instance.remainingFromCSV(result);

        return instance;
    }

}
