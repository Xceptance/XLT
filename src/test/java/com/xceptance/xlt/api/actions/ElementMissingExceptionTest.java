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
package com.xceptance.xlt.api.actions;

import org.junit.Assert;
import org.junit.Test;

public class ElementMissingExceptionTest
{

    @Test
    public final void testElementMissingException()
    {
        try
        {
            throw new ElementMissingException();
        }
        catch (final ElementMissingException e)
        {
            Assert.assertNull(e.getMessage());
        }
    }

    @Test
    public final void testElementMissingExceptionString()
    {
        try
        {
            throw new ElementMissingException("This is a Test");
        }
        catch (final ElementMissingException e)
        {
            Assert.assertTrue(e.getMessage().equals("This is a Test"));
        }
    }

    @Test
    public final void testElementMissingExceptionThrowable()
    {
        try
        {
            throw new ElementMissingException(new Throwable("Throwable Test"));
        }
        catch (final ElementMissingException e)
        {
            Assert.assertTrue(e.getCause().getMessage().equals("Throwable Test"));
        }
    }

    @Test
    public final void testElementMissingExceptionStringThrowable()
    {
        try
        {
            throw new ElementMissingException("Message", new Throwable("Throwable Test"));
        }
        catch (final ElementMissingException e)
        {
            Assert.assertTrue(e.getMessage().equals("Message"));
            Assert.assertTrue(e.getCause().getMessage().equals("Throwable Test"));
        }
    }

}
