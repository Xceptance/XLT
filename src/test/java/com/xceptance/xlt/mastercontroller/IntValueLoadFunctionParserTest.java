/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
 * Tests the implementation of {@link IntValueLoadFunctionParser}.
 */
public class IntValueLoadFunctionParserTest
{
    @Test
    public void testParseLoadFunctionSimple() throws ParseException
    {
        final String loadFunctionProperty = " 1h/1 30m/2,15/3;10s/4 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                    {
                        3600, 1
                    },
                    {
                        1800, 2
                    },
                    {
                        15, 3
                    },
                    {
                        10, 4
                    }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunctionComplex() throws ParseException
    {
        final String loadFunctionProperty = " 1 / 1 30 m / 2 ,15/3;10s/4 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                    {
                        1, 1
                    },
                    {
                        1800, 2
                    },
                    {
                        15, 3
                    },
                    {
                        10, 4
                    }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunctionAwkward() throws ParseException
    {
        final String loadFunctionProperty = " 1h / / 1 30 m / 2 ,,15/3;;10s/4\t ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                    {
                        3600, 1
                    },
                    {
                        1800, 2
                    },
                    {
                        15, 3
                    },
                    {
                        10, 4
                    }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunctionIncomplete0() throws ParseException
    {
        final String loadFunctionProperty = " 0/15 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                    {
                        0, 15
                    }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunctionIncomplete1() throws ParseException
    {
        final String loadFunctionProperty = " 1h ";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunctionIncomplete2() throws ParseException
    {
        final String loadFunctionProperty = " 1h / ";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunctionIncomplete3() throws ParseException
    {
        final String loadFunctionProperty = " 1h / 3;/";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunctionIncomplete4() throws ParseException
    {
        final String loadFunctionProperty = " 1h / 3;/1";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLoadFunctionIncomplete5() throws ParseException
    {
        final String loadFunctionProperty = ",,,,,";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }
}
