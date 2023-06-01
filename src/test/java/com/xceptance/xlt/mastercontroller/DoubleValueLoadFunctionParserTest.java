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
package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link DoubleValueLoadFunctionParser}. For more complex tests, we rely on
 * {@link IntValueLoadFunctionParserTest}.
 */
public class DoubleValueLoadFunctionParserTest
{
    @Test
    public void testParseLoadFunctionSimple() throws ParseException
    {
        final String loadFunctionProperty = "0/1 10/2.0 20/3.33 30/.05 40/.018 45/.0001";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                    {
                        0, 1000
                    },
                    {
                        10, 2000
                    },
                    {
                        20, 3330
                    },
                    {
                        30, 50
                    },
                    {
                        40, 18
                    },
                    {
                        45, 1
                    }
            };
        Assert.assertArrayEquals(expecteds, function);
    }
}
