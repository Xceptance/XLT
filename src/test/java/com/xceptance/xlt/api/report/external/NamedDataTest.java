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
package com.xceptance.xlt.api.report.external;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Oerding
 */
@SuppressWarnings("deprecation")
public class NamedDataTest
{
    @Test
    public void testNamedData()
    {
        final String name = "nd1:name";
        final double value = Double.MAX_VALUE;
        final NamedData data = new NamedData(name, value);
        Assert.assertEquals("Wrong name!", name, data.getName());
        Assert.assertEquals("Wrong value!", value, data.getValue(), 0.0);
    }
}
