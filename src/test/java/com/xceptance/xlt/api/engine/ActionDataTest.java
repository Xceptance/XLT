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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the implementation of {@link ActionData}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ActionDataTest extends TimerDataTest
{

    /**
     * ActionData test instance.
     */
    private ActionData instance = null;

    /**
     * Test initialization.
     */
    @Before
    public void setupActionDataInstance()
    {
        instance = new ActionData("Test");
    }

    /**
     * Tests the proper CSV encoding of the type code.
     */
    @Test
    public void testTypeCode()
    {
        Assert.assertTrue(instance.toCSV().startsWith(String.valueOf(instance.getTypeCode())));
    }

    @Test
    public void testDefaultConstructor()
    {
        instance = new ActionData();
        Assert.assertEquals("Wrong name, ", null, instance.getName());
    }
}
