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
package com.xceptance.xlt.api.engine;

import org.junit.Assert;
import org.junit.Test;

public class CustomDataTest
{

    @Test
    public final void testCustomData()
    {
        final CustomData c = new CustomData();
        Assert.assertEquals('C', c.getTypeCode());
    }

    @Test
    public final void testCustomDataString()
    {
        final CustomData c = new CustomData("Test99");
        Assert.assertEquals('C', c.getTypeCode());
        Assert.assertEquals("Test99", c.getName());
    }

}
