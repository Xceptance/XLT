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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.api.util.XltCharBufferUtil;

/**
 * @author Sebastian Oerding
 */
public class CustomValueTest
{
    @Test
    public void testAddValues()
    {
        final TestCustomValue value = new TestCustomValue();
        value.setValue(0.0);
        final List<String> strings = value.addValues();
        Assert.assertEquals("Wrong number of values!", 4, strings.size());
        Assert.assertEquals("Wrong double value!", "0.0", strings.get(3));
    }

    @Test
    public void testParseValues()
    {
        final TestCustomValue value = new TestCustomValue();
        value.parseRemainingValues(XltCharBufferUtil.toList(new String[]
            {
                "V", "null", "123000", "0.0"
            }));
        Assert.assertTrue("Wrong double value! Expected 0.0d but got " + value.getValue(), Double.compare(0.0, value.getValue()) == 0);
    }

    @Test
    public void testConstructorWithName()
    {
        final CustomValue cv = new CustomValue("Huhu");
        Assert.assertEquals("Wrong name, ", "Huhu", cv.getName());
    }

    private class TestCustomValue extends CustomValue
    {
        @Override
        protected List<String> addValues()
        {
            return super.addValues();
        }

        @Override
        protected void parseRemainingValues(final List<XltCharBuffer> values)
        {
            super.parseRemainingValues(values);
        }
    }
}
