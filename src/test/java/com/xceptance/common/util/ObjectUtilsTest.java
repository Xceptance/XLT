/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Test the implementation of {@link ObjectUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ObjectUtilsTest extends AbstractXLTTestCase
{
    /**
     * Test instance of CanBeSerialized.
     */
    private CanBeSerialized testObject;

    /**
     * Test fixture setup.
     */
    @Before
    public void init()
    {
        testObject = new CanBeSerialized();
    }

    /**
     * Test object cloning.
     */
    @Test
    public void testCloneObject()
    {
        try
        {
            // clone 'testObject'
            final Object o = ObjectUtils.cloneObject(testObject);
            // resulting object must be an instance of CanBeSerialized ...
            Assert.assertTrue(o instanceof CanBeSerialized);

            final CanBeSerialized clone = (CanBeSerialized) o;
            // ... containing the default values of the fields
            Assert.assertEquals("testString", clone.stringField);
            Assert.assertEquals(123, clone.integerField);

        }
        // test failed for any reason
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    /**
     * Private helper class that simply implements the interface {@link java.io.Serializable} along with two dummy
     * fields.
     */
    private static class CanBeSerialized implements Serializable
    {
        /**
         * Serial Version UID.
         */
        static final long serialVersionUID = 1L;

        /**
         * Dummy string field.
         */
        private final String stringField = "testString";

        /**
         * Dummy integer field.
         */
        private final int integerField = 123;

    }
}
