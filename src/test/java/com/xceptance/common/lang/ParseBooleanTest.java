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
package com.xceptance.common.lang;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBuffer;

public class ParseBooleanTest 
{
    @Test
    public void normalTrue()
    {
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("true")));
    }

    @Test
    public void normalFalse()
    {
        assertFalse(ParseBoolean.parse(XltCharBuffer.valueOf("false")));
        assertFalse(ParseBoolean.parse(XltCharBuffer.valueOf("trueish")));
        assertFalse(ParseBoolean.parse(XltCharBuffer.valueOf("wahr")));
        assertFalse(ParseBoolean.parse(XltCharBuffer.valueOf("")));
    }
    
    @Test
    public void slowpath()
    {
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("TRUE")));
        
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("truE")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("trUe")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("tRue")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("True")));

        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("TRue")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("trUE")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("TruE")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("tRUe")));

        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("TRUe")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("tRUE")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("TRuE")));
        assertTrue(ParseBoolean.parse(XltCharBuffer.valueOf("tRUe")));
    }
    
    @Test(expected = NullPointerException.class)
    public void nullNPE()
    {
        ParseBoolean.parse(null);
    }
}
