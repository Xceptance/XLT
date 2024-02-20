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

import org.junit.Test;

import com.xceptance.common.util.CsvLineDecoder;

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
        var data = CsvLineDecoder.parse("E,Test42,1602817628282,TCName,A message");

        var e = new EventData();

        e.setBaseValues(data);
        assertEquals('E', e.getTypeCode());
        assertEquals("Test42", e.getName());
        assertEquals(1602817628282L, e.getTime());

        // not set yet
        assertNull(e.getMessage());
        assertNull(e.getTestCaseName());

        // ok, now what is really important
        e.setRemainingValues(data);
        assertEquals("A message", e.getMessage());
        assertEquals("TCName", e.getTestCaseName());
    }
}
