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
package com.xceptance.xlt.api.actions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Trivial tests for {@link UnexpectedPageTypeException}.
 * 
 * @author Sebastian Oerding
 */
public class UnexpectedPageTypeExceptionTest
{
    @Test
    public void testStringThrowableConstructor()
    {
        final String message = "UnknownType";
        final String causeMessage = "D'oh";
        final UnexpectedPageTypeException upte = new UnexpectedPageTypeException(message, new IllegalStateException(causeMessage));

        Assert.assertEquals("Message changed by UnexpectedPageTypeException", message, upte.getMessage());
        Assert.assertEquals("Message for cause has been modified by UnexpectedPageTypeException", causeMessage, upte.getCause()
                                                                                                                    .getMessage());
        Assert.assertTrue("Cause has been modified by UnexpectedPageTypeException",
                          upte.getCause().getClass() == IllegalStateException.class);
    }

    @Test
    public void testThrowableConstructor()
    {
        final String causeMessage = "D'oh";
        final UnexpectedPageTypeException upte = new UnexpectedPageTypeException(new IllegalStateException(causeMessage));

        Assert.assertEquals("Message changed by UnexpectedPageTypeException", upte.getCause().toString(), upte.getMessage());
        Assert.assertEquals("Message for cause has been modified by UnexpectedPageTypeException", causeMessage, upte.getCause()
                                                                                                                    .getMessage());
        Assert.assertTrue("Cause has been modified by UnexpectedPageTypeException",
                          upte.getCause().getClass() == IllegalStateException.class);
    }
}
