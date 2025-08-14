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
    public void testParseLoadFunction_absoluteFunction() throws ParseException
    {
        final String loadFunctionProperty = "0/1 10/2.0 20/3.33 30/.05 40/.018 45/.0001 50/3";
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
                },
                {
                    50, 3000
                }
            };
        Assert.assertArrayEquals(expecteds, function);
    }

    @Test
    public void testParseLoadFunction_relativeFunction() throws ParseException
    {
        final String loadFunctionProperty = "0/1 +10/+2.0 +20/+3.33 +30/-.05 +40/+.018 +45/-.0001 +50/+3";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                {
                    (0), (1000)
                },
                {
                    (0 + 10), (1000 + 2000)
                },
                {
                    (0 + 10 + 20), (1000 + 2000 + 3330)
                },
                {
                    (0 + 10 + 20 + 30), (1000 + 2000 + 3330 - 50)
                },
                {
                    (0 + 10 + 20 + 30 + 40), (1000 + 2000 + 3330 - 50 + 18)
                },
                {
                    (0 + 10 + 20 + 30 + 40 + 45), (1000 + 2000 + 3330 - 50 + 18 - 1)
                },
                {
                    (0 + 10 + 20 + 30 + 40 + 45 + 50), (1000 + 2000 + 3330 - 50 + 18 - 1 + 3000)
                }
            };
        Assert.assertArrayEquals(expecteds, function);
    }

    @Test
    public void testParseLoadFunction_extraWhitespaces() throws ParseException
    {
        final String loadFunctionProperty = "0  /  1  +  10  /  2.0   20  /  +  3.33  +  30  /  -  .05  40  /  .018";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                {
                    (0), (1000)
                },
                {
                    (0 + 10), (2000)
                },
                {
                    (20), (2000 + 3330)
                },
                {
                    (20 + 30), (2000 + 3330 - 50)
                },
                {
                    (40), (18)
                }
            };
        Assert.assertArrayEquals(expecteds, function);
    }

    @Test
    public void testParseLoadFunction_differentSeparators() throws ParseException
    {
        final String loadFunctionProperty = "0  /  /  1 ,;  , +  10  / \t //  2.0 ;  ;  ;  ;  20  ////  +  3.33  , , , ,  +  30  /  /  /  -  .05  40  /  .018";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                {
                    (0), (1000)
                },
                {
                    (0 + 10), (2000)
                },
                {
                    (20), (2000 + 3330)
                },
                {
                    (20 + 30), (2000 + 3330 - 50)
                },
                {
                    (40), (18)
                }
            };
        Assert.assertArrayEquals(expecteds, function);
    }

    @Test
    public void testParseLoadFunction_highPrecisionValues_absoluteFunction() throws ParseException
    {
        final String loadFunctionProperty = " 0/0.000000001 1m/2.000000001 2m/0.99999999";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                {
                    0, 1
                },
                {
                    60, 2001
                },
                {
                    120, 1000
                }
            };
        Assert.assertArrayEquals(expecteds, function);
    }

    @Test
    public void testParseLoadFunction_highPrecisionValues_relativeFunction() throws ParseException
    {
        final String loadFunctionProperty = " 0/+0.000000001 +1m/+2.000000001 +1m/-0.99999999";
        final int[][] function = new DoubleValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expecteds = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1)
                },
                {
                    (LoadFunctionUtils.START_TIME + 60), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1 + 2001)
                },
                {
                    (LoadFunctionUtils.START_TIME + 60 + 60), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1 + 2001 - 1000)
                }
            };
        Assert.assertArrayEquals(expecteds, function);
    }

    @Test
    public void testParseValue() throws ParseException
    {
        final DoubleValueLoadFunctionParser parser = new DoubleValueLoadFunctionParser();
        Assert.assertEquals(0, parser.parseValue("0"));
        Assert.assertEquals(12000, parser.parseValue("12"));
        Assert.assertEquals(1234, parser.parseValue("1.234"));
        Assert.assertEquals(1001, parser.parseValue("1.0002"));
        Assert.assertEquals(1234001, parser.parseValue("1234.0000000567"));

        Assert.assertEquals(-0, parser.parseValue("-0"));
        Assert.assertEquals(-12000, parser.parseValue("-12"));
        Assert.assertEquals(-1234, parser.parseValue("-1.234"));
        Assert.assertEquals(-1001, parser.parseValue("-1.0002"));
        Assert.assertEquals(-1234001, parser.parseValue("-1234.0000000567"));
    }
}
